package com.neborosoft.jnibridgegenerator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cpp = CppTestNative()
        Log.d("MainActivity", cpp.g())
        cpp.ffg {
            Log.d("MainActivity", it.toString())
        }
        Singleton.nativeInit()
        cpp.getA()

        val k = KotlinInterfaceTest()
        Log.d("MainActivity", k.bbbbbgggg().joinToString(", "))
        Log.d("MainActivity", CppFunctionsTest.yo("dsdsds").joinToString(", "))
    }

    external fun eee(ptr: Long)

    companion object {
        init {
            System.loadLibrary("jnibridgegenerator")
        }
    }
}