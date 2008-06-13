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
 * StandardFragmentTokenizer.java
 *
 * Created on 07 November 2001, 17:42
 */

package org.ntropa.build.html;

import java.util.NoSuchElementException;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.ntropa.utility.Queue;


/**
 * Objects of this class allow a HTML fragment to be iterated over in
 * a way useful to the parsing of server active html sections and
 * element sections.
 *
 * Example:
 *
 * This section of HTML
 *
 * <html>
 * <body>
 * <h1>main Section</h1>
 * <!-- name="my-server-active-html" placeholder-date="01-Nov-8" -->
 * <!-- element="news-link" placeholder-link = "index.html" -->
 * <a href="../news/index.html">The Latest News on 01-Nov-8</a>
 * <!-- element="/news-link" -->
 * <!-- name="/my-server-active-html" -->
 * </body>
 * </html>
 *
 * is tokenized as
 *
 * Fragment:
 * <html>
 * <body>
 * <h1>main Section</h1>
 *
 * MarkUpAttributes:
 * name:              "my-server-active-html"
 * placeholder-date:  "01-Nov-8"
 *
 * MarkUpAttributes:
 * element:           "news-link"
 * placeholder-link:  "index.html"
 *
 * Fragment:
 * <a href="../news/index.html">The Latest News on 01-Nov-8</a>
 *
 * MarkUpAttributes:
 * element:           "/news-link"
 *
 * MarkUpAttributes:
 * name:              "/my-server-active-html"
 *
 * Fragment:
 * </body>
 * </html>
 *
 * @author  jdb
 * @version $Id: StandardFragmentTokenizer.java,v 1.5 2002/03/03 22:35:11 jdb Exp $
 */
public class StandardFragmentTokenizer implements FragmentTokenizer {
    
    protected String _fragment ;
    
    /* The offset of the next possible HTML fragment */
    protected int _htmlFragmentCandidateStart = 0 ;
    
    protected Queue _matchQueue = new Queue () ;
    
    protected boolean _finishedMatching ;
    
    protected PatternMatcher _matcher ;
    
    protected PatternMatcherInput _input ;
    
    private static final String MARKUP_ATTRIBUTES_PATTERN =
     /*
      * Examples of string we want to match.
      * <!-- name="news" -->
      * <!-- name="/news" -->
      * <!-- name="news" template="news-tmpl" -->
      * <!-- element="item-1" placeholder-date="2001-November-8" -->
      * <!-- element="/item-1" -->
      * <!-- element="item-1" placeholder-date="2001-November-8"  placeholder-a = "b" -->
      * <!-- name="news" placeholder-date="2001-November-8" -->
      *
      *
      */
    "<!--\\s*(([\\w\\-]+)\\s*=\\s*\"([^\"]+)\"\\s*)+[^>]*-->" ;
    
    static protected Pattern _pattern ;
    static protected MalformedPatternException _patternException ;
    
    private static final String PARSE_ATTRIBUTES_PATTERN =
     /*
      * Examples of string we want to parse.
      * (See above)
      */
    "([\\w\\-]+)\\s*=\\s*\"([^\"]+)\"" ;
    
    static protected Pattern _parsePattern ;
    
    /* Static initialiser */
    static {
        PatternCompiler compiler = new Perl5Compiler();
        
        try {
            _pattern = compiler.compile(
            MARKUP_ATTRIBUTES_PATTERN,
            Perl5Compiler.CASE_INSENSITIVE_MASK |
            Perl5Compiler.READ_ONLY_MASK );
        }
        catch(MalformedPatternException e) {
            /* Exceptions can not be thrown from static initialisers */
            _pattern = null ;
            _patternException = e ;
        }
        
        try {
            _parsePattern = compiler.compile(
            PARSE_ATTRIBUTES_PATTERN,
            Perl5Compiler.CASE_INSENSITIVE_MASK |
            Perl5Compiler.READ_ONLY_MASK );
        }
        catch(MalformedPatternException e) {
            /* Exceptions can not be thrown from static initialisers */
            _parsePattern = null ;
            _patternException = e ;
        }
        
    }
    
    /**
     * Creates new StandardFragmentTokenizer
     */
    public StandardFragmentTokenizer( String fragment ) {
        
        if ( fragment == null )
            throw new IllegalArgumentException( "Attempt to construct StandardFragmentTokenizer from null String" ) ;
        
        _fragment = fragment ;
        
        init() ;
    }
    
    /**
     * Creates new StandardFragmentTokenizer
     */
    public StandardFragmentTokenizer( Fragment fragment ) {
        
        if ( fragment == null )
            throw new IllegalArgumentException( "Attempt to construct StandardFragmentTokenizer from null Fragment" ) ;
        
        _fragment = fragment.getValue () ;
        
        init() ;
    }
    
    /* Disallow no-arg constructor */
    private StandardFragmentTokenizer() {}
    
    public boolean hasNext() {
        
        /* Do some matching */
        match() ;
        
        /*
         * Check the result cache for an element
         * Return true if at least one element exists
         */
        return _matchQueue.size () > 0 ;
        
    }
    
    public Object next() {
        
        /* Do some matching */
        match() ;
        
        /*
         * Check the result cache for an element.
         * Return element if at least one element exists otherwise throw Exception
         */
        if ( _matchQueue.size() > 0 )
            return _matchQueue.remove() ;
        else
            throw new NoSuchElementException( "Attempt to get non-existent element" ) ;
        
    }
    
    /* Do we really need this? Using a new Parser would be simpler */
    //public void pushbackFragment(Fragment fragment) {
    //}
    
    /**
     * Match some more tokens and store any matches into the queue.
     */
    protected void match() {
        
        if ( _finishedMatching )
            return ;
        
        if ( _matcher.contains( _input, _pattern ) ) {
            
            MatchResult result = _matcher.getMatch();
            /*
            System.out.println ( "*** MATCH ***\n" + result.toString () );
            System.out.println ( "BEGIN OFFSET: " + result.beginOffset ( 0 ) );
            System.out.println ( "END OFFSET:   " + result.endOffset ( 0 ) );
             */
            
            /* The match jumped over some HTML to reach the current match */
            if ( _htmlFragmentCandidateStart < result.beginOffset( 0 ) ) {
                Fragment frag = new Fragment(
                _fragment.substring( _htmlFragmentCandidateStart, result.beginOffset( 0 ) )
                ) ;
                //System.out.println("[StandardFragmentTokenizer] Adding Fragment: " + frag );
                _matchQueue.add( frag ) ;
            }
            
            /* store the offset one beyond the end of the current match */
            _htmlFragmentCandidateStart = result.endOffset( 0 ) ;
            
            /* Convert to MarkUpAttributes */
            //System.out.println("[StandardFragmentTokenizer] parseAttributes: " + result.toString () );
            MarkUpAttributes m = parseAttributes( result.toString() ) ;
            //System.out.println("[StandardFragmentTokenizer] Adding MarkUpAttributes: " + m );
            _matchQueue.add( m ) ;
            
            /*
                if ( true ) {
                    int groupCnt = result.groups ();
                    System.out.println ("Number of Groups: " + groupCnt);
             
                    // group 0 is the entire matched string.
                    for ( int groupIdx = 1 ; groupIdx < groupCnt ; groupIdx++ ) {
                        String group = result.group ( groupIdx ) ;
             
                        System.out.println ("Group index " + groupIdx + ":" + group );
                        //System.out.println ("Begin: " + result.begin (group));
                        //System.out.println ("End: " + result.end (group));
                    }
                }
             */
            
        }
        /* No match and nothing left in the fragment to consider */
        else if ( _htmlFragmentCandidateStart >= _fragment.length() ) {
            _finishedMatching = true ;
        }
        /* No match, something left, therefore the rest of the input is HTML */
        else {
            
            Fragment frag = new Fragment(
            _fragment.substring( _htmlFragmentCandidateStart )
            ) ;
            _matchQueue.add( frag ) ;
            
            _finishedMatching = true ;
        }
        
    }
    
    /**
     * Create a <code>MarkUpAttributes</code> object from a <code>String</code>
     * like
     *
     * <! name= "power-talk" placeholder-date =   "01-Nov-8" -->
     *
     * @param commentForm The attributes in serialized form
     */
    protected MarkUpAttributes parseAttributes( String commentForm ) {
        
        PatternMatcher matcher  = new Perl5Matcher();
        
        PatternMatcherInput input = new PatternMatcherInput( commentForm );
        
        MarkUpAttributes m = new MarkUpAttributes() ;
        while ( matcher.contains( input, _parsePattern ) ) {
            
            MatchResult result = matcher.getMatch();
            
            /*
            System.out.println ( "[parseAttributes] *** MATCH ***\n" + result.toString () );
            System.out.println ( "[parseAttributes] BEGIN OFFSET: " + result.beginOffset ( 0 ) );
            System.out.println ( "[parseAttributes] END OFFSET:   " + result.endOffset ( 0 ) );
             */
            
            
            //actualMatchList += result.toString () + "/" + result.beginOffset (0) + "/" + result.endOffset (0) + "/" ;
            
            String name = result.group( 1 ) ;
            String value = result.group( 2 ) ;
            m.setAttribute( name, value ) ;
            
            //System.out.println ("Tag: " + tag );
            
            /*
            if ( true ) {
                int groupCnt = result.groups ();
                System.out.println ("[parseAttributes] Number of Groups: " + groupCnt);
             
                // group 0 is the entire matched string.
                for ( int groupIdx = 1 ; groupIdx < groupCnt ; groupIdx++ ) {
                    String group = result.group ( groupIdx ) ;
             
                    System.out.println ("[parseAttributes] Group index " + groupIdx + ":" + group );
                    //System.out.println ("Begin: " + result.begin (group));
                    //System.out.println ("End: " + result.end (group));
                }
            }
             */
            
            
        }
        
        return m ;
        
    }
    
    /**
     * Setup environment for matching.
     */
    protected void init() {
        
        /* delayed exception from static initialiser */
        if ( _pattern == null ) {
            throw new IllegalStateException( _patternException.toString() );
        }
        
        _matcher  = new Perl5Matcher();
        
        _input = new PatternMatcherInput( _fragment );
        
        _finishedMatching = false ;
    }
    
}
