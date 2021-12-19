package com.neborosoft.jnibridgegenerator.processors

import com.neborosoft.jnibridgegenerator.AnnotationsResolver
import com.neborosoft.jnibridgegenerator.Utils
import com.neborosoft.jnibridgegenerator.methods.CppMethodGenerator
import com.neborosoft.jnibridgegenerator.methods.MethodGenerator
import com.neborosoft.jnibridgegenerator.replaceStringBetweenTokens
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement

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

            AnnotationsResolver.registerClass(
                kmClass = kmClass,
                functions = element.enclosedElements.mapNotNull {
                    it as? ExecutableElement
                }
            )

            processClass(className, packageName, kmClass, element.getAnnotation(annotation))
        }
    }

    protected abstract fun processClass(
        className: String, packageName: String, kmClass: ImmutableKmClass, annotation: Annotation
    )

    private fun generateHeadersCode(methods: List<MethodGenerator>): String {
        val headersList = methods.flatMap {
            it.getRequestedCppHeaders()
        }

        return headersList.distinct().joinToString("\n") {
            "#include \"$it.h\""
        } + "\n"
    }

    protected fun getCustomPathPrefix(customPath: String): String {
        return if (customPath.isNotEmpty()) {
            customPath.removeSuffix("/") + "/"
        } else {
            ""
        }
    }

    protected fun writeCppFunctions(
        packageName: String,
        namespace: String,
        kotlinClassName: String,
        customPathPrefix: String,
        cppFunctions: List<MethodGenerator>
    ) {
        if (cppFunctions.isNotEmpty()) {
            val template = readResource("CppFunctionsNamespaceHeaderTemplate.h")
            val headers = generateHeadersCode(cppFunctions)
            val code = template.replaceStringBetweenTokens(
                token1 = "namespace CppFunctionsNamespaceHeaderTemplate {\n",
                token2 = "}",
                replacement = cppFunctions.mapNotNull {
                    it.getCppHeaderMethodDeclaration()?.let { c ->
                        "    $c"
                    }
                }.joinToString("\n") + "\n"
            ).replace(
                "CppFunctionsNamespaceHeaderTemplate",
                namespace
            ).replaceStringBetweenTokens(
                token1 = "// headers\n",
                token2 = "// headers",
                replacement = headers
            )

            val file = File(cppOutputDirectory, "$customPathPrefix$namespace.h")
            file.writeText(code)

            val cppFunctionsJniCode = generateJNIBridgeCalls(
                packageName = packageName,
                cppName = namespace,
                kotlinName = kotlinClassName,
                methods = cppFunctions
            )

            val jniFunctionsCppFile = File(
                cppOutputDirectory,
                "$customPathPrefix$namespace.jni.cpp"
            )
            jniFunctionsCppFile.writeText(headers + cppFunctionsJniCode)
        }
    }

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