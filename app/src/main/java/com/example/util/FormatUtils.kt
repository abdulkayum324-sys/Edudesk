package com.example.util

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    /**
     * Formats financial transaction amounts in Nepali Rupees (NPR / Rs.).
     * Standard Nepali currency notation: "Rs. 4,500.00"
     */
    fun formatCurrency(amount: Double): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "Rs. ${numberFormat.format(amount)}"
    }

    /**
     * Formats in Nepali Rupees with symbol "रू" in Devanagari or "Rs."
     */
    fun formatNepaliRupees(amount: Double, useDevanagari: Boolean = false): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        val symbol = if (useDevanagari) "रू" else "Rs."
        return "$symbol ${numberFormat.format(amount)}"
    }

    fun formatNumber(value: Double): String {
        return String.format(Locale.US, "%.1f", value)
    }

    fun calculateGrade(percentage: Double): Pair<String, Double> {
        return when {
            percentage >= 90.0 -> Pair("A+", 4.0)
            percentage >= 80.0 -> Pair("A", 3.7)
            percentage >= 70.0 -> Pair("B+", 3.3)
            percentage >= 60.0 -> Pair("B", 3.0)
            percentage >= 50.0 -> Pair("C", 2.0)
            percentage >= 40.0 -> Pair("D", 1.0)
            else -> Pair("F", 0.0)
        }
    }
}
