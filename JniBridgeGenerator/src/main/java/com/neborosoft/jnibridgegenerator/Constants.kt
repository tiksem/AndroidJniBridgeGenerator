package com.neborosoft.jnibridgegenerator

object Constants {
    const val CPP_TEMPLATE_H = "CppTemplate.h"
    const val J_OBJECT_TEMPLATE_H = "JObjectTemplate.h"
    const val J_OBJECT_SINGLETON_TEMPLATE_H = "JObjectSingletonTemplate.h"
    const val J_OBJECT_TEMPLATE_CPP = "JObjectTemplate.cpp"
    const val J_OBJECT_SINGLETON_TEMPLATE_CPP = "JObjectSingletonTemplate.cpp"
    const val JNI_PUBLIC_INTERFACE_TOKEN = "    // Public Jni Interface\n"
    const val INCLUDE_START_TOKEN = "// headers\n"
    const val JAVA_METHOD_WRAPPERS_TOKEN = "// Java method wrappers\n"
    const val JAVA_WRAPPER_METHODS_ID_DECLARATION_TOKEN = "// Method ides declaration\n"
    const val JAVA_WRAPPER_METHODS_ID_GENERATION_TOKEN = "// Method ides generation\n"
    const val CPP_TEMPLATE_CLASS_NAME = "CppTemplate"
    const val KOTLIN_CLASS_IMPLEMENTATION_POSTFIX = "Native"
    const val PTR = "ptr"
    const val KOTLIN_PACKAGE_NAME = "com.neborosoft.jnibridgegenerator"

    val CPP_RESOURCES = listOf(
        "Converters.h",
        "JArray.h",
        "JArray.cpp",
        "JString.h",
        "JString.cpp",
        "JNIBridgeInit.h",
        "JObject.h",
        "JObject.cpp"
    )

    val CPP_RESOURCES_COPY_ONLY_IF_NOT_EXIST = listOf(
        "CustomConverters.h",
    )

    val CPP_PRIMITIVES = setOf(
        "bool",
        "int8_t", "int16_t", "int32_t", "int64_t",
        "float", "double"
    )
}