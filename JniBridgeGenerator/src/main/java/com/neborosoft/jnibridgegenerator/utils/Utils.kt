package com.neborosoft.jnibridgegenerator.utils

import com.neborosoft.jnibridgegenerator.readIntoString

object Utils {
    fun readResource(resourceName: String): String {
        return javaClass.classLoader.getResourceAsStream(resourceName)
            ?.readIntoString()
            ?: throw IllegalStateException("$resourceName resource not found")
    }
}