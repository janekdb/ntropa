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
 * DirectoryMonitorException.java
 *
 * Created on July 30, 2001, 3:34 PM
 */

package org.ntropa.build;

/**
 * This class is a general purpose exception for DirectoryMonitor
 * @see org.ntropa.build.DirectoryMonitor
 * @author  jdb
 * @version $Id: DirectoryMonitorException.java,v 1.1 2001/09/07 02:40:07 jdb Exp $
 */
public class DirectoryMonitorException extends Exception {
    
    public DirectoryMonitorException () {
        super () ;
    }
    
    /**
     * Creates new DirectoryMonitorException
     * @param message the additional information to add to the exception.
     */
    public DirectoryMonitorException ( String message ) {
        super ( message ) ;
    }
    
}
