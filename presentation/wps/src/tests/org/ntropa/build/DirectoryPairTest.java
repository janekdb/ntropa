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
 * DirectoryPairTest.java
 *
 * Created on July 30, 2001, 3:42 PM
 */

package tests.org.ntropa.build;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.DirectoryPair;
import org.ntropa.build.DirectoryPairException;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.NtropaTestWorkDirHelper;


/**
 *
 * @author  jdb
 * @version $Id: DirectoryPairTest.java,v 1.3 2001/10/18 21:15:38 jdb Exp $
 */
public class DirectoryPairTest extends TestCase {
    
    protected File m_TestRoot ;
    
    protected File m_Source ;
    protected File m_Destination ;
    
    
    public DirectoryPairTest ( String testName ) {
        super(testName);
    }
    
    protected void setUp () throws java.io.IOException {

        m_TestRoot = new File(new NtropaTestWorkDirHelper().getWorkDir() + "/tests.org.ntropa.build.DirectoryPairTest");

        //m_TestRoot= new File ( TEST_ROOT ) ;
        if ( ! m_TestRoot.mkdir () )
            fail ( "Failed to created temporary directory for test." ) ;
        
        m_Source = new File ( m_TestRoot, "source" ) ;
        if ( ! m_Source.mkdir () )
            fail ( "Failed to created temporary directory for test." ) ;
        
        m_Destination = new File ( m_TestRoot, "destination" ) ;
        if ( ! m_Destination.mkdir () )
            fail ( "Failed to created temporary directory for test." ) ;
        
    }
    
    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown () throws Exception {
        
        FileUtilities.killDirectory ( m_TestRoot ) ;
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
        
        TestSuite suite = new TestSuite ( DirectoryPairTest.class );
        return suite;
    }
    
    /**
     * Makes an object that should succeed.
     */
    public void testGoodArgs () {
        
        try {
            DirectoryPair dp = new DirectoryPair ( m_Source, m_Destination ) ;
        }
        catch ( DirectoryPairException e ) {
            fail ( "Correct arguments rejected." ) ;
        }
    }
    
    /**
     * Makes an object that should fail.
     */
    public void testBadArgs () throws IOException {
        
        /* relative */
        File relative = new File ( "some-name" ) ;
        
        try {
            DirectoryPair dp = new DirectoryPair ( relative, m_Destination ) ;
            fail ( "Relative path for first argument accepted." ) ;
        }
        catch ( DirectoryPairException e ) {}
        
        try {
            DirectoryPair dp = new DirectoryPair ( m_Source, relative ) ;
            fail ( "Relative path for second argument accepted." ) ;
        }
        catch ( DirectoryPairException e ) {}
        
        /* non-directory */
        File aFile = new File ( m_TestRoot, "a-file" ) ;
        aFile.createNewFile () ;
        
        try {
            DirectoryPair dp = new DirectoryPair ( aFile, m_Destination ) ;
            fail ( "Non-directory path for first argument accepted." ) ;
        }
        catch ( DirectoryPairException e ) {}
        
        try {
            DirectoryPair dp = new DirectoryPair ( m_Source, aFile ) ;
            fail ( "Non-directory path for second argument accepted." ) ;
        }
        catch ( DirectoryPairException e ) {}
        
        /* zero length */
        File zeroLenFile = new File ( "" ) ;
        try {
            DirectoryPair dp = new DirectoryPair ( zeroLenFile, m_Destination ) ;
            fail ( "Zero length path for first argument accepted." ) ;
        }
        catch ( DirectoryPairException e ) {}
        
        try {
            DirectoryPair dp = new DirectoryPair ( m_Source, zeroLenFile ) ;
            fail ( "Zero length path for second argument accepted." ) ;
        }
        catch ( DirectoryPairException e ) {}
        
    }
    
    public void testAccessors () throws DirectoryPairException {
        
        DirectoryPair dp = new DirectoryPair ( m_Source, m_Destination ) ;
        
        compareValues ( dp, m_Source, m_Destination ) ;
        
    }
    
    public void testConstructors () throws DirectoryPairException {
        DirectoryPair dp ;
        
        dp = new DirectoryPair ( m_Source, m_Destination ) ;
        compareValues ( dp, m_Source, m_Destination ) ;
        
        dp = new DirectoryPair ( m_TestRoot.getPath() + File.separator + "source", m_Destination ) ;
        compareValues ( dp, m_Source, m_Destination ) ;
        
        dp = new DirectoryPair ( m_Source, m_TestRoot.getPath() + File.separator + "destination" ) ;
        compareValues ( dp, m_Source, m_Destination ) ;
        
        dp = new DirectoryPair ( m_TestRoot.getPath() + File.separator + "source", m_TestRoot.getPath() + File.separator + "destination" ) ;
        compareValues ( dp, m_Source, m_Destination ) ;
    }
    
    /* 
     * I became a bit concerned DirectoryPair was not taking copies of the args
     * then I failed to come up with a way of changing the path info in the object
     * after construction. This appears to be A Good Thing(tm).
     */
    /*
     public void testArgsAreCopied () throws DirectoryPairException {}
     */
    
    protected void compareValues ( DirectoryPair dp, File expectedSource, File expectedDestination ) {
        
        if ( ! dp.getSource ().equals ( expectedSource ) )
            fail ( "getSource () broken." ) ;
        
        if ( ! dp.getDestination ().equals ( expectedDestination ) )
            fail ( "getDestination () broken." ) ;
        
    }
}
