//
// Created by Semyon Tikhonenko on 12/23/21.
//

#include "KotlinConstructors.h"
#include "Converters.h"

// id
static jmethodID constructorKotlinObject = nullptr;
// id

// class
static jclass classKotlinObject = nullptr;
// class

void InitKotlinConstructors(JNIEnv* env) {
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
// Constructors
