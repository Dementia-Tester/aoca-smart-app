package org.example.dementia_tester_app.utils

/**
 * Utility function to check if a string is empty after trimming
 */
fun String.isEmptyTrimmed(): Boolean = this.trim().isEmpty()

/**
 * Validates if a string is a valid email format
 * @return True if the string is a valid email format, false otherwise
 */
fun String.isValidEmail(): Boolean {
    // Basic email regex that is more permissive but still checks for @ and .
    val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    return this.trim().matches(emailRegex)
}

/**
 * Validates if a string contains only digits
 * @return True if the string contains only digits, false otherwise
 */
fun String.isDigitsOnly(): Boolean {
    return this.all { it.isDigit() }
}

/**
 * Validates if a string is a valid phone number
 * @return True if the string is a valid phone number, false otherwise
 */
fun String.isValidPhoneNumber(): Boolean {
    val digitsOnly = this.replace(Regex("[\\s\\-()]"), "")
    return digitsOnly.isDigitsOnly() && digitsOnly.length in 8..15
}

/**
 * Validates a map of fields and returns a map of field names to error states
 * @param fields A map of field names to field values
 * @return A map of field names to error states (true if the field is empty)
 */
fun validateFields(fields: Map<String, String>): Map<String, Boolean> {
    val errors = mutableMapOf<String, Boolean>()
    
    // Check each field and mark as error if empty
    fields.forEach { (fieldName, fieldValue) ->
        if (fieldValue.isEmptyTrimmed()) {
            errors[fieldName] = true
        }
    }
    
    return errors
}