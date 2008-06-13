#
# links-collect.sh
#
# Purpose: Collect information about link files
#
# $Id: links-collect.sh,v 1.3 2006/06/08 11:57:41 jdb Exp $
#
# Deployed on: @deployment-time-stamp@

declare -r WPS_IN=/opt/xl/web/webdav
declare -r WPS_SYM=/opt/xl/web/sym

if [ ! -d ${WPS_IN} ] ; then
    printf "The directory path was not valid: %s\nExiting...\n" ${WPS_IN}
    exit 1
fi

if [ ! -d ${WPS_SYM} ] ; then
    printf "The directory path was not valid: %s\nExiting...\n" ${WPS_SYM}
    exit 1
fi

echo "BROKEN LINKS: START"
echo
cd ${WPS_SYM}
# -lname pattern
#   File  is  a symbolic link whose contents match shell pattern pattern.  The metacharacters do not treat `/' or `.' specially.  If the
#   -L option or the -follow option is in effect, this test returns false unless the symbolic link is broken.
#
# -L does not work on GNU find version 4.1.7
# -L does work on
#   GNU find version 4.2.19
#   Features enabled: D_TYPE O_NOFOLLOW(enabled)
#
find -follow -lname '*' 2>&1| cat -n | column -t
echo
echo "BROKEN LINKS: END"
echo "How to interpret the broken links list: The path is the value contained in the link file."
echo "TODO: Show which link file is creating the broken link"
echo

cd ${WPS_IN}

topleveldirs(){
    find -type d -mindepth 1 -maxdepth 1 -name '[a-z]*[a-z0-9]'|cut -c3-|sort
}

display(){
    local link=$1
    printf '%s -> %s\n' $link $(cat $link) |cut -c2-
}

for d in $(topleveldirs) ; do
    echo $d
    cd $d
    find -type f -name '*.link' |
    (
	while read link; do printf '....' ; display $link ;done
    ) | column -t
    cd -
    echo
done