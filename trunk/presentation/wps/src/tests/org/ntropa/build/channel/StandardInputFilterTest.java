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
 * StandardInputFilterTest.java
 *
 * Created on 03 December 2002, 16:52
 */

package tests.org.ntropa.build.channel;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.channel.StandardInputFilter;
import org.ntropa.utility.FilePredicate;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.NtropaTestWorkDirHelper;




/**
 *
 * @author  Janek Bogucki
 * @version $Id: StandardInputFilterTest.java,v 1.2 2002/12/09 18:05:14 jdb Exp $
 */
public class StandardInputFilterTest extends TestCase {
    
    
    public StandardInputFilterTest ( String testName ) {
        super(testName);
    }
    
    
    private File _topFolder ;
    
    protected void setUp () throws Exception {

        String TOP_FOLDER = new NtropaTestWorkDirHelper().getWorkDir() + "/tests.org.ntropa.build.channel.StandardInputFilterTest";

        _topFolder = new File ( TOP_FOLDER ) ;
        if ( ! _topFolder.mkdirs () )
            throw new Exception ( "Failed to create folder or folder already existed: " + TOP_FOLDER ) ;
        
    }
    
    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown () throws Exception {
        
        FileUtilities.killDirectory ( _topFolder ) ;
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
        
        TestSuite suite = new TestSuite ( StandardInputFilterTest.class );
        return suite;
    }
    
    /*
     * Test link files are permitted
     */
    public void testLinkFilesAccepted () throws Exception {
        
        FilePredicate fp = new StandardInputFilter () ;
        
        /* Link to a HTML file */
        File linkFile = new File ( _topFolder, "advice.html.link" ) ;
        linkFile.createNewFile () ;
        
        assertTrue ( "Link file 'advice.html.link' was accepted", fp.accept ( linkFile ) ) ;
        
        /* Link to a directory */
        linkFile = new File ( _topFolder, "general-info.link" ) ;
        linkFile.createNewFile () ;
        
        assertTrue ( "Link file to 'general-info.link' file was accepted", fp.accept ( linkFile ) ) ;
        
    }
}
