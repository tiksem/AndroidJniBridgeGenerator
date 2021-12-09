//
// Created by Semyon Tikhonenko on 12/7/21.
//

#ifndef JNIBRIDGEGENERATOR_JOBJECT_H
#define JNIBRIDGEGENERATOR_JOBJECT_H

#include <jni.h>
#include "Converters.h"

class JObject {
protected:
    JNIEnv* env;
    jobject obj;
public:
    JObject(JNIEnv *env, jobject obj);
    JObject(const JObject& obj);
    JObject(JObject&&) noexcept;
    JObject& operator=(const JObject&);
    JObject& operator=(JObject&&) noexcept;
    ~JObject();

    jobject getJavaObject() const;
};


#endif //JNIBRIDGEGENERATOR_JOBJECT_H
