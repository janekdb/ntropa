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
 * ContextPathException.java
 *
 * Created on 20 June 2002, 16:21
 */

package org.ntropa.build;

/**
 *
 * @author  jdb
 * @version $Id: ContextPathException.java,v 1.1 2002/06/20 19:20:46 jdb Exp $
 */
public class ContextPathException extends RuntimeException {

    /**
     * Creates new <code>ContextPathException</code> without detail message.
     */
    public ContextPathException () {
    }


    /**
     * Constructs an <code>ContextPathException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ContextPathException (String msg) {
        super(msg);
    }
}


