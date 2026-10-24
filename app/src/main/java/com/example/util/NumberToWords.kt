package com.example.util

import kotlin.math.floor
import kotlin.math.roundToInt

object NumberToWords {
    private val UNITS = arrayOf(
        "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
        "seventeen", "eighteen", "nineteen"
    )

    private val TENS = arrayOf(
        "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
    )

    private fun inWords(n: Long): String {
        if (n == 0L) return ""
        val sb = StringBuilder()
        var num = n

        if (num / 1000000L > 0) {
            sb.append(inWords(num / 1000000L)).append(" million ")
            num %= 1000000L
        }
        if (num / 1000L > 0) {
            sb.append(inWords(num / 1000L)).append(" thousand ")
            num %= 1000L
        }
        if (num / 100L > 0) {
            sb.append(inWords(num / 100L)).append(" hundred ")
            num %= 100L
        }
        if (num > 0) {
            if (sb.isNotEmpty()) sb.append("and ")
            if (num < 20) {
                sb.append(UNITS[num.toInt()]).append(" ")
            } else {
                val tensVal = TENS[(num / 10).toInt()]
                val unitsVal = UNITS[(num % 10).toInt()]
                if (unitsVal.isNotEmpty()) {
                    sb.append(tensVal).append("-").append(unitsVal).append(" ")
                } else {
                    sb.append(tensVal).append(" ")
                }
            }
        }
        return sb.toString().trim()
    }

    fun convertToUaeCurrency(amount: Double): String {
        val nonNegative = if (amount < 0) 0.0 else amount
        val dirhams = floor(nonNegative).toLong()
        val fils = ((nonNegative - dirhams) * 100.0).roundToInt()

        val dirhamWords = if (dirhams == 0L) "zero" else inWords(dirhams)
        val filsWords = if (fils > 0) inWords(fils.toLong()) else ""

        val result = StringBuilder("UAE Dirham ").append(dirhamWords)
        if (fils > 0) {
            result.append(" and ").append(filsWords).append(" Fils")
        }
        result.append(" only")
        return result.toString()
    }
}
