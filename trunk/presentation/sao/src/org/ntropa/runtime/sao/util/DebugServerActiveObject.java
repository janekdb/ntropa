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
 * DebugServerActiveObject.java
 *
 * Created on 27 November 2001, 16:32
 */

package org.ntropa.runtime.sao.util;

import java.util.Iterator;

import javax.servlet.jsp.JspWriter;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;


/**
 * Use this class (or other class) to debug WPS JSPs.
 *
 * @author  jdb
 * @version $Id: DebugServerActiveObject.java,v 1.2 2002/04/16 17:55:48 rj Exp $
 */
public class DebugServerActiveObject extends BaseServerActiveObject {
        
    /**
     * Called inside the JSP to either write out some HTML or perform a non-HTML
     * action such as invoking a RequestDispacther.
     *
     */
    public void render ( InvocationContext icb ) throws Exception {
        debug_1 ( icb ) ;
    }
    
    public void debug_2 ( InvocationContext icb ) throws Exception {
        Iterator it = getChildren ().iterator () ;
        while ( it.hasNext () ) {
            String childName = (String)it.next () ;
            getChild ( childName ).render ( icb ) ;
        }
    }
    
    public void debug_1 ( InvocationContext icb ) throws Exception {
        
        JspWriter out = icb.getJspWriter () ;
        
        out.write ( "<b>DebugServerActiveObject.render (...) invoked</b><br>" ) ;
        Iterator it = getChildren ().iterator () ;
        while ( it.hasNext () ) {
            String childName = (String)it.next () ;
            out.write ( "Child: " + childName + "<br>") ;
        }
    }
    
}
