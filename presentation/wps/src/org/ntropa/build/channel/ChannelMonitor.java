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
 * ChannelMonitor.java
 *
 * Created on 22 October 2001, 12:48
 */

package org.ntropa.build.channel;

import org.ntropa.build.FileChangeSet;

/**
 * interface to an object which can monitor and channel and
 * update the channel.
 *
 * @author  jdb
 * @version $Id: ChannelMonitor.java,v 1.1 2001/10/23 00:58:34 jdb Exp $
 */
public interface ChannelMonitor {

    FileChangeSet update ();
    
}

