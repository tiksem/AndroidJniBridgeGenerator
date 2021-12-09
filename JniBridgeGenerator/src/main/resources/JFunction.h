//
// Created by Semyon Tikhonenko on 12/7/21.
//

#ifndef JNIBRIDGEGENERATOR_JFUNCTION_H
#define JNIBRIDGEGENERATOR_JFUNCTION_H

#include <string>

namespace {
    constexpr static int MAX_NUMBER_OF_ARGUMENTS = 22;
    jmethodID ides[MAX_NUMBER_OF_ARGUMENTS] = { nullptr };
}

template<typename R, typename... Args>
class JFunction {
    JNIEnv* env;
    jobject obj;
public:
    static void init(JNIEnv* env) {
        std::string jniSignatureArgs = "";

        for (int i = 0; i <= MAX_NUMBER_OF_ARGUMENTS; ++i) {
            std::string className = "kotlin/jvm/functions/Function";
            className += std::to_string(i);
            jclass c = env->FindClass(className.data());
            std::string jniSignature = "(" + jniSignatureArgs + ")java/lang/Object;";
            jniSignatureArgs += "java/lang/Object;";
            jmethodID id = env->GetMethodID(c, "invoke", jniSignature.data());
            ides[i] = id;
        }
    }

    JFunction(JNIEnv *env, jobject obj) {

    }

    JFunction(const JFunction& obj) {
        this->env = obj.env;
        this->obj = env->NewLocalRef(obj.obj);
    }

    JFunction(JFunction&&) = default;

    JFunction& operator=(const JFunction& o) {
        if (&o == this) {
            return *this;
        }

        env->DeleteGlobalRef(obj);
        obj = env->NewLocalRef(o.obj);
        return *this;
    }

    JFunction& operator=(JFunction&&) = default;

    ~JFunction() {
        env->DeleteLocalRef(obj);
    }
};


#endif //JNIBRIDGEGENERATOR_JFUNCTION_H
