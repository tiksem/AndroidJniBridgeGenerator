//
// Created by Semyon Tikhonenko on 12/23/21.
//

#include "KotlinConstructors.h"
#include "Converters.h"

static jmethodID constructorString = nullptr;
static jclass classString = nullptr;

// id
static jmethodID constructorCppTestConstructor = nullptr;

static jmethodID constructorKotlinStruct = nullptr;
// id

// class
static jclass classCppTestConstructor = nullptr;

static jclass classKotlinStruct = nullptr;
// class

static jclass stringClass = nullptr;

jobjectArray CreateStringArray(JNIEnv* env, jint length) {
    return env->NewObjectArray(length, stringClass, nullptr);
}

void InitKotlinConstructors(JNIEnv* env) {
    stringClass = env->FindClass("java/lang/String");
    // Init constructor
    classCppTestConstructor = env->FindClass("com/neborosoft/jnibridgegenerator/CppTestConstructorNative");
    constructorCppTestConstructor = env->GetMethodID(classCppTestConstructor, "<init>", "(JJ)V");
    
    classKotlinStruct = env->FindClass("com/neborosoft/jnibridgegenerator/KotlinStruct");
    constructorKotlinStruct = env->GetMethodID(classKotlinStruct, "<init>", "(ILjava/lang/String;)V");
    // Init constructor
}

// Constructors
jobject CreateCppTestConstructor(JNIEnv* env, CppTestConstructor* ptr, const std::function<void(CppTestConstructor*)>& deleter) {
    auto _ptr = ConvertFromCppType<jlong>(env, ptr);
    auto _deleter = ConvertFromCppType<jlong>(env, deleter ? new std::function<void(CppTestConstructor*)>(deleter) : nullptr);
    return env->NewObject(classCppTestConstructor, constructorCppTestConstructor, _ptr, _deleter);
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
