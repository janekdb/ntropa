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
 * PlaceholderTest.java
 *
 * Created on 05 September 2002, 11:54
 */

package tests.org.ntropa.build.html;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.html.Element;
import org.ntropa.build.html.Fragment;
import org.ntropa.build.html.MarkedUpHtmlParser;
import org.ntropa.build.html.Placeholder;
import org.ntropa.build.html.ServerActiveHtml;
import org.ntropa.build.html.ServerActiveHtmlException;



/**
 *
 * @author  jdb
 * @version $Id: PlaceholderTest.java,v 1.3 2002/09/06 17:15:02 jdb Exp $
 */
public class PlaceholderTest extends TestCase {
    
    
    /** Creates new PlaceholderTest */
    public PlaceholderTest( String testName ) {
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
    public static Test suite() {
        
        TestSuite suite = new TestSuite( PlaceholderTest.class );
        return suite;
    }
    
    // protected void setUp () throws Exception {}
    
    public void testBasicUse() {
        
        Placeholder pl = new Placeholder( "required-element-name" ) ;
        
        assertEquals( "useElementName was remembered", "required-element-name", pl.getUseElement() ) ;
    }
    
    
    public void testAdditionToElement() {
        
        Element el = new Element( "E" ) ;
        
        Placeholder pl = new Placeholder( "required-element-name" ) ;
        
        el.add( pl ) ;
        
    }
    
    public void testWarningFragment() throws ServerActiveHtmlException {
        
        Placeholder p = new Placeholder( "no-such-element" ) ;
        
        ServerActiveHtml parsedSah = new ServerActiveHtml( "foo-bar" ) ;
        
        Element notmatch = new Element( "foo-bar" ) ;
        
        parsedSah.add( notmatch ) ;
        
        List actual = p.getReplacement( parsedSah ) ;
        
        List expected = new LinkedList() ; //new ServerActiveHtml( "-replacement" ) ;
        expected.add( new Fragment( "<b>Warning: missing element for placeholder 'no-such-element'</b>" ) ) ;
        
        checkObjectTree( "[testWarningFragment]", expected , actual ) ;
    }
    
    
    public void testGetDeepReplacement() throws ServerActiveHtmlException {
        
        Placeholder p = new Placeholder( "target" ) ;
        
        /*
         * SAH root
         *     ELEMENT a
         *          SAH b
         *              ELEMENT target
         *                  FRAG "DEEP FRAG"
         *     FRAG "FOO"
         */
        ServerActiveHtml root = new ServerActiveHtml( "root" ) ;
        
        Element a = new Element( "a" ) ;
        ServerActiveHtml b = new ServerActiveHtml( "b" ) ;
        Element target = new Element( "target" ) ;
        target.add( new Fragment( "DEEP FRAG" ) ) ;
        b.add( target ) ;
        a.add( b ) ;
        root.add( a ) ;
        root.add( new Fragment( "FOO" ) ) ;
        
        List actual = p.getReplacement( root ) ;
        
        List expected = new LinkedList() ; //ServerActiveHtml( "-replacement" ) ;
        expected.add( new Fragment( "DEEP FRAG" ) ) ;
        
        checkObjectTree( "[testGetDeepReplacement]", expected, actual ) ;
        
    }
    
    
    public void testGetDeepReplacementWithDeeperAlternative() throws ServerActiveHtmlException {
        
        Placeholder p = new Placeholder( "target" ) ;
        
        /*
         * SAH root
         *     ELEMENT d-a
         *          SAH d-b
         *              ELEMENT d-c
         *                  SAH d-d
         *                      ELEMENT target
         *                          FRAG "WRONG"
         *     ELEMENT a
         *          SAH b
         *              ELEMENT target
         *                  FRAG "RIGHT"
         *     FRAG "FOO"
         */
        ServerActiveHtml root = new ServerActiveHtml( "root" ) ;
        
        Element a = new Element( "a" ) ;
        ServerActiveHtml b = new ServerActiveHtml( "b" ) ;
        Element target = new Element( "target" ) ;
        target.add( new Fragment( "RIGHT" ) ) ;
        b.add( target ) ;
        a.add( b ) ;
        
        Element d_a = new Element( "d-a" ) ;
        ServerActiveHtml d_b = new ServerActiveHtml( "d-b" ) ;
        Element d_c = new Element( "d-c" ) ;
        ServerActiveHtml d_d = new ServerActiveHtml( "d-d" ) ;
        
        Element targetWrong = new Element( "target" ) ;
        targetWrong.add( new Fragment( "WRONG" ) ) ;
        d_d.add( targetWrong ) ;
        d_c.add( d_d ) ;
        d_b.add( d_c ) ;
        d_a.add( d_b ) ;
        
        root.add( d_a ) ;
        root.add( a ) ;
        root.add( new Fragment( "FOO" ) ) ;
        
        //System.out.println( MarkedUpHtmlParser.objectTreeToString( root ) ) ;
        
        List actual = p.getReplacement( root ) ;
        
        List expected = new LinkedList() ; //ServerActiveHtml( "-replacement" ) ;
        expected.add( new Fragment( "RIGHT" ) ) ;
        
        checkObjectTree(
        "[testGetDeepReplacementWithDeeperAlternative]",
        expected,
        actual
        ) ;
        
    }
    
    public void testPlaceholderTransfer() throws ServerActiveHtmlException {
        
        /*
         * SAH root
         *      ELEMENT target, placeholder-pid = pid-mm-01-123, placeholder-cid = cid-mm-01-456
         *          FRAG pid-mm-01-123 & cid-mm-01-456
         */
        
        Properties placeholders = new Properties() ;
        placeholders.setProperty( "pid", "pid-mm-01-123" ) ;
        placeholders.setProperty( "cid", "cid-mm-01-456" ) ;
        
        ServerActiveHtml root = new ServerActiveHtml( "whatever" ) ;
        Element target = new Element( "target" ) ;
        target.setPlaceholders( placeholders ) ;
        target.add( new Fragment( "pid-mm-01-123 & cid-mm-01-456" ) ) ;
        root.add( target ) ;
        
        Placeholder p = new Placeholder( "target" ) ;
        
        List expected = new LinkedList() ;
        ServerActiveHtml wrapper = new ServerActiveHtml( "-replacement" ) ;
        wrapper.setPlaceholders ( placeholders ) ;
        wrapper.add( new Fragment( "pid-mm-01-123 & cid-mm-01-456" ) ) ;
        expected.add ( wrapper ) ;
        
        List actual = p.getReplacement( root ) ;
        checkObjectTree(
        "[testPlaceholderTransfer]",
        expected,
        actual
        ) ;
        
    }
    
    // ------------------------------------------------------------------------- shared methods
    
    /**
     * Compare two object trees. Copied from MarkedUpHtmlParserTest.
     */
    private void checkObjectTree( String name, List expectedObjectTree, List actualObjectTree ) {
        
        assertEquals( "The number of top level objects was the same (" + name + ")" +
        "\n\nExpected:\n" + MarkedUpHtmlParser.objectTreeToString( expectedObjectTree ) +
        "\n\nActual:\n"   + MarkedUpHtmlParser.objectTreeToString( actualObjectTree ),
        expectedObjectTree.size(),
        actualObjectTree  .size()
        ) ;
        
        for ( int objIdx = 0 ; objIdx < expectedObjectTree.size() ; objIdx++ ) {
            assertEquals( "The objects at index " + objIdx + " were equal (" + name + ")" +
            "\n\nExpected:\n" + MarkedUpHtmlParser.objectTreeToString( expectedObjectTree ) +
            "\n\nActual:\n"   + MarkedUpHtmlParser.objectTreeToString( actualObjectTree ),
            expectedObjectTree.get( objIdx ),
            actualObjectTree  .get( objIdx ) ) ;
        }
        
    }
}
