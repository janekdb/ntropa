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
 * GenericDeserializable.java
 *
 * Created on 14 November 2001, 16:18
 */

package org.ntropa.build.html;

import java.util.Iterator;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ntropa.build.jsp.FinderSet;
import org.ntropa.build.jsp.JspUtility;


/**
 * A class to be as a superclass for objects deserialized from a marked up HTML page.
 *
 * Deals with placeholders which are anticipated to be common to all deserialized objects.
 *
 * @author  jdb
 * @version $Id: GenericDeserializable.java,v 1.7 2002/06/07 13:33:52 jdb Exp $
 */
public abstract class GenericDeserializable {
    
    /*
     * Always creating the object simplifies the code at the expense of
     * a potentially unused object.
     */
    private Properties _placeholders = new Properties () ;
    
    /**
     * Set a placeholder
     *
     * @param name The name of the placeholder to set
     * @param value The value to set the placeholder to
     */
    public void setPlaceholder ( String name, String value ) {
        _placeholders.setProperty ( name.toLowerCase (), value ) ;
    }
    
    /**
     * Set all placeholders from a <code>Properties</code> object.
     * All keys are converted to lower case.
     *
     * @param name The <code>Properties</code> object to get the new placeholders from
     */
    public void setPlaceholders ( Properties placeholders ) {
        _placeholders = new Properties () ;
        
        _placeholders.putAll ( placeholders ) ;
        /*
        Iterator it = placeholders.keySet().iterator() ;
        while ( it.hasNext() ) {
            String name = (String) it.next() ;
            _placeholders.setProperty( name.toLowerCase(), placeholders.getProperty( name ) ) ;
        }
         */
        
        //System.out.println("[setPlaceholders] arg: " + placeholders );
        //System.out.println("[setPlaceholders] member: " + _placeholders );
    }
    
    /**
     * Return a list of placeholders
     *
     * @return a copy of the placeholders as a <code>Properties</code> object.
     */
    public Properties getPlaceholders () {
        Properties p = new Properties () ;
        p.putAll ( _placeholders ) ;
        /*
        Iterator it = _placeholders.keySet().iterator() ;
        while ( it.hasNext() ) {
            String name = (String) it.next() ;
            p.setProperty( name, _placeholders.getProperty( name ) ) ;
        }
         */
        return p ;
    }
    
    /*
     * Return the code to set up the placeholders for this object.
     *
     * A Component which implements the JSP functionality
     * of a Fragment needs to have its HTML available at http-serve time.
     * So the JspSerializable based on an underlying Fragment writes out
     * something like this:
     *
     * AbstractElement component_33 = new org.ntropa.runtime.sao.StandardElement () ;
     *
     * component_33.setName ( "news-item" ) ;
     * component_33.setPlaceholder ( "link", "\"Hidi Fliss\"" ) ;
     *
     * @param objName The name of the object to initialise.
     *
     * @param buffer A <code>StringBuilder</code> to append set up code to
     *
     * @param finderSet A <code>FinderSet</code> used to look up information based
     * on a certain context. The contect is expected to be the current page location.
     */
    public void getSetUpCode ( String objName, StringBuilder buffer, FinderSet finderSet ) {
        /* Order the properties for unit testing */
        SortedSet keys = new TreeSet ( _placeholders.keySet () ) ;
        Iterator it = keys.iterator () ; //_placeholders.keySet ().iterator () ;
        while ( it.hasNext () ) {
            String name = (String) it.next () ;
            String value = JspUtility.makeSafeQuoted ( _placeholders.getProperty ( name ) ) ;
            buffer.append ( objName  +".setPlaceholder ( \"" + name + "\", " + value + " ) ;\n" ) ;
        }
    }
    

}
