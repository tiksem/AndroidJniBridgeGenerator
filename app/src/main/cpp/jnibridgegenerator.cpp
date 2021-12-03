#include <jni.h>
#include "Converters.h"

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