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
 * DefaultingSelectByRequestAttributeSAO.java
 *
 * Created on 12 September 2002, 14:37
 */

package org.ntropa.runtime.sao.util;

/**
 * An extension of SelectByRequestAttributeSAO which will render the element
 * 'default' when the request attribute value does not match any child or
 * if the an element 'default' is not a child then the SAO will render
 * nothing. This is useful for turning elements on and off.
 *
 * @see SelectByRequestAttributeSAO
 * @author  Janek Bogucki
 * @version $Id: DefaultingSelectByRequestAttributeSAO.java,v 1.4 2002/11/13 09:27:43 jdb Exp $
 */
public class DefaultingSelectByRequestAttributeSAO extends SelectByRequestAttributeSAO {
    
    private static final String DEFAULT_ELEMENT_NAME = "default" ;
    
    
    protected void renderElement( String elementName ) throws Exception {
        
        if ( childExists( elementName ) ) {
            getChild( elementName ).render( getInvocationContext() ) ;
            return ;
        }
        
        if ( childExists( DEFAULT_ELEMENT_NAME ) ) {
            getChild( DEFAULT_ELEMENT_NAME ).render( getInvocationContext() ) ;
            return ;
        }
    }
    
}
