package com.neborosoft.jnibridgegenerator

import com.neborosoft.annotations.CppClass
import com.neborosoft.annotations.CppMethod

@CppClass
interface CppTest {
    fun getA(): Int
    fun yo(value: String)
    fun e(list: IntArray)
    fun g(): String
    fun ffg(l: (Int)->Unit)
    fun rrr(i: KotlinInterfaceTest)
    @CppMethod(cppType = "Uee")
    fun prrttr(): Int
}