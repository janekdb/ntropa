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
 * StandardInvocationContext.java
 *
 * Created on 26 November 2001, 14:53
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
 *
 * @author  jdb
 * @version $Id: StandardInvocationContext.java,v 1.5 2002/04/04 12:27:52 jdb Exp $
 *
 *
 */
public class StandardInvocationContext implements InvocationContext {
    
    private PageContext _pageContext ;
    private HttpSession _session ;
    private ServletContext _application ;
    private ServletConfig _config ;
    private JspWriter _out ;
    private Object _page ;
    private HttpServletRequest _request ;
    private HttpServletResponse _response ;
    
    /**
     * If true the context is set for the control phase otherwise
     * we are in the render phase.
     */
    private boolean _controlPhase = true ;
    
    private StandardController controller ;
    
    /**
     * If false no access to the object is permitted
     */
    private boolean enabled = true ;
    
    /** Creates new StandardInvocationContext */
    public StandardInvocationContext () {
        controller = new StandardController () ;
        controller.setInvocationContext ( this ) ;
    }
    /**
     * @param pageContext A <code>PageContext</code> supplied by the JSP.
     */
    public void setPageContext ( PageContext pageContext ) { _pageContext = pageContext ; }
    public PageContext getPageContext () { checkAccess () ; return _pageContext ; }
    
    /**
     * @param session A <code>HttpSession</code> supplied by the JSP.
     */
    public void setHttpSession ( HttpSession session ) { _session = session ; }
    public HttpSession getHttpSession () { checkAccess () ; return _session ; }
    
    /**
     * @param application A <code>ServletContext</code> supplied by the JSP.
     */
    public void setServletContext ( ServletContext application ) { _application = application ; }
    public ServletContext getServletContext () { checkAccess () ; return _application ; }
    
    /**
     * @param config A <code>ServletConfig</code> supplied by the JSP.
     */
    public void setServletConfig ( ServletConfig config ) { _config = config ; }
    public ServletConfig getServletConfig () { checkAccess () ; return _config ; }
    
    /**
     * @param out A <code>JspWriter</code> supplied by the JSP.
     */
    public void setJspWriter ( JspWriter out ) { _out = out ; }
    public JspWriter getJspWriter () {
        checkAccess () ;
        if ( _controlPhase )
            throw new IllegalStateException ( "The JspWriter can not be accessed while in the control phase" ) ;
        return _out ;
    }
    
    /**
     * @param page A <code>Object</code> supplied by the JSP.
     */
    public void setPage ( Object page ) { _page = page ; }
    public Object getPage () { checkAccess () ; return _page ; }
    
    /**
     * @param request A <code>HttpServletRequest</code> supplied by the JSP.
     */
    public void setHttpServletRequest ( HttpServletRequest request ) { _request = request ; }
    public HttpServletRequest getHttpServletRequest () { checkAccess () ; return _request ; }
    
    /**
     * @param response A <code>HttpServletResponse</code> supplied by the JSP.
     */
    public void setHttpServletResponse ( HttpServletResponse response ) { _response = response ; }
    public HttpServletResponse getHttpServletResponse () { checkAccess () ; return _response ; }
    
    
    /**
     * Invoke <code>enableControlPhase</code> to set the context to
     * be suitiable for control logic which includes page flow control
     * via http redirection.
     */
    public void enableControlPhase () {
        checkAccess () ;
        _controlPhase = true ;
    }
    
    
    /**
     * Invoke <code>enableRenderPhase</code> to set the context to
     * be suitiable for rendering work which includes the sending
     * of HTML to the page buffer.
     */
    public void enableRenderPhase () {
        checkAccess () ;
        _controlPhase = false ;
    }
    
    /**
     * Return true if the context is set for the control phase.
     * Return false if the context is set for the render phase.
     * @return True if in control phase
     */
    public boolean isControlPhase () {
        checkAccess () ;
        return _controlPhase ;
    }
    
    
    public Controller getController ()  { checkAccess () ; return controller ; }
    
    /**
     * Http redirection is not allowed during the render phase. The buffer may have
     * already been committed.
     *
     * @return Returns true if redirection is allowed.
     */
    public boolean sendRedirectAllowed () {
        checkAccess () ;
        return isControlPhase () ;
    }
    
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
    public void disable () {
        checkAccess () ;
        /* Release references to allow gc */
        _pageContext = null ;
        _session = null ;
        _application = null ;
        _config = null ;
        _out = null ;
        _page = null ;
        _request = null ;
        _response = null ;
        /* Prevent future access */
        enabled = false ;
    }
    
    private void checkAccess () {
        if ( !enabled )
            throw new IllegalStateException ( "Access not permitted while in disabled state" ) ;
    }
}
