//
// Created by Semyon Tikhonenko on 12/17/21.
//

#ifndef JNIBRIDGEGENERATOR_JOBJECTSINGLETONTEMPLATE_H
#define JNIBRIDGEGENERATOR_JOBJECTSINGLETONTEMPLATE_H


#include <jni.h>
#include <string>
#include <vector>
#include "JObject.h"

class JObjectSingletonTemplate : public JObject {
public:
    JObjectSingletonTemplate(JNIEnv *env, jobject obj);

    static JObjectSingletonTemplate& instance();

    // Java method wrappers
    // Java method wrappers
};


#endif //JNIBRIDGEGENERATOR_JOBJECTSINGLETONTEMPLATE_H
