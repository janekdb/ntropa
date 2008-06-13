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
 * StandardController.java
 *
 * Created on 29 January 2002, 17:13
 */

package org.ntropa.runtime.sao;

import java.io.IOException;

/**
 *
 * @author  jdb
 * @version $Id: StandardController.java,v 1.2 2002/02/01 15:23:24 jdb Exp $
 */
public class StandardController implements Controller {
    
    private boolean proceed = true ;
    
    private InvocationContext invocationContext ;
    
    /** Creates new StandardController */
    public StandardController () {
    }
    
    /**
     * Return true if the next part of the server active object tree
     * should be invoked otherwise return false.
     *
     * One situation in which this method should return false is when
     * the last invoked sao requested a redirection to an alternative
     * page.
     *
     * @return Return <code>true</code> if control should be passed to
     * the next part of the server active tree.
     */
    public boolean proceed () {
        return proceed ;
    }
    
    /**
     * This method has the same functionality as javax.servlet.http.sendRedirect.
     *
     * Server active objects should invoke this method instead of the method of
     * the same name in HttpServletResponse.
     *
     * Implementations will likely record this invocation and ensure proceed returns
     * false on the next invocation.
     *
     * @param location - the redirect location URL
     */
    public void sendRedirect ( String location ) throws IOException {
        if ( ! invocationContext.sendRedirectAllowed () )
            throw new IllegalStateException ( "sendRedirect can not be invoked in the render phase" ) ;
        
        proceed = false ;
        invocationContext.getHttpServletResponse ().sendRedirect ( location ) ;
    }
    
    /**
     * Set the InvocationContext to make callbacks to
     * @param invocationContext The InvocationContext to make callbacks to
     */
    public void setInvocationContext ( InvocationContext invocationContext ) {
        if ( invocationContext == null )
            throw new IllegalArgumentException
            ( "Attempt to invoke StandardController.setInvocationContext with null InvocationContext" ) ;
        this.invocationContext = invocationContext ;
    }
}
