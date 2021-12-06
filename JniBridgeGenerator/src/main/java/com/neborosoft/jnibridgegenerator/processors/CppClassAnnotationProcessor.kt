package com.neborosoft.jnibridgegenerator.processors

import com.neborosoft.jnibridgegenerator.Constants
import com.neborosoft.jnibridgegenerator.CppClassProcessorMethod
import com.neborosoft.jnibridgegenerator.SpecialMethod
import com.neborosoft.jnibridgegenerator.insert
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import java.io.File

@KotlinPoetMetadataPreview
class CppClassAnnotationProcessor(
    annotation: Class<out Annotation>,
    kaptKotlinGeneratedDir: String,
    cppOutputDirectory: String,
) : BaseAnnotationProcessor(
    annotation,
    kaptKotlinGeneratedDir,
    cppOutputDirectory
) {
    private fun generateKotlinClass(
        methods: List<CppClassProcessorMethod>,
        packageName: String,
        kotlinClassName: String,
    ): FileSpec {
        val type = TypeSpec.classBuilder(kotlinClassName)
            .addProperty(
                Constants.PTR, LONG, KModifier.PRIVATE
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

    private fun generateCppTemplateHeader(
        methods: List<CppClassProcessorMethod>,
        cppClassName: String
    ): String {
        val methodsDeclarations = methods.mapNotNull {
            it.getCppHeaderMethodDeclaration()
        }

        val cppTemplate = readResource(Constants.CPP_TEMPLATE_H)
        val index = cppTemplate.indexOf(Constants.JNI_PUBLIC_INTERFACE_TOKEN)
        val code = methodsDeclarations.joinToString("\n") {
            "        $it"
        } + "\n"
        return cppTemplate.insert(index + Constants.JNI_PUBLIC_INTERFACE_TOKEN.length, code)
            .replace(Constants.CPP_TEMPLATE_CLASS_NAME, cppClassName)
    }

    private fun generateJNIBridgeCalls(
        packageName: String,
        cppName: String,
        kotlinName: String,
        methods: List<CppClassProcessorMethod>
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

    override fun processClass(className: String, packageName: String, kmClass: ImmutableKmClass) {
        val kotlinClassName = className + Constants.KOTLIN_CLASS_IMPLEMENTATION_POSTFIX

        val methods = mutableListOf(
            CppClassProcessorMethod(SpecialMethod.NEW_INSTANCE),
            CppClassProcessorMethod(SpecialMethod.RELEASE)
        )

        kmClass.functions.mapTo(methods) {
            CppClassProcessorMethod(it)
        }

        val kotlinFile = File(kaptKotlinGeneratedDir)

        val kotlinClass = generateKotlinClass(
            methods,
            packageName,
            kotlinClassName
        )
        kotlinClass.writeTo(kotlinFile)

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
    }
}