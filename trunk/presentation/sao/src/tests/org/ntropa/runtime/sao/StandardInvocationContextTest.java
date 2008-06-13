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
 * StandardInvocationContextTest.java
 *
 * Created on 29 January 2002, 16:49
 */

package tests.org.ntropa.runtime.sao;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.runtime.sao.InvocationContext;
import org.ntropa.runtime.sao.StandardInvocationContext;


/**
 *
 * @author  jdb
 * @version $Id: StandardInvocationContextTest.java,v 1.5 2003/03/24 17:00:22 jdb Exp $
 */
public class StandardInvocationContextTest extends TestCase {
    
    public StandardInvocationContextTest ( String testName ) {
        super(testName);
    }
    
    
    /* Comments copied from junit.framework.TestSuite. */
    
    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests.
     * It runs a collection of test cases. Here is an example using
     * the dynamic test definition.
     * <pre>
     * TestSuite suite= new TestSuite();
     * suite.addTest(new MathTest("testAdd"));
     * suite.addTest(new MathTest("testDivideByZero"));
     * </pre>
     * Alternatively, a TestSuite can extract the tests to be run automatically.
     * To do so you pass the class of your TestCase class to the
     * TestSuite constructor.
     * <pre>
     * TestSuite suite= new TestSuite(MathTest.class);
     * </pre>
     * This constructor creates a suite with all the methods
     * starting with "test" that take no arguments.
     *
     * @see Test
     */
    public static Test suite () {
        TestSuite suite = new TestSuite ( StandardInvocationContextTest.class );
        
        return suite;
    }
    
    public void testContextByPhase () throws IOException {
        
        InvocationContext ic = new StandardInvocationContext () ;
        
        ic.enableControlPhase () ;
        try {
            JspWriter j = ic.getJspWriter () ;
            fail ( "Access to the JspWriter was allowed" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        ic.enableRenderPhase () ;
        try {
            JspWriter j = ic.getJspWriter () ;
        }
        catch ( IllegalStateException e ) {
            fail ( "Access to the JspWriter was not allowed" ) ;
        }
        
        ic.enableRenderPhase () ;
        try {
            ic.getController ().sendRedirect ( "https://foo-bar-ebank.cx/my-account?deposit=10100200&currency=euro" ) ;
            fail ( "Invocation of sendRedirect () was allowed" ) ;
        }
        catch ( IllegalStateException e ) {}
    }
    
    public void testDisable () {
        
        InvocationContext ic = new StandardInvocationContext () ;
        
        ic.disable () ;
        try {
            ic.getPageContext () ;
            fail ( "Access to getPageContext was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        try {
            ic.getHttpSession () ;
            fail ( "Access to getHttpSession was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        try {
            ic.getServletContext () ;
            fail ( "Access to getServletContext was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        try {
            ic.getServletConfig () ;
            fail ( "Access to getServletConfig was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        try {
            ic.getJspWriter () ;
            fail ( "Access to getJspWriter was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        try {
            ic.getPage () ;
            fail ( "Access to getPage was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        try {
            ic.getHttpServletRequest () ;
            fail ( "Access to getHttpServletRequest was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        try {
            ic.getHttpServletResponse () ;
            fail ( "Access to getHttpServletResponse was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        try {
            ic.enableControlPhase () ;
            fail ( "Access to enableControlPhase was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        try {
            ic.enableRenderPhase () ;
            fail ( "Access to enableRenderPhase was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        try {
            ic.isControlPhase () ;
            fail ( "Access to isControlPhase was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        try {
            ic.getController () ;
            fail ( "Access to getController was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        try {
            ic.sendRedirectAllowed () ;
            fail ( "Access to sendRedirectAllowed was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        try {
            ic.disable () ;
            fail ( "Access to disable was allowed after the invocation context had been disabled" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        
    }
    
}


