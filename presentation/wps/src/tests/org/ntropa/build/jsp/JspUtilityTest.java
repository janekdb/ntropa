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
 * JspUtilityTest.java
 *
 * Created on 07 June 2002, 10:46
 */

package tests.org.ntropa.build.jsp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.jsp.JspUtility;


/**
 *
 * @author  jdb
 * @version $Id: JspUtilityTest.java,v 1.1 2002/06/07 13:36:08 jdb Exp $
 */
public class JspUtilityTest extends TestCase {
    
    /** Creates new JspUtilityTest */
    public JspUtilityTest( String testName ) {
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
    public static Test suite() {
        
        TestSuite suite = new TestSuite( JspUtilityTest.class );
        return suite;
    }
    
    public void testSafe() {
        
        assertEquals( "String with no special characters or special character sequences was unchanged",
        "\"abcdef\"", JspUtility.makeSafeQuoted( "abcdef" ) ) ;
        
        assertEquals( "String with special character was correct",
        "\"abcd\\\"ef\"", JspUtility.makeSafeQuoted( "abcd\"ef" ) ) ;
        
        assertEquals( "String with opening tag was split",
        "\"abc<\" + \"%def\"", JspUtility.makeSafeQuoted( "abc<%def" ) ) ;
        
        assertEquals( "String with closing tag was split",
        "\"abc%\" + \">def\"", JspUtility.makeSafeQuoted( "abc%>def" ) ) ;
        
        assertEquals( "String with opening tag and closing tag was split",
        "\"abc<\" + \"%def%\" + \">\"", JspUtility.makeSafeQuoted( "abc<%def%>" )  ) ;
        
        /*
         * This is the html fragment which exposed the bug in the escaping of text
         * when making JSPs.
         */
        String needsSafe = "<td width=100%>&nbsp;" ;
        String safe = "\"<td width=100%\" + \">&nbsp;\"" ;
        assertEquals( "Original bug exposer was correctly made safe",
        safe, JspUtility.makeSafeQuoted( needsSafe ) ) ;
        
    }
}
