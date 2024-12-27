/**
 * MessierObject.kt
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

import java.util.*

data class NameAndAbbr(val name: String, val abbr: String)

class MessierObject(
    val messier: String, // M97
    val ngc: String, // NGC 3587
    val commonName: String, //Owl Nebula
    @Suppress("unused") val picture: String, // M97-stargazer-obs.jpg
    val type: String, // Planetary nebula
    val distance: String, // 2.03
    val constellation: String, // Ursa Major
    val magnitude: String, // 9.9
    ra: String,
    dec: String
) : AstronomicalObject(ra, dec) {
    override val name: String = "%-3s %s".format(messier, getTypeAbbr())

    override val details: String
        get() = StringBuffer().apply {
            append(if (ngc == "–") "NGC: –" else ngc)
            append("\nCommon name: %s\n".format(commonName))
            append("Type: %s\n".format(type))
            append("Distance (ly): %s\n".format(distance))
            append("Constellation: %s\n".format(constellation))
            append("Magnitude: %s\n".format(magnitude))
        }.toString()

    private fun getTypeAbbr(): String =
        typeList.mapNotNull {
            if (type.lowercase(Locale.getDefault()).indexOf(it.name) >= 0) it.abbr else null
        }.distinct().joinToString()

    companion object {
        private val typeList: List<NameAndAbbr>
            get() = listOf(
                NameAndAbbr("supernova remnant", "□"),
                NameAndAbbr("globular cluster", "⨁"),
                NameAndAbbr("asterism", "◌"),
                NameAndAbbr("open cluster", "◌"),
                NameAndAbbr("diffuse nebula", "□"),
                NameAndAbbr("h ii region nebula", "□"),
                NameAndAbbr("nebula with", "□"),
                NameAndAbbr("planetary nebula", "⌖"),
                NameAndAbbr("galaxy", "⬭")
            )
    }
}
