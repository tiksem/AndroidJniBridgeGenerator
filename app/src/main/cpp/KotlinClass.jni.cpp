
#include <jni.h>
#include "Converters.h"
#include "KotlinClass.h"
#include "FunctionCallsBridge.h"
        
extern "C"
JNIEXPORT jlongArray JNICALL
Java_com_neborosoft_jnibridgegenerator_KotlinInterfaceTest_bbbbbgggg(JNIEnv *env, jobject thiz) {

    KotlinClass _self(env, thiz); auto* self = &_self;
    auto _result = self->bbbbbgggg();
    return ConvertFromCppType<jlongArray>(env, _result);
}
            