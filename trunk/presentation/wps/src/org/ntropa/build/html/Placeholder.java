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
 * Placeholder.java
 *
 * Created on 4 September, 17:20
 */

package org.ntropa.build.html;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is used as a placeholder when parsing templates.
 *
 * @author  jdb
 * @version $Id: Placeholder.java,v 1.6 2002/09/06 17:15:02 jdb Exp $
 */
public class Placeholder implements Deserializable {
    
    private final String useElementName ;
    
    public Placeholder( String useElementName ) {
        
        if ( useElementName == null )
            throw new IllegalArgumentException( "Attempt to construct Placeholder with a null 'useElementName' argument" ) ;
        if ( useElementName.equals( "" ) )
            throw new IllegalArgumentException( "Attempt to construct Placeholder with an empty 'useElementName' argument" ) ;
        
        this.useElementName = useElementName.toLowerCase() ;
    }
    
    
    /**
     * @return Returns the name of the <code>Element</code> this
     * Placeholder should be replaced with
     */
    public String getUseElement() {
        return useElementName ;
    }
    
    
    /**
     * The name of the replacement ServerActiveHtml
     */
    private static final String REPLACEMENT_NAME = "-replacement" ;
    
    
    /**
     * Search the tree of objects starting at the given <code>ServerActiveHtml</code>
     * and return the content of the first <code>Element</code> with the same name as this <code>Placeholder</code>
     * as the children of a new <code>List</code> if the <code>Element</code> does not have a any placeholder (of the
     * form placeholder-x = "foo").
     * <p>
     * Otherise return a singleton list of a <code>ServerActiveHtml</code> which has the children of the found <code>Element</code>
     * and the placeholders of this <code>Element</code>
     *
     * <p>
     * Packaging the content of the matched <code>Element</code> as a <code>ServerActiveHtml</code> allows the
     * placeholders of the <code>Element</code> to be transferred to the <code>ServerActiveHtml</code>
     * <p>
     * If no matching <code>Element</code> return a <code>List</code> with a
     * <code>Fragment</code> warning message
     *
     * @return A <code>ServerActiveHtml</code> containing the child of the matched <code>Element</code>
     * or a warning <code>Fragment</code> if no matching <code>Element</code>
     */
    public List getReplacement( ServerActiveHtml sah ) throws ServerActiveHtmlException {
        
        List result = new LinkedList() ;
        
        Element match = sah.findElement( getUseElement() ) ;
        
        if ( match == null ) {
            result.add( new Fragment( "<b>Warning: missing element for placeholder '" + getUseElement() +"'</b>" ) ) ;
            return result ;
        }
        
        if ( match.getPlaceholders().isEmpty() ) {
            for ( Iterator children = match.getChildren().iterator() ; children.hasNext() ; ) {
                Deserializable child = ( Deserializable ) children.next() ;
                result.add( child ) ;
            }
        }
        else {
            ServerActiveHtml wrapper = new ServerActiveHtml( REPLACEMENT_NAME ) ;
            wrapper.setPlaceholders( match.getPlaceholders() ) ;
            for ( Iterator children = match.getChildren().iterator() ; children.hasNext() ; ) {
                Deserializable child = ( Deserializable ) children.next() ;
                wrapper.add( child ) ;
            }
            result.add( wrapper ) ;
        }
        
        return result ;
    }
    
    
    public int hashCode() {
        return getUseElement().hashCode() ;
    }
    
    
    /**
     * Return true if the objects are the same class and
     * have the same content.
     */
    public boolean equals( Object obj ) {
        
        if ( obj == null )
            return false ;
        
        if ( ! ( obj instanceof Placeholder ) )
            return false ;
        
        Placeholder p = (Placeholder) obj ;
        
        return p.getUseElement().equals( getUseElement() ) ;
        
    }
    
    
    /**
     * Provide object description via reflection
     */
    public String toString() {
        return "[Placeholder]: useElementName: " + useElementName ;
    }
    
    
    /* --- Implementation of Deserializable --- */
    
    /**
     * A method to add a <code>Placeholder</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * An implementation should throw <code>UnsupportedOperationException</code>
     * if <code>Placeholders</code> can not be added.
     *
     * @param placeholder The <code>Placeholder</code> to add.
     */
    public void add(Placeholder placeholder) {
        throw new UnsupportedOperationException() ;
    }
    
    /**
     * A method to add an <code>Element</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * An implementation should throw <code>UnsupportedOperationException</code>
     * if <code>Elements</code> can not be added.
     *
     * @param element The <code>Element</code> to add.
     */
    public void add(Element element) {
        throw new UnsupportedOperationException() ;
    }
    
    /**
     * A method to add a <code>Fragment</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * An implementation should throw <code>UnsupportedOperationException</code>
     * if <code>Fragments</code> can not be added.
     *
     * @param fragment The <code>Fragment</code> to add.
     */
    public void add(Fragment fragment) {
        /* The designer may have added some placeholder text in the use-element section */
        ;
    }
    
    /**
     * A method to add a <code>ServerActiveHtml</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * An implementation should throw <code>UnsupportedOperationException</code>
     * if <code>ServerActiveHtmls</code> can not be added.
     *
     * @param serverActiveHtml The <code>ServerActiveHtml</code> to add.
     */
    public void add(ServerActiveHtml serverActiveHtml) {
        throw new UnsupportedOperationException() ;
    }
    
}
