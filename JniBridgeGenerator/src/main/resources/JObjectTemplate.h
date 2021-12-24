//
// Created by Semyon Tikhonenko on 12/3/21.
//

#ifndef JNIBRIDGEGENERATOR_JObjectTemplate_H
#define JNIBRIDGEGENERATOR_JObjectTemplate_H

#include <jni.h>
#include <string>
#include <vector>
#include "JObject.h"
#include "Converters.h"

class JObjectTemplate : public JObject {
public:
    static void init(JNIEnv* env);

    JObjectTemplate() = default;
    JObjectTemplate(JNIEnv *env, jobject obj);

    // Java method wrappers
    // Java method wrappers
};

#endif //JNIBRIDGEGENERATOR_JOBJECTTEMPLATE_H
