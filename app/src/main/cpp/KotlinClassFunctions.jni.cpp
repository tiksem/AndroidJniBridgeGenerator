
#include <jni.h>
#include "Converters.h"
#include "KotlinClassFunctions.h"
#include "FunctionCallsBridge.h"
        
extern "C"
JNIEXPORT jintArray JNICALL
Java_com_neborosoft_jnibridgegenerator_KotlinInterfaceTest_ahaha(JNIEnv *env, jobject thiz) {

    
    auto _result = KotlinClassFunctions::ahaha();
    return ConvertFromCppType<jintArray>(env, _result);
}
            