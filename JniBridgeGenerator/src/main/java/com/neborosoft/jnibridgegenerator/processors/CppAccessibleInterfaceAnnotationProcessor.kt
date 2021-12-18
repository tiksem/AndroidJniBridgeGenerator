package com.neborosoft.jnibridgegenerator.processors

import com.neborosoft.annotations.CppAccessibleInterface
import com.neborosoft.jnibridgegenerator.*
import com.neborosoft.jnibridgegenerator.methods.MethodGenerator
import com.neborosoft.jnibridgegenerator.methods.KotlinMethodGenerator
import com.neborosoft.jnibridgegenerator.methods.RegularCppMethodGenerator
import com.squareup.kotlinpoet.metadata.*
import java.io.File
import java.lang.IllegalStateException
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

@KotlinPoetMetadataPreview
class CppAccessibleInterfaceAnnotationProcessor(
    annotation: Class<out Annotation>,
    kaptKotlinGeneratedDir: String,
    cppOutputDirectory: String
) : BaseAnnotationProcessor(
    annotation, kaptKotlinGeneratedDir, cppOutputDirectory
) {
    private val bridgeInitCalls = ArrayList<String>()
    private val includes = ArrayList<String>()

    override fun processClass(
        className: String,
        packageName: String,
        kmClass: ImmutableKmClass,
        annotation: Annotation
    ) {
        require(annotation is CppAccessibleInterface)

        val methods = kmClass.functions.mapNotNull {
            when {
                it.returnType.annotations.find {
                    it.className.endsWith("SkipMethod")
                } != null -> null

                it.isExternal && (!annotation.isSingleton || it.name != "nativeInit") -> {
                    RegularCppMethodGenerator(
                        kmFunction = it,
                        lambdaGenerator = LambdaGenerator(kaptKotlinGeneratedDir),
                        generateCppPtr = false
                    )
                }

                !it.isPrivate -> {
                    KotlinMethodGenerator(it)
                }

                else -> null
            }
        }

        if (annotation.isSingleton) {
            require(kmClass.isObject) {
                throw IllegalStateException("$className Singleton should be object")
            }

            val nativeInitMethod = kmClass.functions.find {
                it.name == "nativeInit"
            }

            require(nativeInitMethod != null) {
                throw IllegalStateException("$className should contain nativeInit method, cause it's singleton")
            }

            require(nativeInitMethod.isExternal) {
                throw IllegalStateException("$className.nativeInit should be external")
            }
        }

        var customPath = annotation.customPath
        if (customPath.isNotEmpty()) {
            customPath = customPath.removeSuffix("/") + "/"
        }

        val cppClassName = annotation.cppClassName.takeIf { it.isNotEmpty() } ?: className
        val header = generateJObjectTemplateHeader(
            cppClassName = cppClassName,
            methods = methods,
            isSingleton = annotation.isSingleton
        )
        File(cppOutputDirectory, "$customPath$cppClassName.h").writeText(header)

        val cpp = generateJObjectTemplateCpp(
            cppClassName = cppClassName,
            kotlinClassName = className,
            kotlinJniClassName = kmClass.name,
            isSingleton = annotation.isSingleton,
            packageName = packageName,
            methods = methods
        )
        File(cppOutputDirectory, "$customPath$cppClassName.cpp").writeText(cpp)

        if (!annotation.isSingleton) {
            bridgeInitCalls.add("    $cppClassName::init(env);\n")
        }
        includes.add("#include \"$cppClassName.h\"\n")

        val jniCode = generateJNIBridgeCalls(
            packageName = packageName,
            cppName = cppClassName,
            kotlinName = className,
            methods = methods
        )

        val headersList = methods.flatMap {
            it.getRequestedCppHeaders()
        }

        val headers = headersList.distinct().joinToString("\n") {
            "#include \"$it.h\""
        } + "\n"

        if (jniCode.isNotEmpty()) {
            val jniCppFile = File(cppOutputDirectory, "$customPath$cppClassName.jni.cpp")
            jniCppFile.writeText(headers + jniCode)
        }
    }

    override fun process(processingEnv: ProcessingEnvironment, roundEnv: RoundEnvironment?) {
        super.process(processingEnv, roundEnv)

        val file = File(cppOutputDirectory, "JNIBridgeInit.h")
        val bridgeInitTemplate = file.readText()

        val code = bridgeInitTemplate.insertAfter(
            token = "// Register JObjects\n",
            string = bridgeInitCalls.joinToString("")
        ).insertAfter(
            token = "#include <jni.h>\n",
            string = includes.joinToString("")
        )

        file.writeText(code)
    }

    private fun generateJObjectTemplateHeader(
        cppClassName: String,
        isSingleton: Boolean,
        methods: List<MethodGenerator>
    ): String {
        val cppTemplate = readResource(if (isSingleton) {
            Constants.J_OBJECT_SINGLETON_TEMPLATE_H
        } else {
            Constants.J_OBJECT_TEMPLATE_H
        })

        val code = methods.mapNotNull {
            it.getCppHeaderMethodDeclaration()?.let { "    $it" }
        }.joinToString("\n") + "\n"

        val index = cppTemplate.indexOf(Constants.JAVA_METHOD_WRAPPERS_TOKEN)
        var res = cppTemplate
            .insert(index + Constants.JAVA_METHOD_WRAPPERS_TOKEN.length, code)

        if (isSingleton) {
            res = res.replace("JObjectSingletonTemplate", cppClassName)
        } else {
            res = res.replace("JObjectTemplate", cppClassName)
        }

        return res
    }

    private fun generateJObjectTemplateCpp(
        cppClassName: String,
        kotlinClassName: String,
        kotlinJniClassName: String,
        isSingleton: Boolean,
        packageName: String,
        methods: List<MethodGenerator>
    ): String {
        val cppTemplate = readResource(if (isSingleton) {
            Constants.J_OBJECT_SINGLETON_TEMPLATE_CPP
        } else {
            Constants.J_OBJECT_TEMPLATE_CPP
        })
        val methodGenerationTemplate = cppTemplate.findStringBetweenQuotes(Constants.JAVA_METHOD_WRAPPERS_TOKEN)
        val methodsSource = methods.mapNotNull {
            it.getSourceDeclaration(methodGenerationTemplate)
        }.joinToString("\n\n")

        val methodsIdGenerationTemplate = cppTemplate.findStringBetweenQuotes(
            Constants.JAVA_WRAPPER_METHODS_ID_GENERATION_TOKEN
        )
        val idGeneration = methods.mapNotNull {
            it.getMethodIdGeneration(methodsIdGenerationTemplate)
        }.joinToString("\n")

        val methodsIdDeclarationTemplate = cppTemplate.findStringBetweenQuotes(
            Constants.JAVA_WRAPPER_METHODS_ID_DECLARATION_TOKEN
        )
        val idDeclaration = methods.mapNotNull {
            it.getMethodIdDeclaration(methodsIdDeclarationTemplate)
        }.joinToString("\n")

        var res = cppTemplate
            .replace(methodGenerationTemplate, methodsSource)
            .replace(methodsIdGenerationTemplate, idGeneration)
            .replace(methodsIdDeclarationTemplate, idDeclaration)
            .replace("classname", kotlinJniClassName)

        if (isSingleton) {
            val modifiedPackageName = packageName.replace('.', '_')
            res = res.replace("JniInitCall", "Java_${modifiedPackageName}_${kotlinClassName}_nativeInit")
            res = res.replace("JObjectSingletonTemplate", cppClassName)
        } else {
            res = res.replace("JObjectTemplate", cppClassName)
        }

        return res
    }
}