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
 * InvocationContext.java
 *
 * Created on 26 November 2001, 14:46
 */

package org.ntropa.runtime.sao;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;


/**
 * This interface is used to reduce the amount of typing while thrashing parameters through
 * the server active object tree.
 *
 * To enforce the separation of control and view the JspWriter can be made inaccessible during
 * the control phase.
 *
 * @author  jdb
 * @version $Id: InvocationContext.java,v 1.5 2002/04/04 12:27:52 jdb Exp $
 *
 *
 */
public interface InvocationContext {
    
    /**
     * @param pageContext A <code>PageContext</code> supplied by the JSP.
     */
    void setPageContext ( PageContext pageContext );
    PageContext getPageContext ();
    
    /**
     * @param session A <code>HttpSession</code> supplied by the JSP.
     */
    void setHttpSession ( HttpSession session );
    HttpSession getHttpSession ();
    
    /**
     * @param application A <code>ServletContext</code> supplied by the JSP.
     */
    void setServletContext ( ServletContext application );
    ServletContext getServletContext ();
    
    /**
     * @param config A <code>ServletConfig</code> supplied by the JSP.
     */
    void setServletConfig ( ServletConfig config );
    ServletConfig getServletConfig ();
    
    /**
     * @param out A <code>JspWriter</code> supplied by the JSP.
     */
    void setJspWriter ( JspWriter out );
    JspWriter getJspWriter ();
    
    /**
     * @param page A <code>Object</code> supplied by the JSP.
     */
    void setPage ( Object page );
    Object getPage ();
    
    /**
     * @param request A <code>HttpServletRequest</code> supplied by the JSP.
     */
    void setHttpServletRequest ( HttpServletRequest request );
    HttpServletRequest getHttpServletRequest ();
    
    /**
     * @param response A <code>HttpServletResponse</code> supplied by the JSP.
     */
    void setHttpServletResponse ( HttpServletResponse response );
    HttpServletResponse getHttpServletResponse ();
    
    /**
     * Invoke <code>enableControlPhase</code> to set the context to
     * be suitiable for control logic which includes page flow control
     * via http redirection.
     */
    void enableControlPhase () ;
    
    
    /**
     * Invoke <code>enableRenderPhase</code> to set the context to
     * be suitiable for rendering work which includes the sending
     * of HTML to the page buffer.
     */
    void enableRenderPhase () ;
    
    /**
     * Return true if the context is set for the control phase.
     * Return false if the context is set for the render phase.
     * @return True if in control phase
     */
    boolean isControlPhase () ;
    
    Controller getController () ;
    
    boolean sendRedirectAllowed () ;
    
    /**
     * Invoke this method when the invocation context is finished with.
     * This method should disable the use of the object which prevents
     * the accidental reuse of the object if it has a reference to it
     * is a server active object cache somewhere.
     *
     * This is somewhat an interim solution to the potential pitfall
     * of a SAO retaining a request specific object and accidentally
     * reusing the object in a later request.
     */
    void disable () ;
}

