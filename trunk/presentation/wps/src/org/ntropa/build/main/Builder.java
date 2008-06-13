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
 * UpdateProcess.java
 *
 * Created on 30 October 2001, 17:39
 */

package org.ntropa.build.main;

/**
 * This interface details the bean properties and life cycle methods of a object
 * that can monitor one or more directories for changes and synchronize an equal
 * number of corresponding jsp directories.
 * 
 * @author jdb
 */
public interface Builder {

    int getUpdateCheckSeconds();

    void setUpdateCheckSeconds(int updateCheckSeconds);

    /*
     * Life cycle methods
     */

    void init(BuilderConfiguration bc) throws Exception;

    void start() throws Exception;

    void stop();
}
