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
 * StandardFragmentTokenizerTest.java
 *
 * Created on 07 November 2001, 22:54
 */

package tests.org.ntropa.build.html;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.html.Fragment;
import org.ntropa.build.html.FragmentTokenizer;
import org.ntropa.build.html.MarkUpAttributes;
import org.ntropa.build.html.StandardFragmentTokenizer;



/**
 *
 * @author  jdb
 * @version $Id: StandardFragmentTokenizerTest.java,v 1.2 2001/11/09 17:56:09 jdb Exp $
 */
public class StandardFragmentTokenizerTest extends TestCase {
    
    /** Creates new StandardFragmentTokenizerTest */
    public StandardFragmentTokenizerTest( String testName ) {
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
        
        TestSuite suite = new TestSuite( StandardFragmentTokenizerTest.class );
        return suite;
    }
    
    /*
    protected void setUp () throws Exception {}
     */
    
    /*
    protected void tearDown () throws Exception {}
     */
    
    /**
     * Test minimal scenarios.
     */
    public void testMinimals() {
        
        Fragment frag = new Fragment( "" ) ;
        
        /* Zero length fragment */
        FragmentTokenizer ft = new StandardFragmentTokenizer( frag ) ;
        
        if ( ft.hasNext() )
            fail( "Zero length fragment parse fail" ) ;
        
        /* HTML */
        frag = new Fragment( "<html></html>" ) ;
        
        boolean gotIt = false ;
        ft = new StandardFragmentTokenizer( frag ) ;
        if ( ft.hasNext() ) {
            
            Object o = ft.next() ;
            if ( o instanceof Fragment ) {
                Fragment f = (Fragment) o ;
                gotIt = f.equals( frag ) ;
            }
            
            if ( ft.hasNext() )
                gotIt = false ;
        }
        
        if ( ! gotIt )
            fail( "HTML only parse fail" ) ;
        
        /* One web publishing system markup comment */
        frag = new Fragment( "<!-- name=\"result-list\" -->" ) ;
        MarkUpAttributes markup = new MarkUpAttributes() ;
        markup.setAttribute( "name", "result-list" );
        
        gotIt = false ;
        ft = new StandardFragmentTokenizer( frag ) ;
        if ( ft.hasNext() ) {
            
            Object o = ft.next() ;
            if ( o instanceof MarkUpAttributes ) {
                MarkUpAttributes m = (MarkUpAttributes) o ;
                gotIt = m.equals( markup ) ;
            }
            
            if ( ft.hasNext() ) {
                gotIt = false ;
            }
        }
        
        if ( ! gotIt )
            fail( "Markup only parse fail" ) ;
        
    }
    
    /**
     * Test sequences of automatically generated <code>Fragment</code>s
     */
    /**
     * Test many sequences
     */
    public void testBinarySequences() {
        
       /*
        * We include abutting Hs.
        * (HH = H)
        */
        
        List comboList = Arrays.asList( new String [] {
            /* 1 */
            "H", "A",
            /* 2 */
            "AH", "AA", "HA", "HH",
            /* 3 */
            "AAH", "AAA", "AHA", "AHH",
            "HAH", "HAA", "HHA", "HHH",
            /* 4 */
            "AAAH", "AAAA", "AAHA", "AAHH", "AHAH", "AHAA", "AHHA", "AHHH",
            "HAAH", "HAAA", "HAHA", "HAHH", "HHAH", "HHAA", "HHHA", "HHHH",
            /* 5 */
            "AAAAH", "AAAAA", "AAAHA", "AAAHH", "AAHAH", "AAHAA", "AAHHA", "AAHHH",
            "AHAAH", "AHAAA", "AHAHA", "AHAHH", "AHHAH", "AHHAA", "AHHHA", "AHHHH",
            "HAAAH", "HAAAA", "HAAHA", "HAAHH", "HAHAH", "HAHAA", "HAHHA", "HAHHH",
            "HHAAH", "HHAAA", "HHAHA", "HHAHH", "HHHAH", "HHHAA", "HHHHA", "HHHHH"
            
        } ) ;
        
        
        int namePostfix = 0 ;
        
        AttributesFactory attributes = new AttributesFactory() ;
        
        for ( int runCnt = 0 ; runCnt < 3 ; runCnt++ ) {
            
            HtmlFactory html = new HtmlFactory() ;
            
            /* Go through the tests with the Html is a different sequence each time */
            for( int skip = runCnt ; skip-- >= 0 ; )
                html.next() ;
            
            
            Iterator it = comboList.iterator() ;
            while ( it.hasNext() ) {
                
                String seq = (String) it.next() ;
                
                StringBuffer sb = new StringBuffer() ;
                
                List expectedObjects = new LinkedList() ;
                
                /*
                 * Because we have sequences like HH we accumulate the HTML and
                 * save it at the last moment
                 */
                StringBuffer htmlBuf = new StringBuffer() ;
                for ( int i = 0; i < seq.length() ; i++, namePostfix++ ) {
                    
                    if ( seq.charAt( i ) == 'A' ) {
                        if ( htmlBuf.length() > 0 ) {
                            expectedObjects.add( new Fragment( htmlBuf.toString() ) ) ;
                            htmlBuf.setLength( 0 ) ;
                        }
                        
                        MarkUpAttributes m = attributes.next() ;
                        sb.append( m.toHtml() ) ;
                        expectedObjects.add( m ) ;
                    }
                    else {
                        String h = html.next() ;
                        sb.append( h ) ;
                        htmlBuf.append( h ) ;
                        // expectedObjects.add ( new Fragment ( h ) ) ;
                    }
                }
                
                if ( htmlBuf.length() > 0 )
                    expectedObjects.add( new Fragment( htmlBuf.toString() ) ) ;
                
                //System.out.println("testBinarySequences: " + seq + " (" + runCnt + ")" );
                //long t = System.currentTimeMillis () ;
                StandardFragmentTokenizer sft = new StandardFragmentTokenizer( sb.toString() ) ;
                checkObjectSequence( "testBinarySequences: " + seq, sft, expectedObjects ) ;
                //System.out.println ("Seconds: " + ( ( System.currentTimeMillis () - t ) / 1000 ) +"\n" );
                //System.out.println ("\n\n");
                
            }
        }
    }
    
    /**
     * Check the sequence of objects acquired from tokenizing the HTML
     * matches the sequence of objects used to construct the HTML
     */
    protected void checkObjectSequence(
    String seqNote,
    StandardFragmentTokenizer sft,
    List expectedObjects ) {
        
        Iterator expectedIt = expectedObjects.iterator() ;
        
        while ( expectedIt.hasNext() ) {
            Object expected = expectedIt.next() ;
            //System.out.println ("[checkObjectSequence] checking for " + expected.toString () );
            if ( ! sft.hasNext() )
                fail( "Too few tokens" ) ;
            Object actual = sft.next() ;
            assertEquals( "Object mismatch", expected, actual ) ;
        }
    }
    
    private class HtmlFactory {
        
        int _next = -1 ;
        
        String [] html = new String [] {
            "<b><i>Hi</i></b>\n" ,
            "<!-- the news section goes in here -->",
            "<script language=\"javascript\" type=\"text/html\"><!--\nif ( true )" +
            "\r\ndocument.writeln ( \"<b>It's all true</b>\" ) ; \n//-->\r\n",
            "<!-- --><font color=\"red\">Warning!</font>"
        } ;
        
        public String next() {
            
            _next = ( _next + 1 ) % html.length ;
            
            return html [ _next ] ;
            
        }
        
    }
    
    private class AttributesFactory {
        
        /* Reproducible sequence */
        Random _r = new Random( 33 ) ;
        
        String _chars =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
        "abcdefghijklmnopqrstuvwxyz" +
        "0123456789----" ;
        
        public MarkUpAttributes next() {
            
         /*
          * Make a map of name and value pairs
          */
            
            int attrCnt = _r.nextInt( 5 ) + 1;
            MarkUpAttributes m = new MarkUpAttributes() ;
            for ( int attrIdx = 1 ; attrIdx <= attrCnt ; attrIdx++ ) {
                
                m.setAttribute( nextName(), nextValue() ) ;
                
            }
            
            return m ;
        }
        
        public String nextName() {
            
            int length = 1 + _r.nextInt( 20 ) ;
            
            StringBuffer sb = new StringBuffer( length ) ;
            /* start with a letter */
            sb.append( _chars.charAt( _r.nextInt( 26 + 26 ) ) );
            while ( sb.length() < length )
                sb.append( _chars.charAt( _r.nextInt( _chars.length() ) ) ) ;
            return sb.toString() ;
        }
        
        public String nextValue() {
            
            int length = 1 + _r.nextInt( 20 ) ;
            
            StringBuffer sb = new StringBuffer( length ) ;
            
            while ( sb.length() < length ) {
                /* Random range is 32 -> 126 */
                char c = (char) ( _r.nextInt( 127 - 32 ) + 32 ) ;
                if ( c == '"' )
                    c = 'A' ;
                
                sb.append( c ) ;
            }
            
            return sb.toString() ;
        }
        
    }
}
