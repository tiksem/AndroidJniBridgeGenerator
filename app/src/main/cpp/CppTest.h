//
// Created by Semyon Tikhonenko on 11/29/21.
//

#ifndef JNIBRIDGEGENERATOR_CppTest_H
#define JNIBRIDGEGENERATOR_CppTest_H

#include "JArray.h"
#include "JString.h"
#include <string>
// headers
#include "KotlinClass.h"
// headers

class CppTest {
public:
    // Public Jni Interface
        void e(const JIntArray& list);
        void ffg(const std::function<void(int32_t)>& l);
        std::string g();
        int32_t getA();
        void rrr(KotlinClass i);
        void yo(const JString& value);
    // Public Jni Interface
};


#endif //JNIBRIDGEGENERATOR_CppTest_H
