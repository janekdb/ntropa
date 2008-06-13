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
 * AbstractServerActiveObject.java
 *
 * Created on 16 November 2001, 15:46
 */

package org.ntropa.runtime.sao;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletContext;

/**
 *
 * @author  jdb
 * @version $Id: AbstractServerActiveObject.java,v 1.21 2004/09/17 09:28:19 jdb Exp $
 */
abstract public class AbstractServerActiveObject extends AbstractContainer {
    
    /* store a map of elements for easy access */
    private Map _children = new HashMap () ;
    
    /**
     * This is a static field as a thread has just one InvocationContext.
     */
    private static ThreadLocal _icb = new ThreadLocal () ;
    
    /**
     * This allow named objects to be cached over method invocations.
     *
     * This is an instance field to prevent interference of parent caches
     * from child cache use.
     */
    private ThreadLocal _invocationCache = new ThreadLocal () ;
    
    /**
     * The debug level for this server active object.
     *
     * Higher values result in more messages being sent to the application log.
     */
    private int debug = 0 ;
    
    public void setDebug ( int debug ) {
        /* A negative arg is a mistake; go large in response */
        this.debug = debug >= 0 ? debug : 99 ;
    }
    
    /**
     * To support the basic configuration allowed from the application.properties
     * file which resides in _application we provide a setter that takes a <code>String</code>.
     *
     * @param debug A <code>String</code> representation of the required debug level.
     */
    public void setDebug ( String debugString ) {
        
        try {
            setDebug ( Integer.parseInt ( debugString ) ) ;
        }
        catch ( NumberFormatException e ) {
            setDebug ( 99 ) ;
        }
    }
    
    public int getDebug () {
        return debug ;
    }
    
    // ---------------------------------------------------------------- Implementation of Component
    
    /**
     * Invoked inside the JSP to perform controlling logic. No data/HTML can be
     * sent to the page buffer during this phase
     *
     * This implementation of control ( ... ) tries to reduce the subclass's complexity
     * by factoring the invocation into two further methods. Subclasses can override the
     * minimum number of these. Override controlSelf ( ... ) to provide control logic
     * specific to the sao. Override controlSelf ( ... ) to implement custom invocation
     * of the control ( ... ) methods of the child objects.
     *
     * The normal scenario is to override controlSelf ( ... ) and leave the superclass
     * to handle the control phase for the child objects.
     *
     * If the control logic was distributed between a parent and its child objects then
     * it would be useful to override controlChildren ( ... ) and maybe not override
     * controlSelf ( ... ) if all the controlling logic is in controlChildren.
     *
     * @throws Exception, otherwise it would have to throw every exception that every
     * method of every object could throw.
     *
     * @param icb A <code>InvocationContext</code> supplied by the JSP.
     */
    public void control ( InvocationContext icb ) throws Exception {
        
        controlSelf ( icb ) ;
        controlChildren ( icb ) ;
        
    }
    
    abstract public void controlSelf ( InvocationContext icb ) throws Exception ;
    abstract public void controlChildren ( InvocationContext icb ) throws Exception ;
    
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
     * Add a <code>AbstractElement</code> object as a child
     *
     * A AbstractServerActiveObject only has AbstractElements as children.
     *
     * @param child A <code>AbstractElement</code> to add as a child
     */
    public void addChild ( AbstractElement child ) {
        //FIXME: this should log an error or throw an Exception as it is never expected to happen
        if ( _children.containsKey ( child ) )
            return ;
        
        _children.put ( child.getName (), child ) ;
        
        child.setContainer ( this ) ;
        
        /* The child inherits our placeholders */
        addPlaceholders ( child ) ;
    }
    
    /**
     * Add a <code>AbstractServerActiveObject</code> object as a child
     *
     * @param child A <code>AbstractServerActiveObject</code> to add as a child
     */
    public void addChild ( AbstractServerActiveObject child ) {
        throw new UnsupportedOperationException () ;
    }
    
    /**
     * Add a <code>Fragment</code> object as a child
     *
     * @param child A <code>Fragment</code> to add as a child
     */
    public void addChild (Fragment child) {
        throw new UnsupportedOperationException () ;
    }
    
    /**
     * Test if the child exists
     * @param name name of a child to test
     */
    public boolean childExists ( String name ) {
        
        return _children.containsKey ( name ) ;
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
        
        SortedSet keys = getChildren () ;
        int i = 0 ;
        for ( Iterator it = keys.iterator (); it.hasNext () ; i++ )
            if ( getChild ( ( String ) it.next () ) == child )
                return i ;
        
        return -1 ;
    }
    
    
    /**
     * Return the named <code>AbstractElement</code>.
     *
     * @param name The name of the subclass instance of <code>AbstractElement</code>
     * to return. Returns null if the object does not exist as a child of this object.
     * @param A <code>AbstractElement</code> object corresponding to the name
     * @return A subclass of <code>AbstractElement</code>.
     * @throws NoSuchAbstractElementException when the named child doesn't exist.
     */
    public AbstractElement getChild ( String name ) {
        AbstractElement ae = ( AbstractElement ) _children.get ( name ) ;
        if ( ae == null )
            throw new NoSuchAbstractElementException (
            "The named AbstractElement did not exist: " + name ) ;
        return ae ;
    }
    
    /**
     * Return the names of the children
     *
     * @return A <code>Set</code> of child names
     */
    public SortedSet getChildren () {
        return new TreeSet ( _children.keySet () ) ;
    }
    
    /**
     * Provide support for caching the InvocationContext across method invocations
     * in the same Thread
     *
     * The typical use of this is in the controlSelf (or render) method where it is useful
     * to be able to access the original request object in the getPlaceholderReplacement method.
     *
     * @param icb The InvocationContext to remember
     */
    public void setInvocationContext ( InvocationContext icb ) {
        _icb.set ( icb ) ;
    }
    
    /**
     * Provide support for caching the InvocationContext across method invocations
     * in the same Thread
     *
     * @return Returns the remembered InvocationContext
     */
    public InvocationContext getInvocationContext () {
        return (InvocationContext) _icb.get () ;
    }
    
    /**
     * Create a Map for the exclusive use by the current Thread
     */
    public void initThreadLocalCache () {
        _invocationCache.set ( new HashMap () ) ;
    }
    
    /**
     * Clear the Map
     */
    public void clearThreadLocalCache () {
        Map m = (Map) _invocationCache.get () ;
        if ( m != null )
            m.clear () ;
    }
    
    /**
     * Set a value in the thread local Map, creating the Map if it is non-existenet
     */
    public void setThreadLocalValue ( String key, Object value ) {
        
        Map m = (Map) _invocationCache.get () ;
        
        if ( m == null ) {
            initThreadLocalCache () ;
            m = (Map) _invocationCache.get () ;
            if ( m == null )
                throw new NullPointerException ( "Created a thread local Map but it vanished" ) ;
        }
        m.put ( key, value ) ;
    }
    
    /**
     * Get a value from the thread local Map.
     * @throws IllegalStateException if the thread local Map does not exist.
     * The thread local Map is lazily created in setThreadLocalValue.
     */
    public Object getThreadLocalValue ( String key ) {
        
        Map m = (Map) _invocationCache.get () ;
        
        if ( m == null ) {
            throw new IllegalStateException ( "No previous invocation of 'setThreadLocalValue', key: " + key ) ;
        }
        return m.get ( key ) ;
    }
    
    
    /**
     * Clear up after a request.
     * <p>
     * The child objects are asked to recycle before the parent resulting
     * in a depth first (? not quite sure if depth first is correct description)
     * traversal of the object tree.
     */
    public void recycle () {
        
        for (
        Iterator it = _children.values ().iterator () ;
        it.hasNext () ;
        ) {
            /* We know the child is an AbstractElement but use the super interface */
            Component e = ( Component ) it.next () ;
            e.recycle () ;
        }
        
        clearThreadLocalCache () ;
        
        /* 
         * getPlaceholderReplacementStack () can not be used because this would
         * create both of the object tested for nullness below. Synchronization
         * is not required.
         */
        if ( _placeholderReplacementStacks != null ) {
            PlaceholderReplacementStack s = ( PlaceholderReplacementStack ) _placeholderReplacementStacks.get () ;
            if ( s != null )
                s.clear () ;
        }
    }
    
    
    /**
     * Log a message to the application log if it is available otherwise log
     * the message to standard out.
     * @param message A <code>String</code> to log
     */
    public void log (String message) {
        
        message = "[" + this.getClass ().getName () + "] " + message ;
        
        if (getInvocationContext () == null) {
            System.out.println ( message );
            return ;
        }
        
        ServletContext application = getInvocationContext ().getServletContext ();
        application.log ( message + " [URI: " + getInvocationContext ().getHttpServletRequest ().getRequestURI () + "]" );
        
    }
    
    /**
     * Log a message to the application log if it is available otherwise log
     * the message to standard out.
     * @param message A <code>String</code> to log
     * @param exception An <code>Exception</code> to log
     */
    public void log (String message, Exception exception ) {
        
        message = "[" + this.getClass ().getName () + "] " + message ;
        
        if (getInvocationContext () == null) {
            System.out.println ( message );
            exception.printStackTrace () ;
            return ;
        }
        
        ServletContext application = getInvocationContext ().getServletContext ();
        application.log (
        message + " [URI: " + getInvocationContext ().getHttpServletRequest ().getRequestURI () + "]",
        exception
        );
        
    }
    
    
    /**
     * A general purpose key
     */
    
    /**
     * This returns a key the SAO can use to store data with.
     * <p>
     * This provides the SAO with a string value to store it's data into servlet
     * objects with. The key is unique amongst
     * all SAO configured in the webapp. By storing its data with this key
     * the webapp can be reloaded and the session state will be preserved.
     * <p>
     * It can be used as a key to save objects into <code>HttpSessions</code>
     * (and other Servlet objects).
     * @return a <code>String</code> which is unique to this instance. The key will
     * be the same after recompiling the page provided the heirarcy of objects in the
     * page has not changed. The key has the form servlet-path#-1-0-6 where each number is
     * the index of the object in it's parent's list of children. The left most index is
     * the index of the object in it's parent, the right most index is the index of the
     * object's penultimate ancestor.
     */
    protected String getDataKey () {
        
        InvocationContext icb = getInvocationContext () ;
        if ( icb == null )
            throw new IllegalStateException ( "The InvocationContext was null" ) ;
        
        StringBuffer sb = new StringBuffer ( icb.getHttpServletRequest ().getServletPath () ) ;
        sb.append ( "#" ) ;
        Component currentChild = this ;
        for ( Container parent = getContainer () ; parent != null ;  ) {
            int i = parent.getChildIndex ( currentChild ) ;
            if ( i == -1 )
                throw new RuntimeException ( "child was not in parent's list of children." ) ;
            sb.append ( "-" + i ) ;
            currentChild = ( Component ) parent ;
            parent = ( ( Component ) parent ).getContainer () ;
        }
        
        return sb.toString () ;
    }
    
    
    /**
     * Return the replacement text for a placeholder code from the stack of
     * replacements.
     * @return A <code>String</code> if the placeholder is handled by the <code>PlaceholderReplacementStack</code>
     * or null if the {@link #getPlaceholderReplacement} method should be asked for the
     * replacement value instead.
     */
    public String getPlaceholderReplacementFromAlternativeSource (String name) {
        
        return getPlaceholderReplacementStack ().getPlaceholderReplacement ( name ) ;
    }
    
    
    
    /**
     * Return the instance of <code>PlaceholderReplacementStack</code> unique to
     * this particular object and thread.
     * <p>
     * All code and value pairs are popped from the stack, after the page has been rendered,
     * in the {@link #recycle} method
     *
     * @see PlaceholderReplacementStack
     * @return The instance of <code>PlaceholderReplacementStack</code> unique to
     * this particular object and thread.
     */
    public PlaceholderReplacementStack getPlaceholderReplacementStack () {
        
        /*
         * Lazy initilisation to minimise creation of objects. Some saos
         * will not be using the stack mechanism.
         */
        synchronized ( this ) {
            if ( _placeholderReplacementStacks == null )
                _placeholderReplacementStacks = new ThreadLocal () ;
        }
        
        /* No synchronized required because _placeholderReplacementStacks is a ThreadLocal */
        PlaceholderReplacementStack s = ( PlaceholderReplacementStack ) _placeholderReplacementStacks.get () ;
        if ( s == null )
            _placeholderReplacementStacks.set ( s = new PlaceholderReplacementStack () ) ;
        
        return s ;
    }
    
    /**
     * The object used to store <code>PlaceholderReplacementStack</code>s in.
     */
    private ThreadLocal _placeholderReplacementStacks = null ;
    
    
    /**
     * Enforce identity equality
     * @return true iff other is the same object
     */
    public final boolean equals ( Object other ) {
        
        return this == other ;
    }
}
