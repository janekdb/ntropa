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
 * ServerActiveHtmlAttributesTest.java
 *
 * Created on 14 November 2001, 13:50
 */

package tests.org.ntropa.build.html;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.html.MarkUpAttributes;
import org.ntropa.build.html.ServerActiveHtmlAttributes;


/**
 *
 * @author  jdb
 * @version $Id: ServerActiveHtmlAttributesTest.java,v 1.2 2002/09/04 16:18:09 jdb Exp $
 */
public class ServerActiveHtmlAttributesTest extends TestCase {
    
    /** Creates new ServerActiveHtmlAttributesTest */
    public ServerActiveHtmlAttributesTest ( String testName ) {
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
        
        TestSuite suite = new TestSuite ( ServerActiveHtmlAttributesTest.class );
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
        
        m.setAttribute ( "name", "my-sah" ) ;
        
        ServerActiveHtmlAttributes sahAttrs = new ServerActiveHtmlAttributes ( m ) ;
        
        assertEquals ( "The ServerActiveHtmlAttributes had the 'name' attribute",
        m.getAttribute ( "name" ),
        sahAttrs.getAttribute ( "name" )
        ) ;
        
        assertEquals ( "The ServerActiveHtmlAttributes had the right name",
        "my-sah",
        sahAttrs.getName ()
        ) ;
        
        //System.out.println ("m.getAttribute ( 'name' )" + m.getAttribute ( "name" ) );
        //System.out.println ("sahAttrs.getAttribute ( 'name' )" + sahAttrs.getAttribute ( "name" ) );
        
        if ( sahAttrs.usesTemplate () )
            fail ( "The ServerActiveHtmlAttributes reported it did not use a template when it does not" ) ;
        
        /* add a template */
        m.setAttribute ( "use-template", "my-template" ) ;
        
        sahAttrs = new ServerActiveHtmlAttributes ( m ) ;
        
        assertEquals ( "The ServerActiveHtmlAttributes had the 'name' attribute",
        m.getAttribute ( "name" ),
        sahAttrs.getAttribute ( "name" )
        ) ;
        assertEquals ( "The ServerActiveHtmlAttributes had the 'use-template' attribute",
        m.getAttribute ( "use-template" ),
        sahAttrs.getAttribute ( "use-template" )
        ) ;
        
        if ( ! sahAttrs.usesTemplate () )
            fail ( "The ServerActiveHtmlAttributes reported it uses a template when it does" ) ;
        
        /* add some placeholders */
        for ( int i = 1 ; i <= 500 ; i++ ) {
            m.setAttribute ( "placeholder-p" + i, "value-" + i ) ;
        }
        
        sahAttrs = new ServerActiveHtmlAttributes ( m ) ;
        
        Properties placeholders = sahAttrs.getPlaceholders () ;
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
