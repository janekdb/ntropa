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
 * Named.java
 *
 * Created on 16 November 2001, 16:18
 */

package org.ntropa.runtime.sao;

/**
 * Interface for an instance with a read/write name.
 * @author  jdb
 * @version $Id: Named.java,v 1.1 2001/11/16 17:36:47 jdb Exp $
 */
public interface Named {

    public String getName () ;
    
    public void setName ( String name ) ;
    
}

