package com.neborosoft.jnibridgegenerator.methods

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG

private const val METHOD_NAME = "newInstance"

class NewInstanceCppMethodGenerator : CppMethodGenerator {
    override fun getJniMethodCall(
        packageName: String,
        kotlinClassName: String,
        cppClassName: String
    ): String {
        val jniPackageName = packageName.replace('.', '_')

        return """
            |extern "C"
            |JNIEXPORT jlong JNICALL
            |Java_${jniPackageName}_${kotlinClassName}_$METHOD_NAME(JNIEnv *env, jobject thiz) {
            |    return reinterpret_cast<jlong>(new $cppClassName());
            |}
            """.trimMargin()
    }

    override fun getKotlinSpecs(): List<FunSpec> {
        return listOf(
            FunSpec.builder(METHOD_NAME)
                .addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                .returns(LONG)
                .build(),
        )
    }
}