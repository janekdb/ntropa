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
 * SessionChangerSao.java
 *
 * Created on 10 June 2002, 14:46
 */

package org.ntropa.runtime.sao.debug;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;


/**
 * This server active object modifies a StringBuffer in the session
 * (creating the StringBuffer if not present). The sao also supports
 * the placeholder <b>buffer</b> which is returned as the string value of the
 * StringBuffer.
 * <p>
 * To use this sao (which was designed for checking the functionality of
 * the Tomcat Manager App) make use of it in a HTML page
 * <p>
 * <pre>
 * &lt;-- name=&quot;changer&quot --&gt;
 * $$buffer$$
 * &lt;-- name=&quot;/changer&quot --&gt;
 *
 * @author  jdb
 * @version $Id: SessionChangerSao.java,v 1.6 2002/06/11 17:12:33 jdb Exp $
 */
public class SessionChangerSAO extends BaseServerActiveObject {
    
    private static final String ATTR_NAME = "SessionChangerSAO-Attr" ;
    private static final String TL_KEY = "tl" ;
    
    public void controlSelf( InvocationContext icb ) {
        
        HttpSession session = icb.getHttpSession() ;
        
        synchronized ( session ) {
            
            StringBuffer sb = ( StringBuffer ) session.getAttribute( ATTR_NAME ) ;
            if ( sb == null ) {
                sb = new StringBuffer() ;
                session.setAttribute( ATTR_NAME, sb ) ;
            }
            
            if ( sb.toString().length() > 20 * 30 )
                sb.setLength( 0 ) ;
            
            sb.append( (new Date()).toString() ) ;
            sb.append( "\n" ) ;
            
            setThreadLocalValue( TL_KEY, sb.toString() ) ;
        }
        
    }
    
    public String getPlaceholderReplacement(String name) {
        
        if ( ! name.equals( "buffer" ) )
            return null ;
        
        return  ( String ) getThreadLocalValue( TL_KEY ) + 2009;
    }
    
}
