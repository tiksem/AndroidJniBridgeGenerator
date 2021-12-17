package com.neborosoft.jnibridgegenerator

import com.neborosoft.annotations.CppAccessibleInterface

@CppAccessibleInterface(cppClassName = "KotlinClass")
interface KotlinInterfaceTest {
    fun d(e: Int)
    fun eeer(ee: LongArray): String
}