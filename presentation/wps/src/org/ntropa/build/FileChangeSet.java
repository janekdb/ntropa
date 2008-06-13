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
 * FileChangeSet.java
 *
 * Created on September 11, 2001, 3:53 PM
 */

package org.ntropa.build;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ntropa.utility.FileUtilities;

/**
 * (Renamed from FileEvent.java)
 * 
 * Maintain the list of changes in a certain order.
 * 
 * Let D be the depth of a file or directory.
 * 
 * Extract the ADDED events. Then D should form a monotonic increasing sequence.
 * ie. 1,1,2,3,3,3,4,4,5,6,7,7. This is a failed sequence: 1,4,3,2,4,2,1,1,2,3,4
 * 
 * Extract the DELETED events. Then D should form a monotonic descreasing
 * sequence. ie. 7,7,6,6,5,4,3,3,3,3,2,1,1,1. This is a failed sequence:
 * 9,8,7,6,4,3,2,4,2,1,1,2,3,4
 * 
 * (We don't care about the order of MODIFIED events.)
 * 
 * The order for ADDED and DELETED matches the order the files and directories
 * would be created or deleted in.
 * 
 * (It would be nice to reimplement this using three different sets: AddedSet
 * ModifiedSet DeletedSet )
 * 
 * @author abhishek
 * @version $Id: FileChangeSet.java,v 1.7 2002/12/09 12:54:58 jdb Exp $
 */
public class FileChangeSet {

    protected SortedSet<Change> m_Elements = new TreeSet<Change>();

    public static final int ADDED = 0;

    public static final int DELETED = 1;

    public static final int MODIFIED = 2;

    public static final int TARGET_DELETED = 3;

    public static final int TARGET_ADDED = 4;

    public static final int DIRECTORY_MODIFICATION_TIME = 0;

    public FileChangeSet() {
    }

    public void add(File file, int event) {
        add(file.getAbsolutePath(), event);
    }

    public void add(String path, int event) {
        Change c = new Change(event, path);
        m_Elements.add(c);
    }

    public void fileAdded(File file) {
        add(file, ADDED);
    }

    public void fileModified(File file) {
        add(file, MODIFIED);
    }

    public void fileDeleted(File file) {
        add(file, DELETED);
    }

    public void targetFileDeleted(File file) {
        add(file, TARGET_DELETED);
    }

    public void targetFileAdded(File file) {
        add(file, TARGET_ADDED);
    }

    public void fileAdded(String path) {
        add(path, ADDED);
    }

    public void fileModified(String path) {
        add(path, MODIFIED);
    }

    public void fileDeleted(String path) {
        add(path, DELETED);
    }

    public void targetFileDeleted(String path) {
        add(path, TARGET_DELETED);
    }

    public void targetFileAdded(String path) {
        add(path, TARGET_ADDED);
    }

    /**
     * Make a copy of a <code>FileChangeSet</code> in a thread-safe way.
     */
    public FileChangeSet(FileChangeSet fcs) {
        synchronized (fcs) {
            m_Elements = new TreeSet(fcs.m_Elements);
        }
    }

    public int getEvent(int c) {
        return getChange(c).getEvent();
    }

    public String getPath(int c) {
        return getChange(c).getPath();
    }

    public int size() {
        return m_Elements.size();
    }

    /**
     * Return the <code>Change</code> object at index <code>index</code>.
     * 
     * (This use of indices is undesirable, we should be using Iterators.)
     */
    protected Change getChange(int index) {

        int curIDX = -1;
        Change c = null;

        Iterator<Change> i = m_Elements.iterator();
        while (i.hasNext()) {
            c = i.next();
            curIDX++;
            if (curIDX == index)
                return c;
        }

        throw new IndexOutOfBoundsException();
    }

    /*
     * abhishek NOTES:- The below method was added to cater for the new
     * algorithm in the DirectoryMonitor.java in the method monitorFolders(),
     * this is basically used by the EDIT logic in the method.
     */
    public boolean search(String x) {
        Change c = null;
        Iterator i = m_Elements.iterator();
        while (i.hasNext()) {
            c = (Change) i.next();
            if (c.getPath().equals(x))
                return true;
        }
        return false;
    }

    public FileChangeSet getAdded() {
        return get(ADDED);
    }

    public FileChangeSet getModified() {
        return get(MODIFIED);
    }

    public FileChangeSet getDeleted() {
        return get(DELETED);
    }

    public FileChangeSet getTargetDeleted() {
        return get(TARGET_DELETED);
    }

    public FileChangeSet getTargetAdded() {
        return get(TARGET_ADDED);
    }

    protected FileChangeSet get(int event) {
        FileChangeSet f = new FileChangeSet();

        Change c = null;
        Iterator<Change> i = m_Elements.iterator();
        while (i.hasNext()) {
            c = i.next();
            if (c.getEvent() == event)
                f.add(c.getPath(), event);
        }
        return f;
    }

    public void removePrefix(File prefix) {
        removePrefix(prefix.getAbsolutePath());
    }

    public void removePrefix(String prefix) {
        // System.out.println("prefix:" + prefix);
        synchronized (m_Elements) {
            SortedSet newSet = new TreeSet();
            Iterator<Change> i = m_Elements.iterator();
            while (i.hasNext()) {
                Change c = i.next();
                String path = c.getPath();
                if (path.startsWith(prefix))
                    path = path.substring(prefix.length());

                Change cc = new Change(c.getEvent(), path);
                newSet.add(cc);
            }
            m_Elements = newSet;
        }
    }

    /**
     * The two <code>FileChangeSet</code>s are equal iff the two Change sets
     * are equal.
     */
    public boolean equals(Object obj) {
        FileChangeSet fcs = (FileChangeSet) obj;
        return m_Elements.equals(fcs.m_Elements);
    }

    /**
     * TODO: implement this
     */
    public int hashCode() {

        throw new UnsupportedOperationException();

    }

    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append("FileChangeSet: " + size() + " entries\n");

        Change c = null;
        Iterator<Change> i = m_Elements.iterator();
        while (i.hasNext()) {
            c = i.next();
            switch (c.getEvent()) {
            case ADDED:
                sb.append("ADDED:         ");
                break;
            case DELETED:
                sb.append("DELETED:       ");
                break;
            case MODIFIED:
                sb.append("MODIFIED:      ");
                break;
            case TARGET_DELETED:
                sb.append("TARGET_DELETED:");
                break;
            case TARGET_ADDED:
                sb.append("TARGET_ADDED:  ");
                break;

            default:
                throw new AssertionError("Unhandled case: " + c.getEvent());
            }
            sb.append(c.getPath() + "\n");
        }
        return sb.toString();
    }

    /**
     * The elements stored
     */
    protected static class Change implements Comparable {

        static private final Map<Integer, Integer> GROUP_MAP = new HashMap<Integer, Integer>();

        static {
            GROUP_MAP.put(DELETED, 1);
            GROUP_MAP.put(MODIFIED, 2);
            GROUP_MAP.put(ADDED, 3);
            GROUP_MAP.put(TARGET_DELETED, 4);
            GROUP_MAP.put(TARGET_ADDED, 5);
        }

        static private final Set<Integer> LONGEST_PATH_FIRST = new HashSet<Integer>();

        static {
            LONGEST_PATH_FIRST.add(DELETED);
            LONGEST_PATH_FIRST.add(TARGET_DELETED);
        }

        final String m_path;

        final int m_event;

        final int m_pathElementCnt;

        Change(int event, String path) {

            m_event = event;
            m_path = path;

            /*
             * Cache it for clarity.
             * 
             * Groups order:
             * 
             * DELETED events will sort into a groups in an order that could be
             * used to delete the files and directories from a file system, ie.
             * children get deleted first.
             * 
             * MODIFIED events are dumped in the the middle.
             * 
             * ADDED events will sort into a group in an order that could be
             * used to add the files and directories to a file system, ie.
             * parents get created first.
             * 
             * TARGET_DELETED is in the fourth group.
             * 
             * TARGET_ADDED is in the fifth group.
             */

            m_pathElementCnt = FileUtilities.pathElementCount(m_path);
            //            
            // int apec = FileUtilities.pathElementCount(m_path);
            // if (event == DELETED)
            // apec = -apec;
            // if (event == MODIFIED)
            // apec = 0;
            //            
            // m_AdjustedPathElementCnt = apec;
            //            

        }

        private int getPathElementCnt() {
            return m_pathElementCnt;
        }

        private String getPath() {
            return m_path;
        }

        private int getEvent() {
            return m_event;
        }

        public int compareTo(Object obj) {

            Change other = (Change) obj;

            if (this.getEvent() != other.getEvent())
                return GROUP_MAP.get(this.getEvent()) - GROUP_MAP.get(other.getEvent());

            int otherPec = other.getPathElementCnt();
            int selfPec = getPathElementCnt();

            if (selfPec == otherPec)
                return getPath().compareTo(other.getPath());

            int pecDiff = selfPec - otherPec;
            if (LONGEST_PATH_FIRST.contains(this.getEvent()))
                pecDiff = -pecDiff;

            return pecDiff;
        }

        public boolean equals(Object obj) {
            return compareTo(obj) == 0;
        }

        public int hashCode() {
            return getPath().hashCode();
        }
    }
}
