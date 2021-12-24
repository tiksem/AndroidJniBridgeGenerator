package com.neborosoft.jnibridgegenerator

import android.content.Context
import com.neborosoft.annotations.*

@CppAccessibleInterface(cppClassName = "KotlinClass", base = ["Eblo"])
class KotlinInterfaceTest {
    fun d(e: Int) {

    }

    fun eeer(ee: LongArray): String {
        return "sdhjdfsghjdfsj"
    }

    @SkipMethod
    fun asdfjgasjhd(context: Context) {

    }

    @CppMethod(cppType = "Eblo")
    fun u(@CppParam("Eblo") r: ShortArray): ShortArray {
        return shortArrayOf()
    }

    external fun bbbbbgggg(): LongArray
    @CppFunction
    external fun ahaha(): IntArray
}