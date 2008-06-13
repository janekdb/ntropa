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
 * RequestParamInserter.java
 *
 * Created on 29 AUGUST 2002, 1:09PM
 */

package org.ntropa.runtime.sao.util;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;


/**
 * Server active object to 'reflect' http request parameters as placeholder
 * replacements.
 * <p>
 * This class can be used to replace placeholders with request parameters.
 * <p>
 * Example usage.<br>
 * Let's say we need a page to be served with two hyperlinks with one query arg
 * being the same as the param of the URL
 * <p>
 * <pre>
 * URL = course-list.html?page=1&pid=pid-mm-01-01032f
 * links:
 *   course-list.html?page=2&pid=pid-mm-01-01032f
 *   course-list.html?page=3&pid=pid-mm-01-01032f
 * </pre>
 * This be handled by enclosing the link HTML thus
 * <p>
 * <pre>
 * &lt;!--
 *     name = &quot;param-inserter&quot;
 *     placeholder-pid = &quot;pid-placeholder&quot;
 * --&gt;
 *   &lt;a href=&quot;course-list.html?page=2&pid=pid-placeholder&quot;&gt;page 2&lt;/a&gt;
 *   &lt;a href=&quot;course-list.html?page=3&pid=pid-placeholder&quot;&gt;page 3&lt;/a&gt;
 * &lt;!-- name = &quot;/param-inserter&quot; --&gt;
 * </pre>
 * <p>
 * In application.properties bind the name &quot;param-inserter&quot; with this line
 * <p>
 * <pre>
 * sao.param-inserter.class-name = org.ntropa.runtime.sao.util.RequestParamInserter
 * </pre>
 *
 * @author  Janek Bogucki
 * @version $Id: RequestParamInserter.java,v 1.8 2004/11/18 20:26:20 jdb Exp $
 */
public class  RequestParamInserter extends BaseServerActiveObject {
    
    
    public void controlSelf( InvocationContext icb ) throws Exception {
        
        setInvocationContext( icb ) ;
        
    }
    

    /**
     * Return the value of the param 'name' from the Http request object.
     * <p>
     * If the param is not present then the value returned will be null. null
     * has a special meaning within the placeholder replacement scheme, normally
     * it means the parent SAO is to provider a replacement value. Here it is possible
     * to argue that an IllegalArgumentException should be thrown because the param
     * was missing. However that would break the delegation model. We could fix this
     * by using a special NULL object like JBoss 3 does in the server startup code.
     * <p>
     * @return The value of the param, without encoding, either URL encoding or HTML conversion.
     */
    public String getPlaceholderReplacement( String name ) {
        
        String paramValue = getInvocationContext().getHttpServletRequest().getParameter( name );
        
        return paramValue ;
        
    }
    
}
