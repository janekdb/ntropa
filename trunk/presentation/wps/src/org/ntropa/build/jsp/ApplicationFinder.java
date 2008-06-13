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
 * ApplicationFinder.java
 *
 * Created on 21 November 2001, 15:49
 */

package org.ntropa.build.jsp;

import java.util.Properties;

/**
 * An <code>ApplicationFinder</code> looks up data for a
 * <code>ServerActiveHtml</code> based on the given name.
 *
 * @author  jdb
 * @version $Id: ApplicationFinder.java,v 1.5 2006/03/09 12:49:24 jdb Exp $
 */
public interface ApplicationFinder {

    static final String CLASS_NAME_PROPNAME = "class-name" ;
    static final String PROPERTY_NAMES_PREFIX = "prop." ;
    
    /**
     * Return the name of the class to use for a server active object
     * or null if the name is not in the <code>Properties</code> object.
     *
     * The queried <code>Properties</code> object will typically be obtained
     * from getSaoData ().
     *
     * @param saoData A <code>Properties</code> object which may or may not contain the class name
     *
     * @return A <code>String</code> representing the name of the ServerActiveHtml
     * object to look up the corressponding class name for.
     */
    String getClazzName ( Properties saoData );
   
    
    /**
     * <p>Return the data to use for a server active object based on it's name
     * or null if the name look up failed.
     *
     * @param name A <code>String</code> representing the name of the ServerActiveHtml
     * object to look up the corressponding data for. This name is set in the HTML page
     * <p>
     * <pre>
     * &lt;-- name = &quot;the-name&quot; --&gt;
     * other elements and HTML
     * &lt;-- name = &quot;/the-name&quot; --&gt;
     * </pre>
     * </p>
     *
     * @return A <code>Properties</code> with all the data corresponding to the
     * given name. If no data is found return null.
     */
    Properties getSaoData ( String name ) ;
    
    
    /**
     * Return true if the browser should be instructed to not serve pages from it's
     * local cache.
     *
     * @return True if code should be generated to disable the browser's use of it's
     * local cache otherwise false.
     */
    boolean isBrowserCacheDisable () ;
    
    
    /**
     * Return true if the proxy should be instructed to not serve pages from it's
     * cache.
     *
     * @return True if headers should be generated to disable the proxy's use of it's
     * cache otherwise false.
     */
    boolean isProxyCacheDisable () ;
}

