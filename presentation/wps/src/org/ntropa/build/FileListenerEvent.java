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
 * FileListenerEvent.java
 *
 * Created on 31 August 2001, 17:15
 */

package org.ntropa.build;

import org.ntropa.build.channel.FileLocation;
import org.ntropa.build.channel.FileLocationException;

/**
 * Instances of this class are used to send messages to a JSPBuilder
 * @author  jdb
 * @version $Id: FileListenerEvent.java,v 1.3 2002/06/20 19:22:29 jdb Exp $
 */
public class FileListenerEvent {
    
    /**
     * A <code>FileLocation</code> object requires a <code>ContextPath</code> object
     * in order to be resolved to an actual file. This class assumes the receiptent of the
     * event knows the channel the event applies to.
     */
    protected FileLocation m_file;
    
    /** Creates new FileListenerEvent */
    public FileListenerEvent ( FileLocation file) throws FileLocationException {
        m_file = new FileLocation ( file ) ;
    }
    
    public String getLocation () {
        return m_file.getLocation () ;
    }
    
    public String toString () {
        return "FileListenerEvent: " + m_file.getLocation () ;
    }
}
