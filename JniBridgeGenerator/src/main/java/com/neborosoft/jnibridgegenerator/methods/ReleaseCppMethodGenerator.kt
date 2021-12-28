package com.neborosoft.jnibridgegenerator.methods

import com.neborosoft.jnibridgegenerator.Constants
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterSpec

private const val METHOD_NAME = "release"

class ReleaseCppMethodGenerator : CppMethodGenerator {
    override fun getJniMethodCall(
        packageName: String,
        kotlinClassName: String,
        cppClassName: String
    ): String {
        val jniPackageName = packageName.replace('.', '_')

        return """
            |extern "C"
            |JNIEXPORT void JNICALL
            |Java_${jniPackageName}_${kotlinClassName}_$METHOD_NAME(JNIEnv *env, jobject thiz, jlong ptr) {
            |    delete reinterpret_cast<$cppClassName*&>(ptr);
            |}
            """.trimMargin()
    }

    override fun getKotlinSpecs(): List<FunSpec> {
        return listOf(
            FunSpec.builder(METHOD_NAME)
                .addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                .addParameters(
                    parameterSpecs = listOf(
                        ParameterSpec(
                        name = Constants.PTR,
                        type = LONG
                    )
                    )
                ).build(),
            FunSpec.builder("finalize")
                .addModifiers(KModifier.PROTECTED)
                .addStatement("release()")
                .build(),
            FunSpec.builder(METHOD_NAME)
                .beginControlFlow("if (${Constants.PTR} != 0L)")
                .addStatement("release(${Constants.PTR})")
                .addStatement("${Constants.PTR} = 0")
                .endControlFlow()
                .build()
        )
    }
}