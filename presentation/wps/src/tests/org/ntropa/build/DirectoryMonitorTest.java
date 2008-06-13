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
 * DirectoryMonitorTest.java
 *
 * Created on July 20, 2001, 17:30:01 PM
 */

/**
 *
 * @author  jdb
 * @version $Id: DirectoryMonitorTest.java,v 1.23 2002/12/09 21:58:50 jdb Exp $
 */

package tests.org.ntropa.build;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.DirectoryMonitor;
import org.ntropa.build.DirectoryMonitorException;
import org.ntropa.build.FileChangeSet;
import org.ntropa.build.channel.StandardInputFilter;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.FilesystemChange;
import org.ntropa.utility.NtropaTestWorkDirHelper;
import org.ntropa.utility.UniqueFilenameSequence;

/**
 * 
 * @author jdb
 * @version $Id: DirectoryMonitorTest.java,v 1.23 2002/12/09 21:58:50 jdb Exp $
 */
public class DirectoryMonitorTest extends TestCase {

    private String TEST_ROOT;

    private File m_TestRoot;

    private static final int MAX_TREE_DEPTH = 3;

    private List m_Tokens;

    private List m_TestTreeList;

    private long m_LastModificationTime;

    public DirectoryMonitorTest(java.lang.String testName) {
        super(testName);

        m_TestTreeList = new ArrayList();

    }

    protected void setUp() throws java.io.IOException {

        TEST_ROOT = new NtropaTestWorkDirHelper().getWorkDir() + "/tests.org.ntropa.build.DirectoryMonitorTest";

        m_TestRoot = new File(TEST_ROOT);

        // / Leave this around to inspect it.
        FileUtilities.killDirectory(m_TestRoot);

        if (!m_TestRoot.mkdirs())
            throw new IOException();

        m_LastModificationTime = 0;
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        /* Remove all files created */

        FileUtilities.killDirectory(TEST_ROOT);

    }

    /* Comments copied from junit.framework.TestSuite. */

    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests. It
     * runs a collection of test cases. Here is an example using the dynamic
     * test definition.
     * 
     * <pre>
     * TestSuite suite = new TestSuite();
     * suite.addTest(new MathTest(&quot;testAdd&quot;));
     * suite.addTest(new MathTest(&quot;testDivideByZero&quot;));
     * </pre>
     * 
     * Alternatively, a TestSuite can extract the tests to be run automatically.
     * To do so you pass the class of your TestCase class to the TestSuite
     * constructor.
     * 
     * <pre>
     * TestSuite suite = new TestSuite(MathTest.class);
     * </pre>
     * 
     * This constructor creates a suite with all the methods starting with
     * "test" that take no arguments.
     * 
     * @see Test
     */
    public static Test suite() {

        TestSuite suite = new TestSuite(DirectoryMonitorTest.class);

        return suite;
    }

    /*
     * public void testAlwaysSucceeds () { //
     * System.out.println("testAlwaysSucceeds");
     * 
     * assertTrue ( true ) ; }
     */

    /**
     * Test constructors reject bad args.
     */
    public void testConstructors() throws IOException {

        File f = new File(m_TestRoot, "index.html");
        if (!f.createNewFile())
            fail("Failed to create fixture.");

        try {
            DirectoryMonitor dm = new DirectoryMonitor((String) null);
            fail("Null String accepted.");
        } catch (DirectoryMonitorException e) {
        }

        try {
            DirectoryMonitor dm = new DirectoryMonitor(f.getAbsolutePath());
            fail("Non directory accepted: " + f.getAbsolutePath());
        } catch (DirectoryMonitorException e) {
        }

        try {
            DirectoryMonitor dm = new DirectoryMonitor((File) null);
            fail("Null File accepted.");
        } catch (DirectoryMonitorException e) {
        }

        try {
            DirectoryMonitor dm = new DirectoryMonitor(f);
            fail("Non directory accepted: " + f.getAbsolutePath());
        } catch (DirectoryMonitorException e) {
        }

    }

    /*
     * There should be no change when a minimal filesystem (one directory) is
     * monitored and has not changed.
     */
    public void testNoChangeOnMinimalDirectory() throws DirectoryMonitorException {

        DirectoryMonitor fm = new DirectoryMonitor(TEST_ROOT);
        FileChangeSet fe = fm.monitorFolder();

        // System.out.println("testAlwaysSucceeds");

        boolean okay = fe.size() == 0;
        assertTrue("Change reported for unchanged minimal directory", okay);

    }

    /*
     * There should be no change when a non-minimal filesystem is monitored and
     * has not changed.
     */
    public void testNoChangeOnDirectory() throws DirectoryMonitorException {

        makeFilesystem();

        DirectoryMonitor fm = new DirectoryMonitor(TEST_ROOT);
        FileChangeSet fe = fm.monitorFolder();

        // System.out.println("testAlwaysSucceeds");

        boolean okay = fe.size() == 0;
        assertTrue("Change reported for unchanged non-minimal directory", okay);

    }

    public void testAddOneFile() throws DirectoryMonitorException {

        DirectoryMonitor fm = new DirectoryMonitor(TEST_ROOT);

        File f = new File(TEST_ROOT + File.separator + "one-file.html");
        try {
            f.createNewFile();
        } catch (Exception e) {
            fail("Failed to create fixture.");
        }

        FileChangeSet fe = fm.monitorFolder();

        if (fe.size() == 0)
            fail("Change missed for single file add");
        if (fe.size() > 1)
            fail("Too many changes reported for single file add");

        boolean okay = fe.getEvent(0) == FileChangeSet.ADDED;

        assertTrue("Event was not ADDED for single file add", okay);

    }

    public void testDeleteOneFile() throws DirectoryMonitorException {

        /* Start with the file then delete it */
        File f = new File(TEST_ROOT + File.separator + "one-file.html");
        try {
            f.createNewFile();
        } catch (Exception e) {
            fail("Failed to create fixture.");
        }

        DirectoryMonitor fm = new DirectoryMonitor(TEST_ROOT);

        if (!f.delete())
            fail("Failed to create fixture.");

        FileChangeSet fe = fm.monitorFolder();

        if (fe.size() == 0)
            fail("Change missed for single file delete");
        if (fe.size() > 1)
            fail("Too many changes reported for single file delete");

        boolean okay = fe.getEvent(0) == FileChangeSet.DELETED;

        assertTrue("Event was not DELETED for single file delete", okay);

    }

    public void testAddOneDeleteOne() throws DirectoryMonitorException {

        String OneFile = TEST_ROOT + File.separator + "one-file.html";
        String OneFileNew = TEST_ROOT + File.separator + "one-file-new.html";

        /* Start with the file, add another, then delete this one */
        File f = new File(OneFile);
        try {
            f.createNewFile();
        } catch (Exception e) {
            fail("Failed to create fixture.");
        }

        DirectoryMonitor fm = new DirectoryMonitor(TEST_ROOT);

        /* ADD */
        File g = new File(OneFileNew);
        try {
            g.createNewFile();
        } catch (Exception e) {
            fail("Failed to create fixture: File addition failed.");
        }

        /* DELETE */
        if (!f.delete())
            fail("Failed to create fixture: File deletion failed.");

        FileChangeSet fe = fm.monitorFolder();

        if (fe.size() < 2)
            fail("Too few changes reported");
        if (fe.size() > 2)
            fail("Too many changes reported");

        boolean gotAdded = false;
        boolean gotDeleted = false;
        for (int i = 0; i < 2; i++) {
            switch (fe.getEvent(i)) {
            case FileChangeSet.ADDED:
                gotAdded = true;
                if (!OneFileNew.equals(fe.getPath(i)))
                    fail("The file reported as added was not the file that was added");
                break;
            case FileChangeSet.DELETED:
                gotDeleted = true;
                if (!OneFile.equals(fe.getPath(i)))
                    fail("The file reported as deleted was not the file that was deleted");
                break;
            default:
                fail("Wrong event type reported.");

            }
        }
        assertTrue("Event types were not ADDED and DELETED", gotAdded && gotDeleted);

    }

    /**
     * This might flush an unstated file listing order dependency
     * 
     */
    public void testDeleteOneAddOne() throws DirectoryMonitorException {

        String OneFileDelete = TEST_ROOT + File.separator + "file-a1";
        String OneFileAdd = TEST_ROOT + File.separator + "one-file.html";

        /*
         * abhishek NOTES:- This has been commented by me as i felt that there
         * is no need to execute this method. Also i am not clearly sure of what
         * this does except from my knowledge it tells me that it creates a
         * normal tree structure. But then its not getting used in the rest of
         * the execution of this method, so I have commented it out.
         * 
         * janek NOTES:- I commented this back in. The tree structure is used
         * when the DirectoryMonitor is created.
         */

        makeFilesystem();

        /*
         * abhishek NOTES:- Didn't really quite understand the purpose of using
         * this over here. If a file has to be deleted and from the system,
         * shouldn't it be done after we have executed the initialization of
         * DirectoryMonitor, as its only after that, that the system can keep
         * track of the changes. Also if its done this way then fe.size() will
         * always be less then 2, as system won't record the changes made to it.
         * 
         * janek NOTES:- This appears to a correct observation. I changed the
         * testAddOneDeleteOne test which was in the wrong order.
         */

        /*
         * Add a file, create the directory monitor, then delete this file and
         * add another file
         */
        File f = new File(OneFileDelete);

        try {
            f.createNewFile();
        } catch (IOException e) {
            fail("Failed to create fixture: File creation failed.");
        }

        DirectoryMonitor fm = new DirectoryMonitor(TEST_ROOT);

        /* DELETE */
        if (!f.delete())
            fail("Failed to create fixture: File deletion failed.");

        /* ADD */
        File g = new File(OneFileAdd);
        try {
            g.createNewFile();
        } catch (Exception e) {
            fail("Failed to create fixture: File addition failed,");
        }

        FileChangeSet fe = fm.monitorFolder();

        if (fe.size() < 2)
            fail("Too few changes reported");
        if (fe.size() > 2)
            fail("Too many changes reported");

        boolean gotAdded = false;
        boolean gotDeleted = false;
        for (int i = 0; i < 2; i++) {
            switch (fe.getEvent(i)) {
            case FileChangeSet.ADDED:
                gotAdded = true;
                if (!OneFileAdd.equals(fe.getPath(i)))
                    fail("The file reported as added was not the file that was added");
                break;
            case FileChangeSet.DELETED:
                gotDeleted = true;
                if (!OneFileDelete.equals(fe.getPath(i)))
                    fail("The file reported as deleted was not the file that was deleted");
                break;
            default:
                fail("Wrong event type reported.");

            }
        }
        assertTrue("Event types were not ADDED and DELETED", gotAdded && gotDeleted);

    }

    public void testModifyOne() throws DirectoryMonitorException {

        String OneFile = TEST_ROOT + File.separator + "one-file.html";

        File f = new File(OneFile);
        try {
            f.createNewFile();
        } catch (Exception e) {
            fail("Failed to create fixture: File addition failed,");
        }

        DirectoryMonitor fm = new DirectoryMonitor(TEST_ROOT);

        modifyFile(f);

        FileChangeSet fe = fm.monitorFolder();

        // System.out.println ("fe.size = "+fe.size ());

        if (fe.size() < 1)
            fail("Too few changes reported");
        if (fe.size() > 1)
            fail("Too many changes reported");

        if (fe.getEvent(0) != FileChangeSet.MODIFIED)
            fail("The file was modified but not reported as modified.");

        if (!OneFile.equals(fe.getPath(0)))
            fail("The file reported as modified was not the file that was modified");

    }

    public void testModifyMany() throws DirectoryMonitorException {

        String FilePrefix = TEST_ROOT + File.separator + "file-";

        int fileCnt = 100;

        for (int i = 1; i <= fileCnt; i++) {
            String fn = FilePrefix + i;
            File f = new File(fn);
            try {
                f.createNewFile();
            } catch (Exception e) {
                fail("Failed to create fixture: File addition failed: " + fn);
            }
        }

        DirectoryMonitor fm = new DirectoryMonitor(TEST_ROOT);

        /*
         * Modify the files in groups and check the report. Groups:
         * 1,2,3,...,100 1,3,5,...,99 1,4,7,...,100 1,5,9,...,97 ... 1,100
         * 
         * 2,3,4,...,100 2,4,6,...,100 2,5,8,...,98 2,6,10,...,98 ... 2
         */
        for (int offset = 0; offset <= 1; offset++) {
            for (int step = 1; step < 100; step++) {

                // Modify a group, remember the modified files for easy
                // comparision.
                SortedSet modSet = new TreeSet();

                for (int fileIDX = 1 + offset; fileIDX <= fileCnt; fileIDX = fileIDX + step) {
                    String fn = FilePrefix + fileIDX;
                    try {
                        File f = new File(fn);
                        modifyFile(f);
                        // System.out.println ("Modified: " + fn + " - " +
                        // f.lastModified () );
                    } catch (Exception e) {
                        fail("Failed to create fixture");
                    }
                    modSet.add(fn);
                } // fileIDX

                // The group has been modified, find out if the changes were
                // detected.
                FileChangeSet fe = fm.monitorFolder();

                // System.out.println ("step/offset:" + step + "/" + offset );
                if (fe.size() == 0) {
                    System.out.println("Dumping returned FileChangeSet:");
                    System.out.println(fe.toString());
                    fail("There were modified files but none were detected.");

                }
                checkFileChangeSet(fe, null, modSet, null);

            } // s
        } // offset

    }

    public void testTargetDeletedThenAdded() throws DirectoryMonitorException, IOException {

        DirectoryMonitor fm = new DirectoryMonitor(TEST_ROOT);

        File f = new File(TEST_ROOT, "one-file.html");
        assertTrue(f.createNewFile());

        FileChangeSet fcs = fm.monitorFolder();
        FileChangeSet efcs = new FileChangeSet();
        efcs.fileAdded(f);
        assertEquals("Correct events for fixture creation", efcs, fcs);

        /* Add a symlink. */
        File s = new File(TEST_ROOT, "symlink.html");
        FileUtilities.makeSymbolicLink(f, s);
        fcs = fm.monitorFolder();
        efcs = new FileChangeSet();
        efcs.fileAdded(s);
        assertEquals("Correct events for symlink creation", efcs, fcs);

        // dump(s);

        /* Delete target */
        assertTrue(f.delete());
        // dump(s);
        fcs = fm.monitorFolder();
        efcs = new FileChangeSet();
        efcs.fileDeleted(f);
        efcs.targetFileDeleted(s);

        assertEquals("Correct events for deleted target file", efcs, fcs);

        /* Re-add the target */
        assertTrue(f.createNewFile());
        // dump(s);
        fcs = fm.monitorFolder();
        efcs = new FileChangeSet();
        efcs.fileAdded(f);
        efcs.targetFileAdded(s);
        assertEquals("Correct events for re-added target file", efcs, fcs);

    }

    // private void dump(File s) throws IOException{
    // PrintStream out = System.out;
    // out.println("s.lastModified: "+s.lastModified());
    //        
    // out.println("s.getAbsolutePath: "+s.getAbsolutePath());
    // out.println("s.getCanonicalPath: "+s.getCanonicalPath());
    //        
    // out.println("s.exists(): "+s.exists());
    // out.println("s.getCanonicalFile().exists():
    // "+s.getCanonicalFile().exists());
    // }

    /**
     * Added in response to a bug with uploading files over a slow connection.
     * Sometimes the file had not finished uploading when it was reported as
     * modified or added.
     * 
     * This test adds a file and then modifies it by appending to it to simulate
     * a file upload.
     * 
     * The test is repeated with the 'uploading' file inside a sub-directory.
     * This is to test an alternative code path within 'readFiles'.
     */
    public void testFileUpload() throws DirectoryMonitorException {
        String oneFile = TEST_ROOT + "/uploaded.html";

        File f = new File(oneFile);
        try {
            f.createNewFile();
        } catch (Exception e) {
            fail("Failed to create fixture: File addition failed");
        }

        for (int trial = 1; trial <= 2; trial++) {
            DirectoryMonitor fm = new DirectoryMonitor(TEST_ROOT);

            /*
             * We need at least two identical modification times for this test
             * to test that the modification time is not the sole determinant of
             * modification detection.
             */
            boolean twoTheSame = false;
            long lastLastModified = 0;
            for (int i = 1; i <= 5; i++) {
                appendToFile(f);

                if (f.lastModified() == lastLastModified)
                    twoTheSame = true;
                lastLastModified = f.lastModified();

                FileChangeSet fe = fm.monitorFolder();

                // System.out.println ("fe.size = "+fe.size ());

                if (fe.size() < 1)
                    fail("Too few changes reported");
                if (fe.size() > 1)
                    fail("Too many changes reported");

                if (fe.getEvent(0) != FileChangeSet.MODIFIED)
                    fail("The file was modified but not reported as modified.");

                if (!oneFile.equals(fe.getPath(0)))
                    fail("The file reported as modified was not the file that was modified");
            }

            if (!twoTheSame)
                fail("The test was not based on a changing file with an unchanging modification time. Try running the test again.");

            if (trial == 2)
                break;

            /* Now the fixture with the sub-directory */
            File subDir = new File(TEST_ROOT, "sub-dir");
            if (!subDir.mkdir())
                fail("Failed to create fixture: Directory addition failed");

            oneFile = TEST_ROOT + "/sub-dir/uploaded.html";

            f = new File(oneFile);
            try {
                f.createNewFile();
            } catch (Exception e) {
                fail("Failed to create fixture: File addition failed");
            }

        }

    }

    /**
     * Adding a file of the same name as the one just deleted may flush a bug
     */
    public void testDeleteOneAddSame() throws DirectoryMonitorException {

        makeFilesystem();

        String OneFile = TEST_ROOT + File.separator + "a1.d/file-b2";

        SortedSet deletedSet = new TreeSet();
        deletedSet.add(OneFile);

        DirectoryMonitor fm = new DirectoryMonitor(TEST_ROOT);

        /* Delete a file, create the directory monitor, then add the same file */

        /* Delete */
        File f = new File(OneFile);

        if (!f.delete())
            fail("Failed to create fixture: File deletion failed.");

        FileChangeSet fe = fm.monitorFolder();

        checkFileChangeSet(fe, null, null, deletedSet);

        /* Add */
        try {
            f.createNewFile();
        } catch (Exception e) {
            fail("Failed to create fixture: File addition failed,");
        }

        fe = fm.monitorFolder();
        // deletedSet = addedSet
        checkFileChangeSet(fe, deletedSet, null, null);

    }

    /**
     * Use a seeded random sequence of changes to run a sequence tests.
     */
    public void testSequenceOfChanges() throws DirectoryMonitorException {

        /*
         * Repeat these actions:
         * 
         * 1. Make and remember a sequence of changes to the filesystem. (Remove
         * events that would not be reported i.e. add file 'x' followed by
         * delete file 'x' would not result in any detected change.
         * 
         * We need to remove references to ADDED or MODIFIED events when we have
         * a DELETED event.
         * 
         * We need to remove references to MODIFIED events when we have an ADDED
         * event.
         * 
         * By using a non repeating sequence of file and directory names we
         * avoid having to consider converting a DELETE then ADD into a MODIFY.
         * 2. Check the results.
         * 
         */
        makeFilesystem();

        UniqueFilenameSequence ufs = new UniqueFilenameSequence();
        // for ( int i = 0 ; i < 100 ; i++ )
        // System.out.println ( ufs.next () );
        Random random = new Random(0);

        FilesystemChange FSChange = new FilesystemChange(TEST_ROOT, ufs, random);

        DirectoryMonitor fm = new DirectoryMonitor(TEST_ROOT);

        short sequenceCnt = 20;
        for (int seqIDX = 1; seqIDX <= sequenceCnt; seqIDX++) {
            SortedSet addedSet = new TreeSet();
            SortedSet modifiedSet = new TreeSet();
            SortedSet deletedSet = new TreeSet();

            // System.out.println("" + seqIDX + " of " + sequenceCnt );
            // Do a random change to the filesystem
            try {
                FSChange.changeFilesystem(addedSet, modifiedSet, deletedSet);
                // changeFilesystem ( TEST_ROOT, ufs, random, addedSet,
                // modifiedSet, deletedSet ) ;
            } catch (IOException e) {
                fail("An exception was encountered by the test while changing the filesystem.");
            }

            // How did we get on?
            FileChangeSet fe = fm.monitorFolder();
            checkFileChangeSet(fe, addedSet, modifiedSet, deletedSet);
        }

    }

    /*
     * SNIP
     */

    /**
     * Test the class by asking for a report on a changing filesystem
     */
    public void testConcurrentChanges() throws DirectoryMonitorException {

        /*
         * Start with an empty filesystem and add files to it while collecting
         * the changes concurrently.
         */

        int producerThreadCnt = 10;
        int fileCntPerThread = 40;

        DirectoryMonitor dm = new DirectoryMonitor(TEST_ROOT);

        // Create new file producers.
        List tList = new ArrayList();
        for (int tIDX = 1; tIDX <= producerThreadCnt; tIDX++) {
            FileProducer fp = new FileProducer(TEST_ROOT, "file-" + tIDX + "-", fileCntPerThread);
            Thread fpThread = new Thread(fp);
            tList.add(fpThread);
            fpThread.start();
        }

        List feList = new ArrayList();

        FileChangeSet fe;
        while (isOneThreadAlive(tList)) {

            fe = dm.monitorFolder();
            feList.add(fe);

            // System.out.println ("Waiting...");
            /*
             * try { Thread.sleep ( 20 ) ; } catch ( InterruptedException e ) {}
             */

        }

        // Don't forget about file which may have been added since the last call
        // to monitorFolder ().
        fe = dm.monitorFolder();
        feList.add(fe);

        // Count the changes.
        int changeCnt = 0;
        Iterator it = feList.iterator();
        while (it.hasNext()) {
            fe = (FileChangeSet) it.next();
            changeCnt += fe.size();
        }

        /*
         * static public void assertEquals(String message, Object expected,
         * Object actual) {...}
         */

        assertEquals("Actual and reported number of changes different.", producerThreadCnt * fileCntPerThread,
                changeCnt);

        /*
         * try { Thread.sleep ( 20*1000 ) ; } catch ( InterruptedException e ) {}
         */

        // Loop until the file producer finishes.
        // Consolidate the reported changes.
        // Check expectations against reported cahanges.
    }

    private boolean isOneThreadAlive(List threadList) {

        boolean gotOne = false;

        Iterator it = threadList.iterator();
        while (it.hasNext()) {
            Thread fp = (Thread) it.next();
            if (fp.isAlive()) {
                gotOne = true;
                break;
            }
        }

        return gotOne;
    }

    /**
     * Test the filtering is working.
     * 
     * Ignore:
     * 
     * 1. files/directories using characters outside of a-zA-Z0-9, hyphen,
     * underscore, dot. 2. files/directories starting with a dot. 4. directories
     * starting with an underscore except _include, _application, _data,
     * _presentation. 4. files/directories inside an ignored directory (This is
     * handled in the implementation of DirectoryMonitor. When an ignored
     * directory is encountered it is not descended into.) 5. files matching the
     * pattern ".*\.html.+ (gets index.html-old, nt.html.021025 ( .DAV gets
     * modified a lot during WebDAV use, even in directories not being directly
     * accessed.)
     * 
     * Directory: _notes, used by Dreamweaver. .* *~ #* *.html.+ (gets
     * index.html-old)
     * 
     */
    public void testFilteringInitialisation() throws DirectoryMonitorException {

        /* This ensures the filter used properly in the intial scan */
        modifyFile(new File(m_TestRoot, "index#.html"));
        DirectoryMonitor dm = new DirectoryMonitor(m_TestRoot, new StandardInputFilter());
        FileChangeSet fcs = dm.monitorFolder();
        if (fcs.size() > 0)
            fail("A change was reported where no change should have been reported.\n" + fcs);

    }

    public void testFiltering() throws DirectoryMonitorException {

        DirectoryMonitor dm = new DirectoryMonitor(m_TestRoot, new StandardInputFilter());

        String okayChars = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + ".-_";

        char charArray[] = new char[1];
        for (char b = 32; b < 255; b++) {
            charArray[0] = b;
            String s = new String(charArray);
            if (s.equals(File.separator))
                continue;
            if (okayChars.indexOf(s) != -1)
                continue;
            // 02-10-29 RJ. Wouldn't work on Gattaca??
            if (!s.equals("?"))
                continue;

            // System.out.println ("testFiltering: " + b + " " + s );
            modifyFile(new File(m_TestRoot, s));
            modifyFile(new File(m_TestRoot, s + "somechars"));
            modifyFile(new File(m_TestRoot, "someotherchars" + s));

            File dir = new File(m_TestRoot, "a-" + s + ".dir");
            if (!dir.mkdir())
                fail("Failed to create directory for fixture: " + dir);

        }

        modifyFile(new File(m_TestRoot, ".shouldbeignored"));
        modifyFile(new File(m_TestRoot, ".cvsrc"));
        modifyFile(new File(m_TestRoot, "endswithdot."));
        modifyFile(new File(m_TestRoot, "no-extension"));
        modifyFile(new File(m_TestRoot, "index.htmll"));
        modifyFile(new File(m_TestRoot, "index.html-old"));
        modifyFile(new File(m_TestRoot, "index.html (bu)"));
        modifyFile(new File(m_TestRoot, "index.html~"));
        modifyFile(new File(m_TestRoot, "index.html.021025"));

        File dotDir = new File(m_TestRoot, ".DAV");
        if (!dotDir.mkdir())
            fail("Failed to create directory for fixture: " + dotDir);
        modifyFile(new File(dotDir, "home.html"));

        File underScoreDir = new File(m_TestRoot, "_info");
        if (!underScoreDir.mkdir())
            fail("Failed to create directory for fixture: " + underScoreDir);
        modifyFile(new File(underScoreDir, "home.html"));

        FileChangeSet fcs = dm.monitorFolder();
        if (fcs.size() != 0)
            fail("Files that should have been ignored were reported.\n" + fcs);

        /*
         * Enter system dirs literally to make a better test. interface don't
         * use Constants
         */
        List systemDirs = Arrays.asList(new String[] { "_include", "_application", "_data", "_presentation" });
        SortedSet addedSet = new TreeSet();
        Iterator it = systemDirs.iterator();
        while (it.hasNext()) {
            String dir = (String) it.next();
            File sysDir = new File(m_TestRoot, dir);
            if (!sysDir.mkdir())
                fail("Failed to create directory for fixture: " + sysDir);
            addedSet.add(sysDir.getAbsolutePath());

            File addedFile = new File(sysDir, "home.html");
            modifyFile(addedFile);
            addedSet.add(addedFile.getAbsolutePath());
        }
        fcs = dm.monitorFolder();

        checkFileChangeSet(fcs, addedSet, null, null);

    }

    /*
     * In 2007-Jan ntropa was configured to use as an input folder a folder that
     * was unreadable. The result was NPE. This test confirms that an exception
     * is thrown instead.
     */
    public void testExceptionThrownWhenDirectoryNotReadable() {
        File unreadableDirectory = new File(TEST_ROOT, "unreadable");
        assertTrue(unreadableDirectory.mkdir());
        assertTrue(FileUtilities.chmod(unreadableDirectory, "000"));

        try {
            new DirectoryMonitor(unreadableDirectory);
            fail("Correct exception was thrown");
        } catch (DirectoryMonitorException e) {
            // expected
        } finally {
            // tearDown will fail with this otherwise
            // [junit] java.lang.NullPointerException
            // [junit] at java.util.Arrays$ArrayList.<init>(Arrays.java:2355)
            // [junit] at java.util.Arrays.asList(Arrays.java:2341)
            // [junit] at
            // org.ntropa.utility.FileUtilities.killDirectory(FileUtilities.java:71)
            assertTrue(FileUtilities.chmod(unreadableDirectory, "700"));
        }
    }

    public void testExceptionThrownWhenDirectoryDoesNotExist() {
        try {
            new DirectoryMonitor(new File(TEST_ROOT, "no-such-directory"));
        } catch (DirectoryMonitorException e) {
            // expected
        }
    }

    /*
     * When a developer needs to get a local copy of the files on the
     * development server wdfs can be used to mount the remote directory over
     * the local directory. At the time this test was added File.listFiles would fail with
     * a NPE but it this test did not fail so the cause was elsewhere.
     */
    public void testMonitoringContinesWhenDirectoryIsReplaced() throws Exception {

        File monitored = new File(TEST_ROOT, "my-dir");
        String path = monitored.getAbsolutePath();
        assertTrue(monitored.mkdir());

        DirectoryMonitor dm = new DirectoryMonitor(monitored);
        assertEquals(dm.monitorFolder(), new FileChangeSet());

        /* Add a file so the subsequent test can monitor a deletion. */
        {
            File a = new File(monitored, "a-file.txt");
            assertTrue(a.createNewFile());

            FileChangeSet fe = new FileChangeSet();
            fe.fileAdded(a);

            assertEquals(fe, dm.monitorFolder());

        }

        /* Replace the monitored directory */
        {
            FileUtilities.killDirectory(monitored);
            assertTrue(!monitored.exists());
            /* Maybe no requirement to reinitialise */
            monitored = new File(path);
            assertTrue(monitored.mkdir());

            File b = new File(monitored, "b-file.txt");
            assertTrue(b.createNewFile());

            FileChangeSet fe = new FileChangeSet();
            fe.fileAdded(b);

            File a = new File(monitored, "a-file.txt");
            fe.fileDeleted(a);

            assertEquals(fe, dm.monitorFolder());
        }

        /* Rename the monitored directory */
        {
            File renamed = new File(TEST_ROOT, "my-dir-moved");
            monitored.renameTo(renamed);
            /* Maybe no requirement to reinitialise */
            monitored = new File(path);
            assertTrue(monitored.mkdir());

            File c = new File(monitored, "c-file.txt");
            assertTrue(c.createNewFile());

            FileChangeSet fe = new FileChangeSet();
            fe.fileAdded(c);

            File b = new File(monitored, "b-file.txt");
            fe.fileDeleted(b);

            assertEquals(fe, dm.monitorFolder());
        }
    }

    /*
     * Shared methods
     */

    /**
     * Modify a file
     */
    protected void modifyFile(File f) {
        try {
            FileWriter w = new FileWriter(f);
            w.write("modified");
            w.close();
            // Although Java reports the modification time in milliseconds
            // Linux only records to the second so we bump the modification time
            // forward.

            f.setLastModified(nextModificationTime());
            // System.out.println ("modifyFile:" + f + ":" + f.lastModified ()
            // );

        } catch (Exception e) {
            fail("Failed to create fixture: File modification failed,");
        }
    }

    /**
     * Return a time guaranteed to be greater than the last retuned value by at
     * least 2 seconds and greater than the current time by at least 2 seconds.
     */
    protected long nextModificationTime() {

        long mt;

        synchronized (this) {
            long curTime = System.currentTimeMillis();
            m_LastModificationTime += 2000;
            if (m_LastModificationTime <= curTime + 2000)
                m_LastModificationTime = curTime + 2000;
            mt = m_LastModificationTime;
        }

        return mt;

    }

    /**
     * Append to a file
     * 
     * This *does not* artificially change the modification time.
     */
    protected void appendToFile(File f) {
        try {
            FileWriter w = new FileWriter(f.getCanonicalPath(), true);
            w.write("appended");
            w.close();
        } catch (Exception e) {
            fail("Failed to create fixture: File appendage failed,");
        }

    }

    /*
     * Given a FileChangeSet and a group of expected changes fail on the first
     * difference.
     */
    protected void checkFileChangeSet(FileChangeSet fcs, SortedSet actualAdded, SortedSet actualModified,
            SortedSet actualDeleted) {

        /* allow null args */
        if (actualAdded == null)
            actualAdded = new TreeSet();
        if (actualModified == null)
            actualModified = new TreeSet();
        if (actualDeleted == null)
            actualDeleted = new TreeSet();

        SortedSet reportedAdded = new TreeSet();
        SortedSet reportedModified = new TreeSet();
        SortedSet reportedDeleted = new TreeSet();

        for (int feIDX = 0; feIDX < fcs.size(); feIDX++) {

            String path = fcs.getPath(feIDX);

            switch (fcs.getEvent(feIDX)) {
            case FileChangeSet.ADDED:
                reportedAdded.add(path);
                break;
            case FileChangeSet.MODIFIED:
                reportedModified.add(path);
                break;
            case FileChangeSet.DELETED:
                reportedDeleted.add(path);
                break;
            default:
                fail("Wrong event type reported.");
            }

        }

        /*
         * static public void assertEquals(String message, Object expected,
         * Object actual) {...}
         */

        assertEquals("Actual and reported file count for ADDED different.", actualAdded.size(), reportedAdded.size());

        assertEquals("Actual and reported file count for MODIFIED different.", actualModified.size(), reportedModified
                .size());

        assertEquals("Actual and reported file count for DELETED different.", actualDeleted.size(), reportedDeleted
                .size());

        /* Look for different paths */

        SortedSet reported[] = { reportedAdded, reportedModified, reportedDeleted };
        SortedSet actual[] = { actualAdded, actualModified, actualDeleted };
        String msg[] = { "A pathname for ADDED entries was wrong.", "A pathname for MODIFIED entries was wrong.",
                "A pathname for DELETED entries was wrong." };

        for (int setIDX = 0; setIDX < reported.length; setIDX++) {
            Iterator reportedIt = reported[setIDX].iterator();
            Iterator actualIt = actual[setIDX].iterator();
            while (reportedIt.hasNext()) {
                String reportedFilepath = (String) reportedIt.next();
                String actualFilepath = (String) actualIt.next();
                assertTrue(msg[setIDX], reportedFilepath.equals(actualFilepath));
            }
        }

        /**
         * Test the changes returned from a sequence of changes is in a certain
         * order.
         * 
         * Let D be the depth of a file or directory.
         * 
         * Extract the ADDED events. Then D should form a monotonic increasing
         * sequence. ie. 1,1,2,3,3,3,4,4,5,6,7,7. This is a failed sequence:
         * 1,4,3,2,4,2,1,1,2,3,4
         * 
         * Extract the DELETED events. Then D should form a monotonic
         * descreasing sequence. ie. 7,7,6,6,5,4,3,3,3,3,2,1,1,1. This is a
         * failed sequence: 9,8,7,6,4,3,2,4,2,1,1,2,3,4
         * 
         * (We don't care about the order of MODIFIED events.)
         * 
         * The order for ADDED and DELETED matches the order the files and
         * directories would be created or deleted in.
         */

        int lastAddedDepth = 0;
        int lastDeletedDepth = 0;

        for (int feIDX = 0; feIDX < fcs.size(); feIDX++) {

            String path = fcs.getPath(feIDX);
            int depth = FileUtilities.pathElementCount(path);

            switch (fcs.getEvent(feIDX)) {
            case FileChangeSet.ADDED:
                if (lastAddedDepth == 0)
                    lastAddedDepth = depth;

                if (depth < lastAddedDepth)
                    fail("The deepest last order for ADDED events was violated:\n" + fcs);

                break;
            case FileChangeSet.MODIFIED:
                // don't care
                break;
            case FileChangeSet.DELETED:
                if (lastDeletedDepth == 0)
                    lastDeletedDepth = depth;

                if (depth > lastDeletedDepth)
                    fail("The deepest first order for DELETED events was violated:\n" + fcs);

                break;
            default:
                fail("Wrong event type reported.");
            }

        }
        // System.out.println("DirectoryMonitorTest:\n" + fcs );

    }

    /**
     * Make a sample directory tree:
     * 
     * root: a1.d b1.d c1.d d1.d file-a1 file-b1 file-c1 file-d1
     * 
     * Each directory a1.d, b1.d, c1.d, d1.d is populated with:
     * 
     * a2.d b2.d c2.d d2.d file-a2 file-b2 file-c2 file-d2
     * 
     * Each directory a2.d, b2.d, c2.d, d2.d is populated with:
     * 
     * a3.d b3.d c3.d d3.d file-a3 file-b3 file-c3 file-d3
     */

    protected void makeFilesystem() {

        m_Tokens = Arrays.asList(new String[] { "a", "b", "c", "d" });

        int currentDepth = 1;
        try {
            makeContents(m_TestRoot, m_Tokens, currentDepth);
        } catch (Exception e) {
            fail("Failed to create fixture.");
        }

    }

    protected void makeContents(File root, List tokens, int currentDepth) throws Exception, IOException {

        for (Iterator it = tokens.iterator(); it.hasNext();) {
            String tok = (String) it.next();

            File f = new File(root + File.separator + "file-" + tok + currentDepth);
            f.createNewFile();

            m_TestTreeList.add(f.getAbsolutePath());

            File d = new File(root + File.separator + tok + currentDepth + ".d");
            if (d.mkdirs()) {
                m_TestTreeList.add(d.getAbsolutePath());

                if (currentDepth < MAX_TREE_DEPTH)
                    makeContents(d.getAbsoluteFile(), tokens, currentDepth + 1);
            } else {
                throw new Exception("Failed to create folder or folder already existed");
            }

        }

    }

    public class FileProducer implements Runnable {

        String m_RootDir;

        String m_FilePrefix;

        int m_FileCnt;

        public void run() {

            for (int fileIDX = 0; fileIDX < m_FileCnt; fileIDX++) {
                // System.out.println ("FileProducer: " + fileIDX );

                File f = new File(m_RootDir + File.separator + m_FilePrefix + fileIDX);
                try {
                    f.createNewFile();
                } catch (Exception e) {
                    fail("Failed to create fixture.");
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        }

        FileProducer(String rootDir, String filePrefix, int fileCnt) {

            m_RootDir = rootDir;
            m_FilePrefix = filePrefix;
            m_FileCnt = fileCnt;

        }

    }

}
