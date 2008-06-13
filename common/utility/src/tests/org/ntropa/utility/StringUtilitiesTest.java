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
 * StringUtilitiesTest.java
 *
 * Created on 19 November 2001, 11:40
 */

package tests.org.ntropa.utility;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.utility.StringUtilities;


/**
 *
 * @author  jdb
 * @version $Id: StringUtilitiesTest.java,v 1.9 2002/11/02 00:54:43 jdb Exp $
 */
public class StringUtilitiesTest extends TestCase {
    
    /** Creates new StringUtilitiesTest */
    public StringUtilitiesTest ( String testName ) {
        super(testName);
    }
    
    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests.
     * It runs a collection of test cases.
     */
    public static Test suite () {
        
        TestSuite suite = new TestSuite ( StringUtilitiesTest.class );
        return suite;
    }
    
    public void testEscapeString () {
        
        assertEquals ( "null not handled correctly", null, StringUtilities.escapeString ( null ) ) ;
        assertEquals ( "zero-length not handled correctly", "", StringUtilities.escapeString ( "" ) ) ;
        
        String noEscapes =
        "abcdefghijklmnopqrstuvwxyz"  +
        "ABCDEFGHIJKLMNOPQRSTUVWZYZ" +
        "0123456789" +
        "!£$%^&*()_+{}[]#~@;:|,.<> ";
        
        assertEquals ( "String with no escaping required not handled correctly",
        noEscapes, StringUtilities.escapeString ( noEscapes ) ) ;
        
        String needsEscaping = "\n" + "\t" + "\b" + "\r" + "\f" ;
        //System.out.println( "needsEscaping:" + needsEscaping );
        String escaped = "\\n" + "\\t" + "\\b" + "\\r" + "\\f" ;
        //System.out.println( "escaped:" + escaped );
        
        assertEquals ( "String requiring escapes not handled correctly",
        escaped, StringUtilities.escapeString ( needsEscaping ) ) ;
        
        needsEscaping = "\\" + "\'" + "\"" ;
        //System.out.println( "needsEscaping:" + needsEscaping );
        escaped = "\\\\" + "\\\'" + "\\\"" ;
        //System.out.println( "escaped:" + escaped );
        
        assertEquals ( "String requiring escapes not handled correctly",
        escaped, StringUtilities.escapeString ( needsEscaping ) ) ;
        
    }
    
    /**
     * abhishek@studylink.com.au reported a bug in the creation of JSPs by the
     * web publishing system. This test is a simple check that the fault is not
     * in escapeString.
     * <p>
     *
     */
    public void testPercentAngle () {
        assertEquals ( "100%> escaped correctly", "100%>", "100%>") ;
    }
    
    
    public void testReplace () {
        
        assertEquals ( "replace failed", "", StringUtilities.replace ( "", "K", "" ) ) ;
        assertEquals ( "replace failed", "a", StringUtilities.replace ( "a", "f", "g" ) ) ;
        assertEquals ( "replace failed", "b", StringUtilities.replace ( "a", "a", "b" ) ) ;
        assertEquals ( "replace failed", "abdc", StringUtilities.replace ( "abc", "b", "bd" ) ) ;
        assertEquals ( "replace failed", "A<\\%B", StringUtilities.replace ( "A<%B", "<%", "<\\%" ) ) ;
        assertEquals ( "replace failed", "A%\\>B", StringUtilities.replace ( "A%>B", "%>", "%\\>" ) ) ;
        
    }
    
    public void testSplit () {
        
        String r [] = null ;
        
        try {
            r = StringUtilities.split ( null ) ;
            fail ( "Null falsely accepted" ) ;
        }
        catch ( IllegalArgumentException e ) {
        }
        
        try {
            r = StringUtilities.split ( "string" , null ) ;
            fail ( "Null falsely accepted" ) ;
        }
        catch ( IllegalArgumentException e ) {
        }
        
        try {
            r = StringUtilities.split ( null , ",") ;
            fail ( "Null falsely accepted" ) ;
        }
        catch ( IllegalArgumentException e ) {
        }
        
        try {
            r = StringUtilities.split ( null , null) ;
            fail ( "Null falsely accepted" ) ;
        }
        catch ( IllegalArgumentException e ) {
        }
        
        r = StringUtilities.split ( "" ) ;
        assertEquals ( "Wrong number of elements for empty string", 0, r.length ) ;
        
        r = StringUtilities.split ( "a" ) ;
        assertEquals (
        "The split array was wrong: a",
        Arrays.asList ( new String [] { "a" } ),
        Arrays.asList ( r )
        );
        
        /* what should this be ? */
        r = StringUtilities.split ( " a" ) ;
        assertEquals (
        "The split array was wrong: <space>a",
        Arrays.asList ( new String [] { "a" } ),
        Arrays.asList ( r )
        );
        r = StringUtilities.split ( "a " ) ;
        assertEquals (
        "The split array was wrong: a<space>",
        Arrays.asList ( new String [] { "a" } ),
        Arrays.asList ( r )
        );
        
        r = StringUtilities.split ( "a b" ) ;
        assertEquals (
        "The split array was wrong: a<space>b",
        Arrays.asList ( new String [] { "a", "b" } ),
        Arrays.asList ( r )
        );
        
        /* what should this be ? */
        r = StringUtilities.split ( "a b " ) ;
        assertEquals (
        "The split array was wrong: a<space>b<space>",
        Arrays.asList ( new String [] { "a", "b" } ),
        Arrays.asList ( r )
        );
        r = StringUtilities.split ( " a b" ) ;
        assertEquals (
        "The split array was wrong: <space>a<space>b",
        Arrays.asList ( new String [] { "a", "b" } ),
        Arrays.asList ( r )
        );
        
        /* test alternative separator */
        r = StringUtilities.split ( "1.aus.nt\n1.aus.nsw", "\n" ) ;
        assertEquals (
        "The split array was wrong for: 1.aus.nt\\n1.aus.nsw, \\n",
        Arrays.asList ( new String [] { "1.aus.nt", "1.aus.nsw" } ),
        Arrays.asList ( r )
        );
        
    }
    
    public void testRemovePrefix () {
        
        try {
            StringUtilities.removePrefix ( null, "" ) ;
            fail ( "Null arg not detected (1)" ) ;
        }
        catch ( IllegalArgumentException e ) {}
        try {
            StringUtilities.removePrefix ( "", null ) ;
            fail ( "Null arg not detected (2)" ) ;
        }
        catch ( IllegalArgumentException e ) {}
        
        assertEquals ( "Failed to remove prefix (1)", "", StringUtilities.removePrefix ( "", "" ) ) ;
        assertEquals ( "Failed to remove prefix (2)", "", StringUtilities.removePrefix ( "a", "a" ) ) ;
        assertEquals ( "Failed to ignore prefix (3)", "b", StringUtilities.removePrefix ( "b", "a" ) ) ;
        assertEquals ( "Failed to remove prefix (4)", "suffix", StringUtilities.removePrefix ( "prefix-suffix", "prefix-" ) ) ;
        assertEquals ( "Failed to ignore prefix (5)", "12", StringUtilities.removePrefix ( "12", "123" ) ) ;
    }
    
    public void testCapitaliseFirstLetter () {
        
        try {
            StringUtilities.capitaliseFirstLetter ( null ) ;
            fail ( "Null arg not detected (1)" ) ;
        }
        catch ( IllegalArgumentException e ) {}
        try {
            StringUtilities.capitaliseFirstLetter ( "" ) ;
            fail ( "Zero-length arg not detected (1)" ) ;
        }
        catch ( IllegalArgumentException e ) {}
        
        assertEquals ( "Failed to capitalise", "A", StringUtilities.capitaliseFirstLetter ( "A" ) ) ;
        assertEquals ( "Failed to capitalise", "A", StringUtilities.capitaliseFirstLetter ( "a" ) ) ;
        assertEquals ( "Failed to capitalise", "Zack", StringUtilities.capitaliseFirstLetter ( "zack" ) ) ;
        
    }
    
    public void testvalidateNonZeroLength () {
        
        try {
            StringUtilities.validateNonZeroLength ( null, "identifier" ) ;
            fail ( "null thrown exception" ) ;
        }
        catch ( IllegalArgumentException e ) {}
        
        try {
            StringUtilities.validateNonZeroLength ( "", "identifier" ) ;
            fail ( "zero length thrown exception" ) ;
        }
        catch ( IllegalArgumentException e ) {}
        
        StringUtilities.validateNonZeroLength ( "non zero length", "identifier" ) ;
        
    }
    
    public void testIsSubset () {
        
        if ( ! StringUtilities.isSubset ( "", "" ) )
            fail ( "Empty set was subset of empty set" ) ;
        
        if ( StringUtilities.isSubset ( "k", "" ) )
            fail ( "Non-empty set was not subset of empty set" ) ;
        
        if ( StringUtilities.isSubset ( "a", "b" ) )
            fail ( "Disjoint set was not subset" ) ;
        
        if ( ! StringUtilities.isSubset ( "a", "a" ) )
            fail ( "Set was subset of self" ) ;
        
        if ( ! StringUtilities.isSubset ( "a", "abc" ) )
            fail ( "Set was subset of superset" ) ;
        
        if ( StringUtilities.isSubset ( "A", "abc" ) )
            fail ( "Test was case sensitive" ) ;
        
        if ( ! StringUtilities.isSubset ( "DEF9", "ABCDEFG0123456789" ) )
            fail ( "Set was subset of superset (2)" ) ;
        
        if ( ! StringUtilities.isSubset ( "KKKKKKK77777", "7K" ) )
            fail ( "Repetions were handled" ) ;
        
    }
    
}
