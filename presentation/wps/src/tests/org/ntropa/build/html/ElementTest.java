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
 * ElementTest.java
 *
 * Created on 16 October 2001, 18:21
 */

package tests.org.ntropa.build.html;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.html.Element;
import org.ntropa.build.html.Fragment;
import org.ntropa.build.html.ServerActiveHtml;
import org.ntropa.build.html.ServerActiveHtmlException;


/**
 *
 * @author  jdb
 * @version $Id: ElementTest.java,v 1.6 2002/03/03 22:39:08 jdb Exp $
 */
public class ElementTest extends TestCase {
    
    final Fragment SIMPLE_FRAGMENT = new Fragment ( "<hr>" ) ;
    
    /** Creates new ElementTest */
    public ElementTest ( String testName ) {
        super(testName);
    }
    
    
    /* Comments copied from junit.framework.TestSuite. */
    
    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests.
     * It runs a collection of test cases.
     *
     * This constructor creates a suite with all the methods
     * starting with "test" that take no arguments.
     */
    public static Test suite () {
        
        TestSuite suite = new TestSuite ( ElementTest.class );
        return suite;
    }
    
    // protected void setUp () throws Exception {}
    
    /*
    public void testPlaceholderDetection () throws ServerActiveHtmlException {
     
        StringBuffer aList = new StringBuffer () ;
     
        int ATTRIBUTE_COUNT = 10 ;
     
        for ( int i = 1 ; i <= ATTRIBUTE_COUNT ; i++ )
            aList.append ( "placeholder-name-" + i + "=" +"\"value-" + i + "\"" + "  " ) ;
     
        Element el = new Element ( SIMPLE_FRAGMENT, aList.toString () ) ;
     
        Map pl = el.getPlaceholders () ;
     
        assertEquals ( "The number of parsed placeholders was wrong", ATTRIBUTE_COUNT, pl.size () ) ;
     
        for ( int i = 1 ; i < ATTRIBUTE_COUNT ; i++ ) {
            String name = "name-" + i ;
            assertTrue ( "The placeholder was missing: " + name, pl.containsKey ( name ) ) ;
     
            String value = (String) pl.get ( name ) ;
     
            String expectedValue = "value-" + i ;
            assertEquals ( "The value for the placeholder was wrong: " + name, expectedValue, value ) ;
     
        }
     
        //System.out.println ("Element:" + el );
     
    }
     */
    
    public void testDoublePlaceholder () throws ServerActiveHtmlException {
        
        Element el = new Element ( "my-element" ) ;
        el.setPlaceholder ( "logged-on", "you are logged on" ) ;
        el.setPlaceholder ( "logged-ON", "you are logged on" ) ;
        
        Map pl = el.getPlaceholders () ;
        
        assertEquals ( "The number of parsed placeholders was wrong", 1, pl.size () ) ;
        assertEquals ( "The placeholder value was wrong", "you are logged on", pl.get ( "logged-on" ) ) ;
        
    }
    
    public void testEquals () throws ServerActiveHtmlException {
        
        String MSG = "equals () broken" ;
        
        Properties placeholders = new Properties () ;
        placeholders.setProperty ( "time", "17:55 hrs" ) ;
        placeholders.setProperty ( "date", "2001-October-17" ) ;
        
        Element elA, elB ;
        
        // group 1
        elA = new Element ( "my-element" ) ;
        elA.add ( SIMPLE_FRAGMENT ) ;
        elB = elA ;
        assertEquals ( MSG, elA, elB ) ;
        
        elB = new Element ( "my-element" ) ;
        elB.add ( SIMPLE_FRAGMENT ) ;
        assertEquals ( MSG, elA, elB ) ;
        
        // group 2
        elA = new Element ( "my-element", placeholders ) ;
        elB = elA ;
        assertEquals ( MSG, elA, elB ) ;
        
        elB = new Element ( "my-element", placeholders ) ;
        assertEquals ( MSG, elA, elB ) ;
        
        // group 3
        elA = new Element ( "my-element", placeholders ) ;
        elB = new Element ( "my-element" ) ;
        if ( elA.equals ( elB ) )
            fail  ( MSG + ": group 3\n" + elA + "\n" + elA ) ;
        
        Fragment SIMPLE_FRAGMENT_PLUS = new Fragment ( SIMPLE_FRAGMENT.getValue () + "&nbsp;") ;
        
        elA = new Element ( "my-element") ;
        elA.add ( SIMPLE_FRAGMENT ) ;
        elB = new Element ( "my-element"  ) ;
        elB.add ( SIMPLE_FRAGMENT_PLUS ) ;
        if ( elA.equals ( elB ) )
            fail  ( MSG + ": group 3\n" + elA + "\n" + elA ) ;
        
        elA = new Element ( "my-element", placeholders ) ;
        elA.add ( SIMPLE_FRAGMENT ) ;
        elB = new Element ( "my-element" ) ;
        elB.add ( SIMPLE_FRAGMENT_PLUS ) ;
        if ( elA.equals ( elB ) )
            fail  ( MSG + ": group 3\n" + elA + "\n" + elA ) ;
        
        // group 4
        elA = new Element ( "name-1" ) ;
        elB = new Element ( "name-2" ) ;
        if ( elA.equals ( elB ) )
            fail  ( MSG + ": group 4\n" + elA + "\n" + elA ) ;
        
    }
    
    public void testSystemPlaceHolders () {
        Element el = new Element ( "test22" ) ;
        Fragment frag = new Fragment ( "<h1>$$TheDate$$</h1><hr><p align=\"center\">$$Num-of-Ops$$</p>" ) ;
        el.add (frag) ;
        
        /* Had to change this from $$Dat e$$ (a space added to prevent CVS doing a keyword replacement */
        Properties placeholders = new Properties () ;
        placeholders.setProperty ( "thedate", "$$TheDate$$" ) ;
        placeholders.setProperty ( "num-of-ops", "$$Num-of-Ops$$" ) ;
        
        Properties pl = el.getPlaceholders () ;
        assertEquals ( "The number of parsed placeholders was wrong", 2, pl.size () ) ;
        assertEquals ( "The placeholders were different\n" + placeholders + "\n" + pl, placeholders, pl ) ;
        
        // testing empty fragment
        el = new Element ( "test22" ) ;
        frag = new Fragment ( "" ) ;
        el.add (frag) ;
        pl = el.getPlaceholders () ;
        assertEquals ( "The number of parsed placeholders was wrong", 0, pl.size () ) ;
        
        // testing $ range
        el = new Element ( "test22" ) ;
        frag = new Fragment ( "$100-$200" ) ;
        el.add (frag) ;
        pl = el.getPlaceholders () ;
        assertEquals ( "The number of parsed placeholders was wrong", 0, pl.size () ) ;
        
        // test space foils match
        el = new Element ( "test22" ) ;
        frag = new Fragment ( "$$100 20$$dd0" ) ;
        el.add (frag) ;
        pl = el.getPlaceholders () ;
        assertEquals ( "The number of parsed placeholders was wrong", 0, pl.size () ) ;
    }
    
    /*
     * Test replacement of a child with one or more children
     */
    public void testReplacement () throws ServerActiveHtmlException {
        
        Element el = new Element ( "test-replacement" ) ;
        
        ServerActiveHtml sah = new ServerActiveHtml ( "sah-1" ) ;
        
        el.add ( sah ) ;
        
        
        /*
         * Test null list is rejected
         */
        try {
            el.replace ( sah, null ) ;
            fail ( "null list was accepted by replace.") ;
        }
        catch ( IllegalArgumentException e ) {}
        
        /*
         * Test empty list is rejected
         */
        try {
            el.replace ( sah, Collections.EMPTY_LIST ) ;
            fail ( "Empty list was accepted by replace.") ;
        }
        catch ( IllegalArgumentException e ) {}
        
        /*
         * Test replacement of non-child sah is rejected
         */
        List replacements = new LinkedList () ;
        ServerActiveHtml replacementSah = new ServerActiveHtml ( "sah-replacement" ) ;
        replacements.add ( replacementSah ) ;
        try {
            el.replace ( new ServerActiveHtml ( "not-a-child" ), replacements ) ;
            fail ( "Alien sah was accepted for replacement by replace.") ;
        }
        catch ( IllegalArgumentException e ) {}
        
        /*
         * Test null sah is rejected
         */
        try {
            el.replace ( null, replacements ) ;
            fail ( "null sah was accepted by replace.") ;
        }
        catch ( IllegalArgumentException e ) {}
        
       /*
        * Test an acceptable replacement works
        */
        el.replace ( sah, replacements ) ;
        checkObjectsAreIndentical (
        "testReplacement (acceptable replacement 1)",
        replacements,
        el.getChildren () ) ;
        
        /*
         * More complex tests.
         */
        
        /*
         * Replacement at head of list.
         */
        
        el = new Element ( "test-replacement" ) ;
        
        ServerActiveHtml sah1 = new ServerActiveHtml ( "sah-1" ) ;
        ServerActiveHtml sah2 = new ServerActiveHtml ( "sah-2" ) ;
        ServerActiveHtml sah3 = new ServerActiveHtml ( "sah-3" ) ;
        
        el.add ( sah1 ) ;
        el.add ( sah2 ) ;
        el.add ( sah3 ) ;
        
        replacements = new LinkedList () ;
        ServerActiveHtml replacementSah1 = new ServerActiveHtml ( "sah-replacement-1" ) ;
        replacements.add ( replacementSah1 ) ;
        ServerActiveHtml replacementSah2 = new ServerActiveHtml ( "sah-replacement-2" ) ;
        replacements.add ( replacementSah2 ) ;
        
        el.replace ( sah1, replacements ) ;
        
        List expectedObjList = new LinkedList () ;
        expectedObjList.add ( replacementSah1 ) ;
        expectedObjList.add ( replacementSah2 ) ;
        expectedObjList.add ( sah2 ) ;
        expectedObjList.add ( sah3 ) ;
        
        checkObjectsAreIndentical (
        "testReplacement (replacement at head of list)",
        expectedObjList,
        el.getChildren () ) ;
        
        /*
         * Replacement in middle of list.
         */
        
        el = new Element ( "test-replacement" ) ;
        
        sah1 = new ServerActiveHtml ( "sah-1" ) ;
        sah2 = new ServerActiveHtml ( "sah-2" ) ;
        sah3 = new ServerActiveHtml ( "sah-3" ) ;
        
        el.add ( sah1 ) ;
        el.add ( sah2 ) ;
        el.add ( sah3 ) ;
        
        replacements = new LinkedList () ;
        replacementSah1 = new ServerActiveHtml ( "sah-replacement-1" ) ;
        replacementSah2 = new ServerActiveHtml ( "sah-replacement-2" ) ;
        
        replacements.add ( replacementSah1 ) ;
        replacements.add ( replacementSah2 ) ;
        
        el.replace ( sah2, replacements ) ;
        
        expectedObjList = new LinkedList () ;
        expectedObjList.add ( sah1 ) ;
        expectedObjList.add ( replacementSah1 ) ;
        expectedObjList.add ( replacementSah2 ) ;
        expectedObjList.add ( sah3 ) ;
        
        checkObjectsAreIndentical (
        "testReplacement (replacement in middle of list)",
        expectedObjList,
        el.getChildren () ) ;
        
        /*
         * Replacement at tail of list.
         */
        
        el = new Element ( "test-replacement" ) ;
        
        sah1 = new ServerActiveHtml ( "sah-1" ) ;
        sah2 = new ServerActiveHtml ( "sah-2" ) ;
        sah3 = new ServerActiveHtml ( "sah-3" ) ;
        
        el.add ( sah1 ) ;
        el.add ( sah2 ) ;
        el.add ( sah3 ) ;
        
        replacements = new LinkedList () ;
        replacementSah1 = new ServerActiveHtml ( "sah-replacement-1" ) ;
        replacementSah2 = new ServerActiveHtml ( "sah-replacement-2" ) ;
        
        replacements.add ( replacementSah1 ) ;
        replacements.add ( replacementSah2 ) ;
        
        el.replace ( sah3, replacements ) ;
        
        expectedObjList = new LinkedList () ;
        expectedObjList.add ( sah1 ) ;
        expectedObjList.add ( sah2 ) ;
        expectedObjList.add ( replacementSah1 ) ;
        expectedObjList.add ( replacementSah2 ) ;
        
        checkObjectsAreIndentical (
        "testReplacement (replacement at tail of list)",
        expectedObjList,
        el.getChildren () ) ;
        
        /*
         * Replacement at head of list with varied list.
         */
        
        el = new Element ( "test-replacement" ) ;
        
        sah1 = new ServerActiveHtml ( "sah-1" ) ;
        sah2 = new ServerActiveHtml ( "sah-2" ) ;
        sah3 = new ServerActiveHtml ( "sah-3" ) ;
        
        el.add ( sah1 ) ;
        el.add ( sah2 ) ;
        el.add ( sah3 ) ;
        
        replacements = new LinkedList () ;
        replacementSah1 = new ServerActiveHtml ( "sah-replacement-1" ) ;
        replacementSah2 = new ServerActiveHtml ( "sah-replacement-2" ) ;
        Fragment replacementFragment1 = new Fragment ( "frag-replacement-1" ) ;
        Fragment replacementFragment2 = new Fragment ( "frag-replacement-2" ) ;
        
        replacements.add ( replacementSah1 ) ;
        replacements.add ( replacementFragment1 ) ;
        replacements.add ( replacementSah2 ) ;
        replacements.add ( replacementFragment2 ) ;
        
        el.replace ( sah1, replacements ) ;
        
        expectedObjList = new LinkedList () ;
        expectedObjList.add ( replacementSah1 ) ;
        expectedObjList.add ( replacementFragment1 ) ;
        expectedObjList.add ( replacementSah2 ) ;
        expectedObjList.add ( replacementFragment2 ) ;
        expectedObjList.add ( sah2 ) ;
        expectedObjList.add ( sah3 ) ;
        
        checkObjectsAreIndentical (
        "testReplacement (replacement at head of list with varied list)",
        expectedObjList,
        el.getChildren () ) ;
        
        /*
         * Multiple replacements.
         */
        
        el = new Element ( "test-replacement" ) ;
        
        sah1 = new ServerActiveHtml ( "sah-1" ) ;
        sah2 = new ServerActiveHtml ( "sah-2" ) ;
        sah3 = new ServerActiveHtml ( "sah-3" ) ;
        ServerActiveHtml sah4 = new ServerActiveHtml ( "sah-4" ) ;
        ServerActiveHtml sah5 = new ServerActiveHtml ( "sah-5" ) ;
        
        el.add ( sah1 ) ;
        el.add ( sah2 ) ;
        el.add ( sah3 ) ;
        el.add ( sah4 ) ;
        el.add ( sah5 ) ;
        
        replacements = new LinkedList () ;
        replacementSah1 = new ServerActiveHtml ( "sah-replacement-1" ) ;
        replacementSah2 = new ServerActiveHtml ( "sah-replacement-2" ) ;
        replacementFragment1 = new Fragment ( "frag-replacement-1" ) ;
        replacementFragment2 = new Fragment ( "frag-replacement-2" ) ;
        
        replacements.add ( replacementSah1 ) ;
        replacements.add ( replacementFragment1 ) ;
        replacements.add ( replacementSah2 ) ;
        replacements.add ( replacementFragment2 ) ;
        
        el.replace ( sah1, replacements ) ;
        el.replace ( sah3, replacements ) ;
        
        List replacements2 = new LinkedList () ;
        ServerActiveHtml replacement2Sah1 = new ServerActiveHtml ( "sah-replacement-2-1" ) ;
        ServerActiveHtml replacement2Sah2 = new ServerActiveHtml ( "sah-replacement-2-2" ) ;
        Fragment replacement2Fragment1 = new Fragment ( "frag-replacement-2-1" ) ;
        Fragment replacement2Fragment2 = new Fragment ( "frag-replacement-2-2" ) ;
        
        replacements2.add ( replacement2Sah1 ) ;
        replacements2.add ( replacement2Fragment1 ) ;
        replacements2.add ( replacement2Sah2 ) ;
        replacements2.add ( replacement2Fragment2 ) ;
        
        el.replace ( sah5, replacements2 ) ;
        
        expectedObjList = new LinkedList () ;
        expectedObjList.add ( replacementSah1 ) ;
        expectedObjList.add ( replacementFragment1 ) ;
        expectedObjList.add ( replacementSah2 ) ;
        expectedObjList.add ( replacementFragment2 ) ;
        expectedObjList.add ( sah2 ) ;
        expectedObjList.add ( replacementSah1 ) ;
        expectedObjList.add ( replacementFragment1 ) ;
        expectedObjList.add ( replacementSah2 ) ;
        expectedObjList.add ( replacementFragment2 ) ;
        expectedObjList.add ( sah4 ) ;
        expectedObjList.add ( replacement2Sah1 ) ;
        expectedObjList.add ( replacement2Fragment1 ) ;
        expectedObjList.add ( replacement2Sah2 ) ;
        expectedObjList.add ( replacement2Fragment2 ) ;
        
        checkObjectsAreIndentical (
        "testReplacement (multiple replacements)",
        expectedObjList,
        el.getChildren () ) ;
        
    }
    
    
    public void testFragmentMerging () throws ServerActiveHtmlException {
        
        /*
         * In this first test the first replacement Fragment should merge into
         * the Fragment at the head of the list
         */
        Element el = new Element ( "replace-with-merging-of-fragments" ) ;
        
        Fragment frag1 = new Fragment ( "<b>Fragment 1</b>" ) ;
        ServerActiveHtml sah1 = new ServerActiveHtml ( "sah-1" ) ;
        ServerActiveHtml sah2 = new ServerActiveHtml ( "sah-2" ) ;
        
        el.add ( frag1 ) ;
        el.add ( sah1 ) ;
        el.add ( sah2 ) ;
        
        
        List replacements = new LinkedList () ;
        
        Fragment replacementFrag1 = new Fragment ( "<b>Replacement Fragment 1</b>" ) ;
        ServerActiveHtml replacementSah1 = new ServerActiveHtml ( "sah-replacement-1" ) ;
        Fragment replacementFrag2 = new Fragment ( "<b>Replacement Fragment 2</b>" ) ;
        
        replacements.add ( replacementFrag1 ) ;
        replacements.add ( replacementSah1 ) ;
        replacements.add ( replacementFrag2 ) ;
        
        el.replace ( sah1, replacements ) ;
        
        List expectedObjList = new LinkedList () ;
        expectedObjList.add ( frag1 ) ;
        expectedObjList.add ( replacementSah1 ) ;
        expectedObjList.add ( replacementFrag2 ) ;
        expectedObjList.add ( sah2 ) ;
        
        checkObjectsAreIndentical (
        "testFragmentMerging (replacement at head of list)",
        expectedObjList,
        el.getChildren () ) ;
        
        /*
         * In this second test the first replacement Fragment should merge into
         * the Fragment at the tail of the list
         */
        el = new Element ( "replace-with-merging-of-fragments-at-tail" ) ;
        
        frag1 = new Fragment ( "<b>Fragment 1</b>" ) ;
        sah1 = new ServerActiveHtml ( "sah-1" ) ;
        sah2 = new ServerActiveHtml ( "sah-2" ) ;
        
        el.add ( sah1 ) ;
        el.add ( frag1 ) ;
        el.add ( sah2 ) ;
        
        
        replacements = new LinkedList () ;
        
        replacementFrag1 = new Fragment ( "<b>Replacement Fragment 1</b>" ) ;
        replacementSah1 = new ServerActiveHtml ( "sah-replacement-1" ) ;
        replacementFrag2 = new Fragment ( "<b>Replacement Fragment 2</b>" ) ;
        
        replacements.add ( replacementFrag1 ) ;
        replacements.add ( replacementSah1 ) ;
        replacements.add ( replacementFrag2 ) ;
        
        el.replace ( sah2, replacements ) ;
        
        expectedObjList = new LinkedList () ;
        expectedObjList.add ( sah1 ) ;
        expectedObjList.add ( frag1 ) ;
        expectedObjList.add ( replacementSah1 ) ;
        expectedObjList.add ( replacementFrag2 ) ;
        
        checkObjectsAreIndentical (
        "testFragmentMerging (replacement at tail of list)",
        expectedObjList,
        el.getChildren () ) ;
        
        
    }
    
    
    /**
     * This test established the replace method cannot be used with an active ListIterator.
     * The solution is to make a copy of the List of children and use a ListIterator on the
     * new List instead.
     *
     */
    public void testReplacementWithListIterator () throws ServerActiveHtmlException {
        
        Element el = new Element ( "replace-with-ListIterator" ) ;
        
        ServerActiveHtml sah1 = new ServerActiveHtml ( "sah-1" ) ;
        ServerActiveHtml sah2 = new ServerActiveHtml ( "sah-2" ) ;
        ServerActiveHtml sah3 = new ServerActiveHtml ( "sah-3" ) ;
        
        el.add ( sah1 ) ;
        el.add ( sah2 ) ;
        el.add ( sah3 ) ;
        
        List replacements = new LinkedList () ;
        
        ServerActiveHtml replacementSah1 = new ServerActiveHtml ( "sah-replacement-1" ) ;
        ServerActiveHtml replacementSah2 = new ServerActiveHtml ( "sah-replacement-2" ) ;
        
        replacements.add ( replacementSah1 ) ;
        replacements.add ( replacementSah2 ) ;
        
        List children = new LinkedList ( el.getChildren () ) ;
        ListIterator it = children.listIterator () ;
        
        /* move to sah2 */
        it.next () ;
        Object itSah2 = it.next () ;
        if ( itSah2 != sah2 )
            fail ( "Problem with test (1)" ) ;
        /*
         * Do a replacement in the same way as would happen in MarkedUpHtmlParser, i.e. while
         * the ListIterator is in use.
         */
        el.replace ( ( ServerActiveHtml ) itSah2, replacements ) ;
        Object itSah3 = it.next () ;
        if ( itSah3 != sah3 )
            fail ( "Problem with test (2)" ) ;
        
        List expectedObjList = new LinkedList () ;
        expectedObjList.add ( sah1 ) ;
        expectedObjList.add ( replacementSah1 ) ;
        expectedObjList.add ( replacementSah2 ) ;
        expectedObjList.add ( sah3 ) ;
        
        checkObjectsAreIndentical (
        "testReplacementWithListIterator (replacement in middle of list)",
        expectedObjList,
        el.getChildren () ) ;
        
    }
    
    /*
     * Check each list contains the same objects in the same order.
     */
    private void checkObjectsAreIndentical ( String message, List expected, List actual ) {
        
        if ( expected == null )
            fail ( message + ": expected List was null" ) ;
        if ( actual == null )
            fail ( message + ": actual List was null" ) ;
        
        assertEquals ( message + ": expected List had different number of items to actual List", expected.size (), actual.size () ) ;
        
        if ( expected.size () == 0 )
            fail ( message + ": expected List had no items" ) ;
        
        Iterator eIt = expected.iterator () ;
        Iterator aIt = actual.iterator () ;
        while ( eIt.hasNext () ) {
            Object expectedObj = eIt.next () ;
            Object actualObj = aIt.next () ;
            
            /* do not use equals ( Object ) */
            if ( expectedObj != actualObj )
                fail ( message + ": The List did not contain identical objects.\nExpected:\n"
                + easyReadingList ( expected ) + "\nActual:\n" + easyReadingList ( actual ) ) ;
            
        }
        
    }
    
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
