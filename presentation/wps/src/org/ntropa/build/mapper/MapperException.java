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
 * MapperException.java
 *
 * Created on September 11, 2001, 4:44 PM
 */

package org.ntropa.build.mapper;

/**
 *
 * @author  jdb
 * @version $Id: MapperException.java,v 1.1 2001/09/11 16:27:51 jdb Exp $
 */
public class MapperException extends java.lang.Exception {

    /**
     * Creates new <code>MapperException</code> without detail message.
     */
    public MapperException () {
    }


    /**
     * Constructs an <code>MapperException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MapperException (String msg) {
        super(msg);
    }
}


