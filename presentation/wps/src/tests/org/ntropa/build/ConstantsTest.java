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
 * ConstantsTest.java
 *
 * Created on 24 October 2001, 13:03
 */

package tests.org.ntropa.build;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.Constants;


/**
 *
 * @author  jdb
 * @version $Id: ConstantsTest.java,v 1.2 2001/10/25 20:15:50 jdb Exp $
 */
public class ConstantsTest extends TestCase {
    
    public ConstantsTest( String testName ) {
        super(testName);
    }
    
    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests.
     * It runs a collection of test cases.
     *
     * This constructor creates a suite with all the methods
     * starting with "test" that take no arguments.
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite( ConstantsTest.class );
        return suite;
    }
    
    /**
     * Each set returned from should be a copy.
     */
    public void testCopiesAreReturned() {
        
       if ( Constants.getNonHtmlDirectoryNames() == Constants.getNonHtmlDirectoryNames() )
            fail( "Constants.getNonHtmlDirectoryNames () did not return a copy" ) ;
        
       if ( Constants.getMirroredFileSuffixes() == Constants.getMirroredFileSuffixes() )
           fail( "Constants.getMirroredFileSuffixes () did not return a copy" ) ;
        
       if ( Constants.getSystemDirectoryNames() == Constants.getSystemDirectoryNames() )
           fail( "Constants.getSystemDirectoryNames () did not return a copy" ) ;
    }
}
