//
// Created by Semyon Tikhonenko on 12/1/21.
//

#ifndef JNIBRIDGEGENERATOR_JARRAY_H
#define JNIBRIDGEGENERATOR_JARRAY_H

#include <jni.h>

template <typename CppT, typename JavaT>
class JArray {
    JavaT javaArray;
    CppT *data = nullptr;
    JNIEnv *env;

    void releaseArrayElements();
    CppT* getElements();
public:
    JArray(JavaT javaArray, JNIEnv *env) : javaArray(javaArray),
                                                 env(env) {}

    JArray(const JArray<CppT, JavaT>&) = delete;
    JArray(JArray<CppT, JavaT>&&) = default;

    JArray& operator=(const JArray<CppT, JavaT>&) = delete;
    JArray& operator=(JArray<CppT, JavaT>&&) = default;

    CppT *getData() {
        assert(!data);
        data = getElements();
        return data;
    }

    int32_t length() const {
        return env->GetArrayLength(javaArray);
    }

    ~JArray() {
        if (data) {
            releaseArrayElements();
            data = nullptr;
        }
    }
};

typedef JArray<double, jdoubleArray> JDoubleArray;
typedef JArray<float, jfloatArray> JFloatArray;
typedef JArray<int8_t, jbyteArray> JByteArray;
typedef JArray<int16_t, jshortArray> JShortArray;
typedef JArray<int32_t, jintArray> JIntArray;
typedef JArray<int64_t, jlongArray> JLongArray;
typedef JArray<bool, jbooleanArray> JBooleanArray;

#endif //JNIBRIDGEGENERATOR_JARRAY_H
