/**
 * Star.kt
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

class Star(
    val magnitude: String, // −1.46
    val commonName: String, // Sirius
    val bayer1: String, // α
    val bayer2: String, // CMa
    val distance: Float, // 8.6
    val spectral: String, // A0mA1 Va, DA2
    ra: String,
    dec: String
) : AstronomicalObject(ra, dec) {
    override val name: String = commonName.let { if (it == "–") "$bayer1 $bayer2" else it }

    override val details: String
        get() = StringBuffer().apply {
            append("Magnitude: %s\n".format(magnitude))
            append("Common name: %s\n".format(commonName))
            append("Bayer: %s %s\n".format(bayer1, bayer2))
            append("Distance (ly): %s\n".format(distance))
            append("Spectral: %s\n".format(spectral))
        }.toString()
}