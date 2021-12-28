package com.neborosoft.jnibridgegenerator.processors

import com.neborosoft.jnibridgegenerator.*
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import kotlin.collections.getOrNull

@KotlinPoetMetadataPreview
class KotlinConstructorAnnotationProcessor(
    annotation: Class<out Annotation>,
    kaptKotlinGeneratedDir: String,
    cppOutputDirectory: String
) : BaseAnnotationProcessor(annotation, kaptKotlinGeneratedDir, cppOutputDirectory, false) {
    override fun processClass(
        className: String,
        packageName: String,
        kmClass: ImmutableKmClass,
        annotation: Annotation
    ) {
        KotlinCppConstructorGenerator.addConstructor(kmClass, className, packageName)
    }
}