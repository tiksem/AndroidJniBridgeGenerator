#include <jni.h>
#include "Converters.h"
#include "JNIBridgeInit.h"

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("jnibridgegenerator");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("jnibridgegenerator")
//      }
//    }
extern "C"
JNIEXPORT void JNICALL
Java_com_neborosoft_jnibridgegenerator_MainActivity_eee(JNIEnv *env, jobject thiz, jlong ptr) {
    // TODO: implement eee()
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env),
                   JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    JNIBridgeInit(env);

    return JNI_VERSION_1_6;
}