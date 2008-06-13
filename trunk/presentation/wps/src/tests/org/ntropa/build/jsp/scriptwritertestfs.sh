#
# scriptwritertestfs.sh
# Created on October 23, 2001, 3:32 PM
#
# @author  jdb
# @version $Id: scriptwritertestfs.sh,v 1.1 2001/11/23 17:52:44 jdb Exp $
#
# Run this to package the test filesystem into a zip.
#   
#   $ sh ./scriptwritertestfs.sh

# Get rid of Emacs backup files.
find scriptwritertestfs -name '*~' -exec rm '{}' ';'

rm scriptwritertestfs.zip
zip -r scriptwritertestfs scriptwritertestfs

echo "Done"
