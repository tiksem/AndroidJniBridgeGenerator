//
// Created by Semyon Tikhonenko on 12/8/21.
//

#ifndef JNIBRIDGEGENERATOR_FUNCTIONCALLSBRIDGE_H
#define JNIBRIDGEGENERATOR_FUNCTIONCALLSBRIDGE_H

#include <jni.h>
#include "JObject.h"

namespace FunctionCallsBridgeMethods {
    // Lambda method ides
    extern jmethodID methodIdLambdaUnitInt;
    
// Lambda method ides
}

// Lambda call
inline void CallLambdaFunctionLambdaUnitInt(JNIEnv* env, jobject obj, jint arg0) {
    return env->CallVoidMethod(obj, FunctionCallsBridgeMethods::methodIdLambdaUnitInt, arg0);
}

// Lambda call

inline void InitFunctionsCallBridge(JNIEnv* env) {
// InitFunctionsCallBridge
    jclass clazzLambdaUnitInt = env->FindClass("com/neborosoft/jnibridgegenerator/LambdaUnitInt");
    FunctionCallsBridgeMethods::methodIdLambdaUnitInt = env->GetMethodID(clazzLambdaUnitInt, "invoke", "(I)V");

// InitFunctionsCallBridge
}

#endif //JNIBRIDGEGENERATOR_FUNCTIONCALLSBRIDGE_H
