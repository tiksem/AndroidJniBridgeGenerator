package com.neborosoft.jnibridgegenerator.methods

interface MethodGenerator {
    fun getCppHeaderMethodDeclaration(): String?
    fun getSourceDeclaration(template: String): String?
    fun getMethodIdGeneration(template: String): String?
    fun getMethodIdDeclaration(template: String): String?

    fun getRequestedCppHeaders(): List<String> {
        return emptyList()
    }

    fun getJniMethodCall(
        packageName: String,
        kotlinClassName: String,
        cppClassName: String
    ): String? {
        return null
    }
}