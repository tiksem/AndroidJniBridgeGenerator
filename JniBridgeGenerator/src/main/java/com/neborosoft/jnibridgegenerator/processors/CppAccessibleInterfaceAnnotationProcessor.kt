package com.neborosoft.jnibridgegenerator.processors

import com.neborosoft.annotations.CppAccessibleInterface
import com.neborosoft.jnibridgegenerator.*
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import java.io.File
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

        val methods = kmClass.functions.map {
            CppKotlinInterfaceWrapperProcessorMethod(it)
        }

        var customPath = annotation.customPath
        if (customPath.isNotEmpty()) {
            customPath = customPath.removeSuffix("/") + "/"
        }

        val cppClassName = annotation.cppClassName.takeIf { it.isNotEmpty() } ?: className
        val header = generateJObjectTemplateHeader(cppClassName, methods)
        File(cppOutputDirectory, "$customPath$cppClassName.h").writeText(header)

        val cpp = generateJObjectTemplateCpp(
            cppClassName = cppClassName,
            kotlinJniClassName = kmClass.name,
            methods = methods
        )
        File(cppOutputDirectory, "$customPath$cppClassName.cpp").writeText(cpp)

        bridgeInitCalls.add("    $cppClassName::init(env);\n")
        includes.add("#include \"$cppClassName.h\"\n")
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
        methods: List<CppKotlinInterfaceWrapperProcessorMethod>
    ): String {
        val cppTemplate = readResource(Constants.J_OBJECT_TEMPLATE_H)

        val code = methods.joinToString("\n") {
            "    " + it.getHeaderDeclaration()
        } + "\n"

        val index = cppTemplate.indexOf(Constants.JAVA_METHOD_WRAPPERS_TOKEN)
        return cppTemplate
            .insert(index + Constants.JAVA_METHOD_WRAPPERS_TOKEN.length, code)
            .replace(Constants.J_OBJECT_TEMPLATE_CLASS_NAME, cppClassName)
    }

    private fun generateJObjectTemplateCpp(
        cppClassName: String,
        kotlinJniClassName: String,
        methods: List<CppKotlinInterfaceWrapperProcessorMethod>
    ): String {
        val cppTemplate = readResource(Constants.J_OBJECT_TEMPLATE_CPP)
        val methodGenerationTemplate = cppTemplate.findStringBetweenQuotes(Constants.JAVA_METHOD_WRAPPERS_TOKEN)
        val methodsSource = methods.joinToString("\n\n") {
            it.getSourceDeclaration(methodGenerationTemplate)
        }

        val methodsIdGenerationTemplate = cppTemplate.findStringBetweenQuotes(
            Constants.JAVA_WRAPPER_METHODS_ID_GENERATION_TOKEN
        )
        val idGeneration = methods.joinToString("\n") {
            it.getMethodIdGeneration(methodsIdGenerationTemplate)
        }

        val methodsIdDeclarationTemplate = cppTemplate.findStringBetweenQuotes(
            Constants.JAVA_WRAPPER_METHODS_ID_DECLARATION_TOKEN
        )
        val idDeclaration = methods.joinToString("\n") {
            it.getMethodIdDeclaration(methodsIdDeclarationTemplate)
        }

        return cppTemplate
            .replace(methodGenerationTemplate, methodsSource)
            .replace(methodsIdGenerationTemplate, idGeneration)
            .replace(methodsIdDeclarationTemplate, idDeclaration)
            .replace("classname", kotlinJniClassName)
            .replace(Constants.J_OBJECT_TEMPLATE_CLASS_NAME, cppClassName)

    }
}