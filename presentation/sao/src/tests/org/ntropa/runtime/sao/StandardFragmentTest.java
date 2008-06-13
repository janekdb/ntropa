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
 * StandardFragmentTest.java
 *
 * Created on 27 November 2001, 12:25
 */

package tests.org.ntropa.runtime.sao;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.runtime.sao.AbstractElement;
import org.ntropa.runtime.sao.AbstractServerActiveObject;
import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;
import org.ntropa.runtime.sao.StandardElement;
import org.ntropa.runtime.sao.StandardFragment;
import org.ntropa.runtime.sao.StandardInvocationContext;

import com.mockobjects.servlet.MockJspWriter;

/**
 *
 * @author  jdb
 * @version $Id: StandardFragmentTest.java,v 1.8 2003/11/27 12:52:41 jdb Exp $
 */
public class StandardFragmentTest extends TestCase {
    
    /** Creates new StandardFragmentTest */
    public StandardFragmentTest ( String testName ) {
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
        
        TestSuite suite = new TestSuite ( StandardFragmentTest.class );
        return suite;
    }
    
    /**
     * Check assumption about intern
     */
    public void testIntern () throws Exception {
        
        //AbstractElement sao = new StandardElement () ;
        
        StandardFragment frags [] = new StandardFragment [ 10 ] ;
        
        for ( int i = 0 ; i < frags.length ; i ++ ) {
            frags [ i ] = new StandardFragment () ;
            frags [ i ] .setHtml ( "<html></html>" ) ;
            //System.out.println("getHtmlIdentityHashCode" + frags [ i ].getHtmlIdentityHashCode () );
        }
        
        int identity = getHtmlIdentityHashCode(frags [ 0 ]);
        for ( int i = 1 ; i < frags.length ; i ++ )
            assertEquals (
            "The intern assumption was true",
            identity,
		    getHtmlIdentityHashCode(frags [ i ] )
            ) ;
        
    }
    
    /*
     * Using reflection to access private methods was suggested by this article:
     * http://www.onjava.com/pub/a/onjava/2003/11/12/reflection.html
     */
    private int getHtmlIdentityHashCode (StandardFragment frag ) throws Exception {
    	
    	Method m = StandardFragment.class.getDeclaredMethod("testGetHtmlIdentityHashCode", new Class[]{});
    	m.setAccessible(true);
    	
    	Integer id = (Integer) m.invoke(frag, new Object[]{});
    	return id.intValue();
    	
    }
    
    /**
     * Test a StandardFragment reduces the list of placeholders to the minimum necessary
     */
    public void testApplicablePlaceholders () throws Exception {
        AbstractElement el = new StandardElement () ;
        for ( int i = 1 ; i <= 50 ; i++ )
            el.setPlaceholder ( "p-" + i, "[replaceable-text-" + i + "]" ) ;
        
        for (
        int numberOfApplicablePlaceholders = 0 ;
        numberOfApplicablePlaceholders <= 50 ;
        numberOfApplicablePlaceholders++
        ) {
            //System.out.println("Generating test for " + numberOfApplicablePlaceholders );
            Properties applicable = new Properties () ;
            StandardFragment sf = new StandardFragment () ;
            String html = "START" ;
            for ( int i = 1 ; i <= numberOfApplicablePlaceholders ; i++ ) {
                applicable.setProperty ( "p-" + i, "[replaceable-text-" + i + "]" ) ;
                html += "[replaceable-text-" + i + "]" ;
                
            }
            html += "END" ;
            sf.setHtml ( html ) ;
            
            el.addChild ( sf ) ;
            //sf.setContainer( el ) ;
            
            Map m = getApplicablePlaceholders ( sf ) ;
            String placeholderKeys [] = (String []) m.get ( "keys" ) ;
            String placeholders [] = (String []) m.get ( "values" ) ;
            
            assertEquals ( "Number of keys and values mismatch", placeholderKeys.length, placeholders.length ) ;
            assertEquals ( "Cached placeholder count wrong", numberOfApplicablePlaceholders, placeholderKeys.length ) ;
            /*
             * Compare the arrays and the expected placeholders
             * There is no order guarantee for the placeholders returned from the Fragment
             */
            for ( int i = 1 ; i<= placeholderKeys.length ; i++ ) {
                // date = 2001-November-27, Tuesday
                String actualKey = placeholderKeys [ i - 1 ] ;
                String actualValue = placeholders [ i - 1 ] ;
                String expectedValue = applicable.getProperty ( actualKey ) ;
                if ( expectedValue == null )
                    fail ( "Value missing for key: " + actualKey ) ;
                assertEquals ( "The cached replacement text was wrong", expectedValue, actualValue ) ;
            }
            
        }
    }
    
	/*
	 * Using reflection to access private methods was suggested by this article:
	 * http://www.onjava.com/pub/a/onjava/2003/11/12/reflection.html
	 */
	private Map getApplicablePlaceholders (StandardFragment frag ) throws Exception {
    	
		Method m = StandardFragment.class.getDeclaredMethod("testGetApplicablePlaceholders", new Class[]{});
		m.setAccessible(true);
    	
		Map map = (Map) m.invoke(frag, new Object[]{});
		return map;
    	
	}

    
    public void testLongestFirstComparator () {
        
        Properties p = new Properties () ;
        
        p.setProperty ( "key-1", "12345" ) ;
        p.setProperty ( "key-2", "123456" ) ;
        p.setProperty ( "key-z", "123" ) ;
        p.setProperty ( "key-a", "123" ) ;
        
        List l = Arrays.asList ( new String [] { "key-1", "key-2", "key-z", "key-a" } ) ;
        
        Collections.sort ( l, ( new StandardFragment ()).getLongestFirstComparator ( p ) ) ;
        
        List expected = Arrays.asList ( new String [] { "key-2", "key-1", "key-a", "key-z" } ) ;
        
        assertEquals ( "The LongestFirst comparator worked", expected, l ) ;
    }
    
    
    /**
     * Check sequences of placeholder text and non-placeholder text
     */
    public void testSequences () throws Exception {
        
        /*
         * The values to return from getPlaceholderReplacement
         */
        Properties repTable = new Properties () ;
        String repa = "rep-a" ;
        String repb = "rep-b" ;
        repTable.setProperty ( "code-1", repa ) ;
        repTable.setProperty ( "code-2", repb ) ;
        
        AbstractServerActiveObject container = new RepTableContainer ( repTable ) ;
        
        /*
         * The text of the placeholders and the placeholder code.
         */
        Properties placeholders = new Properties () ;
        String holder1 = "The event is in June." ;
        String holder2 = "The event is in July." ;
        placeholders.setProperty ( "code-1", holder1) ;
        placeholders.setProperty ( "code-2", holder2 ) ;
        
        
        //checkReplacements( "", "", placeholders, container ) ;
        
        String [] elements = new String [] { "", holder1, holder2, "normal text 1.", "normal text 2." } ;
        
        String html = null ;
        String expected = null ;
        try {
            for ( int i1 = 0 ; i1 < 5 ; i1++ ) {
                for ( int i2 = 0 ; i2 < 5 ; i2++ ) {
                    for ( int i3 = 0 ; i3 < 5 ; i3++ ) {
                        for ( int i4 = 0 ; i4 < 5 ; i4++ ) {
                            
                            html = elements [ i1 ] + elements [ i2 ] + elements [ i3 ] + elements [ i4 ] ;
                            expected = getExpected ( i1 ) + getExpected ( i2 ) + getExpected ( i3 ) + getExpected ( i4 );
                            checkReplacements ( expected, html, placeholders, container ) ;
                        }
                    }
                }
            }
        }
        catch ( Exception e ) {
            System.out.println ("html: " + html );
            System.out.println ("expected: " + expected);
            throw e ;
        }
    }
    
    
    public void testLongestFirst () throws Exception {
        
        /*
         * The values to return from getPlaceholderReplacement
         */
        Properties repTable = new Properties () ;
        String repa = "rep-a" ;
        String repb = "rep-b" ;
        repTable.setProperty ( "code-1", repa ) ;
        repTable.setProperty ( "code-2", repb ) ;
        
        AbstractServerActiveObject container = new RepTableContainer ( repTable ) ;
        
        /*
         * The text of the placeholders and the placeholder code.
         */
        Properties placeholders = new Properties () ;
        String holder1 = "01" ;
        String holder2 = "012345" ;
        placeholders.setProperty ( "code-1", holder1) ;
        placeholders.setProperty ( "code-2", holder2 ) ;
        
        
        checkReplacements ( repb, "012345", placeholders, container ) ;
        checkReplacements ( repa + repb, "01012345", placeholders, container ) ;
        
        String [] elements = new String [] { "", "01", "012345", "normal text 1." } ;
        
        for ( int i1 = 0 ; i1 < 4 ; i1++ ) {
            for ( int i2 = 0 ; i2 < 4 ; i2++ ) {
                for ( int i3 = 0 ; i3 < 4 ; i3++ ) {
                    
                    String html = elements [ i1 ] + elements [ i2 ] + elements [ i3 ] ;
                    String expected = getExpected ( i1 ) + getExpected ( i2 ) + getExpected ( i3 ) ;
                    
                    checkReplacements ( expected, html, placeholders, container ) ;
                }
            }
            
        }
    }
    
    /*
     * Hardcoded to prevent fixture errors from masking themselves.
     */
    private String getExpected ( int i ) {
        
        switch ( i ) {
            
            case 0 :
                return "" ;
                
            case 1 :
                return "rep-a" ;
                
            case 2 :
                return "rep-b" ;
                
            case 3 :
                return "normal text 1." ;
                
            case 4 :
                return "normal text 2." ;
                
            default :
                throw new IllegalArgumentException ( "i: " + i ) ;
        }
    }
    
    
    /**
     * Test for replacement on replacement bug identified by Abhishek, 02-8-30.
     * <p>
     * The problem is shown in this example, (needs HTML entities!)
     *
     * <!-- name="foo"
     *      placeholder-pid="pid-placeholder"
     *      placeholder-cid="22"
     * -->
     *
     * The pid is pid-placeholder and the cid is 22
     *
     * <!-- name="/foo" -->
     *
     * Let the replacement for the pid placeholder code be done first so we have
     * this working string
     *
     * The pid is pid-mm-01-07229k and the cid is 22
     *
     * Then the replacement for the cid placeholder code is done on both "22" strings
     *
     * The pid is pid-mm-01-07cid-mm-01-559k and the cid is cid-mm-01-55
     *
     */
    public void testReplacementTextNotChanged () throws Exception {
        
        /*
         * The values to return from getPlaceholderReplacement
         */
        Properties repTable = new Properties () ;
        repTable.setProperty ( "code-1", "p2" ) ;
        repTable.setProperty ( "code-2", "p1" ) ;
        
        AbstractServerActiveObject container = new RepTableContainer ( repTable ) ;
        
        /*
         * The text of the placeholders and the placeholder code.
         */
        Properties placeholders = new Properties () ;
        placeholders.setProperty ( "code-1", "p1" ) ;
        placeholders.setProperty ( "code-2", "p2" ) ;
        
        /*
         * In order to be free of ordering assumptions each replacement text contains the
         * placeholder of the other.
         */
        checkReplacements ( "p2-o-p1", "p1-o-p2", placeholders, container ) ;
        
    }
    
    
    private void checkReplacements ( String expected, String html, Properties placeholders, AbstractServerActiveObject container )
    throws Exception {
        
        AbstractElement el = new StandardElement () ;
        for ( Iterator it = placeholders.keySet ().iterator () ; it.hasNext () ; ) {
            String p = ( String ) it.next () ;
            el.setPlaceholder ( p, placeholders.getProperty ( p ) ) ;
        }
        
        StandardFragment sf = new StandardFragment () ;
        
        sf.setHtml ( html ) ;
        
        el.addChild ( sf ) ;
        
        container.addChild ( el ) ;
        
        InvocationContext icb = new StandardInvocationContext () ;
        icb.enableRenderPhase () ;
        
        MockJspWriter out = new MyMockJspWriter () ;
        
        out.setExpectedData ( expected ) ;
        
        icb.setJspWriter ( out ) ;
        
        /* Kick it off */
        sf.render ( icb ) ;
        
        out.verify () ;
    }
    
    
    private class MyMockJspWriter extends MockJspWriter {
        
        MyMockJspWriter () {
            super() ;
        }
        
        public void write (char[] c, int i1, int i2) {
            print ( new String ( c, i1, i2 ) ) ;
        }
    }
    
    private class RepTableContainer extends BaseServerActiveObject {
        
        Properties repTable ;
        
        RepTableContainer ( Properties props ) {
            repTable = props ;
        }
        
        public String getPlaceholderReplacement ( String name ) {
            return repTable.getProperty ( name ) ;
        }
        
    }
}
