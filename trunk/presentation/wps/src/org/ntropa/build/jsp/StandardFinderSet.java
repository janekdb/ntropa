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
 * StandardFinderSet.java
 *
 * Created on 21 November 2001, 15:29
 */

package org.ntropa.build.jsp;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 *
 * @author  jdb
 * @version $Id: StandardFinderSet.java,v 1.3 2002/11/30 23:03:09 jdb Exp $
 */
public class StandardFinderSet implements FinderSet {
    
    private ApplicationFinder _applicationFinder ;
    
    private PresentationFinder _presentationFinder ;
    
    private TemplateFinder _templateFinder ;
    
    /**
     * Return an ApplicationFinder
     */
    public ApplicationFinder getApplicationFinder () {
        return _applicationFinder ;
    }
    
    /**
     * Set an ApplicationFinder
     */
    public void setApplicationFinder ( ApplicationFinder applicationFinder ) {
        _applicationFinder = applicationFinder ;
    }
    
    /**
     * Set an PresentationFinder
     */
    public void setPresentationFinder ( PresentationFinder presentationFinder ) {
        _presentationFinder = presentationFinder ;
    }
    
    /**
     * Return an PresentationFinder
     */
    public PresentationFinder getPresentationFinder () {
        return _presentationFinder ;
    }
    
    
    /**
     * Return a TemplateFinder
     */
    public TemplateFinder getTemplateFinder () {
        return _templateFinder ;
    }
    
    /**
     * Set an TemplateFinder
     */
    public void setTemplateFinder ( TemplateFinder templateFinder ) {
        _templateFinder = templateFinder ;
    }
    
    public String toString () {
        
        return new ToStringBuilder (this, ToStringStyle.MULTI_LINE_STYLE ).
        append ( "_applicationFinder", _applicationFinder).
        append ( "_presentationFinder", _presentationFinder).
        append ( "_templateFinder", _templateFinder ).
        toString ();
        
    }
    
}
