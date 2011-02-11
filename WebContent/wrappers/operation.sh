#!/bin/bash
#use: isplain.sh <username> <command> <file>
USERNAME=$1
COMMAND=$2
FILE=$3
RESULT=0
case $COMMAND in
    "list")
    cmd="ls -1A ${FILE}"
    ;;
    "isPlain")
    cmd="test -f $FILE"
    ;;
    "isSymbolicLink")
    cmd="test -h $FILE"
    ;;
    "isBlockSpecial")
    cmd="test -b $FILE"
    ;;
    "isCharecterSpecial")
    cmd="test -c $FILE"
    ;;
    "hasUserIDbitSet")
    cmd="test -u $FILE"
    ;;
    "hasStickyBitSet")
    cmd="test -k $FILE"
    ;;
    "hasGroupIDbitSet")
    cmd="test -g $FILE"
    ;;
    "isReadable")
    cmd="test -r $FILE"
    ;;
    "isWritable")
    cmd="test -w $FILE"
    ;;
    "isExecutable")
    cmd="test -x $FILE"
    ;;
    "read")
    cmd="cat $FILE"
    ;;
    "stat")
    cmd="stat --format=\"%d;%i;%a;%h;%u;%g;%t%T;%s;%X;%Y;%Z;%B;%b\" $FILE"
    ;;
    "lstat")
    cmd="lstat $FILE"
    ;;
    "unlink")
    cmd="unlink $FILE"
    ;;
    "fullResolve")
    cmd="readlink -f $FILE"
    ;;
    "write")
    cmd="exec >$FILE cat"
    ;;
esac
cmd="sudo -u ${USERNAME} -s $cmd"
echo >&2 "*** COMMAND: " ${cmd};
if ${cmd};
then
    echo >&2 "OK"
    RESULT=0
else
    echo >&2 "FAIL"
    RESULT=1
fi
exit ${RESULT}
