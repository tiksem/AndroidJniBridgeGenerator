package com.neborosoft.jnibridgegenerator.processors

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

    override fun processClass(className: String, packageName: String, kmClass: ImmutableKmClass) {
        val methods = kmClass.functions.map {
            CppKotlinInterfaceWrapperProcessorMethod(it)
        }

        val header = generateJObjectTemplateHeader(className, methods)
        File(cppOutputDirectory, "$className.h").writeText(header)

        val cpp = generateJObjectTemplateCpp(className, methods)
        File(cppOutputDirectory, "$className.cpp").writeText(cpp)

        bridgeInitCalls.add("    $className::init(env);\n")
        includes.add("#include \"$className.h\"\n")
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
            .replace(Constants.J_OBJECT_TEMPLATE_CLASS_NAME, cppClassName)

    }
}