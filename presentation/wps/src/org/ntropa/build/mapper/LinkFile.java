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
 * LinkFile.java
 *
 * Created on 03 December 2002, 12:37
 */

package org.ntropa.build.mapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This class models a link file.
 * <p>
 * The file is a loadable by java.util.Properties.
 * <p>
 * One property is defined:
 * <table><tr><td>property name<td>meaning<td>example
 * <tr><td>href
 * <td>The URI of the file to link to
 * <td>wps://mba/advice/advice.html
 * </table>
 *
 * @author  Janek Bogucki
 * @version $Id: LinkFile.java,v 1.4 2002/12/12 10:42:46 jdb Exp $
 */
public class LinkFile {
    
    /**
     * The URI of the linked file.
     *
     * Example: http://mba/advice/advice.html
     *
     * (The source file will contain wps://mba/advice/advice.html)
     */
    private URI uri = null ;
    
    public static final String WPS_PROTOCOL = "wps" ;
    public static final String WPS_SCHEME_URI_PREFIX = WPS_PROTOCOL + "://" ;
    
    /**
     * Construct a <code>LinkFile</code> based on the href property found in
     * sourceFile
     * @param sourceFile a file containing a href property
     * @throws IOException
     * @throws URISyntaxException
     */
    public LinkFile ( File sourceFile ) throws URISyntaxException, IOException {
        
        if ( sourceFile == null )
            throw new IllegalArgumentException ( "sourceFile was null" ) ;
        
        if ( ! sourceFile.canRead () )
            throw new IOException ( "can not read sourceFile: " + sourceFile ) ;
        
        Properties p = new Properties () ;
        
        p.load ( new FileInputStream ( sourceFile ) ) ;
        
        String href = p.getProperty ( "href" ) ;
     
        init ( href ) ;
    }

    /*
     * TODO
     * Check for removal of FileNotFoundException, IOException in client classes.
     */
    
    /**
     * Construct a <code>LinkFile</code> based on the URI
     * @param uri A URI such as wps://mba/advice.html
     */
    public LinkFile ( String uri ) throws URISyntaxException {
        
        init ( uri ) ;
    }
    
    
    private void init ( String href ) throws URISyntaxException {
        
        if ( href == null )
            throw new IllegalArgumentException ( "sourceFile did not have 'href' property" ) ;
        
        if ( ! href.startsWith ( WPS_SCHEME_URI_PREFIX ) )
            throw new IllegalArgumentException ( "'href' did not start with " + WPS_SCHEME_URI_PREFIX ) ;
        
        uri = new URI ( href ) ;
    }
    
    
    public String getHost () {
        
        return uri.getHost () ;
    }
    
    
    /**
     * Always returns "wps"
     */
    public String getProtocol () {
        
        return uri.getScheme() ;
    }
    
    
    public String getPath () {
        
        return uri.getPath () ;
    }
    
    
    /**
     * Un-bastardised version of the URL
     */
    public String toString () {
        
        String s = uri.toString() ;
        
        //s = WPS_SCHEME_URI_PREFIX + s.substring ( "http://".length () ) ;
        
        return new ToStringBuilder ( this ).append ( "uri", s ).toString ()  ;
    }
}
