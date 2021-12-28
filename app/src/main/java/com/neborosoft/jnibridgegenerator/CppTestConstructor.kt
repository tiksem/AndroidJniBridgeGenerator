package com.neborosoft.jnibridgegenerator

import com.neborosoft.annotations.CppClass

@CppClass(withNativeConstructor = true)
interface CppTestConstructor {
    fun a(): BooleanArray
}