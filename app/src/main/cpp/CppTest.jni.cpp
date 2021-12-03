#include <jni.h>
#include "Converters.h"
#include "CppTest.h"
        
extern "C"
JNIEXPORT jlong JNICALL
Java_com_neborosoft_jnibridgegenerator_CppTestNative_newInstance() {
    return reinterpret_cast<jlong>(new CppTest());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_neborosoft_jnibridgegenerator_CppTestNative_release(jlong ptr) {
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
            