package com.neborosoft.jnibridgegenerator

import java.io.InputStream

fun InputStream.readIntoString(): String {
    val bytes = readAllBytes()
    return String(bytes)
}

fun InputStream.readIntoStringBuilder(): StringBuilder {
    val bytes = readAllBytes()
    return StringBuilder(readIntoString())
}