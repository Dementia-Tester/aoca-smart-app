package org.example.dementia_tester_app.data

/**
 * Result class to handle database operations
 */
sealed class DatabaseResult<out T> {
    data class Success<T>(val data: T) : DatabaseResult<T>()
    data class Error(val message: String) : DatabaseResult<Nothing>()
}
