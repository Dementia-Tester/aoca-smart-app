package org.example.dementia_tester_app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform