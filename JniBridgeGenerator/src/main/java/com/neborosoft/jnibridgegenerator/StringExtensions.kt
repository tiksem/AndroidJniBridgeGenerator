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
    return findStringBetweenQuotes(quote, quote)
}

fun String.findStringBetweenQuotes(quoteBegin: String, quoteEnd: String): String {
    return substringAfter(quoteBegin, "")
        .substringBefore(quoteEnd, "")
}

fun String.replace(startIndex: Int, endIndex: Int, token: String): String {
    if (startIndex < 0 || endIndex !in startIndex..length) {
        return this
    }

    return substring(0, startIndex) + token + substring(endIndex, length)
}

fun String.replaceStringBetweenTokens(token1: String,
                                      token2: String,
                                      replacement: String,
                                      replaceTokens: Boolean = false): String {
    var index1 = indexOf(token1)
    var index2 = indexOf(token2, index1 + token1.length)

    if (replaceTokens) {
        index2 += token2.length
    } else {
        index1 += token1.length
    }

    return replace(startIndex = index1, endIndex = index2, token = replacement)
}
