//
// Created by Semyon Tikhonenko on 12/23/21.
//

#ifndef JNIBRIDGEGENERATOR_KOTLINCONSTRUCTORS_H
#define JNIBRIDGEGENERATOR_KOTLINCONSTRUCTORS_H

#include <jni.h>
#include <string>
#include <vector>

void InitKotlinConstructors(JNIEnv* env);

// Constructors
jobject CreateKotlinStruct(JNIEnv* env, int32_t a, const std::string& b);
// Constructors

#endif //JNIBRIDGEGENERATOR_KOTLINCONSTRUCTORS_H
