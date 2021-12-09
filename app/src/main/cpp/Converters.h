//
// Created by Semyon Tikhonenko on 12/1/21.
//

#ifndef JNIBRIDGEGENERATOR_CONVERTERS_H
#define JNIBRIDGEGENERATOR_CONVERTERS_H

#include <jni.h>
#include "JArray.h"
#include "JString.h"
#include <string>
#include <vector>
#include <functional>

template<typename CppType, typename JavaType>
CppType ConvertToCppType(JNIEnv *env, JavaType javaType) {
    return javaType;
}

template<>
inline JString ConvertToCppType<JString>(JNIEnv *env, jstring javaType) {
    return JString(javaType, env);
}

template<>
inline std::string ConvertToCppType<std::string>(JNIEnv *env, jstring javaType) {
    return JString(javaType, env).getData();
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

template<>
inline jbooleanArray ConvertFromCppType<jbooleanArray>(JNIEnv *env, const std::vector<bool>& v) {
    int length = v.size();
    jbooleanArray array = env->NewBooleanArray(length);
    std::vector<jboolean> temp(v.begin(), v.end());
    env->SetBooleanArrayRegion(array, 0, length, temp.data());
    return array;
}

template<>
inline jshortArray ConvertFromCppType<jshortArray>(JNIEnv *env, const std::vector<int16_t>& v) {
    int length = v.size();
    jshortArray array = env->NewShortArray(length);
    env->SetShortArrayRegion(array, 0, length, v.data());
    return array;
}

template<>
inline jintArray ConvertFromCppType<jintArray>(JNIEnv *env, const std::vector<int32_t>& v) {
    int length = v.size();
    jintArray array = env->NewIntArray(length);
    env->SetIntArrayRegion(array, 0, length, v.data());
    return array;
}

template<>
inline jlongArray ConvertFromCppType<jlongArray>(JNIEnv *env, const std::vector<int64_t>& v) {
    int length = v.size();
    jlongArray array = env->NewLongArray(length);
    env->SetLongArrayRegion(array, 0, length, v.data());
    return array;
}

template<>
inline jfloatArray ConvertFromCppType<jfloatArray>(JNIEnv *env, const std::vector<float>& v) {
    int length = v.size();
    jfloatArray array = env->NewFloatArray(length);
    env->SetFloatArrayRegion(array, 0, length, v.data());
    return array;
}

template<>
inline jdoubleArray ConvertFromCppType<jdoubleArray>(JNIEnv *env, const std::vector<double>& v) {
    int length = v.size();
    jdoubleArray array = env->NewDoubleArray(length);
    env->SetDoubleArrayRegion(array, 0, length, v.data());
    return array;
}

#endif //JNIBRIDGEGENERATOR_CONVERTERS_H
