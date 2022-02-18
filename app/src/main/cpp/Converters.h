//
// Created by Semyon Tikhonenko on 12/1/21.
//

#ifndef JNIBRIDGEGENERATOR_CONVERTERS_H
#define JNIBRIDGEGENERATOR_CONVERTERS_H

#include <jni.h>
#include "JArray.h"
#include "JString.h"
#include "JObject.h"
#include "Converters.h"
#include <string>
#include <vector>
#include <functional>

template<typename CppType, typename JavaType>
CppType ConvertToCppType(JNIEnv *env, JavaType javaType) {
    return javaType;
}

template<>
JString ConvertToCppType<JString>(JNIEnv *env, jstring javaType);

template<>
std::string ConvertToCppType<std::string>(JNIEnv *env, jstring javaType);

template<>
JByteArray ConvertToCppType(JNIEnv *env, jbyteArray javaType);

template<>
JShortArray ConvertToCppType(JNIEnv *env, jshortArray javaType);

template<>
JIntArray ConvertToCppType(JNIEnv *env, jintArray javaType);

template<>
JLongArray ConvertToCppType(JNIEnv *env, jlongArray javaType);

template<>
JDoubleArray ConvertToCppType(JNIEnv *env, jdoubleArray javaType);

template<>
JFloatArray ConvertToCppType(JNIEnv *env, jfloatArray javaType);

template<>
JObject ConvertToCppType(JNIEnv *env, jobject javaType);

template<typename JavaType, typename CppType>
JavaType ConvertFromCppType(JNIEnv *env, const CppType& cppType) {
    return (JavaType)cppType;
}

template<>
jobject ConvertFromCppType(JNIEnv *env, const JObject& cppType);

template<>
jstring ConvertFromCppType<jstring>(JNIEnv *env, const std::string& str);

template<>
jbooleanArray ConvertFromCppType<jbooleanArray>(JNIEnv *env, const std::vector<bool>& v);

template<>
jshortArray ConvertFromCppType<jshortArray>(JNIEnv *env, const std::vector<int16_t>& v);

template<>
jintArray ConvertFromCppType<jintArray>(JNIEnv *env, const std::vector<int32_t>& v);

template<>
jlongArray ConvertFromCppType<jlongArray>(JNIEnv *env, const std::vector<int64_t>& v);

template<>
jfloatArray ConvertFromCppType<jfloatArray>(JNIEnv *env, const std::vector<float>& v);

template<>
jdoubleArray ConvertFromCppType<jdoubleArray>(JNIEnv *env, const std::vector<double>& v);

template<typename CppType>
std::vector<CppType> ConvertToCppArray(JNIEnv *env, jobjectArray array) {
    std::vector<CppType> res;
    jsize length = env->GetArrayLength(array);
    res.reserve(length);
    for (int i = 0; i < length; ++i) {
        CppType cppType(env, env->GetObjectArrayElement(array, i));
        res.push_back(cppType);
    }

    return res;
}

template<>
std::vector<std::string> ConvertToCppArray(JNIEnv *env, jobjectArray array);

#include "CustomConverters.h"

#endif //JNIBRIDGEGENERATOR_CONVERTERS_H
