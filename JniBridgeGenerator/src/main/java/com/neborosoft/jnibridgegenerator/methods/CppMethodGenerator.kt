package com.neborosoft.jnibridgegenerator.methods

import com.squareup.kotlinpoet.FunSpec

interface CppMethodGenerator : MethodGenerator {
    override fun getCppHeaderMethodDeclaration(): String? {
        return null
    }

    fun getKotlinSpecs(): List<FunSpec>

    override fun getSourceDeclaration(template: String): String? {
        return null
    }

    override fun getMethodIdGeneration(template: String): String? {
        return null
    }

    override fun getMethodIdDeclaration(template: String): String? {
        return null
    }
}