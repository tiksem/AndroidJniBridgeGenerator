//
// Created by Semyon Tikhonenko on 12/3/21.
//

#ifndef JNIBRIDGEGENERATOR_KotlinClass_H
#define JNIBRIDGEGENERATOR_KotlinClass_H

#include <jni.h>
#include <string>
#include <vector>
#include "JObject.h"
#include "Converters.h"
// headers
#include "CppTestConstructor.h"
// headers

class KotlinClass : public JObject, public Eblo {
public:
    static void init(JNIEnv* env);

    KotlinClass() = default;
    KotlinClass(JNIEnv *env, jobject obj);

    // Java method wrappers
    std::vector<int64_t> bbbbbgggg();
    void d(int32_t e, CppTestConstructor* cppTest, const std::function<void(CppTestConstructor*)>& cppTestDeleter);
    std::string eeer(const std::vector<int64_t>& ee);
    Eblo u(const Eblo& r);
    // Java method wrappers
};

#endif //JNIBRIDGEGENERATOR_JOBJECTTEMPLATE_H
