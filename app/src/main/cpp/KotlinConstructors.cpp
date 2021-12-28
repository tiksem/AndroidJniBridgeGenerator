//
// Created by Semyon Tikhonenko on 12/23/21.
//

#include "KotlinConstructors.h"
#include "Converters.h"

// id
static jmethodID constructorCppTestConstructor = nullptr;

static jmethodID constructorKotlinStruct = nullptr;
// id

// class
static jclass classCppTestConstructor = nullptr;

static jclass classKotlinStruct = nullptr;
// class

void InitKotlinConstructors(JNIEnv* env) {
    // Init constructor
    classCppTestConstructor = env->FindClass("com/neborosoft/jnibridgegenerator/CppTestConstructorNative");
    constructorCppTestConstructor = env->GetMethodID(classCppTestConstructor, "<init>", "(J)V");
    
    classKotlinStruct = env->FindClass("com/neborosoft/jnibridgegenerator/KotlinStruct");
    constructorKotlinStruct = env->GetMethodID(classKotlinStruct, "<init>", "(ILjava/lang/String;)V");
    // Init constructor
}

// Constructors
jobject CreateCppTestConstructor(JNIEnv* env, CppTestConstructor* ptr) {
    auto _ptr = ConvertFromCppType<jlong>(env, ptr);
    return env->NewObject(classCppTestConstructor, constructorCppTestConstructor, _ptr);
}

jobjectArray CreateCppTestConstructorArray(JNIEnv* env, jint length) {
    return env->NewObjectArray(length, classCppTestConstructor, nullptr);
}

jobject CreateKotlinStruct(JNIEnv* env, int32_t a, const std::string& b) {
    auto _a = ConvertFromCppType<jint>(env, a);
    auto _b = ConvertFromCppType<jstring>(env, b);
    return env->NewObject(classKotlinStruct, constructorKotlinStruct, _a, _b);
}

jobjectArray CreateKotlinStructArray(JNIEnv* env, jint length) {
    return env->NewObjectArray(length, classKotlinStruct, nullptr);
}
// Constructors
