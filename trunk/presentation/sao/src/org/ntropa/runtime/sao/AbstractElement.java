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
 * AbstractElement.java
 *
 * Created on 16 November 2001, 16:11
 */

package org.ntropa.runtime.sao;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 *
 * @author  jdb
 * @version $Id: AbstractElement.java,v 1.8 2004/09/17 09:28:19 jdb Exp $
 */
public abstract class AbstractElement extends AbstractContainer implements Named {
    
    private List _children = new LinkedList () ;
    
    private String _name;
    
    // ---------------------------------------------------------------- Implementation of Component
    
    /**
     * Invoked inside the JSP to perform controlling logic. No data/HTML can be
     * sent to the page buffer during this phase
     *
     * @throws Exception, otherwise it would have to throw every exception that every
     * method of every object could throw.
     *
     * @param icb A <code>InvocationContext</code> supplied by the JSP.
     */
    abstract public void control ( InvocationContext icb ) throws Exception ;
    
    
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
    abstract public void render ( InvocationContext icb ) throws Exception ;
    
    /* --- Implementation of Container --- */
    
    /**
     * Add a <code>AbstractServerActiveObject</code> object as a child
     *
     * An AbstractElement only has AbstractServerActiveObjects and StandardFragments as children.
     *
     * @param child A <code>AbstractServerActiveObject</code> to add as a child
     */
    public void addChild ( AbstractServerActiveObject child ) {
        //FIXME: this should log an error or throw an Exception as it is never expected to happen
        if ( _children.contains ( child ) )
            return ;
        
        _children.add ( child ) ;
        
        child.setContainer ( this ) ;
        
        /* The child inherits our placeholders */
        addPlaceholders ( child ) ;
    }
    
    /**
     * Add a <code>Component</code> object as a child
     *
     * @param child A <code>StandardFragment</code> to add as a child
     */
    public void addChild (Fragment child) {
        
        //FIXME: this should log an error or throw an Exception as it is never expected to happen
        if ( _children.contains ( child ) )
            return ;
        
        child.setContainer ( this ) ;
        
        _children.add ( child ) ;
    }
    
    /**
     * Add a <code>AbstractElement</code> object as a child
     *
     * @param child A <code>AbstractElement</code> to add as a child
     */
    public void addChild ( AbstractElement child ) {
        throw new UnsupportedOperationException () ;
    }
    
    /**
     * Test if the child exists
     * @param name name of a child to test
     */
    public boolean childExists ( String name ) {
        
        return _children.contains ( name ) ;
    }
    
    
    /**
     * Return the index of the child in this objects list of children.
     * <p>
     * The comparision uses ==.
     * <p>
     * -1 is returned if the component is not a child
     * @return the index of the child or -1 if the child is unknown.
     */
    public int getChildIndex (Component child) {

        for ( int i = 0 ; i < _children.size () ; i++ )
            if ( _children.get ( i ) == child )
                return i ;
        
        return - 1;
    }
    
    // -------------------------------------------------------------------- Implementation of Named
    
    public String getName () {
        return _name ;
    }
    
    public void setName ( String name ) {
        _name = name ;
    }
    
    // ------------------------------------------------------------------------------ Miscellaneous
    
    /**
     * Provide access to the children of this object.
     */
    protected Iterator childIterator () {
        return _children.iterator () ;
    }
    
    
}
