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
 * FileLocationTest.java
 *
 * Created on 08 August 2001, 18:43
 */

package tests.org.ntropa.build.channel;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.channel.FileLocation;
import org.ntropa.build.channel.FileLocationException;


/**
 *
 * @author  jdb
 * @version $Id: FileLocationTest.java,v 1.2 2001/08/31 16:20:54 jdb Exp $
 */
public class FileLocationTest extends TestCase {
    
    
    public FileLocationTest( String testName ) {
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
        
        TestSuite suite = new TestSuite( FileLocationTest.class );
        return suite;
    }
    
    /**
     * Check the constructor throws an Exception when given bad arguments.
     */
    public void testConstructor() {
        
        try {
            FileLocation f = new FileLocation( ( String) null ) ;
            fail( "null string accepted." );
        }
        catch ( FileLocationException e ) {}
        
        try {
            FileLocation f = new FileLocation( "" ) ;
            fail( "Empty string accepted." );
        }
        catch ( FileLocationException e ) {}
        
        try {
            FileLocation f = new FileLocation( "/" ) ;
            FileLocation g = new FileLocation( "/index.html" ) ;
            fail( "non-relative string accepted." );
        }
        catch ( FileLocationException e ) {}
        
        try {
            FileLocation f = new FileLocation( "okay.html" ) ;
            FileLocation g = new FileLocation( "advice/okay.html" ) ;
        }
        catch ( FileLocationException e ) {
            fail( "Good argument rejected." ) ;
        }
        
    }
    
    /**
     * Check constructor FileLocation ( FileLocation ).
     */
    public void testConstructorFromSameObject() throws FileLocationException {
        
        FileLocation f = new FileLocation( "advice/okay.html" ) ;
        FileLocation g = new FileLocation( f ) ;
        assertEquals ( "Constructor from FileLocation object failed.", f.getLocation (), g.getLocation () ) ;
    }
}
