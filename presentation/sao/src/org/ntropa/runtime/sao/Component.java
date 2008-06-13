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
 * Component.java
 *
 * Created on 15 November 2001, 17:39
 */

package org.ntropa.runtime.sao;


/**
 * This interface provides the list of methods an object must implement
 * to be used in a wps generated JSP.
 *
 * @author  jdb
 * @version $Id: Component.java,v 1.5 2003/03/11 22:33:11 jdb Exp $
 */
public interface Component {
    
    /**
     * Set a <code>Container</code> object as the container/parent.
     *
     * @param container A <code>Container</code> to add as the container/parent
     */
    public void setContainer ( Container container );
    

    /**
     * Get the <code>Container</code> object of this component/child.
     *
     * @return A <code>Container</code> which is the parent of this component/child
     */
    public Container getContainer ();

    /**
     * Invoked inside the JSP to perform controlling logic. No data/HTML can be
     * sent to the page buffer during this phase
     *
     * @throws Exception, otherwise it would have to throw every exception that every
     * method of every object could throw.
     *
     * @param icb A <code>InvocationContext</code> supplied by the JSP.
     */
    public void control ( InvocationContext icb ) throws Exception ;

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
    public void render ( InvocationContext icb ) throws Exception ;
    
    /**
     * Invoked inside the JSP to clean up after a request. All rendering is complete
     * by the time this method is invoked.
     */
    public void recycle () ;
}

