/**
 * AstronomicalObjectList.kt
 *
 * Copyright 2020-2025 Yasuhiro Yamakawa <withlet11@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.withlet11.astronomical

import android.content.Context
import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayList

abstract class AstronomicalObjectList(private val filename: String) {
    protected var list = ArrayList<AstronomicalObject>()

    /*
    fun load(context: Context): ArrayList<AstronomicalObject> {
        val assetManager: AssetManager  = context.resources.assets

        try {
            var bufferReader: BufferedReader?  = null
            val inputStream: InputStream = assetManager.open(filename)
            val inputStreamReader = InputStreamReader(inputStream)
            bufferReader = BufferedReader(inputStreamReader)
            bufferReader.readLine() // skip first line
            var line: String?
            while (true) {
                line = bufferReader.readLine()
                if (line == null) break
                getObject(line.split('\t'))
            }
        } catch (e: IOException) {
            println("Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                bufferReader?.close()
            } catch (e: IOException) {
//                println("Closing fileReader Error!")
                e.printStackTrace()
            }
        }

        return list
    }
     */
    fun load(context: Context): ArrayList<AstronomicalObject> {
        val assetManager: AssetManager = context.resources.assets

        try {
            // Use 'use' to ensure InputStream is closed
            assetManager.open(filename).use { inputStream: InputStream ->
                // Use 'use' to ensure InputStreamReader is closed
                InputStreamReader(inputStream).use { inputStreamReader ->
                    // Use 'use' to ensure BufferedReader is closed
                    BufferedReader(inputStreamReader).use { bufferReader ->
                        bufferReader.readLine() // skip first line
                        var line: String?
                        while (true) {
                            line = bufferReader.readLine()
                            if (line == null) break
                            getObject(line.split('\t'))
                        }
                    }
                }
            }
        } catch (e: IOException) {
            println("Reading CSV Error!")
            e.printStackTrace()
        }
        // No finally block needed for closing resources, 'use' handles it

        return list
    }


    abstract fun getObject(line: List<String>)
}
