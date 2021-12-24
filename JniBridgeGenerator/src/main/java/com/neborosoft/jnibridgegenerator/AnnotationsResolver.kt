package com.neborosoft.jnibridgegenerator

import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.lang.model.element.ExecutableElement

@KotlinPoetMetadataPreview
private data class FunctionKey(
    val className: String,
    val function: ImmutableKmFunction
)

@KotlinPoetMetadataPreview
interface ClassAnnotationResolver {
    fun <T : Annotation> getAnnotation(
        func: ImmutableKmFunction,
        annotation: Class<T>
    ): T?

    fun getParameterAnnotationResolver(func: ImmutableKmFunction): ParameterAnnotationResolver
}

@KotlinPoetMetadataPreview
interface ParameterAnnotationResolver {
    fun <T : Annotation> getAnnotation(
        parameterIndex: Int,
        annotation: Class<T>
    ): T?
}

@KotlinPoetMetadataPreview
object AnnotationsResolver {
    private val map = HashMap<FunctionKey, ExecutableElement>()

    fun <T : Annotation> getAnnotation(
        kmClass: ImmutableKmClass,
        func: ImmutableKmFunction,
        annotation: Class<T>
    ): T? {
        return map[FunctionKey(className = kmClass.name, function = func)]?.getAnnotation(annotation)
    }

    fun registerClass(kmClass: ImmutableKmClass, functions: List<ExecutableElement>) {
        kmClass.functions.forEach { f ->
            map[FunctionKey(
                className = kmClass.name,
                function = f
            )] = functions.find {
                it.simpleName.toString() == f.name &&
                it.parameters.contentEqualsUsingPredicate(f.valueParameters) { a, b ->
                    val bTypeName = b.type!!.getTypeName()
                    val bType = bTypeName.toString()
                    val aType = a.asType().toString()

                    fun compareLambdas(): Boolean {
                        val lambda = bTypeName as? LambdaTypeName ?: return false
                        return aType.startsWith(
                            "kotlin.jvm.functions.Function${lambda.parameters.size}"
                        )
                    }

                    when {
                        a.simpleName.toString() != b.name -> {
                            false
                        }
                        aType == bType -> {
                            true
                        }
                        bTypeName is ParameterizedTypeName -> {
                            if (bTypeName.rawType.simpleName == "Array") {
                                val canonicalName = bTypeName.typeArguments[0].getCanonicalName()
                                if (canonicalName == "kotlin.String") {
                                    aType == "java.lang.String[]"
                                } else {
                                    aType == "$canonicalName[]"
                                }
                            } else {
                                throw UnsupportedOperationException(
                                    "${f.name} is not supported. Only Array is supported " +
                                            "as parameterized param for now."
                                )
                            }
                        }
                        aType == TypesMapping.getJavaTypeFromKotlinType(bType) -> {
                            true
                        }
                        else -> {
                            compareLambdas()
                        }
                    }
                }
            } ?: throw IllegalStateException("Function $f not found")
        }
    }

    fun getClassAnnotationResolver(kmClass: ImmutableKmClass): ClassAnnotationResolver {
        return object : ClassAnnotationResolver {
            override fun <T : Annotation> getAnnotation(
                func: ImmutableKmFunction,
                annotation: Class<T>
            ): T? {
                return getAnnotation(kmClass, func, annotation)
            }

            override fun getParameterAnnotationResolver(
                func: ImmutableKmFunction
            ): ParameterAnnotationResolver {
                val element = map[FunctionKey(
                    className = kmClass.name,
                    function = func
                )] ?: throw IllegalStateException(
                    "No such function ${func.name} in ${kmClass.name}"
                )

                return object : ParameterAnnotationResolver {
                    override fun <T : Annotation> getAnnotation(
                        parameterIndex: Int,
                        annotation: Class<T>
                    ): T? {
                        return element.parameters[parameterIndex].getAnnotation(annotation)
                    }
                }
            }
        }
    }
}