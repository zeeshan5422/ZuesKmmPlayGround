package com.kmm.zues.playground

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform