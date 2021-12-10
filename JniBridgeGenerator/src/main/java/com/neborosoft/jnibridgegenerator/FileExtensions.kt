package com.neborosoft.jnibridgegenerator

import java.io.File

fun File.tryReadText(): String? {
    return if (canRead()) {
        readText()
    } else {
        null
    }
}