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
 * Queue.java
 *
 * Created on 08 November 2001, 15:17
 */

package org.ntropa.utility;

import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * A simple FIFO. java.util seems to be missing this data structure.
 *
 * To add an object at the end of the queue invoke <code>add ( Object )</code>.
 * To remove an object from the start of the queue invoke <code>remove ()</code>.
 *
 * No attempt has been made to make this efficient. This class is not thread-safe.
 *
 * @author  jdb
 * @version $Id: Queue.java,v 1.1 2001/11/08 18:16:05 jdb Exp $
 */
public class Queue extends Vector {
    
    
    public Object remove () throws NoSuchElementException {
        
        if ( size () == 0 )
            throw new NoSuchElementException () ;
        
        Object o = elementAt ( 0 );
        removeElementAt ( 0 );
        return o ;
        
    }
    
}
