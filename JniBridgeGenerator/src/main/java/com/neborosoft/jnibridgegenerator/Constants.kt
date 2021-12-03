package com.neborosoft.jnibridgegenerator

object Constants {
    const val CPP_TEMPLATE_H = "CppTemplate.h"
    const val JNI_PUBLIC_INTERFACE_TOKEN = "// Public Jni Interface\n"
    const val CPP_TEMPLATE_CLASS_NAME = "CppTemplate"
    const val KOTLIN_CLASS_IMPLEMENTATION_POSTFIX = "Native"
    const val PTR = "ptr"

    val CPP_RESOURCES = listOf(
        "Converters.h",
        "JArray.h",
        "JArray.cpp",
        "JString.h",
        "JString.cpp"
    )
}