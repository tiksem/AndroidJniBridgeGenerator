package com.neborosoft.jnibridgegenerator

object Utils {
    fun readResource(resourceName: String): String {
        return javaClass.classLoader.getResourceAsStream(resourceName)
            ?.readIntoString()
            ?: throw IllegalStateException("$resourceName resource not found")
    }
}