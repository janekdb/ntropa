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

// This is just a test comment
/*
 * DirectoryMonitor.java
 *
 * Created in July 2001
 */
package org.ntropa.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ntropa.build.FileDescriptionList.FD;
import org.ntropa.utility.FilePredicate;
import org.ntropa.utility.FileUtilities;

/**
 * Note: The JSP updation process would have been easier if this class had been
 * changed to record the file type (file or directory) when the intial ADDED or
 * MODIFIED data is made.
 * 
 * @author abhishek
 * @author Janek Bogucki
 * @version $Id: DirectoryMonitor.java,v 1.22 2004/04/13 11:00:35 jdb Exp $
 */
public class DirectoryMonitor {

    protected String m_path;

    protected FileDescriptionList m_fd = new FileDescriptionList();

    protected Vector listeners = new Vector();

    protected FilePredicate _filter;

    protected long _initialScanMillis;

    /**
     * Constructor to store the initial contents of the Folder to monitor in an
     * array.
     * 
     * @param directory
     *            A <code>String</code> representing a directory. The
     *            directory must already exist.
     * 
     * @param filter
     *            A <code>FilePredicate</code> object used to filter the files
     *            and directories considerd by this
     *            <code>DirectoryMonitor</code>.
     */
    public DirectoryMonitor(String directory, FilePredicate filter) throws DirectoryMonitorException {

        if (directory == null)
            throw new DirectoryMonitorException("String expected: had null");

        init(new File(directory), filter);

    }

    /**
     * Constructor to store the initial contents of the Folder to monitor in an
     * array.
     * 
     * @param directory
     *            A <code>String</code> representing a directory. The
     *            directory must already exist.
     */
    public DirectoryMonitor(String directory) throws DirectoryMonitorException {

        if (directory == null)
            throw new DirectoryMonitorException("String expected: had null");

        init(new File(directory), null);
    }

    /**
     * Constructor to store the initial contents of the Folder to monitor in an
     * array.
     * 
     * @param directory
     *            A <code>File</code> representing a directory. The directory
     *            must already exist.
     * 
     * @param filter
     *            A <code>FilePredicate</code> object used to filter the files
     *            and directories considerd by this
     *            <code>DirectoryMonitor</code>.
     */
    public DirectoryMonitor(File directory, FilePredicate filter) throws DirectoryMonitorException {

        init(directory, filter);

    }

    /**
     * Constructor to store the initial contents of the Folder to monitor in an
     * array.
     * 
     * @param directory
     *            <code>File</code> representing a directory. The directory
     *            must already exist.
     */
    public DirectoryMonitor(File directory) throws DirectoryMonitorException {

        init(directory, null);
    }

    protected void init(File directory, FilePredicate filter) throws DirectoryMonitorException {

        if (directory == null)
            throw new DirectoryMonitorException("File object expected: had null");

        try {
            FileUtilities.ensureDirectoryIsReadable(directory);
        } catch (FileNotFoundException e) {
            throw new DirectoryMonitorException(e.getMessage());
        } catch (IOException e) {
            throw new DirectoryMonitorException(e.getMessage());
        }

        setFilter(filter);

        m_path = directory.getAbsolutePath();

        initialScan();
    }

    /**
     * Scan the root directory for the first time.
     */
    protected void initialScan() {

        /* Capture a snapshot of the directory content meta-data */
        long t = System.currentTimeMillis();
        m_fd = readFiles(m_path);
        _initialScanMillis = System.currentTimeMillis() - t;

    }

    /**
     * Check for any changes to the directory being monitored since the last
     * time called. If this is the first time this method has been called report
     * any differences since the object was constructed.
     * 
     * When the target of a symlink is missing the symlink is listed, has a last
     * modification time of 0 and does not exist. When the target of a symlink
     * is restored the symlink is listed, has a last modification time > 0 and
     * does exist.
     * 
     * <pre>
     *     [junit] ------------- Standard Output ---------------
     *     --- TARGET EXISTS --
     *     [junit] s.lastModified: 1188411416000
     *     [junit] s.getAbsolutePath: /home/jdb/work/ntropa/build/work/tests.org.ntropa.build.DirectoryMonitorTest/symlink.html
     *     [junit] s.getCanonicalPath: /home/jdb/work/ntropa/build/work/tests.org.ntropa.build.DirectoryMonitorTest/one-file.html
     *     [junit] s.exists(): true
     *     [junit] s.getCanonicalFile().exists(): true
     *     
     *     --- TARGET DELETED ---
     *     [junit] s.lastModified: 0
     *     [junit] s.getAbsolutePath: /home/jdb/work/ntropa/build/work/tests.org.ntropa.build.DirectoryMonitorTest/symlink.html
     *     [junit] s.getCanonicalPath: /home/jdb/work/ntropa/build/work/tests.org.ntropa.build.DirectoryMonitorTest/symlink.html
     *     [junit] s.exists(): false
     *     [junit] s.getCanonicalFile().exists(): false
     *     
     *     --- TARGET RESTORED ---
     *     [junit] s.lastModified: 1188411416000
     *     [junit] s.getAbsolutePath: /home/jdb/work/ntropa/build/work/tests.org.ntropa.build.DirectoryMonitorTest/symlink.html
     *     [junit] s.getCanonicalPath: /home/jdb/work/ntropa/build/work/tests.org.ntropa.build.DirectoryMonitorTest/symlink.html
     *     [junit] s.exists(): true
     *     [junit] s.getCanonicalFile().exists(): true
     *     [junit] ------------- ---------------- ---------------
     * </pre>
     * 
     * @return a <code>FileChangeSet</code> representation of all changes
     */
    public FileChangeSet monitorFolder() {

        FileDescriptionList oldfdl = m_fd;
        FileChangeSet fcs = new FileChangeSet();

        m_fd = readFiles(m_path);

        // FILE ADDED
        for (FD fd : m_fd) {
            if (!oldfdl.includes(fd.getPath())) {
                fcs.add(fd.getPath(), FileChangeSet.ADDED);
            }
        }

        // FILE DELETED
        for (FD fd : oldfdl) {
            if (!m_fd.includes(fd.getPath())) {
                fcs.add(fd.getPath(), FileChangeSet.DELETED);
            }
        }

        // FILE MODIFIED, TARGET FILE DELETED or ADDED
        for (FD fd : m_fd) {
            String path = fd.getPath();

            FD oldfd = oldfdl.getFD(path);

            if (oldfd != null) {
                long oldTimestamp = oldfd.getTimeStamp();
                long newTimestamp = fd.getTimeStamp();

                if (oldTimestamp != newTimestamp) {
                    /*
                     * When the last modification time is 0 and the file
                     * corresponding to the path does not exist we are dealing
                     * with a dangling symlink. Checking the file's existence is
                     * better than confirming f.getAbsolutePath !=
                     * f.getCanonicalPath because if the parent directory is a
                     * symlink that these values will always be different.
                     */
                    if (newTimestamp == 0 && !fd.isExists()) {
                        fcs.targetFileDeleted(path);
                    } else if (oldTimestamp == 0 && !oldfd.isExists()) {
                        fcs.targetFileAdded(path);
                    } else
                        fcs.add(path, FileChangeSet.MODIFIED);
                    continue;
                }

                /*
                 * Java under Linux 2.4.x reported modification time to second
                 * resolution. This additional test detects files in the state
                 * of being appended to. This can happen when a file is being
                 * uploaded or otherwise copied into the input directory slowly.
                 */
                if (oldfd.getSize() != fd.getSize()) {
                    fcs.add(path, FileChangeSet.MODIFIED);
                }

            }
        }

        if (fcs.size() > 0)
            notifyListeners(fcs);

        return fcs;

    }

    // // this is just for a test... but this will list all the files in the
    // object
    // // selected with their timestamps
    // public void listFileDescription() {
    // long m_t1 = System.currentTimeMillis();
    // for (int c = 0; c < m_fd.size(); c++) {
    // System.out.println(m_fd.getPath(c) + " == " + m_fd.getTimestamp(c));
    // }
    //
    // System.out.println("\nRecursion time =" + (_initialScanMillis / 1000) + "
    // seconds");
    // System.out.println("Display Time =" + ((System.currentTimeMillis() -
    // m_t1) / 1000) + " seconds");
    // System.out.println("\n" + m_fd.size() + " Object[s] selected.");
    // }

    /**
     * Recursively read all the files and directories in the specified
     * directory.
     * 
     * Apply the filter if set to ignore certain files and directories.
     * 
     * @param directory
     *            The path to the directory to recurse through.
     */
    protected FileDescriptionList readFiles(String directory) {

        FileDescriptionList fld = new FileDescriptionList();

        File fp[] = new File(directory).listFiles();

        if (null == fp)
            throw new NullPointerException("File.listFiles() returned null: File.getAbsolutePath () = '"
                    + new File(directory).getAbsolutePath() + "'");

        for (File f : fp) {
            String path = f.toString();

            if (!accept(path))
                continue;

            if (f.isDirectory()) {

                /*
                 * Add self, not ever interested in the modification time for a
                 * directory.
                 */
                fld.add(path, FileChangeSet.DIRECTORY_MODIFICATION_TIME);

                FileDescriptionList nfdl = readFiles(f.toString());
                for (FD nfd : nfdl) {
                    fld.add(nfd.getPath(), nfd.getTimeStamp(), nfd.getSize(), nfd.isExists());
                }
            } else
                fld.add(path, f.lastModified(), f.length(), f.exists());

        }

        return fld;
    }

    /*
     * Support for FileChangeSetListeners
     */
    public void addFileChangeSetListener(FileChangeSetListener listener) {
        listeners.addElement(listener);
    }

    public void removeFileChangeSetListener(FileChangeSetListener listener) {
        listeners.removeElement(listener);
    }

    public void notifyListeners(FileChangeSet fcs) {
        /*
         * This avoids a race condition
         * 
         * (I kept this comment in from the Sun example I copied this from
         * although it won't affect xl - jdb.)
         */
        Vector v = (Vector) listeners.clone();

        // Deliver a FileChangeSetEvent to all listeners.
        for (int i = 0; i < v.size(); i++) {
            ((FileChangeSetListener) v.elementAt(i)).changed(new FileChangeSet(fcs));
        }
    }

    /**
     * Set the <code>FilePredicate</code> object for the DirectoryMonitor to
     * use.
     * 
     * If a file (or directory) is acceptable to the <code>FilePredicate</code>
     * inspect the file (or directory) and allow it to be be monitored. This
     * allows a <code>DirectoryMonitor</code> to ignore certain files and
     * directories.
     * 
     * Note: the root from which the <code>DirectoryMonitor</code> is using is
     * not considered in the filtering action.
     * 
     * @param filePredicate
     *            The <code>FilePredicate</code> to query. If null then accept
     *            all files and directories.
     */
    private void setFilter(FilePredicate filePredicate) {

        _filter = filePredicate;

    }

    /**
     * Check if a file or directory is acceptable
     * 
     * @return Return true if acceptable
     */
    protected boolean accept(String path) {

        return (_filter == null) || _filter.accept(path);

    }

    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("m_path", m_path).append("m_fd", m_fd)
                .append("listeners", listeners).append("_filter", _filter).append("_intialScanMillis",
                        _initialScanMillis).toString();

    }

}
