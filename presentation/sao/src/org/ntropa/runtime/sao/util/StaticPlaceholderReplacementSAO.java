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
 * StaticPlaceholderReplacementSAO.java
 *
 * Created on 07 February 2003, 16:17
 */

package org.ntropa.runtime.sao.util;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.utility.StringUtilities;



/**
 * This server active object can be configured to provide a fixed replacement value
 * for a placeholder code.
 * <p>
 * Any child sao which requests a replacement value will recieve the value this
 * object is configured to supply provided no closer sao provided a value.
 * </p>
 * <p>
 * To configure the sao to return "03-Feb-2003" for the code "date", enter these
 * properties in application.properties
 * <br>
 * <pre>
 * sao.intercept.class-name=StaticPlaceholderReplacementSAO
 * sao.intercept.prop.code=date
 * sao.intercept.prop.value=03-Feb-2003
 * </pre>
 * </p>
 *
 * @author  jdb
 * @version $Id: StaticPlaceholderReplacementSAO.java,v 1.1 2003/02/07 16:00:52 jdb Exp $
 */
public class StaticPlaceholderReplacementSAO extends BaseServerActiveObject {
    
    
    private String code = null ;
    
    
    public void setCode ( String code ) {
        
        StringUtilities.validateNonZeroLength ( code ) ;
        
        this.code = code ;
    }
    
    
    private String value = null ;
    
    
    public void setValue ( String value ) {
        
        StringUtilities.validateNonZeroLength ( value ) ;
        
        this.value = value ;
    }
    
    
    public String getPlaceholderReplacement ( String name ) {
        
        if ( code == null )
            throw new IllegalStateException ( "code was not set" ) ;
        
        if ( value == null )
            throw new IllegalStateException ( "value was not set" ) ;
        
        if ( name.equals ( code ) )
            return value ;
        
        /*
         * Delegate to parent SAO.
         */
        return null ;
    }
}
