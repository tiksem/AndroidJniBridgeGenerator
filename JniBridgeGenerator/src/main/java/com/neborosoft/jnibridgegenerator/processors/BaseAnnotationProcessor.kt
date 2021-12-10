package com.neborosoft.jnibridgegenerator.processors

import com.neborosoft.jnibridgegenerator.Utils
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

@KotlinPoetMetadataPreview
abstract class BaseAnnotationProcessor(
    private val annotation: Class<out Annotation>,
    protected val kaptKotlinGeneratedDir: String,
    protected val cppOutputDirectory: String
) {
    protected fun readResource(resourceName: String): String {
        return Utils.readResource(resourceName)
    }

    open fun process(
        processingEnv: ProcessingEnvironment,
        roundEnv: RoundEnvironment?
    ) {
        roundEnv?.getElementsAnnotatedWith(annotation)?.forEach { element ->
            val className = element.simpleName.toString()
            val packageName = processingEnv.elementUtils.getPackageOf(element).toString()

            val typeMetadata = element.getAnnotation(Metadata::class.java) ?: return@forEach
            val kmClass = typeMetadata.toImmutableKmClass()
            require(kmClass.constructors.isEmpty()) {
                "invalid $className, should be interface"
            }

            processClass(className, packageName, kmClass, element.getAnnotation(annotation))
        }
    }

    protected abstract fun processClass(
        className: String, packageName: String, kmClass: ImmutableKmClass, annotation: Annotation
    )
}