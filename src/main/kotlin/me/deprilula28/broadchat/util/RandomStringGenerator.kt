package me.deprilula28.broadchat.util

import java.math.BigInteger
import java.util.*

object RandomStringGenerator {
    fun next(random: Random) = Base62.encode(BigInteger(48, random))

    object Base62 {

        val BASE = BigInteger.valueOf(62)
        val DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val REGEXP = "^[0-9A-Za-z]+$"

        /**
         * Encodes a number using Base62 encoding.

         * @param number a positive integer
         * *
         * @return a Base62 string
         * *
         * @throws IllegalArgumentException if `number` is a negative integer
         */
        fun encode(number: BigInteger): String {
            var number = number
            if (number.compareTo(BigInteger.ZERO) == -1) { // number < 0
                throw IllegalArgumentException("number must not be negative")
            }
            val result = StringBuilder()
            while (number.compareTo(BigInteger.ZERO) == 1) { // number > 0
                val divmod = number.divideAndRemainder(BASE)
                number = divmod[0]
                val digit = divmod[1].toInt()
                result.insert(0, DIGITS[digit])
            }
            return if (result.isEmpty()) DIGITS.substring(0, 1) else result.toString()
        }

        fun encode(number: Long): String {
            return encode(BigInteger.valueOf(number))
        }

        /**
         * Decodes a string using Base62 encoding.

         * @param string a Base62 string
         * *
         * @return a positive integer
         * *
         * @throws IllegalArgumentException if `string` is empty
         */
        fun decode(string: String): BigInteger {
            if (string.length == 0) {
                throw IllegalArgumentException("string must not be empty")
            }
            var result = BigInteger.ZERO
            val digits = string.length
            for (index in 0 .. digits - 1) {
                val digit = DIGITS.indexOf(string[digits - index - 1])
                result = result.add(BigInteger.valueOf(digit.toLong()).multiply(BASE.pow(index)))
            }
            return result
        }

    }
}