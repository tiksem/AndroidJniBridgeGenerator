//
// Created by Semyon Tikhonenko on 12/1/21.
//

#include "JArray.h"

#define DEFINE_METHODS(T, CppT) template<> \
void J##T##Array::releaseArrayElements() { \
    env->Release##T##ArrayElements(javaArray, data, 0); \
} \
 \
template<> \
CppT* J##T##Array::getElements() const { \
    return env->Get##T##ArrayElements(javaArray, nullptr); \
}

DEFINE_METHODS(Byte, int8_t)
DEFINE_METHODS(Short, int16_t)
DEFINE_METHODS(Int, int32_t)
DEFINE_METHODS(Long, int64_t)
DEFINE_METHODS(Float, float)
DEFINE_METHODS(Double, double)

template<>
void JBooleanArray::releaseArrayElements() {
    env->ReleaseBooleanArrayElements(javaArray, reinterpret_cast<jboolean *>(data), 0);
}

template<>
bool* JBooleanArray::getElements() const {
    return reinterpret_cast<bool *>(env->GetBooleanArrayElements(javaArray, nullptr));
}