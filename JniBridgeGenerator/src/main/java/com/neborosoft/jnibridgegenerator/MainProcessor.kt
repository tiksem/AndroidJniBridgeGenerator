package com.neborosoft.jnibridgegenerator

import com.google.auto.service.AutoService
import com.neborosoft.annotations.CppAccessibleInterface
import com.neborosoft.annotations.CppClass
import com.neborosoft.jnibridgegenerator.*
import com.neborosoft.jnibridgegenerator.processors.CppAccessibleInterfaceAnnotationProcessor
import com.neborosoft.jnibridgegenerator.processors.CppClassAnnotationProcessor
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.*
import java.io.File
import java.io.FileOutputStream
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@KotlinPoetMetadataPreview
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class MainProcessor : AbstractProcessor() {
    private var kaptKotlinGeneratedDir = ""
    private var cppOutputDirectory = ""

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        if (annotations.isNullOrEmpty()) {
            return false
        }

        kaptKotlinGeneratedDir = processingEnv.options["kapt.kotlin.generated"]
            ?: throw IllegalStateException("kapt.kotlin.generated is missing")
        cppOutputDirectory = processingEnv.options["cpp.outputDirectory"]
            ?: throw IllegalStateException("cpp.outputDirectory is missing")

        copyResources(File(cppOutputDirectory))

        CppClassAnnotationProcessor(
            CppClass::class.java, kaptKotlinGeneratedDir, cppOutputDirectory
        ).process(processingEnv, roundEnv)

        CppAccessibleInterfaceAnnotationProcessor(
            CppAccessibleInterface::class.java, kaptKotlinGeneratedDir, cppOutputDirectory
        ).process(processingEnv, roundEnv)

        return true
    }

    private fun copyResources(dir: File) {
        val classLoader = javaClass.classLoader
        Constants.CPP_RESOURCES.forEach {
            classLoader.getResourceAsStream(it)?.copyTo(
                FileOutputStream(File(dir, it))
            ) ?: throw IllegalStateException("$it resource not found")
        }
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(CppClass::class.java.name, CppAccessibleInterface::class.java.name)
    }
}