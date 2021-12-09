//
// Created by Semyon Tikhonenko on 12/7/21.
//

#include "JObject.h"
#include "Converters.h"

JObject::JObject(JNIEnv *env, jobject obj) : env(env) {
    this->obj = env->NewGlobalRef(obj);
}

JObject::~JObject() {
    if (obj) {
        env->DeleteGlobalRef(obj);
        obj = nullptr;
    }
}

JObject::JObject(const JObject &obj) {
    this->env = obj.env;
    this->obj = env->NewGlobalRef(obj.obj);
}

JObject &JObject::operator=(const JObject & o) {
    if (&o == this) {
        return *this;
    }

    env->DeleteGlobalRef(obj);
    obj = env->NewGlobalRef(o.obj);
    return *this;
}

JObject::JObject(JObject&& o) noexcept {
    this->obj = o.obj;
    this->env = o.env;
    o.obj = nullptr;
}

JObject& JObject::operator=(JObject&& o)  noexcept {
    if (&o == this) {
        return *this;
    }

    this->obj = o.obj;
    this->env = o.env;
    o.obj = nullptr;

    return *this;
}

jobject JObject::getJavaObject() const {
    return obj;
}
