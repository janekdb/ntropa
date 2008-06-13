#! /bin/sh
#
# add-directory-jsp.sh
#
# Purpose: add a new jsp wps directory:
#
# /opt/xl/web/
#            jsp/<name>
#
# $Id: add-directory-jsp.sh,v 1.2 2006/01/24 15:02:50 jdb Exp $
#
# Deployed on: @deployment-time-stamp@

if [ $(id -u) != 0 ] ; then
    printf "Must be run as root.\nExiting...\n"
    exit 1
fi

. add-directory-common.sh

if [[ $# > 1 ]] ;then
    printf "Too many arguments\nExiting...\n"
    exit 1
fi

TARGET=$1

if [ "x"${TARGET} = "x" ] ; then
    printf "Target was missing\nExiting...\n"
    exit 1
fi

# Jsp

D=${WPS_OUT}/${TARGET}

if [ ! -e ${D} ] ; then
    if ! mkdir -m 2770 $D ; then
        printf "Failed to create directory: %s\nExiting...\n" $D
        exit 1 
    fi
    chown root:www $D
    printf "Created: %s\n" $D
fi

