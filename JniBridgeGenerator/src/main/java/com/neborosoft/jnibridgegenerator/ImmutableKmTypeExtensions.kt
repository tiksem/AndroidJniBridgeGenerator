package com.neborosoft.jnibridgegenerator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.metadata.KmClassifier

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
        return className.parameterizedBy(
            typeArguments = arguments
        )
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

@KotlinPoetMetadataPreview
fun ImmutableKmType.getCppTypeName(isReturn: Boolean): String {
    val kotlinTypeName = getStringTypeNameWithoutDots()
    val mapping = if (isReturn) {
        DEFAULT_KOTLIN_TO_CPP_RETURN_TYPES_MAPPING
    } else {
        DEFAULT_KOTLIN_TO_CPP_TYPES_MAPPING
    }
    return mapping[kotlinTypeName] ?: run {
        annotations.find {
            it.className.endsWith("CppParam")
        }?.arguments?.get("cppType")?.value?.toString() ?:
        throw IllegalStateException("Unable to find cpp mapping type for $kotlinTypeName")
    }
}