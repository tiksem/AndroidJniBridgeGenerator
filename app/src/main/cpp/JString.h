//
// Created by Semyon Tikhonenko on 7/27/17.
//

#ifndef CPPUTILS_JNIJniUTFString_H
#define CPPUTILS_JNIJniUTFString_H

#include <jni.h>

class JString {
    jstring string;
    mutable const char *data = nullptr;
    JNIEnv *env;
public:
    JString(jstring string, JNIEnv *env);

    JString(const JString&) = delete;
    JString(JString&&);

    JString& operator=(const JString&) = delete;
    JString& operator=(JString&&);

    const char *getData() const;
    jint length() const;

    ~JString();
};

#endif //CPPUTILS_JNIJniUTFString_H
