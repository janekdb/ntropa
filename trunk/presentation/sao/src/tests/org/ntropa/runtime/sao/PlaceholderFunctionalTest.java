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
 * PlaceholderReplacementTest.java
 *
 * Created on 27 November 2001, 16:21
 */

package tests.org.ntropa.runtime.sao;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.runtime.sao.AbstractElement;
import org.ntropa.runtime.sao.AbstractServerActiveObject;
import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.StandardElement;
import org.ntropa.runtime.sao.StandardFragment;
import org.ntropa.utility.HtmlUtils;


/**
 * This is a cross class test which moves it into the functional
 * test category.
 *
 * @author  jdb
 * @version $Id: PlaceholderFunctionalTest.java,v 1.7 2003/11/27 12:52:41 jdb Exp $
 */
public class PlaceholderFunctionalTest extends TestCase {
    
    Set _set ;
    
    /** Creates new AbstactContainerTest */
    public PlaceholderFunctionalTest ( String testName ) {
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
        
        TestSuite suite = new TestSuite ( PlaceholderFunctionalTest.class );
        return suite;
    }
    
    
    /**
     * Test the replacement of a varying number of placeholders
     */
    public void testPlaceholderReplacement () throws Exception {
        
        /*
         * Tests:
         * Basic replacements
         * Nesting operations
         * System placeholders
         */
        
        /*
         * With NUM_PLACEHOLDERS = 600, the test took 230 seconds to execute
         * on a Duron 800 Mhz, 256 MB SDRAM. That's approx 600 * 300 inner loops.
         * 180,000 loops / 230 seconds = 782 loops/second.
         */
        int NUM_PLACEHOLDERS = 30 ;
        AbstractServerActiveObject baseSao = new TestSOA () ;
        /* Add some placeholders */
        for ( int i = 1 ; i <= NUM_PLACEHOLDERS ; i++ )
            baseSao.setPlaceholder ( "date-" + i, "22-" + i + "-1972" ) ;
        
        AbstractElement el = new StandardElement () ;
        el.setName ( "my-element" ) ;
        baseSao.addChild ( el ) ;
        el.setContainer ( baseSao ) ;
        
        
        StandardFragment sf = new StandardFragment () ;
        sf.setHtml ( "The date is October the Eleventh" ) ;
        
        el.addChild ( sf ) ;
        sf.setContainer ( el ) ;
        
        //InvocationContext icb = new StandardInvocationContext () ;
        //icb.setJspWriter ( new TestJspWriter () ) ;
        
        for ( int numApplicable = 0 ; numApplicable <= NUM_PLACEHOLDERS ; numApplicable ++ ) {
            /* Collect all placeholders that are requested */
            String html = "START" ;
            for ( int i = 1 ; i <= numApplicable ; i++ )
                html += "The date is 22-" + i + "-1972, " ;
            html += "END" ;
            
            sf.setHtml ( html ) ;
            _set = new TreeSet () ;
            String replaced = getReplacedPlaceholders ( sf ) ;
            
            //System.out.println ( "replaced: " + replaced );
            
            String expectedReplaced = "START" ;
            Set expectedKeys = new TreeSet () ;
            for ( int i = 1 ; i <= numApplicable ; i++ ) {
                expectedKeys.add ( "date-" + i ) ;
                expectedReplaced += "The date is " + "[Dynamic-value:date-" + i + "]" + ", " ;
            }
            expectedReplaced += "END" ;
            
            assertEquals ( "The set of requested placeholder keys was wrong", expectedKeys, _set ) ;
            assertEquals ( "The replaced string was wrong", expectedReplaced, replaced ) ;
            
            //System.out.println ( _set );
            
        }
        
    }
    
    /**
     * Test the delegation of placeholder resolution with this object tree:
     *
     * (A) SAO 1, knows how to resolve 'p1'
     * (B)    Element
     * (C)        SOA, defines the placeholder 'p1' but cannot resolve it
     * (D)            Element
     * (E)                 Fragment with 'p1'
     */
    public void testDelegation () throws Exception {
        
        /* A */
        AbstractServerActiveObject baseSao = new TestSOA () ;
        
        /* B */
        AbstractElement el = new StandardElement () ;
        el.setName ( "my-element" ) ;
        baseSao.addChild ( el ) ;
        el.setContainer ( baseSao ) ;
        
        /* C */
        /* This sao will delegate all placeholder resolution requests */
        AbstractServerActiveObject dumbSao = new BaseServerActiveObject () ;
        dumbSao.setPlaceholder ( "p1", "Rosebud" ) ;
        
        el.addChild ( dumbSao ) ;
        dumbSao.setContainer ( el ) ;
        
        /* D */
        AbstractElement D = new StandardElement () ;
        D.setName ( "my-element" ) ;
        dumbSao.addChild ( D ) ;
        D.setContainer ( dumbSao ) ;
        
        
        StandardFragment sf = new StandardFragment () ;
        sf.setHtml ( "What is \"Rosebud\"?" ) ;
        
        /* This 'add' will parse the html of sf for system placeholders */
        D.addChild ( sf ) ;
        sf.setContainer ( D ) ;
        
        assertEquals (
        "The replaced value was wrong (possibly due to failed replacement)",
        "What is \"[Dynamic-value:p1]\"?",
		getReplacedPlaceholders ( sf )
        ) ;
        
    }
    
	/*
	 * Using reflection to access private methods was suggested by this article:
	 * http://www.onjava.com/pub/a/onjava/2003/11/12/reflection.html
	 */
	private String getReplacedPlaceholders (StandardFragment frag ) throws Exception {
    	
		Method m = StandardFragment.class.getDeclaredMethod("testGetReplacedPlaceholders", new Class[]{});
		m.setAccessible(true);
    	
		String s = (String) m.invoke(frag, new Object[]{});
		return s;
    	
	}

    /**
     * This class is used as a container to test replacement.
     */
    private class TestSOA extends BaseServerActiveObject {
        
        public String getPlaceholderReplacement (String name) {
            if ( _set != null )
                _set.add ( name ) ;
            String replacement = "[Dynamic-value:" + name + "]" ;
            //System.out.println ( "TestSOA: " + replacement );
            return replacement ;
        }
        
    }
    
    
    public void testStackBasedPlaceholderReplacements () throws Exception {
        
        
        /*
         * Make a SAO which uses the stack based placeholder replacement mechanism and
         * test the correct value is returned under various scenarios
         *
         *      getPlaceholderReplacement       stack based mechanism
         *
         *      no                              no
         *      yes                             no
         *      no                              yes
         *      yes                             yes
         *
         * yes means the method/mechanism provides a value for the placeholder code.
         * no means the method/mechanism returns null
         */
        
        /*
         * (A) SAO 1, knows how to provide a replacement value for 'p1'
         * (B)    Element, defines placeholder 'p1'
         * (C)        Fragment with text corresponding to placeholder code 'p1'
         */
        
        /* A */
        MyStackUsingSAO stackBasedSao = new MyStackUsingSAO () ;
        
        /* B */
        AbstractElement el = new StandardElement () ;
        el.setName ( "my-element" ) ;
        stackBasedSao.setPlaceholder ( "p1", "Rosebud" ) ;
        stackBasedSao.setPlaceholder ( "raw", "?" ) ;
        stackBasedSao.addChild ( el ) ;
        
        /* C */
        StandardFragment sf = new StandardFragment () ;
        sf.setHtml ( "What is \"Rosebud\"?" ) ;
        
        /* This 'add' will parse the html of sf for system placeholders */
        el.addChild ( sf ) ;
        
        /* sanity test */
        stackBasedSao.isGetPlaceholderReplacementUsing = false ;
        if ( getReplacedPlaceholders ( sf ).indexOf (
        "What is \"Warning:"
        ) != 0
        )
            fail ( "placeholder code was not handled when the sao did not handle it" ) ;
        
        stackBasedSao.isGetPlaceholderReplacementUsing = true ;
        assertEquals (
        "replacement value from method was used",
        "What is \"p1: &lt;replacement value from getPlaceholderReplacement&gt;\"<b>?</b>",
		getReplacedPlaceholders ( sf )
        ) ;
        
        stackBasedSao.isGetPlaceholderReplacementUsing = false ;
        stackBasedSao.setupStack () ;
        assertEquals (
        "replacement value from stack was used",
        "What is \"p1: &lt;replacement value from stack&gt;\"<b>?</b>",
		getReplacedPlaceholders ( sf )
        ) ;
        stackBasedSao.teardownStack () ;
        
        /* verify stack used in preference to method */
        stackBasedSao.isGetPlaceholderReplacementUsing = true ;
        stackBasedSao.setupStack () ;
        assertEquals (
        "replacement value from stack was used in preference to value from method",
        "What is \"p1: &lt;replacement value from stack&gt;\"<b>?</b>",
		getReplacedPlaceholders ( sf )
        ) ;
        stackBasedSao.teardownStack () ;
    }
    
    
    private static class MyStackUsingSAO extends BaseServerActiveObject {
        
        boolean isGetPlaceholderReplacementUsing = false ;
        
        
        void setupStack () {
            getPlaceholderReplacementStack ().push ( "p1", "p1: <replacement value from stack>" ) ;
            /* No conversion to HTML */
            getPlaceholderReplacementStack ().push ( "raw", "<b>?</b>", false ) ;
        }
        
        
        void teardownStack () {
            getPlaceholderReplacementStack ().pop () ;
            getPlaceholderReplacementStack ().pop () ;
        }
        
        public String getPlaceholderReplacement (String name) {
            
            if ( isGetPlaceholderReplacementUsing ) {
                if ( name.equals ( "p1" ) )
                    return HtmlUtils.convertToHtml ( name + ": <replacement value from getPlaceholderReplacement>" ) ;
                
                if ( name.equals ( "raw" ) )
                    return "<b>?</b>" ;
            }
            
            /* delegate to parent container */
            return null ;
        }
        
    }
    
    
    /*
     * I tried to make an extension of JspWriter in order to invoke the
     * sao object tree. The protected constructor of JspWriter defeated
     * me. In a JSP the JspWriter is obtained from a PageContext object,
     */
    /**
     * This class pretends to be a JspWriter so we can invoke Fragments
     */
    /*
    private class TestJspWriter extends JspWriter {
     
        protected TestJspWriter () {
        int i = 0 ;
        i++ ;
     
        } ;
     
        public void write ( String html ) throws java.io.IOException {
            System.out.println ( "[TestJspWriter] " + html );
        }
     
        public void close () throws java.io.IOException {
        }
     
        public void print (char param) throws java.io.IOException {
        }
     
        public void print (char[] values) throws java.io.IOException {
        }
     
        public void println (java.lang.Object obj) throws java.io.IOException {
        }
     
        public void print (boolean param) throws java.io.IOException {
        }
     
        public void println (long param) throws java.io.IOException {
        }
     
        public void clear () throws java.io.IOException {
        }
     
        public void println (java.lang.String str) throws java.io.IOException {
        }
     
        public void println (boolean param) throws java.io.IOException {
        }
     
        public void flush () throws java.io.IOException {
        }
     
        public void println () throws java.io.IOException {
        }
     
        public void println (char[] values) throws java.io.IOException {
        }
     
        public int getRemaining () {
        }
     
        public void println (float param) throws java.io.IOException {
        }
     
        public void print (java.lang.Object obj) throws java.io.IOException {
        }
     
        public void print (long param) throws java.io.IOException {
        }
     
        public void newLine () throws java.io.IOException {
        }
     
        public void print (int param) throws java.io.IOException {
        }
     
        public void print (float param) throws java.io.IOException {
        }
     
        public void println (char param) throws java.io.IOException {
        }
     
        public void print (double param) throws java.io.IOException {
        }
     
        public void clearBuffer () throws java.io.IOException {
        }
     
        public void print (java.lang.String str) throws java.io.IOException {
        }
     
        public void println (double param) throws java.io.IOException {
        }
     
        public void write (char[] values, int param, int param2) throws java.io.IOException {
        }
     
        public void println (int param) throws java.io.IOException {
        }
     
    }
     */
}
