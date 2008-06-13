/*
 * Copyright 2001-2006 LEARNING INFORMATION SYSTEMS PTY LIMITED
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * FileMirror.java
 *
 * Created on 18 October 2001, 13:00
 */

package org.ntropa.build;

import java.io.File;

import org.ntropa.utility.FileUtilities;

/**
 * 
 * @author jdb
 * @version $Id: FileMirror.java,v 1.4 2002/11/30 23:03:09 jdb Exp $
 */
public class FileMirror implements FileListener {

    /* The folder for reading HTML files and presentation parameters from */
    private final File _sourceDir;

    /* The folder for writing JSPs to and deleting JSPs from */
    private final File _destinationDir;

    /** Creates new FileMirror */
    public FileMirror(DirectoryPair dp) {
        /* getAbsoluteFile () makes a new object */
        _sourceDir = dp.getSource().getAbsoluteFile();
        _destinationDir = dp.getDestination().getAbsoluteFile();
    }

    private void copy(File source, File dest) {

        if (!FileUtilities.makeSymbolicLink(source, dest)) {
            log("Failed to create symbolic link:\n" + "Source: " + source + "\nDestincation: " + dest);
        }

    }

    public void fileAdded(FileListenerEvent e) {
        File sourceFile = new File(_sourceDir, e.getLocation());
        File destFile = new File(_destinationDir, e.getLocation());

        if (false)
            System.out.println("FileMirror: fileAdded: " + e.getLocation() + "\nSource file:      "
                    + sourceFile.getAbsolutePath() + "\nDestination file: " + destFile.getAbsolutePath());

        if (!sourceFile.isFile()) {
            log("Source was not a file: " + sourceFile);
            return;
        }

        if (destFile.exists())
            log("Destination already existed. Continued processing anyway: " + destFile);

        copy(sourceFile, destFile);

    }

    public void targetFileAdded(FileListenerEvent e) {
        fileAdded(e);
    }

    public void fileModified(FileListenerEvent e) {
        File sourceFile = new File(_sourceDir, e.getLocation());
        File destFile = new File(_destinationDir, e.getLocation());

        if (false)
            System.out.println("FileMirror: fileModified: " + e.getLocation() + "\nSource file:      "
                    + sourceFile.getAbsolutePath() + "\nDestination file: " + destFile.getAbsolutePath());

        if (!sourceFile.isFile()) {
            log("Source was not a file: " + sourceFile);
            return;
        }

        if (!destFile.isFile())
            log("Destination was not a file. Continued processing anyway: " + destFile);

        copy(sourceFile, destFile);
    }

    public void fileDeleted(FileListenerEvent e) {
        File destFile = new File(_destinationDir, e.getLocation());

        if (false)
            System.out.println("FileMirror: fileDeleted: " + e.getLocation() + "\nDestination file: "
                    + destFile.getAbsolutePath());

        /*
         * if the destination file was a link and the file the link links to has
         * been deleted, isFile () returns false. Since we have chosen to make
         * links from the symbolic file system it will often be the case that we
         * are deleting a dangling symbolic link so we drop the test here.
         * 
         * (exists () has this behaviour as well.)
         */
        /*
         * if ( ! destFile.isFile () ) { log ( "Destination was not a file: " +
         * destFile ) ; return ; }
         */

        if (destFile.isDirectory()) {
            log("Destination was a directory: " + destFile);
            return;
        }

        if (!destFile.delete())
            log("Failed to delete file: " + destFile);

    }

    public void targetFileDeleted(FileListenerEvent e) {
        fileDeleted(e);
    }

    /**
     * FIXME: use proper logger passed in at construction.
     */
    private void log(String msg) {
        System.out.println(this.toString() + "\n" + msg);
    }

    public String toString() {
        return "[FileMirror] _sourceDir: " + _sourceDir.getAbsolutePath() + ", _destinationDir: "
                + _destinationDir.getAbsolutePath();
    }

}
