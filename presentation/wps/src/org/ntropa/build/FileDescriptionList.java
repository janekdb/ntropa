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
 * FileDescriptionList.java
 *
 * Created in July 2001
 */

package org.ntropa.build;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Not thread safe.
 * 
 * @author abhishek
 * @author Janek Bogucki
 * @version $Id: FileDescriptionList.java,v 1.7 2002/12/09 22:05:59 jdb Exp $
 */
public class FileDescriptionList implements Iterable<FileDescriptionList.FD> {

    /*
     * When these fields were Vectors getSize and getTimestamp each accounted
     * for 18% of the cpu when running tickle.sh.
     * 
     * LinkedList: ?% for each ArrayList: 5 % for each
     * 
     * tickle.sh for 30 minutes
     * 
     * ArrayList: 15% (getTimestamp), 13% (getSize).
     * 
     * Post-cache-size: 5% for both. 10% in the snapshot analysis.
     * 
     * Post loop removal: 4% for both. 8% in the snapshot analysis
     * 
     * Post caching and switch to index methods in DirectoryMonitor: 1% for
     * both. 0% in the snapshot analysis.
     * 
     * Pre-map conversion:
     * 
     * Scan millis: 325
     * 
     * Over 12hrs
     * 
     * DirectoryMonitor.readFiles: 20%
     * 
     * search: ArrayList.contains: 21%
     * 
     * getTimestamp: 7%
     * 
     * getSize: 7%
     * 
     * Hotspots from snapshot:
     * 
     * ArrayList.contains: 39%
     * 
     * DirectoryMonitor.readFiles: 30%
     * 
     * ArrayList.indexOf: 26%
     * 
     * Post-map conversion:
     * 
     * Stage #1. HashMap used in search only:
     * 
     * Scan millis: 260
     * 
     * 
     * Stage #2: TreeMap used in search only:
     * 
     * Scan millis: 230
     * 
     * Stage #2: TreeMap used in getXXX(path)
     * 
     * Scan millis: 220
     * 
     * Stage #3: Discrete Lists removed, indexed accessors removed: 224
     * 
     * Stage #4: synchronized modifiers removed: 216
     */

    Map<String, FD> fds = new TreeMap<String, FD>();

    public static class FD {

        final String path;

        final long timeStamp;

        final long size;

        final boolean exists;

        public FD(String path, long timeStamp, long size, boolean exists) {
            this.path = path;
            this.timeStamp = timeStamp;
            this.size = size;
            this.exists = exists;
        }

        public String getPath() {
            return path;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public long getSize() {
            return size;
        }

        public boolean isExists() {
            return exists;
        }

    }

    /**
     * @param path
     *            The path of the file that information is being recorded for
     * @param lastModified
     *            The last modification time to record for path
     */
    public void add(String path, long lastModified) {
        add(path, lastModified, 0, true);
    }

    /**
     * @param path
     *            The path of the file that information is being recorded for
     * @param lastModified
     *            The last modification time to record for path
     * @param size
     *            The file size to record for the path
     * @param exists
     *            True if the File.exists() returns true. Dangling symlinks
     *            return false.
     */
    public void add(String path, long lastModified, long size, boolean exists) {
        fds.put(path, new FD(path, lastModified, size, exists));
    }

    /**
     * @param path
     *            The path to the file to get the <code>FD</code> for.
     * @return The FD for the file or directory or null if there is no entry for
     *         the given path. The fields on FD have the following meanings:
     *         <ul>
     *         <li>timeStamp: The last modification time of the file referred
     *         to by path or 0 if path has no recorded information</li>
     *         <li>size: The size of the file referred to by path or 0 if path
     *         has no recorded information</li>
     *         <li>exists: True if path exists.</li>
     *         </ul>
     */
    public FD getFD(String path) {
        return fds.get(path);
    }

    public boolean includes(String path) {
        return fds.containsKey(path);
    }

    public Iterator<FD> iterator() {
        return fds.values().iterator();
    }

}

/**
 * <pre>
 *  tickle.sh
 * 
 *  !/bin/bash
 * 
 *  cd &tilde;/../demo/input/master/studylink_essentials
 * 
 *  while : ; do
 * 
 *  for i in *-moved; do
 *  ##echo i: $i
 *  d=${i/-moved/}
 *  echo &quot;Moving $i to $d&quot;
 *  mv $i $d
 *  sleep 1
 *  done
 * 
 *  for i in *; do
 *  d=$i-moved
 *  echo &quot;Moving $i to $d&quot;
 *  mv $i $d
 *  sleep 1
 *  done
 * 
 *  done
 *  &lt;pre&gt;
 * 
 */
