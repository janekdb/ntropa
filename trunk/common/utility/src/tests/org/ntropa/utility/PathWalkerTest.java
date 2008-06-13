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
 * PathWalkerTest.java
 *
 * Created on 22 October 2001, 23:10
 */

package tests.org.ntropa.utility;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.utility.PathWalker;


/**
 *
 * @author  jdb
 * @version $Id: PathWalkerTest.java,v 1.1 2001/10/23 00:55:29 jdb Exp $
 */
public class PathWalkerTest extends TestCase {
    
    public PathWalkerTest ( String testName ) {
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
        
        TestSuite suite = new TestSuite ( PathWalkerTest.class );
        return suite;
    }
    
    public void testRelativePath () {
        
        String path ;
        List expected ;
        
        path = "" ;
        expected = Arrays.asList ( new String [] { "" } ) ;
        check ( path, expected ) ;
        
        path = "a" ;
        expected = Arrays.asList ( new String [] { "a", "" } ) ;
        check ( path, expected ) ;
        
        path = "a/b" ;
        expected = Arrays.asList ( new String [] { "a/b", "a", ""  } ) ;
        check ( path, expected ) ;
        
        path = "a/b/c" ;
        expected = Arrays.asList ( new String [] { "a/b/c", "a/b", "a", ""  } ) ;
        check ( path, expected ) ;
        
        path = "main.html" ;
        expected = Arrays.asList ( new String [] { "main.html", ""  } ) ;
        check ( path, expected ) ;
        
    }
    
    public void testAbsolutePath () {
        String path ;
        List expected ;
        
        path = "/" ;
        expected = Arrays.asList ( new String [] { "/" } ) ;
        check ( path, expected ) ;
        
        path = "/a" ;
        expected = Arrays.asList ( new String [] { "/a", "/" } ) ;
        check ( path, expected ) ;
        
        path = "/a/b" ;
        expected = Arrays.asList ( new String [] { "/a/b", "/a", "/"  } ) ;
        check ( path, expected ) ;
        
        path = "/a/b/c" ;
        expected = Arrays.asList ( new String [] { "/a/b/c", "/a/b", "/a", "/"  } ) ;
        check ( path, expected ) ;
    }
    
    public void testTrailingSeparator () {
        
        String path ;
        List expected ;
        
        path = "/a/b/c/" ;
        expected = Arrays.asList ( new String [] { "/a/b/c", "/a/b", "/a", "/"  } ) ;
        check ( path, expected ) ;
        
        path = "a/b/c/" ;
        expected = Arrays.asList ( new String [] { "a/b/c", "a/b", "a", ""  } ) ;
        check ( path, expected ) ;
        
    }
    
    protected void check ( String path, List expected ) {
        
        PathWalker p = new PathWalker ( path ) ;
        
        List actual = new LinkedList () ;
        Iterator it = p.iterator () ;
        while ( it.hasNext () ) {
            Object o = it.next () ;
            //System.out.println( path + ": " + o );
            actual.add ( o ) ;
        }
        
        assertEquals (
        "THe PathWalker iterated over <" + path + "> incorrectly", expected, actual ) ;
    }
}
