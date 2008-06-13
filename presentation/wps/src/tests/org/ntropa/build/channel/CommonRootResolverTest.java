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
 * CommonRootResolverTest.java
 *
 * Created on 10 December 2002, 23:16
 */

package tests.org.ntropa.build.channel;

import java.io.File;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.ContextPath;
import org.ntropa.build.channel.CommonRootResolver;
import org.ntropa.build.mapper.LinkFile;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.NtropaTestWorkDirHelper;



/**
 *
 * @author  Janek Bogucki
 * @version $Id: CommonRootResolverTest.java,v 1.2 2002/12/11 14:34:10 jdb Exp $
 */
public class CommonRootResolverTest extends TestCase {
    
    public CommonRootResolverTest ( String testName) {
        super (testName);
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
        
        TestSuite suite = new TestSuite ( CommonRootResolverTest.class );
        
        return suite;
    }
    
    protected File m_TestRoot ;
    
    protected File m_WebDAVDir ;
    protected File m_SymlinkDir ;
    
    protected File mbaWebDAV ;
    protected File mbaSymlinkDir ;
    
    protected void setUp () throws java.io.IOException {
        
        String TEST_ROOT = new NtropaTestWorkDirHelper().getWorkDir() + "/tests.org.ntropa.build.channel.CommonRootResolverTest";

        m_TestRoot= new File ( TEST_ROOT ) ;
        
        if ( ! m_TestRoot.mkdir () )
            fail ( "Failed to created temporary directory for test." ) ;
        
        /* master */
        m_WebDAVDir = new File ( TEST_ROOT, "webdav" ) ;
        if ( ! m_WebDAVDir.mkdirs () )
            fail ( "Failed to create directory: " + m_WebDAVDir.getPath () ) ;
        mbaWebDAV = new File ( m_WebDAVDir, "mba" ) ;
        if ( ! mbaWebDAV.mkdir () )
            fail ( "Failed to create directory: " + mbaWebDAV.getPath () ) ;
        
        m_SymlinkDir = new File ( TEST_ROOT, "sym" ) ;
        if ( ! m_SymlinkDir.mkdirs () )
            fail ( "Failed to create directory: " + m_SymlinkDir.getPath () ) ;
        mbaSymlinkDir = new File ( m_SymlinkDir, "mba" ) ;
        if ( ! mbaSymlinkDir.mkdir () )
            fail ( "Failed to create directory: " + mbaSymlinkDir.getPath () ) ;
        
    }
    
    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown () throws Exception {
        FileUtilities.killDirectory ( m_TestRoot ) ;
    }
    
    
    public void testResolution () throws Exception {
        
        CommonRootResolver crr = new CommonRootResolver ( m_SymlinkDir, Collections.singleton ( new ContextPath ( "mba" ) ) ) ;
        
        File targetFile = new File ( mbaWebDAV, "advice.html" ) ;
        FileUtilities.writeString ( targetFile, "some content" ) ;
        
        FileUtilities.makeSymbolicLink ( targetFile, new File ( mbaSymlinkDir, "advice.html" ) ) ;
        
        LinkFile linkFile = new LinkFile ( "wps://mba/advice.html" ) ;
        
        File resolved = crr.resolve ( linkFile ) ;
        
        File expected = new File ( mbaSymlinkDir, "advice.html" ) ;
        
        assertEquals ( "The resolved file was correct", expected.getAbsolutePath (), resolved.getAbsolutePath () ) ;
        
        linkFile = new LinkFile ( "wps://mba/../index.html" ) ;
        try {
            resolved = crr.resolve ( linkFile ) ;
            fail ( "Directory traversal attempt rejected" ) ;
        }
        catch ( IllegalArgumentException e ) {
            if ( e.getMessage ().indexOf (
            "There was a security problem with the resolved file (directory traversal attempt): "
            ) == -1 )
                fail ( "Correct problem detected" ) ;
        }
        
        linkFile = new LinkFile ( "wps://mba/%2e%2e/temp" ) ;
        try {
            resolved = crr.resolve ( linkFile ) ;
            fail ( "Disallowed characters rejected" ) ;
        }
        catch ( IllegalArgumentException e ) {
            if ( e.getMessage ().indexOf (
              "There was a security problem with the resolved file (directory traversal attempt): "
            //"There was a security problem with the resolved file (dissallowed characters present): "
            ) == -1 )
                fail ( "Correct problem detected: " + e.getMessage () ) ;
        }
    }
}
