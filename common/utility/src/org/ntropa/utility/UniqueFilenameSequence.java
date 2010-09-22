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
 * UniqueFilenameSequence.java
 *
 * Created on September 13, 2001, 12:48 PM
 */

package org.ntropa.utility;

import java.util.Random;

/**
 *
 * @author  jdb
 * @version $Id: UniqueFilenameSequence.java,v 1.1 2001/09/13 12:51:42 jdb Exp $
 */
public class UniqueFilenameSequence {
    
    /**
     * Creates new UniqueFilenameSequence
     *
     * Filesystem manipulation methods suitable for creating and changing
     * filesystems for use as test fixtures.
     */
    Random m_r ;
    
    int m_prefix ;
    
    public UniqueFilenameSequence () {
        m_r = new Random ( 0 ) ;
        m_prefix = 1 ;
    }
    
    public String next () {
        String s = "" + m_prefix++ + "-" + m_r.nextInt (1000*1000) ;
        return s ;
    }
    
}
