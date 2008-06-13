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
 * DirectoryPair.java
 *
 * Created on July 30, 2001, 3:28 PM
 */

package org.ntropa.build;

import java.io.File;

/**
 * This class maintains a mapping between two directories refered
 * to the source directory and the destination directory.
 * @author  jdb
 * @version $Id: DirectoryPair.java,v 1.4 2002/04/04 12:27:52 jdb Exp $
 */
public class DirectoryPair {
    
    protected File m_Source;
    
    protected File m_Destination;
    
    /**
     * Creates new DirectoryPair
     *
     *
     * @param source a <code>File</code> object using an absolute
     * path to a directory which exists.
     * @param destination a <code>File</code> object using an absolute
     * path to a directory which exists.
     */
    public DirectoryPair ( File source, File destination) throws DirectoryPairException {
        
        assign ( source, destination ) ;
    }
    
    public DirectoryPair ( String source, File destination) throws DirectoryPairException {
        
        assign ( new File ( source ), destination ) ;
    }
    
    public DirectoryPair ( File source, String destination) throws DirectoryPairException {
        
        assign ( source, new File ( destination ) ) ;
    }
    
    public DirectoryPair ( String source, String destination) throws DirectoryPairException {
        
        assign ( new File ( source ), new File ( destination ) ) ;
    }
    
    protected void assign ( File source, File destination ) throws DirectoryPairException {
        
        if ( ! source.isAbsolute () )
            throw new DirectoryPairException ( "Path was not absolute: " + source.getPath () );
        
        if ( ! source.isDirectory () )
            throw new DirectoryPairException ( "Path was not a directory: " + source.getPath () );
        
        // DirectoryPairTest showed this tests was unneccessary.
        //if ( source.getPath ().length () == 0 )
        //    throw new DirectoryPairException () ;
        
        if ( ! destination.isAbsolute () )
            throw new DirectoryPairException ( "Path was not absolute: " + destination.getPath () );
        
        if ( ! destination.isDirectory () )
             throw new DirectoryPairException ( "Path was not a directory: " + destination.getPath ()  );
        
        // DirectoryPairTest showed this tests was unneccessary.
        //if ( destination.getPath ().length () == 0 )
        //    throw new DirectoryPairException () ;
        
        m_Source = source.getAbsoluteFile () ;
        m_Destination = destination.getAbsoluteFile () ;
        
    }
    
    public File getSource () {
        return m_Source ;
    }
    
    public File getDestination () {
        return m_Destination ;
    }
    
    public String toString () {
        return "DirectoryPair: [" + m_Source + "," + m_Destination + "]" ;
    }
}
