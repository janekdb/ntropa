#!/bin/sh
#
# backup-pages.sh
#
# This script backs up the wps input files into a data stamped
# directories. 
#
# The list of directories to backup is sourced from the page promotion
# directory list and backup.conf for extras.
#
# $Id: backup-pages.sh,v 1.3 2006/02/20 14:04:14 jdb Exp $
#
# Deployed on: 2006-02-08 at 01:45:38 EST

# TODO
# Conditional backup with: find -mtime -2

# Make list of directories to back up

declare -r WEB_ROOT=/opt/xl/web

if [ ! -d ${WEB_ROOT} ] ; then
    printf "The directory %s did not exist.\nExiting...\n" ${WEB_ROOT}
    exit 1
fi

declare -r STATIC_DIR_FILE=${WEB_ROOT}/jsp/promote-pages.conf

if [ ! -r ${STATIC_DIR_FILE} ] ; then
    printf "The configuration file %s was not readable.\nExiting...\n" ${STATIC_DIR_FILE}
    exit 1
fi

declare -r DYNAMIC_DIR_FILE=${WEB_ROOT}/webdav/promote-pages-dynamic.conf

if [ ! -r ${DYNAMIC_DIR_FILE} ] ; then
    printf "The configuration file %s was not readable.\nExiting...\n" ${DYNAMIC_DIR_FILE}
    exit 1
fi

# List directories in webdav that should be backed up in addition to
# the directories already listed in the two conf files above.
declare -r EXTRA_DIR_FILE=${WEB_ROOT}/webdav/backup-extra.conf

if [ ! -r ${EXTRA_DIR_FILE} ] ; then
    printf "The configuration file %s was not readable.\nExiting...\n" ${EXTRA_DIR_FILE}
    exit 1
fi

declare -r WEBDAV_DIR=${WEB_ROOT}/webdav

if [ ! -d ${WEBDAV_DIR} ] ; then
    printf "The directory %s did not exist.\nExiting...\n" ${WEBDAV_DIR}
    exit 1
fi

declare -r BACKUP_DIR=${WEBDAV_DIR}/BACKUP-SCHEDULED
if [ ! -d ${BACKUP_DIR} ] ; then
    printf "The directory %s did not exist. Creating now...\n" ${BACKUP_DIR}
    mkdir ${BACKUP_DIR}
fi

if [ ! -d ${BACKUP_DIR} ] ; then
    printf "The directory %s did not exist even after attempting to create it.\nExiting...\n" ${BACKUP_DIR}
    exit 1
fi

# Make the list of directories to backup
declare -r RAW=$(cat ${STATIC_DIR_FILE} ${DYNAMIC_DIR_FILE} ${EXTRA_DIR_FILE}|sort|uniq)

# Remove non-existent directories
VALID=""
for DIR in ${RAW} ; do
    TARGET=${WEBDAV_DIR}/${DIR}
    if [ ! -d ${TARGET} ] ; then
        printf "The directory to be backed up did not exist %s. Skipping...\n" ${TARGET}
    else
        VALID="${VALID} ${DIR}"
    fi
done

declare -r DT_STAMP=$(date +%Y-%m-%d-%H-%M-%S)
declare -r DESTINATION=${BACKUP_DIR}/${DT_STAMP}


if ! mkdir ${DESTINATION} ; then
    printf "Failed to create destination directory %s.\nExiting...\n" ${DESTINATION}
    exit 1
fi

# capture stdout and stderr and email it
declare -r LOG=/tmp/backup-pages.out
(
echo "Backup started at" $(date)
printf "Backup destination: '%s'\n" ${DESTINATION}
echo

for DIR in ${VALID} ; do
    printf "Backing up '%s'.." ${DIR}
    TARGET=${WEBDAV_DIR}/${DIR}
    if cp -a ${TARGET} ${DESTINATION} ; then
        printf "OK\n"
    else
        printf "FAIL\n"
    fi
done

echo
echo "Backup completed at" $(date)

) >& ${LOG}

TO=studylink.dev.emailmonitor@gmail.com
CC=yan@studylink.com

cat ${LOG} | mail -s "NAVDEV WPS Backup Log" -c $CC $TO

