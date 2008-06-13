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
 * LoadUtilities.java
 *
 * Created on 09 January 2002, 16:38
 */

package com.studylink.utility ;

import com.studylink.utility.StringUtilities ;

import java.util.Arrays ;
import java.util.Date ;
import java.util.SortedSet ;
import java.util.TreeSet ;

/**
 *
 * @author  jdb
 * @version $Id: LoadUtilities.java,v 1.2 2003/04/14 15:24:21 jdb Exp $
 */
public class LoadUtilities {
    
    /**
     * Parse some text into an array with these properties:
     * <ul>
     * <li>Each element is unique
     * <li>No element is null or the zero length String
     * </ul>
     *
     * @text The test to parse, separated by ASCII 10
     */
    static public String [] getArraySet ( String text ) {
        return getArraySet ( text, "\n" ) ;
    }
    /**
     * <p>Parse some text into an array with these properties</p>
     * <ul>
     * <li>Each element is unique
     * <li>No element is null or zero length
     * <li>The array is sorted
     * </ul>
     *
     * @text The test to parse, separated by ASCII 10
     * @return A normalized <code>Array</code> of <code>String</code>
     */
    static public String [] getArraySet ( String text, String separator ) {
        
        String[] a = StringUtilities.split ( text , separator) ;
        //Set s = new TreeSet ( Arrays.asList ( a ) ) ;
        
        return normalize ( a ) ; //( String [] ) s.toArray ( new String [ 0 ] ) ;
        
    }
    
    /**
     * <p>Normalise an array in a way useful to option lists</p>
     * <ul>
     * <li>Each element is unique
     * <li>No element is null or zero length
     * <li>The array is sorted
     * </ul>
     * @param input An <code>Array</code> of <code>String</code>
     * @return A normalized <code>Array</code> of <code>String</code>
     */
    static public String [] normalize ( String [] input ) {
        
        if ( input == null )
            return null ;
        
        SortedSet s = new TreeSet ( ) ;
        for ( int i = 0 ; i < input.length ; i++ ) {
            if ( input [ i ] == null )
                continue ;
            if ( input [ i ] .length () == 0 )
                continue ;
            s.add ( input [ i ] ) ;
        }
        return ( String [] ) s.toArray ( new String [ 0 ] ) ;
        
    }
    
    /**
     * <p>Normalise an array in a way useful to option lists</p>
     * <ul>
     * <li>Each element is unique
     * <li>No element is null or zero length
     * <li>The array is sorted
     * </ul>
     * @param inputArray An <code>Array</code> of <code>String</code>
     * @param inputElement A <code>String</code> to add the the returned array
     * @return A normalized <code>Array</code> of <code>String</code> which includes inputElement
     */
    static public String [] normalize ( String [] inputArray, String inputElement ) {
        
        if ( inputArray == null )
            return null ;

        String [] newArray = new String [ inputArray.length + 1 ] ;
        System.arraycopy ( inputArray, 0, newArray, 0, inputArray.length ) ;
        newArray [ newArray.length - 1 ] = inputElement ;
        
        return normalize ( newArray ) ;
    }

    
    
    /**
     * <p>Normalise an array in a way useful to option lists</p>
     * <ul>
     * <li>Each element is unique
     * <li>No element is null
     * <li>The array is sorted
     * </ul>
     * @param input An <code>Array</code> of <code>Date</code>
     * @return A normalized <code>Array</code> of <code>Date</code>
     */
    static public Date [] normalize ( Date [] input ) {
        
        if ( input == null )
            return null ;
        
        SortedSet s = new TreeSet ( ) ;
        for ( int i = 0 ; i < input.length ; i++ ) {
            if ( input [ i ] == null )
                continue ;
            s.add ( input [ i ] ) ;
        }
        return ( Date [] ) s.toArray ( new Date [ 0 ] ) ;
        
    }
    
    
    /**
     * <p>Normalise an array of <code>Dates</code>s in a way useful to option lists</p>
     * <ul>
     * <li>Each element is unique
     * <li>No element is null
     * <li>The array is sorted
     * </ul>
     * @param inputArray An <code>Array</code> of <code>Date</code>
     * @param inputElement A <code>Date</code> to add the the returned array
     * @return A normalized <code>Array</code> of <code>Date</code> which includes inputElement
     */
    static public Date [] normalize ( Date [] inputArray, Date inputElement ) {
        
        if ( inputArray == null )
            return null ;

        Date [] newArray = new Date [ inputArray.length + 1 ] ;
        System.arraycopy ( inputArray, 0, newArray, 0, inputArray.length ) ;
        newArray [ newArray.length - 1 ] = inputElement ;
        
        return normalize ( newArray ) ;
    }


}
