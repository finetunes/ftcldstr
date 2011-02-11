#!/bin/bash

#call me as "sudo checkpasswd.sh usernametotest passwordtotest"
#i will return 0 on success
#i will return 1 on fail
#i will return 111 on strange fail
#i will also write "OK" or "FAIL" in those cases

#http://ubuntuforums.org/archive/index.php/t-1232715.html

US=$1
GIVENPW=$2

#echo $UID

if [ $# -ne 2 ] || [ $UID -ne 0 ] ; then
	echo call me as \"sudo $0 usernametotest passwordtotest\"
	exit 111
fi

kk=`grep thiess /etc/shadow`
#hopefully only one line...
#echo KK: $kk

#echo $kk | awk -F: '{printf("1: %s\n2: %s\n3: %s\n",  $1, $2, $3);}'
UU=`echo $kk | awk -F: '{printf("%s", $2);}'`
#echo UU: $UU

ENCTYPE=`echo $UU | awk -F\$ '{printf("%s",  $2);}'`
#echo ENCTYPE: $ENCTYPE

SALT=`echo $UU | awk -F\$ '{printf("%s",  $3);}'`
#echo SALT: $SALT

PW=`echo $UU | awk -F\$ '{printf("%s",  $4);}'`
#echo PW: $PW

#still have to check the enctype... here fixed on sha-512

#echo MKPASSWD_USED: mkpasswd -m sha-512 ${GIVENPW} ${SALT}
TOTEST=`mkpasswd -m sha-512 ${GIVENPW} ${SALT}`

#echo $TOTEST AGAINST $UU

if [ "${TOTEST}" == "${UU}" ] ; then
	echo OK
	exit 0
else
	echo FAIL
	exit 1
fi

echo FAIL
exit 1111

