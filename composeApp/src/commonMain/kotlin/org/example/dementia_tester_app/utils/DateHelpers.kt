package org.example.dementia_tester_app.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Helper function to get month name from month number
 * 
 * @param month The month number (1-12)
 * @return The name of the month
 */
fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> "Unknown"
    }
}

/**
 * Calculates age from a date-of-birth string in DD/MM/YYYY format.
 *
 * @param dateOfBirth The date-of-birth string in DD/MM/YYYY.
 * @return The age in years, or null if input is invalid.
 */
fun calculateAgeFromDateOfBirth(dateOfBirth: String): Int? {
    // Parse the birthdate components
    val parts = dateOfBirth.split("/")
    if (parts.size != 3) return null
    val (day, month, year) = parts.map { it.toIntOrNull() ?: return null }

    // Construct LocalDate for birth (strict validation)
    val birthDate = try {
        LocalDate(year, month, day)
    } catch (_: IllegalArgumentException) {
        return null
    }

    // Get current date in system default time zone
    val today = Clock.System
        .now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    // Compute preliminary age
    var age = today.year - birthDate.year

    // If birthday hasn't occurred yet this year, subtract one
    // Create a date with birth month/day in current year for comparison
    val birthdayThisYear = try {
        LocalDate(today.year, birthDate.month, birthDate.dayOfMonth)
    } catch (_: IllegalArgumentException) {
        // Handle edge cases like February 29 in non-leap years
        LocalDate(today.year, birthDate.month, 1)
    }
    
    // If today is before their birthday this year, subtract one from age
    if (today < birthdayThisYear) {
        age--
    }
    return age
}