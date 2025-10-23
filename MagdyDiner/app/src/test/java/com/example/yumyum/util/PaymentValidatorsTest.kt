package com.example.yumyum.util

import org.junit.Test
import org.junit.Assert.*

class PaymentValidatorsTest {
    @Test
    fun cardNumber_valid_exact16() {
        assertTrue(PaymentValidators.isCardNumberValid("4242424242424242"))
    }

    @Test
    fun cardNumber_valid_withSpaces() {
        assertTrue(PaymentValidators.isCardNumberValid("4242 4242 4242 4242"))
    }

    @Test
    fun cardNumber_valid_withDashes() {
        assertTrue(PaymentValidators.isCardNumberValid("4242-4242-4242-4242"))
    }

    @Test
    fun cardNumber_invalid_short() {
        assertFalse(PaymentValidators.isCardNumberValid("12345678"))
    }

    @Test
    fun cardNumber_invalid_nonDigit() {
        assertFalse(PaymentValidators.isCardNumberValid("4242 4242 4242 424X"))
    }

    @Test
    fun cvc_valid_3_and_4() {
        assertTrue(PaymentValidators.isCvcValid("123"))
        assertTrue(PaymentValidators.isCvcValid("1234"))
    }

    @Test
    fun cvc_invalid() {
        assertFalse(PaymentValidators.isCvcValid("12"))
        assertFalse(PaymentValidators.isCvcValid("abcd"))
    }

    @Test
    fun expiry_dynamic_current_and_future_and_past() {
        val cal = java.util.Calendar.getInstance()
        val yearFull = cal.get(java.util.Calendar.YEAR)
        val month = cal.get(java.util.Calendar.MONTH) + 1 // 1..12
        val yearTwo = yearFull % 100

        val currentMmYy = String.format("%02d/%02d", month, yearTwo)
        val currentMmYyNoSlash = String.format("%02d%02d", month, yearTwo)

        // Next month (roll over year if necessary)
        val nextMonthCal = java.util.Calendar.getInstance()
        nextMonthCal.add(java.util.Calendar.MONTH, 1)
        val nextYearFull = nextMonthCal.get(java.util.Calendar.YEAR)
        val nextMonth = nextMonthCal.get(java.util.Calendar.MONTH) + 1
        val nextYearTwo = nextYearFull % 100
        val nextMmYy = String.format("%02d/%02d", nextMonth, nextYearTwo)

        // Previous month (could be in same or previous year)
        val prevMonthCal = java.util.Calendar.getInstance()
        prevMonthCal.add(java.util.Calendar.MONTH, -1)
        val prevYearFull = prevMonthCal.get(java.util.Calendar.YEAR)
        val prevMonth = prevMonthCal.get(java.util.Calendar.MONTH) + 1
        val prevYearTwo = prevYearFull % 100
        val prevMmYy = String.format("%02d/%02d", prevMonth, prevYearTwo)

        // Option A expectations: expiry is valid when its YEAR <= currentYear
        val expectedCurrent = true // current year => year <= currentYear
        val expectedNext = (nextYearFull <= yearFull)
        val expectedPrev = (prevYearFull <= yearFull)

        assertEquals(expectedCurrent, PaymentValidators.isExpiryValid(currentMmYy))
        assertEquals(expectedCurrent, PaymentValidators.isExpiryValid(currentMmYyNoSlash))
        assertEquals(expectedNext, PaymentValidators.isExpiryValid(nextMmYy))
        assertEquals(expectedPrev, PaymentValidators.isExpiryValid(prevMmYy))
    }
}
