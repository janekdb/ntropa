#! /bin/sh
#
# $Id: find-dependencies.sh,v 1.2 2005/05/31 11:50:19 jdb Exp $

if [ "x"$1 = "x" ] ; then
    printf "usage: $0 <directory>\n"
    exit 1
fi

declare -r SITE_ID=$1

if ! [ -d ${SITE_ID} ] ; then
    printf "${SITE_ID} was not a directory\n"
    exit 1
fi

# grep is used to force a newline at the end of the contents of the link file
# -h = --no-filename
LINKS=$(find ${SITE_ID} -name '*.link')
if [ -z "${LINKS}" ] ; then
    exit 0
fi

grep -h wps:// ${LINKS}|cut -f3 -d/|sort|uniq

