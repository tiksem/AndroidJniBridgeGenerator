package com.neborosoft.jnibridgegenerator

import android.util.Log
import com.neborosoft.annotations.CppAccessibleInterface

@CppAccessibleInterface
object Singleton {
    fun yo() {
        Log.d("Singleton", "yoyoyo")
    }

    external fun nativeInit()
}