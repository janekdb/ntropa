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
 * Resolver.java
 *
 * Created on 03 December 2002, 14:15
 */

package org.ntropa.build.mapper;

import java.io.File;

/**
 * This interface defines the methods used to map <code>LinkFile</code> to
 * physical files.
 *
 * @author  Janek Bogucki
 * @version $Id: Resolver.java,v 1.1 2002/12/03 14:42:09 jdb Exp $
 */
public interface Resolver {

    /**
     * Given a <code>LinkFile</code> containing a URI such as
     * <p>
     * wps://mba/advice.html
     * <p>
     * and a mba channel setup up under /opt/xl/web/webdav/mba
     * this method will return /opt/xl/web/webdav/mba/advice.html
     */
    File resolve ( LinkFile linkFile ) throws Exception ;
}

