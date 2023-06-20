#!/bin/bash

#Usage package_name ClassName Variant Auxiliary_Mod_To_Include TesterName(default package_name)

#This is an executable script for java advanced CT course's homework
#It can be used in windows if you execute it from GitBash

#for all hwks you just have to specify 3 arguments for example

#./test student StudentDB StudentQuery

#but for ParallelMapper I had to add the other flags

#./test.sh concurrent IterativeParallelism scalar mapper 
#(to test IterativeParallelism, only when you have completed ParallelMapper hwk, before that it works ok with just 3 arguments)

#./test.sh concurrent IterativeParallelism scalar mapper mapper
#(To test ParallelMapper hwk)

#Write your last name
STUDENT=piche_kruz

#it seems, after kgeorgiy, folder's name is different every year, change if needed
SOL_PATH=info/kgeorgiy/ja
PACKAGE_NAME=$1
SOL_NAME=$2
SOL_CLASS="info.kgeorgiy.ja.$STUDENT.$PACKAGE_NAME.$SOL_NAME"
MOD_PATH=shared
MOD_ROOT=info.kgeorgiy.java.advanced
MODS="$MOD_ROOT.$PACKAGE_NAME"
TESTER=$PACKAGE_NAME

if [[ (( $# > 3 )) && $4 != 'o' ]]
then
    MODS="$MODS,$MOD_ROOT.$4"
fi

if [[ (( $# > 4 )) && $5 != 'o' ]]
then
    MODS="$MODS,$MOD_ROOT.$5"
    TESTER=$5
fi

if test -f $SOL_PATH/$STUDENT/$PACKAGE_NAME/$SOL_NAME.class
then
    rm $SOL_PATH/$STUDENT/$PACKAGE_NAME/$SOL_NAME.class
fi

echo "" > output.txt

javac -Xlint:unchecked -p $MOD_PATH --add-modules $MODS -cp . -d . $SOL_PATH/$STUDENT/$PACKAGE_NAME/*.java

if test -f $SOL_PATH/$STUDENT/$PACKAGE_NAME/$SOL_NAME.class
then
    if [[ ${@: -1} == 'o' ]]
    then
        java -cp . -p old_tests/ --add-modules $MODS -m $MOD_ROOT.$TESTER $3 $SOL_CLASS &> output.txt
    else
        java -cp . -p $MOD_PATH --add-modules $MODS -m $MOD_ROOT.$TESTER $3 $SOL_CLASS &> output.txt
    fi
fi