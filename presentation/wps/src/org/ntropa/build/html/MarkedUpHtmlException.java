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
 * MarkedUpHtmlException.java
 *
 * Created on 08 October 2001, 22:41
 */

package org.ntropa.build.html;

/**
 *
 * @author  jdb
 * @version $Id: MarkedUpHtmlException.java,v 1.1 2001/10/09 09:42:38 jdb Exp $
 */
public class MarkedUpHtmlException extends Exception {

    /**
     * Creates new <code>MarkedUpHtmlException</code> without detail message.
     */
    public MarkedUpHtmlException () {
    }


    /**
     * Constructs an <code>MarkedUpHtmlException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MarkedUpHtmlException (String msg) {
        super(msg);
    }
}


