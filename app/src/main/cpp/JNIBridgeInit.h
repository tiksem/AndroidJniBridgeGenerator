//
// Created by Semyon Tikhonenko on 12/3/21.
//

#ifndef JNIBRIDGEGENERATOR_JNIBRIDGEINIT_H
#define JNIBRIDGEGENERATOR_JNIBRIDGEINIT_H

#include <jni.h>
#include "Singleton.h"
#include "KotlinClass.h"
#include "FunctionCallsBridge.h"
#include "KotlinConstructors.h"

void JNIBridgeInit(JNIEnv* env) {
    InitFunctionsCallBridge(env);
    InitKotlinConstructors(env);
// Register JObjects
    KotlinClass::init(env);
// Register JObjects
}

#endif //JNIBRIDGEGENERATOR_JNIBRIDGEINIT_H
