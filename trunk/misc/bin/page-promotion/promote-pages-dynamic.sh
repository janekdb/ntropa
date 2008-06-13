#!/bin/sh
#
# promote-pages-dynamic.sh
#
# This script synchronises a subdirectory of /opt/xl/web/webdav on a remote host with
# the local copy.
#
# If the local host is dev.nav then the remote host will always be test.nav.
# If the local host is test.nav then the remote host will be one of treebeard or xenolith, depending
# on where the production site currently lives.
#
# $Id: promote-pages-dynamic.sh,v 1.2 2006/02/07 12:00:54 jdb Exp $
#
# Deployed on: @deployment-time-stamp@

if [ $(id -u) != 0 ] ; then
    printf "Must be run as root.\nExiting...\n"
    exit 1
fi

declare -r CONFIG_NAME=promote-pages-dynamic.conf
declare -r MAIN_DIR=/opt/xl/web/webdav
declare -r TEST_HOST=test.navigator.studylink.com
declare -r PRODUCTION_1_HOST=xenolith.studylink.com
declare -r PRODUCTION_2_HOST=treebeard.studylink.com

declare REMOTE_HOST=

if [ ! -d $MAIN_DIR ] ; then
    printf "The directory %s did not exist.\nExiting...\n" $MAIN_DIR
    exit 1
fi

declare -r CONFIG_FILE=${MAIN_DIR}/${CONFIG_NAME}

if [ ! -r ${CONFIG_FILE} ] ; then
    printf "The configuration file %s was not readable.\nThis file contains a list of directories that can be promoted. For example,\n    intl-yahoo-india\n    intl-yahoo-asia\nExiting...\n" ${CONFIG_FILE}
    exit 1
fi

# A list of directories to promote
declare -r CHOICES=$(cat ${CONFIG_FILE}|sort)
#$(find $MAIN_DIR -regex "$MAIN_DIR/[a-z]+[a-z-]+[a-z]" -maxdepth 1 -not -regex "$MAIN_DIR/\(intl\|mba\|media\|postmanpat\|shared-automatic\)" -printf "%f\n"|sort)

# Verify each directory
for D in ${CHOICES}; do
    if [ ! -d ${MAIN_DIR}/$D ] ; then
        printf "The directory %s did not exist. Please fix the configuration file.\nExiting...\n" $D
        exit 1
    fi
done

PS3="Pick a directory to promote. Ctrl-d to exit: "
select DIR in $CHOICES ;
do
    break
done

# Protect against user choosing out of range
if [ -z $DIR ] ; then
    printf "The directory was not set.\nExiting....\n"
    exit 1
fi


case $(hostname) in
    dev1.navigator.studylink.com)
    REMOTE_HOST=${TEST_HOST}
    ;;

    test.navigator.studylink.com)
    # There is not default case.
    case $DIR in
        edge)
        REMOTE_HOST=${PRODUCTION_1_HOST}
        ;;

        pg-au-fairfax)
        REMOTE_HOST=${PRODUCTION_1_HOST}
        ;;

        ug-au-fairfax)
        REMOTE_HOST=${PRODUCTION_1_HOST}
        ;;

        intl-yahoo-asia)
        REMOTE_HOST=${PRODUCTION_1_HOST}
        ;;

        intl-yahoo-india)
        REMOTE_HOST=${PRODUCTION_1_HOST}
        ;;

        intl-tafe-southaustralia)
        REMOTE_HOST=${PRODUCTION_1_HOST}
        ;;

        *)
        printf "No REMOTE_HOST specified for %s.\nExiting...\n" ${DIR}
        exit 1
        ;;
    esac
    ;;

    *)
    printf "This script can not be run on this host.\nExiting...\n"
    exit 1
    ;;
esac

printf "The remote host for %s is %s\n" ${DIR} ${REMOTE_HOST}

# The trailing slash on the source is important
CMD="rsync -avz --delete ${MAIN_DIR}/${DIR}/ ${REMOTE_HOST}:${MAIN_DIR}/${DIR}"

printf "Executing: %s\n" "${CMD}"

$CMD

# could not get this to run as a non-super-user
#printf "Setting group write bit...\n"
#
## Let the * be expanded on the remote machine. This means there will not be
## any attempt to change the mode of the top level directory which would fail.
#ssh ${REMOTE_HOST} chmod -R g+w ${MAIN_DIR}/${DIR}'/*'
