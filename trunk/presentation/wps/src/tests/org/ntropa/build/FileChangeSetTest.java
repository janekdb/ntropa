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
 * FileChangeSetTest.java
 *
 * Created on 09 December 2002, 12:18
 */

package tests.org.ntropa.build;

// import java.util.Collections ;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.FileChangeSet;

/**
 * 
 * @author Janek Bogucki
 * @version $Id: FileChangeSetTest.java,v 1.1 2002/12/09 12:49:01 jdb Exp $
 */
public class FileChangeSetTest extends TestCase {

    public FileChangeSetTest(String testName) {
        super(testName);
    }

    /* Comments copied from junit.framework.TestSuite. */

    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests. It
     * runs a collection of test cases.
     * 
     * This constructor creates a suite with all the methods starting with
     * "test" that take no arguments.
     */
    public static Test suite() {

        TestSuite suite = new TestSuite(FileChangeSetTest.class);
        return suite;
    }

    /*
     * This tests the short parameter list methods for adding events to the
     * FileChangeSet
     */
    public void testMutators() throws Exception {

        FileChangeSet fcs = new FileChangeSet();

        /*
         * Not the greatest test fixture ever. Overcome lack of accessors
         * returning Sets
         */
        FileChangeSet added = new FileChangeSet();
        added.fileAdded("file-1.html");

        FileChangeSet modified = new FileChangeSet();
        modified.fileModified("file-2.html");

        FileChangeSet deleted = new FileChangeSet();
        deleted.fileDeleted("file-3.html");

        FileChangeSet targetDeleted = new FileChangeSet();
        targetDeleted.targetFileDeleted("file-4.html");

        FileChangeSet targetAdded = new FileChangeSet();
        targetAdded.targetFileAdded("file-5.html");

        assertEquals("size of ADDED correct", 0, fcs.getAdded().size());
        assertEquals("size of MODIFIED correct", 0, fcs.getModified().size());
        assertEquals("size of DELETED correct", 0, fcs.getDeleted().size());
        assertEquals("size of TARGET FILE DELETED correct", 0, fcs.getTargetDeleted().size());
        assertEquals("size of TARGET FILE ADDED correct", 0, fcs.getTargetAdded().size());

        fcs.fileAdded("file-1.html");

        assertEquals("size of ADDED correct", 1, fcs.getAdded().size());
        assertEquals("content of ADDED correct", added, fcs.getAdded());
        assertEquals("size of MODIFIED correct", 0, fcs.getModified().size());
        assertEquals("size of DELETED correct", 0, fcs.getDeleted().size());
        assertEquals("size of TARGET FILE DELETED correct", 0, fcs.getTargetDeleted().size());
        assertEquals("size of TARGET FILE ADDED correct", 0, fcs.getTargetAdded().size());

        fcs.fileModified("file-2.html");

        assertEquals("size of ADDED correct", 1, fcs.getAdded().size());
        assertEquals("content of ADDED correct", added, fcs.getAdded());
        assertEquals("size of MODIFIED correct", 1, fcs.getModified().size());
        assertEquals("content of MODIFIED correct", modified, fcs.getModified());
        assertEquals("size of DELETED correct", 0, fcs.getDeleted().size());
        assertEquals("size of TARGET FILE DELETED correct", 0, fcs.getTargetDeleted().size());
        assertEquals("size of TARGET FILE ADDED correct", 0, fcs.getTargetAdded().size());

        fcs.fileDeleted("file-3.html");

        assertEquals("size of ADDED correct", 1, fcs.getAdded().size());
        assertEquals("content of ADDED correct", added, fcs.getAdded());
        assertEquals("size of MODIFIED correct", 1, fcs.getModified().size());
        assertEquals("content of MODIFIED correct", modified, fcs.getModified());
        assertEquals("size of DELETED correct", 1, fcs.getDeleted().size());
        assertEquals("content of DELETED correct", deleted, fcs.getDeleted());
        assertEquals("size of TARGET FILE DELETED correct", 0, fcs.getTargetDeleted().size());
        assertEquals("size of TARGET FILE ADDED correct", 0, fcs.getTargetAdded().size());

        fcs.targetFileDeleted("file-4.html");

        assertEquals("size of ADDED correct", 1, fcs.getAdded().size());
        assertEquals("content of ADDED correct", added, fcs.getAdded());
        assertEquals("size of MODIFIED correct", 1, fcs.getModified().size());
        assertEquals("content of MODIFIED correct", modified, fcs.getModified());
        assertEquals("size of DELETED correct", 1, fcs.getDeleted().size());
        assertEquals("content of DELETED correct", deleted, fcs.getDeleted());
        assertEquals("size of TARGET FILE DELETED correct", 1, fcs.getTargetDeleted().size());
        assertEquals("content of TARGET FILE DELETED correct", targetDeleted, fcs.getTargetDeleted());
        assertEquals("size of TARGET FILE ADDED correct", 0, fcs.getTargetAdded().size());

        fcs.targetFileAdded("file-5.html");
        assertEquals("size of ADDED correct", 1, fcs.getAdded().size());
        assertEquals("content of ADDED correct", added, fcs.getAdded());
        assertEquals("size of MODIFIED correct", 1, fcs.getModified().size());
        assertEquals("content of MODIFIED correct", modified, fcs.getModified());
        assertEquals("size of DELETED correct", 1, fcs.getDeleted().size());
        assertEquals("content of DELETED correct", deleted, fcs.getDeleted());
        assertEquals("size of TARGET FILE DELETED correct", 1, fcs.getTargetDeleted().size());
        assertEquals("content of TARGET FILE DELETED correct", targetDeleted, fcs.getTargetDeleted());
        assertEquals("size of TARGET FILE ADDED correct", 1, fcs.getTargetAdded().size());
        assertEquals("content of TARGET FILE ADDED correct", targetAdded, fcs.getTargetAdded());

    }

    public void testChangeOrdering() {
        FileChangeSet fcs = new FileChangeSet();

        fcs.fileAdded("/tmp/file-added.txt");
        fcs.fileAdded("/tmp");

        fcs.fileDeleted("/var/file-deleted.txt");
        fcs.fileDeleted("/var");

        fcs.fileModified("/home/jdb/file-2.txt");
        fcs.fileModified("/home/jdb/file-1.txt");

        fcs.targetFileDeleted("/opt/target-1.png");
        fcs.targetFileDeleted("/opt");

        fcs.targetFileAdded("/usr/local");
        fcs.targetFileAdded("/usr/local/bin");

        StringBuilder expected = new StringBuilder();

        expected.append("FileChangeSet: 10 entries\n");

        expected.append("DELETED:       /var/file-deleted.txt\n");
        expected.append("DELETED:       /var\n");

        expected.append("MODIFIED:      /home/jdb/file-1.txt\n");
        expected.append("MODIFIED:      /home/jdb/file-2.txt\n");

        expected.append("ADDED:         /tmp\n");
        expected.append("ADDED:         /tmp/file-added.txt\n");

        expected.append("TARGET_DELETED:/opt/target-1.png\n");
        expected.append("TARGET_DELETED:/opt\n");

        expected.append("TARGET_ADDED:  /usr/local\n");
        expected.append("TARGET_ADDED:  /usr/local/bin\n");

        assertEquals(expected.toString(), fcs.toString());
    }

}
