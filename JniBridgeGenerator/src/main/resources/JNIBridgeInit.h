//
// Created by Semyon Tikhonenko on 12/3/21.
//

#ifndef JNIBRIDGEGENERATOR_JNIBRIDGEINIT_H
#define JNIBRIDGEGENERATOR_JNIBRIDGEINIT_H

#include <jni.h>
#include "FunctionCallsBridge.h"

void JNIBridgeInit(JNIEnv* env) {
    InitFunctionsCallBridge(env);
// Register JObjects
// Register JObjects
}

#endif //JNIBRIDGEGENERATOR_JNIBRIDGEINIT_H
