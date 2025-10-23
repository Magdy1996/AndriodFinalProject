package com.example.yumyum.util

/**
 * Small, pure validator functions for payment input so they can be easily tested.
 */
object PaymentValidators {
    /**
     * Card number is valid if it contains exactly 16 digits (spaces/dashes allowed in input).
     */
    fun isCardNumberValid(num: String): Boolean {
        val digits = num.filter { it.isDigit() }
        return digits.length == 16
    }

    /**
     * Expiry accepts MM/YY or MMYY. Does not check for future date here.
     */
    fun isExpiryValid(exp: String): Boolean {
        // Option A: Accept MM/YY or MMYY and ensure the expiry YEAR is not after the current year
        val cleaned = exp.replace(" ", "")
        val regex = "^(0[1-9]|1[0-2])/(\\d{2})$".toRegex()
        val regex2 = "^(0[1-9]|1[0-2])(\\d{2})$".toRegex()
        val matched = when {
            regex.matches(cleaned) -> {
                val parts = cleaned.split("/")
                parts[0] to parts[1]
            }
            regex2.matches(cleaned) -> {
                cleaned.substring(0, 2) to cleaned.substring(2, 4)
            }
            else -> return false
        }

        val month = matched.first.toIntOrNull() ?: return false
        if (month !in 1..12) return false
        val yearTwoDigits = matched.second.toIntOrNull() ?: return false
        // Map two-digit year to 2000+YY
        val yearFull = 2000 + (yearTwoDigits % 100)

        val cal = java.util.Calendar.getInstance()
        val currentYear = cal.get(java.util.Calendar.YEAR)

        // Disallow expiry years beyond the current year
        return yearFull <= currentYear
    }

    /**
     * CVC valid if 3 or 4 digits.
     */
    fun isCvcValid(code: String): Boolean {
        val d = code.filter { it.isDigit() }
        return d.length == 3 || d.length == 4
    }

    /**
     * Card holder name should be non-empty after trimming.
     */
    fun isCardHolderValid(name: String) = name.trim().isNotEmpty()
}
