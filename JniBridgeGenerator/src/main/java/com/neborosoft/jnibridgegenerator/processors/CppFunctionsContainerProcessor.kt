package com.neborosoft.jnibridgegenerator.processors

import com.neborosoft.jnibridgegenerator.AnnotationsResolver
import com.neborosoft.jnibridgegenerator.LambdaGenerator
import com.neborosoft.jnibridgegenerator.methods.GenerationPolicy
import com.neborosoft.jnibridgegenerator.methods.RegularCppMethodGenerator
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isExternal

@KotlinPoetMetadataPreview
class CppFunctionsContainerProcessor(
    annotation: Class<out Annotation>,
    kaptKotlinGeneratedDir: String,
    cppOutputDirectory: String,
    private val lambdaGenerator: LambdaGenerator
) : BaseAnnotationProcessor(annotation, kaptKotlinGeneratedDir, cppOutputDirectory) {
    override fun processClass(
        className: String,
        packageName: String,
        kmClass: ImmutableKmClass,
        annotation: Annotation
    ) {
        val functions = kmClass.functions.mapNotNull {
            if (it.isExternal) {
                RegularCppMethodGenerator(
                    kmFunction = it,
                    lambdaGenerator = lambdaGenerator,
                    generationPolicy = GenerationPolicy.EXTERNAL_FUNCTION,
                    annotationResolver = AnnotationsResolver.getClassAnnotationResolver(kmClass)
                )
            } else {
                null
            }
        }

        writeCppFunctions(
            packageName = packageName,
            namespace = className + "Functions",
            kotlinClassName = className,
            customPathPrefix = "",
            cppFunctions = functions
        )
    }
}