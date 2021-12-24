package com.neborosoft.jnibridgegenerator

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

private val DEFAULT_KOTLIN_TO_CPP_TYPES_MAPPING_FROM_JNI_TO_CPP  = mapOf(
    "Unit" to "void",
    "Byte" to "int8_t",
    "Short" to "int16_t",
    "Int" to "int32_t",
    "Long" to "int64_t",
    "Float" to "float",
    "Double" to "double",
    "Boolean" to "bool",
    "Char" to "char16_t",
    "BooleanArray" to "JBooleanArray",
    "ByteArray" to "JByteArray",
    "ShortArray" to "JShortArray",
    "IntArray" to "JIntArray",
    "LongArray" to "JLongArray",
    "FloatArray" to "JFloatArray",
    "DoubleArray" to "JDoubleArray",
    "String" to "JString"
)

private val DEFAULT_KOTLIN_TO_CPP_TYPES_MAPPING_FROM_CPP_TO_JNI  = mapOf(
    "Unit" to "void",
    "Byte" to "int8_t",
    "Short" to "int16_t",
    "Int" to "int32_t",
    "Long" to "int64_t",
    "Float" to "float",
    "Double" to "double",
    "Boolean" to "bool",
    "Char" to "char16_t",
    "ByteArray" to "std::string",
    "BooleanArray" to "std::vector<bool>",
    "ShortArray" to "std::vector<int16_t>",
    "IntArray" to "std::vector<int32_t>",
    "LongArray" to "std::vector<int64_t>",
    "FloatArray" to "std::vector<float>",
    "DoubleArray" to "std::vector<double>",
    "String" to "std::string"
)

private val KOTLIN_TO_JNI_TYPES_MAPPING = mapOf(
    "Unit" to "void",
    "Byte" to "jbyte",
    "Short" to "jshort",
    "Int" to "jint",
    "Long" to "jlong",
    "Float" to "jfloat",
    "Double" to "jdouble",
    "Boolean" to "jboolean",
    "Char" to "char16_t",
    "ByteArray" to "jbyteArray",
    "ShortArray" to "jshortArray",
    "IntArray" to "jintArray",
    "LongArray" to "jlongArray",
    "FloatArray" to "jfloatArray",
    "DoubleArray" to "jdoubleArray",
    "String" to "jstring"
)

private val JNI_SIGNATURE_MAPPING = mapOf(
    "Unit" to "V",
    "Byte" to "B",
    "Short" to "S",
    "Int" to "I",
    "Long" to "J",
    "Float" to "F",
    "Double" to "D",
    "Boolean" to "Z",
    "ByteArray" to "B]",
    "ShortArray" to "S]",
    "IntArray" to "I}",
    "LongArray" to "L]",
    "FloatArray" to "F]",
    "DoubleArray" to "D]",
)

private val KOTLIN_TO_JAVA_TYPE_MAPPING = mapOf(
    "kotlin.Byte" to "byte",
    "kotlin.Short" to "short",
    "kotlin.Int" to "int",
    "kotlin.Long" to "long",
    "kotlin.Float" to "float",
    "kotlin.Double" to "double",
    "kotlin.Boolean" to "boolean",
    "kotlin.Char" to "char",
    "kotlin.ByteArray" to "byte[]",
    "kotlin.ShortArray" to "short[]",
    "kotlin.IntArray" to "int[]",
    "kotlin.LongArray" to "long[]",
    "kotlin.FloatArray" to "float[]",
    "kotlin.DoubleArray" to "double[]",
    "kotlin.String" to "java.lang.String"
)

object TypesMapping {
    private val registeredCppTypesMapping = HashBiMap.create<String, String>()

    fun getCppTypeName(kotlinTypeName: String, fromJniToCpp: Boolean): String? {
        val map = if (fromJniToCpp) {
            DEFAULT_KOTLIN_TO_CPP_TYPES_MAPPING_FROM_JNI_TO_CPP
        } else {
            DEFAULT_KOTLIN_TO_CPP_TYPES_MAPPING_FROM_CPP_TO_JNI
        }

        return map[kotlinTypeName]
    }

    fun getJniSignature(kotlinTypeName: String): String? {
        return JNI_SIGNATURE_MAPPING[kotlinTypeName]
    }

    fun getJniType(kotlinTypeName: String): String {
        return KOTLIN_TO_JNI_TYPES_MAPPING[kotlinTypeName] ?: "jobject"
    }

    fun registerCppTypeMapping(kotlinTypeName: String, cppTypeName: String) {
        registeredCppTypesMapping[kotlinTypeName] = cppTypeName
    }

    fun isCppTypeRegistered(cppTypeName: String): Boolean {
        return registeredCppTypesMapping.containsValue(cppTypeName)
    }

    fun getRegisteredCppTypeName(kotlinTypeName: String): String? {
        return registeredCppTypesMapping[kotlinTypeName]
    }

    fun getJavaTypeFromKotlinType(kotlinTypeName: String): String? {
        return KOTLIN_TO_JAVA_TYPE_MAPPING[kotlinTypeName]
    }
}