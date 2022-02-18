//
// Created by Semyon Tikhonenko on 12/3/21.
//

#include "KotlinClass.h"
#include "Converters.h"
#include "KotlinConstructors.h"

// Method ides declaration
static jmethodID dId = nullptr;

static jmethodID eeerId = nullptr;

static jmethodID uId = nullptr;
// Method ides declaration

void KotlinClass::init(JNIEnv* env) {
    jclass clazz = env->FindClass("com/neborosoft/jnibridgegenerator/KotlinInterfaceTest");
    // Method ides generation
    dId = env->GetMethodID(clazz, "d", "(ILcom/neborosoft/jnibridgegenerator/CppTestConstructor;)V");
    
    eeerId = env->GetMethodID(clazz, "eeer", "([J)Ljava/lang/String;");
    
    uId = env->GetMethodID(clazz, "u", "([S)[S");
    // Method ides generation
}

KotlinClass::KotlinClass(JNIEnv *env, jobject obj) : JObject(env, obj) {
}

// Java method wrappers
void KotlinClass::d(int32_t e, CppTestConstructor* cppTest, const std::function<void(CppTestConstructor*)>& cppTestDeleter) {
    jint _e = ConvertFromCppType<jint>(env, e);   
jobject _cppTest = CreateCppTestConstructor(env, cppTest, cppTestDeleter);   
    env->CallVoidMethod(obj, dId, _e, _cppTest);
}


std::string KotlinClass::eeer(const std::vector<int64_t>& ee) {
    jlongArray _ee = ConvertFromCppType<jlongArray>(env, ee);   
    auto res = (jstring)env->CallObjectMethod(obj, eeerId, _ee);
    return ConvertToCppType<std::string>(env, res);
}


Eblo KotlinClass::u(const Eblo& r) {
    jshortArray _r = ConvertFromCppType<jshortArray>(env, r);   
    auto res = (jshortArray)env->CallObjectMethod(obj, uId, _r);
    return ConvertToCppType<Eblo>(env, res);
}
// Java method wrappers

