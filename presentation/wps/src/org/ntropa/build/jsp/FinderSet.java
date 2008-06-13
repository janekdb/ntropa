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
 * FinderSet.java
 *
 * Created on 21 November 2001, 15:26
 */

package org.ntropa.build.jsp;

/**
 * This interface defines the methods used to obtain various Finders
 *
 * On 01-November-21 there was just one method.
 *
 * @author  jdb
 * @version $Id: FinderSet.java,v 1.2 2001/11/29 16:03:50 jdb Exp $
 */
public interface FinderSet {

    /**
     * Return an ApplicationFinder
     */
    public ApplicationFinder getApplicationFinder () ;
    
    /**
     * Return an PresentationFinder
     */
    public PresentationFinder getPresentationFinder () ;
    
    /**
     * Return a TemplateFinder
     */
    public TemplateFinder getTemplateFinder () ;
}
