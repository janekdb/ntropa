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
 * Constants.java
 *
 * Created on 23 October 2001, 15:02
 */

package org.ntropa.build;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class is a repository for fixed value objects which
 * must be shared.
 *
 * @author  jdb
 * @version $Id: Constants.java,v 1.13 2004/01/19 06:43:38 arun Exp $
 */
public class Constants {
    
    
    /** Prevent no-arg construction */
    private Constants () {
    }
    
    
    /**
     * The JSPBuilder must not create JSPs from HTML files in
     * any of these directories
     */
    public static Set getNonHtmlDirectoryNames () {
        
        return new TreeSet ( Arrays.asList ( new String [] {
            "_presentation",
            "_application",
            "_data"
        } ) ) ;
    }
    
    
    /**
     * Suffixes of files which are copied into the JSP file system as-is
     */
    public static Set getMirroredFileSuffixes () {
        
        return new TreeSet ( Arrays.asList ( new String [] {
            ".bmp",
            ".css",
            ".gif",
            ".ico",
            ".jpg",
            ".jpeg",
            ".js",
            ".pdf",
            ".swf",
            ".txt",
            ".zip",
			".xml",
        } ) ) ;
    }
    
    /**
     * The name of the directory which contains included templates.
     */
    public static String getIncludeDirectoryName () {
        
        return "_include" ;
    }
    
    /**
     * The name of the directory which contains application parameter files
     */
    public static String getApplicationDirectoryName () {
        
        return "_application" ;
    }
    
    /**
     * The name of an application parameter file.
     */
    public static String getApplicationParamFileName () {
        
        return "application.properties" ;
    }
    
    /**
     * The name of the directory which contains presentation parameter files
     */
    public static String getPresentationDirectoryName () {
        
        return "_presentation" ;
    }
    
    /**
     * The name of an presentation parameter file.
     */
    public static String getPresentationParamFileName () {
        
        return "presentation.properties" ;
    }
    
    /**
     * The name of the directory which contains data files
     */
    public static String getDataDirectoryName () {
        
        return "_data" ;
    }
    
    /**
     *
     * Directories with a special meaning in the WPS.
     */
    public static Set getSystemDirectoryNames () {
        Set s = getNonHtmlDirectoryNames () ;
        s.add ( getIncludeDirectoryName () ) ;
        return s ;
    }
    
    /* This is a nasty hack to give a 'type' to a managed HTML page.
     * If would be better to improve the object orientation of the approach.
     */
    static public interface PageType {
        public static final int TEMPLATE = 1 ;
        public static final int PUBLIC_HTML = 2 ;
        
    }
    
    /**
     * Constants related to the HTML representation of Server Active HTML.
     */
    static public interface MarkUp {
        public static final String SERVER_ACTIVE_HTML_ATTRIBUTE = "name" ;
        public static final String ELEMENT_ATTRIBUTE = "element" ;
        public static final String USE_TEMPLATE_ATTRIBUTE = "use-template" ;
        public static final String USE_ELEMENT_ATTRIBUTE = "use-element" ;
        
        public static final String PLACEHOLDER_PREFIX = "placeholder-" ;
        
    }
    
    private static final String LINK_FILE_SUFFIX = ".link" ;
    
    /** The suffix for link files */
    public static String getLinkFileSuffix () {
        
        return LINK_FILE_SUFFIX ;
        
    }
}
