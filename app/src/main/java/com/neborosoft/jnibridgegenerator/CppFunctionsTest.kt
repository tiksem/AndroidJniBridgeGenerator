package com.neborosoft.jnibridgegenerator

import com.neborosoft.annotations.CppFunctionsContainer

@CppFunctionsContainer
object CppFunctionsTest {
    external fun yo(l: String): LongArray
}