//
// Created by Semyon Tikhonenko on 12/3/21.
//

#include "CppTest.h"
#include "Singleton.h"

void CppTest::e(const JIntArray &ptr) {

}

std::string CppTest::g() {
    return "str";
}

int32_t CppTest::getA() {
    Singleton::instance().yo();
    return 35;
}

void CppTest::yo(const JString &ptr) {

}

void CppTest::ffg(const std::function<void(int32_t)> &l) {
    l(35);
}

void CppTest::rrr(KotlinClass i) {

}
