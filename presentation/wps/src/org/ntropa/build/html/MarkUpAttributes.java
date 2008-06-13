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
 * MarkUpAttributes.java
 *
 * Created on 07 November 2001, 23:29
 */

package org.ntropa.build.html;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.ntropa.build.Constants;


/**
 * Represent a web publishing system markup comment.
 *
 * Some examples of such tags are:
 *
 * <!-- name = "result-list" -->
 * <!-- name = "header" template = "header" -->
 * <!-- name = "header" placeholder-date = "2001-Nov-7" -->
 * <!-- element = "item" placeholder-link = "link goes here" placeholder-time = "17:55 hrs" -->
 *
 * @author  jdb
 * @version $Id: MarkUpAttributes.java,v 1.6 2002/09/05 17:13:41 jdb Exp $
 */
public class MarkUpAttributes extends java.util.Properties {
    
    
    /**
     * Copy construtor.
     */
    public MarkUpAttributes ( MarkUpAttributes m ) {
       /*
        * Umm, this didn't construct the object correctly, I don't know why.
        * Maybe it just copies a reference to the Map. Suggestions please.
        */
        //super ( m ) ; /* HashMap ( Map t ) */
        
        putAll ( m ) ;
        /*
        Iterator it = m.keySet ().iterator () ;
        while ( it.hasNext () ) {
            String key = (String) it.next () ;
            setAttribute ( key, m.getAttribute ( key ) ) ;
        }
        */
    }
    
    
    /**
     * Need this since we added a construtor.
     */
    public MarkUpAttributes () {} ;
    
    /**
     * Set the value of an attribute.
     *
     * The name is used case-insensitive.
     *
     * @param name The name of the attribute to set
     * @param value The value to set the attribute to
     * @see java.util.Properties
     */
    public void setAttribute (String name, String value) {
        
        setProperty ( name.toLowerCase (), value ) ;
    }
    
    
    /**
     * Get the value of an attribute.
     *
     * The name is used case-insensitive.
     *
     * @param name The name of the attribute to get
     * @see java.util.Properties
     */
    public String getAttribute (String name) {
        
        return getProperty ( name.toLowerCase () ) ;
    }
    
    
    /**
     * Remove an attribute.
     *
     * The name is used case-insensitive.
     *
     * @param name The name of the attribute to remove
     * @see java.util.Properties
     */
    public void removeAttribute (String name) {
        
        remove ( ( Object ) name.toLowerCase () ) ;
    }
    

    /**
     * Return a html representaion of the attributes
     *
     * Example:
     * <!-- a="b" c="d2" -->
     */
    public String toHtml () {
        
        Set e = entrySet () ;
        /* Reduce number of resizes; The default initial capacity is 16 characters */
        StringBuffer sb = new StringBuffer ( 40 * e.size () ) ;
        sb.append ( "<!-- " ) ;
        
        Iterator it = e.iterator () ;
        while ( it.hasNext () ) {
            Entry entry = (Entry) it.next () ;
            sb.append ( (String)entry.getKey () ).append ( "=\"" )
            .append ( (String)entry.getValue () ).append ( "\" " );
        }
        
        sb.append ( "-->" ) ;
        return sb.toString () ;
    }
    
    /**
     * Return a map of placeholder names and the values to
     * replace in with the value of the name which is known at serve time.
     *
     * For example:
     *
     * placeholder-date = "01-November-12'
     * placeholder-user = "LOGON NAME"
     *
     * results in this map:
     *
     * "date":        "01-November-12"
     * "user":        "LOGON NAME"
     *
     * @return <code>Properties</code> a map of placeholder names and placeholder
     * values.
     */
    public Properties getPlaceholders () {
        
        Properties p = new Properties () ;
        Iterator it  = keySet ().iterator () ;
        while ( it.hasNext () ) {
            String s = (String) it.next () ;
            if ( ! s.startsWith ( Constants.MarkUp.PLACEHOLDER_PREFIX ) )
                continue ;
            
            String placeholderName = s.substring ( Constants.MarkUp.PLACEHOLDER_PREFIX.length () ) ;
            if ( placeholderName.length () > 0 )
                p.setProperty ( placeholderName, getAttribute ( s ) ) ;
            
        }
        return p ;
    }
    
    /**
     * Returns a new MarkUpAttributes with all values in lower case.
     * Note: the keys will be lower case due to the conversion in setAttribute ( String ).
     *
     * @return A new <code>MarkUpAttributes</code> with all values in lower case.
     */
    public MarkUpAttributes toLowerCase () {
        
        MarkUpAttributes m = new MarkUpAttributes () ;
        
        Iterator it = keySet ().iterator () ;
        while ( it.hasNext () ) {
            String key = (String) it.next () ;
            m.setAttribute ( key, getAttribute ( key ).toLowerCase () ) ;
        }
        
        return m ;
    }
    

    /**
     * Return true for any of these forms:
     *
     * <!-- name = "x" -->
     * <!-- name = "x" template = "y" -->
     * <!-- use-template = "y" -->
     *
     * @return true if the map of markup attributes is server active HTML
     */
    public boolean isServerActiveHtml () {
        if ( getAttribute ( Constants.MarkUp.SERVER_ACTIVE_HTML_ATTRIBUTE ) != null )
            return true ;
        
        if ( getAttribute ( Constants.MarkUp.USE_TEMPLATE_ATTRIBUTE ) != null )
            return true ;
        
        return false ;
    }
    
    
    /**
     * Return true for this form:
     *
     * <!-- element = "x" -->
     *
     * @return true if the map of markup attributes is an element
     */
     public boolean isElement () {
        if ( getAttribute ( Constants.MarkUp.ELEMENT_ATTRIBUTE ) != null )
            return true ;
   
        return false ;
     }
     
     
    /**
     * Return true for this form:
     *
     * <!-- use-element = "x" -->
     *
     * @return true if the map of markup attributes is a placeholder
     */
     public boolean isPlaceholder () {
        if ( getAttribute ( Constants.MarkUp.USE_ELEMENT_ATTRIBUTE ) != null )
            return true ;
   
        return false ;
     }

    /**
     * Change the map so ServerActiveHtml does not have to change to accomodate
     * the newer, simpler syntax.
     *
     *      name        ->  name
     *
     *      name        ->  name
     *      template    ->  use-template
     *
     *      use-template = "x"  ->  name = "-use-template"
     *                              use-template = "x"
     *
     *
     *      use-template = "/x" ->  name = "/-use-template"
     *
     * When the earlier name, template form is removed from production html docs remove
     * this method and update ServerActiveHtmlAttributes to understand the next form.
     *
     */
    public void convertUseTemplateFudge () throws MarkedUpHtmlException {
        
        if ( getAttribute ( Constants.MarkUp.SERVER_ACTIVE_HTML_ATTRIBUTE ) != null ) {
            /* effectively rename template -> use-template */
            String template = getAttribute ( "template" ) ;
            if ( template != null ) {
                removeAttribute ( "template" ) ;
                setAttribute ( Constants.MarkUp.USE_TEMPLATE_ATTRIBUTE, template ) ;
            }
            return ;
        }
        
        String template = getAttribute ( Constants.MarkUp.USE_TEMPLATE_ATTRIBUTE ) ;
        
        /* should be impossible due to earlier validation */
        if ( template == null )
            throw new RuntimeException ( "USE_TEMPLATE_ATTRIBUTE missing" ) ;
        
        if ( template.length () == 0 )
            throw new MarkedUpHtmlException ( "value of template was the empty string" ) ;
        
        if ( template.startsWith ( "/" ) ) {
            setAttribute ( Constants.MarkUp.SERVER_ACTIVE_HTML_ATTRIBUTE, "/-use-template" ) ;
            removeAttribute ( Constants.MarkUp.USE_TEMPLATE_ATTRIBUTE ) ;
        }
        else
            setAttribute ( Constants.MarkUp.SERVER_ACTIVE_HTML_ATTRIBUTE, "-use-template" ) ;
    }

}
