package com.neborosoft.jnibridgegenerator

val DEFAULT_KOTLIN_TO_CPP_TYPES_MAPPING_FROM_JNI_TO_CPP  = mapOf(
    "Unit" to "void",
    "Byte" to "int8_t",
    "Short" to "int16_t",
    "Int" to "int32_t",
    "Long" to "int64_t",
    "Float" to "float",
    "Double" to "double",
    "Boolean" to "bool",
    "BooleanArray" to "JBooleanArray",
    "ByteArray" to "JByteArray",
    "ShortArray" to "JShortArray",
    "IntArray" to "JIntArray",
    "LongArray" to "JLongArray",
    "FloatArray" to "JFloatArray",
    "DoubleArray" to "JDoubleArray",
    "String" to "JString"
)

val DEFAULT_KOTLIN_TO_CPP_TYPES_MAPPING_FROM_CPP_TO_JNI  = mapOf(
    "Unit" to "void",
    "Byte" to "int8_t",
    "Short" to "int16_t",
    "Int" to "int32_t",
    "Long" to "int64_t",
    "Float" to "float",
    "Double" to "double",
    "Boolean" to "bool",
    "ByteArray" to "std::string",
    "BooleanArray" to "std::vector<bool>",
    "ShortArray" to "std::vector<int16_t>",
    "IntArray" to "std::vector<int32_t>",
    "LongArray" to "std::vector<int64_t>",
    "FloatArray" to "std::vector<float>",
    "DoubleArray" to "std::vector<double>",
    "String" to "std::string"
)

val KOTLIN_TO_JNI_TYPES_MAPPING = mapOf(
    "Unit" to "void",
    "Byte" to "jbyte",
    "Short" to "jshort",
    "Int" to "jint",
    "Long" to "jlong",
    "Float" to "jfloat",
    "Double" to "jdouble",
    "Boolean" to "jboolean",
    "ByteArray" to "jbyteArray",
    "ShortArray" to "jshortArray",
    "IntArray" to "jintArray",
    "LongArray" to "jlongArray",
    "FloatArray" to "jfloatArray",
    "DoubleArray" to "jdoubleArray",
    "String" to "jstring"
)