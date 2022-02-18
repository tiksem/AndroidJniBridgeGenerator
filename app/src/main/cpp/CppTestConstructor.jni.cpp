
#include <jni.h>
#include "Converters.h"
#include "CppTestConstructor.h"
#include "FunctionCallsBridge.h"
        
extern "C"
JNIEXPORT void JNICALL
Java_com_neborosoft_jnibridgegenerator_CppTestConstructorNative_release(JNIEnv *env, jobject thiz, jlong ptr, jlong deleter) {
    if (deleter == 0) { 
        delete reinterpret_cast<CppTestConstructor*&>(ptr);
    } else {
        auto* d = reinterpret_cast<std::function<void(CppTestConstructor*)>*&>(deleter);
        (*d)(reinterpret_cast<CppTestConstructor*&>(ptr));
        delete d;
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_neborosoft_jnibridgegenerator_CppTestConstructorNative_a(JNIEnv *env, jobject thiz, jlong ptr) {

    auto* self = reinterpret_cast<CppTestConstructor*&>(ptr);
    auto _result = self->a();
    return ConvertFromCppType<jobject>(env, _result);
}
            