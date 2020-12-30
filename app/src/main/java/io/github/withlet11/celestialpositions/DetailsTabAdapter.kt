/**
 * DetailsTabAdapter.kt
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

package io.github.withlet11.celestialpositions

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter


class DetailsTabAdapter(
    fa: DetailsActivity,
    private val latitude: Double,
    private val longitude: Double,
    private val declinationList: DoubleArray,
    private val rightAscensionList: LongArray,
    private val nameList: ArrayList<String>,
    private val detailsList: ArrayList<String>
) : FragmentStateAdapter(fa) {

    init {
        DetailsFragment.latitude = latitude
        DetailsFragment.longitude = longitude
    }

    override fun getItemCount(): Int = nameList.size

    override fun createFragment(position: Int): Fragment = DetailsFragment().apply {
        arguments = Bundle().apply {
            putDouble("LATITUDE", latitude)
            putDouble("LONGITUDE", longitude)
            putDouble("DECLINATION", declinationList[position])
            putLong("RIGHT_ASCENSION", rightAscensionList[position])
            putString("NAME", nameList[position])
            putString("DETAILS", detailsList[position])
        }
    }
}
