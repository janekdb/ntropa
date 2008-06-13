#
# standardapplicationfinderfs.sh
# Created on October 23, 2001, 3:32 PM
#
# @author  jdb
# @version $Id: standardapplicationfinderfs.sh,v 1.1 2001/11/22 15:04:03 jdb Exp $
#
# Run this to package the test filesystem into a zip.
#   
#   $ sh ./standardapplicationfinderfs.sh

# Get rid of Emacs backup files.
find standardapplicationfinderfs -name '*~' -exec rm '{}' ';'

rm standardapplicationfinderfs.zip
zip -r standardapplicationfinderfs standardapplicationfinderfs

echo "Done"
