#
# standardchannelmonitorfs.sh
# Created on October 23, 2001, 3:32 PM
#
# @author  jdb
# @version $Id: standardchannelmonitorfs.sh,v 1.1 2001/10/23 15:35:20 jdb Exp $
#
# Run this to package the test filesystem into a zip.
#   
#   $ sh ./standardchannelmonitorfs.sh

# Get rid of Emacs backup files.
find standardchannelmonitorfs -name '*~' -exec rm '{}' ';'

rm standardchannelmonitorfs.zip
zip -r standardchannelmonitorfs standardchannelmonitorfs

echo "Done"
