//
// Created by Semyon Tikhonenko on 12/21/21.
//

#ifndef JNIBRIDGEGENERATOR_CUSTOMCONVERTERS_H
#define JNIBRIDGEGENERATOR_CUSTOMCONVERTERS_H

#include <jni.h>

class Eblo {

};

template<>
inline Eblo ConvertToCppType(JNIEnv *env, jshortArray javaType) {
    return Eblo();
}

template<>
inline jshortArray ConvertFromCppType(JNIEnv *env, const Eblo& e) {
    return env->NewShortArray(35);
}

class Uee {

};

template<>
inline jint ConvertFromCppType(JNIEnv *env, const Uee& uee) {
    return 5;
}

#endif //JNIBRIDGEGENERATOR_CUSTOMCONVERTERS_H
