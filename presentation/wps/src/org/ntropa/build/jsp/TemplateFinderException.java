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
 * TemplateFinderException.java
 *
 * Created on 20 November 2001, 21:12
 */

package org.ntropa.build.jsp;

/**
 *
 * @author  jdb
 * @version $Id: TemplateFinderException.java,v 1.1 2001/11/20 22:19:03 jdb Exp $
 */
public class TemplateFinderException extends Exception {

    /**
     * Creates new <code>TemplateFinderException</code> without detail message.
     */
    public TemplateFinderException () {
    }


    /**
     * Constructs an <code>TemplateFinderException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TemplateFinderException (String msg) {
        super(msg);
    }
}


