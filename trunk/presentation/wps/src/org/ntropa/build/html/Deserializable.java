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
 * Deserializable.java
 *
 * Created on 12 November 2001, 11:35
 */

package org.ntropa.build.html;

/**
 * An interface which provides the contract for objects which can
 * be deserialized from a Fragment.
 * 
 * @author  jdb
 * @version $Id: Deserializable.java,v 1.2 2002/09/05 17:13:41 jdb Exp $
 */
public interface Deserializable {

    /**
     * A method to add a <code>Fragment</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * An implementation should throw <code>UnsupportedOperationException</code>
     * if <code>Fragments</code> can not be added.
     *
     * @param fragment The <code>Fragment</code> to add.
     */
    public void add ( Fragment fragment );
    
    
    /**
     * A method to add a <code>ServerActiveHtml</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * An implementation should throw <code>UnsupportedOperationException</code>
     * if <code>ServerActiveHtmls</code> can not be added.
     *
     * @param serverActiveHtml The <code>ServerActiveHtml</code> to add.
     */
    public void add ( ServerActiveHtml serverActiveHtml ) ;
    
    
    /**
     * A method to add an <code>Element</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * An implementation should throw <code>UnsupportedOperationException</code>
     * if <code>Elements</code> can not be added.
     *
     * @param element The <code>Element</code> to add.
     */
    public void add ( Element element ) ;
 
    
    /**
     * A method to add a <code>Placeholder</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * An implementation should throw <code>UnsupportedOperationException</code>
     * if <code>Placeholders</code> can not be added.
     *
     * @param placeholder The <code>Placeholder</code> to add.
     */
    public void add ( Placeholder placeholder ) ;
 
}

