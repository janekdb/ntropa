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
 * TemplateException.java
 *
 * Created on 19 October 2001, 16:06
 */

package org.ntropa.build.jsp;

/**
 *
 * @author  jdb
 * @version $Id: TemplateException.java,v 1.1 2001/10/19 17:00:45 jdb Exp $
 */
public class TemplateException extends Exception {

    /**
     * Creates new <code>TemplateException</code> without detail message.
     */
    public TemplateException() {
    }


    /**
     * Constructs an <code>TemplateException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TemplateException(String msg) {
        super(msg);
    }
}


