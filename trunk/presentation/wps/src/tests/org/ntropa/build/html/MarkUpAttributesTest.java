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
 * MarkUpAttributesTest.java
 *
 * Created on 12 November 2001, 12:10
 */

package tests.org.ntropa.build.html;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.html.MarkUpAttributes;


/**
 *
 * @author  jdb
 * @version $Id: MarkUpAttributesTest.java,v 1.4 2002/09/05 17:13:41 jdb Exp $
 */
public class MarkUpAttributesTest extends TestCase {
    
    
    /** Creates new MarUpAttributesTest */
    public MarkUpAttributesTest ( String testName ) {
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
        
        TestSuite suite = new TestSuite ( MarkUpAttributesTest.class );
        return suite;
    }
    
    /*
    protected void setUp () throws Exception {}
     */
    
    /*
    protected void tearDown () throws Exception {}
     */
    
    public void testPlaceholders () {
        
        /* test duplicates are ignored */
        
        MarkUpAttributes m = new MarkUpAttributes () ;
        
        m.setAttribute ( "placeholder-date", "Monday, 2001-November-12" ) ;
        m.setAttribute ( "placeholder-date", "Friday" ) ;
        
        Properties p = m.getPlaceholders () ;
        
        if ( p.size () == 0 )
            fail ( "The placeholder attributes were not stored" ) ;
        
        if ( p.size () > 1 )
            fail ( "A duplicate was allowed" ) ;
        
        if ( ! p.containsKey ( "date" ) )
            fail ( "The placeholder list was wrong: 'date' was missing" ) ;
        
        if ( (! p.containsValue ( "Monday, 2001-November-12" ) ) && (! p.containsValue ( "Friday" ) ) )
            fail ( "Neither of the possible values was present" ) ;
        
        m = new MarkUpAttributes () ;
        /*
         * test various lengths of key
         *
         * placeholder-k
         * placeholder-kk
         * placeholder-kkk
         * ...
         *
         */
        String attributeName = "placeholder-" ;
        for ( int i = 1 ; i <= 50 ; i++ ) {
            attributeName += "k" ;
            m.setAttribute ( attributeName, "value-" + i ) ;
        }
        p = m.getPlaceholders () ;
        
        assertEquals ( "The number of placeholder was wrong", 50, m.size () ) ;
        
        /* This time without the prefix */
        attributeName = "" ;
        for ( int i = 1 ; i <= 50 ; i++ ) {
            attributeName += "k" ;
            assertEquals ( "The value of the placeholder was wrong: " + attributeName,
            "value-" + i,
            p.getProperty ( attributeName )
            ) ;
        }
        
        
    }
    
    
    public void testRemoving () {
        
        String ATTR_NAME = "name" ;
        
        MarkUpAttributes m = new MarkUpAttributes () ;
        
        m.setAttribute ( ATTR_NAME, "shark" ) ;
        m.removeAttribute ( ATTR_NAME ) ;
        
        assertEquals ( "The attribute was null", null, m.getAttribute ( ATTR_NAME ) ) ;
    }
    
}
