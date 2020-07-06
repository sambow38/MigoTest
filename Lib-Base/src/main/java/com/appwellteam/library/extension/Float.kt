package com.appwellteam.library.extension




/**
 * 取得指定小數位數的小數位數
 *
 * @return 小數位數
 */
val Float.digit: Int
    get() {
    val tDecimal = java.lang.Float.valueOf(this)
        if (tDecimal.toString().contains("E")) {
            val afterDot = tDecimal.toString().split("[.]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].length
            val afterE = tDecimal.toString().split("E".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].length
            var decimal = afterDot - afterE - 1
            val multiple = Integer.parseInt(tDecimal.toString().split("E".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
            decimal -= multiple
            return if (decimal < 0) 0 else decimal
        } else if (tDecimal.toString().contains(".")) {
            return tDecimal.toString().split("[.]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].length
        }
    return 0
}