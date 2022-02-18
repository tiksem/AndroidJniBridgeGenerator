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

jobjectArray CreateStringArray(JNIEnv* env, jint length);

template<typename Factory>
jobjectArray CreateStringArray(JNIEnv* env, jint length, const Factory& factory) {
    jobjectArray array = CreateStringArray(env, length);
    for (int i = 0; i < length; ++i) {
        env->SetObjectArrayElement(array, i, factory(i));
    }

    return array;
}

// Constructors
jobject CreateKotlinObject(JNIEnv* env___Args);
jobjectArray CreateKotlinObjectArray(JNIEnv* env, jint length);

template<typename Container, typename Factory>
jobjectArray CreateKotlinObjectArray(JNIEnv* env, const Container& container, const Factory& factory) {
    int size = container.size();
    auto result = CreateKotlinObjectArray(env, size);
    for (int i = 0; i < size; ++i) {
        jobject jo = factory(container[i]);
        env->SetObjectArrayElement(result, i, jo);
    }

    return result;
}

// Constructors

#endif //JNIBRIDGEGENERATOR_KOTLINCONSTRUCTORS_H
