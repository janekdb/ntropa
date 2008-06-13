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
 * SelectByRequestAttributeSAO.java
 *
 * Created on 12 March 2002, 12:40
 */

package org.ntropa.runtime.sao.util;

//import javax.servlet.ServletContext ;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//import javax.servlet.jsp.JspWriter ;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;

/**
 * This server active object selects a child element to render
 * based on the value of a request attribute. The value is used
 * directly. If the element does not exist an exception is thrown
 * which notifies the web developer via the served HTML page.
 * <p>
 * To use this sao markup the HTML with a list of elements
 * <p>
 * <pre>
 *      &lt;-- name = &quot;my-selector&gt; --&gt;
 *      &lt;-- element = &quot;on&gt; --&gt;
 *          the qwon feature is available
 *      &lt;-- element = &quot;/on&gt; --&gt;
 *      &lt;-- element = &quot;offline&gt; --&gt;
 *          &lt;font color=red&gt;Warning! The qwon feature is unavailable&lt;/font&gt;
 *      &lt;-- element = &quot;/offline&gt; --&gt;
 *      &lt;-- name = &quot;/my-selector&gt; --&gt;
 * </pre>
 * <p>
 * Set the name of the request attribute to inspect in the application.properties
 * file
 * <p>
 * <pre>
 * sao.my-selector.class-name = org.ntropa.runtime.sao.util.SelectByRequestAttributeSAO
 * sao.my-selector.prop.attributeName = provider-halalFood
 * </pre>
 * <p>
 * In the SAO that has an instance of this class as a child set up the request attribute
 * <p>
 * <pre>
 *  request.setAttribute ( "provider-halalFood", okay () ? "on" : "offline" ) ;
 * </pre>
 * <p>
 * then render the child (or a child containing the child).
 * <p>
 *
 * @author  Janek Bogucki
 * @version $Id: SelectByRequestAttributeSAO.java,v 1.5 2002/11/13 09:27:43 jdb Exp $
 */
public class SelectByRequestAttributeSAO extends BaseServerActiveObject {
    
    /*
     * Bean getters/setters
     */
    
    /**
     * The name of the request attribute to inspect to get
     * the name of the child element to render.
     */
    private String attributeName = null ;
    
    /**
     * Set the name of the request attribute to inspect
     */
    public void setAttributeName( String attributeName ) {
        this.attributeName = attributeName ;
    }
    
    
    private String getAttributeName() {
        if ( attributeName == null )
            throw new IllegalStateException( "attributeName had not been set" ) ;
        return attributeName ;
    }
    
    
    public void render( InvocationContext icb ) throws Exception {
        
        /*
         * Store a reference to the InvocationContext so the application
         * log can be accessed in AbstractServerActiveObject.
         *
         * This method does not invoke any child elements which might make
         * a callback so it not neccessary to set the invocation context
         * for any other reason than logging.
         */
        setInvocationContext( icb ) ;
        
        String attributeValue = ( String ) icb.getHttpServletRequest().getAttribute( getAttributeName() ) ;
        
        if ( getDebug() >= 1 )
            log( "attributeName: " + getAttributeName() + ", attributeValue: " + attributeValue ) ;
        
        if ( attributeValue == null )
            throw new IllegalStateException( "'attributeValue' had not been set for attributeName = " + getAttributeName() ) ;
        
        /*
         * getChild will throw an exception if the named element does not exist.
         */
        renderElement( attributeValue ) ;
    }
    
    protected void renderElement( String elementName ) throws Exception {
        
         /*
          * getChild will throw an exception if the named element does not exist.
          */
        getChild( elementName ).render( getInvocationContext() ) ;
    }
    
}
