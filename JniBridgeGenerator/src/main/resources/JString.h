//
// Created by Semyon Tikhonenko on 7/27/17.
//

#ifndef CPPUTILS_JNIJniUTFString_H
#define CPPUTILS_JNIJniUTFString_H


#include <jni.h>

class JString {
    jstring string;
    const char *data = nullptr;
    JNIEnv *env;
public:
    JString(jstring string, JNIEnv *env);

    JString(const JString&) = delete;
    JString(JString&&) = default;

    JString& operator=(const JString&) = delete;
    JString& operator=(JString&&) = default;

    const char *getData();
    jint length() const;

    ~JString();
};

#endif //CPPUTILS_JNIJniUTFString_H
