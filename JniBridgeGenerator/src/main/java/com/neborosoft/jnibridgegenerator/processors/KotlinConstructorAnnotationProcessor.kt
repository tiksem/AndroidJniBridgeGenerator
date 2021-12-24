package com.neborosoft.jnibridgegenerator.processors

import com.neborosoft.jnibridgegenerator.*
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import kotlin.collections.getOrNull

private const val ID_TOKEN = "// id\n"
private const val CLASS_TOKEN = "// class\n"
private const val INIT_CONSTRUCTOR_TOKEN = "// Init constructor\n"
private const val CONSTRUCTORS_TEMPLATE = "// Constructors\n"

private data class Constructor(
    val id: String,
    val clazz: String,
    val init: String,
    val h: String,
    val cpp: String
)

@KotlinPoetMetadataPreview
class KotlinConstructorAnnotationProcessor(
    annotation: Class<out Annotation>,
    kaptKotlinGeneratedDir: String,
    cppOutputDirectory: String
) : BaseAnnotationProcessor(annotation, kaptKotlinGeneratedDir, cppOutputDirectory, false) {
    private lateinit var idTemplate: String
    private lateinit var classTemplate: String
    private lateinit var initConstructorTemplate: String
    private lateinit var constructorCppTemplate: String
    private lateinit var constructorHeaderTemplate: String
    private val constructors = ArrayList<Constructor>()

    override fun processClass(
        className: String,
        packageName: String,
        kmClass: ImmutableKmClass,
        annotation: Annotation
    ) {
        val constructor = kmClass.constructors.getOrNull(0)
            ?: throw IllegalStateException("$className doesn't have constructors")

        val signature = constructor.signature?.toString()?.removePrefix("<init>")
            ?: throw IllegalStateException("Construct doesn't have jni signature")

        val constructorArgs = ArrayList<String>()
        val constructorCallArgs = ArrayList<String>()
        val converters = ArrayList<String>()

        constructor.valueParameters.forEach {
            val cppType = it.type!!.getCppTypeName(convertFromCppToJni = true, cppParam = null)
            constructorArgs.add("${cppType.addConstReferenceToCppTypeNameIfRequired()} ${it.name}")
            constructorCallArgs.add("_" + it.name)

            val jniType = it.type!!.getTypeName().getJniTypeName()
            val converter = "    auto _${it.name} = ConvertFromCppType<$jniType>(env, ${it.name});"
            converters.add(converter)
        }

        var constructorArgsStr = ""
        var constructorCallArgsStr = ""
        if (constructorArgs.isNotEmpty()) {
            constructorArgsStr = ", " + constructorArgs.joinToString(", ")
            constructorCallArgsStr = ", " + constructorCallArgs.joinToString(", ")
        }

        val convertersStr = converters.joinToString("\n") + '\n'

        val constructorCode = Constructor(
            id = idTemplate.replace("KotlinObject", className),
            clazz = classTemplate.replace("KotlinObject", className),
            init = initConstructorTemplate
                .replace("KotlinObject", className)
                .replace("kotlinclassname",
                "${packageName.replace('.', '/')}/$className"
                ).replace("jniSignature", signature),
            h = constructorHeaderTemplate
                .replace("KotlinObject", className)
                .replace("___Args", constructorArgsStr),
            cpp = constructorCppTemplate
                .replace("KotlinObject", className)
                .replace("___Args", constructorArgsStr)
                .replace("___args", constructorCallArgsStr)
                .replace("// converters\n",  convertersStr)
        )

        constructors.add(constructorCode)
    }

    override fun process(processingEnv: ProcessingEnvironment, roundEnv: RoundEnvironment?) {
        val kotlinConstructorsCpp = readResource("KotlinConstructors.cpp")
        val kotlinConstructorsH = readResource("KotlinConstructors.h")
        idTemplate = kotlinConstructorsCpp.findStringBetweenQuotes(ID_TOKEN)
        classTemplate = kotlinConstructorsCpp.findStringBetweenQuotes(CLASS_TOKEN)
        initConstructorTemplate = kotlinConstructorsCpp.findStringBetweenQuotes(INIT_CONSTRUCTOR_TOKEN)
        constructorHeaderTemplate = kotlinConstructorsH.findStringBetweenQuotes(
            CONSTRUCTORS_TEMPLATE
        )
        constructorCppTemplate = kotlinConstructorsCpp.findStringBetweenQuotes(
            CONSTRUCTORS_TEMPLATE
        )

        super.process(processingEnv, roundEnv)

        val h = kotlinConstructorsH.replaceStringBetweenTokens(
            CONSTRUCTORS_TEMPLATE,
            constructors.joinToString("\n") { it.h }
        )
        File(cppOutputDirectory, "KotlinConstructors.h").writeText(h)

        val cpp = kotlinConstructorsCpp.replaceStringBetweenTokens(
            ID_TOKEN,
            constructors.joinToString("\n") { it.id }
        ).replaceStringBetweenTokens(
            CLASS_TOKEN,
            constructors.joinToString("\n") { it.clazz }
        ).replaceStringBetweenTokens(
            INIT_CONSTRUCTOR_TOKEN,
            constructors.joinToString("\n") { it.init }
        ).replaceStringBetweenTokens(
            CONSTRUCTORS_TEMPLATE,
            constructors.joinToString("\n") { it.cpp }
        )
        File(cppOutputDirectory, "KotlinConstructors.cpp").writeText(cpp)
    }
}