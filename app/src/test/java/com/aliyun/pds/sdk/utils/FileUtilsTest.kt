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

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mockito
import java.io.File


@RunWith(MockitoJUnitRunner::class)
class FileUtilsTest {

    private val testName1 = "abc.txt"
    private val testName2 = "0123456789.txt"
    private val testName3 = "01234567890123456789.txt"
    private val testName4 = "test4"

    @Test
    fun renameByLengthTest() {
        val new1 = FileUtils.instance.renameByLength(8, testName1)
        assert(new1 == testName1)

        val new2 = FileUtils.instance.renameByLength(8, testName2)
        assert(new2 == "0....txt")

        val new3 = FileUtils.instance.renameByLength(10, testName3)
        assert(new3 == "01...9.txt")

    }

    @Test
    fun renameByRepeatTest() {
        val file = File(testName1)
        file.createNewFile()
        val file1 = File("abc(1).txt")
        file1.createNewFile()
        val new1 = FileUtils.instance.renameByRepeat("./", testName1)
        file.delete()
        file1.delete()
        assert(new1 == "abc(2).txt")

        val new4 = FileUtils.instance.renameByRepeat("./", testName4)
        assert(new4 == testName4)
    }

    @Test
    fun crc64Test() {
        println("file")
        val r = CRC64.fromFile(File("./test/crc.jpg"))?.stringValue
        println(r)
        assert(r == "4470037415778292408")
    }
}