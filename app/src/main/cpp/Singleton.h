//
// Created by Semyon Tikhonenko on 12/17/21.
//

#ifndef JNIBRIDGEGENERATOR_JOBJECTSINGLETONTEMPLATE_H
#define JNIBRIDGEGENERATOR_JOBJECTSINGLETONTEMPLATE_H


#include <jni.h>
#include <string>
#include <vector>
#include "JObject.h"

class Singleton : public JObject {
public:
    Singleton(JNIEnv *env, jobject obj);

    static Singleton& instance();

    // Java method wrappers
    void nativeInit();
    void yo();
    // Java method wrappers
};


#endif //JNIBRIDGEGENERATOR_JOBJECTSINGLETONTEMPLATE_H
