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
 * StringUtilities.java
 *
 * Created on 15 November 2001, 21:11
 */

package org.ntropa.utility;
import java.util.StringTokenizer;

/**
 * String utilities.
 *
 * @author  jdb
 * @version $Id: StringUtilities.java,v 1.13 2002/11/22 06:48:49 rj Exp $
 */
public class StringUtilities {
    
    static private char _escapeMapKey [] ;
    static private String _escapeMapVal [] ;
    
    static {
        
        /* Escapes taken from 'The Java Programming Language' 3/e p.142-143 */
        _escapeMapKey = new char [ 8 ] ;
        _escapeMapVal = new String [ 8 ] ;
        
        _escapeMapKey [ 0 ] = 0x0A ;
        _escapeMapVal [ 0 ] = "\\n" ;
        
        _escapeMapKey [ 1 ] = 0x09 ;
        _escapeMapVal [ 1 ] = "\\t" ;
        
        _escapeMapKey [ 2 ] = 0x08 ;
        _escapeMapVal [ 2 ] = "\\b" ;
        
        _escapeMapKey [ 3 ] = 0x0D ;
        _escapeMapVal [ 3 ] = "\\r" ;
        
        _escapeMapKey [ 4 ] = 0x0C ;
        _escapeMapVal [ 4 ] = "\\f" ;
        
        _escapeMapKey [ 5 ] = 0x5C ;
        _escapeMapVal [ 5 ] = "\\\\" ;
        
        _escapeMapKey [ 6 ] = 0x27 ;
        _escapeMapVal [ 6 ] = "\\\'" ;
        
        _escapeMapKey [ 7 ] = 0x22;
        _escapeMapVal [ 7 ] = "\\\"" ;
        
        
    }
    /** Creates new StringUtilities */
    private StringUtilities () {
    }
    
    /**
     * Escape a <code>String</code> so it can be used between quotes
     * as if it were typed in at the keyboard
     *
     * @param unescaped The unescaped <code>String</code>
     *
     * @return An escaped <code>String</code>
     */
    static public String escapeString ( String unescaped ) {
        
        if ( unescaped == null )
            return null ;
        
        /* Try to avoid reallocation of the buffer */
        StringBuffer sb = new StringBuffer ( Math.round ( unescaped.length () * 1.05f ) ) ;
        
        for ( int i = 0 ; i < unescaped.length () ; i++ ) {
            char c = unescaped.charAt ( i ) ;
            /*
             * This test is to prevent the creation of thousands of String objects
             * while converting a String as long as a typical HTML page.
             */
            int idx = getEscapeIndex ( c ) ;
            if ( idx != -1 )
                sb.append ( _escapeMapVal [ idx ] ) ;
            else
                sb.append ( c ) ;
        }
        
        return sb.toString () ;
    }
    
    static private int getEscapeIndex ( char c ) {
        
        for ( int i = 0 ; i < _escapeMapKey.length ; i++ )
            if ( _escapeMapKey [ i ] == c )
                return i ;
        
        return -1 ;
    }
    
    /*
    Code taken from usenet and modified:
    From: Steve Chapel (schapel@breakthr.com)
    Subject: Re: Easy way to replace substrings in a String?
    Newsgroups: comp.lang.java.programmer
    Date: 2000/04/10
     */
    
    /**
     * Replace all occurrences of o in str with n,
     * or only the first occurrence if all is false.
     * replace("aaaa", "aa", "bbb", false) returns "bbbaa"
     * replace("aaaa", "aa", "bbb", true)  returns "bbbbbb"
     *
     * @param str The <code>String</code> to do replacements on
     * @param o The original <code>String</code> to replace
     * @param n The <code>String</code> use as the replacement
     * @param all If true all occurences of o are replaced otherwise just the first
     */
    static public String replace (String str, String o, String n, boolean all) {
        if (str == null)
            throw new IllegalArgumentException ("null String (str)");
        if (o == null)
            throw new IllegalArgumentException ("null Original String (o)");
        if (o.length () == 0)
            throw new IllegalArgumentException ("empty Original String (o)");
        if (n == null)
            throw new IllegalArgumentException ("null replacement String (n)");
        
        if ( o.equals ( n ) )
            return str ;
        StringBuffer result = null;
        int oldpos = 0;
        int pos = 0;
        do {
            pos = str.indexOf (o, oldpos);
            if (pos < 0)
                break;
            if (result == null)
                result = new StringBuffer ();
            result.append (str.substring (oldpos, pos));
            result.append (n);
            pos += o.length ();
            oldpos = pos;
        } while (all);
        if (oldpos == 0) {
            return str;
        } else {
            result.append (str.substring (oldpos));
            return new String (result);
        }
    }
    
    /**
     * As replace above but all occurences always replaced
     *
     * @param str The <code>String</code> to do replacements on
     * @param o The original <code>String</code> to replace
     * @param n The <code>String</code> use as the replacement
     */
    static public String replace (String str, String o, String n) {
        return replace ( str, o, n, true ) ;
    }
    
    static public String[] split (String str) {
        return split (str, " ") ;
    }
    
    // Splits a string into an array, seperated by the deliminator...
    static public String[] split (String str, String delim) {
        
        if (str == null) {
            throw new IllegalArgumentException ("Null string argument") ;
        }
        if (delim == null) {
            throw new IllegalArgumentException ("Null delimiter argument") ;
        }
        StringTokenizer st = new StringTokenizer (str, delim) ;
        int ct = st.countTokens () ;
        String strArray[] = new String[ct] ;
        
        for (int i = 0; i < ct; i++) {
            strArray[i] = st.nextToken () ;
        }
        
        return strArray ;
    }
    
    /**
     * Removes a prefix from a <code>String</code>. If the prefix is absent returns
     * the original <code>String</code>
     *
     * @param prefixedString A <code>String</code> with a possible prefix
     * @param prefix The prefix to look for
     * @return A <code>String</code> with the prefix removed or the original value if
     * the prefix was absent
     */
    public static String removePrefix ( String prefixedString, String prefix ) {
        
        if ( prefixedString == null )
            throw new IllegalArgumentException ("Null string argument (prefixedString)") ;
        
        if ( prefix == null )
            throw new IllegalArgumentException ("Null string argument (prefix)") ;
        
        if ( ! prefixedString.startsWith ( prefix ) )
            return prefixedString ;
        
        return prefixedString.substring ( prefix.length () ) ;
    }
    
    
    /**
     * Capitalise the first letter
     *
     * @param text The text to capitalise
     * @return The capitalised text
     */
    public static String capitaliseFirstLetter ( String text ) {
        
        if ( text == null )
            throw new IllegalArgumentException ("Null string argument (text)") ;
        
        if ( text.length () == 0 )
            throw new IllegalArgumentException ("Zero-length string argument (text)") ;
        
        return text.substring ( 0, 1).toUpperCase () + text.substring ( 1 ) ;
    }
    
    /**
     * Validate the String as not null and not empty.
     *
     * @param s The <code>String</code> reference to test.
     * @param identifier A <code>String</code> to use in making the message form the Exception.
     * @throws IllegalArgumentException Thrown is the <code>String</code> is null or zero length.
     */
    public static void validateNonZeroLength ( String s, String identifier ) {
        
        if ( s == null )
            throw new IllegalArgumentException ( "null String '" + identifier + "'" ) ;
        
        if ( s.length () == 0 )
            throw new IllegalArgumentException ( "zero length String '" + identifier + "'" ) ;
    }
    
    /**
     * Validate the String as not null and not empty.
     *
     * @param s The <code>String</code> reference to test.
     * @throws IllegalArgumentException Thrown is the <code>String</code> is null or zero length.
     */
    public static void validateNonZeroLength ( String s ) {
        
        if ( s == null )
            throw new IllegalArgumentException ( "null String" ) ;
        
        if ( s.length () == 0 )
            throw new IllegalArgumentException ( "zero length String" ) ;
    }
    
    /**
     * Treat a <code>String</code> as a set and test if it is a subset of another string.
     * @param subset A <code>String</code> treated as a set of characters
     * @param superset A <code>String</code> treated as a set of characters
     * @return true if all the characters in subset are in superset
     * @throws IllegalArgumentException if either input is null
     */
    public static boolean isSubset ( String subset, String superset ) {
        if ( subset == null )
            throw new IllegalArgumentException ( "subset was null" ) ;
        if ( superset == null )
            throw new IllegalArgumentException ( "superset was null" ) ;
        
        if ( subset.length () == 0 )
            return true ;
        if ( superset.length () == 0 )
            return false ;
        
        for ( int i = 0 ; i < subset.length () ; i++ )
            if ( superset.indexOf ( subset.charAt ( i ) ) == -1 )
                return false ;
        
        return true ;
    }
    
    /**
     * @return An empty <code>String</code> if maybeNull is null, otherwise maybeNull
     */
    public static String makeNullEmpty ( String maybeNull ) {
        
        if ( maybeNull == null )
            return "" ;
        
        return maybeNull ;
    }
}
