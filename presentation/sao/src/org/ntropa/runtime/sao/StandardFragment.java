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
 * StandardFragment.java
 *
 * Created on 16 November 2001, 15:00
 */

package org.ntropa.runtime.sao;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.Transformer;
import org.ntropa.utility.CollectionsUtilities;


/**
 * This class is a concrete implementation of the <code>Fragment</code> interface.
 * <p>
 * An object of this class will ask it's parent container for placeholder replacement
 * value. Only the placeholders which apply are asked for.
 *
 * @author  jdb
 * @version $Id: StandardFragment.java,v 1.15 2003/11/27 12:52:41 jdb Exp $
 */
public final class StandardFragment implements Fragment {
    
    private Container _container ;
    
    private String _html ;
    
    /* Temporary use arrays to reduce parameter passing */
    private String [] _placeholderKeys ;
    private String [] _placeholders ;
    
    /** Creates new StandardFragment */
    public StandardFragment () {}
    
    /**
     * Set up this object inside a JSP
     *
     * Note on String.intern ().
     * Because this object will be initialised from a String literal, html will
     * be 'intern' which means there is no need to intern it here.
     *
     * ('Interning' a String means invoking intern () and using the String reference
     * that results. The reference comes from a JVM-wide shared pool of Strings )
     *
     * (01-Nov-20 JDB: Tested this in a JSP with a couple of Strings and it's correct.)
     *
     * @param html A <code>String</code> representing the HTML this <code>Fragment</code>
     * is responsible for.
     */
    public void setHtml ( String html ) {
        _html = html ;
    }
    
    /**
     * This method is required for testing the intern assumption. It is
     * not part of the Fragment interface.
     */
    private int testGetHtmlIdentityHashCode () {
        return System.identityHashCode ( _html ) ;
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
     * A <code>StandardFragment</code> does not contain any controlling logic.
     */
    public void control ( InvocationContext icb ) throws Exception {}
    
    /**
     * Invoked inside the JSP to render content into the page buffer. All control
     * logic has already been executed by the time this method is invoked. The method
     * is responsible for rendering the view (HTML) from the model (session data) and
     * nothing more. In particular no http redirection should be attempted during
     * the rendering phase.
     *
     * A <code>StandardFragment</code> performs placeholder replacements with the
     * help of its container which provides two services
     *
     *  a) A list of placeholders
     *  b) Replacement Strings for placeholders
     *
     * The <code>StandardFragment</code> optimises the list of placeholders the first
     * time it is invoked by caching a list of applicable placeholders and thereafter
     * ignoring all others.
     *
     * Because this is invoked at serve time in a multithreaded environment we must
     * obtain a lock before proceeding.
     *
     * @throws Exception, otherwise it would have to throw every exception that every
     * method of every object could throw.
     *
     * @param icb A <code>InvocationContext</code> supplied by the JSP.
     */
    public void render ( InvocationContext icb ) throws Exception {
        
        if ( ( _html == null ) && ( _parts == null ) )
            return ;
        
        if ( _parts == null ) {
            synchronized ( this ) {
                /* another thread may have already done the work */
                if ( _parts == null ) {
                    cacheApplicablePlaceholders () ;
                    buildPartsArray () ;
                    deleteCache () ;
                    _html = null ;
                }
            }
        }
        
        icb.getJspWriter ().write ( buildHtml () ) ;
    }
    
    
    /**
     * The last size of the built html is used as the estimate
     * for the size of the next
     */
    private int sizeEstimate = 0 ;
    
    
    /**
     * Build the text from dynamic replacement values of the placeholder codes
     * and the static html.
     * <p>
     * This method uses an array of placeholder codes and static html. If p1-text, p2-text
     * are placeholder texts for placeholder codes p1 and p2, then this html
     * <p>
     * <pre>
     * Clash of the p1-text, featuring p2-text. Do not miss the p1-text.
     * </pre>
     * <p>
     * results in this array:
     * <p>
     * <pre>
     * [ 0 ] = &quot;Clash of the &quot;
     * [ 1 ] = p1
     * [ 2 ] = &quot;, featuring &quot;
     * [ 3 ] = p2
     * [ 4 ] = &quot;. Do not miss the &quot;
     * [ 5 ] = p1
     * [ 6 ] = &quot;.&quot;
     * </pre>
     * <p>
     */
    private String buildHtml () {
        
        StringBuffer sb = new StringBuffer ( sizeEstimate ) ;
        
        for ( int i = 0 ; i < _parts.length ; i++ )
            sb.append ( _parts [ i ] .toString () ) ;
        
        sizeEstimate = sb.length () ;
        
        return sb.toString () ;
    }
    
    
    private Object [] _parts ;
    
    
    /**
     * Make an array of placeholder codes and static HTML.
     * <p>
     * From the list of applicable placeholder texts and placeholder codes and the html
     * this object is responsible for making dynamic versions of, calculate the
     * parts array.
     * <p>
     * Each element of the array is either a <code>String</String> or a <code>Placeholder</code>.
     * <p>
     * The placeholder code with the longest corresponding placeholder text is used in preference
     * to any other applicable placeholder.
     */
    private void buildPartsArray () {
        
        List partsList = new LinkedList () ;
        
        partsList.add ( _html ) ;
        
        CollectionsUtilities.expandList ( partsList, new PlaceholderTextFinder () ) ;
        
        _parts = partsList.toArray () ;
        
        /* Save memory */
        for ( int i = 0 ; i < _parts.length ; i++ )
            if ( _parts [ i ] instanceof String )
                _parts [ i ] = ( ( String ) _parts [ i ] ).intern () ;
        
    }
    
    
    private class PlaceholderTextFinder implements Transformer {
        
        public Object transform ( Object input ) {
            
            if ( input instanceof PlaceholderCode )
                return null ;
            
            String inputStr = ( String ) input ;
            int placeholderIX = getLargestPlaceholderTextIndex ( inputStr ) ;
            
            if ( placeholderIX == -1 )
                return null ;
            
            /*
             * Split the input into one of
             *
             *  placeholder
             *  html, placeholder
             *  placeholder, html
             *  html, placeholder, html
             */
            List result = new LinkedList () ;
            
            int pos = inputStr.indexOf ( _placeholders [ placeholderIX ] ) ;
            
            if ( pos > 0 )
                result.add ( inputStr.substring ( 0, pos ) ) ;
            
            result.add ( new PlaceholderCode ( _placeholderKeys [ placeholderIX ] ) ) ;
            
            int lastCharPos = pos + _placeholders [ placeholderIX] .length () - 1 ;
            
            if (  lastCharPos < inputStr.length () - 1 )
                result.add ( inputStr.substring ( lastCharPos + 1 ) ) ;
            
            return result ;
            
        }
    }
    
    
    /**
     * Remember a placeholder code and override <code>toString</code>
     * to return the dynamic replacement text for a placeholder code.
     */
    private class PlaceholderCode {
        
        final String code ;
        
        PlaceholderCode ( String code ) {
            
            this.code = code.intern () ;
        }
        
        public String toString () {
            
            return _container.getPlaceholderReplacementRecursively ( code  ) ;
        }
    }
    
    
    /**
     * Return the index of the largest placeholder text in the
     * target string.
     * <p>
     * Return -1 if no occurance of any placeholder text.
     * <p>
     * Consider<br>
     * <br>
     * Initial html: 01012345<br>
     * Placeholder text: 01<br>
     * Placeholder text: 012345<br>
     * <br>
     * Both 01 and 012345 will be found<br>
     * <br>
     * Consider<br>
     * <br>
     * Initial html: 01012345<br>
     * Placeholder text: 010<br>
     * Placeholder text: 012345<br>
     * <br>
     * Only 012345 will be found.
     *
     * @param A <code>String</code> to search for placeholder text in.
     * @return index of largest placeholder text or -1
     */
    private int getLargestPlaceholderTextIndex ( String target ) {
        
        for ( int i = 0 ; i < _placeholders.length ; i++ ) {
            
            int pos = target.indexOf ( _placeholders [ i ] ) ;
            
            if ( pos != -1 )
                return i ;
        }
        
        return -1 ;
    }
    
    
    /**
     * The parent container may have many inapplicable placeholders i.e. placeholders
     * which will never result in any replacement because the placeholder text is not in
     * the html managed by this <code>StandardFragment</code>.
     * <p>
     * This method is invoked once per instance to cache the applicable placeholders
     */
    private void cacheApplicablePlaceholders () {
        
        Properties p = _container.getPlaceholders () ;
        
        /* first get the applicable placeholders... */
        List placeholderKeys = new LinkedList () ;
        Iterator it = p.keySet ().iterator () ;
        while ( it.hasNext () ) {
            String placeholder = (String) it.next () ;
            /* If the text to replace exists then it's applicable */
            if ( _html.indexOf ( p.getProperty ( placeholder ) ) > -1 )
                placeholderKeys.add ( placeholder ) ;
        }
        
        /*
         * The applicable placeholders are stored in order determined by
         * the length of the corresponding placeholder text. The placeholders
         * with longer placeholder text come earlier. This is used to do
         * replacement with greedy matching.
         */
        Collections.sort ( placeholderKeys, getLongestFirstComparator ( p ) ) ;
        
        /*
         * ...then store the applicable placeholders in an array.
         */
        _placeholderKeys = new String [ placeholderKeys.size () ] ;
        _placeholders = new String [ placeholderKeys.size () ] ;
        it = placeholderKeys.iterator () ;
        for ( int i = 0 ; it.hasNext () ; i++ ) {
            String placeholder = (String) it.next () ;
            _placeholderKeys [ i ] = placeholder ;
            _placeholders [ i ] = p.getProperty ( placeholder ) ;
        }
        
    }
    
    
    private void deleteCache () {
        
        _placeholderKeys = null ;
        _placeholders = null ;
    }
    
    
    /**
     * public for testing.
     */
    public Comparator getLongestFirstComparator ( Properties p ) {
        
        return new LongestFirst ( p ) ;
    }
    
    
    private class LongestFirst implements Comparator {
        
        Properties p ;
        
        LongestFirst ( Properties p ) {
            this.p = p ;
        }
        
        
        public int compare ( Object obj1, Object obj2 ) {
            
            String str1 = ( String ) obj1 ;
            String str2 = ( String ) obj2 ;
            
            int lengthDiff = p.getProperty ( str2 ).length () - p.getProperty ( str1 ).length () ;
            
            if ( lengthDiff != 0 )
                return lengthDiff ;
            
            return str1.compareTo ( str2 ) ;
        }
    }
    
    
    public void recycle () {}

    
    /**
     * This method is used for testing only. It is accessed via reflection.
     *
     * @return A <code>Properties</code> object with all the applicable placeholders
     * as a pair of arrays. Recalculated on each invocation to ease testing.
     */
    private Map testGetApplicablePlaceholders () {
        
        cacheApplicablePlaceholders () ;
        Map m = new HashMap () ;
        m.put ( "keys", _placeholderKeys ) ;
        m.put ( "values", _placeholders ) ;
        return m ;
    }
    
    /**
     * This method is used for testing only. It is accessed via reflection.
     *
     * @return A <code>String</code> object with all the applicable placeholders
     * replaced with the corresponding value provided by the parent container.
     * The applicable placeholders are recalculated on each invocation to ease testing.
     */
	private String testGetReplacedPlaceholders () {
        cacheApplicablePlaceholders () ;
        buildPartsArray () ;
        
        return buildHtml () ;
    }
    
}
