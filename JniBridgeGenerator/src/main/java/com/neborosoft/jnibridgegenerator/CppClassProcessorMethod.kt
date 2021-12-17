package com.neborosoft.jnibridgegenerator

import com.neborosoft.annotations.CppMethod
import com.neborosoft.jnibridgegenerator.Constants.PTR
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.hasAnnotations

enum class SpecialMethod {
    RELEASE,
    NEW_INSTANCE
}

@KotlinPoetMetadataPreview
class CppClassProcessorMethod {
    private val kotlinSpecs: List<FunSpec>
    private val jniTypes: List<String>
    private val jniArgNames: List<String>
    private val cppTypeNames: List<String>
    private val lambdas: List<LambdaTypeName?>
    private val specialMethod: SpecialMethod?
    private val jniCallReturnType: String
    private val cppReturnType: String
    private val requestedIncludeHeaders = ArrayList<String>()
    private val methodName: String
    private val shouldGenerateHeaderDeclaration: Boolean
    private lateinit var lambdaGenerator: LambdaGenerator

    constructor(specialMethod: SpecialMethod) {
        this.specialMethod = specialMethod
        when (specialMethod) {
            SpecialMethod.RELEASE -> {
                methodName = "release"
                kotlinSpecs = listOf(
                    FunSpec.builder(methodName)
                        .addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                        .addParameters(
                            parameterSpecs = listOf(ParameterSpec(
                                name = PTR,
                                type = LONG
                            ))
                        ).build(),
                    FunSpec.builder("finalize")
                        .addModifiers(KModifier.PROTECTED)
                        .addStatement("release()")
                        .build(),
                    FunSpec.builder(methodName)
                        .addStatement("release($PTR)")
                        .addStatement("$PTR = 0")
                        .build()
                )

                jniTypes = listOf("jlong")
                jniCallReturnType = "void"
                jniArgNames = listOf(PTR)
            }
            SpecialMethod.NEW_INSTANCE -> {
                methodName = "newInstance"
                kotlinSpecs = listOf(
                    FunSpec.builder(methodName)
                        .addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                        .returns(LONG)
                        .build(),
                )

                jniTypes = listOf()
                jniCallReturnType = "jlong"
                jniArgNames = listOf()
            }
        }
        cppTypeNames = listOf()
        lambdas = listOf()
        cppReturnType = ""
        shouldGenerateHeaderDeclaration = false
    }

    constructor(kmFunction: ImmutableKmFunction, lambdaGenerator: LambdaGenerator) {
        this.specialMethod = null
        this.lambdaGenerator = lambdaGenerator

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

        lambdas = kmFunction.valueParameters.map {
            it.type?.getTypeName() as? LambdaTypeName
        }

        val callArgs = callArgsList.joinToString(", ")

        val nativeParameters = baseParameterSpecs.withPrependedItem(
            ParameterSpec(
                name = PTR,
                type = LONG
            )
        )

        jniArgNames = nativeParameters.map {
            it.name
        }

        jniTypes = nativeParameters.map {
            it.type.getJniTypeName()
        }

        val nativeFun = FunSpec.builder(kmFunction.name)
            .addParameters(
                parameterSpecs = nativeParameters
            ).addModifiers(KModifier.EXTERNAL, KModifier.PRIVATE)
            .returns(kmFunction.returnType.getTypeName())
            .build()

        kotlinSpecs = listOf(
            FunSpec.builder(kmFunction.name)
                .addParameters(parameterSpecs = baseParameterSpecs)
                .addStatement("return ${kmFunction.name}($callArgs)")
                .returns(kmFunction.returnType.getTypeName())
                .build(),
            nativeFun
        )

        cppTypeNames = kmFunction.valueParameters.map {
            it.type!!.getCppTypeName(convertFromCppToJni = false)
                .addConstReferenceToCppTypeNameIfRequired()
        }

        jniCallReturnType = kmFunction.returnType.getTypeName().getJniTypeName()
        cppReturnType = kmFunction.returnType.getCppTypeName(convertFromCppToJni = true)
        methodName = kmFunction.name

        shouldGenerateHeaderDeclaration = kmFunction.returnType.annotations
            .filterIsInstance<CppMethod>().elementAtOrNull(0)?.skipHeaderGeneration?.not() ?: true
    }

    fun getKotlinSpecs(): List<FunSpec> {
        return kotlinSpecs
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

    fun getJniMethodCall(
        packageName: String,
        kotlinClassName: String,
        cppClassName: String
    ): String {
        val jniPackageName = packageName.replace('.', '_')

        if (specialMethod == SpecialMethod.NEW_INSTANCE) {
            return """
            |extern "C"
            |JNIEXPORT $jniCallReturnType JNICALL
            |Java_${jniPackageName}_${kotlinClassName}_${methodName}(JNIEnv *env, jobject thiz) {
            |    return reinterpret_cast<$jniCallReturnType>(new $cppClassName());
            |}
            """.trimMargin()
        } else if (specialMethod == SpecialMethod.RELEASE) {
            return """
            |extern "C"
            |JNIEXPORT $jniCallReturnType JNICALL
            |Java_${jniPackageName}_${kotlinClassName}_${methodName}(JNIEnv *env, jobject thiz, jlong ptr) {
            |    delete reinterpret_cast<$cppClassName*&>(ptr);
            |}
            """.trimMargin()
        }

        val jniCallArgsList = mutableListOf("JNIEnv *env", "jobject thiz")
        (jniTypes zip jniArgNames).mapTo(jniCallArgsList) {
            it.first + " " + it.second
        }
        val jniCallArgs = jniCallArgsList.joinToString(", ")
        val converters = if (specialMethod == null) {
            generateToCppParamConverters(
                cppTypeNames = cppTypeNames,
                unconvertedParamNames = jniArgNames.shifted(1),
                lambdas = lambdas
            )
        } else {
            Converters(code = "", args = "")
        }

        val ending = if (cppReturnType.isEmpty() || cppReturnType == "void") {
            "|    self->$methodName(${converters.args});"
        } else {
            """
            |    auto _result = self->$methodName(${converters.args});
            |    return ConvertFromCppType<$jniCallReturnType>(env, _result);
            """.trimMargin()
        }

        return """
        |extern "C"
        |JNIEXPORT $jniCallReturnType JNICALL
        |Java_${jniPackageName}_${kotlinClassName}_${methodName}($jniCallArgs) {
        |${converters.code}
        |    auto* self = reinterpret_cast<$cppClassName*&>($PTR);
        |$ending
        |}
        """.trimMargin()
    }

    fun getCppHeaderMethodDeclaration(): String? {
        if (!shouldGenerateHeaderDeclaration) {
            return null
        }

        return CodeGenerationUtils.getCppHeaderMethodDeclaration(
            methodName = methodName,
            returnType = cppReturnType,
            types = cppTypeNames,
            names = jniArgNames.drop(1)
        )
    }

    fun getRequestedCppHeaders(): List<String> {
        return requestedIncludeHeaders
    }
}