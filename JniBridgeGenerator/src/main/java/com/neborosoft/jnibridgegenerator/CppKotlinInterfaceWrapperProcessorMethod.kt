package com.neborosoft.jnibridgegenerator

import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

@KotlinPoetMetadataPreview
class CppKotlinInterfaceWrapperProcessorMethod(
    private val kmFunction: ImmutableKmFunction
) {
    private val cppReturnType: String
    private val jniReturnType: String
    private val cppTypes: List<String>
    private val jniTypes: List<String>
    private val names: List<String>

    init {
        cppReturnType = kmFunction.returnType.getCppTypeName(convertFromCppToJni = true)
        jniReturnType = kmFunction.returnType.getTypeName().getJniTypeName()
        cppTypes = kmFunction.valueParameters.map {
            it.type!!.getCppTypeName(convertFromCppToJni = true)
                .addConstReferenceToCppTypeNameIfNotPrimitive()
        }
        names = kmFunction.valueParameters.map {
            it.name
        }
        jniTypes = kmFunction.valueParameters.map {
            it.type!!.getTypeName().getJniTypeName()
        }
    }

    private fun getJniMethodCallMethodName(): String {
        if (jniReturnType == "void") {
            return "CallVoidMethod"
        }

        if (jniReturnType == "jstring") {
            return "CallObjectMethod"
        }

        val type = jniReturnType.drop(1).replaceFirstChar(Char::titlecase)
        return "Call${type}Method"
    }

    fun getHeaderDeclaration(): String {
        return CodeGenerationUtils.getCppHeaderMethodDeclaration(
            methodName = kmFunction.name,
            returnType = cppReturnType,
            types = cppTypes,
            names = names
        )
    }

    fun getSourceDeclaration(template: String): String {
        val jniCallArgsList = ArrayList<String>()

        val converters = jniTypes.mapIndexed { index, type ->
            val name = names[index]
            val convertedName = "_$name"
            jniCallArgsList.add(convertedName)

            """
            |$type $convertedName = ConvertFromCppType<$type>(env, $name);   
            """.trimMargin()
        }.joinToString("\n")

        var convertedArgs = jniCallArgsList.joinToString(", ")
        if (convertedArgs.isNotEmpty()) {
            convertedArgs = ", $convertedArgs"
        }

        return template.let {
            if (cppReturnType == "void") {
                it.replace(
                    "auto res = ",
                    ""
                ).replace(
                    "    return ConvertToCppType<ReturnType>(env, res);\n",
                    ""
                ).replace("static_cast<JniReturnType>", "")
            } else {
                it
            }
        }.replace("JniReturnType", jniReturnType)
            .replace("ReturnType", cppReturnType)
            .replace("args", CodeGenerationUtils.generateCppMethodArgs(cppTypes, names))
            .replace("CallObjectMethod", getJniMethodCallMethodName())
            .replace("methodName", kmFunction.name)
            .replace("convertedArgs", convertedArgs)
            .replace("converters", converters)
    }

    fun getMethodIdGeneration(template: String): String {
        return template
            .replace("methodName", kmFunction.name)
            .replace("jvmSignature", getJvmSignature())
    }

    fun getMethodIdDeclaration(template: String): String {
        return template.replace("methodName", kmFunction.name)
    }

    fun getJvmSignature(): String {
        val str = kmFunction.signature!!.toString()
        return str.substring(startIndex = str.indexOf('('))
    }
}