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
 * FragmentComponent.java
 *
 * Created on 27 November 2001, 12:18
 */

package org.ntropa.runtime.sao;

/**
 * The interface for objects wishing to be used as primitives with the
 * server active framework.
 *
 * @author  jdb
 * @version $Id: Fragment.java,v 1.1 2001/11/27 21:03:05 jdb Exp $
 */
public interface Fragment extends Component {

    /**
     * Set up this object inside a JSP
     *
     * @param html A <code>String</code> representing the HTML this <code>Fragment</code>
     * is responsible for.
     */
    void setHtml ( String html ) ;

}

