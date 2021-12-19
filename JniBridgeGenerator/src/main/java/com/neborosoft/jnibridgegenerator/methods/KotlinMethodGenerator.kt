package com.neborosoft.jnibridgegenerator.methods

import com.neborosoft.annotations.CppParam
import com.neborosoft.jnibridgegenerator.*
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

@KotlinPoetMetadataPreview
class KotlinMethodGenerator(
    private val kmFunction: ImmutableKmFunction,
    private val annotationResolver: ClassAnnotationResolver
): MethodGenerator {
    private val cppReturnType: String
    private val jniReturnType: String
    private val cppTypes: List<String>
    private val jniTypes: List<String>
    private val names: List<String>
    private val parameterAnnotationResolver =
        annotationResolver.getParameterAnnotationResolver(kmFunction)

    init {
        val cppReturnParam = annotationResolver.getAnnotation(kmFunction, CppParam::class.java)
        cppReturnType = kmFunction.returnType.getCppTypeName(
            convertFromCppToJni = true,
            cppParam = cppReturnParam
        )
        jniReturnType = kmFunction.returnType.getTypeName().getJniTypeName()
        cppTypes = kmFunction.valueParameters.mapIndexed { index, it ->
            val cppParam = parameterAnnotationResolver.getAnnotation(index, CppParam::class.java)
            it.type!!.getCppTypeName(convertFromCppToJni = true, cppParam)
                .addConstReferenceToCppTypeNameIfRequired()
        }
        names = kmFunction.valueParameters.map {
            it.name
        }
        jniTypes = kmFunction.valueParameters.map {
            it.type!!.getTypeName().getJniTypeName()
        }
    }

    override fun getCppHeaderMethodDeclaration(): String {
        return CodeGenerationUtils.getCppHeaderMethodDeclaration(
            methodName = kmFunction.name,
            returnType = cppReturnType,
            types = cppTypes,
            names = names
        )
    }

    override fun getSourceDeclaration(template: String): String {
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
            } else if(TypesMapping.isCppTypeRegistered(cppReturnType)) {
                it.replace(
                    "ConvertToCppType<ReturnType>(env, res)",
                    "$cppReturnType(env, res)"
                )
            } else {
                it
            }
        }.replace("JniReturnType", jniReturnType)
            .replace("ReturnType", cppReturnType)
            .replace("args", CodeGenerationUtils.generateCppMethodArgs(cppTypes, names))
            .replace("CallObjectMethod", jniReturnType.getJniMethodCallMethodNameFromJniTypeName())
            .replace("methodName", kmFunction.name)
            .replace("convertedArgs", convertedArgs)
            .replace("converters", converters)
    }

    override fun getMethodIdGeneration(template: String): String {
        return template
            .replace("methodName", kmFunction.name)
            .replace("jvmSignature", getJvmSignature())
    }

    override fun getMethodIdDeclaration(template: String): String {
        return template.replace("methodName", kmFunction.name)
    }

    fun getJvmSignature(): String {
        val str = kmFunction.signature!!.toString()
        return str.substring(startIndex = str.indexOf('('))
    }
}