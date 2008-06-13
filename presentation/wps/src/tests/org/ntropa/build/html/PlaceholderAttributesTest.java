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
 * PlaceholderAttributesTest.java
 *
 * Created on 05 September 2002, 12:42
 */

package tests.org.ntropa.build.html;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.html.MarkUpAttributes;
import org.ntropa.build.html.PlaceholderAttributes;


/**
 *
 * @author  jdb
 * @version $Id: PlaceholderAttributesTest.java,v 1.1 2002/09/05 17:19:01 jdb Exp $
 */
public class PlaceholderAttributesTest extends TestCase {
    
    /** Creates new PlaceholderAttributesTest */
    public PlaceholderAttributesTest ( String testName ) {
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
        
        TestSuite suite = new TestSuite ( PlaceholderAttributesTest.class );
        return suite;
    }
    
    /*
    protected void setUp () throws Exception {}
     */
    
    /*
    protected void tearDown () throws Exception {}
     */
    
    public void testConstructor () {
        
        MarkUpAttributes m = new MarkUpAttributes () ;
        
        m.setAttribute ( "use-element", "element-to-use" ) ;
        
        PlaceholderAttributes placeholderAttrs = new PlaceholderAttributes ( m ) ;
        
        assertEquals ( "The PlaceholderAttributes did not have the 'use-element' attribute",
        m.getAttribute ( "use-element" ),
        placeholderAttrs.getAttribute ( "use-element" )
        ) ;
        
        assertEquals ( "The PlaceholderAttributes API was broken",
        m.getAttribute ( "use-element" ),
        placeholderAttrs.getUseElement ()
        ) ;
        
    }
    
}
