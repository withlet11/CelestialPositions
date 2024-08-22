/**
 * MessierObjectList.kt
 *
 * Copyright 2020 Yasuhiro Yamakawa <withlet11@gmail.com>
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

class MessierObjectList : AstronomicalObjectList("messier_objects.csv") {
    override fun getObject(line: List<String>) {
        if (line.isNotEmpty()) {
            list.add(
                MessierObject(
                    messier = line[0],
                    ngc = line[1],
                    commonName = line[2],
                    picture = line[3],
                    type = line[4],
                    distance = line[5],
                    constellation = line[6],
                    magnitude = line[7],
                    ra = Regex("[.][0-9]*s").replace(line[8], "s"),
                    dec = Regex("[.][0-9]*″").replace(line[9], "″")
                )
            )
        }
    }
}
