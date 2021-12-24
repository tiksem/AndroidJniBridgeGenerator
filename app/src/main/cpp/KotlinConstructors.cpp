//
// Created by Semyon Tikhonenko on 12/23/21.
//

#include "KotlinConstructors.h"
#include "Converters.h"

// id
static jmethodID constructorKotlinStruct = nullptr;
// id

// class
static jclass classKotlinStruct = nullptr;
// class

void InitKotlinConstructors(JNIEnv* env) {
    // Init constructor
    classKotlinStruct = env->FindClass("com/neborosoft/jnibridgegenerator/KotlinStruct");
    constructorKotlinStruct = env->GetMethodID(classKotlinStruct, "<init>", "(ILjava/lang/String;)V");
    // Init constructor
}

// Constructors
jobject CreateKotlinStruct(JNIEnv* env, int32_t a, const std::string& b) {
    auto _a = ConvertFromCppType<jint>(env, a);
    auto _b = ConvertFromCppType<jstring>(env, b);
    return env->NewObject(classKotlinStruct, constructorKotlinStruct, _a, _b);
}
// Constructors
