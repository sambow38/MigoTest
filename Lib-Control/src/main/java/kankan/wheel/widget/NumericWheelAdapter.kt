/*
 *  Copyright 2010 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package kankan.wheel.widget


/**
 * Numeric Wheel adapter.
 */
@Suppress("unused")
class NumericWheelAdapter
/**
 * Constructor
 * @param minValue the wheel min value
 * @param maxValue the wheel max value
 * @param format the format string
 */
@JvmOverloads
constructor(
        private val minValue: Int = DEFAULT_MIN_VALUE, private val maxValue: Int = DEFAULT_MAX_VALUE,
        private val format: String? = null) : WheelAdapter
{

    override val itemsCount: Int
        get() = maxValue - minValue + 1

    override val maximumLength: Int
        get() {
            val max = Math.max(Math.abs(maxValue), Math.abs(minValue))
            var maxLen = Integer.toString(max).length
            if (minValue < 0) {
                maxLen++
            }
            return maxLen
        }

    override fun getItem(index: Int): String? {
        if (index in 0 until itemsCount) {
            val value = minValue + index
            return if (format != null) String.format(format, value) else Integer.toString(value)
        }
        return null
    }

    companion object {

        /** The default min value  */
        const val DEFAULT_MAX_VALUE = 9

        /** The default max value  */
        private const val DEFAULT_MIN_VALUE = 0
    }
}
