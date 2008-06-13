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
 * Controller.java
 *
 * Created on 29 January 2002, 17:09
 */

package org.ntropa.runtime.sao;

import java.io.IOException;

/**
 *
 * @author  jdb
 * @version $Id: Controller.java,v 1.1 2002/01/29 17:46:10 jdb Exp $
 */
public interface Controller {

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
    boolean proceed () ;
    
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
    void sendRedirect ( String location ) throws IOException ;
    
}

