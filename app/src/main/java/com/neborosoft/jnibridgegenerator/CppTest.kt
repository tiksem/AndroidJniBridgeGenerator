package com.neborosoft.jnibridgegenerator

import com.neborosoft.annotations.CppClass

@CppClass
interface CppTest {
    fun getA(): Int
    fun yo(value: String)
    fun e(list: IntArray)
    fun g(): String
}