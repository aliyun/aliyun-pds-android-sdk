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