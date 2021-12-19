package com.neborosoft.jnibridgegenerator

import android.content.Context
import com.neborosoft.annotations.CppAccessibleInterface
import com.neborosoft.annotations.CppFunction
import com.neborosoft.annotations.SkipMethod

@CppAccessibleInterface(cppClassName = "KotlinClass")
class KotlinInterfaceTest {
    fun d(e: Int) {

    }

    fun eeer(ee: LongArray): String {
        return "sdhjdfsghjdfsj"
    }

    @SkipMethod
    fun asdfjgasjhd(context: Context) {

    }

    external fun bbbbbgggg(): LongArray
    @CppFunction
    external fun ahaha(): IntArray
}