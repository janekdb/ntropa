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
 * StandardInputFilter.java
 *
 * Created on 25 October 2001, 12:35
 */

package org.ntropa.build.channel;

import java.io.File;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.ntropa.build.Constants;
import org.ntropa.utility.FilePredicate;


/**
 * Provide file and directory filtering for a channel.
 *
 * Ignore:
 *
 *    1. files/directories using characters outside of a-zA-Z0-9, hyphen, underscore, dot.
 *    2. files/directories starting or ending with a dot.
 *    3. files with no extension.
 *    4. directories starting with an underscore except _include, _application, _data, _presentation.
 *    5. files/directories inside an ignored directory (This is handled in the implementation of
 *       DirectoryMonitor. When an ignored directory is encountered it is not descended into.)
 *    6. files matching the pattern ".*\.html.+ (gets index.html-old, nt.html.021025 but not files
 *       ending in .link such as advice.html.link or general-info.link
 *
 * ( .DAV gets modified a lot during WebDAV use, even in directories not being
 *  directly accessed.)
 *
 * Example of files/directories we want to ignore.
 *
 * Directory: _notes, used by Dreamweaver.
 * .emacs
 * index.html~
 * #index.html#
 * about.html-old (*.html.+ (gets index.html-old))
 *
 *
 * @author  jdb
 * @version $Id: StandardInputFilter.java,v 1.5 2002/12/11 14:34:10 jdb Exp $
 */
public class StandardInputFilter implements FilePredicate {
    
    private static final PatternMatcher _matcher ;
    private static final Pattern _acceptableCharsPattern ;
    private static final Pattern _htmlBackupPattern ;
    
    private static final String ACCEPTABLE_CHARS_PATTERN = "[a-zA-Z0-9\\-\\._]+" ;
    private static final String HTML_BACKUP_PATTERN = ".+\\.html.+" ;
    
    /* Static initialiser */
    static {

        _matcher  = new Perl5Matcher () ;

        PatternCompiler compiler = new Perl5Compiler ();

        try {
            _acceptableCharsPattern = compiler.compile (
                    ACCEPTABLE_CHARS_PATTERN,
                    /* The READ_ONLY_MASK makes the compiled pattern thread safe */
                    Perl5Compiler.READ_ONLY_MASK );

            _htmlBackupPattern = compiler.compile (
                    HTML_BACKUP_PATTERN,
                    /* The READ_ONLY_MASK makes the compiled pattern thread safe */
                    Perl5Compiler.READ_ONLY_MASK );
        }
        catch(MalformedPatternException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    /** Creates new StandardInputFilter */
    public StandardInputFilter () {
    }
    
    public boolean accept ( String file ) {
        return accept ( new File ( file ) ) ;
    }
    
    public boolean accept ( File file ) {
        
        if ( file.isDirectory () )
            return acceptDirectory ( file.getName () ) ;
        
        if ( file.isFile () )
            return acceptFile ( file.getName () ) ;
        
        return false ;
    }
    
    protected boolean acceptDirectory ( String name ) {
        
        if ( ! charactersAcceptable ( name ) )
            return false ;
        
        if ( startsWithPeriod ( name ) )
            return false ;
        
        if ( name.startsWith ( "_" ) )
            if ( ! Constants.getSystemDirectoryNames ().contains ( name ) )
                return false ;
        
        return true ;
        
    }
    
    protected boolean acceptFile ( String name ) {
        
        if ( ! charactersAcceptable ( name ) )
            return false ;
        
        if ( startsWithPeriod ( name ) )
            return false ;
        
        if ( endsWithPeriod ( name ) )
            return false ;
        
        if ( name.indexOf ( "." ) == -1 )
            return false ;
        
        if ( _matcher.matches ( name, _htmlBackupPattern ) && ( ! name.endsWith ( Constants.getLinkFileSuffix () ) ) )
            return false ;
        
        return true ;
        
    }
    
    public boolean charactersAcceptable ( String name ) {
        
        /*
         From org.apache.oro.text.regex
         Interface PatternMatcher
         
         public boolean matches(java.lang.String input,
                       Pattern pattern)
         
           Determines if a string exactly matches a given pattern. If there is an
           exact match, a MatchResult instance representing the
           match is made accesible via getMatch().
         
           Parameters:
              input - The String to test for an exact match.
              pattern - The Pattern to be matched.
           Returns:
              True if input matches pattern, false otherwise.
         */
        
        return _matcher.matches ( name, _acceptableCharsPattern ) ;
    }
    
    protected boolean startsWithPeriod ( String name ) {
        return name.startsWith ( "." ) ;
    }
    
    protected boolean endsWithPeriod ( String name ) {
        return name.endsWith ( "." ) ;
    }
    
}
