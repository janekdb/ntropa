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
 * PlaceholderReplacementStackTest.java
 *
 * Created on 24 March 2003, 16:27
 */

package tests.org.ntropa.runtime.sao;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.runtime.sao.PlaceholderReplacementStack;


/**
 *
 * @author  Janek Bogucki
 * @version $Id: PlaceholderReplacementStackTest.java,v 1.3 2003/05/02 13:59:42 jdb Exp $
 */
public class PlaceholderReplacementStackTest extends TestCase {
    
    /** Creates new AbstactServerActiveObjectTest */
    public PlaceholderReplacementStackTest ( String testName ) {
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
        
        TestSuite suite = new TestSuite ( PlaceholderReplacementStackTest.class );
        return suite;
    }
    
    
    public void testBasicBehaviour () {
        
        PlaceholderReplacementStack s = new PlaceholderReplacementStack () ;
        
        String NAME = "p1" ;
        
        assertEquals ( "Empty stack gave null", null, s.getPlaceholderReplacement ( NAME ) ) ;
        
        s.push ( NAME, "p1-value" ) ;
        assertEquals ( "stack with one pushed gave correct value", "p1-value", s.getPlaceholderReplacement ( NAME ) ) ;
        
        s.push ( "foo", "bar" ) ;
        s.push ( NAME, "p1-SECOND-value" ) ;
        assertEquals ( "stack with three pushed gave correct value", "p1-SECOND-value", s.getPlaceholderReplacement ( NAME ) ) ;
        
        s.pop () ;
        assertEquals ( "stack with one popped gave correct value", "p1-value", s.getPlaceholderReplacement ( NAME ) ) ;
        
        s.pop () ;
        s.pop () ;
        assertEquals ( "stack with three popped gave null", null, s.getPlaceholderReplacement ( NAME ) ) ;
        
        
    }
    
    
    public void testParameterizedPop () {
        
        PlaceholderReplacementStack s = new PlaceholderReplacementStack () ;
        
        String NAME = "p1" ;
        
        s.push ( NAME, "value-1" ) ;
        s.push ( NAME, "value-2" ) ;
        s.push ( NAME, "value-3" ) ;
        
        assertEquals ( "stack with three items gave correct value", "value-3", s.getPlaceholderReplacement ( NAME ) ) ;
        
        s.pop ( 2 ) ;
        
        assertEquals ( "stack had one item after pop ( 2 )", "value-1", s.getPlaceholderReplacement ( NAME ) ) ;
        
    }
    
    public void testMark () {
        
        PlaceholderReplacementStack s = new PlaceholderReplacementStack () ;
        
        /*
         * 1
         */
        s.mark () ;
        
        s.popToMark () ;
        
        /*
         * 2
         */
        s.mark () ;
        s.mark () ;
        s.popToMark () ;
        s.popToMark () ;
        
        /*
         * 3
         */
        s = new PlaceholderReplacementStack () ;
        
        try {
            s.popToMark () ;
            fail ( "popToMark was rejected when no marks" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        /*
         * 4
         */
        s = new PlaceholderReplacementStack () ;
        
        s.mark () ;
        s.popToMark () ;
        try {
            s.popToMark () ;
            fail ( "popToMark was rejected when no marks after adding one then removing it" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        /*
         * 5: Check the stack is reduced to the correct size
         */
        s = new PlaceholderReplacementStack () ;
        String NAME = "p1" ;
        
        s.mark () ;
        s.push ( NAME, "value-1" ) ;
        s.popToMark () ;
        assertEquals ( "popToMark correctly removed item (1)", null, s.getPlaceholderReplacement ( NAME ) ) ;
        
        for ( int i = 1 ; i <= 5 ; i++ ) {
            s.push ( NAME, "value-" + i ) ;
            s.mark () ;
        }
        s.push ( NAME, "value-top" ) ;
        
        for ( int i = 5 ; i >= 1 ; i-- ) {
            s.popToMark () ;
            assertEquals ( "popToMark correctly removed item (2)", "value-" + i , s.getPlaceholderReplacement ( NAME )) ;
        }
        
        /*
         * Same test but with some gaps
         */
        s = new PlaceholderReplacementStack () ;
        for ( int i = 1 ; i <= 5 ; i++ ) {
            s.push ( NAME, "value-" + i ) ;
            s.mark () ;
            for ( int j = 1 ; j<= i ; j++ )
                s.push ( NAME, "value-" + i + "-" + j ) ;
        }
        s.push ( NAME, "value-top" ) ;
        
        for ( int i = 5 ; i >= 1 ; i-- ) {
            s.popToMark () ;
            assertEquals ( "popToMark correctly removed item (3)", "value-" + i, s.getPlaceholderReplacement ( NAME ) ) ;
        }
        
        
    }
}
