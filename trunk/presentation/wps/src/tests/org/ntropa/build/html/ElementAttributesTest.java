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
 * ElementAttributesTest.java
 *
 * Created on 14 November 2001, 14:11
 */

package tests.org.ntropa.build.html;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.html.ElementAttributes;
import org.ntropa.build.html.MarkUpAttributes;


/**
 *
 * @author  jdb
 * @version $Id: ElementAttributesTest.java,v 1.2 2002/09/05 17:13:41 jdb Exp $
 */
public class ElementAttributesTest extends TestCase {
    
    /** Creates new ServerActiveHtmlAttributesTest */
    public ElementAttributesTest ( String testName ) {
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
        
        TestSuite suite = new TestSuite ( ElementAttributesTest.class );
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
        
        m.setAttribute ( "element", "my-element" ) ;
        
        ElementAttributes elementAttrs = new ElementAttributes ( m ) ;
        
        assertEquals ( "The ElementAttributes did not have the 'element' attribute",
        m.getAttribute ( "element" ),
        elementAttrs.getAttribute ( "element" )
        ) ;
        
        assertEquals ( "The ElementAttributes had the wrong name",
        "my-element",
        elementAttrs.getName ()
        ) ;
        
        /* add some placeholders */
        for ( int i = 1 ; i <= 500 ; i++ ) {
            m.setAttribute ( "placeholder-p" + i, "value-" + i ) ;
        }
        
        elementAttrs = new ElementAttributes ( m ) ;
        
        Properties placeholders = elementAttrs.getPlaceholders () ;
        assertEquals ( "The number of placeholders was wrong", 500, placeholders.size () ) ;
        for ( int i = 1 ; i <= 500 ; i++ ) {
            String placeholderName = "p" + i ;
            assertEquals ( "The ElementAttributes had the wrong value for the placeholder: " + placeholderName,
            "value-" + i,
            placeholders.getProperty ( placeholderName )
            ) ;
        }
        
    }
    
}
