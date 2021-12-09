#include <jni.h>
#include "Converters.h"
#include "CppTest.h"
#include "FunctionCallsBridge.h"
        
extern "C"
JNIEXPORT jlong JNICALL
Java_com_neborosoft_jnibridgegenerator_CppTestNative_newInstance(JNIEnv *env, jobject thiz) {
    return reinterpret_cast<jlong>(new CppTest());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neborosoft_jnibridgegenerator_CppTestNative_release(JNIEnv *env, jobject thiz, jlong ptr) {
    delete reinterpret_cast<CppTest*&>(ptr);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neborosoft_jnibridgegenerator_CppTestNative_e(JNIEnv *env, jobject thiz, jlong ptr, jintArray list) {
    JIntArray _list = ConvertToCppType<JIntArray>(env, list);   
    auto* self = reinterpret_cast<CppTest*&>(ptr);
    self->e(_list);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neborosoft_jnibridgegenerator_CppTestNative_ffg(JNIEnv *env, jobject thiz, jlong ptr, jobject l) {
    JObject l_obj(env, l);
    std::function<void(int32_t)> _l = [=] (int32_t param0) {
        jint _param0 = ConvertFromCppType<int32_t>(env, param0);   
        CallLambdaFunctionLambdaUnitInt(env, l_obj.getJavaObject(), _param0);   
    };
    auto* self = reinterpret_cast<CppTest*&>(ptr);
    self->ffg(_l);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_neborosoft_jnibridgegenerator_CppTestNative_g(JNIEnv *env, jobject thiz, jlong ptr) {

    auto* self = reinterpret_cast<CppTest*&>(ptr);
    auto _result = self->g();
    return ConvertFromCppType<jstring>(env, _result);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_neborosoft_jnibridgegenerator_CppTestNative_getA(JNIEnv *env, jobject thiz, jlong ptr) {

    auto* self = reinterpret_cast<CppTest*&>(ptr);
    auto _result = self->getA();
    return ConvertFromCppType<jint>(env, _result);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neborosoft_jnibridgegenerator_CppTestNative_yo(JNIEnv *env, jobject thiz, jlong ptr, jstring value) {
    JString _value = ConvertToCppType<JString>(env, value);   
    auto* self = reinterpret_cast<CppTest*&>(ptr);
    self->yo(_value);
}
            