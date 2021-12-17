package com.neborosoft.jnibridgegenerator.processors

import com.neborosoft.annotations.CppAccessibleInterface
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
        require(annotation is CppAccessibleInterface)
        val cppClassName = annotation.cppClassName.takeIf { it.isNotEmpty() } ?: className
        TypesMapping.registerCppTypeMapping(kotlinTypeName = className, cppTypeName = cppClassName)
    }
}