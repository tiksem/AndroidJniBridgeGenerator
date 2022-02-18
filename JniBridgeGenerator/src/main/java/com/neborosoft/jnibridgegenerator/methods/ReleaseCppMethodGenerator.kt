package com.neborosoft.jnibridgegenerator.methods

import com.neborosoft.jnibridgegenerator.Constants
import com.neborosoft.jnibridgegenerator.withAppendedItem
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterSpec

private const val METHOD_NAME = "release"

class ReleaseCppMethodGenerator(
    private val includeDeleter: Boolean
) : CppMethodGenerator {
    override fun getJniMethodCall(
        packageName: String,
        kotlinClassName: String,
        cppClassName: String,
    ): String {
        val jniPackageName = packageName.replace('.', '_')

        if (includeDeleter) {
            return """
            |extern "C"
            |JNIEXPORT void JNICALL
            |Java_${jniPackageName}_${kotlinClassName}_$METHOD_NAME(JNIEnv *env, jobject thiz, jlong ptr, jlong deleter) {
            |    if (deleter == 0) { 
            |        delete reinterpret_cast<$cppClassName*&>(ptr);
            |    } else {
            |        auto* d = reinterpret_cast<std::function<void($cppClassName*)>*&>(deleter);
            |        (*d)(reinterpret_cast<$cppClassName*&>(ptr));
            |        delete d;
            |    }
            |}
            """.trimMargin()
        } else {
            return """
            |extern "C"
            |JNIEXPORT void JNICALL
            |Java_${jniPackageName}_${kotlinClassName}_$METHOD_NAME(JNIEnv *env, jobject thiz, jlong ptr) {
            |    delete reinterpret_cast<$cppClassName*&>(ptr);
            |}
            """.trimMargin()
        }
    }

    override fun getKotlinSpecs(): List<FunSpec> {
        val nativeReleaseParameters = mutableListOf(
            ParameterSpec(
                name = Constants.PTR,
                type = LONG
            )
        )
        if (includeDeleter) {
            nativeReleaseParameters.add(
                ParameterSpec(
                    name = "deleter",
                    type = LONG
                )
            )
        }

        val nativeReleaseCall = if (includeDeleter) {
            "$METHOD_NAME(${Constants.PTR}, deleter)"
        } else {
            "$METHOD_NAME(${Constants.PTR})"
        }

        return listOf(
            FunSpec.builder(METHOD_NAME)
                .addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                .addParameters(
                    parameterSpecs = nativeReleaseParameters
                ).build(),
            FunSpec.builder("finalize")
                .addModifiers(KModifier.PROTECTED)
                .addStatement("$METHOD_NAME()")
                .build(),
            FunSpec.builder(METHOD_NAME)
                .addModifiers(KModifier.OVERRIDE)
                .beginControlFlow("if (${Constants.PTR} != 0L)")
                .addStatement(nativeReleaseCall)
                .addStatement("${Constants.PTR} = 0")
                .endControlFlow()
                .build()
        )
    }
}