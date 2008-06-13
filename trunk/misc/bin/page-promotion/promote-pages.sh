#!/bin/sh
#
# promote-pages.sh
#
# This script synchronises a subdirectory of /opt/xl/web/jsp on a remote host with
# the local copy.
#
# If the local host is dev.nav then the remote host will always be test.nav.
# If the local host is test.nav then the remote host will be one of treebeard or xenolith, depending
# on where the production site currently lives.
#
# $Id: promote-pages.sh,v 1.16 2006/02/07 14:50:24 jdb Exp $
#
# Deployed on: @deployment-time-stamp@

# We want to avoid requiring root privileges for page promotions
# but I failed to do this on 060207 jdb.
if [ $(id -u) != 0 ] ; then
    printf "Must be run as root.\nExiting...\n"
    exit 1
fi

declare -r CONFIG_NAME=promote-pages.conf
declare -r MAIN_DIR=/opt/xl/web/jsp
declare -r TEST_HOST=test.navigator.studylink.com
declare -r PRODUCTION_1_HOST=xenolith.studylink.com
#declare -r PRODUCTION_2_HOST=treebeard.studylink.com

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

# Verify each directory
for D in ${CHOICES}; do
    if [ ! -d ${MAIN_DIR}/$D ] ; then
        printf "The directory '%s' did not exist. Please fix the configuration file.\nExiting...\n" $D
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
    REMOTE_HOST=${PRODUCTION_1_HOST}
    ;;

    *)
    printf "This script can not be run on this host.\nExiting...\n"
    exit 1
    ;;
esac

printf "The remote host for %s is %s\n" ${DIR} ${REMOTE_HOST}

# The trailing slash on the source is important
# 'WEB-INF/' will not match a file.
#
# options explanation.
#
# -r: recurse into directories
# -L: copy the referent of symlinks. This is required because the wps generates links from jsp to sym, and from
#     sym to webdev
# -p: preserve permissions
# -o: preserve owner, root only
# -z: compress file data
# -v: increase verbosity
# --delete: delete files that do not exist in the sending side
# --exclude: exclude files matching pattern
#         WEB-INF/  : because the promotion of the WADD needs to be controlled by the developer
#         _include/ : because these are not used at runtime
#
CMD="rsync -rLpo -zv --stats --delete --exclude=WEB-INF/ --exclude=_include/ ${MAIN_DIR}/${DIR}/ ${REMOTE_HOST}:${MAIN_DIR}/${DIR}"

printf "Executing: %s\n" "${CMD}"

# This did not result in directories being created with the group write bit set
# even with -p not present in the rsync command:
#umask 002

$CMD

# Fix permissions so a different user can do a promotion.
#
# Defer expansion of * until the command is executed on the
# remote server by single quoting.
#
# Only root can change the permissions of a file owned by a different user.
# SWITCHED BACK TO RUNNING AS root so this problem went away: ssh ${REMOTE_HOST} chmod -R g+w ${MAIN_DIR}/${DIR}/'*'