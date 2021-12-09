//
// Created by Semyon Tikhonenko on 7/27/17.
//

#include <cassert>
#include "JString.h"

JString::JString(jstring string, JNIEnv *env) : string(string),
                                                env(env) {}
JString::~JString() {
    if (data) {
        env->ReleaseStringUTFChars(string, data);
        data = nullptr;
    }
}

const char *JString::getData() const {
    assert(!data);
    data = env->GetStringUTFChars(string, nullptr);
    return data;
}

jint JString::length() const {
    return env->GetStringLength(string);
}
