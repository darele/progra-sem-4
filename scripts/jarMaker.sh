#!/bin/bash

ROOT=../../
MY_REPO=$ROOT/java-advanced
SCRIPTS=$MY_REPO/scripts
SOURCE=$MY_REPO/java-solutions
COURSE_REPO=../../java-advanced-2023/artifacts
PACK=info/kgeorgiy/ja/piche_kruz/implementor
MODULE_NAME=info.kgeorgiy.java.advanced.implementor

cd $SOURCE

javac -p $COURSE_REPO --add-modules $MODULE_NAME -cp . -d . $PACK/Implementor.java

JARCLASS=info.kgeorgiy.ja.piche_kruz.implementor.Implementor
MODULE_NAME=info.kgeorgiy.java.advanced.implementor

echo "Class-Path: $COURSE_REPO/$MODULE_NAME.jar" > tempfile.txt

jar --create --verbose --file $SCRIPTS/JarImplementor.jar --module-path $COURSE_REPO --manifest tempfile.txt --main-class $JARCLASS $PACK/*.class

rm tempfile.txt

cd ../scripts

jar xf JarImplementor.jar META-INF/MANIFEST.MF

mv META-INF/MANIFEST.MF ./
rm -r META-INF