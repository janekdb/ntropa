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
 * ElementAttributes.java
 *
 * Created on 13 November 2001, 11:34
 */

package org.ntropa.build.html;

import org.ntropa.build.Constants;


/**
 *
 * @author  jdb
 * @version $Id: ElementAttributes.java,v 1.4 2002/09/05 17:13:41 jdb Exp $
 */
public class ElementAttributes extends MarkUpAttributes {
    
    /** Creates new ElementAttributes */
    public ElementAttributes ( MarkUpAttributes m ) {
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
        
        if ( getName () == null )
            throw new IllegalArgumentException (
            "Attempt to construct a ElementAttributes from a MarkUpAttributes without a 'element' entry" ) ;
        
        String name = getName () ;
        
        if ( name.length () < 1 )
            throw new IllegalArgumentException (
            "Attempt to construct a ElementAttributes from a MarkUpAttributes with a zero-length 'element' entry" ) ;
        
        if ( name.startsWith ( "/" ) && ( name.length () < 2 ) )
            throw new IllegalArgumentException (
            "Attempt to construct a closing ElementAttributes from a MarkUpAttributes with a one-length 'element' entry: " + name ) ;
        
    }
    
    /* Prevent no-arg construction */
    private ElementAttributes () {} ;
    
    public String getName () {
        return getAttribute ( Constants.MarkUp.ELEMENT_ATTRIBUTE ) ;
    }
    
    public boolean isOpening () {
        return ! getAttribute ( Constants.MarkUp.ELEMENT_ATTRIBUTE ).startsWith ( "/" ) ;
    }
    
}
