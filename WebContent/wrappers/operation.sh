#!/bin/bash
#use: isplain.sh <username> <command> <arg1> <arg2>
USERNAME=$1
COMMAND=$2
ARG1=$3
ARG2=${4}
RESULT=0
export LC_ALL=en_US.UTF-8
case $COMMAND in
    "list")
    cmd="ls -1A ${ARG1}"
    ;;
    "isPlain")
    cmd="test -f $ARG1"
    ;;
    "isSymbolicLink")
    cmd="test -h $ARG1"
    ;;
    "isBlockSpecial")
    cmd="test -b $ARG1"
    ;;
    "isCharacterSpecial")
    cmd="test -c $ARG1"
    ;;
    "hasUserIDbitSet")
    cmd="test -u $ARG1"
    ;;
    "hasStickyBitSet")
    cmd="test -k $ARG1"
    ;;
    "hasGroupIDbitSet")
    cmd="test -g $ARG1"
    ;;
    "isReadable")
    cmd="test -r $ARG1"
    ;;
    "isWritable")
    cmd="test -w $ARG1"
    ;;
    "isExecutable")
    cmd="test -x $ARG1"
    ;;
    "read")
    cmd="cat $ARG1"
    ;;
    "stat")
    cmd="stat --format=\"%d;%i;%a;%h;%u;%g;%t%T;%s;%X;%Y;%Z;%B;%b\" $ARG1"
    ;;
    "lstat")
    cmd="lstat $ARG1"
    ;;
    "unlink")
    cmd="unlink $ARG1"
    ;;
    "fullResolve")
    cmd="readlink -f $ARG1"
    ;;
    "write")
    cmd="exec >$ARG1 cat"
    ;;
    "mkdir")
        mask=${ARG2}
        if [ ${mask} ] ;then
            mask="-m ${mask}"
        fi
        cmd="mkdir ${mask} -p $ARG1"
    ;;
    "rename")
    cmd="mv ${ARG1} ${ARG2}"
    ;;
    "symlink")
    cmd="ln -s ${ARG1} ${ARG2}"
    ;;
    "processuid")
    cmd="ps -eopid,uid | grep \"^${ARG1}\" | (read p1 p2; echo \$p2)"
    ;;
    "chmod")
    cmd="chmod ${ARG2} ${ARG1}"
    ;;
    "rmdir")
    cmd="rmdir $ARG1"
    ;;
    "groupname")
    cmd="cat /etc/group | grep ':${ARG1}:'"
    ;;
    "username")
    cmd="getent passwd ${ARG1}"
    ;;
esac
export SHELL=/bin/bash
cmd="sudo -u ${USERNAME} -s ${cmd} "
echo >&2 "*** COMMAND: " ${cmd};
if ${cmd}
then
    echo >&2 "OK"
    RESULT=0
else
    echo >&2 "FAIL"
    RESULT=1
fi
exit ${RESULT}
