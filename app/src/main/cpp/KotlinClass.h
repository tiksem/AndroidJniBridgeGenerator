//
// Created by Semyon Tikhonenko on 12/3/21.
//

#ifndef JNIBRIDGEGENERATOR_KotlinClass_H
#define JNIBRIDGEGENERATOR_KotlinClass_H

#include <jni.h>
#include <string>
#include <vector>
#include "JObject.h"

class KotlinClass : public JObject {
public:
    static void init(JNIEnv* env);

    KotlinClass(JNIEnv *env, jobject obj);

    // Java method wrappers
    std::vector<int64_t> bbbbbgggg();
    void d(int32_t e);
    std::string eeer(const std::vector<int64_t>& ee);
    // Java method wrappers
};

#endif //JNIBRIDGEGENERATOR_JOBJECTTEMPLATE_H
