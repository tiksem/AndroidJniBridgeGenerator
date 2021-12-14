package com.neborosoft.jnibridgegenerator.processors

import com.neborosoft.annotations.CppClass
import com.neborosoft.jnibridgegenerator.*
import com.neborosoft.jnibridgegenerator.Constants.INCLUDE_START_TOKEN
import com.neborosoft.jnibridgegenerator.Constants.JNI_PUBLIC_INTERFACE_TOKEN
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import java.io.File

@KotlinPoetMetadataPreview
class CppClassAnnotationProcessor(
    annotation: Class<out Annotation>,
    kaptKotlinGeneratedDir: String,
    cppOutputDirectory: String,
    private val lambdaGenerator: LambdaGenerator
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
                PropertySpec.builder(Constants.PTR, LONG, KModifier.PRIVATE)
                    .mutable()
                    .build()
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
        existedClassCode: String?,
        methods: List<CppClassProcessorMethod>,
        includes: String,
        cppClassName: String,
        base: String
    ): String {
        val methodsDeclarations = methods.mapNotNull {
            it.getCppHeaderMethodDeclaration()
        }

        val cppTemplate = readResource(Constants.CPP_TEMPLATE_H)
        val code = methodsDeclarations.joinToString("\n") {
            "        $it"
        } + "\n"

        val replacement = existedClassCode ?: cppTemplate

        return replacement.replaceStringBetweenTokens(
            token1 = JNI_PUBLIC_INTERFACE_TOKEN,
            token2 = JNI_PUBLIC_INTERFACE_TOKEN,
            replacement = code
        ).replaceStringBetweenTokens(
            token1 = INCLUDE_START_TOKEN,
            token2 = INCLUDE_START_TOKEN,
            replacement = includes
        ).replace(Constants.CPP_TEMPLATE_CLASS_NAME, cppClassName)
            .replaceStringBetweenTokens(
                token1 = ": public ",
                token2 = " ",
                base,
                replaceTokens = base.isEmpty()
            )
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
        |#include "FunctionCallsBridge.h"
        
        |$methodsCode
            
        """.trimMargin()
    }

    override fun processClass(
        className: String,
        packageName: String,
        kmClass: ImmutableKmClass,
        annotation: Annotation
    ) {
        require(annotation is CppClass)

        val kotlinClassName = className + Constants.KOTLIN_CLASS_IMPLEMENTATION_POSTFIX

        val methods = mutableListOf(
            CppClassProcessorMethod(SpecialMethod.NEW_INSTANCE),
            CppClassProcessorMethod(SpecialMethod.RELEASE)
        )

        kmClass.functions.mapTo(methods) {
            CppClassProcessorMethod(it, lambdaGenerator)
        }

        val kotlinFile = File(kaptKotlinGeneratedDir)

        val kotlinClass = generateKotlinClass(
            methods,
            packageName,
            kotlinClassName
        )
        kotlinClass.writeTo(kotlinFile)

        val jniCode = generateJNIBridgeCalls(
            packageName = packageName,
            cppName = className,
            kotlinName = kotlinClassName,
            methods = methods
        )

        var headersList = methods.flatMap {
            it.getRequestedCppHeaders()
        }

        if (annotation.base.isNotEmpty()) {
            headersList = headersList.withPrependedItem(annotation.base)
        }

        val headers = headersList.distinct().joinToString("\n") {
            "#include \"$it.h\""
        } + "\n"

        var customPath = annotation.customPath
        if (customPath.isNotEmpty()) {
            customPath = customPath.removeSuffix("/") + "/"
        }

        val jniCppFile = File(cppOutputDirectory, "$customPath$className.jni.cpp")
        jniCppFile.writeText(headers + jniCode)

        val cppFile = File(cppOutputDirectory, "$customPath$className.h")
        val cppClass = generateCppTemplateHeader(
            methods = methods,
            cppClassName = className,
            includes = headers,
            base = annotation.base,
            existedClassCode = cppFile.tryReadText()
        )
        cppFile.writeText(cppClass)
    }
}