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
 * Container.java
 *
 * Created on 16 November 2001, 14:58
 */

package org.ntropa.runtime.sao;

import java.util.Properties;


/**
 * This interface provides the list of methods an object must implement
 * to be used as a container of <code>Component</code>s in a wps generated JSP.
 *
 * @author  jdb
 * @version $Id: Container.java,v 1.6 2002/09/11 22:14:02 jdb Exp $
 */
public interface Container {
    
    /* --- methods for adding child objects --- */
    
    /**
     * Add a <code>AbstractElement</code> object as a child
     *
     * @param child A <code>AbstractElement</code> to add as a child
     */
    void addChild ( AbstractElement child );
    
    /**
     * Add a <code>AbstractServerActiveObject</code> object as a child
     *
     * @param child A <code>AbstractServerActiveObject</code> to add as a child
     */
    void addChild ( AbstractServerActiveObject child );
    
    /**
     * Add a <code>Fragment</code> object as a child
     *
     * @param child A <code>Fragment</code> to add as a child
     */
    void addChild ( Fragment child );
    
    /**
     * Test if the child exists
     * @param name name of a child to test
     */
    boolean childExists ( String name ) ;
    
    
    /**
     * Return the index of the child in this objects list of children.
     * <p>
     * The comparision uses ==.
     * <p>
     * -1 is returned if the component is not a child
     * @return the index of the child or -1 if the child is unknown.
     */
    int getChildIndex ( Component child ) ;
    
    
   /* --- methods for using placeholders --- */
    
    /**
     * Helper method for allowing containers to add to the set of placeholders
     * held by their child containers i.e. a container adds placeholders to the
     * child.
     *
     * @param child The <code>Container</code> object to add the receivers
     * placeholders to.
     */
    void addPlaceholders ( Container child ) ;
    
    /**
     * Set a placeholder.
     *
     * @param name The name of the placeholder i.e. 'num-op'
     * @param value The text to replace with the value of the placeholder
     * i.e. '198,000'
     */
    void setPlaceholder ( String name, String value ) ;
    
    /**
     * Get a placeholder.
     *
     * @return A <code>String</code> which is the text to replace with a dynamic value
     * at http request time.
     */
    String getPlaceholder ( String name ) ;
    
    /**
     * Return a copy of the placeholder held by this container
     *
     * @return a <code>Properties</code> object which is a copy of the placeholders
     * owned by the container.
     */
    Properties getPlaceholders () ;
    
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
    String getPlaceholderReplacement ( String name ) ;
    
    
    /**
     * Return the value for the placeholder code.
     * <p>
     * The standard implemetation of this method retuens the value of getPlaceholderReplacement if
     * not null otherwise it asks the parent container for the replacement value and returns that.
     * If there is no parent container a warning message is returned.
     * <p>
     * The passing of the request to the parent container allows generally useful placeholders to
     * be embedded in the html of the <code>Element</code>s of server active objects which do not
     * explicitly handle the placeholder code in their getPlaceholderReplacement method.
     * <p>
     * For example
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
    String getPlaceholderReplacementRecursively ( String name ) ;
}

