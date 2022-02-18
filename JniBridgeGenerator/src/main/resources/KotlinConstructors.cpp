//
// Created by Semyon Tikhonenko on 12/23/21.
//

#include "KotlinConstructors.h"
#include "Converters.h"

static jmethodID constructorString = nullptr;
static jclass classString = nullptr;

// id
static jmethodID constructorKotlinObject = nullptr;
// id

// class
static jclass classKotlinObject = nullptr;
// class

static jclass stringClass = nullptr;

jobjectArray CreateStringArray(JNIEnv* env, jint length) {
    return env->NewObjectArray(length, stringClass, nullptr);
}

void InitKotlinConstructors(JNIEnv* env) {
    stringClass = env->FindClass("java/lang/String");
    // Init constructor
    classKotlinObject = env->FindClass("kotlinclassname");
    constructorKotlinObject = env->GetMethodID(classKotlinObject, "<init>", "jniSignature");
    // Init constructor
}

// Constructors
jobject CreateKotlinObject(JNIEnv* env___Args) {
// converters
    return env->NewObject(classKotlinObject, constructorKotlinObject___args);
}

jobjectArray CreateKotlinObjectArray(JNIEnv* env, jint length) {
    return env->NewObjectArray(length, classKotlinObject, nullptr);
}
// Constructors
