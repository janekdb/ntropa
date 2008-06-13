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
 * StandardFilePredicateTest.java
 *
 * Created on 17 October 2001, 21:29
 */

package tests.org.ntropa.utility;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.utility.FilePredicate;
import org.ntropa.utility.StandardFilePredicate;



/**
 *
 * @author  jdb
 * @version $Id: StandardFilePredicateTest.java,v 1.4 2002/12/04 00:31:07 jdb Exp $
 */
public class StandardFilePredicateTest extends TestCase {
    
    static List _filePaths ;
    static List _dirPaths ;
    
    /** Creates new StandardFilePredicateTest */
    public StandardFilePredicateTest ( String testName ) {
        super(testName);
    }
    
    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests.
     * It runs a collection of test cases.
     */
    public static Test suite () {
        
        TestSuite suite = new TestSuite ( StandardFilePredicateTest.class );
        return suite;
    }
    
    protected void setUp () throws Exception {
        
        
        _filePaths = Arrays.asList ( new String [] {
            "",             //1
            "file.aa",      //2
            "file.ab",      //3
            "dir/file.aa",  //4
            "dir/file.ab",  //5
            "file.xa",      //6
            "file.xb",      //7
            "dir/file.xa",  //8
            "dir/file.xb",  //9
            "file.other",   //10
            "dir/file.other"//11
        } ) ;
        
        _dirPaths = Arrays.asList ( new String [] {
            "",
            
            "xn",
            "xm",
            "an",
            "am",
            
            "file",
            "xn/file",
            "xm/file",
            "xm/xn/file",
            "an/file",
            "an/xn/file",
            "an/xm/file",
            "an/xm/xn/file",
            
            "am/file",
            "am/xn/file",
            "am/xm/file",
            "am/xm/xn/file",
            "am/an/file",
            "am/an/xn/file",
            "am/an/xm/file",
            "am/an/xm/xn/file",
            
            "other/file",
            "xn/other/file",
            "xm/other/file",
            "xm/xn/other/file",
            "an/other/file",
            "an/xn/other/file",
            "an/xm/other/file",
            "an/xm/xn/other/file",
            
            "am/other/file",
            "am/xn/other/file",
            "am/xm/other/file",
            "am/xm/xn/other/file",
            "am/an/other/file",
            "am/an/xn/other/file",
            "am/an/xm/other/file",
            "am/an/xm/xn/other/file",
            
        } ) ;
        
        
    }
    
    public void testFiles () {
        
        StandardFilePredicate fp = new StandardFilePredicate () ;
        
        Set allowedFileSuffixes = new TreeSet () ;
        allowedFileSuffixes.add ( ".aa" ) ;
        allowedFileSuffixes.add ( ".ab" ) ;
        
        fp.setIncludeFileSuffixes ( allowedFileSuffixes ) ;
        
        check ( fp, _filePaths, new boolean [] {
            false,          //"",             //1
            true,           //"file.aa",      //2
            true,           //"file.ab",      //3
            true,           //"dir/file.aa",  //4
            true,           //"dir/file.ab",  //5
            false,          //"file.xa",      //6
            false,          //"file.xb",      //7
            false,          //"dir/file.xa",  //8
            false,          //"dir/file.xb",  //9
            false,          //"file.other",   //10
            false,          //"dir/file.other"//11
        } );
        
        Set disallowedFileSuffixes = new TreeSet () ;
        disallowedFileSuffixes.add ( ".xa" ) ;
        disallowedFileSuffixes.add ( ".xb" ) ;
        
        fp.setExcludeFileSuffixes ( disallowedFileSuffixes ) ;
        
        check ( fp, _filePaths, new boolean [] {
            false,          //"",             //1
            true,           //"file.aa",      //2
            true,           //"file.ab",      //3
            true,           //"dir/file.aa",  //4
            true,           //"dir/file.ab",  //5
            false,          //"file.xa",      //6
            false,          //"file.xb",      //7
            false,          //"dir/file.xa",  //8
            false,          //"dir/file.xb",  //9
            true,          //"file.other",   //10
            true,           //"dir/file.other"//11
        } );
    }
    
    public void testDirectories () {
        
        StandardFilePredicate fp = new StandardFilePredicate () ;
        
        Set requiredDirNames = new TreeSet () ;
        requiredDirNames.add ( "am" ) ;
        requiredDirNames.add ( "an" ) ;
        
        fp.setIncludeDirectoryNames ( requiredDirNames ) ;
        
        check ( fp, _dirPaths, new boolean [] {
            false,              //"",
            
            false,              //"xn",
            false,              //"xm",
            true,               //"an",
            true,               //"am",
            
            false,              //"file"
            false,              //"xn/file",
            false,              //"xm/file",
            false,              //"xm/xn/file",
            true,               //"an/file",
            true,               //"an/xn/file",
            true,               //"an/xm/file",
            true,               //"an/xm/xn/file",
            
            true,               //"am/file"
            true,               //"am/xn/file",
            true,               //"am/xm/file",
            true,               //"am/xm/xn/file",
            true,               //"am/an/file",
            true,               //"am/an/xn/file",
            true,               //"am/an/xm/file",
            true,               //"am/an/xm/xn/file",
            
            false,              //"other/file"
            false,              //"xn/other/file",
            false,              //"xm/other/file",
            false,              //"xm/xn/other/file",
            true,               //"an/other/file",
            true,               //"an/xn/other/file",
            true,               //"an/xm/other/file",
            true,               //"an/xm/xn/other/file",
            
            true,               //"am/other/file"
            true,               //"am/xn/other/file",
            true,               //"am/xm/other/file",
            true,               //"am/xm/xn/other/file",
            true,               //"am/an/other/file",
            true,               //"am/an/xn/other/file",
            true,               //"am/an/xm/other/file",
            true,               //"am/an/xm/xn/other/file",
            
        } ) ;
        
        fp = new StandardFilePredicate () ;
        
        Set disallowedDirNames = new TreeSet () ;
        disallowedDirNames.add ( "xm" ) ;
        disallowedDirNames.add ( "xn" ) ;
        
        fp.setExcludeDirectoryNames ( disallowedDirNames ) ;
        
        check ( fp, _dirPaths, new boolean [] {
            false,              //"",
            
            false,             //"xn",
            false,             //"xm",
            true,              //"an",
            true,              //"am",
            
            true,              //"file"
            false,             //"xn/file",
            false,             //"xm/file",
            false,             //"xm/xn/file",
            true,              //"an/file",
            false,             //"an/xn/file",
            false,             //"an/xm/file",
            false,             //"an/xm/xn/file",
            
            true,              //"am/file"
            false,             //"am/xn/file",
            false,             //"am/xm/file",
            false,             //"am/xm/xn/file",
            true,              //"am/an/file",
            false,             //"am/an/xn/file",
            false,             //"am/an/xm/file",
            false,             //"am/an/xm/xn/file",
            
            true,              //"other/file"
            false,             //"xn/other/file",
            false,             //"xm/other/file",
            false,             //"xm/xn/other/file",
            true,              //"an/other/file",
            false,             //"an/xn/other/file",
            false,             //"an/xm/other/file",
            false,             //"an/xm/xn/other/file",
            
            true,              //"am/other/file"
            false,             //"am/xn/other/file",
            false,             //"am/xm/other/file",
            false,             //"am/xm/xn/other/file",
            true,              //"am/an/other/file",
            false,             //"am/an/xn/other/file",
            false,             //"am/an/xm/other/file",
            false,             //"am/an/xm/xn/other/file",
            
        } ) ;
        
        fp = new StandardFilePredicate () ;
        
        fp.setIncludeDirectoryNames ( requiredDirNames ) ;
        fp.setExcludeDirectoryNames ( disallowedDirNames ) ;
        
        check ( fp, _dirPaths, new boolean [] {
            false,              //"",
            
            false,             //"xn",
            false,             //"xm",
            true,              //"an",
            true,              //"am",
            
            false,              //"file"
            false,             //"xn/file",
            false,             //"xm/file",
            false,             //"xm/xn/file",
            true,              //"an/file",
            false,             //"an/xn/file",
            false,             //"an/xm/file",
            false,             //"an/xm/xn/file",
            
            true,              //"am/file"
            false,             //"am/xn/file",
            false,             //"am/xm/file",
            false,             //"am/xm/xn/file",
            true,              //"am/an/file",
            false,             //"am/an/xn/file",
            false,             //"am/an/xm/file",
            false,             //"am/an/xm/xn/file",
            
            false,              //"other/file"
            false,             //"xn/other/file",
            false,             //"xm/other/file",
            false,             //"xm/xn/other/file",
            true,              //"an/other/file",
            false,             //"an/xn/other/file",
            false,             //"an/xm/other/file",
            false,             //"an/xm/xn/other/file",
            
            true,              //"am/other/file"
            false,             //"am/xn/other/file",
            false,             //"am/xm/other/file",
            false,             //"am/xm/xn/other/file",
            true,              //"am/an/other/file",
            false,             //"am/an/xn/other/file",
            false,             //"am/an/xm/other/file",
            false,             //"am/an/xm/xn/other/file",
            
        } ) ;
    }
    
    /* shared methods */
    
    protected void check ( FilePredicate fp, List filePaths, boolean [] expected ) {
        
        Iterator it = filePaths.iterator () ;
        int i = 0 ;
        while ( it.hasNext () ) {
            String filepath = (String) it.next () ;
            
            assertEquals ( "FilePredicate wrong for file path: " + filepath + "\n" + fp,
            expected [ i ], fp.accept ( new File ( filepath ) ) ) ;
            
            i++ ;
        }
        
    }
    
    /**
     * Once used to view the output. Not a neccessary test.
     */
    public void testToString () {
        
        StandardFilePredicate fp = new StandardFilePredicate () ;
        
        Set requiredDirNames = new TreeSet () ;
        requiredDirNames.add ( "am" ) ;
        requiredDirNames.add ( "an" ) ;
        
        fp.setIncludeDirectoryNames ( requiredDirNames ) ;
        
        Set disallowedDirNames = new TreeSet () ;
        disallowedDirNames.add ( "xm" ) ;
        disallowedDirNames.add ( "xn" ) ;
        
        fp.setExcludeDirectoryNames ( disallowedDirNames ) ;
        
        Set allowedFileSuffixes = new TreeSet () ;
        allowedFileSuffixes.add ( ".aa" ) ;
        allowedFileSuffixes.add ( ".ab" ) ;
        
        fp.setIncludeFileSuffixes ( allowedFileSuffixes ) ;
        
        Set disallowedFileSuffixes = new TreeSet () ;
        disallowedFileSuffixes.add ( ".xa" ) ;
        disallowedFileSuffixes.add ( ".xb" ) ;
        
        fp.setExcludeFileSuffixes ( disallowedFileSuffixes ) ;
        
        /*
             org.ntropa.utility.StandardFilePredicate@1977bd[
              _wantDirectories=false
              _wantFiles=false
              _includeDirNames=[am, an]
              _excludeDirNames=[xm, xn]
              _includeFileSuffixes=<null>
              _excludeFileSuffixes=[.xa, .xb]
              _root=<null>
             ]
         */
        
        String s = fp.toString () ;
    }
}
