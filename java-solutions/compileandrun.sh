#!/bin/bash

PACK=info/kgeorgiy/ja/piche_kruz/bank
PACKNAME=info.kgeorgiy.ja.piche_kruz.bank
if [ ! -f $PACK/Server.class ]
then
    javac -cp . -d . $PACK/Server.java
fi

if [ ! -f $PACK/BankWebServer.class ]
then
    javac -cp . -d . $PACK/BankWebServer.java
fi

java -cp . $PACKNAME.BankWebServer
java -cp . $PACKNAME.Server

# if test -f info/kgeorgiy/ja/piche_kruz/$1/$2.class
# then
#     rm info/kgeorgiy/ja/piche_kruz/$1/*.class
# fi

# javac -Xlint:unchecked -p shared/ --add-modules info.kgeorgiy.java.advanced.$1,info.kgeorgiy.java.advanced.base -cp . info/kgeorgiy/ja/piche_kruz/$1/*.java

# if test -f info/kgeorgiz/ja/piche_kruz/$1/$2.class
# then
#     java -cp . -p shared/ --add-modules info.kgeorgiy.java.advanced.$1,info.kgeorgiy.java.advanced.base info.kgeorgiy.ja.piche_kruz.$1.$2 $3 $4 $5
# fi