package com.neborosoft.jnibridgegenerator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClassifier
import java.lang.UnsupportedOperationException

@KotlinPoetMetadataPreview
fun ImmutableKmType.getTypeName(): TypeName {
    val s = (classifier as KmClassifier.Class).name.split("/")

    val className = ClassName(
        packageName = s.subList(0, s.size - 1).joinToString("/"),
        simpleNames = listOf(s.last())
    )

    val arguments = this.arguments.map {
        it.type!!.getTypeName()
    }

    if (arguments.isNotEmpty()) {
        return if (
            className.packageName == "kotlin" &&
            className.simpleName.matches(Regex("Function\\d+"))
        ) {
            LambdaTypeName.get(
                parameters = arguments.dropLast(1).map { ParameterSpec.unnamed(it) },
                returnType = arguments.last()
            )
        } else {
            className.parameterizedBy(
                typeArguments = arguments
            )
        }
    }

    return className
}

@KotlinPoetMetadataPreview
fun ImmutableKmType.getStringTypeNameWithoutDots(): String {
    val name = (classifier as KmClassifier.Class).name.getStringEndingAfterToken("/")

    val arguments = this.arguments.joinToString(", ") {
        it.type!!.getStringTypeNameWithoutDots()
    }

    if (arguments.isNotEmpty()) {
        return "$name<$arguments>"
    }

    return name
}

fun String.addConstReferenceToCppTypeNameIfNotPrimitive(): String {
    if (isPrimitiveCppType()) {
        return this
    }

    return "const $this&"
}

fun String.isPrimitiveCppType(): Boolean {
    return Constants.CPP_PRIMITIVES.contains(this)
}

fun String.removeConstReferenceFromCppType(): String {
    return this.replace("const ", "").replace("&", "")
}

fun TypeName.getCppTypeName(convertFromCppToJni: Boolean): String {
    val mapping = if (convertFromCppToJni) {
        DEFAULT_KOTLIN_TO_CPP_TYPES_MAPPING_FROM_CPP_TO_JNI
    } else {
        DEFAULT_KOTLIN_TO_CPP_TYPES_MAPPING_FROM_JNI_TO_CPP
    }

    return when (this) {
        is ClassName -> mapping[this.simpleName]
        is LambdaTypeName -> {
            val returnType = this.returnType.getCppTypeName(convertFromCppToJni = true)
            val args = this.parameters.joinToString(", ") {
                it.type.getCppTypeName(convertFromCppToJni = false)
            }

            "std::function<$returnType($args)>"
        }
        else -> null
    } ?: throw UnsupportedOperationException(
            "TypeName.getCppTypeName failed, unsupported typename $this"
        )
}

@KotlinPoetMetadataPreview
fun ImmutableKmType.getCppTypeName(convertFromCppToJni: Boolean): String {
    return annotations.find {
        it.className.endsWith("CppParam")
    }?.arguments?.get("cppType")?.value?.toString() ?: getTypeName().getCppTypeName(convertFromCppToJni)
}

fun TypeName?.getJniTypeName(): String {
    return when (this) {
        null -> "void"
        is ClassName -> KOTLIN_TO_JNI_TYPES_MAPPING[simpleName] ?: "jobject"
        is LambdaTypeName -> "jobject"
        is ParameterizedTypeName -> "jobject"
        else -> throw UnsupportedOperationException("Unsupported $this")
    }
}

fun TypeName.tryGetSimpleName(): String? {
    return when (this) {
        is ClassName -> simpleName
        is ParameterizedTypeName -> {
            val name = rawType.simpleName
            val args = typeArguments.map {
                it.getSimpleName()
            }.joinToString(", ")

            "$name<$args>"
        }
        else -> null
    }
}

fun TypeName.getSimpleName(): String {
    return tryGetSimpleName()
        ?: throw UnsupportedOperationException(
            "getSimpleName is supported only for ClassName and ParameterizedTypeName"
        )
}

fun TypeName.getJniSignatureMapping(): String {
    return tryGetSimpleName()?.let { JNI_SIGNATURE_MAPPING[it] } ?: run {
        when (this) {
            is ClassName -> "L" + this.canonicalName
                .replace("kotlin.Any", "java/lang/Object;")
                .replace('.', '/')
                .replace("kotlin", "java/lang") + ";"
            else -> throw UnsupportedOperationException("Not supported yet")
        }
    }
}

fun LambdaTypeName.getJniSignature(): String {
    val args = parameters.joinToString("") {
        it.type.getJniSignatureMapping()
    }

    val returnType = this.returnType.getJniSignatureMapping()

    return "($args)$returnType"
}

fun LambdaTypeName.getLambdaInterfaceTypeName(): String {
    val returnTypeString = returnType.getSimpleName()
    val args = this.parameters.joinToString("") { it.type.getSimpleName() }
    return "Lambda$returnTypeString$args"
}

fun String.getJniMethodCallMethodNameFromJniTypeName(): String {
    if (this == "void") {
        return "CallVoidMethod"
    }

    if (this == "jstring") {
        return "CallObjectMethod"
    }

    val type = this.drop(1).replaceFirstChar(Char::titlecase)
    return "Call${type}Method"
}