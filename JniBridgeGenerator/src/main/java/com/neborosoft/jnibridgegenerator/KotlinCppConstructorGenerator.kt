package com.neborosoft.jnibridgegenerator

import com.neborosoft.jnibridgegenerator.Utils.readResource
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import java.io.File

private const val ID_TOKEN = "// id\n"
private const val CLASS_TOKEN = "// class\n"
private const val INIT_CONSTRUCTOR_TOKEN = "// Init constructor\n"
private const val CONSTRUCTORS_TEMPLATE = "// Constructors\n"
private const val HEADERS = "// headers\n"

data class Constructor(
    val id: String,
    val clazz: String,
    val init: String,
    val h: String,
    val cpp: String,
    val headers: String?
)

data class ConstructorParam(
    val name: String,
    val type: TypeName
) {
    fun isCppPtr(): Boolean {
        return name == Constants.PTR && type == LONG
    }
}

@KotlinPoetMetadataPreview
object KotlinCppConstructorGenerator {
    private val kotlinConstructorsH: String
    private val kotlinConstructorsCpp: String

    private val idTemplate: String
    private val classTemplate: String
    private val initConstructorTemplate: String
    private val constructorCppTemplate: String
    private val constructorHeaderTemplate: String

    private val constructors = ArrayList<Constructor>()

    init {
        kotlinConstructorsCpp = readResource("KotlinConstructors.cpp")
        kotlinConstructorsH = readResource("KotlinConstructors.h")

        idTemplate = kotlinConstructorsCpp.findStringBetweenQuotes(ID_TOKEN)
        classTemplate = kotlinConstructorsCpp.findStringBetweenQuotes(CLASS_TOKEN)
        initConstructorTemplate = kotlinConstructorsCpp.findStringBetweenQuotes(
            INIT_CONSTRUCTOR_TOKEN
        )
        constructorHeaderTemplate = kotlinConstructorsH.findStringBetweenQuotes(
            CONSTRUCTORS_TEMPLATE
        )
        constructorCppTemplate = kotlinConstructorsCpp.findStringBetweenQuotes(
            CONSTRUCTORS_TEMPLATE
        )
    }

    private fun getJniSignature(params: List<ConstructorParam>): String {
        val args = params.joinToString("") {
            it.type.getJniSignatureMapping()
        }

        return "($args)V"
    }

    fun addConstructor(
        className: String,
        cppClassName: String?,
        packageName: String,
        params: List<ConstructorParam>, jniSignature: String? = null
    ) {
        val constructorArgs = ArrayList<String>()
        val constructorCallArgs = ArrayList<String>()
        val converters = ArrayList<String>()

        params.forEach {
            val cppType = if (it.isCppPtr()) {
                "$cppClassName*"
            } else {
                it.type.getCppTypeName(convertFromCppToJni = true, cppParam = null)
                    .addConstReferenceToCppTypeNameIfRequired()
            }
            constructorArgs.add("$cppType ${it.name}")
            constructorCallArgs.add("_" + it.name)

            val jniType = it.type.getJniTypeName()
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

        val cppClassNameToken = cppClassName ?: className

        val constructorCode = Constructor(
            id = idTemplate.replace("KotlinObject", cppClassNameToken),
            clazz = classTemplate.replace("KotlinObject", cppClassNameToken),
            init = initConstructorTemplate
                .replace("KotlinObject", cppClassNameToken)
                .replace(
                    "kotlinclassname",
                    "${packageName.replace('.', '/')}/$className"
                ).replace("jniSignature", jniSignature ?: getJniSignature(params)),
            h = constructorHeaderTemplate
                .replace("KotlinObject", cppClassNameToken)
                .replace("___Args", constructorArgsStr),
            cpp = constructorCppTemplate
                .replace("KotlinObject", cppClassNameToken)
                .replace("___Args", constructorArgsStr)
                .replace("___args", constructorCallArgsStr)
                .replace("// converters\n", convertersStr),
            headers = cppClassName?.let {
                "#include \"$it.h\""
            }
        )

        constructors.add(constructorCode)
    }

    fun addConstructor(kmClass: ImmutableKmClass, className: String, packageName: String) {
        val constructor = kmClass.constructors.getOrNull(0)
            ?: throw IllegalStateException("$className doesn't have constructors")

        val signature = constructor.signature?.toString()?.removePrefix("<init>")

        addConstructor(
            className = className,
            cppClassName = null,
            packageName = packageName,
            params = constructor.valueParameters.map {
                ConstructorParam(name = it.name, type = it.type!!.getTypeName())
            },
            jniSignature = signature
        )
    }

    fun generateConstructors(cppOutputDirectory: String) {
        val h = kotlinConstructorsH.replaceStringBetweenTokens(
            CONSTRUCTORS_TEMPLATE,
            constructors.joinToString("\n") { it.h }
        ).replaceStringBetweenTokens(HEADERS, constructors.mapNotNull {
            it.headers
        }.joinToString("\n") + '\n')
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