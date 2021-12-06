//
// Created by Semyon Tikhonenko on 12/3/21.
//

#include "JObjectTemplate.h"
#include "Converters.h"

// Method ides declaration
static jmethodID methodNameId = nullptr;
// Method ides declaration

JObjectTemplate::JObjectTemplate(JNIEnv *env, jobject obj) : env(env), obj(obj) {
}

void JObjectTemplate::init(JNIEnv* env) {
    jclass clazz = env->FindClass("classname");
    // Method ides generation
    methodNameId = env->GetMethodID(clazz, "methodName", "jvmSignature");
    // Method ides generation
}

JObjectTemplate::~JObjectTemplate() {
    env->DeleteLocalRef(obj);
}

JObjectTemplate::JObjectTemplate(const JObjectTemplate &obj) {
    this->env = obj.env;
    this->obj = env->NewLocalRef(obj.obj);
}

JObjectTemplate &JObjectTemplate::operator=(const JObjectTemplate & o) {
    env->DeleteGlobalRef(obj);
    obj = env->NewLocalRef(o.obj);
    return *this;
}

// Java method wrappers
ReturnType JObjectTemplate::methodName(args) {
    converters
    auto res = static_cast<JniReturnType>(env->CallObjectMethod(obj, methodNameIdconvertedArgs));
    return ConvertToCppType<ReturnType>(env, res);
}
// Java method wrappers

