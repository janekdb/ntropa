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
 * FileLocation.java
 *
 * Created on 08 August 2001, 18:27
 */

package org.ntropa.build.channel;

import java.io.File;

/**
 * A <code>FileLocation</code> object represents a file within the context
 * of a channel. The only requirement is that the <code>location</code> parameter
 * to the constructor should not start with a separator.
 * @author  jdb
 * @version $Id: FileLocation.java,v 1.6 2001/11/20 22:20:51 jdb Exp $
 */
public class FileLocation implements Cloneable {
    
    protected String m_Location;
    
    /**
     * Creates new FileLocation
     */
    public FileLocation ( String location ) throws FileLocationException {
        setLocation ( location ) ;
    }
    
    public FileLocation ( FileLocation location ) throws FileLocationException {
        setLocation ( location.getLocation () ) ;
    }
    
    public void setLocation ( String location ) throws FileLocationException {
        
        if ( location == null )
            throw new FileLocationException ( "location was null." ) ;
        
        if ( location.equals ( "" ) )
            throw new FileLocationException ( "location was empty." ) ;
        
        if ( location.startsWith ( File.separator ) )
            throw new FileLocationException ( "location was not relative: " + location ) ;
        
        m_Location = location ;
    }
    
    public String getLocation () {
        return m_Location ;
    }
    
    /** --- Implementation of Cloneable --- */
    public Object clone () {
        try {
            return super.clone () ;
        }
        catch ( CloneNotSupportedException e ) {
            /*
             * Cannot happen -- we support clone and Object provides the implementation.
             * See Java Programming Language 3/e p92
             */
            throw new InternalError ( e.toString () ) ;
        }
    }
    
}
