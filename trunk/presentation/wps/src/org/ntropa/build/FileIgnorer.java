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
 * FileIgnorer.java
 *
 * Created on 18 October 2001, 11:24
 */

package org.ntropa.build;

/**
 * This class is unused to absorb <code>FileListenerEvent</code>s. This
 * prevents any further notification of <code>FileListenerEvent</code>s to
 * listeners later in the list of listeners.
 * 
 * FIXME: remove System.out.println("")s;
 * 
 * @author jdb
 * @version $Id: FileIgnorer.java,v 1.2 2001/11/22 18:20:14 jdb Exp $
 */
public class FileIgnorer implements FileListener {

    /* The debug level */
    private int _debug = 0;

    public void fileDeleted(FileListenerEvent e) {
        if (_debug > 0)
            log("FileIgnorer: fileDeleted: " + e.getLocation());
    }

    public void fileAdded(FileListenerEvent e) {
        if (_debug > 0)
            log("FileIgnorer: fileAdded: " + e.getLocation());
    }

    public void fileModified(FileListenerEvent e) {
        if (_debug > 0)
            log("FileIgnorer: fileModified: " + e.getLocation());
    }

    public void targetFileDeleted(FileListenerEvent e) {
        if (_debug > 0)
            log("FileIgnorer: targetFileDeleted: " + e.getLocation());
    }

    public void targetFileAdded(FileListenerEvent e) {
        if (_debug > 0)
            log("FileIgnorer: targetFileAdded: " + e.getLocation());
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

}
