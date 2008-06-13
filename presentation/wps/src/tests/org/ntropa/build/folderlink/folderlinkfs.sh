#
# folderlinkfs.sh
# Created on August, 2001, 3.51 PM
#
# @author  jdb
# @version $Id: folderlinkfs.sh,v 1.2 2001/08/02 16:50:32 jdb Exp $
#
# Run this to package the test filesystem into a zip.
#   
#   $ sh ./folderlinkfs.sh

# Get rid of Emacs backup files.
find folderlinkfs -name '*~' -exec rm '{}' ';'

rm folderlinkfs.zip
zip -r folderlinkfs folderlinkfs

echo "Done"
