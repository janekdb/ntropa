#! /bin/sh
#
# $Id: find-dependents.sh,v 1.3 2005/05/31 11:50:19 jdb Exp $

if [ "x"$1 = "x" ] ; then
    printf "usage: $0 <directory>\n"
    exit 1
fi

# Strip any trailing solidus
declare -r SITE_ID=${1%/}

if ! [ -d ${SITE_ID} ] ; then
    printf "${SITE_ID} was not a directory\n"
    exit 1
fi

LINKS=$(find [a-z]* -name '*.link')
if [ -z "${LINKS}" ] ; then
    exit 0
fi

grep -l href=wps://${SITE_ID}/ ${LINKS}|cut -f1 -d/|sort|uniq
