//
// Created by Semyon Tikhonenko on 12/3/21.
//

#include "JObjectTemplate.h"
#include "Converters.h"
#include "KotlinConstructors.h"

// Method ides declaration
static jmethodID methodNameId = nullptr;
// Method ides declaration

void JObjectTemplate::init(JNIEnv* env) {
    jclass clazz = env->FindClass("classname");
    // Method ides generation
    methodNameId = env->GetMethodID(clazz, "methodName", "jvmSignature");
    // Method ides generation
}

JObjectTemplate::JObjectTemplate(JNIEnv *env, jobject obj) : JObject(env, obj) {
}

// Java method wrappers
ReturnType JObjectTemplate::methodName(args) {
    converters
    auto res = (JniReturnType)env->CallObjectMethod(obj, methodNameIdconvertedArgs);
    return ConvertToCppType<ReturnType>(env, res);
}
// Java method wrappers

