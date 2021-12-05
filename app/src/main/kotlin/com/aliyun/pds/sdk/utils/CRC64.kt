/*
 *  Copyright 2009-2021 Alibaba Cloud All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.aliyun.pds.sdk.utils

import java.io.*
import java.lang.Exception
import java.math.BigInteger
import java.util.zip.Checksum

/**
 * CRC-64 implementation with ability to combine checksums calculated over
 * different blocks of data. Standard ECMA-182,
 * http://www.ecma-international.org/publications/standards/Ecma-182.htm
 */
class CRC64 : Checksum {
    /* Current CRC value. */
    private var value: Long

    companion object {

        // 0xc96c5795d7870f42L; // ECMA-182
        private const val POLY = -0x3693a86a2878f0beL // ECMA-182

        /* CRC64 calculation table. */
        private val table: LongArray


        fun fromFile(f: File?): CRC64? {
            var `in`: InputStream? = null
            try {
                `in` = FileInputStream(f)
                val crc = CRC64()
                val b = ByteArray(1024 * 10)
                var l = 0
                while (`in`.read(b).also { l = it } != -1) {
                    crc.update(b, l)
                }
                return crc
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    `in`!!.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return null
        }

        private const val GF2_DIM = 64 /*
     * dimension of GF(2) vectors (length
     * of CRC)
     */
        private fun gf2MatrixTimes(mat: LongArray, vec: Long): Long {
            var vec = vec
            var sum: Long = 0
            var idx = 0
            while (vec != 0L) {
                if (vec and 1 == 1L) sum = sum xor mat[idx]
                vec = vec ushr 1
                idx++
            }
            return sum
        }

        private fun gf2MatrixSquare(square: LongArray, mat: LongArray) {
            for (n in 0 until GF2_DIM) square[n] = gf2MatrixTimes(mat,
                mat[n])
        }

        /*
     * Return the CRC-64 of two sequential blocks, where summ1 is the CRC-64 of
     * the first block, summ2 is the CRC-64 of the second block, and len2 is the
     * length of the second block.
     */
        fun combine(summ1: CRC64, summ2: CRC64, len2: Long): CRC64 {
            // degenerate case.
            var len2 = len2
            if (len2 == 0L) return CRC64(summ1.getValue())
            var n: Int
            var row: Long
            val even = LongArray(GF2_DIM) // even-power-of-two zeros operator
            val odd = LongArray(GF2_DIM) // odd-power-of-two zeros operator

            // put operator for one zero bit in odd
            odd[0] = POLY // CRC-64 polynomial
            row = 1
            n = 1
            while (n < GF2_DIM) {
                odd[n] = row
                row = row shl 1
                n++
            }

            // put operator for two zero bits in even
            gf2MatrixSquare(even, odd)

            // put operator for four zero bits in odd
            gf2MatrixSquare(odd, even)

            // apply len2 zeros to crc1 (first square will put the operator for one
            // zero byte, eight zero bits, in even)
            var crc1 = summ1.getValue()
            val crc2 = summ2.getValue()
            do {
                // apply zeros operator for this bit of len2
                gf2MatrixSquare(even, odd)
                if (len2 and 1 == 1L) crc1 = gf2MatrixTimes(even, crc1)
                len2 = len2 ushr 1

                // if no more bits set, then done
                if (len2 == 0L) break

                // another iteration of the loop with odd and even swapped
                gf2MatrixSquare(odd, even)
                if (len2 and 1 == 1L) crc1 = gf2MatrixTimes(odd, crc1)
                len2 = len2 ushr 1

                // if no more bits set, then done
            } while (len2 != 0L)

            // return combined crc.
            crc1 = crc1 xor crc2
            return CRC64(crc1)
        }

        /*
     * Return the CRC-64 of two sequential blocks, where summ1 is the CRC-64 of
     * the first block, summ2 is the CRC-64 of the second block, and len2 is the
     * length of the second block.
     */
        fun combine(crc1: Long, crc2: Long, len2: Long): Long {
            // degenerate case.
            var crc1 = crc1
            var len2 = len2
            if (len2 == 0L) return crc1
            var n: Int
            var row: Long
            val even = LongArray(GF2_DIM) // even-power-of-two zeros operator
            val odd = LongArray(GF2_DIM) // odd-power-of-two zeros operator

            // put operator for one zero bit in odd
            odd[0] = POLY // CRC-64 polynomial
            row = 1
            n = 1
            while (n < GF2_DIM) {
                odd[n] = row
                row = row shl 1
                n++
            }

            // put operator for two zero bits in even
            gf2MatrixSquare(even, odd)

            // put operator for four zero bits in odd
            gf2MatrixSquare(odd, even)

            // apply len2 zeros to crc1 (first square will put the operator for one
            // zero byte, eight zero bits, in even)
            do {
                // apply zeros operator for this bit of len2
                gf2MatrixSquare(even, odd)
                if (len2 and 1 == 1L) crc1 = gf2MatrixTimes(even, crc1)
                len2 = len2 ushr 1

                // if no more bits set, then done
                if (len2 == 0L) break

                // another iteration of the loop with odd and even swapped
                gf2MatrixSquare(odd, even)
                if (len2 and 1 == 1L) crc1 = gf2MatrixTimes(odd, crc1)
                len2 = len2 ushr 1

                // if no more bits set, then done
            } while (len2 != 0L)

            // return combined crc.
            crc1 = crc1 xor crc2
            return crc1
        }

        init {
            table = LongArray(256)
            for (n in 0..255) {
                var crc = n.toLong()
                for (k in 0..7) {
                    crc = if (crc and 1 == 1L) {
                        crc ushr 1 xor POLY
                    } else {
                        crc ushr 1
                    }
                }
                table[n] = crc
            }
        }
    }

    constructor() {
        value = 0
    }

    constructor(value: Long) {
        this.value = value
    }

    constructor(b: ByteArray, len: Int) {
        value = 0
        update(b, len)
    }

    /**
     * Get 8 byte representation of current CRC64 value.
     */
    val bytes: ByteArray
        get() {
            val b = ByteArray(8)
            for (i in 0..7) {
                b[7 - i] = (value ushr i * 8).toByte()
            }
            return b
        }

    /**
     * Get long representation of current CRC64 value.
     */
    override fun getValue(): Long {
        return value
    }

    val stringValue: String
        get() = if (value > 0) {
            value.toString()
        } else {
            val b1 = BigInteger(value.toString())
            b1.add(BigInteger.ONE.shiftLeft(64)).toString()
        }

    /**
     * Update CRC64 with new byte block.
     */
    fun update(b: ByteArray, len: Int) {
        var len = len
        var idx = 0
        value = value.inv()
        while (len > 0) {
            value = table[((value xor b[idx].toLong()).toInt()) and 0xff] xor (value ushr 8)
            idx++
            len--
        }
        value = value.inv()
    }

    /**
     * Update CRC64 with new byte.
     */
    fun update(b: Byte) {
        value = value.inv()
        value = table[((value xor b.toLong()).toInt()) and 0xff] xor (value ushr 8)
        value = value.inv()
    }

    override fun update(b: Int) {
        update((b and 0xFF).toByte())
    }

    override fun update(b: ByteArray, off: Int, len: Int) {
        var len = len
        var i = off
        while (len > 0) {
            update(b[i++])
            len--
        }
    }

    override fun reset() {
        value = 0
    }
}