package com.neborosoft.jnibridgegenerator

import com.neborosoft.jnibridgegenerator.Constants.KOTLIN_PACKAGE_NAME
import com.squareup.kotlinpoet.*
import java.io.File

private fun LambdaTypeName.generateLambdaInterface(packageName: String): FileSpec {
    var paramIndex = 0;
    val typeSpec = TypeSpec.interfaceBuilder(getLambdaInterfaceTypeName())
        .addModifiers(KModifier.FUN)
        .addFunction(
            FunSpec.builder("invoke")
                .addParameters(
                    parameterSpecs = this.parameters.map {
                        ParameterSpec(name = "param${paramIndex++}", type = it.type)
                    }
                )
                .addModifiers(KModifier.OPERATOR, KModifier.ABSTRACT)
                .returns(returnType)
                .build()
        ).build()
    return FileSpec.get(packageName, typeSpec)
}

private data class Lambda(
    val className: ClassName,
    val jniSignature: String,
    val jniReturnType: String,
    val jniArgs: List<String>
)

class LambdaGenerator(
    private val kaptKotlinGeneratedDir: String
) {
    private val generatedLambdas = HashMap<String, Lambda>()

    fun generateIfNotGenerated(lambdaTypeName: LambdaTypeName): TypeName {
        val interfaceName = lambdaTypeName.getLambdaInterfaceTypeName()
        generatedLambdas[interfaceName]?.let {
            return it.className
        }

        val file = lambdaTypeName.generateLambdaInterface(
            KOTLIN_PACKAGE_NAME
        )
        file.writeTo(File(kaptKotlinGeneratedDir))
        val className = ClassName(KOTLIN_PACKAGE_NAME, interfaceName)
        generatedLambdas[interfaceName] = Lambda(
            className = className,
            jniSignature = lambdaTypeName.getJniSignature(),
            jniReturnType = lambdaTypeName.returnType.getJniTypeName(),
            jniArgs = lambdaTypeName.parameters.map {
                it.type.getJniTypeName()
            }
        )
        return className
    }

    fun generateFunctionsCallBridgeHeader(template: String): String {
        val lambdas = generatedLambdas.values
        val initFunctionsCallBridgeTemplate = template.findStringBetweenQuotes(
            "// InitFunctionsCallBridge\n"
        )

        val initFunctionsCallBridgeGenerations = lambdas.joinToString("\n") {
            initFunctionsCallBridgeTemplate
                .replace(
                    "lambdaClassName",
                    it.className.canonicalName.replace(".", "/")
                )
                .replace(
                    "jvmSignature",
                    it.jniSignature
                )
                .replace("InterfaceName", it.className.simpleName)
                .replace(
                    "clazz",
                    "clazz" + it.className.simpleName
                )
        } + "\n"

        val callLambdaFunctionTemplate = template.findStringBetweenQuotes("// Lambda call\n")

        val callLambdaFunctionsGeneration = lambdas.joinToString("\n") { lambda ->
            var callArgs = lambda.jniArgs.indices.joinToString(", ") {
                "arg$it"
            }
            if (callArgs.isNotEmpty()) {
                callArgs = ", $callArgs"
            }

            var args = lambda.jniArgs.mapIndexed { index, it ->
                "$it arg$index"
            }.joinToString(", ")
            if (args.isNotEmpty()) {
                args = ", $args"
            }

            callLambdaFunctionTemplate.replace("ReturnType", lambda.jniReturnType)
                .replace("CallObjectMethod", lambda.jniReturnType.getJniMethodCallMethodNameFromJniTypeName())
                .replace("CallLambdaFunction",
                    "CallLambdaFunction${lambda.className.simpleName}"
                ).replace("InterfaceName", lambda.className.simpleName)
                .replace("callArgs", callArgs)
                .replace("args", args)
        } + "\n"

        return template
            .generateMethodIdes()
            .replace(callLambdaFunctionTemplate, callLambdaFunctionsGeneration)
            .replace(initFunctionsCallBridgeTemplate, initFunctionsCallBridgeGenerations)
    }

    private fun String.generateMethodIdes(): String {
        val methodIdTemplate = findStringBetweenQuotes("// Lambda method ides\n")

        val lambdas = generatedLambdas.values
        val methodIdes = lambdas.joinToString("\n") {
            methodIdTemplate.replace("InterfaceName", it.className.simpleName)
        } + "\n";

        return replace(methodIdTemplate, methodIdes)
    }

    fun generateFunctionsCallBridgeCpp(template: String): String {
        return template.generateMethodIdes()
    }
}