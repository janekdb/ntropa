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
 * LoadUtilitiesTest.java
 *
 * Created on 09 January 2002, 16:25
 */

package tests.com.studylink.utility;

import java.util.Arrays ;
import java.util.Date ;

import junit.framework.*;

import com.studylink.utility.LoadUtilities ;

/**
 *
 * @author  jdb
 * @version $Id: LoadUtilitiesTest.java,v 1.2 2003/04/14 15:24:21 jdb Exp $
 */
public class LoadUtilitiesTest extends TestCase {
    
    /** Creates new LoadUtilitiesTest */
    public LoadUtilitiesTest ( String testName ) {
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
        
        TestSuite suite = new TestSuite ( LoadUtilitiesTest.class );
        return suite;
    }
    
    public void testGetArraySet () {
        /* test */
        String s = "" ;
        String expected [] = {} ;
        
        String actual [] = LoadUtilities.getArraySet ( s ) ;
        
        if ( ! Arrays.equals ( expected, actual ) )
            fail ( "The sanitised array was correct. Expected:\n" +
            Arrays.asList ( expected ) + "\nActual:\n" + Arrays.asList ( actual ) ) ;
        
        /* test */
        s = "AA" ;
        expected =  new String [] { "AA" } ;
        
        actual = LoadUtilities.getArraySet ( s ) ;
        
        if ( ! Arrays.equals ( expected, actual ) )
            fail ( "The sanitised array was correct. Expected:\n" +
            Arrays.asList ( expected ) + "\nActual:\n" + Arrays.asList ( actual ) ) ;
        
        /* test */
        s = "AA\n\nBB\nCC\nCC" ;
        expected =  new String [] { "AA", "BB", "CC" } ;
        
        actual = LoadUtilities.getArraySet ( s ) ;
        
        if ( ! Arrays.equals ( expected, actual ) )
            fail ( "The sanitised array was correct. Expected:\n" +
            Arrays.asList ( expected ) + "\nActual:\n" + Arrays.asList ( actual ) ) ;
        
    }
    
    public void testNormalize () {
        /* null */
        String [] inArray = null ;
        assertEquals ( "Null was ignored",
        null,
        LoadUtilities.normalize ( inArray )
        ) ;
        
        /* Test 1 */
        inArray = new String [] { "" } ;
        
        assertEquals ( "normalize succeeded (1)",
        Arrays.asList ( new String [] {} ),
        Arrays.asList ( LoadUtilities.normalize ( inArray ) )
        ) ;
        
        /* Test 2 */
        inArray = new String [ 2 ] ;
        inArray [ 0 ] = null ;
        inArray [ 1 ] = "" ;
        
        assertEquals ( "normalize succeeded (2)",
        Arrays.asList ( new String [] {} ),
        Arrays.asList ( LoadUtilities.normalize ( inArray ) )
        ) ;
        
        /* Test 3 */
        inArray = new String [ 3 ] ;
        inArray [ 0 ] = null ;
        inArray [ 1 ] = "" ;
        inArray [ 2 ] = "a" ;
        
        assertEquals ( "normalize succeeded (3)",
        Arrays.asList ( new String [] { "a" } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray ) )
        ) ;
        
        /* Test 4 */
        inArray = new String [ 4 ] ;
        inArray [ 0 ] = "a" ;
        inArray [ 1 ] = "a" ;
        inArray [ 2 ] = "E" ;
        inArray [ 3 ] = "E" ;
        
        assertEquals ( "normalize succeeded (4)",
        Arrays.asList ( new String [] { "E", "a" } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray ) )
        ) ;
        
        /* Test 5 */
        inArray = new String [ 3 ] ;
        inArray [ 0 ] = "3" ;
        inArray [ 1 ] = "2" ;
        inArray [ 2 ] = "1" ;
        
        assertEquals ( "normalize succeeded (5)",
        Arrays.asList ( new String [] { "1", "2", "3" } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray ) )
        ) ;
    }
    
    /**
     * Test normalize ( String [], String ).
     */
    public void testNormalize2 () {
        /* null */
        String [] inArray = new String [] { "t" } ;
        assertEquals ( "null was ignored",
        Arrays.asList ( new String [] { "t" } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray, null ) )
        ) ;
        /* zero-length */
        inArray = new String [] { "AA" } ;
        assertEquals ( "zero-length was ignored",
        Arrays.asList ( new String [] { "AA" } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray, "" ) )
        ) ;
        /* Ordering */
        inArray = new String [] { "t" } ;
        assertEquals ( "Order was maintained",
        Arrays.asList ( new String [] { "a", "t" } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray, "a" ) )
        ) ;
        /* Duplicate */
        inArray = new String [] { "s", "t" } ;
        assertEquals ( "Duplicate was ignored",
        Arrays.asList ( new String [] { "s", "t" } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray, "s" ) )
        ) ;
        
        
    }
    
    
    public void testNormalizeDate () {
        /* null */
        Date [] inArray = null ;
        assertEquals ( "Null was ignored",
        null,
        LoadUtilities.normalize ( inArray )
        ) ;
        
        /* no zero length equivalent */
        
        /* Test 1 */
        inArray = new Date [ 1 ] ;
        inArray [ 0 ] = null ;
        
        assertEquals ( "normalize succeeded (1)",
        Arrays.asList ( new Date [] {} ),
        Arrays.asList ( LoadUtilities.normalize ( inArray ) )
        ) ;
        
        /* Test 2 */
        inArray = new Date [ 2 ] ;
        inArray [ 0 ] = null ;
        inArray [ 1 ] = new Date ( 1000 ) ;
        
        assertEquals ( "normalize succeeded (2)",
        Arrays.asList ( new Date [] { new Date ( 1000 ) } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray ) )
        ) ;
        
        /* Test 3 */
        inArray = new Date [ 4 ] ;
        inArray [ 0 ] = new Date ( 4000 ) ;
        inArray [ 1 ] = new Date ( 4000 ) ;
        inArray [ 2 ] = new Date ( 1000 ) ;
        inArray [ 3 ] = new Date ( 1000 ) ;
        
        assertEquals ( "normalize succeeded (3)",
        Arrays.asList ( new Date [] { new Date ( 1000 ), new Date ( 4000 ) } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray ) )
        ) ;
        
        /* Test 4 */
        inArray = new Date [ 3 ] ;
        inArray [ 0 ] = new Date ( 3000 ) ;
        inArray [ 1 ] = new Date ( 2000 ) ;
        inArray [ 2 ] = new Date ( 1000 ) ;
        
        assertEquals ( "normalize succeeded (4)",
        Arrays.asList ( new Date [] { new Date ( 1000 ), new Date ( 2000 ), new Date ( 3000 ) } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray ) )
        ) ;
    }
    
    
    /**
     * Test normalize ( Date [], Date ).
     */
    public void testNormalizeDate2 () {
        /* null */
        Date [] inArray = new Date [] { new Date ( 1000 ) } ;
        assertEquals ( "null was ignored",
        Arrays.asList ( new Date [] { new Date ( 1000 ) } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray, null ) )
        ) ;
        /* zero-length * /
        inArray = new Date [] { new Date ( 2000 ) } ;
        assertEquals ( "zero-length was correctly handled",
        Arrays.asList ( new Date [] { new Date ( 2000 ) } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray, "" ) )
        ) ;*/
        /* Ordering */
        inArray = new Date [] { new Date ( 1000 ) } ;
        assertEquals ( "Order was maintained",
        Arrays.asList ( new Date [] { new Date ( 500 ), new Date ( 1000 ) } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray, new Date ( 500 ) ) )
        ) ;
        /* Duplicate */
        inArray = new Date [] { new Date ( 2000 ), new Date ( 3000 ) } ;
        assertEquals ( "Duplicate was ignored",
        Arrays.asList ( new Date [] { new Date ( 2000 ), new Date ( 3000 ) } ),
        Arrays.asList ( LoadUtilities.normalize ( inArray, new Date ( 2000 ) ) )
        ) ;
        
        
    }
}
