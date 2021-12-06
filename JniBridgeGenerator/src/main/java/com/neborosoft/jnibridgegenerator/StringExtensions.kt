package com.neborosoft.jnibridgegenerator

fun String.insert(index: Int, string: String): String {
    return this.substring(0, index) + string + this.substring(index, this.length)
}

fun String.insertAfter(token: String, string: String): String {
    val index = indexOf(token)
    if (index < 0) {
        return this
    }

    return insert(index + token.length, string)
}

fun String.getStringEndingAfterToken(token: String): String {
    return substring(lastIndexOf(token) + token.length)
}

fun String.findStringBetweenQuotes(quote: String): String {
    return substringAfter(quote, "")
        .substringBefore(quote, "")
}
