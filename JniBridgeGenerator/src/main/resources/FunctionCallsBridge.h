//
// Created by Semyon Tikhonenko on 12/8/21.
//

#ifndef JNIBRIDGEGENERATOR_FUNCTIONCALLSBRIDGE_H
#define JNIBRIDGEGENERATOR_FUNCTIONCALLSBRIDGE_H

#include <jni.h>
#include "JObject.h"

namespace FunctionCallsBridgeMethods {
    // Lambda method ides
    extern jmethodID methodIdInterfaceName;
    // Lambda method ides
}

// Lambda call
inline ReturnType CallLambdaFunction(JNIEnv* env, jobject objargs) {
    return env->CallObjectMethod(obj, FunctionCallsBridgeMethods::methodIdInterfaceNamecallArgs);
}
// Lambda call

inline void InitFunctionsCallBridge(JNIEnv* env) {
// InitFunctionsCallBridge
    jclass clazz = env->FindClass("lambdaClassName");
    FunctionCallsBridgeMethods::methodIdInterfaceName = env->GetMethodID(clazz, "invoke", "jvmSignature");
// InitFunctionsCallBridge
}

#endif //JNIBRIDGEGENERATOR_FUNCTIONCALLSBRIDGE_H
