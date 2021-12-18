package com.neborosoft.jnibridgegenerator.processors

import com.neborosoft.jnibridgegenerator.Utils
import com.neborosoft.jnibridgegenerator.methods.CppMethodGenerator
import com.neborosoft.jnibridgegenerator.methods.MethodGenerator
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

            processClass(className, packageName, kmClass, element.getAnnotation(annotation))
        }
    }

    protected abstract fun processClass(
        className: String, packageName: String, kmClass: ImmutableKmClass, annotation: Annotation
    )

    protected fun generateJNIBridgeCalls(
        packageName: String,
        cppName: String,
        kotlinName: String,
        methods: List<MethodGenerator>
    ): String {
        val jniMethods = methods.mapNotNull {
            it.getJniMethodCall(
                packageName = packageName,
                kotlinClassName = kotlinName,
                cppClassName = cppName
            )
        }
        if (jniMethods.isEmpty()) {
            return ""
        }

        val methodsCode = jniMethods.joinToString("\n\n")

        return """
        |#include <jni.h>
        |#include "Converters.h"
        |#include "$cppName.h"
        |#include "FunctionCallsBridge.h"
        
        |$methodsCode
            
        """.trimMargin()
    }
}