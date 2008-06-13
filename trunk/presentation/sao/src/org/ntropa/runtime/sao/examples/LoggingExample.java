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
 * LoggingExample.java
 *
 * Created on 07 February 2002, 14:46
 */

package org.ntropa.runtime.sao.examples;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;

/**
 * <p>This class is a server active object which log a message and exception
 * first to catalina.out and then to the application log.
 *
 * When the invocation context is not set AbstractServerActiveObject uses System.out
 * for logging which Catalina remaps to catalina.out.
 *
 * </p>
 * <p>
 * Bind the SAO to the name in 'application.properties' like this:</p>
 * <p><pre>
 * sao.conf-view.class-name = org.ntropa.runtime.sao.examples.LoggingExample
 * sao.conf-view.prop.debug = 7
 * </pre></p>
 * <p>
 * Note: this sao is not a good WPS citizen. It fails to invoke any of it's
 * children.</p>
 * @author  jdb
 * @version $Id: LoggingExample.java,v 1.4 2002/04/04 12:50:15 jdb Exp $
 */
public class LoggingExample extends BaseServerActiveObject {
    
    
    public void controlSelf ( InvocationContext icb ) throws Exception {
        
        /*
         * The AbstractServerActiveObject does not have access to the
         * application object until the invocation context is set so the
         * message will be logged to System.out which Catalina remaps to catalina.out.
         */        
        log ( "controlSelf: debug level: " + getDebug () ) ;
        if ( getDebug () >= 9 )
            log (
            "controlSelf: a level 9 or higher message with an exception",
            new IllegalArgumentException ( "I don't like your argument")
            ) ;
        
    }
    
    
    /**
     * Called inside the JSP to either write out some HTML.
     *
     * Throws Exception otherwise it would have to throw every exception that every
     * method of every object could throw.
     *
     * @param icb A <code>InvocationContext</code> supplied by the JSP.
     */
    public void render (InvocationContext icb) throws Exception {
        
  
        /*
         * Setting the invocation context gives AbstractServerActiveObject access to the
         * application object and the message will be logged in the application log file.
         */
        setInvocationContext ( icb ) ;
        
        // Memory stats for JVM...
        log("freeMemory (kb): " + (Runtime.getRuntime().freeMemory() / 1000 ) ) ;
        log("totalMemory (kb): " + (Runtime.getRuntime().totalMemory() / 1000 )) ;
        
        
        log ( "render: debug level: " + getDebug () ) ;
        if ( getDebug () >= 9 )
            log (
            "render: a level 9 or higher message with an exception",
            new IllegalArgumentException ( "I don't like your argument")
            ) ;
        
    }
    
    
}
