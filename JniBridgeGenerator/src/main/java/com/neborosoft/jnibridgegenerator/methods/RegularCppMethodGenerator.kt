package com.neborosoft.jnibridgegenerator.methods

import com.neborosoft.annotations.CppMethod
import com.neborosoft.annotations.CppParam
import com.neborosoft.jnibridgegenerator.*
import com.neborosoft.jnibridgegenerator.Constants.PTR
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

enum class GenerationPolicy {
    WITH_CPP_PTR,
    EXTERNAL_METHOD,
    EXTERNAL_FUNCTION
}

@KotlinPoetMetadataPreview
class RegularCppMethodGenerator(
    private val kmFunction: ImmutableKmFunction,
    private val lambdaGenerator: LambdaGenerator,
    private val generationPolicy: GenerationPolicy,
    private val annotationResolver: ClassAnnotationResolver
): CppMethodGenerator {
    init {
        if (generationPolicy != GenerationPolicy.WITH_CPP_PTR) {
            kmFunction.valueParameters.forEach {
                if (it.type?.getTypeName() is LambdaTypeName) {
                    throw UnsupportedOperationException("Lambdas are not supported for external functions")
                }
            }
        }
    }

    private val requestedIncludeHeaders = ArrayList<String>()
    private val parameterAnnotationResolver = annotationResolver.getParameterAnnotationResolver(kmFunction)

    private val cppReturnType by lazy {
        kmFunction.returnType.getCppTypeName(
            convertFromCppToJni = true,
            cppParam = annotationResolver.getAnnotation(kmFunction, CppParam::class.java)
        )
    }

    private val cppTypeNames by lazy {
        kmFunction.valueParameters.mapIndexed { index, it ->
            val cppParam = parameterAnnotationResolver.getAnnotation(index, CppParam::class.java)
            it.type!!.getCppTypeName(convertFromCppToJni = false, cppParam)
                .addConstReferenceToCppTypeNameIfRequired()
        }
    }

    private val jniTypes by lazy {
        val res = kmFunction.valueParameters.map {
            it.type?.getTypeName().getJniTypeName()
        }
        if (generationPolicy == GenerationPolicy.WITH_CPP_PTR) {
            res.withPrependedItem("jlong")
        } else {
            res
        }
    }

    private val jniArgNames by lazy {
        val res = kmFunction.valueParameters.map {
            it.name
        }
        if (generationPolicy == GenerationPolicy.WITH_CPP_PTR) {
            res.withPrependedItem(PTR)
        } else {
            res
        }
    }

    private val jniCallReturnType by lazy {
        kmFunction.returnType.getTypeName().getJniTypeName()
    }

    override fun getKotlinSpecs(): List<FunSpec> {
        require(generationPolicy == GenerationPolicy.WITH_CPP_PTR) {
            throw UnsupportedOperationException(
                "getKotlinSpecs is not supported for $generationPolicy"
            )
        }

        val callArgsList = arrayListOf(PTR)
        val baseParameterSpecs = kmFunction.valueParameters.map { p ->
            callArgsList.add(p.name)
            ParameterSpec(
                name = p.name,
                type = p.type!!.getTypeName().let {
                    if (it is LambdaTypeName) {
                        lambdaGenerator.generateIfNotGenerated(it)
                    } else {
                        it
                    }
                }
            )
        }

        val nativeParameters = baseParameterSpecs.withPrependedItem(
            ParameterSpec(
                name = PTR,
                type = LONG
            )
        )

        val callArgs = callArgsList.joinToString(", ")
        val nativeFun = FunSpec.builder(kmFunction.name)
            .addParameters(
                parameterSpecs = nativeParameters
            ).addModifiers(KModifier.EXTERNAL, KModifier.PRIVATE)
            .returns(kmFunction.returnType.getTypeName())
            .build()
        return listOf(
            FunSpec.builder(kmFunction.name)
                .addParameters(parameterSpecs = baseParameterSpecs)
                .addStatement("return ${kmFunction.name}($callArgs)")
                .returns(kmFunction.returnType.getTypeName())
                .build(),
            nativeFun
        )
    }

    private data class Converters(
        val code: String,
        val args: String
    )

    private fun addRequestedCppHeaders(cppTypes: List<String>) {
        requestedIncludeHeaders.addAll(cppTypes.map {
            it.removeConstReferenceFromCppType()
        }.filter {
            TypesMapping.isCppTypeRegistered(cppTypeName = it)
        })
    }

    private fun generateToCppParamConverters(
        cppTypeNames: List<String>,
        unconvertedParamNames: List<String>,
        lambdas: List<LambdaTypeName?>
    ): Converters {
        addRequestedCppHeaders(cppTypeNames)

        val cppCallArgsList = mutableListOf<String>()
        val code = cppTypeNames.mapIndexed { index, cppTypeName ->
            val noRefCppTypeName = cppTypeName.removeConstReferenceFromCppType()
            val paramName = unconvertedParamNames[index]
            val convertedParamName = "_$paramName"
            cppCallArgsList.add(convertedParamName)

            val lambda = lambdas[index]
            if (lambda == null) {
                val convertCall = if (TypesMapping.isCppTypeRegistered(noRefCppTypeName)) {
                    "(env, $paramName)"
                } else {
                    " = ConvertToCppType<$noRefCppTypeName>(env, $paramName)"
                }

                """
                |    $noRefCppTypeName $convertedParamName$convertCall;   
                """.trimMargin()
            } else {
                val cppTypeNames = lambda.parameters.map {
                    it.type.getCppTypeName(convertFromCppToJni = true)
                }

                addRequestedCppHeaders(cppTypeNames)

                val lambdaObjParamName = "${paramName}_obj"

                val cppReturnType = lambda.returnType.getCppTypeName(convertFromCppToJni = true)

                val cppLambdaArgs = cppTypeNames.mapIndexed { i, it ->
                    "$it param$i"
                }.joinToString(", ")

                val converters = cppTypeNames.mapIndexed { i, it ->
                    val jniTypeName = lambda.parameters[0].type.getJniTypeName()
                    """
                    |    $jniTypeName _param$i = ConvertFromCppType<$it>(env, param$i);   
                    """.trimMargin()
                }.joinToString("\n")

                var callLambdaArgs = cppTypeNames.indices.joinToString(", ") {
                    "_param$it"
                }
                if (callLambdaArgs.isNotEmpty()) {
                    callLambdaArgs = ", $callLambdaArgs"
                }

                val callFunctionName = "CallLambdaFunction${lambda.getLambdaInterfaceTypeName()}"
                val callFunctionObjParam = "$lambdaObjParamName.getJavaObject()"

                val convertCall = if (TypesMapping.isCppTypeRegistered(cppReturnType)) {
                    "ConvertToCppType<$cppReturnType>(env, callResult)"
                } else {
                    "$cppReturnType(env, callResult)"
                }

                val body = if (cppReturnType != "void") {
                    """
                    auto callResult = $callFunctionName(env, $callFunctionObjParam$callLambdaArgs);
                    return $convertCall;
                    """.trimMargin()
                } else {
                    "$callFunctionName(env, $callFunctionObjParam$callLambdaArgs);"
                }

                """
                |    JObject $lambdaObjParamName(env, $paramName);
                |    $noRefCppTypeName $convertedParamName = [=] ($cppLambdaArgs) {
                |    $converters
                |        $body   
                |    };
                """.trimMargin()
            }
        }.joinToString("\n")

        return Converters(code, args = cppCallArgsList.joinToString(", "))
    }

    override fun getJniMethodCall(
        packageName: String,
        kotlinClassName: String,
        cppClassName: String
    ): String {
        val jniPackageName = packageName.replace('.', '_')

        val jniCallArgsList = mutableListOf("JNIEnv *env", "jobject thiz")
        (jniTypes zip jniArgNames).mapTo(jniCallArgsList) {
            it.first + " " + it.second
        }
        val jniCallArgs = jniCallArgsList.joinToString(", ")
        val converters = generateToCppParamConverters(
            cppTypeNames = cppTypeNames,
            unconvertedParamNames = if (generationPolicy == GenerationPolicy.WITH_CPP_PTR) {
                jniArgNames.shifted(1)
            } else {
                jniArgNames
            },
            lambdas = kmFunction.valueParameters.map {
                it.type?.getTypeName() as? LambdaTypeName
            }
        )

        val cast = when (generationPolicy) {
            GenerationPolicy.WITH_CPP_PTR -> {
                "auto* self = reinterpret_cast<$cppClassName*&>($PTR);"
            }
            GenerationPolicy.EXTERNAL_METHOD -> {
                "$cppClassName _self(env, thiz); auto* self = &_self;"
            }
            GenerationPolicy.EXTERNAL_FUNCTION -> {
                ""
            }
        }

        val self = if (generationPolicy == GenerationPolicy.EXTERNAL_FUNCTION) {
            "$cppClassName::"
        } else {
            "self->"
        }

        val ending = if (cppReturnType.isEmpty() || cppReturnType == "void") {
            "|    $self${kmFunction.name}(${converters.args});"
        } else {
            """
            |    auto _result = $self${kmFunction.name}(${converters.args});
            |    return ConvertFromCppType<$jniCallReturnType>(env, _result);
            """.trimMargin()
        }

        return """
        |extern "C"
        |JNIEXPORT $jniCallReturnType JNICALL
        |Java_${jniPackageName}_${kotlinClassName}_${kmFunction.name}($jniCallArgs) {
        |${converters.code}
        |    $cast
        |$ending
        |}
        """.trimMargin()
    }

    override fun getCppHeaderMethodDeclaration(): String? {
        val shouldGenerateHeaderDeclaration = annotationResolver.getAnnotation(
            kmFunction, CppMethod::class.java
        ) ?.skipHeaderGeneration?.not() ?: true

        if (!shouldGenerateHeaderDeclaration) {
            return null
        }

        return CodeGenerationUtils.getCppHeaderMethodDeclaration(
            methodName = kmFunction.name,
            returnType = cppReturnType,
            types = cppTypeNames,
            names = if (generationPolicy == GenerationPolicy.WITH_CPP_PTR) {
                jniArgNames.drop(1)
            } else {
                jniArgNames
            }
        )
    }

    override fun getRequestedCppHeaders(): List<String> {
        return requestedIncludeHeaders
    }
}