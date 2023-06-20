#!/bin/bash

#Usage <PACK_NAME> List of ClassNames...

if [[ $# < 1 ]]
then
    echo "Expected at least 1 argument, given $#"
    exit 1
fi

ROOT=$(realpath ../../)
MY_REPO=$ROOT/java-advanced
OUTPUT=$MY_REPO/javadoc
SOURCE=$MY_REPO/java-solutions
PACK=$SOURCE/info/kgeorgiy/ja/piche_kruz/$1
PACK_NAME=info.kgeorgiy.ja.piche_kruz.$1
COURSE_REPO=$ROOT/java-advanced-2023
MODULE_PATH=$COURSE_REPO/artifacts:$COURSE_REPO/lib
MODULE_NAME=info.kgeorgiy.java.advanced.$1
DEPENDENCY_ROOT=$COURSE_REPO/modules/$MODULE_NAME
DEPENDENCY_DIR=$DEPENDENCY_ROOT/info/kgeorgiy/java/advanced/$1
DOCS="https://docs.oracle.com/en/java/javase/17/docs/api/"
J=".java"

if ! [ -d $OUTPUT ]
then
    mkdir $OUTPUT
fi

rm -r $OUTPUT/* &> /dev/null

CMD1="-d $OUTPUT -link $DOCS -sourcepath $SOURCE:$DEPENDENCY_ROOT -p $MODULE_PATH --add-modules $MODULE_NAME -author -private"
CMD2="$PACK_NAME"
CMD3="$PACK/*$J"
CMD4=""
for i in ${@:2}
do
    CMD4="$CMD4 $DEPENDENCY_DIR/$i$J"
done

javadoc $CMD1 $CMD2 $CMD3 $CMD4