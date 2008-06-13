#
# scriptwritertestfs.sh
# Created on October 23, 2001, 3:32 PM
#
# @author  jdb
# @version $Id: standardpresentationfindertestfs.sh,v 1.1 2001/11/29 10:55:22 rj Exp $
#
# Run this to package the test filesystem into a zip.
#   
#   $ sh ./scriptwritertestfs.sh

# Get rid of Emacs backup files.
find standardpresentationfindertestfs -name '*~' -exec rm '{}' ';'

rm standardpresentationfindertestfs.zip
zip -r standardpresentationfindertestfs standardpresentationfindertestfs

echo "Done"
