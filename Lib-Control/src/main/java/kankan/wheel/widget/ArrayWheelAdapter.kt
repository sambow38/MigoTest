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
 * The simple Array wheel adapter
 * @param <T> the element type
</T> */
@Suppress("unused")
class ArrayWheelAdapter<T>
/**
 * Constructor
 * @param items the items
 * @param maximumLength the max items length
 */
@JvmOverloads
constructor(private val items: Array<T>,
            override val maximumLength: Int = DEFAULT_LENGTH) : WheelAdapter {

    override val itemsCount: Int
        get() = items.size

    override fun getItem(index: Int): String? {
        return if (index >= 0 && index < items.size) {
            items[index].toString()
        } else null
    }

    companion object {

        /** The default items length  */
        const val DEFAULT_LENGTH = -1
    }

}
