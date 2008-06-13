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
 * StandardElement.java
 *
 * Created on 16 November 2001, 16:30
 */

package org.ntropa.runtime.sao;

import java.util.Iterator;


/**
 * A <code>StandardElement</code> is a concrete version of AbstractElement (for the moment).
 *
 * @author  jdb
 * @version $Id: StandardElement.java,v 1.5 2003/03/11 22:33:11 jdb Exp $
 */
public class StandardElement extends AbstractElement {
    
    // ---------------------------------------------------------------- Implementation of Component
    
    /**
     * Invoked inside the JSP to perform controlling logic. No data/HTML can be
     * sent to the page buffer during this phase
     *
     * A <code>StandardElement</code> asks its children to execute their control logic in
     * order provider control is allowed proceed.
     *
     * @throws Exception, otherwise it would have to throw every exception that every
     * method of every object could throw.
     *
     * @param icb A <code>InvocationContext</code> supplied by the JSP.
     */
    public void control ( InvocationContext icb ) throws Exception {
        Iterator it = childIterator () ;
        while ( it.hasNext () ) {
            if ( icb.getController ().proceed () ) {
                ( ( Component ) it.next () ).control ( icb ) ;
            }
            else
                break ;
        }
    }
    
    
    /**
     * Invoked inside the JSP to render content into the page buffer. All control
     * logic has already been executed by the time this method is invoked. The method
     * is responsible for rendering the view (HTML) from the model (session data) and
     * nothing more. In particular no http redirection should be attempted during
     * the rendering phase.
     *
     * A <code>StandardElement</code> renders its children in order.
     *
     * @throws Exception, otherwise it would have to throw every exception that every
     * method of every object could throw.
     *
     * @param icb A <code>InvocationContext</code> supplied by the JSP.
     */
    public void render ( InvocationContext icb ) throws Exception {
        Iterator it = childIterator () ;
        while ( it.hasNext () )
            ( ( Component ) it.next () ).render ( icb ) ;
    }
    
    
    public void recycle () {
        Iterator it = childIterator () ;
        while ( it.hasNext () )
            ( ( Component ) it.next () ).recycle () ;
        
    }
    
}
