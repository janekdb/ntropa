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
 * CommonRootResolver.java
 *
 * Created on 10 December 2002, 23:31
 */

package org.ntropa.build.channel;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.ntropa.build.ContextPath;
import org.ntropa.build.mapper.LinkFile;
import org.ntropa.build.mapper.Resolver;




/**
 *
 * @author  Janek Bogucki
 * @version $Id: CommonRootResolver.java,v 1.2 2002/12/11 14:34:10 jdb Exp $
 */
public class CommonRootResolver implements Resolver {
    
    Set allowedHosts = new HashSet () ;
    
    File commonRoot ;
    
    String absolutePath = null ;
    
    
    /**
     * Resolve uris of form 'wps://mba/advice.html' into a <code>File</code> object.
     * <p>
     * @param commonRoot The root of the directory tree the resolved files will be in.
     * For example if we want this object to resolve 'wps://mba/advice.html to /opt/xl/web/webdav/sym/mba
     * then we would construct a <code>CommonRootResolver</code> like this
     * <p>
     * <pre>
     * Set s = new HashSet () ;
     * s.add ( "mba" ) ;
     * <code>
     * Resolver r = new CommonRootResolver ( new File ( "/opt/xl/web/webdav/sym" ), s ) ;
     * </code>
     * </pre>
     * @param commonRoot The root directory to resolve relative to
     * @param contextPathSet The set of allowable <code>ContextPath</code>s
     */
    public CommonRootResolver ( File commonRoot, Set contextPathSet ) {
        
        this.commonRoot = commonRoot ;
        
        for ( Iterator it = contextPathSet.iterator () ; it.hasNext () ; ) {
            ContextPath cp = ( ContextPath ) it.next () ;
            allowedHosts.add ( cp.getPath () ) ;
        }
        
    }
    
    /**
     * Given a <code>LinkFile</code> containing a URI such as
     * <p>
     * wps://mba/advice.html
     * <p>
     * and a mba channel setup up under /opt/xl/web/webdav/mba
     * this method will return /opt/xl/web/sym/mba/advice.html
     */
    public File resolve (LinkFile linkFile) throws Exception {
        
        String host = linkFile.getHost () ;
        
        if ( ! allowedHosts.contains ( host ) )
            throw new IllegalArgumentException ( "The host is not allowed: " + linkFile ) ;
        
        File resolved = new File ( commonRoot, host + linkFile.getPath () ).getAbsoluteFile () ;
        
        /* lazy initialisation so the constructor is not required to throw IOException */
        if ( absolutePath == null )
            absolutePath = commonRoot.getAbsolutePath () ;
        
        if ( ! resolved.getAbsolutePath ().startsWith ( absolutePath ) )
            throw new IllegalArgumentException (
            "There was a security problem with the resolved file (not inside common root): " + linkFile + ", resolved: " + resolved ) ;
        
        /* Defend against directory traversal with .. */
        if ( resolved.getAbsolutePath ().indexOf ( ".." ) > 0 )
            throw new IllegalArgumentException (
            "There was a security problem with the resolved file (directory traversal attempt): " + linkFile + ", resolved: " + resolved ) ;
        
        checkPath ( resolved.getAbsolutePath () ) ;
        
        return resolved ;
        
    }
    
    private StandardInputFilter sif = new StandardInputFilter () ;
    
    private void checkPath ( String path ) {
        
        for (
        StringTokenizer stok = new StringTokenizer ( path, File.separator ) ;
        stok.hasMoreTokens () ;
        ) {
            String component = stok.nextToken () ;
            if ( ! sif.charactersAcceptable ( component ) )
                throw new IllegalArgumentException (
                "There was a security problem with the resolved file (dissallowed characters present): " +
                path + " had '" + component + "'" ) ;
        }
        
    }
    
    
}