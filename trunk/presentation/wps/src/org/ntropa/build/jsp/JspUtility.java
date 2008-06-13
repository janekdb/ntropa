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
 * JspUtility.java
 *
 * Created on 22 October 2001, 22:32
 */

package org.ntropa.build.jsp;

import org.ntropa.utility.StringUtilities;

/**
 * Utility methods related to JSP
 *
 * @author  jdb
 * @version $Id: JspUtility.java,v 1.7 2002/06/07 13:33:52 jdb Exp $
 */
public class JspUtility {
    
    private static final String START_TOKEN = "<%" ;
    private static final String END_TOKEN = "%>" ;
    
    /** prevent no-arg constructor */
    private JspUtility() {
    }
    
    /**
     * Perform subsitutions to escape JSP tags
     *
     * <%    becomes   <\%
     * %>    becomes   %\>
     *
     * @param jsp JSP to escape.
     */
    /*private static String _jspEscape( String jsp ) {
        if ( jsp == null )
            return null ;
        
        if ( jsp.indexOf( START_TOKEN ) == 0 && jsp.indexOf( END_TOKEN ) == 0 )
            return jsp ;
        
        return StringUtilities.replace(
        StringUtilities.replace(
        jsp, START_TOKEN, "<\\%" ),
        END_TOKEN, "%\\>" ) ;
        
    }*/
    
    /**
     * Perform subsitutions to escape a String to
     *
     * 1. Make it safe as a quoted Java string
     * 2. Make it JSP safe
     *
     *
     * @param string <code>String</code> to escape.
     */
    /*private static String _escape( String string ) {
        return _jspEscape( StringUtilities.escapeString( string ) ) ;
    }*/
    
    /**
     * Perform modifications to make a quoted String which is
     * safe to use in JSP declaration elements.
     * <p>
     * The String is first made safe for use in a Java source code
     * file by escaping quotes, new lines, etc.
     * <p>
     * Then the JSP opening and closing character sequences are made
     * safe by splitting each across two Strings. e.g.
     * <p>
     * &lt;td width=100%&gt; becomes &quot;&lt;td width=100%&quot; + &quot;&gt;&quot;
     *
     * @param string The <code>String</code> to quote and make safe
     * @return a <code>String</code> which can be used in a JSP declaration element.
     */
    public static String makeSafeQuoted( String string ) {
        StringBuffer sb = new StringBuffer( string.length() * 2 ) ;
        sb.append( "\"" ) ;
        splitSpecialSequences( StringUtilities.escapeString( string ), sb ) ;
        sb.append( "\"" ) ;
        return sb.toString() ;
    }
    
    private static void splitSpecialSequences( String stringWithSpecialSequences, StringBuffer sb ) {
        if ( stringWithSpecialSequences == null )
            throw new IllegalArgumentException( "stringWithSpecialSequences was null" ) ;
        
        if ( stringWithSpecialSequences.indexOf( START_TOKEN ) == 0 && stringWithSpecialSequences.indexOf( END_TOKEN ) == 0 ) {
            sb.append( stringWithSpecialSequences ) ;
            return ;
        }
        
        sb.append(
        StringUtilities.replace(
        StringUtilities.replace(
        stringWithSpecialSequences, START_TOKEN, "<\" + \"%" ),
        END_TOKEN, "%\" + \">" )
        ) ;
        
    }
    
    /**
     * Writes a JSP tag to include another JSP at runtime.
     *
     * @param includedJsp The context relative path to the JSP to include.
     * Normmally the file path will start with a /.
     */
    public static String includeJsp( String includedJsp ) {
        return "<jsp:include page=\"" + includedJsp + "\" flush=\"true\" />" ;
    }
    
    /**
     * <p>Helper method to create Java code to create this code</p>
     * <pre>
     *  <objName> = new <clazzName> () ; \n
     *
     * @param objName The Java identifier of the object to be assigned to
     * @param clazzName The class name of the new object
     * @param buffer The <code>StringBuilder</code> to append the code to
     */
    public static void getObjectCreationCode( String objName, String clazzName, StringBuilder buffer ) {
        
        if ( objName == null )
            throw new IllegalArgumentException("Null objName argument") ;
        if ( objName.length() == 0 )
            throw new IllegalArgumentException("Zero-length objName argument") ;
        
        if ( clazzName == null )
            throw new IllegalArgumentException("Null clazzName argument") ;
        if ( clazzName.length() == 0 )
            throw new IllegalArgumentException("Zero-length clazzName argument") ;
        
        if ( buffer == null )
            throw new IllegalArgumentException("Null buffer argument") ;
        
        buffer.append( objName + " = new " + clazzName + " () ;\n" ) ;
    }
}
