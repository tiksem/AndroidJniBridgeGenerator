//
// Created by Semyon Tikhonenko on 12/3/21.
//

#ifndef JNIBRIDGEGENERATOR_JOBJECTTEMPLATE_H
#define JNIBRIDGEGENERATOR_JOBJECTTEMPLATE_H

#include <jni.h>
#include <string>
#include <vector>
#include "JObject.h"

class KotlinInterfaceTest : public JObject {
public:
    static void init(JNIEnv* env);

    KotlinInterfaceTest(JNIEnv *env, jobject obj);

    // Java method wrappers
    void d(int32_t e);
    std::string eeer(const std::vector<int64_t>& ee);
    // Java method wrappers
};

#endif //JNIBRIDGEGENERATOR_JOBJECTTEMPLATE_H
