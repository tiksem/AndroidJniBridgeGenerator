package com.neborosoft.jnibridgegenerator.processors

import com.neborosoft.annotations.CppAccessibleInterface
import com.neborosoft.annotations.CppClass
import com.neborosoft.jnibridgegenerator.TypesMapping
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

@KotlinPoetMetadataPreview
class CppTypeRegistrar(annotation: Class<out Annotation>,
                       kaptKotlinGeneratedDir:
                       String, cppOutputDirectory: String
) : BaseAnnotationProcessor(annotation, kaptKotlinGeneratedDir, cppOutputDirectory) {
    override fun processClass(
        className: String,
        packageName: String,
        kmClass: ImmutableKmClass,
        annotation: Annotation
    ) {
        var withNativeConstructor = false
        val cppClassName = when (annotation) {
            is CppAccessibleInterface -> {
                annotation.cppClassName.takeIf { it.isNotEmpty() } ?: className
            }
            is CppClass -> {
                if (!annotation.withNativeConstructor) {
                    return
                }
                withNativeConstructor = true
                className
            }
            else -> {
                throw UnsupportedOperationException("Unsupported annotation")
            }
        }
        TypesMapping.registerCppTypeMapping(
            kotlinTypeName = className,
            cppTypeName = cppClassName,
            hasJavaNativeConstructor = withNativeConstructor
        )
    }
}