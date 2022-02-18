package com.neborosoft.jnibridgegenerator.methods

import com.neborosoft.annotations.CppMethod
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
    private val cppTypes: List<List<String>>
    private val cppNames = ArrayList<List<String>>()
    private val jniTypes: List<String>
    private val parameterAnnotationResolver =
        annotationResolver.getParameterAnnotationResolver(kmFunction)
    private val requestedIncludeHeaders = ArrayList<String>()

    init {
        val cppReturnParam = annotationResolver.getAnnotation(kmFunction, CppMethod::class.java)
        cppReturnType = kmFunction.returnType.getCppTypeName(
            convertFromCppToJni = true,
            cppMethod = cppReturnParam
        )
        jniReturnType = kmFunction.returnType.getTypeName().getJniTypeName()
        cppTypes = kmFunction.valueParameters.mapIndexed { index, it ->
            val cppParam = parameterAnnotationResolver.getAnnotation(index, CppParam::class.java)
            val cppTypeName = it.type!!.getCppTypeName(convertFromCppToJni = true, cppParam)
            if (TypesMapping.isCppTypeWithJavaConstructor(cppTypeName)) {
                requestedIncludeHeaders.add(cppTypeName)
                cppNames.add(listOf(it.name, "${it.name}Deleter"))
                listOf("$cppTypeName*", "const std::function<void($cppTypeName*)>&")
            } else {
                cppNames.add(listOf(it.name))
                listOf(cppTypeName.addConstReferenceToCppTypeNameIfRequired())
            }
        }
        jniTypes = kmFunction.valueParameters.map {
            it.type!!.getTypeName().getJniTypeName()
        }
    }

    override fun getCppHeaderMethodDeclaration(): String {
        return CodeGenerationUtils.getCppHeaderMethodDeclaration(
            methodName = kmFunction.name,
            returnType = cppReturnType,
            types = cppTypes.flatten(),
            names = cppNames.flatten()
        )
    }

    override fun getSourceDeclaration(template: String): String {
        val jniCallArgsList = ArrayList<String>()

        val converters = jniTypes.mapIndexed { index, type ->
            val cppNames = cppNames[index]
            val name = cppNames.first()
            val convertedName = "_$name"
            jniCallArgsList.add(convertedName)
            if (cppNames.size == 1) {
                """
                |$type $convertedName = ConvertFromCppType<$type>(env, $name);   
                """.trimMargin()
            } else {
                val cppType = cppTypes[index][0]
                """
                |$type $convertedName = Create${cppType.removePointerFromCppType()}(env, $name, ${cppNames[1]});   
                """.trimMargin()
            }
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
                ).replace("(JniReturnType)", "")
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
            .replace("args", CodeGenerationUtils.generateCppMethodArgs(
                cppTypes.flatten(), cppNames.flatten()
            ))
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

    override fun getRequestedCppHeaders(): List<String> {
        return requestedIncludeHeaders
    }
}