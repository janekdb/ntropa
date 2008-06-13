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
 * ContextPathTest.java
 *
 * Created on 20 June 2002, 16:00
 */

package tests.org.ntropa.build;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.ContextPath;
import org.ntropa.build.ContextPathException;


/**
 *
 * @author  jdb
 * @version $Id: ContextPathTest.java,v 1.1 2002/06/20 19:20:46 jdb Exp $
 */
public class ContextPathTest extends TestCase {
    
    /** Creates new ContextPathTest */
    public ContextPathTest ( String testName ) {
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
        
        TestSuite suite = new TestSuite ( ContextPathTest.class );
        return suite;
    }
    
    public void testConstructor () {
        
        try {
            ContextPath cp = new ContextPath ( (String)null ) ;
            fail ( "Null String rejected for constructor" );
        }
        catch ( ContextPathException e ) {}
        
        try {
            ContextPath cid = new ContextPath ( "" ) ;
            fail ( "Empty String rejected for constructor"  );
        }
        catch ( ContextPathException e ) {}
        
        try {
            ContextPath cid = new ContextPath ( "has a space" ) ;
            fail ( "String with space rejected for constructor"  );
        }
        catch ( ContextPathException e ) {}
        
        try {
            ContextPath cid = new ContextPath ( "shell-meta->character" ) ;
            fail ( "String with shell meta-character rejected for constructor"  );
        }
        catch ( ContextPathException e ) {}

    }
    
    public void testComparable() {
        SortedSet<ContextPath> paths = new TreeSet<ContextPath>();
        
        paths.add(new ContextPath("zzz"));
        paths.add(new ContextPath("aa"));
        paths.add(new ContextPath("bbb"));
        
        assertEquals(3, paths.size());
        
        Iterator it = paths.iterator();
        
        assertEquals(new ContextPath("aa"), it.next());
        assertEquals(new ContextPath("bbb"), it.next());
        assertEquals(new ContextPath("zzz"), it.next());
    }
}
