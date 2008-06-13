#
# add-directory-common.sh
#
# Purpose: common definitions used to set up wps directories
#
# /opt/xl/web/
#            webdav/<name>
#            sym/<name>
#            jsp/<name>
#
# $Id: add-directory-common.sh,v 1.1 2006/01/24 14:51:32 jdb Exp $
#
# Deployed on: @deployment-time-stamp@

declare -r WPS_IN=/opt/xl/web/webdav
declare -r WPS_SYM=/opt/xl/web/sym
declare -r WPS_OUT=/opt/xl/web/jsp


if [ ! -d ${WPS_IN} ] ; then
    printf "The directory path was not valid: %s\nExiting...\n" ${WPS_IN}
    exit 1
fi

if [ ! -d ${WPS_SYM} ] ; then
    printf "The directory path was not valid: %s\nExiting...\n" ${WPS_SYM}
    exit 1
fi

if [ ! -d ${WPS_OUT} ] ; then
    printf "The directory path was not valid: %s\nExiting...\n" ${WPS_OUT}
    exit 1
fi
