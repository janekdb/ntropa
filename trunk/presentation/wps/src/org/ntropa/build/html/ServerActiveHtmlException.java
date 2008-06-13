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
 * ServerActiveHtmlException.java
 *
 * Created on 15 October 2001, 16:46
 */

package org.ntropa.build.html;

/**
 *
 * @author  jdb
 * @version $Id: ServerActiveHtmlException.java,v 1.1 2001/10/17 10:23:15 jdb Exp $
 */
public class ServerActiveHtmlException extends MarkedUpHtmlException {

    /**
     * Creates new <code>ServerActiveHtmlException</code> without detail message.
     */
    public ServerActiveHtmlException () {
    }


    /**
     * Constructs an <code>ServerActiveHtmlException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ServerActiveHtmlException (String msg) {
        super(msg);
    }
}


