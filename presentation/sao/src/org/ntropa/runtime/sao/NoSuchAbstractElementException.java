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
 * NoSuchAbstractElementException.java
 *
 * Created on 11 March 2002, 11:18
 */

package org.ntropa.runtime.sao;

/**
 *
 * @author  jdb
 * @version $Id: NoSuchAbstractElementException.java,v 1.1 2002/03/11 12:14:42 jdb Exp $
 */
public class NoSuchAbstractElementException extends RuntimeException {

    /**
     * Creates new <code>NoSuchAbstractElementException</code> without detail message.
     */
    public NoSuchAbstractElementException () {
    }


    /**
     * Constructs an <code>NoSuchAbstractElementException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchAbstractElementException (String msg) {
        super(msg);
    }
}


