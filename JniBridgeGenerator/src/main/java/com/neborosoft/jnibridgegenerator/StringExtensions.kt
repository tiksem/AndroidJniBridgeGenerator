package com.neborosoft.jnibridgegenerator

fun String.insert(index: Int, string: String): String {
    return this.substring(0, index) + string + this.substring(index, this.length)
}

fun String.getStringEndingAfterToken(token: String): String {
    return substring(lastIndexOf(token) + token.length)
}
fun String.addConstReferenceToCppTypeName(): String {
    return "const $this&"
}
