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
 * PlaceholderAttributes.java
 *
 * Created on 05 September 2002, 12:49
 */

package org.ntropa.build.html;

import org.ntropa.build.Constants;

/**
 *
 * @author  jdb
 * @version $Id: PlaceholderAttributes.java,v 1.1 2002/09/05 17:19:01 jdb Exp $
 */
public class PlaceholderAttributes extends MarkUpAttributes {
    
    /** Creates new PlaceholderAttributes */
    public PlaceholderAttributes ( MarkUpAttributes m ) {
        
        putAll ( m ) ;
        
        //if ( getTemplate () == null )
        //    throw new IllegalArgumentException (
        //    "Attempt to construct a PlaceholderAttributes from a MarkUpAttributes without a 'use-element' entry" ) ;
        
        String useElement = getUseElement () ;
        
        if ( useElement.length () < 1 )
            throw new IllegalArgumentException (
            "Attempt to construct a PlaceholderAttributes from a MarkUpAttributes with a zero-length 'use-element' entry" ) ;
        
        /* = / */
        if ( useElement.startsWith ( "/" ) && ( useElement.length () < 2 ) )
            throw new IllegalArgumentException (
            "Attempt to construct a closing PlaceholderAttributes from a MarkUpAttributes with a one-length 'use-element' entry: " + useElement ) ;
    }
    
    /* Prevent no-arg construction */
    private PlaceholderAttributes () {} ;
    
    //public String getName () {
    //    return "-placeholder" ;
    //}
    
    public String getUseElement () {
        return getAttribute ( Constants.MarkUp.USE_ELEMENT_ATTRIBUTE ) ;
    }

    public boolean isOpening () {
        return ! getAttribute ( Constants.MarkUp.USE_ELEMENT_ATTRIBUTE ).startsWith ( "/" ) ;
    }
    
}
