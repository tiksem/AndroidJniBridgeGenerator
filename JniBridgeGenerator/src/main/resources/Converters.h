//
// Created by Semyon Tikhonenko on 12/1/21.
//

#ifndef JNIBRIDGEGENERATOR_CONVERTERS_H
#define JNIBRIDGEGENERATOR_CONVERTERS_H

#include <jni.h>
#include "JArray.h"
#include "JString.h"
#include <string>

template<typename CppType, typename JavaType>
CppType ConvertToCppType(JNIEnv *env, JavaType javaType) {
    return javaType;
}

template<>
inline JString ConvertToCppType(JNIEnv *env, jstring javaType) {
    return JString(javaType, env);
}

template<>
inline JByteArray ConvertToCppType(JNIEnv *env, jbyteArray javaType) {
    return JByteArray(javaType, env);
}

template<>
inline JShortArray ConvertToCppType(JNIEnv *env, jshortArray javaType) {
    return JShortArray(javaType, env);
}

template<>
inline JIntArray ConvertToCppType(JNIEnv *env, jintArray javaType) {
    return JIntArray(javaType, env);
}

template<>
inline JLongArray ConvertToCppType(JNIEnv *env, jlongArray javaType) {
    return JLongArray(javaType, env);
}

template<>
inline JDoubleArray ConvertToCppType(JNIEnv *env, jdoubleArray javaType) {
    return JDoubleArray(javaType, env);
}

template<>
inline JFloatArray ConvertToCppType(JNIEnv *env, jfloatArray javaType) {
    return JFloatArray(javaType, env);
}

template<typename JavaType, typename CppType>
JavaType ConvertFromCppType(JNIEnv *env, const CppType& cppType) {
    return cppType;
}

template<>
inline jstring ConvertFromCppType<jstring>(JNIEnv *env, const std::string& str) {
    return env->NewStringUTF(str.data());
}

#endif //JNIBRIDGEGENERATOR_CONVERTERS_H
