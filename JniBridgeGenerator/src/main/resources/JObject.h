//
// Created by Semyon Tikhonenko on 12/7/21.
//

#ifndef JNIBRIDGEGENERATOR_JOBJECT_H
#define JNIBRIDGEGENERATOR_JOBJECT_H

#include <jni.h>

class JObject {
protected:
    JNIEnv* env;
    jobject obj;
public:
    JObject();
    JObject(JNIEnv *env, jobject obj);
    JObject(const JObject& obj);
    JObject& operator=(const JObject&);
    ~JObject();

    jobject getJavaObject() const;

    JNIEnv *getEnv() const;
};


#endif //JNIBRIDGEGENERATOR_JOBJECT_H
