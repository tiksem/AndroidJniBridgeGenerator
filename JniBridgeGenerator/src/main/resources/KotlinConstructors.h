//
// Created by Semyon Tikhonenko on 12/23/21.
//

#ifndef JNIBRIDGEGENERATOR_KOTLINCONSTRUCTORS_H
#define JNIBRIDGEGENERATOR_KOTLINCONSTRUCTORS_H

#include <jni.h>
#include <string>
#include <vector>
// headers
// headers

void InitKotlinConstructors(JNIEnv* env);

// Constructors
jobject CreateKotlinObject(JNIEnv* env___Args);
jobjectArray CreateKotlinObjectArray(JNIEnv* env, jint length);
// Constructors

#endif //JNIBRIDGEGENERATOR_KOTLINCONSTRUCTORS_H
