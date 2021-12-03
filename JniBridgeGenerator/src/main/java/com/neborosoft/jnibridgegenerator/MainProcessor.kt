package com.neborosoft.jnibridgegenerator

import com.google.auto.service.AutoService
import com.neborosoft.annotations.CppClass
import com.neborosoft.jnibridgegenerator.*
import com.neborosoft.jnibridgegenerator.Constants.CPP_TEMPLATE_CLASS_NAME
import com.neborosoft.jnibridgegenerator.Constants.CPP_TEMPLATE_H
import com.neborosoft.jnibridgegenerator.Constants.JNI_PUBLIC_INTERFACE_TOKEN
import com.neborosoft.jnibridgegenerator.Constants.KOTLIN_CLASS_IMPLEMENTATION_POSTFIX
import com.neborosoft.jnibridgegenerator.Constants.PTR
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.*
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@KotlinPoetMetadataPreview
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class MainProcessor : AbstractProcessor() {
    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        roundEnv?.getElementsAnnotatedWith(CppClass::class.java)?.forEach { element ->
            val className = element.simpleName.toString()
            val kotlinClassName = className + KOTLIN_CLASS_IMPLEMENTATION_POSTFIX
            val packageName = processingEnv.elementUtils.getPackageOf(element).toString()

            val typeMetadata = element.getAnnotation(Metadata::class.java) ?: return@forEach
            val kmClass = typeMetadata.toImmutableKmClass()
            require(kmClass.constructors.isEmpty()) {
                "invalid $className. @CppClass should be interface"
            }

            val methods = mutableListOf(
                Method(SpecialMethod.NEW_INSTANCE),
                Method(SpecialMethod.RELEASE)
            )

            kmClass.functions.mapTo(methods) {
                Method(it)
            }

            val kaptKotlinGeneratedDir = processingEnv.options["kapt.kotlin.generated"]
                ?: throw IllegalStateException("kapt.kotlin.generated is missing")
            val cppOutputDirectory = processingEnv.options["cpp.outputDirectory"]
                ?: throw IllegalStateException("cpp.outputDirectory is missing")

            val kotlinFile = File(kaptKotlinGeneratedDir)

            val kotlinClass = generateKotlinClass(
                methods,
                packageName,
                kotlinClassName
            )
            kotlinClass.writeTo(kotlinFile)

            System.out.println("kaptKotlinGeneratedDir=" + kaptKotlinGeneratedDir)
            System.out.println("cppOutputDirectory=" + cppOutputDirectory)

            val cppClass = generateCppTemplateHeader(
                methods = methods,
                cppClassName = className
            )
            val cppFile = File(cppOutputDirectory, "$className.h")
            cppFile.writeText(cppClass)

            val jniCode = generateJNIBridgeCalls(
                packageName = packageName,
                cppName = className,
                kotlinName = kotlinClassName,
                methods = methods
            )
            val jniCppFile = File(cppOutputDirectory, "$className.jni.cpp")
            jniCppFile.writeText(jniCode)

            copyResources(File(cppOutputDirectory))
        }

        processingEnv.options.forEach {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "${it.key}=${it.value}")
        }

        return true
    }

    fun generateKotlinClass(
        methods: List<Method>,
        packageName: String,
        kotlinClassName: String,
    ): FileSpec {
        val type = TypeSpec.classBuilder(kotlinClassName)
            .addProperty(
                PTR, LONG, KModifier.PRIVATE
            )
            .addFunctions(funSpecs = methods.flatMap {
                it.getKotlinSpecs()
            })
            .addInitializerBlock(
                CodeBlock.of("ptr = newInstance()\n")
            )
            .build()

        return FileSpec.builder(packageName, kotlinClassName)
            .addType(type).build()
    }

    fun generateCppTemplateHeader(
        methods: List<Method>,
        cppClassName: String
    ): String {
        val methodsDeclarations = methods.mapNotNull {
            it.getCppHeaderMethodDeclaration()
        }

        val cppTemplate = javaClass.classLoader.getResourceAsStream(CPP_TEMPLATE_H)
            ?.readIntoString()
            ?: throw IllegalStateException("$CPP_TEMPLATE_H resource not found")
        val index = cppTemplate.indexOf(JNI_PUBLIC_INTERFACE_TOKEN)
        val code = methodsDeclarations.joinToString("\n") {
            "        $it"
        } + "\n"
        return cppTemplate.insert(index + JNI_PUBLIC_INTERFACE_TOKEN.length, code)
            .replace(CPP_TEMPLATE_CLASS_NAME, cppClassName)
    }

    fun generateJNIBridgeCalls(
        packageName: String,
        cppName: String,
        kotlinName: String,
        methods: List<Method>
    ): String {
        val methodsCode = methods.map {
            it.getJniMethodCall(
                packageName = packageName,
                kotlinClassName = kotlinName,
                cppClassName = cppName
            )
        }.joinToString("\n\n")

        return """
        |#include <jni.h>
        |#include "Converters.h"
        |#include "$cppName.h"
        
        |$methodsCode
            
        """.trimMargin()
    }

    fun copyResources(dir: File) {
        val classLoader = javaClass.classLoader
        Constants.CPP_RESOURCES.forEach {
            classLoader.getResourceAsStream(it)?.copyTo(
                FileOutputStream(File(dir, it))
            ) ?: throw IllegalStateException("$it resource not found")
        }
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(CppClass::class.java.name)
    }
}