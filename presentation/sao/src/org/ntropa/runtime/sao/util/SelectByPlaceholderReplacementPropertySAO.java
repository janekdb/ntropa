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
 * SelectByPlaceholderPropertySAO.java
 *
 * Created on 13 May 2002, 15:25
 */

package org.ntropa.runtime.sao.util;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;
import org.ntropa.utility.StringUtilities;


/**
 * The base class for server active objects which selectively render one
 * of one or more sub-elements based on a property of the replacement value
 * for the placeholder code.
 * <p>
 * Examples of possible sub-element names and meanings are<br>
 * empty: returned if the placeholder replacement value is null or zero-length.<br>
 * not-empty: replacement if the placeholder replacement value is non null and non zero-length.<br>
 *
 * @author  jdb
 * @version $Id: SelectByPlaceholderReplacementPropertySAO.java,v 1.1 2004/10/08 15:08:12 jdb Exp $
 */
abstract public class SelectByPlaceholderReplacementPropertySAO extends BaseServerActiveObject {
    
    private String placeholderCode ;
    
    /**
     * Set the name of the placeholder code to inspect the value of
     * to determine the <code>Element</code> to render.
     * <p>
     * The placeholder code is converted to lowercase to match the transformation
     * in org.ntropa.build.html.MarkUpAttributes.
     */
    public void setPlaceholderCode ( String placeholderCode ) {
        StringUtilities.validateNonZeroLength ( placeholderCode, "placeholderCode" ) ;
        this.placeholderCode = placeholderCode.toLowerCase () ;
    }
    
    /**
     * Invoked inside the JSP to render content into the page buffer. All control
     * logic has already been executed by the time this method is invoked. The method
     * is responsible for rendering the view (HTML) from the model (session data) and
     * nothing more. In particular no http redirection should be attempted during
     * the rendering phase.
     *
     * @throws Exception, otherwise it would have to throw every exception that every
     * method of every object could throw.
     *
     * @param icb A <code>InvocationContext</code> supplied by the JSP.
     */
    public void render ( InvocationContext icb ) throws Exception {
        
        /* This provides an early warning if the placeholderCode was never set */
        StringUtilities.validateNonZeroLength ( placeholderCode, "placeholderCode" ) ;
        
        String rep = getPlaceholderReplacementRecursively ( placeholderCode ) ;
        String elementName = getElementNameForPlaceholderReplacementValue ( rep ) ;
        if ( elementName != null )
            getChild ( elementName ).render ( icb ) ;
    }
    
    /**
     * Return the name of an <code>Element</code> to render based on the value of
     * the placeholder replacement.
     * <p>
     * Subclasses should implement this method to provide specialised behaviour.
     *
     * @param rep The value of the placeholder to calculate the returned element name from
     * @return A <code>String</code> which is the name of an sub-element to render. By
     * convention the method should return &quot;default&quot; if no other return value
     * is appropriate. A null return value is honoured, which results in no element being
     * rendered.
     */
    abstract public String getElementNameForPlaceholderReplacementValue ( String rep ) ;
}
