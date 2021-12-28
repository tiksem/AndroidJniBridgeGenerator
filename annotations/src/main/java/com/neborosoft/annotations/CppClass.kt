package com.neborosoft.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class CppClass(
    val base: String = "",
    val customPath: String = "",
    val withNativeConstructor: Boolean = false
)
