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
 * DirectoryMirror.java
 *
 * Created on 18 October 2001, 11:35
 */

package org.ntropa.build;

import java.io.File;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A <code>DirectoryMirror</code> receives notifications of directories that
 * have been added or deleted in one file system and adds or deletes the
 * corresponding directories in the other file system.
 * 
 * The <code>DirectoryMirror</code> does not need to be able to access the
 * source file system, therefore this information is not available to the
 * object.
 * 
 * @author jdb
 * @version $Id: DirectoryMirror.java,v 1.4 2002/11/30 23:03:09 jdb Exp $
 */
public class DirectoryMirror implements FileListener {

    /* The folder for creating directories in and removing directories from */
    protected File m_DestinationDir;

    /* The debug level */
    private int _debug = 0;

    /* Prevent no-arg construction */
    private DirectoryMirror() {
    }

    /** Creates new DirectoryMirror */
    public DirectoryMirror(File destinationDir) {

        /* getAbsoluteFile () makes a new object */
        m_DestinationDir = destinationDir.getAbsoluteFile();

    }

    public void fileDeleted(FileListenerEvent e) {
        File destFile = new File(m_DestinationDir, e.getLocation());

        if (_debug > 0)
            log("DirectoryMirror: fileDeleted: " + e.getLocation() + "\nDestination file: "
                    + destFile.getAbsolutePath());

        if (!destFile.delete())
            log("Failed to delete directory: " + destFile);

    }

    public void fileAdded(FileListenerEvent e) {
        File destFile = new File(m_DestinationDir, e.getLocation());

        if (_debug > 0)
            log("DirectoryMirror: fileAdded: " + e.getLocation() + "\nDestination file: " + destFile.getAbsolutePath());

        if (!destFile.mkdir())
            log("Failed to create directory: " + destFile);

    }

    public void fileModified(FileListenerEvent e) {

        File destFile = new File(m_DestinationDir, e.getLocation());

        if (_debug > 0)
            log("DirectoryMirror: **ERROR**: this object does not handle directory modifications." + e.getLocation()
                    + "\nDestination file: " + destFile.getAbsolutePath());

    }

    public void targetFileDeleted(FileListenerEvent e) {

        File destFile = new File(m_DestinationDir, e.getLocation());

        log("DirectoryMirror: **ERROR**: this object does not handle target file deletions because these only apply to symlinks."
                + e.getLocation() + "\nDestination file: " + destFile.getAbsolutePath());

    }

    public void targetFileAdded(FileListenerEvent e) {

        File destFile = new File(m_DestinationDir, e.getLocation());

        log("DirectoryMirror: **ERROR**: this object does not handle target file additions because these only apply to symlinks."
                + e.getLocation() + "\nDestination file: " + destFile.getAbsolutePath());

    }

    /**
     * Set the debug level 0 - 99
     * 
     * @param debug
     *            The required debug level in the range 0 - 99.
     */
    public void setDebugLevel(int debug) {
        /* A negative arg is a mistake; go large in response */
        _debug = debug >= 0 ? debug : 99;
    }

    /**
     * FIXME: use proper logger passed in at construction.
     */
    private void log(String msg) {
        System.out.println( /* this.toString () + "\n" + */msg);
    }

    public String toString() {

        return new ToStringBuilder(this).append("m_DestinationDir", m_DestinationDir).append("_debug", _debug)
                .toString();

    }

}
