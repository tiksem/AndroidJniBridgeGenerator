
#include <jni.h>
#include "Converters.h"
#include "CppFunctionsTestFunctions.h"
#include "FunctionCallsBridge.h"
        
extern "C"
JNIEXPORT jlongArray JNICALL
Java_com_neborosoft_jnibridgegenerator_CppFunctionsTest_yo(JNIEnv *env, jobject thiz, jstring l) {
    JString _l = ConvertToCppType<JString>(env, l);   
    
    auto _result = CppFunctionsTestFunctions::yo(_l);
    return ConvertFromCppType<jlongArray>(env, _result);
}
            