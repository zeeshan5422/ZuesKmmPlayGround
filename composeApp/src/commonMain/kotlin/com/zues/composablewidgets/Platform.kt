package com.zues.composablewidgets

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform