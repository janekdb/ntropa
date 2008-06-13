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
 * PageProperties.java
 *
 * Created on 18 April 2002, 17:04
 */

package org.ntropa.runtime.sao.util;

import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.ntropa.runtime.sao.InvocationContext;

/**
 * Change the HTTP header according to the properties for the page.
 * <p>
 * The current list of changes is
 * <ul>
 * <li>Add Cache-Control: no-cache if <code>proxy.cache.disable</code>
 * exists and is equal to &quot;yes&quot;.
 * </ul>
 *
 * @author  rj
 * @version $Id: PageProperties.java,v 1.4 2002/09/18 23:22:06 jdb Exp $
 */
public class PageProperties {
    
    /** Creates a new instance of PageProperties */
    private PageProperties() {
    }
    
    private static final String CACHE_CONTROL = "Cache-Control" ;
    private static final String NO_CACHE = "no-cache" ;
    
    public static void jspService(InvocationContext invocationBean, Properties pageProperties) {
        
        HttpServletResponse response = invocationBean.getHttpServletResponse() ;
        
        String proxyCacheDisable = pageProperties.getProperty("proxy.cache.disable") ;
        
        if ( (proxyCacheDisable != null) && proxyCacheDisable.equals("yes") ) {
            
            if ( ! response.containsHeader( CACHE_CONTROL ) )
                response.setHeader(CACHE_CONTROL, NO_CACHE) ;
//            else
//                throw new IllegalStateException( "Cache-Control header already present." );
//            
        }
        
    }
    
}
