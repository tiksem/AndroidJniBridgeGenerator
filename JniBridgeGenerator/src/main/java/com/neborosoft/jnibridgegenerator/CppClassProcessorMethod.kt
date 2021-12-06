package com.neborosoft.jnibridgegenerator

import com.neborosoft.jnibridgegenerator.Constants.PTR
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

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
    private val specialMethod: SpecialMethod?
    private val jniCallReturnType: String
    private val cppReturnType: String
    private val methodName: String

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
                    FunSpec.builder(methodName)
                        .addStatement("release($PTR)")
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
        cppReturnType = ""
    }

    constructor(kmFunction: ImmutableKmFunction) {
        this.specialMethod = null

        val callArgsList = arrayListOf(PTR)

        val baseParameterSpecs = kmFunction.valueParameters.map { p ->
            callArgsList.add(p.name)
            ParameterSpec(
                name = p.name,
                type = p.type!!.getTypeName()
            )
        }

        val callArgs = callArgsList.joinToString(", ")

        val nativeParameters = baseParameterSpecs.withPrependedItem(
            ParameterSpec(
                name = Constants.PTR,
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
                .addConstReferenceToCppTypeNameIfNotPrimitive()
        }

        jniCallReturnType = kmFunction.returnType.getTypeName().getJniTypeName()
        cppReturnType = kmFunction.returnType.getCppTypeName(convertFromCppToJni = true)
        methodName = kmFunction.name
    }

    private fun postInit() {

    }

    fun getKotlinSpecs(): List<FunSpec> {
        return kotlinSpecs
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
        val cppCallArgsList = mutableListOf<String>()
        val converters = if (specialMethod == null) {
            cppTypeNames.mapIndexed { index, cppTypeName ->
                val noRefCppTypeName = cppTypeName.removeConstReferenceFromCppType()
                val paramName = jniArgNames[index + 1]
                val convertedParamName = "_$paramName"
                cppCallArgsList.add(convertedParamName)

                """
                |    $noRefCppTypeName $convertedParamName = ConvertToCppType<$noRefCppTypeName>(env, $paramName);   
                """.trimMargin()
            }.joinToString("\n")
        } else {
            ""
        }

        val cppCallArgs = cppCallArgsList.joinToString(", ")

        val ending = if (cppReturnType.isEmpty() || cppReturnType == "void") {
            "|    self->$methodName($cppCallArgs);"
        } else {
            """
            |    auto _result = self->$methodName($cppCallArgs);
            |    return ConvertFromCppType<$jniCallReturnType>(env, _result);
            """.trimMargin()
        }

        return """
        |extern "C"
        |JNIEXPORT $jniCallReturnType JNICALL
        |Java_${jniPackageName}_${kotlinClassName}_${methodName}($jniCallArgs) {
        |$converters
        |    auto* self = reinterpret_cast<$cppClassName*&>($PTR);
        |$ending
        |}
        """.trimMargin()
    }

    fun getCppHeaderMethodDeclaration(): String? {
        if (specialMethod != null) {
            return null
        }

        return CodeGenerationUtils.getCppHeaderMethodDeclaration(
            methodName = methodName,
            returnType = cppReturnType,
            types = cppTypeNames,
            names = jniArgNames.drop(1)
        )
    }
}