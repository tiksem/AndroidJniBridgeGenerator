package com.neborosoft.jnibridgegenerator

import com.neborosoft.annotations.KotlinCppConstructor

@KotlinCppConstructor
data class KotlinStruct(
    val a: Int,
    val b: String
)