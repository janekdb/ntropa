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
 * AbstractContainer.java
 *
 * Created on 16 November 2001, 15:10
 */

package org.ntropa.runtime.sao;

import java.util.Iterator;
import java.util.Properties;


/**
 * This is the abstract root of AbstractServerActiveObject and AbstractElement.
 * <p>
 * BaseServerActiveObject extends AbstractServerActiveObject.
 * <p>
 * StandardElement extends AbstractElement.
 * <p>
 * A placeholder definition in a parent object affects all child objects. To achieve this the parent
 * adds all its placeholders to each AbstractContainer which is added to it.
 * <p>
 * (This is an example of a placeholder definition<p>
 * <pre>
 *      &lt;-- name=&quot;x&quot; placeholder-date=&quot;7th May 2002&quot; --&gt;
 *      ...
 * </pre>
 *
 *
 * @author  jdb
 * @version $Id: AbstractContainer.java,v 1.11 2003/03/25 11:51:00 jdb Exp $
 */
abstract public class AbstractContainer implements Container, Component {
    
    private Container _container ;
    
    private Properties _placeholders = new Properties () ;
    
    private static final String PLACEHOLDER_REPLACEMENT_ERROR =
    "Warning: there was no parent container to delegate the placeholder replacement request to:" ;
    
    /**
     * Set a placeholder.
     *
     * A later optimisation would be to group the placeholder in two.
     *
     * Group 1:
     *  placeholders which can have an effect on owned <code>Fragment</code>s
     *
     * Group 2:
     *  placeholders which have no effect on owned <code>Fragment</code>s but are
     *  needed to pass on to child <code>AbstractContainer</code>s
     *
     */
    public void setPlaceholder ( String name, String value ) {
        _placeholders.setProperty ( name, value ) ;
    }
    
    /**
     * Get a placeholder.
     *
     * @return A <code>String</code> which is the text to replace with a dynamic value
     * at http request time.
     */
    public String getPlaceholder ( String name ) {
        return _placeholders.getProperty ( name ) ;
    }
    
    /**
     * Return a copy of the placeholder held by this container
     */
    public Properties getPlaceholders () {
        Properties p = new Properties () ;
        p.putAll ( _placeholders ) ;
        return p ;
    }
    
    // ---------------------------------------------------------------- Implementation of Component
    
    /**
     * Set a <code>Container</code> object as the container/parent
     *
     * @param container A <code>Container</code> to add as the container/parent
     */
    public void setContainer ( Container container ) {
        _container = container ;
    }
    
    
    /**
     * Get the <code>Container</code> object of this component/child.
     *
     * @return A <code>Container</code> which is the parent of this component/child
     */
    public Container getContainer () {
        return _container ;
    }
    
    
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
    
    // ---------------------------------------------------------------- Implementation of Container
    
    /**
     * Add a <code>Component</code> object as a child
     *
     * @param child A <code>Component</code> to add as a child
     */
    //abstract public void addChild ( Component child ) ;
    
    /**
     * Add a <code>AbstractElement</code> object as a child
     *
     * @param child A <code>AbstractElement</code> to add as a child
     */
    abstract public void addChild ( AbstractElement child );
    
    /**
     * Add a <code>AbstractServerActiveObject</code> object as a child
     *
     * @param child A <code>AbstractServerActiveObject</code> to add as a child
     */
    abstract public void addChild ( AbstractServerActiveObject child );
    
    /**
     * Add a <code>Fragment</code> object as a child
     *
     * @param child A <code>Fragment</code> to add as a child
     */
    public abstract void addChild (Fragment child);
    
    /**
     * Test if the child exists
     * @param name name of a child to test
     */
    public abstract boolean childExists ( String name ) ;
    
    
    /**
     * Return the index of the child in this objects list of children.
     * <p>
     * The comparision uses ==.
     * <p>
     * -1 is returned if the component is not a child
     * @return the index of the child or -1 if the child is unknown.
     */
    public abstract int getChildIndex (Component child) ;
    
    
    /**
     * Helper method for allowing containers to add to the set of placeholders
     * held by their child containers
     *
     * @param child The <code>Container</code> object to add the receivers
     * placeholders to.
     */
    public void addPlaceholders ( Container child ) {
        Iterator it = _placeholders.keySet ().iterator () ;
        while ( it.hasNext () ) {
            String key = (String) it.next () ;
            /*
             * This allows a child to override a parent. i.e. if the child
             * already has a placeholder currentTime = "17:55 hrs GMT" then
             * the parent placeholder currentTime = "midnight" will not be used.
             *
             * If the child does not have the placeholder it will be added allowing
             * a parent to 'reach into' a child in a non intrusive fashion.
             */
            if ( child.getPlaceholder ( key ) == null )
                child.setPlaceholder ( key, _placeholders.getProperty ( key ) ) ;
        }
    }
    
    
    /**
     * Return the replacement text for a placeholder code. Typically this will
     * be a dynamically generated value such as the current date or the number
     * of objects in the current result set, or the user name of a logged in user.
     * <p>
     * Subclasses should override this method if they are assuming reponsibility for
     * one or more placeholder codes.
     * <p>
     * <b>A child must invoke getPlaceholderReplacementRecursively and not invoke this
     * method<</b>. Invoking getPlaceholderReplacement directly fails to allow the potential
     * replacement of an unhandled placeholder code by a parent (or other ancestor) sao.
     *
     * @param name The name of the placeholder to replace. For example if the placeholder
     * code and placeholder text was<p>
     * <pre>
     *   date: "2001-11-27, Tuesday"
     * </pre><p>
     * then a child object would invoke this method indirectly by invoking getPlaceholderReplacementRecursively<p>
     * <pre>
     *   theActualDate = container.getPlaceholderReplacementRecursively ( "date" ) ;
     *   html = replace ( html, "2001-11-27, Tuesday", theActualDate ) ;
     * </pre>
     * <p>
     * (StandardFragment does something very similar to this.)
     *
     * @return A <code>String</code> if the placeholder is handled by the container
     * or null if the parent container of the container should be asked for the
     * replacement value. This scheme allow a top level sao to provider replacement
     * values for generally useful placeholder code such as current-date, noof-course-in-database
     * etc.
     *
     */
    public String getPlaceholderReplacement (String name) {
        
        /*
         * If this method is being invoked it's because the class
         * that has extended us has not implemented it. The extension
         * will be either an extension of AbstractElement or AbstractServerActiveObject.
         *
         * In order to get the request deferred to the parent of this object we return
         * null which cause getPlaceholderReplacementRecursively to ask the parent for
         * a replacement value.
         *
         * We signal this with a rogue replacement value.
         */
        return null ;
    }
    
    /**
     * Return the replacement text for a placeholder code.
     * <p>
     * This method was added to allow {@link AbstractServerActiveObject} to return
     * a replacement value from an instance of {@link PlaceholderReplacementStack}
     * instead of {@link AbstractServerActiveObject#getPlaceholderReplacement}
     *
     * @return A <code>String</code> if the placeholder is handled by the container
     * or null if the {@link #getPlaceholderReplacement} method should be asked for the
     * replacement value
     */
    public String getPlaceholderReplacementFromAlternativeSource (String name) {
        
        /*
         * If this method is being invoked it's because the class
         * that has extended us has not implemented it. The extension
         * will be either an extension of AbstractElement or AbstractServerActiveObject.
         *
         * We return null which causes getPlaceholderReplacementRecursively to
         * ask {@link #getPlaceholderReplacement} for a replacement value.
         */
        return null ;
    }
    
    /**
     * Return the value for the placeholder code.
     * <p>
     * The standard implemetation of this method returns the first non-null value of
     * {@link #getPlaceholderReplacementFromAlternativeSource} or {@link #getPlaceholderReplacement},
     * otherwise it asks the parent container for the replacement value and returns that.
     * If there is no parent container a warning message is returned.
     * <p>
     * The passing of the request to the parent container allows generally useful placeholders to
     * be embedded in the html of the <code>Element</code>s of server active objects which do not
     * explicitly handle the placeholder code in their getPlaceholderReplacement method.
     * <p>
     * For example<br>
     * suppose sao A handles the system placeholder $$current-time$$ and sao B has an
     * <code>Element</code> with the text<p>
     * <pre>
     *      The current time is $$current-time$$.
     * </pre>
     * <p>
     * then if B is a child of A (omitting the implicit intermediate <code>Element</code> '-implicit') as
     * this relationship<p>
     * <pre>
     *      A is a parent of B
     * </pre>
     * <p>then when the Fragment containing the given text asks B for a replacement value for the placeholder
     * code &quot;current-time&quot; by invoking B.getPlaceholderReplacementRecursively, B invokes
     * getPlaceholderReplacement which returns null so B.getPlaceholderReplacementRecursively asks
     * A.getPlaceholderReplacementRecursively which asks A.getPlaceholderReplacement which supplies the value.
     *
     * @param name The name of the placeholder to replace.
     *
     * @return A <code>String</code> if the placeholder is handled by the container
     * or the parent container, recursively.
     */
    public String getPlaceholderReplacementRecursively ( String name ) {
        
        /*
         * Invoke the method in this class or a subclass's override of this method.
         */
        String replacementValue = getPlaceholderReplacementFromAlternativeSource ( name ) ;
        if ( replacementValue != null )
            return replacementValue ;
        
        /*
         * Invoke the method in this class or a subclass's override of this method.
         */
        replacementValue = getPlaceholderReplacement ( name ) ;
        
        if ( replacementValue != null )
            return replacementValue ;
        
        /*
         * AbstractElements always have containers (if the object tree was constructed properly
         * and AbstractServerActiveObjects should not fail to always return a non null value
         * from getPlaceholderReplacement if they are to be the root object of a tree of server
         * active objects. Therefore it is any error for this method to be invoked
         * when the object does not have a parent container.
         *
         * We signal this with a rogue replacement value otherise we ask the parent of this object
         * for a replacement value.
         */
        
        if ( _container == null )
            return PLACEHOLDER_REPLACEMENT_ERROR + "[ name = " + name + " ] [" + this.getClass ().getName () + "]" ;
        
        /* Delegate to parent container; this is the standard behaviour for AbstractElements */
        return _container.getPlaceholderReplacementRecursively ( name ) ;
        
        
    }
    
}
