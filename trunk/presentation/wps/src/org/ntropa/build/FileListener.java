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
 * FileListener.java
 *
 * Created on June 28, 2001, 1:24 PM
 */

package org.ntropa.build;

/**
 * 
 * @author jdb
 * @version $Id: FileListener.java,v 1.3 2001/08/31 16:20:54 jdb Exp $
 */

public interface FileListener {

    void fileAdded(FileListenerEvent e);

    void fileModified(FileListenerEvent e);

    void fileDeleted(FileListenerEvent e);

    void targetFileAdded(FileListenerEvent e);

    void targetFileDeleted(FileListenerEvent e);

}
