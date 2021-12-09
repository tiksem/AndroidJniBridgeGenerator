//
// Created by Semyon Tikhonenko on 12/3/21.
//

#include "KotlinInterfaceTest.h"
#include "Converters.h"

// Method ides declaration
static jmethodID dId = nullptr;

static jmethodID eeerId = nullptr;
// Method ides declaration

void KotlinInterfaceTest::init(JNIEnv* env) {
    jclass clazz = env->FindClass("com/neborosoft/jnibridgegenerator/KotlinInterfaceTest");
    // Method ides generation
    dId = env->GetMethodID(clazz, "d", "(I)V");
    
    eeerId = env->GetMethodID(clazz, "eeer", "([J)Ljava/lang/String;");
    // Method ides generation
}

KotlinInterfaceTest::KotlinInterfaceTest(JNIEnv *env, jobject obj) : JObject(env, obj) {
}

// Java method wrappers
void KotlinInterfaceTest::d(int32_t e) {
    jint _e = ConvertFromCppType<jint>(env, e);   
    (env->CallVoidMethod(obj, dId, _e));
}


std::string KotlinInterfaceTest::eeer(const std::vector<int64_t>& ee) {
    jlongArray _ee = ConvertFromCppType<jlongArray>(env, ee);   
    auto res = static_cast<jstring>(env->CallObjectMethod(obj, eeerId, _ee));
    return ConvertToCppType<std::string>(env, res);
}
// Java method wrappers

