//
// Created by Semyon Tikhonenko on 12/8/21.
//

#ifndef JNIBRIDGEGENERATOR_FUNCTIONCALLSBRIDGE_H
#define JNIBRIDGEGENERATOR_FUNCTIONCALLSBRIDGE_H

#include <jni.h>
#include "JObject.h"

namespace FunctionCallsBridgeMethods {
    // Lambda method ides
    extern jmethodID methodIdLambda_Unit_Int;
    
// Lambda method ides
}

// Lambda call
inline void CallLambdaFunctionLambda_Unit_Int(JNIEnv* env, jobject obj, jint arg0) {
    return env->CallVoidMethod(obj, FunctionCallsBridgeMethods::methodIdLambda_Unit_Int, arg0);
}

// Lambda call

inline void InitFunctionsCallBridge(JNIEnv* env) {
// InitFunctionsCallBridge
    jclass clazzLambda_Unit_Int = env->FindClass("com/neborosoft/jnibridgegenerator/Lambda_Unit_Int");
    FunctionCallsBridgeMethods::methodIdLambda_Unit_Int = env->GetMethodID(clazzLambda_Unit_Int, "invoke", "(I)V");

// InitFunctionsCallBridge
}

#endif //JNIBRIDGEGENERATOR_FUNCTIONCALLSBRIDGE_H
