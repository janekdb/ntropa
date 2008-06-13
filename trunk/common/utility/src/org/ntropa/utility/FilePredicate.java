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
 * FilePredicate.java
 *
 * Created on 17 October 2001, 15:51
 */

package org.ntropa.utility;

import java.io.File;

/**
 * An interface which sets a contract for a file path to be tested against some criteria.
 *
 *
 * @author  jdb
 * @version $Id: FilePredicate.java,v 1.3 2001/10/25 20:14:29 jdb Exp $
 */
public abstract interface FilePredicate {
    
    
    /**
     * Test a file path to see if it matches our criteria
     *
     * @param file A <File> object representing the file path
     * we want to test. THe file does not need to exist.
     */
    public boolean accept ( File f ) ;
    
    public boolean accept ( String s ) ;
    
}

