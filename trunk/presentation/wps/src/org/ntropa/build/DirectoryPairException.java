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
 * DirectoryPairException.java
 *
 * Created on July 30, 2001, 3:34 PM
 */

package org.ntropa.build;

/**
 * This class is a general purpose exception for DirectoryPair
 * @see org.ntropa.build.DirectoryPair
 * @author  jdb
 * @version $Id: DirectoryPairException.java,v 1.1 2001/07/30 17:50:51 jdb Exp $
 */
public class DirectoryPairException extends Exception {
    
    public DirectoryPairException () {
        super () ;
    }
    
    /**
     * Creates new DirectoryPairException
     * @param message the additional information to add to the exception.
     */
    public DirectoryPairException ( String message ) {
        super ( message ) ;
    }
    
}
