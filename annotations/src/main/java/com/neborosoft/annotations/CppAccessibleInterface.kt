package com.neborosoft.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class CppAccessibleInterface(
    val customPath: String = "",
    val cppClassName: String = "",
    val isSingleton: Boolean = false,
    val base: Array<String> = []
)
