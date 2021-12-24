package com.neborosoft.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class CppParam(val cppType: String)
