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
 * PathWalker.java
 *
 * Created on 22 October 2001, 22:50
 */

package org.ntropa.utility;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Allows
 * @author  jdb
 * @version $Id: PathWalker.java,v 1.2 2001/10/24 00:10:38 jdb Exp $
 */
public class PathWalker {
    
    protected List _elements ;
    protected Iterator _iterator ;
    
    /** Creates new PathWalker */
    public PathWalker ( File file ) {
        init ( file.getPath () ) ;
    }
    
    /** Creates new PathWalker */
    public PathWalker ( String path ) {
        init ( path ) ;
    }
    
    protected void init ( String path ) {
        List pathElements = FileUtilities.pathElements ( path ) ;
        _elements = new LinkedList () ;
        
        /*
         * Build a longest first walk up the path.
         *
         * 'a/b/c/d' gives
         * 1. a/b/c/d
         * 2. a/b/c
         * 3. a/b
         * 4. a
         * 5.
         *
         *
         * '/a/b/c/d' gives
         * 1. /a/b/c/d
         * 2. /a/b/c
         * 3. /a/b
         * 4. /a
         * 5. /
         *
         */
        StringBuffer culmulativePath = new StringBuffer () ;
        
        boolean needSeparator = false ;
        if ( path.startsWith ( File.separator ) ) {
            _elements.add ( 0, File.separator ) ;
            needSeparator = true ;
        }
        else
            _elements.add ( 0, "" ) ;
        
        Iterator it = pathElements.iterator () ;
        while ( it.hasNext () ) {
            
            /*
             * Use a separator the first or second time depending on weather
             * the path was relative or absolute.
             */
            if ( needSeparator )
                culmulativePath.append ( File.separator ) ;
            needSeparator = true ;
            
            culmulativePath.append ( (String) it.next () ) ;
            
            _elements.add ( 0, culmulativePath.toString () ) ;
        }
        
    }
    
    public Iterator iterator () {
        _iterator = _elements.iterator () ;
        return _iterator ;
    }
    
    public boolean hasNext () {
        return _iterator.hasNext () ;
    }
    
    public Object next () throws NoSuchElementException {
        return _iterator.next () ;
    }
    
}
