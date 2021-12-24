//
// Created by Semyon Tikhonenko on 12/17/21.
//

#include "Singleton.h"
#include "Converters.h"
#include <cassert>

// Method ides declaration
static jmethodID yoId = nullptr;
// Method ides declaration

static Singleton* _instance = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_neborosoft_jnibridgegenerator_Singleton_nativeInit(JNIEnv *env, jobject thiz) {
    jclass clazz = env->GetObjectClass(thiz);
    // Method ides generation
    yoId = env->GetMethodID(clazz, "yo", "()V");
    // Method ides generation
    _instance = new Singleton(env, thiz);
}

Singleton::Singleton(JNIEnv *env, jobject obj) : JObject(env, obj) {
}

Singleton& Singleton::instance() {
    assert(_instance && "Singleton has not been initialized, call nativeInit from kotlin side");
    return *_instance;
}

// Java method wrappers
void Singleton::yo() {
    
    static_cast<void>(env->CallVoidMethod(obj, yoId));
}
// Java method wrappers
