//
// Created by Semyon Tikhonenko on 12/3/21.
//

#ifndef JNIBRIDGEGENERATOR_JNIBRIDGEINIT_H
#define JNIBRIDGEGENERATOR_JNIBRIDGEINIT_H

#include <jni.h>
#include "KotlinInterfaceTest.h"
#include "FunctionCallsBridge.h"

void JNIBridgeInit(JNIEnv* env) {
    InitFunctionsCallBridge(env);
// Register JObjects
    KotlinInterfaceTest::init(env);
// Register JObjects
}

#endif //JNIBRIDGEGENERATOR_JNIBRIDGEINIT_H
