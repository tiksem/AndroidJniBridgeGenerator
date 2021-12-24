//
// Created by Semyon Tikhonenko on 12/7/21.
//

#include "JObject.h"
#include "Converters.h"

JObject::JObject(): env(nullptr), obj(nullptr) {
}

JObject::JObject(JNIEnv *env, jobject obj) : env(env), obj(obj) {
}

JObject::~JObject() {
    if (obj) {
        env->DeleteGlobalRef(obj);
        obj = nullptr;
    }
}

JObject::JObject(const JObject &obj) {
    this->env = obj.env;
    if (obj.obj != nullptr) {
        this->obj = env->NewGlobalRef(obj.obj);
    } else {
        this->obj = nullptr;
    }
}

JObject &JObject::operator=(const JObject & o) {
    if (&o == this) {
        return *this;
    }

    env->DeleteGlobalRef(obj);
    obj = env->NewGlobalRef(o.obj);
    return *this;
}

jobject JObject::getJavaObject() const {
    return obj;
}

JNIEnv *JObject::getEnv() const {
    return env;
}
