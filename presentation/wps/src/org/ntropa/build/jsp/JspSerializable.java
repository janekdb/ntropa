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
 * JspSerializable.java
 *
 * Created on 15 November 2001, 12:35
 */

package org.ntropa.build.jsp;

import java.util.List;


/**
 *
 * @author  jdb
 * @version $Id: JspSerializable.java,v 1.5 2002/02/05 16:19:33 jdb Exp $
 */
public interface JspSerializable {
    
    /**
     * Return a list of children
     *
     * @return A <code>List</code> of objects owned by the object.
     */
    public List getChildren ();
    
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
    public String getComponentClassName ( FinderSet finderSet );
     */
    
    /**
     * Return the full name of the object type implemented by the class from getComponentClassName.
     *
     * @return A <code>String</code> representing the full name of the type
     */
    public String getComponentTypeName ();
    
    /**
     * Create the code to set up this object
     *
     * <p>Example 1.</p>
     *
     * <p>A ServerActiveObject which implements the JSP functionality
     * of a Fragment needs to have its HTML available at http-serve time.
     * So the JspSerializable based on an underlying Fragment writes out
     * something like this:</p>
     * <pre>
     * ServerActiveObject component_1 = new org.ntropa.runtime.sao.JspFragment () ;
     *
     * component_1.setHtml ( "escaped-html-string" ) ;
     * </pre>
     *
     * <p>Example 2.</p>
     *
     * <p>A ServerActiveObject which implements the JSP functionality
     * of a ServerActiveObject needs to have its property setters invoked at init-time.
     * So the JspSerializable based on an underlying Fragment writes out
     * something like this:</p>
     * <pre>
     * ServerActiveObject component_1 = new org.ntropa.runtime.sao.BaseServerActiveObject () ;
     *
     * component_1.setWebmasterEmail ( "webmaster@studylink.com" ) ;
     * component_1.setErrorPage ( "../error/error.html" ) ;
     * </pre>
     *
     * @param objName The name of the object to create and initialise.
     *
     * @param buffer A <code>StringBuilder</code> to append set up code to
     *
     * @param finderSet A <code>FinderSet</code> used to look up information based
     * on a certain context. The context is expected to be the current page location.
     */
    public void getSetUpCode ( String objName, StringBuilder buffer, FinderSet finderSet );
}

