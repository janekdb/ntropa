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
 * SelectByRequestAttributeSAODriverSAO.java
 *
 * Created on 12 March 2002, 13:19
 */

package org.ntropa.runtime.sao.examples;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;


/**
 * This class will set six request attributes so the SelectByRequestAttributeSAO
 * can be demoed/tested.
 *
 *  hours-tens
 *  hours-units
 *
 *  minutes-tens
 *  minutes-units
 *
 *  seconds-tens
 *  seconds-units
 *
 *  Each of these will be one of "0", "1", ..., "9".
 *
 * @author  jdb
 * @version $Id: SelectByRequestAttributeSAODriverSAO.java,v 1.1 2002/03/12 14:01:05 jdb Exp $
 */
public class SelectByRequestAttributeSAODriverSAO extends BaseServerActiveObject {
    
    public void controlSelf ( InvocationContext icb ) throws Exception {
        
        HttpServletRequest request = icb.getHttpServletRequest () ;
        
        Calendar now = GregorianCalendar.getInstance () ;
        
        int hour = now.get ( Calendar.HOUR_OF_DAY ) ;
        request.setAttribute ( "hours-tens", "" + hour / 10 ) ;
        request.setAttribute ( "hours-units", "" + hour % 10 ) ;
        
        int minute = now.get ( Calendar.MINUTE ) ;
        request.setAttribute ( "minutes-tens", "" + minute / 10 ) ;
        request.setAttribute ( "minutes-units", "" + minute % 10 ) ;
        
        int second = now.get ( Calendar.SECOND ) ;
        request.setAttribute ( "seconds-tens", "" + second / 10 ) ;
        request.setAttribute ( "seconds-units", "" + second % 10 ) ;
        
    }
}
