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

JString::JString(JString&& o) {
    this->data = o.data;
    this->env = o.env;
    this->string = o.string;
    o.string = nullptr;
    o.data = nullptr;
}

JString& JString::operator=(JString&& o) {
    if (&o == this) {
        return *this;
    }

    this->data = o.data;
    this->env = o.env;
    this->string = o.string;
    o.string = nullptr;
    o.data = nullptr;

    return *this;
}
