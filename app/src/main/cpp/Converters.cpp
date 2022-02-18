//
// Created by Semyon Tikhonenko on 1/15/22.
//

#include "Converters.h"

template<>
JString ConvertToCppType<JString>(JNIEnv *env, jstring javaType) {
    return JString(javaType, env);
}

template<>
std::string ConvertToCppType<std::string>(JNIEnv *env, jstring javaType) {
    return JString(javaType, env).getData();
}

template<>
JByteArray ConvertToCppType(JNIEnv *env, jbyteArray javaType) {
    return JByteArray(javaType, env);
}

template<>
JShortArray ConvertToCppType(JNIEnv *env, jshortArray javaType) {
    return JShortArray(javaType, env);
}

template<>
JIntArray ConvertToCppType(JNIEnv *env, jintArray javaType) {
    return JIntArray(javaType, env);
}

template<>
JLongArray ConvertToCppType(JNIEnv *env, jlongArray javaType) {
    return JLongArray(javaType, env);
}

template<>
JDoubleArray ConvertToCppType(JNIEnv *env, jdoubleArray javaType) {
    return JDoubleArray(javaType, env);
}

template<>
JFloatArray ConvertToCppType(JNIEnv *env, jfloatArray javaType) {
    return JFloatArray(javaType, env);
}

template<>
JObject ConvertToCppType(JNIEnv *env, jobject javaType) {
    return JObject(env, javaType);
}

template<>
jobject ConvertFromCppType(JNIEnv *env, const JObject& cppType) {
    return cppType.getJavaObject();
}

template<>
jstring ConvertFromCppType<jstring>(JNIEnv *env, const std::string& str) {
    return env->NewStringUTF(str.data());
}

template<>
jbooleanArray ConvertFromCppType<jbooleanArray>(JNIEnv *env, const std::vector<bool>& v) {
    int length = v.size();
    jbooleanArray array = env->NewBooleanArray(length);
    std::vector<jboolean> temp(v.begin(), v.end());
    env->SetBooleanArrayRegion(array, 0, length, temp.data());
    return array;
}

template<>
jshortArray ConvertFromCppType<jshortArray>(JNIEnv *env, const std::vector<int16_t>& v) {
    int length = v.size();
    jshortArray array = env->NewShortArray(length);
    env->SetShortArrayRegion(array, 0, length, v.data());
    return array;
}

template<>
jintArray ConvertFromCppType<jintArray>(JNIEnv *env, const std::vector<int32_t>& v) {
    int length = v.size();
    jintArray array = env->NewIntArray(length);
    env->SetIntArrayRegion(array, 0, length, v.data());
    return array;
}

template<>
jlongArray ConvertFromCppType<jlongArray>(JNIEnv *env, const std::vector<int64_t>& v) {
    int length = v.size();
    jlongArray array = env->NewLongArray(length);
    env->SetLongArrayRegion(array, 0, length, v.data());
    return array;
}

template<>
jfloatArray ConvertFromCppType<jfloatArray>(JNIEnv *env, const std::vector<float>& v) {
    int length = v.size();
    jfloatArray array = env->NewFloatArray(length);
    env->SetFloatArrayRegion(array, 0, length, v.data());
    return array;
}

template<>
jdoubleArray ConvertFromCppType<jdoubleArray>(JNIEnv *env, const std::vector<double>& v) {
    int length = v.size();
    jdoubleArray array = env->NewDoubleArray(length);
    env->SetDoubleArrayRegion(array, 0, length, v.data());
    return array;
}

template<>
std::vector<std::string> ConvertToCppArray(JNIEnv *env, jobjectArray array) {
    std::vector<std::string> res;
    jsize length = env->GetArrayLength(array);
    res.reserve(length);
    for (int i = 0; i < length; ++i) {
        JString str((jstring)env->GetObjectArrayElement(array, i), env);
        res.push_back(str.getData());
    }

    return res;
}