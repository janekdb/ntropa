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
 * AbstactServerActiveObjectTest.java
 *
 * Created on 10 January 2002, 14:47
 */

package tests.org.ntropa.runtime.sao;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.runtime.sao.AbstractElement;
import org.ntropa.runtime.sao.AbstractServerActiveObject;
import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;
import org.ntropa.runtime.sao.NoSuchAbstractElementException;
import org.ntropa.runtime.sao.StandardElement;
import org.ntropa.runtime.sao.StandardFragment;
import org.ntropa.runtime.sao.StandardInvocationContext;

import com.mockobjects.servlet.MockHttpServletRequest;

/**
 *
 * @author  jdb
 * @version $Id: AbstactServerActiveObjectTest.java,v 1.7 2003/03/24 16:58:35 jdb Exp $
 */
public class AbstactServerActiveObjectTest extends TestCase {
    
    /** Creates new AbstactServerActiveObjectTest */
    public AbstactServerActiveObjectTest ( String testName ) {
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
        
        TestSuite suite = new TestSuite ( AbstactServerActiveObjectTest.class );
        return suite;
    }
    
    /**
     * Test understanding of ThreadLocal behaviour.
     *
     * Create a sub-type of AbstactServerActiveObject which tests itself.
     *
     * Create object with references to:
     *
     *  i) A list to note its values in
     *  ii) A control variable starting at 2
     *
     *  Thread 1            Thread 2
     *
     *  Set icb to x        Set icb to y
     *
     *  Set "key" = 1       Set "key" = 2
     *
     *  decrement control and wait until 0
     *
     *  Add id of icb to    Add id of icb to
     *  list                list
     *
     *  Add value of key    Add value of key
     *  to list             to list
     */
    public void testThreadLocalAssumptions () throws InterruptedException {
        
        MyController c = new MyController () ;
        
        AbstractServerActiveObject sao = new BaseServerActiveObject () ;
        
        String icbValue [] = new String [ 2 ] ;
        String mapValue [] = new String [ 2 ] ;
        
        Thread thread_1 = new Thread (
        new ThreadLocalTask ( 0, c, sao, icbValue, mapValue )
        ) ;
        thread_1.start () ;
        
        Thread thread_2 = new Thread (
        new ThreadLocalTask ( 1, c, sao, icbValue, mapValue )
        ) ;
        thread_2.start () ;
        
        thread_1.join () ;
        thread_2.join () ;
        
        //System.out.println("icbValue:\n" + Arrays.asList ( icbValue ) );
        //System.out.println("mapValue:\n" + Arrays.asList ( mapValue) ) ;
        
        if ( icbValue [ 0 ] . equals ( icbValue [ 1 ] ) )
            fail ( "The cached InvocationContexts were not different" ) ;
        
        assertEquals ( "The first map value was wrong", "value-0", mapValue [ 0 ] ) ;
        assertEquals ( "The second map value was wrong", "value-1", mapValue [ 1 ] ) ;
    }
    
    /*
     * There is most probably a computer science semaphore
     * or monitor type I should use here.
     */
    public class MyController {
        
        int count = 2 ;
        
        public synchronized void decrement () {
            count-- ;
        }
        
        public synchronized int getValue () {
            return count ;
        }
    }
    
    public class ThreadLocalTask implements Runnable {
        
        int id ;
        MyController c ;
        AbstractServerActiveObject sao ;
        
        String [] icbValue ;
        String [] mapValue ;
        
        public ThreadLocalTask (
        int id,
        MyController c,
        AbstractServerActiveObject sao,
        String [] icbValue,
        String [] mapValue ) {
            this.id = id ;
            this.c = c ;
            this.sao = sao ;
            this.icbValue = icbValue ;
            this.mapValue = mapValue ;
        }
        
        public void run () {
            
            sao.setInvocationContext ( new StandardInvocationContext () ) ;
            sao.setThreadLocalValue ( "key", "value-" + id ) ;
            
            c.decrement () ;
            while ( c.getValue () != 0 )
                ;
            
            icbValue [ id ] = sao.getInvocationContext ().toString () ;
            mapValue [ id ] = sao.getThreadLocalValue ( "key" ).toString () ;
            
        }
        
    }
    
    public void testGetChild () {
        
        AbstractServerActiveObject sah = new AbstractServerActiveObject () {
            public void controlSelf ( InvocationContext icb ) throws Exception {} ;
            public void controlChildren ( InvocationContext icb ) throws Exception {} ;
            public void render ( InvocationContext icb ) throws Exception {} ;
        } ;
        
        AbstractElement ae = null ;
        try {
            ae = sah.getChild ( "no-such-element" ) ;
            fail ( "getChild failed to throw NoSuchAbstractElementException" ) ;
        }
        catch ( NoSuchAbstractElementException e ) {}
        
        AbstractElement element = new StandardElement () ;
        element.setName ( "actual-element" ) ;
        
        sah.addChild ( element ) ;
        ae = sah.getChild ( "actual-element" ) ;
        
    }
    
    public void testChildExists () {
        
        AbstractServerActiveObject sao = new BaseServerActiveObject () ;
        
        if (sao.childExists ("randomname")) {
            fail ("SAO says child exists, but doesn't!") ;
        }
        
        AbstractElement element = new StandardElement () ;
        element.setName ( "actual-element" ) ;
        sao.addChild ( element ) ;
        
        if (! sao.childExists ("actual-element")) {
            fail ("SAO says child doesn't exist, but does!") ;
        }
        
    }
    
    /**
     * This test case flushed a couple of bug in code apparently too simple
     * to get wrong and it highlighted some dubious equality by value tests
     * in AbstractServerActiveObject.addChild.
     */
    public void testGetDataKey () throws Exception {
        
        MySAO sao = new MySAO () ;
        
        InvocationContext icb = new StandardInvocationContext () ;
        icb.enableControlPhase () ;
        
        MockHttpServletRequest request = new MyMockHttpServletRequest () ;
        
        //out.setExpectedData ( expected ) ;
        
        icb.setHttpServletRequest ( request ) ;
        
        /* Kick it off */
        sao.controlSelf ( icb ) ;
        
        //out.verify () ;
        
        assertEquals (
        "The data key was correct when there was no parent",
        "index.html#",
        sao.getDataKeyPublic ()
        ) ;
        
        AbstractElement el_b = new StandardElement () ;
        el_b.setName ( "B" ) ;
        
        el_b.addChild ( sao ) ;
        
        assertEquals (
        "The data key was correct when there was one parent",
        "index.html#-0",
        sao.getDataKeyPublic ()
        ) ;
        
        AbstractServerActiveObject grandParent = new BaseServerActiveObject () ;
        grandParent.addChild ( el_b ) ;
        
        AbstractElement el_a = new StandardElement () ;
        el_a.setName ( "A" ) ;
        grandParent.addChild ( el_a ) ;
        
        AbstractElement el_c = new StandardElement () ;
        el_c.setName ( "C" ) ;
        grandParent.addChild ( el_c ) ;
        
        assertEquals (
        "The data key was correct when there was a grandparent",
        "index.html#-0-1",
        sao.getDataKeyPublic ()
        ) ;
        
        
        AbstractElement grandGrandParent = new StandardElement () ;
        grandGrandParent.setName ( "D" ) ;
        
        grandGrandParent.addChild ( new StandardFragment () ) ;
        grandGrandParent.addChild ( new BaseServerActiveObject () ) ;
        grandGrandParent.addChild ( new StandardFragment () ) ;
        grandGrandParent.addChild ( grandParent ) ;
        grandGrandParent.addChild ( new BaseServerActiveObject () ) ;
        
        assertEquals (
        "The data key was correct when there was a grandparent",
        "index.html#-0-1-3",
        sao.getDataKeyPublic ()
        ) ;
        
    }
    
    private class MySAO extends BaseServerActiveObject {
        
        public void controlSelf ( InvocationContext icb ) {
            setInvocationContext ( icb ) ;
        }
        
        public String getDataKeyPublic () {
            return getDataKey () ;
        }
        
    }
    
    private class MyMockHttpServletRequest extends MockHttpServletRequest  {
        
        public String getServletPath () {
            return "index.html" ;
        }
    }
    
    public void testRecycle () {
        
        /*
         * Check recycle walks the tree of saos depth first.
         * This is because breadth first makes less sense if
         * a child were to use a service of a parent. Currently
         * there are no such server-client relationships
         */
        
        /*
         *               a
         */
        List recordOfRecycling = new LinkedList () ;
        MyRecycler root = new MyRecycler ( "a", recordOfRecycling ) ;
        
        
        /*
         *               a
         *             /  \
         *           aa    ab
         */
        AbstractElement element = new StandardElement () ;
        element.setName ( "element" ) ;
        
        root.addChild ( element ) ;
        
        MyRecycler aa = new MyRecycler ( "aa", recordOfRecycling ) ;
        element.addChild ( aa ) ;
        
        MyRecycler ab = new MyRecycler ( "ab", recordOfRecycling ) ;
        element.addChild ( ab ) ;
        
        /*
         *                 a
         *             /      \
         *           aa        ab
         *        aaa  aab  aba  abb
         */
        element = new StandardElement () ;
        element.setName ( "element" ) ;
        
        aa.addChild ( element ) ;
        MyRecycler aaa = new MyRecycler ( "aaa", recordOfRecycling ) ;
        element.addChild ( aaa ) ;
        
        MyRecycler aab = new MyRecycler ( "aab", recordOfRecycling ) ;
        element.addChild ( aab ) ;
        
        element = new StandardElement () ;
        element.setName ( "element" ) ;
        
        ab.addChild ( element ) ;
        MyRecycler aba = new MyRecycler ( "aba", recordOfRecycling ) ;
        element.addChild ( aba ) ;
        
        MyRecycler abb = new MyRecycler ( "abb", recordOfRecycling ) ;
        element.addChild ( abb ) ;
        
        root.recycle () ;
        
        assertEquals (
        "recycle was invoked depth first",
        Arrays.asList ( new String [] { "aaa", "aab", "aa", "aba", "abb", "ab", "a" } ),
        recordOfRecycling
        ) ;
        
    }
    
    private static class MyRecycler extends BaseServerActiveObject {
        
        String id ;
        List recordOfRecycling ;
        
        MyRecycler ( String id, List recordOfRecycling ) {
            
            this.id = id ;
            this.recordOfRecycling = recordOfRecycling ;
        }
        
        public void recycle () {
            super.recycle () ;
            recordOfRecycling.add ( id ) ;
        }
    }
    
    
}
