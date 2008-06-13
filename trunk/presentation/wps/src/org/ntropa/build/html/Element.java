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
 * Element.java
 *
 * Created on 15 October 2001, 17:00
 */

package org.ntropa.build.html;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.ntropa.build.jsp.FinderSet;
import org.ntropa.build.jsp.JspSerializable;
import org.ntropa.build.jsp.JspUtility;

/**
 * An <code>Element</code> maintains a list of <code>Fragment</code>s and
 * <code>ServerActiveHtml</code>s.
 *
 * @author  jdb
 * @author  rj
 * @version $Id: Element.java,v 1.17 2002/11/30 23:03:09 jdb Exp $
 */
public class Element extends GenericDeserializable
implements Deserializable, JspSerializable {
    
    // TODO: Revert to ntropa class names.
    //private static final String DEFAULT_COMPONENT_CLASS = "org.ntropa.runtime.sao.StandardElement" ;
    private static final String DEFAULT_COMPONENT_CLASS = "com.studylink.sao.StandardElement" ;
    
    // TODO: Revert to ntropa class names.
    //private static final String DEFAULT_COMPONENT_TYPE = "org.ntropa.runtime.sao.AbstractElement" ;
    private static final String DEFAULT_COMPONENT_TYPE = "com.studylink.sao.AbstractElement" ;
    
    protected PatternMatcher _matcher ;
    
    protected PatternMatcherInput _input ;
    
    private static final String SYSTEM_PLACEHOLDER_PATTERN =
     /*
      * Examples of string we want to match.
      * <!-- name="news" -->
      * <!-- name="/news" -->
      * <!-- name="news" template="news-tmpl" -->
      * <!-- element="item-1" placeholder-date="2001-November-8" -->
      * <!-- element="/item-1" -->
      * <!-- element="item-1" placeholder-date="2001-November-8"  placeholder-a = "b" -->
      * <!-- name="news" placeholder-date="2001-November-8" -->
      *
      *
      */
    //  "<!--\\s*(([\\w\\-]+)\\s*=\\s*\"([^\"]+)\"\\s*)+[^>]*-->" ;
    "\\$\\$([\\w\\-]+)\\$\\$" ;
    static protected Pattern _pattern ;
    static protected MalformedPatternException _patternException ;
    
    static protected Pattern _parsePattern ;
    
    /* Static initialiser */
    static {
        PatternCompiler compiler = new Perl5Compiler ();
        
        try {
            _pattern = compiler.compile (
            SYSTEM_PLACEHOLDER_PATTERN,
            Perl5Compiler.CASE_INSENSITIVE_MASK |
            Perl5Compiler.READ_ONLY_MASK );
        }
        catch(MalformedPatternException e) {
            /* Exceptions can not be thrown from static initialisers */
            _pattern = null ;
            _patternException = e ;
        }
        
    }
    
    private static final String MSG_ADD_ELEMENT_NOT_ALLOWED =
    "An Element is not allowed to be included within another Element." ;
    
    private String _name ;
    
    /* It is normal for an Element to have children so we initialise the list here */
    private List _children = new LinkedList () ;
    
    
    public Element ( String name ) {
        
        if ( name == null )
            throw new IllegalArgumentException ( "Attempt to construct Element with a null 'name' argument" ) ;
        if ( name.equals ( "" ) )
            throw new IllegalArgumentException ( "Attempt to construct Element with an empty 'name' argument" ) ;
        
        _name = name.toLowerCase () ;
        
    }
    
    public Element ( String name, Properties placeholders ) {
        
        this( name ) ;
        
        if ( placeholders != null )
            setPlaceholders ( placeholders ) ;
    }
    
    
    /** Prevent no-arg construction */
    private Element () {}
    
    /**
     * @return Returns the name of the <code>Element</code>
     */
    public String getName () {
        return _name ;
    }
    
    
    /**
     * Return the value of this <code>Element</code> which is the
     * template text.
     * public Fragment getValue () {
     * return new Fragment ( _value ) ;
     * }
     */
    
    /**
     * Return the list of children maintained by this <code>Element</code>
     *
     * Note: This is the actual list used by the object so changing the
     * list or its items will change this object.
     *
     * @return Returns a list of children which may be a mixture of <code>Fragments</code>
     * , <code>ServerActiveHtmls</code> and <code>Placeholders</code>.
     */
    public List getChildren () {
        return _children ;
    }
    
    
    /**
     * Return a list of <code>ServerActiveHtmls</code> for the list of children
     * <p>
     * @return A <code>List</code> containing just the children of this <code>Element</code>
     * which are <code>ServerActiveHtmls</code>
     */
    public List getServerActiveHtmls () {
        
        List result = new LinkedList () ;
        for ( int i = 0 ; i < _children.size () ; i++ )
            if ( _children.get ( i ) instanceof ServerActiveHtml )
                result.add ( _children.get ( i ) ) ;
        
        return result ;
    }
    
    
    public int hashCode () {
        return 37 * getName ().hashCode () + getPlaceholders ().hashCode () ;
    }
    
    /**
     * Return true if the objects are the same class and
     * have the same content.
     */
    public boolean equals ( Object obj ) {
        
        if ( obj == null )
            return false ;
        
        if ( ! ( obj instanceof Element ) )
            return false ;
        
        Element el = (Element) obj ;
        
        if ( ! el.getName ().equals ( getName () ) )
            return false ;
        
        if ( ! el.getPlaceholders ().equals ( getPlaceholders () ) )
            return false ;
        
        if ( ! el.getChildren ().equals ( getChildren () ) )
            return false ;
        
        return true ;
    }
    
    public String toString () {
        
        return new ToStringBuilder (this, ToStringStyle.MULTI_LINE_STYLE ).
        append ( "_name", _name).
        append ( "_children", _children).
        append ( "placeholders", getPlaceholders ()).
        append ( "_matcher", _matcher).
        append ( "_input", _input).
        toString ();
        
    }
    
    /* ---- Implementation of JspSerializable --- */
    
    /*
     * Already implemented
    public List getChildren () {
        return getElements () ;
    }
     */
    
    /**
     * Return the full name of the class implementing the
     * interface within the current page context and dependent on the type
     * of the underlying class.
     *
     * @param finderSet A <code>FinderSet</code> used to look up information based
     * on a certain context. The contect is expected to be the current page location.
     *
     * @return A <code>String</code> representing the full name of the class
     * implementing the interface.
     */
    /*
     * 02-1-18 jdb
    public String getComponentClassName (FinderSet finderSet) {
        if ( finderSet == null )
            throw new IllegalArgumentException (
            "Attempt to invoke Element.getComponentClassName with null finderSet" ) ;
        //FIXME: temporary.
        return "org.ntropa.runtime.sao.StandardElement" ;
    }
     **/
    
    public String getComponentTypeName () {
        return DEFAULT_COMPONENT_TYPE ;
    }
    
    /**
     * Create the code to set up this object. See interface JspSerializable for details.
     *
     * @param objName The name of the object to create and initialise.
     *
     * @param buffer A <code>StringBuilder</code> to append set up code to
     *
     * @param finderSet A <code>FinderSet</code> used to look up information based
     * on a certain context. The context is expected to be the current page location.
     */
    public void getSetUpCode ( String objName, StringBuilder buffer, FinderSet finderSet ) {
        JspUtility.getObjectCreationCode ( objName, DEFAULT_COMPONENT_CLASS, buffer ) ;
        buffer.append ( objName  +".setName ( \"" + getName () + "\" ) ;\n" ) ;
        super.getSetUpCode ( objName, buffer, finderSet ) ;
    }
    
    /* --- Implementation of Deserializable --- */
    
    /**
     * A method to add a <code>Fragment</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * @param fragment The <code>Fragment</code> to add.
     */
    public void add ( Fragment fragment ) {
        add ( fragment, _children.size () ) ;
    }
    
    
    /**
     * A method to add a <code>Fragment</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * @param fragment The <code>Fragment</code> to add.
     * @param index The index to add the new child at.
     */
    public void add ( Fragment fragment, int index ) {
        //System.out.println ("[Element] add Fragment");
        
        Properties p = getSystemPlaceHolders ( fragment ) ;
        
        Iterator it = p.keySet ().iterator () ;
        while ( it.hasNext ()) {
            String key = (String) it.next () ;
            String value = p.getProperty ( key ) ;
            
            setPlaceholder ( key, value ) ;
        }
        
        /*
         * if the child before the index this new Fragment is being added
         * at is a Fragment then merge to that otherwise add a new object.
         *
         * There is no previous Fragment if the required index is 0.
         */
        
        if ( ( _children.size () > 0 ) && ( index > 0 ) ) {
            Object o = _children.get ( index - 1 ) ;
            if ( o instanceof Fragment ) {
                ((Fragment) o).add ( fragment ) ;
                return ;
            }
        }
        
        _children.add ( index, fragment ) ;
    }
    
    
    /**
     * A method to add a <code>ServerActiveHtml</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * @param child The <code>ServerActiveHtml</code> to add.
     */
    public void add ( ServerActiveHtml child ) {
        
        add ( child, _children.size () ) ;
    }
    
    
    /**
     * A method to add a <code>ServerActiveHtml</code> object to a deserializable object.
     *
     * @param child The <code>ServerActiveHtml</code> to add.
     * @param index The index to add the new child at.
     */
    public void add ( ServerActiveHtml child, int index ) {
        
        _children.add ( index, child ) ;
    }
    
    
    /**
     * A method to add an <code>Element</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * @param child The <code>Element</code> to add.
     */
    public void add (Element child) {
        throw new UnsupportedOperationException (
        MSG_ADD_ELEMENT_NOT_ALLOWED +
        "\nParent Element: " + getName () + ", disallowed child Element: " + child.getName ()
        ) ;
    }
    
    /**
     * A method to add a <code>Placeholder</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * @param child The <code>Placeholder</code> to add.
     */
    public void add (Placeholder child) {
        
        add ( child, _children.size () ) ;
    }
    
    
    /**
     * A method to add a <code>Placeholder</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     *
     * @param child The <code>Placeholder</code> to add.
     * @param index The index to add the new child at.
     */
    public void add ( Placeholder child, int index ) {
        
        _children.add ( index, child ) ;
    }
    
    
    /**
     * A method to replace a child <code>ServerActiveHtml</code> object
     * with a list of <code>ServerActiveHtml</code>, <code>Fragment</code>
     * and <code>Placeholder</code> objects.
     *
     * @param child The <code>ServerActiveHtml</code> object to replace
     * @param replacements The list of objects to replace child with
     */
    public void replace ( ServerActiveHtml child, List replacements ) {
        
        if ( child == null )
            throw new IllegalArgumentException ( "Attempt to invoke replace with null child" ) ;
        
        if ( replacements == null )
            throw new IllegalArgumentException ( "Attempt to invoke replace with null list of replacements" ) ;
        if ( replacements.size () == 0 )
            throw new IllegalArgumentException ( "Attempt to invoke replace with empty list of replacements" ) ;
        
        /* Locate the child to be replaced */
        int childIndex = -1 ;
        for ( int i = 0 ; i < _children.size () ; i++ )
            if ( _children.get ( i ) == child ) {
                childIndex = i ;
                break ;
            }
        
        if ( childIndex == -1 )
            throw new IllegalArgumentException ( "Attempt to invoke replace with child not in list of children" ) ;
        
        /* Remove the object we are replacing */
        _children.remove ( childIndex ) ;
        
        /*
         * The index to add the new children at.
         */
        int addIndex = childIndex ;
        
        Iterator it = replacements.iterator () ;
        for ( int i = 0 ; i < replacements.size () ; i++ ) {
            Object o = replacements.get ( i ) ;
            if ( o instanceof ServerActiveHtml ) {
                add ( ( ServerActiveHtml ) o, addIndex ) ;
                addIndex++ ;
            }
            else if ( o instanceof Fragment ) {
                
                /*
                 * If the Fragment is merged to an existing Fragment then
                 * do not advance the addition index.
                 */
                int childCount = _children.size () ;
                add ( ( Fragment ) o, addIndex ) ;
                if ( _children.size () > childCount )
                    addIndex++ ;
                
            }
            else if ( o instanceof Placeholder ) {
                add ( ( Placeholder ) o, addIndex ) ;
                addIndex++ ;
            }
            else {
                throw new IllegalArgumentException (
                "Attempt to replace with list that includes an unhandled object type: " + o.getClass ().getName () ) ;
            }
        }
    }
    
    /**
     * Search for system placeholders in a fragment and return a <code>Properties</code>
     * object representing the placeholders and the corresponding keys
     *
     * Example:
     *
     * "<h1>$$TheDate$$</h1><hr><p align=\"center\">$$Num-of-Ops$$</p>"
     *
     * yields
     *
     * thedate:     $$DateTime$$
     * num-of-ops:  $$Num-if-Ops$$
     *
     * @param fragment a <code>Fragment</code> to parse for system placeholders
     * @return A <code>Properties</code> object storing the keys and values.
     */
    private Properties getSystemPlaceHolders ( Fragment fragment ) {
        
        Properties p = new Properties () ;
        PatternMatcher matcher  = new Perl5Matcher ();
        PatternMatcherInput input = new PatternMatcherInput ( fragment.getValue () );
        
        while ( matcher.contains ( input, _pattern ) ) {
            
            MatchResult result = matcher.getMatch ();
            String value = result.toString () ;
            String key = getSystemPlaceholderKey (value) ;
            p.setProperty (key,value) ;
            /*
            System.out.println ( "*** MATCH ***\n" + result.toString () );
            System.out.println ( "BEGIN OFFSET: " + result.beginOffset ( 0 ) );
            System.out.println ( "END OFFSET:   " + result.endOffset ( 0 ) );
             */
            
            /*
                if ( true ) {
                    int groupCnt = result.groups ();
                    System.out.println ("Number of Groups: " + groupCnt);
             
                    // group 0 is the entire matched string.
                    for ( int groupIdx = 1 ; groupIdx < groupCnt ; groupIdx++ ) {
                        String group = result.group ( groupIdx ) ;
             
                        System.out.println ("Group index " + groupIdx + ":" + group );
                        //System.out.println ("Begin: " + result.begin (group));
                        //System.out.println ("End: " + result.end (group));
                    }
                }
             */
            
        }
        return p ;
    }
    
    /**
     * Strip $$ from ends of string while checking this is possible while leaving a useful String
     */
    private String getSystemPlaceholderKey (String value) {
        if (value == null )
            return null ;
        if (value.length () < 5)
            return null ;
        if (!value.startsWith ("$$"))
            return value ;
        if (!value.endsWith ("$$"))
            return value ;
        return value.substring (2,value.length ()-2).toLowerCase () ;
        
    }
    
    /**
     * Debugging support
     */
    private String easyReadingList ( List list ) {
        StringBuffer sb = new StringBuffer () ;
        
        for ( int i = 0 ; i < list.size () ; i++ ) {
            sb.append ( "" + i + ": " ) ;
            Object o = list.get ( i ) ;
            if ( o instanceof ServerActiveHtml )
                sb.append ( o.hashCode () + " " + ( ( ServerActiveHtml ) o ).getName () + "\n" ) ;
            else if ( o instanceof Fragment )
                sb.append ( o.hashCode () + " " + "Fragment: " +  ( ( Fragment ) o ).getValue () + "\n" ) ;
            else
                sb.append ( "" + o.hashCode () + "\n" ) ;
        }
        
        return sb.toString () ;
    }
    
}
