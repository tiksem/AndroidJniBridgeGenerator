package com.neborosoft.jnibridgegenerator

object CodeGenerationUtils {
    fun getCppHeaderMethodDeclaration(
        methodName: String,
        returnType: String,
        types: List<String>,
        names: List<String>
    ): String {
        val args = generateCppMethodArgs(types, names)
        return "$returnType ${methodName}($args);"
    }

    fun generateCppMethodArgs(
        types: List<String>,
        names: List<String>
    ): String {
        return (types zip names).joinToString(",") {
            it.first + " " + it.second
        }
    }
}