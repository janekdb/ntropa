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
 * StandardTemplateFinder.java
 *
 * Created on 20 November 2001, 17:43
 */

package org.ntropa.build.jsp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.ntropa.build.channel.FileLocation;
import org.ntropa.build.channel.FileLocationException;
import org.ntropa.build.html.Fragment;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.PathWalker;


/**
 *
 * @author  jdb
 * @version $Id: StandardTemplateFinder.java,v 1.4 2001/11/22 18:20:14 jdb Exp $
 */
public class StandardTemplateFinder implements TemplateFinder {
    
    /* The directory to conduct template finding within */
    private File _rootDirectory ;
    
    private FileLocation _pageLocation ;
    
    private int _debug = 0 ;
    
    /**
     * Creates new StandardTemplateFinder
     *
     * @param rootDirectory The top level directory.
     *
     * @param pageLocation A <code>FileLocation</code> object representing the
     * relative location of the page for which we are finding the template
     * 
     * @param encoding
     *            The encoding that HTML files will be read and written to/from
     *            disk with.
     */
    public StandardTemplateFinder ( File rootDirectory, FileLocation pageLocation, Charset encoding ) {
        if ( rootDirectory == null )
            throw new IllegalArgumentException (
            "Attempt to construct StandardTemplateFinder from null rootDirectory" ) ;
        
        if ( pageLocation == null )
            throw new IllegalArgumentException (
            "Attempt to construct StandardTemplateFinder from null pageLocation" ) ;
        
        _rootDirectory = rootDirectory.getAbsoluteFile () ;
        
        _pageLocation = (FileLocation) pageLocation.clone () ;
        
        this.encoding = encoding;
    }
    
    private final Charset encoding;
    
    /**
     * 
     * @return The encoding for all files handled by this
     */
    private Charset getEncoding() {
        return encoding;
    }
    
    /**
     * Set the debug level 0 - 99
     *
     * @param debug The required debug level in the range 0 - 99.
     */
    public void setDebugLevel ( int debug ) {
        /* A negative arg is a mistake; go large in response */
        _debug = debug >= 0 ? debug : 99 ;
    }
    
    /**
     * Locate a template by name.
     *
     * If found return a <code>Fragment</code> representing the template content.
     *
     * @param templateName The name of the template to locate
     */
    public Fragment getTemplate ( String templateName ) throws TemplateFinderException {
        
        if ( templateName == null || templateName.length () == 0 )
            return null ;
        
        
        /*
         * Look for the template in the _include directory in the same
         * directory as the JSP being built. If not found look in the
         * _include directory in the parent directory and so on until
         * we get to /_include (context relative).
         */
        PathWalker p = new PathWalker ( _pageLocation.getLocation () ) ;
        
        if ( _debug >= 2 )
            log ("resolveServerActiveHtmlTemplate: _pageLocation: " + _pageLocation );
        
        Iterator it = p.iterator () ;
        FileLocation templateFileLocaton = null ;
        String templateFile = null ;
        /* drop last path element, the file name */
        if ( it.hasNext () )
            it.next () ;
        
        while ( it.hasNext () ) {
            
            String nextParentDir = (String) it.next () ;
            
            String includeDir ;
            if ( nextParentDir != "" )
                includeDir = nextParentDir + File.separator +  JSPBuilder.INCLUDE_DIR  ;
            else
                includeDir = JSPBuilder.INCLUDE_DIR  ;
            
            File searchDir = new File ( _rootDirectory, includeDir ) ;
            templateFile = Template.findTemplateJsp ( searchDir, templateName ) ;
            if ( templateFile == null )
                continue ;
            
            /* We have found a matching template */
            try {
                if ( _debug >= 3 ) {
                    log ( "getTemplate: includeDir: " +  includeDir ) ;
                    log ( "getTemplate: searchDir:" +  searchDir ) ;
                    log ( "getTemplate: templateFile: " + templateFile );
                }
                templateFileLocaton = new FileLocation ( includeDir + File.separator + templateFile ) ;
            }
            catch ( FileLocationException e ) {
                log ( "getTemplate: " + e ) ;
            }
            break ;
            
        }
        
        /*
         * If the template was found read the file and return the content.
         */
        if ( templateFileLocaton != null ) {
            File f =  new File ( _rootDirectory, templateFileLocaton.getLocation () ) ;
            try {
                StringBuffer sb = new StringBuffer ( 2000 ) ;
                FileUtilities.readFile (
                f,
                sb,
                getEncoding()) ;
                return new Fragment ( sb.toString () ) ;
            }
            catch ( IOException e ) {
                throw new TemplateFinderException ( "Problem reading file: " + f + "\n" + e.toString () ) ;
            }
        }
        
        return null ;
    }
    
    /**
     * FIXME: use proper logger passed in at construction.
     */
    private void log ( String msg ) {
        System.out.println ( "[" + this.getClass ().getName () + "] " + msg /*+ "\n" + this.toString () */ );
    }
    
}
