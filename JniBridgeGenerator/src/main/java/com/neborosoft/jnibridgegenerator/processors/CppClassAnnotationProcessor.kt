package com.neborosoft.jnibridgegenerator.processors

import com.neborosoft.annotations.CppClass
import com.neborosoft.jnibridgegenerator.*
import com.neborosoft.jnibridgegenerator.Constants.INCLUDE_START_TOKEN
import com.neborosoft.jnibridgegenerator.Constants.JNI_PUBLIC_INTERFACE_TOKEN
import com.neborosoft.jnibridgegenerator.methods.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import java.io.File
import java.util.*

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
        methods: List<CppMethodGenerator>,
        packageName: String,
        kotlinInterfaceName: String,
        kotlinClassName: String,
        generateNativeConstructor: Boolean
    ): FileSpec {
        val type = TypeSpec.classBuilder(kotlinClassName).apply {
            if (generateNativeConstructor) {
                primaryConstructor(
                    PropertySpec.builder(name = "ptr", type = LONG, KModifier.PRIVATE)
                        .mutable().build(),
                    PropertySpec.builder(name = "deleter", type = LONG, KModifier.PRIVATE)
                        .mutable().build()
                )
            } else {
                addProperty(
                    PropertySpec.builder(Constants.PTR, LONG, KModifier.PRIVATE)
                        .mutable()
                        .build()
                )

                addInitializerBlock(
                    CodeBlock.of("ptr = newInstance()\n")
                )
            }

            addFunctions(funSpecs = methods.flatMap {
                it.getKotlinSpecs()
            })

            addSuperinterface(ClassName(packageName = packageName, kotlinInterfaceName))
        }.build()

        return FileSpec.builder(packageName, kotlinClassName)
            .addType(type).build()
    }

    private fun generateCppTemplateHeader(
        existedClassCode: String?,
        methods: List<CppMethodGenerator>,
        includes: String,
        cppClassName: String
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
    }

    override fun processClass(
        className: String,
        packageName: String,
        kmClass: ImmutableKmClass,
        annotation: Annotation
    ) {
        require(annotation is CppClass)

        require(kmClass.constructors.isEmpty()) {
            "@CppClass processor failed. Invalid $className, should be interface"
        }

        require(kmClass.supertypes.contains { it.getTypeName().getSimpleName() == "Releasable" }) {
            "@CppClass processor failed. $className should inherit Releasable"
        }

        val kotlinInterfaceName = className
        val kotlinClassName = className + Constants.KOTLIN_CLASS_IMPLEMENTATION_POSTFIX

        val methods: MutableList<CppMethodGenerator> = if (annotation.withNativeConstructor) {
            mutableListOf(
                ReleaseCppMethodGenerator(includeDeleter = true)
            )
        } else {
            mutableListOf(
                NewInstanceCppMethodGenerator(),
                ReleaseCppMethodGenerator(includeDeleter = false)
            )
        }

        kmClass.functions.mapTo(methods) {
            RegularCppMethodGenerator(
                it,
                lambdaGenerator,
                generationPolicy = GenerationPolicy.WITH_CPP_PTR,
                annotationResolver = AnnotationsResolver.getClassAnnotationResolver(kmClass)
            )
        }

        val kotlinFile = File(kaptKotlinGeneratedDir)

        val kotlinClass = generateKotlinClass(
            methods,
            packageName,
            kotlinInterfaceName,
            kotlinClassName,
            generateNativeConstructor = annotation.withNativeConstructor
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
            existedClassCode = cppFile.tryReadText()
        )
        cppFile.writeText(cppClass)

        if (annotation.withNativeConstructor) {
            KotlinCppConstructorGenerator.addConstructor(
                className = kotlinClassName,
                cppClassName = className,
                packageName = packageName,
                params = listOf(
                    ConstructorParam(
                        name = "ptr",
                        type = LONG
                    ),
                    ConstructorParam(
                        name = "deleter",
                        type = LONG
                    )
                )
            )
        }
    }
}