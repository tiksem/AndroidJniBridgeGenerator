//
// Created by Semyon Tikhonenko on 12/3/21.
//

#ifndef JNIBRIDGEGENERATOR_JOBJECTTEMPLATE_H
#define JNIBRIDGEGENERATOR_JOBJECTTEMPLATE_H

#include <jni.h>
#include <string>
#include <vector>

class KotlinInterfaceTest {
    JNIEnv* env;
    jobject obj;
public:
    static void init(JNIEnv* env);

    KotlinInterfaceTest(JNIEnv *env, jobject obj);
    KotlinInterfaceTest(const KotlinInterfaceTest& obj);
    KotlinInterfaceTest(KotlinInterfaceTest&&) = default;
    KotlinInterfaceTest& operator=(const KotlinInterfaceTest&);
    KotlinInterfaceTest& operator=(KotlinInterfaceTest&&) = default;
    ~KotlinInterfaceTest();

    // Java method wrappers
    void d(int32_t e);
    std::string eeer(const std::vector<int64_t>& ee);
    // Java method wrappers
};

#endif //JNIBRIDGEGENERATOR_JOBJECTTEMPLATE_H
