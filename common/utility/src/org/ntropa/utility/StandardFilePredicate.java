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
 * StandardFilePredicate.java
 *
 * Created on 17 October 2001, 15:51
 */

package org.ntropa.utility;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Class which allows a file path to be tested against some criteria.
 *
 * Subclasses are free to use the set of
 * directory names and the set of file extensions as
 * they like.
 *
 * The default is to not test the type of the file. This allows file paths
 * to be tested without a corresponding file system.
 *
 * If either type is specified then the type of the actual
 * file is obtained from the operating system. Asked for both files and
 * directories is the same as asking for existence.
 *
 * @author  jdb
 * @version $Id: StandardFilePredicate.java,v 1.5 2002/11/30 23:05:26 jdb Exp $
 */
public class StandardFilePredicate implements FilePredicate {
    
    
    boolean _wantDirectories = false ;
    boolean _wantFiles = false ;
    
    Set _includeDirNames ;
    Set _excludeDirNames ;
    
    Set _includeFileSuffixes ;
    Set _excludeFileSuffixes ;
    
    File _root ;
    
    public StandardFilePredicate () {}
    
    /**
     * Test a file path to see if it matches our criteria
     *
     * @param file A <File> object representing the file path
     * we want to test. The file does not need to exist.
     */
    public boolean accept (File f) {
        
        /*
         * When the type of the file is unknown consider this path
         *
         *    /tmp/my-directory-or-file
         *
         * if we're not testing the existance of files then 'my-directory-or-file' will
         * be treated as a either a file name or a directory name.
         */
        boolean exists = false ;
        
        File absoluteFile = f ;
        if ( _root != null )
            absoluteFile = new File ( _root, f.getPath () ) ;
        
        /* if the type of the file is important then the file needs to exist */
        if ( _wantFiles || _wantDirectories ) {
            exists = absoluteFile.exists () ;
            if ( ! exists )
                return false ;
        }
        
        /* if only interested in files then it needs to be a file */
        if ( _wantFiles && ( ! _wantDirectories ) ) {
            if ( ! absoluteFile.isFile () )
                return false ;
        }
        
       /* if only interested in directories then it needs to be a directory */
        if ( ( ! _wantFiles ) && _wantDirectories ) {
            if ( ! absoluteFile.isDirectory () )
                return false ;
        }
        
        /* Now we need to apply the name rules */
        
        /*
         * Say the file path is a/b/c/d. Then
         *
         * if existance is known
         *
         *    if type is file
         *        file = d
         *        dirs = a, b, c
         *
         *    if type is directory
         *        file = <nothing>
         *        dirs = a, b, c, d
         *
         * if type is unknown
         *
         *    file = d
         *    dirs = a, b, c, d
         *
         * So in the case where the existance of the file/directory is unknown
         * we assume it could be either a file or a directory.
         */
        
        boolean lastPartIsFile ;
        boolean lastPartIsDirectory ;
        
        if ( exists ) {
            lastPartIsFile = f.isFile () ;
            lastPartIsDirectory = f.isDirectory () ;
        }
        else {
            lastPartIsFile = true ;
            lastPartIsDirectory = true ;
        }
        
        List pathElements = FileUtilities.pathElements ( f ) ;
        
        /* What's the alternative? */
        if ( pathElements.size () == 0 )
            return false ;
        
        String fileName = null ;
        if ( lastPartIsFile )
            fileName = (String) pathElements.get ( pathElements.size () - 1 ) ;
        
        
        /* we have a file name, test it */
        if ( fileName != null ) {
            if ( ! fileNameAcceptable ( fileName ) )
                return false ;
        }
        
        List dirs ;
        if ( lastPartIsDirectory )
            dirs = pathElements ;
        else
            dirs = pathElements.subList ( 0, pathElements.size () - 1 ) ;
        
        if ( ! directoriesAcceptable ( dirs ) )
            return false ;
        
        return true ;
    }
    
    /**
     * Test a file path to see if it matches our criteria
     *
     * @param file A <String> object representing the file path
     * we want to test. The file does not need to exist.
     */
    public boolean accept (String file) {
        return accept ( new File ( file ) ) ;
    }
    
    protected boolean fileNameAcceptable ( String fileName ) {
        
        /* if neither set of criteria exist then the file name is acceptable */
        if ( ( _excludeFileSuffixes == null ) && ( _includeFileSuffixes == null ) )
            return true ;
        
        Iterator it ;
        /* not allowed to have any of these suffixes */
        if ( _excludeFileSuffixes != null ) {
            it = _excludeFileSuffixes.iterator () ;
            while ( it.hasNext () ) {
                String suffix = (String) it.next () ;
                if ( fileName.endsWith ( suffix ) )
                    return false ;
            }
            
            return true ;
        }
        
        /* must have one of these suffixes */
        if ( _includeFileSuffixes != null ) {
            it = _includeFileSuffixes.iterator () ;
            while ( it.hasNext () ) {
                String suffix = (String) it.next () ;
                if ( fileName.endsWith ( suffix ) )
                    return true ;
            }
            return false ;
        }
        
        /* statement never reached but compiler doesn't know it */
        return false ;
    }
    
    protected boolean directoriesAcceptable ( List dirs ) {
        
        /* if neither set of criteria exist then the list of directories is acceptable */
        if ( ( _excludeDirNames == null ) && ( _includeDirNames == null ) )
            return true ;
        
        Iterator it ;
        /* no directory name allowed to be in the list of exclusions */
        if ( _excludeDirNames != null ) {
            it = _excludeDirNames.iterator () ;
            while ( it.hasNext () ) {
                String dir = (String) it.next () ;
                if ( dirs.contains ( dir ) )
                    return false ;
            }
        }
        /* if any directory name is in the list of inclusions it's accepted */
        if ( _includeDirNames != null ) {
            it = dirs.iterator () ;
            while ( it.hasNext () ) {
                String dir = (String) it.next () ;
                if ( _includeDirNames.contains ( dir ) )
                    return true ;
            }
            return false ;
        }
        
        /* statement reached when not excluded and no inclusion criteria */
        return true ;
    }
    
    public void setWantDirectories ( boolean b ) {
        _wantDirectories = b ;
    }
    
    public void setWantFiles ( boolean b ) {
        _wantFiles = b ;
    }
    
    public void setIncludeDirectoryNames ( Set s ) {
        
        _includeDirNames = new TreeSet ( s ) ;
    }
    
    public void setIncludeDirectoryNames ( String dirName ) {
        Set s = new TreeSet () ;
        s.add ( dirName ) ;
        setIncludeDirectoryNames ( s ) ;
    }
    
    public void setExcludeDirectoryNames ( Set s ) {
        
        _excludeDirNames = new TreeSet ( s ) ;
    }
    
    public void setExcludeDirectoryNames ( String dirName ) {
        Set s = new TreeSet () ;
        s.add ( dirName ) ;
        setExcludeDirectoryNames ( s ) ;
    }
    
    public void setIncludeFileSuffixes ( Set s ) {
        
        _includeFileSuffixes = new TreeSet ( s ) ;
        /* No point in having exclusions if inclusions are active */
        _excludeFileSuffixes = null ;
    }
    
    public void setExcludeFileSuffixes ( Set s ) {
        
        _excludeFileSuffixes = new TreeSet ( s ) ;
        /* No point in having inclusions if exclusions are active */
        _includeFileSuffixes = null ;
    }
    
    /**
     * Set an optional root to consider files and directories to be
     * relative to when testing for existence.
     *
     * @param rootFile Optional root location.
     */
    public void setRoot ( File rootFile ) {
        
        if ( rootFile == null ) {
            _root = null ;
            return ;
        }
        
        _root = rootFile.getAbsoluteFile () ;
        
    }
    
    public String toString () {
        
        return new ToStringBuilder (this, ToStringStyle.MULTI_LINE_STYLE ).
        append ( "_wantDirectories",     _wantDirectories).
        append ( "_wantFiles",           _wantFiles).
        append ( "_includeDirNames",     _includeDirNames).
        append ( "_excludeDirNames",     _excludeDirNames).
        append ( "_includeFileSuffixes", _includeFileSuffixes).
        append ( "_excludeFileSuffixes", _excludeFileSuffixes).
        append ( "_root", _root).
        toString ();
        
    }
    
}

