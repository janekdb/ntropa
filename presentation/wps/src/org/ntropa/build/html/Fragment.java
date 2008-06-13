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
 * Fragment.java
 *
 * Created on 11 October 2001, 16:39
 */

package org.ntropa.build.html;

import java.util.Collections;
import java.util.List;

import org.ntropa.build.jsp.FinderSet;
import org.ntropa.build.jsp.JspSerializable;
import org.ntropa.build.jsp.JspUtility;


/**
 * This class is a representation of a fragment of HTML.
 * It is essential the same as a String and provides improved
 * type safety.
 *
 * @author  jdb
 * @version $Id: Fragment.java,v 1.15 2002/09/05 17:13:41 jdb Exp $
 */
public class Fragment
implements Deserializable, JspSerializable {
    
    private String _value ;
    
    // TODO: Revert to ntropa class names.
    //private static final String DEFAULT_COMPONENT_CLASS = "org.ntropa.runtime.sao.StandardFragment" ;
    private static final String DEFAULT_COMPONENT_CLASS = "com.studylink.sao.StandardFragment" ;
    
    // TODO: Revert to ntropa class names.
    //private static final String DEFAULT_COMPONENT_TYPE = "org.ntropa.runtime.sao.Fragment" ;
    private static final String DEFAULT_COMPONENT_TYPE = "com.studylink.sao.Fragment" ;
    
    /** Creates new Fragment */
    public Fragment () {
        _value = "" ;
    }
    
    /** Creates new <code>Fragment</code> from a <code>String</code>*/
    public Fragment ( String s ) {
        if ( s == null )
            throw new IllegalArgumentException ( "Attempt to construct Fragment with a null String" ) ;
        _value = s;
    }
    
    /** Creates new <code>Fragment</code> from a <code>Fragment</code>*/
    public Fragment ( Fragment f ) {
        _value = f.getValue () ;
    }
    
    public String getValue () {
        return _value ;
    }
    
    
    public String toString () {
        return getValue ()  ;
    }
    
    
    public int hashCode () {
        return getValue ().hashCode () ;
    }
    
    
    /**
     * Return true if the objects are the same class and
     * have the same content.
     */
    public boolean equals ( Object obj ) {
        
        if ( obj == null )
            return false ;
        
        if ( ! ( obj instanceof Fragment ) )
            return false ;
        
        Fragment f = (Fragment) obj ;
        
        return f.getValue ().equals ( getValue () ) ;
        
    }
    
    
    /* ---- Implementation of JspSerializable --- */
    
    /**
     * A <code>Fragment</code> is a basic atom and cannot
     * contain other objects.
     */
    public List getChildren () {
        return Collections.EMPTY_LIST ;
    }
    
    /**
     * Return the full name of the class implementing the
     * interface within the current page context and dependent on the type
     * of the underlying class.
     *
     * @param finderSet A <code>FinderSet</code> used to look up information based
     * on a certain context. The contect is expected to be the current page location.
     *
     * @return A <code>String</code> representing the full name of the class
     * implementing the interface.
     */
    /*
     * 02-1-18 jdb
    public String getComponentClassName (FinderSet finderSet) {
        if ( finderSet == null )
            throw new IllegalArgumentException (
            "Attempt to invoke Fragment.getComponentClassName with null finderSet" ) ;
     
        return DEFAULT_COMPONENT_CLASS ;
    }
     */
    
    public String getComponentTypeName () {
        return DEFAULT_COMPONENT_TYPE ;
    }
    
    /**
     * Create the code to set up this object. See interface JspSerializable for details.
     *
     * @param objName The name of the object to create and initialise.
     *
     * @param buffer A <code>StringBuStringBuilderffer</code> to append set up code to
     *
     * @param finderSet A <code>FinderSet</code> used to look up information based
     * on a certain context. The context is expected to be the current page location.
     */
    public void getSetUpCode ( String objName, StringBuilder buffer, FinderSet finderSet ) {
        JspUtility.getObjectCreationCode ( objName, DEFAULT_COMPONENT_CLASS, buffer ) ;
        buffer.append ( objName + ".setHtml ( " +
        JspUtility.makeSafeQuoted ( getValue () ) +
        " ) ;\n" ) ;
    }
    
    /* --- Implementation of Deserializable --- */
    
    /**
     * A method to append a <code>Fragment</code> to an object,
     * typically used in the deserialization of that object.
     */
    public void add (Fragment fragment) {
        
        if ( fragment == null )
            return ;
        
        _value += fragment.getValue () ;
    }
    
    /**
     * A method to add an <code>Element</code> object to a deserializable object.
     *
     */
    public void add (Element child) {
        throw new UnsupportedOperationException () ;
    }
    
    /**
     * A method to add a <code>ServerActiveHtml</code> object to a deserializable object.
     *
     */
    public void add (ServerActiveHtml child) {
        throw new UnsupportedOperationException () ;
    }
    
    /**
     * A method to add a <code>Placeholder</code> object to a deserializable object.
     *
     */
    public void add (Placeholder child) {
        throw new UnsupportedOperationException () ;
    }
    
}
