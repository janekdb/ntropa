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
 * PlaceholderExample.java
 *
 * Created on 28 March 2002, 12:03
 */

package org.ntropa.runtime.sao.examples;

import java.text.DecimalFormat;

import org.ntropa.runtime.sao.BaseServerActiveObject;


/**
 * This class demonstrates the placeholder replacement feature of the
 * web publishing system.
 *
 * 
 * @author  jdb
 * @version $Id: PlaceholderExample.java,v 1.2 2002/09/11 22:14:02 jdb Exp $
 */
public class PlaceholderExample extends BaseServerActiveObject {
    
    private DecimalFormat df = new DecimalFormat ( "#,##.##" ) ;
    
    public String getPlaceholderReplacement (String name) {
        
        
        if ( name.equals( "price" ) ) {
            double price = Math.random () * 50 ;
            return df.format ( price ) ;
        }
        else if( name.equals( "delivery-time" ) ) {
            return "" + Math.round( Math.random() * 120 ) ;
        }

        /* Delegate to parent container */
        return null ;
    }    
}
