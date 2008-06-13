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
 * Logger.java
 *
 * Created on 16 May 2002, 13:27
 */

package org.ntropa.utility;

/**
 * This interface is a temporary measure.
 * <p>
 * It should be replaced with the Jakarta commons stuff.
 *
 * @author  jdb
 * @version $Id: Logger.java,v 1.1 2002/05/16 14:17:15 jdb Exp $
 */
public interface Logger {
    
    void log( String message ) ;
    
    void log( String message, Exception e ) ;
    
}
