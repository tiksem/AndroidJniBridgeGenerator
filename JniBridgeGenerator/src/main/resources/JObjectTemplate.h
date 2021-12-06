//
// Created by Semyon Tikhonenko on 12/3/21.
//

#ifndef JNIBRIDGEGENERATOR_JOBJECTTEMPLATE_H
#define JNIBRIDGEGENERATOR_JOBJECTTEMPLATE_H

#include <jni.h>
#include <string>
#include <vector>

class JObjectTemplate {
    JNIEnv* env;
    jobject obj;
public:
    static void init(JNIEnv* env);

    JObjectTemplate(JNIEnv *env, jobject obj);
    JObjectTemplate(const JObjectTemplate& obj);
    JObjectTemplate(JObjectTemplate&&) = default;
    JObjectTemplate& operator=(const JObjectTemplate&);
    JObjectTemplate& operator=(JObjectTemplate&&) = default;
    ~JObjectTemplate();

    // Java method wrappers
    // Java method wrappers
};

#endif //JNIBRIDGEGENERATOR_JOBJECTTEMPLATE_H
