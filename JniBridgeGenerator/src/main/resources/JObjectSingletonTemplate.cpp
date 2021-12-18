//
// Created by Semyon Tikhonenko on 12/17/21.
//

#include "JObjectSingletonTemplate.h"
#include "Converters.h"
#include <cassert>

// Method ides declaration
static jmethodID methodNameId = nullptr;
// Method ides declaration

static JObjectSingletonTemplate* _instance = nullptr;

extern "C"
JNIEXPORT void JNICALL
JniInitCall(JNIEnv *env, jobject thiz) {
    jclass clazz = env->GetObjectClass(thiz);
    // Method ides generation
    methodNameId = env->GetMethodID(clazz, "methodName", "jvmSignature");
    // Method ides generation
    _instance = new JObjectSingletonTemplate(env, thiz);
}

JObjectSingletonTemplate::JObjectSingletonTemplate(JNIEnv *env, jobject obj) : JObject(env, obj) {
}

JObjectSingletonTemplate& JObjectSingletonTemplate::instance() {
    assert(_instance && "JObjectSingletonTemplate has not been initialized, call nativeInit from kotlin side");
    return *_instance;
}

// Java method wrappers
ReturnType JObjectSingletonTemplate::methodName(args) {
    converters
    auto res = static_cast<JniReturnType>(env->CallObjectMethod(obj, methodNameIdconvertedArgs));
    return ConvertToCppType<ReturnType>(env, res);
}
// Java method wrappers
