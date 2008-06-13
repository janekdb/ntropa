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
 * MapperTest.java
 *
 * Created on September 11, 2001, 11:11 AM
 */

package tests.org.ntropa.build.mapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.ContextPath;
import org.ntropa.build.ContextPathException;
import org.ntropa.build.DirectoryMonitor;
import org.ntropa.build.DirectoryMonitorException;
import org.ntropa.build.DirectoryPair;
import org.ntropa.build.DirectoryPairException;
import org.ntropa.build.FileChangeSet;
import org.ntropa.build.FileListener;
import org.ntropa.build.FileListenerEvent;
import org.ntropa.build.mapper.LinkFile;
import org.ntropa.build.mapper.Mapper;
import org.ntropa.build.mapper.MapperException;
import org.ntropa.build.mapper.Resolver;
import org.ntropa.utility.FilePredicate;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.FilesystemChange;
import org.ntropa.utility.NtropaTestWorkDirHelper;
import org.ntropa.utility.UniqueFilenameSequence;

/**
 * 
 * @author jdb
 * @version $Id: MapperTest.java,v 1.22 2005/06/24 13:10:33 jdb Exp $
 */
public class MapperTest extends TestCase {

    public MapperTest(String testName) {
        super(testName);
    }

    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests. It
     * runs a collection of test cases.
     */
    public static Test suite() {

        TestSuite suite = new TestSuite(MapperTest.class);
        return suite;
    }

    private final String TEST_ROOT = new NtropaTestWorkDirHelper().getWorkDir()
            + "/tests.org.ntropa.build.mapper.MapperTest";

    private final File m_TestRoot = new File(TEST_ROOT);

    private final String MASTER_CONTEXT_PATH = "mba";

    private final File m_MasterInputDir = new File(TEST_ROOT, "input/" + MASTER_CONTEXT_PATH);;

    private final File m_MasterSymlinkDir = new File(TEST_ROOT, "sym/" + MASTER_CONTEXT_PATH);

    private final String COBRAND_CONTEXT_PATH = "metro-mba";

    private final File m_CobrandInputDir = new File(TEST_ROOT, "input/" + COBRAND_CONTEXT_PATH);

    private final File m_CobrandSymlinkDir = new File(TEST_ROOT, "sym/" + COBRAND_CONTEXT_PATH);

    protected void setUp() throws java.io.IOException {

        if (!m_TestRoot.mkdir())
            fail("Failed to created temporary directory for test.");

        /* master */
        if (!m_MasterInputDir.mkdirs())
            fail("Failed to create directory: " + m_MasterInputDir.getPath());

        if (!m_MasterSymlinkDir.mkdirs())
            fail("Failed to create directory: " + m_MasterSymlinkDir.getPath());

        /* cobrand */
        if (!m_CobrandInputDir.mkdirs())
            fail("Failed to create directory: " + m_CobrandInputDir.getPath());

        if (!m_CobrandSymlinkDir.mkdirs())
            fail("Failed to create directory: " + m_CobrandSymlinkDir.getPath());

        /* Crappy hack, see class for details */
        Mapper.setDefaultDebugLevel(0);
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        FileUtilities.killDirectory(m_TestRoot);
    }

    /**
     * Makes an object that should succeed. Tries to make two objects that
     * should fail.
     */
    public void testConstructor() throws ContextPathException, DirectoryPairException {

        ContextPath cp = new ContextPath("pg-au");
        DirectoryPair dp = new DirectoryPair(m_MasterInputDir, m_MasterSymlinkDir);

        /*
         * Good args
         */
        try {
            Mapper mapper = new Mapper(cp, dp);
        } catch (MapperException e) {
            fail("Correct arguments rejected: " + e);
        }

        /*
         * Bad args.
         */
        try {
            Mapper mapper = new Mapper((ContextPath) null, dp);
            fail("Null ContextPath accepted");
        } catch (MapperException e) {
        }

        try {
            Mapper mapper = new Mapper(cp, (DirectoryPair) null);
            fail("Null DirectoryPair accepted");
        } catch (MapperException e) {
        }

    }

    /**
     * Send a FileChangeSetEvent and check the correct symbolic links are made.
     */
    public void testBasicListening() throws ContextPathException, DirectoryPairException, DirectoryMonitorException,
            IOException, MapperException {

        /*
         * Create a DirectoryMonitor object and add a Mapper as a listener.
         */

        DirectoryMonitor dm = new DirectoryMonitor(m_MasterInputDir);
        Mapper mapper = getMasterMapper();

        dm.addFileChangeSetListener(mapper);

        /*
         * add a file
         */
        File f = new File(m_MasterInputDir, "index.html");
        createFile(f);

        /* calls listeners */
        dm.monitorFolder();

        /* Symbolic link should exist now */
        File symlink = new File(m_MasterSymlinkDir, "index.html");
        assertTrue("Failed to create symbolic link to source file:" + f.getAbsolutePath(), symlink.exists());

        /*
         * delete a file
         */
        deleteFile(f);

        /* calls listeners */
        dm.monitorFolder();

        /* Symbolic link should not exist now */
        symlink = new File(m_MasterSymlinkDir, "index.html");
        assertTrue("Failed to delete symbolic link to source file:" + f.getAbsolutePath(), !symlink.exists());

    }

    /*
     * Test the construction and maintainence of a filesystem with some
     * sub-folders.
     * 
     * Let U be the upload folder, S the symbolic link, file-lk denote a symlink
     * to file.
     * 
     * We have U/index.html -> We want S/index.html-lk
     * 
     * U/index.html U/about.html -> S/index.html-lk S/about.html-lk
     * 
     * U/index.html U/services/help.html -> S/index.html-lk
     * S/services/help.html-lk
     * 
     * U/index.html U/services/help.html U/services/extra/index.html ->
     * S/index.html-lk S/services/help.html-lk S/services/extra/index.html-lk
     * 
     * Note that files are symlinks, directories are not. This is to allow the
     * content of a directories to be varied by adding and omitting files from
     * it compared to the source directory. This will allow the folder link file
     * mechanism to operate.
     * 
     */
    public void testDirectoryActions() throws IOException, DirectoryMonitorException, DirectoryPairException,
            MapperException {

        /*
         * Create a DirectoryMonitor object and add a Mapper as a listener.
         */

        DirectoryMonitor dm = new DirectoryMonitor(m_MasterInputDir);
        Mapper mapper = getMasterMapper();

        dm.addFileChangeSetListener(mapper);

        String[] dirs = new String[4];

        dirs[0] = "a.d";
        dirs[1] = "b.d";
        dirs[2] = "c.d";
        dirs[3] = "d.d";

        String dirpath = "";
        for (int i = 0; i < dirs.length; i++)
            dirpath += dirs[i] + File.separator;

        File deepDir = new File(m_MasterInputDir, dirpath);
        mkDirectory(deepDir);

        File f = new File(deepDir, "index.html");
        createFile(f);

        /* calls listeners */
        dm.monitorFolder();

        /*
         * Directories and symbolic link should exist now
         */
        dirpath = "";
        for (int i = 0; i < dirs.length; i++) {

            dirpath += dirs[i] + File.separator;
            File curDir = new File(m_MasterSymlinkDir, dirpath);

            if (!curDir.exists())
                fail("Failed to create directory:" + curDir);
            if (!curDir.isDirectory())
                fail("Non-directory found:" + curDir);
        }

        File symlink = new File(m_MasterSymlinkDir, dirpath + "index.html");
        assertTrue("Failed to create symbolic link to source file:" + f.getAbsolutePath(), symlink.exists());

        /*
         * Now delete everything in the source; the symbolic link fs should
         * follow.
         */

        File deleteRoot = new File(m_MasterInputDir, dirs[0]);
        FileUtilities.killDirectory(deleteRoot);

        /* calls listeners */
        dm.monitorFolder();

        int fileCnt = m_MasterInputDir.list().length;
        assertTrue("The symbolic filesystem was not empty: file count: " + fileCnt, fileCnt == 0);

    }

    /**
     * Execute a psuedo random sequence of filesystem changes and check the
     * symbolic filesystem mirrors the changes
     */
    public void testDirectorySynchronization() throws IOException, DirectoryMonitorException, DirectoryPairException,
            MapperException {

        UniqueFilenameSequence ufs = new UniqueFilenameSequence();

        /* Use different seed to DirectoryMonitorTest */
        Random random = new Random(48);

        /* Record the initial state of the WebDAV filesystem */
        DirectoryMonitor webDAV_DM = new DirectoryMonitor(m_MasterInputDir);
        webDAV_DM.addFileChangeSetListener(getMasterMapper());

        /* Record the initial state of the Symbolic filesystem */
        DirectoryMonitor sym_DM = new DirectoryMonitor(m_MasterSymlinkDir);

        FilesystemChange d = new FilesystemChange(m_MasterInputDir, ufs, random);

        /* 100 iterations took 83 secs */
        int changeCnt = 5;
        for (int changeIDX = 0; changeIDX < changeCnt; changeIDX++) {
            // System.out.println ("changeIDX: " + changeIDX );
            d.changeFilesystem();

            /*
             * Get both sets of changes and compare the ADDEDs and DELETEDs.
             * Account for different root directories.
             * 
             * MODIFIEDs are a bit more difficult as it would require the Mapper
             * to modify the symbolic link using a future timestamp. This is too
             * invasive of Mapper so we skip testing MODIFIEDs.
             */

            FileChangeSet webDAV_fcs = webDAV_DM.monitorFolder();
            FileChangeSet sym_fcs = sym_DM.monitorFolder();

            FileChangeSet webDAV_fcsAdded = webDAV_fcs.getAdded();
            webDAV_fcsAdded.removePrefix(m_MasterInputDir);
            // System.out.println ("webDAV_fcsAdded\n" + webDAV_fcsAdded);

            FileChangeSet sym_fcsAdded = sym_fcs.getAdded();
            sym_fcsAdded.removePrefix(m_MasterSymlinkDir);
            // System.out.println ("sym_fcsAdded\n" + sym_fcsAdded);

            assertTrue("The set of added files was different.", webDAV_fcsAdded.equals(sym_fcsAdded));

            FileChangeSet webDAV_fcsDeleted = webDAV_fcs.getDeleted();
            webDAV_fcsDeleted.removePrefix(m_MasterInputDir);
            // System.out.println ("webDAV_fcsDeleted\n" + webDAV_fcsDeleted);

            FileChangeSet sym_fcsDeleted = sym_fcs.getDeleted();
            sym_fcsDeleted.removePrefix(m_MasterSymlinkDir);
            // System.out.println ("sym_fcsDeleted\n" + sym_fcsDeleted);

            assertTrue("The set of deleted files was different.", webDAV_fcsDeleted.equals(sym_fcsDeleted));
        }

    }

    /**
     * <pre>
     * The following test cases exercise a Mapper under all supported
     * transitions. At present we do not test behaviour when a link to file is
     * modified and becomes a link to a directory, nor do we test the reverse of
     * this.
     * 
     * TODO: Add tests to determine behaviour when the target of the link is
     * missing or is deleted after the link has been made. We are not supporting
     * this behaviour but we do want to be able to recover from a link becoming
     * invalid then becoming valid.
     * 
     * In all of these transitions it is assumed the target of the link already
     * exists.
     * 
     * Transitions for a link to a file.
     * ---------------------------------
     * 
     * (FL = file link)
     * 
     * Case The link is
     * 
     * FLA ADDED
     * 
     * FLM MODIFIED (i.e. changes to link to a different file)
     * 
     * FLD DELETED
     * 
     * Transitions for the file linked to
     * -----------------------------------
     * 
     * The file linked to is
     * 
     * FLTA ADDED
     * 
     * FLTM MODIFIED
     * 
     * FLTD-PRE DELETED but the master symlink is still present
     * 
     * FLTD-POST DELETED and the master symlink is also missing
     * 
     * 
     * Transitions for a link to a directory.
     * --------------------------------------
     * 
     * (DL = directory link)
     * 
     * Case The link is
     * 
     * DLA ADDED
     * 
     * DLM MODIFIED (i.e. changes to link to a different directory)
     * 
     * DLD DELETED
     * 
     * Transitions for the directory linked to.
     * ----------------------------------------
     * 
     * The directory linked to is
     * 
     * DLTA ADDED (not supported)
     * 
     * MODIFIED
     * 
     * DLTM-FA gains a file
     * 
     * DLTM-FM a file contained within the linked directory is modified
     * 
     * DLTM-FD loses a file
     * 
     * DLTM-DA gains a sub-directory
     * 
     * DLTM-DD loses a sub-directory
     * 
     * DLTD DELETED (not supported)
     * 
     * </pre>
     */

    /**
     * 
     * Cases: FLA, FLM, FLD
     * 
     * This test checks links to files.
     * 
     * Add a link file in the cobrand and check the expected symlink is created
     * and the content of the file is as expected.
     * 
     * Modify the link file to point to a different file and check the symlink
     * contains the new content.
     * 
     * Delete the link file and check the symlink is deleted.
     * 
     * In all cases checks the correct FileListenerEvents are generated.
     * 
     * CRUD = Create, update, delete
     */
    public void testCRUDLinkFileToFile() throws IOException, DirectoryMonitorException, DirectoryPairException,
            MapperException {

        /* Crappy hack, see class for details */
        // Mapper.setDefaultDebugLevel ( 99 ) ;
        /* Make channel being linked to */
        DirectoryMonitor masterDm = new DirectoryMonitor(m_MasterInputDir);
        Mapper masterMapper = getMasterMapper();

        masterDm.addFileChangeSetListener(masterMapper);

        String FILE_CONTENT = "qwest";
        String FILE_NAME = "index.html";
        FileUtilities.writeString(new File(m_MasterInputDir, FILE_NAME), FILE_CONTENT);

        String ALT_FILE_NAME = "alt-index.html";
        String ALT_FILE_CONTENT = "second";
        FileUtilities.writeString(new File(m_MasterInputDir, ALT_FILE_NAME), ALT_FILE_CONTENT);

        /* This creates the symlinks we link to with LinkFiles */
        masterDm.monitorFolder();

        /* Setup a mapper listening to changes in the cobrand WebDAV filesystem */
        DirectoryMonitor dm = new DirectoryMonitor(m_CobrandInputDir);
        Mapper mapper = getCobrandMapper();
        dm.addFileChangeSetListener(mapper);
        // mapper.setDebugLevel ( 99 ) ;
        mapper.setResolver(new MyResolver());

        /*
         * Case: FLA
         */

        /* Listen for all file events for later checking */
        EventLogger e = addEventLogger(mapper);

        /*
         * Add the link
         */
        // wps://mba/index.html
        String LINK_CONTENT = "href=wps://" + MASTER_CONTEXT_PATH + "/" + FILE_NAME;
        String LINK_NAME = "start.html.link";
        String SYMLINK_NAME = "start.html";
        File linkFile = new File(m_CobrandInputDir, LINK_NAME);
        FileUtilities.writeString(linkFile, LINK_CONTENT);

        /* calls listeners */
        e.reset();
        dm.monitorFolder();
        mapper.monitorLinkFolder();

        /* Symbolic link should exist now */
        File symlink = new File(m_CobrandSymlinkDir, SYMLINK_NAME);
        assertTrue("symbolic link was created:" + symlink.getAbsolutePath(), symlink.exists());
        assertEquals("The content of the symbolic link was correct", FILE_CONTENT, FileUtilities.readFile(symlink));

        FileChangeSet expectedFcs = new FileChangeSet();
        expectedFcs.fileAdded("start.html");

        assertEquals("The correct FileEvents were sent after a link file was added", expectedFcs, e.getChangeSet());

        /*
         * Case: FLM
         */

        /*
         * modify link file, not content of file linked to
         */
        String ALT_LINK_CONTENT = "href=wps://" + MASTER_CONTEXT_PATH + "/" + ALT_FILE_NAME;
        FileUtilities.writeString(linkFile, ALT_LINK_CONTENT);

        /* calls listeners */
        e.reset();
        dm.monitorFolder();
        mapper.monitorLinkFolder();

        /* Symbolic link should still exist */
        assertTrue("symbolic link was still there:" + symlink.getAbsolutePath(), symlink.exists());
        assertEquals("The content of the symbolic link was correct", ALT_FILE_CONTENT, FileUtilities.readFile(symlink));

        expectedFcs = new FileChangeSet();
        expectedFcs.fileModified("start.html");

        assertEquals("The correct FileEvents were sent after a link file was modified", expectedFcs, e.getChangeSet());

        /*
         * Case: FLD
         */

        /*
         * delete it
         */
        deleteFile(linkFile);

        /* calls listeners */
        e.reset();
        dm.monitorFolder();
        mapper.monitorLinkFolder();

        /* Symbolic link should not exist now */
        assertTrue("symbolic link was deleted:" + symlink.getAbsolutePath(), !symlink.exists());

        expectedFcs = new FileChangeSet();
        expectedFcs.fileDeleted("start.html");

        assertEquals("The correct FileEvents were sent after a link file was deleted", expectedFcs, e.getChangeSet());

    }

    /**
     * Cases: FLTM
     * 
     * This tests the behaviour when the target file of a link is modified.
     */
    public void testModifyTargetFile() throws IOException, DirectoryMonitorException, DirectoryPairException,
            MapperException {

        /* Make channel being linked to */
        DirectoryMonitor masterDm = new DirectoryMonitor(m_MasterInputDir);
        Mapper masterMapper = getMasterMapper();

        masterDm.addFileChangeSetListener(masterMapper);

        String FILE_CONTENT = "qwest";
        String ALT_FILE_CONTENT = "qwest-extra";
        String FILE_NAME = "index.html";
        File TARGET_FILE = new File(m_MasterInputDir, FILE_NAME);
        FileUtilities.writeString(TARGET_FILE, FILE_CONTENT);

        /* This creates the symlinks we link to with LinkFiles */
        masterDm.monitorFolder();

        /* Setup a mapper listening to changes in the cobrand WebDAV filesystem */
        DirectoryMonitor dm = new DirectoryMonitor(m_CobrandInputDir);
        Mapper mapper = getCobrandMapper();
        dm.addFileChangeSetListener(mapper);
        // mapper.setDebugLevel ( 99 ) ;
        mapper.setResolver(new MyResolver());

        /*
         * Case: FLA, repeated with deletions
         */

        /* Listen for all file events for later checking */
        EventLogger e = addEventLogger(mapper);

        /*
         * Add the link
         */
        // wps://mba/index.html
        String LINK_CONTENT = "href=wps://" + MASTER_CONTEXT_PATH + "/" + FILE_NAME;
        String LINK_NAME = "start.html.link";
        String SYMLINK_NAME = "start.html";
        File linkFile = new File(m_CobrandInputDir, LINK_NAME);
        FileUtilities.writeString(linkFile, LINK_CONTENT);

        /* calls listeners */
        e.reset();
        dm.monitorFolder();
        mapper.monitorLinkFolder();

        File symlink = new File(m_CobrandSymlinkDir, SYMLINK_NAME);
        assertTrue("symbolic link was created:" + symlink.getAbsolutePath(), symlink.exists());

        /*
         * Case: FLTM
         */

        FileUtilities.writeString(TARGET_FILE, ALT_FILE_CONTENT);

        /*
         * The mapper needs to respond to this change but invoking
         * dm.monitorFolder () will send a FileChangeSet with zero entries. This
         * is because 'dm' monitors the webdav directory of the cobrand and no
         * changes will have happened in that directoty.
         * 
         * An extra message needs to be sent to the mapper to allow it to do
         * something
         */
        /* calls listeners */
        e.reset();
        dm.monitorFolder();
        mapper.monitorLinkFolder();

        FileChangeSet expectedFcs = new FileChangeSet();
        expectedFcs.fileModified("start.html");

        assertEquals("The correct FileEvents were sent after the target of a link file was modified", expectedFcs, e
                .getChangeSet());
    }

    /**
     * Cases: DLA, DLM, DLD
     * 
     * This test checks links to directories.
     * 
     * Add a link file in the cobrand and check the expected symlink is created
     * and the content of the directory is as expected.
     * 
     * Modify the link file to point to a different directory and check the
     * symlink contains the new list of files.
     * 
     * Delete the link file and check the symlink is deleted.
     * 
     * In all cases checks the correct FileListenerEvents are generated.
     * 
     * CRUD = Create, update, delete
     */
    public void testCRUDLinkFileToDirectory() throws IOException, DirectoryMonitorException, DirectoryPairException,
            MapperException {

        /* Make channel being linked to */
        DirectoryMonitor masterDm = new DirectoryMonitor(m_MasterInputDir);
        Mapper masterMapper = getMasterMapper();

        masterDm.addFileChangeSetListener(masterMapper);

        /*
         * dir file-1.html file-2.html file-3.html sub-dir file-4.html
         * shared.html
         * 
         * 
         * alt-dir file-11.html file-12.html file-13.html file-14.html
         * alt-sub-dir file-15.html shared.html
         */

        String DIR_NAME = "dir";
        File dir = new File(m_MasterInputDir, DIR_NAME);
        if (!dir.mkdir())
            fail("Created directory: " + dir);
        for (int i = 1; i <= 3; i++) {
            File f = new File(dir, "file-" + i + ".html");
            FileUtilities.writeString(f, "dir/" + f.getName());
        }

        File subDir = new File(dir, "sub-dir");
        if (!subDir.mkdir())
            fail("Created directory: " + subDir);
        File file4 = new File(subDir, "file-4.html");
        FileUtilities.writeString(file4, "sub-dir/" + file4.getName());
        FileUtilities.writeString(new File(dir, "shared.html"), "dir/shared.html");

        String ALT_DIR_NAME = "alt-dir";
        File altDir = new File(m_MasterInputDir, ALT_DIR_NAME);
        if (!altDir.mkdir())
            fail("Created directory: " + altDir);
        for (int i = 11; i <= 14; i++) {
            File f = new File(altDir, "file-" + i + ".html");
            FileUtilities.writeString(f, "alt-dir/" + f.getName());
        }

        File altSubDir = new File(altDir, "alt-sub-dir");
        if (!altSubDir.mkdir())
            fail("Created directory: " + altSubDir);
        File file15 = new File(altSubDir, "file-15.html");
        FileUtilities.writeString(file15, "alt-sub-dir/" + file15.getName());
        FileUtilities.writeString(new File(altDir, "shared.html"), "alt-dir/shared.html");

        /*
         * This creates the symlinks to files and the directories we link to
         * with LinkFiles
         */
        masterDm.monitorFolder();

        /* Setup a mapper listening to changes in the cobrand WebDAV filesystem */
        DirectoryMonitor dm = new DirectoryMonitor(m_CobrandInputDir);
        Mapper mapper = getCobrandMapper();
        dm.addFileChangeSetListener(mapper);
        // mapper.setDebugLevel ( 99 ) ;
        mapper.setResolver(new MyResolver());

        /* Listen for all file events for later checking */
        EventLogger e = addEventLogger(mapper);

        /*
         * Case: DLA
         */

        /*
         * Add the link
         */
        // wps://mba/dir
        String LINK_CONTENT = "href=wps://" + MASTER_CONTEXT_PATH + "/" + dir.getName();
        String LINK_NAME = "files-1-n.link";
        String SYMLINK_NAME = "files-1-n";
        File linkFile = new File(m_CobrandInputDir, LINK_NAME);
        FileUtilities.writeString(linkFile, LINK_CONTENT);

        /* calls listeners */
        e.reset();
        dm.monitorFolder();
        mapper.monitorLinkFolder();

        /* Symbolic link should exist now */
        File symlink = new File(m_CobrandSymlinkDir, SYMLINK_NAME);
        assertTrue("symbolic link was created:" + symlink.getAbsolutePath(), symlink.exists());
        assertEquals("The content of the symbolic link was correct", normalize(dir.list()), normalize(symlink.list()));

        FileChangeSet expectedFcs = new FileChangeSet();
        expectedFcs.fileAdded("files-1-n");
        expectedFcs.fileAdded("files-1-n/file-1.html");
        expectedFcs.fileAdded("files-1-n/file-2.html");
        expectedFcs.fileAdded("files-1-n/file-3.html");
        expectedFcs.fileAdded("files-1-n/sub-dir");
        expectedFcs.fileAdded("files-1-n/sub-dir/file-4.html");
        expectedFcs.fileAdded("files-1-n/shared.html");

        assertEquals("The correct FileEvents were sent after linking in a directory", expectedFcs, e.getChangeSet());

        /*
         * Case: DLM
         */

        /*
         * modify link file, not content of directory linked to
         */
        String ALT_LINK_CONTENT = "href=wps://" + MASTER_CONTEXT_PATH + "/" + altDir.getName();
        FileUtilities.writeString(linkFile, ALT_LINK_CONTENT);

        /* calls listeners */
        e.reset();
        dm.monitorFolder();
        mapper.monitorLinkFolder();

        /* Symbolic link should still exist */
        assertTrue("symbolic link was still there:" + symlink.getAbsolutePath(), symlink.exists());
        assertEquals("The content of the symbolic link was correct", normalize(altDir.list()),
                normalize(symlink.list()));

        expectedFcs = new FileChangeSet();
        expectedFcs.fileDeleted("files-1-n/file-1.html");
        expectedFcs.fileDeleted("files-1-n/file-2.html");
        expectedFcs.fileDeleted("files-1-n/file-3.html");
        expectedFcs.fileDeleted("files-1-n/sub-dir");
        expectedFcs.fileDeleted("files-1-n/sub-dir/file-4.html");

        expectedFcs.fileAdded("files-1-n/file-11.html");
        expectedFcs.fileAdded("files-1-n/file-12.html");
        expectedFcs.fileAdded("files-1-n/file-13.html");
        expectedFcs.fileAdded("files-1-n/file-14.html");
        expectedFcs.fileAdded("files-1-n/alt-sub-dir");
        expectedFcs.fileAdded("files-1-n/alt-sub-dir/file-15.html");

        expectedFcs.fileModified("files-1-n/shared.html");

        assertEquals("The correct FileEvents were sent after changing the directory linked to", expectedFcs, e
                .getChangeSet());

        /*
         * Case: DLD
         */

        /*
         * delete it
         */
        deleteFile(linkFile);

        /* calls listeners */
        e.reset();
        dm.monitorFolder();
        mapper.monitorLinkFolder();

        /* Symbolic link should not exist now */
        assertTrue("symbolic link was deleted:" + symlink.getAbsolutePath(), !symlink.exists());

        expectedFcs = new FileChangeSet();
        expectedFcs.add("files-1-n", FileChangeSet.DELETED);
        expectedFcs.add("files-1-n/file-11.html", FileChangeSet.DELETED);
        expectedFcs.add("files-1-n/file-12.html", FileChangeSet.DELETED);
        expectedFcs.add("files-1-n/file-13.html", FileChangeSet.DELETED);
        expectedFcs.add("files-1-n/file-14.html", FileChangeSet.DELETED);
        expectedFcs.add("files-1-n/alt-sub-dir", FileChangeSet.DELETED);
        expectedFcs.add("files-1-n/alt-sub-dir/file-15.html", FileChangeSet.DELETED);
        expectedFcs.add("files-1-n/shared.html", FileChangeSet.DELETED);

        assertEquals("The correct FileEvents were sent after changing the directory linked to", expectedFcs, e
                .getChangeSet());

    }

    /**
     * Cases: DLTM-FA, DLTM-FM, DLTM-FD, DLTM-DA, DLTM-DD
     * 
     * DLTM-FA gains a file
     * 
     * DLTM-FM a file contained within the linked directory is modified
     * 
     * DLTM-FD loses a file
     * 
     * DLTM-DA gains a sub-directory DLTM-DD loses a sub-directory
     * 
     * 
     */
    public void testModifyTargetDirectory() throws IOException, DirectoryMonitorException, DirectoryPairException,
            MapperException {

        /* Make channel being linked to */
        DirectoryMonitor masterDm = new DirectoryMonitor(m_MasterInputDir);
        Mapper masterMapper = getMasterMapper();

        masterDm.addFileChangeSetListener(masterMapper);

        /*
         * dir file-1.html file-2.html file-3.html sub-dir file-4.html sub-dir-2
         */

        String DIR_NAME = "dir";
        File dir = new File(m_MasterInputDir, DIR_NAME);
        if (!dir.mkdir())
            fail("Created directory: " + dir);
        for (int i = 1; i <= 3; i++) {
            File f = new File(dir, "file-" + i + ".html");
            FileUtilities.writeString(f, "dir/" + f.getName());
        }

        File subDir = new File(dir, "sub-dir");
        if (!subDir.mkdir())
            fail("Created directory: " + subDir);
        File file4 = new File(subDir, "file-4.html");
        FileUtilities.writeString(file4, "sub-dir/" + file4.getName());
        File subDir2 = new File(dir, "sub-dir-2");
        if (!subDir2.mkdir())
            fail("Created directory: " + subDir2);

        /*
         * This creates the symlinks to files and the directories we link to
         * with LinkFiles
         */
        masterDm.monitorFolder();

        /* Setup a mapper listening to changes in the cobrand WebDAV filesystem */
        DirectoryMonitor dm = new DirectoryMonitor(m_CobrandInputDir);
        Mapper mapper = getCobrandMapper();
        dm.addFileChangeSetListener(mapper);
        // mapper.setDebugLevel ( 99 ) ;
        mapper.setResolver(new MyResolver());

        /* Listen for all file events for later checking */
        EventLogger e = addEventLogger(mapper);

        /*
         * Add the link
         */
        // wps://mba/dir
        String LINK_CONTENT = "href=wps://" + MASTER_CONTEXT_PATH + "/" + dir.getName();
        String LINK_NAME = "linked-dir.link";
        String SYMLINK_NAME = "linked-dir";
        File linkFile = new File(m_CobrandInputDir, LINK_NAME);
        FileUtilities.writeString(linkFile, LINK_CONTENT);

        /* calls listeners */
        e.reset();
        dm.monitorFolder();
        mapper.monitorLinkFolder();

        FileChangeSet expectedFcs = new FileChangeSet();
        expectedFcs.fileAdded("linked-dir");
        expectedFcs.fileAdded("linked-dir/file-1.html");
        expectedFcs.fileAdded("linked-dir/file-2.html");
        expectedFcs.fileAdded("linked-dir/file-3.html");
        expectedFcs.fileAdded("linked-dir/sub-dir");
        expectedFcs.fileAdded("linked-dir/sub-dir/file-4.html");
        expectedFcs.fileAdded("linked-dir/sub-dir-2");

        assertEquals("The correct FileEvents were sent after linking in a directory", expectedFcs, e.getChangeSet());

        /*
         * Cases: DLTM-FA gains a file DLTM-FM a file contained within the
         * linked directory is modified DLTM-FD loses a file DLTM-DA gains a
         * sub-directory DLTM-DD loses a sub-directory
         */

        /* DLTM-FA */
        File gainedFile = new File(dir, "gained.html");
        FileUtilities.writeString(gainedFile, "dir/" + gainedFile.getName());

        /* DLTM-FM */
        File modifiedFile = new File(dir, "file-2.html");
        FileUtilities.writeString(modifiedFile, "modified-content-plus-padding-to-make-length-greater");

        /* DLTM-FD */
        File deletedFile = new File(dir, "file-1.html");
        if (!deletedFile.delete())
            fail("Deleted file: " + deletedFile);

        /* DLTM-DA */
        File subDirAdded = new File(dir, "sub-dir-added");
        if (!subDirAdded.mkdir())
            fail("Created directory: " + subDirAdded);

        /* DLTM-DD */
        if (!subDir2.delete())
            fail("Deleted directory: " + subDir2);

        /* Extra: add and delete a file within a sub-directory */
        File addedInSubDir = new File(subDir, "added.html");
        FileUtilities.writeString(addedInSubDir, "added in sub-directory");

        File deletedFromSubDir = new File(subDir, "file-4.html");
        if (!deletedFromSubDir.delete())
            fail("Deleted file: " + deletedFromSubDir);

        /* This creates the symlinks to files and the directories created above */
        masterDm.monitorFolder();

        /* calls listeners */
        e.reset();
        dm.monitorFolder();
        mapper.monitorLinkFolder();

        expectedFcs = new FileChangeSet();
        expectedFcs.fileAdded("linked-dir/gained.html");
        expectedFcs.fileModified("linked-dir/file-2.html");
        expectedFcs.fileDeleted("linked-dir/file-1.html");
        expectedFcs.fileAdded("linked-dir/sub-dir-added");
        expectedFcs.fileDeleted("linked-dir/sub-dir-2");
        expectedFcs.fileAdded("linked-dir/sub-dir/added.html");
        expectedFcs.fileDeleted("linked-dir/sub-dir/file-4.html");

        assertEquals("The correct FileEvents were sent after modifying a directory", expectedFcs, e.getChangeSet());
    }

    /**
     * When the file being linked to by a link file is deleted the result of the
     * link file should be deleted.
     * 
     * Case: FLTD-PRE. The file linked to is deleted and the cobrand channel is
     * updated first. This means the symlink in master is still present but the
     * target file is not.
     * 
     */
    public void testDeleteTargetFilePreMasterUpdate() throws Exception {

        FLTDObjects OBJECTS = setupFLTD();

        /*
         * Case: FLTD-PRE
         */

        assertTrue(OBJECTS.TARGET_FILE.delete());

        /*
         * The mapper needs to respond to this change but invoking
         * cobrandDm.monitorFolder () will send a FileChangeSet with zero
         * entries. This is because 'cobrandDm' monitors the input directory of
         * the cobrand and no changes will have happened in that directory.
         * 
         * An extra message needs to be sent from the mapper to allow the
         * JSPBuilder that listens to it to do something.
         * 
         * When the target of the link has been deleted
         * mapper.monitorLinkFolder() used to return a FileChangeSet with one
         * MODIFIED entry for the symlink. This was changed to TARGET_DELETED
         * (and when the target reappears TARGET_ADDED).
         */
        OBJECTS.e.reset();
        /*
         * This sends events to cobrandMapper, which responds by modifying the
         * content of the symlink directory.
         */
        OBJECTS.cobrandDm.monitorFolder();
        /* This sends events to EventLogger. */
        OBJECTS.cobrandMapper.monitorLinkFolder();

        FileChangeSet expectedFcs = new FileChangeSet();
        expectedFcs.targetFileDeleted("start.html");

        /*
         * Nothing more than this can be tested because the deletion of the JSP
         * that would result from the symlink changing to link to a non-existent
         * file is done by JSPBuilder.
         */
        assertEquals("The correct FileEvents were sent after the target of a link file was deleted", expectedFcs,
                OBJECTS.e.getChangeSet());

    }

    /**
     * When the file being linked to by a link file is deleted and the link to
     * this file is deleted (by virtue of the master channel being updated
     * first), the result of the link file should be deleted.
     * 
     * Test the case when the target file is deleted as above but the master
     * channel is updated first (with masterDm.monitorFolder()) thus removing
     * the symlink in master/symlink. The link in cobrand/symlink is again a
     * dangling symlink.
     * 
     * Case: FLTD-POST. The file linked to is deleted and the master channel is
     * updated first. This means the symlink and the target file in master are
     * missing.
     * 
     */
    public void testDeleteTargetFilePostMasterUpdate() throws Exception {

        FLTDObjects OBJECTS = setupFLTD();

        /*
         * Case: FLTD-POST
         */

        assertTrue(OBJECTS.TARGET_FILE.delete());
        /* Delete symlink in symlink/master */
        assertEquals(1, OBJECTS.masterDm.monitorFolder().getDeleted().size());

        /*
         * The mapper needs to respond to this change but invoking
         * cobrandDm.monitorFolder () will send a FileChangeSet with zero
         * entries. This is because 'cobrandDm' monitors the input directory of
         * the cobrand and no changes will have happened in that directory.
         * 
         * An extra message needs to be sent from the mapper to allow the
         * JSPBuilder that listens to it to do something.
         * 
         * When the target of the link has been deleted
         * mapper.monitorLinkFolder() used to return a FileChangeSet with one
         * MODIFIED entry for the symlink. This was changed to TARGET_DELETED
         * (and when the target reappears TARGET_ADDED).
         */
        OBJECTS.e.reset();
        /*
         * This sends events to cobrandMapper, which responds by modifying the
         * content of the symlink directory.
         */
        OBJECTS.cobrandDm.monitorFolder();
        /* This sends events to EventLogger. */
        OBJECTS.cobrandMapper.monitorLinkFolder();

        FileChangeSet expectedFcs = new FileChangeSet();
        expectedFcs.targetFileDeleted("start.html");

        /*
         * Nothing more than this can be tested because the deletion of the JSP
         * that would result from the symlink changing to link to a non-existent
         * file is done by JSPBuilder.
         */
        assertEquals("The correct FileEvents were sent after the target of a link file was deleted", expectedFcs,
                OBJECTS.e.getChangeSet());
    }

    /**
     * Ensure that the setup for FLTD-PRE and FLTD-POST is identical.
     */
    private FLTDObjects setupFLTD() throws Exception {
        FLTDObjects OBJECTS = new FLTDObjects();
        /* Configure site being linked to */
        OBJECTS.masterDm = new DirectoryMonitor(m_MasterInputDir);
        Mapper masterMapper = getMasterMapper();
        OBJECTS.masterDm.addFileChangeSetListener(masterMapper);

        String FILE_CONTENT = "content";
        String FILE_NAME = "index.html";
        OBJECTS.TARGET_FILE = new File(m_MasterInputDir, FILE_NAME);
        FileUtilities.writeString(OBJECTS.TARGET_FILE, FILE_CONTENT);

        /* This creates the symlinks we link to with LinkFiles */
        OBJECTS.masterDm.monitorFolder();
        assertFileExists("The symlink for <master>/index.html was created after monitoring the master input directory",
                new File(m_MasterSymlinkDir, FILE_NAME));

        /* Setup a mapper listening to changes in the cobrand input directory */
        OBJECTS.cobrandDm = new DirectoryMonitor(m_CobrandInputDir);
        OBJECTS.cobrandMapper = getCobrandMapper();
        OBJECTS.cobrandDm.addFileChangeSetListener(OBJECTS.cobrandMapper);
        // mapper.setDebugLevel ( 99 ) ;
        OBJECTS.cobrandMapper.setResolver(new MyResolver());

        /*
         * Case: FLA, repeated to set up the fixture for FLTD.
         */

        /* Listen for all file events for later checking */
        OBJECTS.e = addEventLogger(OBJECTS.cobrandMapper);

        /*
         * Add the link
         */
        // wps://mba/index.html
        String LINK_CONTENT = "href=wps://" + MASTER_CONTEXT_PATH + "/" + FILE_NAME;
        String LINK_NAME = "start.html.link";
        String SYMLINK_NAME = "start.html";
        File linkFile = new File(m_CobrandInputDir, LINK_NAME);
        FileUtilities.writeString(linkFile, LINK_CONTENT);

        OBJECTS.e.reset();
        /*
         * This sends events to cobrandMapper, which responds by modifying the
         * content of the symlink directory.
         */
        OBJECTS.cobrandDm.monitorFolder();
        /* This sends events to EventLogger. */
        OBJECTS.cobrandMapper.monitorLinkFolder();

        FileChangeSet expectedFcs = new FileChangeSet();
        expectedFcs.fileAdded("start.html");
        assertEquals("The correct FileEvents were sent after a link file that links to an existing file was added",
                expectedFcs, OBJECTS.e.getChangeSet());

        File symlink = new File(m_CobrandSymlinkDir, SYMLINK_NAME);
        assertFileExists("symbolic link was created", symlink);

        return OBJECTS;
    }

    private static class FLTDObjects {
        File TARGET_FILE;

        DirectoryMonitor masterDm;

        DirectoryMonitor cobrandDm;

        Mapper cobrandMapper;

        EventLogger e;
    }

    /**
     * Case: FLTA. The file linked to is added
     */
    public void testAddTargetFile() throws Exception {

        /*
         * PDL
         * 
         * Add a link file to a non-existent file.
         * 
         * Confirm the correct event is sent.
         * 
         * Add the target file.
         * 
         * Confirm the correct event is sent.
         */

        /* Setup a mapper listening to changes in the cobrand input directory */
        DirectoryMonitor cobrandDm = new DirectoryMonitor(m_CobrandInputDir);
        Mapper cobrandMapper = getCobrandMapper();
        cobrandDm.addFileChangeSetListener(cobrandMapper);
        // mapper.setDebugLevel ( 99 ) ;
        cobrandMapper.setResolver(new MyResolver());

        /* Listen for all file events for later checking */
        EventLogger e = addEventLogger(cobrandMapper);

        /*
         * Add the link
         */
        // wps://mba/index.html
        String FILE_NAME = "index.html";
        String LINK_CONTENT = "href=wps://" + MASTER_CONTEXT_PATH + "/" + FILE_NAME;
        String LINK_NAME = "start.html.link";
        String SYMLINK_NAME = "start.html";
        File linkFile = new File(m_CobrandInputDir, LINK_NAME);
        FileUtilities.writeString(linkFile, LINK_CONTENT);

        e.reset();
        /*
         * This sends events to cobrandMapper, which responds by modifying the
         * content of the symlink directory.
         */
        cobrandDm.monitorFolder();
        /* This sends events to EventLogger. */
        cobrandMapper.monitorLinkFolder();

        /*
         * fileAdded is the correct event to expect because JSPBuilder responds
         * to the addition of a dangling link by establishing the the file is
         * not a file with File.isFile and returning from fileAddedInternal
         * before doing any work. This means when targetFileAdded is fired
         * JSPBuilder will be working with a blank canvas so there will not be
         * any obstacles to a correct response. fileWithMissingTargetFileAdded
         * makes less sense.
         */
        FileChangeSet expectedFcs = new FileChangeSet();
        expectedFcs.fileAdded(SYMLINK_NAME);
        assertEquals("The correct FileEvents were sent after a link file that links to an non-existent file was added",
                expectedFcs, e.getChangeSet());

        File symlink = new File(m_CobrandSymlinkDir, SYMLINK_NAME);
        assertTrue("Dangling symlink was created", FileUtilities.isSymbolicLinkWithMissingTarget(symlink));

        /*
         * Now add the target file, update the master channel, then update the
         * cobrand.
         */

        /* Configure site being linked to */
        DirectoryMonitor masterDm = new DirectoryMonitor(m_MasterInputDir);
        Mapper masterMapper = getMasterMapper();
        masterDm.addFileChangeSetListener(masterMapper);

        String FILE_CONTENT = "content";
        File TARGET_FILE = new File(m_MasterInputDir, FILE_NAME);
        FileUtilities.writeString(TARGET_FILE, FILE_CONTENT);

        /* This creates the symlinks we link to with LinkFiles */
        masterDm.monitorFolder();
        assertFileExists("The symlink for <master>/index.html was created after monitoring the master input directory",
                new File(m_MasterSymlinkDir, FILE_NAME));

        /*
         * This path should now be valid symlink/cobrand/start.html ->
         * symlink/master/index.html -> input/master/index.html
         */
        assertEquals("The cobrand link linked to the correct target", FILE_CONTENT, FileUtilities.readFile(symlink));

        e.reset();
        /*
         * This sends events to cobrandMapper, which responds by modifying the
         * content of the symlink directory.
         */
        cobrandDm.monitorFolder();
        /* This sends events to EventLogger. */
        cobrandMapper.monitorLinkFolder();

        expectedFcs = new FileChangeSet();
        expectedFcs.targetFileAdded(SYMLINK_NAME);
        assertEquals("The correct FileEvents were sent after a link file gained it's previously missing target",
                expectedFcs, e.getChangeSet());
    }

    private final boolean DISABLE_UNTIL_POST_NAVDEV_NTROPA_INSTALL = true;
    
    /**
     * Case: DLTD. The directory linked to is deleted.
     */
    public void testDeleteTargetDirectory() {
        if(DISABLE_UNTIL_POST_NAVDEV_NTROPA_INSTALL)
            return;
        fail("This might need work. link/cobrand/dir-1 can be a symlink to link/master/dir-1 "
                + "and the current inconsistency is the non-deletion of output/cobrand/dir-1 when the target dir-1 is deleted");
        fail("Not written");
    }

    /**
     * Case: DLTA. The directory linked to is added
     */
    public void testAddTargetDirectory() {
        if(DISABLE_UNTIL_POST_NAVDEV_NTROPA_INSTALL)
            return;
        fail("Not written; maybe omit because it might require a set of dangling link files to be available but check that because it seems to work already.");
    }

    public void testJavaCommentCorrected() {
        if(DISABLE_UNTIL_POST_NAVDEV_NTROPA_INSTALL)
            return;
        fail("Correct the comment about cases DLTD, DLTA being unsupported if support has been added.");
    }

    public void testReminder() {
        if(DISABLE_UNTIL_POST_NAVDEV_NTROPA_INSTALL)
            return;
        fail("What event should mapper.monitorLinkFolder send when "
                + "a) A link file to a non-existent file is deleted? Maybe nothing, or maybe DANGLING_LINK_ADDED, DANGLING_LINK_DELETED.");
    }

    /**
     * This test ensures a link is created correctly when a link file
     * transitions from being invalid to valid.
     * 
     * Before this test was completing successfully the addition of a link file
     * with this content
     * 
     * jref=wps://mba/index.html
     * 
     * would (correctly) not create a symbolic link because the 'href' property
     * is missing. Correcting the link file to
     * 
     * href=wps://mba/index.html
     * 
     * then failed to create the symbolic link because the Mapper was expecting
     * to replace an existing symbolic link (which had never been created).
     * 
     * This test ensures correcting the link file is enough to create the link.
     */
    public void testCorrectionOfBadLinkFileContent() throws IOException, DirectoryMonitorException,
            DirectoryPairException, MapperException {

        /* Make channel being linked to */
        DirectoryMonitor masterDm = new DirectoryMonitor(m_MasterInputDir);
        Mapper masterMapper = getMasterMapper();

        masterDm.addFileChangeSetListener(masterMapper);

        String FILE_CONTENT = "qwest";
        String FILE_NAME = "index.html";
        FileUtilities.writeString(new File(m_MasterInputDir, FILE_NAME), FILE_CONTENT);

        /* This creates the symlinks we link to with LinkFiles */
        masterDm.monitorFolder();

        /* Setup a mapper listening to changes in the cobrand WebDAV filesystem */
        DirectoryMonitor dm = new DirectoryMonitor(m_CobrandInputDir);
        Mapper mapper = getCobrandMapper();
        dm.addFileChangeSetListener(mapper);
        // mapper.setDebugLevel ( 99 ) ;
        mapper.setResolver(new MyResolver());

        /* Listen for all file events for later checking */
        EventLogger e = addEventLogger(mapper);

        /*
         * Add the bad link
         */
        // wps://mba/index.html
        String BAD_LINK_CONTENT = "X=wps://" + MASTER_CONTEXT_PATH + "/" + FILE_NAME;
        String LINK_NAME = "start.html.link";
        String SYMLINK_NAME = "start.html";
        File linkFile = new File(m_CobrandInputDir, LINK_NAME);
        FileUtilities.writeString(linkFile, BAD_LINK_CONTENT);

        /* calls listeners */
        e.reset();
        dm.monitorFolder();
        mapper.monitorLinkFolder();

        /* Symbolic link should not exist now */
        File symlink = new File(m_CobrandSymlinkDir, SYMLINK_NAME);
        assertTrue("symbolic link was not created:" + symlink.getAbsolutePath(), !symlink.exists());

        FileChangeSet expectedFcs = new FileChangeSet();

        assertEquals("The correct FileEvents were sent after a bad link file was added", expectedFcs, e.getChangeSet());

        /*
         * Correct the bad link
         */
        // wps://mba/index.html
        String GOOD_LINK_CONTENT = "href=wps://" + MASTER_CONTEXT_PATH + "/" + FILE_NAME;
        FileUtilities.writeString(linkFile, GOOD_LINK_CONTENT);

        /* calls listeners */
        e.reset();
        dm.monitorFolder();
        mapper.monitorLinkFolder();

        /* Symbolic link should exist now */
        assertTrue("symbolic link was created:" + symlink.getAbsolutePath(), symlink.exists());
        assertEquals("The content of the symbolic link was correct", FILE_CONTENT, FileUtilities.readFile(symlink));

        expectedFcs = new FileChangeSet();
        expectedFcs.fileAdded("start.html");

        assertEquals("The correct FileEvents were sent after a bad link file was corrected", expectedFcs, e
                .getChangeSet());

    }

    /**
     * After much struggling to think through the algorithm to get this method
     * right based on the table in the JavaDoc for the method I added this
     * method, which I should have done at the beginning.
     * 
     * This table shows the the symbolic links that would be created. Read it as
     * "what directory results from making a link with the name 'proposed' that
     * links to 'existing'". 'H' is always a name that does not already exist.
     * 
     * <pre>
     * 
     *                          |proposed
     *                          |
     *                 existing | A       AB      ABC     ABCD    AG      AH      ABH
     *                 ---------|---------------------------------------------------------
     *                 A        | AA      ABA     ABCA    ABCDA   AGA     AH      ABH 
     *                 AB       | AB      ABB     ABCB    ABCDB   AGB     AH      ABH
     *                 ABC      | AC      ABC     ABCC    ABCDC   AGC     AH      ABH
     *                 ABCD     | AD      ABD     ABCD    ABCDD   AGD     AH      ABH
     *                 AG       | AGA     ABG     ABCG    ABCDG   AGG     AH      ABH
     *                 
     *       This table shows the combinations that are not allowed and why
     *       
     *         . : the link is allowed
     *         S : directory is either directly or indirectly contained within itself
     *         R : directory replaced itself in the same parent directory
     *         
     *                          |proposed
     *                          |
     *                 existing | A       AB      ABC     ABCD    AG      AH      ABH
     *                 ---------|---------------------------------------------------------
     *                 A        | S       S       S       S       S       S       S 
     *                 AB       | R       S       S       S       .       .       S
     *                 ABC      | .       R       S       S       .       .       .
     *                 ABCD     | .       .       R       S       .       .       .
     *                 AG       | R       .       .       .       S       .       .
     *                 
     * </pre>
     * 
     */
    public void testSymbolicLinkWouldBeSafe() throws DirectoryMonitorException, DirectoryPairException, MapperException {

        /*
         * Create the directories that exist in the above table
         */
        File A = new File(m_MasterSymlinkDir, "A");
        mkDirectory(A);
        File AB = new File(A, "B");
        mkDirectory(AB);
        File ABC = new File(AB, "C");
        mkDirectory(ABC);
        File ABCD = new File(ABC, "D");
        mkDirectory(ABCD);
        File AG = new File(A, "G");
        mkDirectory(AG);

        /* proposed that do no exist */
        File AH = new File(A, "H");
        File ABH = new File(AB, "H");

        InputAndExpected tests[] = new InputAndExpected[] {
        /*
         * 1
         */
        new InputAndExpected(A, A, false),//
                new InputAndExpected(A, AB, false),//
                new InputAndExpected(A, ABC, false),//
                new InputAndExpected(A, ABCD, false),//
                new InputAndExpected(A, AG, false),//
                new InputAndExpected(A, AH, false),//
                new InputAndExpected(A, ABH, false),
                /*
                 * 2
                 */
                new InputAndExpected(AB, A, false),//
                new InputAndExpected(AB, AB, false),//
                new InputAndExpected(AB, ABC, false),//
                new InputAndExpected(AB, ABCD, false),//
                new InputAndExpected(AB, AG, true),//
                new InputAndExpected(AB, AH, true),//
                new InputAndExpected(AB, ABH, false),//
                /*
                 * 3
                 */
                new InputAndExpected(ABC, A, true),//
                new InputAndExpected(ABC, AB, false),//
                new InputAndExpected(ABC, ABC, false),//
                new InputAndExpected(ABC, ABCD, false),//
                new InputAndExpected(ABC, AG, true),//
                new InputAndExpected(ABC, AH, true),//
                new InputAndExpected(ABC, ABH, true),//
                /*
                 * 4
                 */
                new InputAndExpected(ABCD, A, true),//
                new InputAndExpected(ABCD, AB, true),//
                new InputAndExpected(ABCD, ABC, false),//
                new InputAndExpected(ABCD, ABCD, false),//
                new InputAndExpected(ABCD, AG, true),//
                new InputAndExpected(ABCD, AH, true),//
                new InputAndExpected(ABCD, ABH, true),//
                /*
                 * 5
                 */
                new InputAndExpected(AG, A, false),//
                new InputAndExpected(AG, AB, true),//
                new InputAndExpected(AG, ABC, true),//
                new InputAndExpected(AG, ABCD, true),//
                new InputAndExpected(AG, AG, false),//
                new InputAndExpected(AG, AH, true),//
                new InputAndExpected(AG, ABH, true),//

        };

        Mapper mapper = getMasterMapper();

        int rootPrefixLength = m_MasterSymlinkDir.getAbsolutePath().length();
        for (int i = 0; i < tests.length; i++) {
            InputAndExpected test = tests[i];
            String targetName = test.targetName.getAbsolutePath().substring(rootPrefixLength);
            String proposedLinkName = test.proposedLinkName.getAbsolutePath().substring(rootPrefixLength);

            assertEquals(targetName + ", " + proposedLinkName, test.expected, mapper.symbolicLinkWouldBeSafe(
                    test.targetName, test.proposedLinkName));
        }
    }

    private static class InputAndExpected {
        final File targetName;

        final File proposedLinkName;

        final boolean expected;

        InputAndExpected(File targetName, File proposedLinkName, boolean expected) {
            this.targetName = targetName;
            this.proposedLinkName = proposedLinkName;
            this.expected = expected;
        }

    }

    /**
     * This test examines the behaviour when a link file that makes a link to a
     * directory is added in the same directory as the directory it links to and
     * it has the same name.
     * 
     * This test aims to expose a bug encountered at least twice in May/Jun 2005
     * by Janek/Dru and Vishal.
     * 
     * I manually recreated something like the problem by adding these files and
     * directories to 'intl-yahoo-india'
     * 
     * /request-info/tabs.html
     * 
     * 
     * and then adding a link file 'request-info.link' with this content
     * 
     * href=wps://intl-yahoo-india/request-info
     * 
     * to the same directory as the target of the link. The directory then
     * contained
     * 
     * /request-info/tabs.html /request-info.link
     * 
     * This cause a problem in the running WPS. These are the log entries
     * 
     * Failed to create directory: /request-info/request-info Failed to create
     * directory: /request-info/request-info/request-info Directory or file
     * already existed. Continued processing anyway: /request-info/request-info/
     * tabs.html Failed to create directory:
     * /request-info/request-info/request-info /request-info
     * 
     * the paths get deeper with each subsequent entry until finally this is
     * seen
     * 
     * Source was not a file: /request-info x 41/tabs.html
     * 
     * The method invocation that creates the directory within itself is
     * 
     * [junit] Mapper:processLinkFileEvent, FileUtilities.makeSymbolicLink (
     * /temp/xl-MapperTest/sym/mba/request-info,
     * /temp/xl-MapperTest/sym/mba/request-info )
     * 
     * @throws DirectoryMonitorException
     * @throws MapperException
     * @throws DirectoryPairException
     * @throws IOException
     * 
     */
    public void testAdditionOfSecondSourceForSameTargetDirectory() throws DirectoryMonitorException,
            DirectoryPairException, MapperException, IOException {

        /*
         * PDL
         * 
         * Create /request-info/tabs.html Check sym file system
         * 
         * Add link request-info.link with request-info as the target Check sym
         * file system is the same.
         * 
         */

        /*
         * Create a DirectoryMonitor object and add a Mapper as a listener.
         */

        DirectoryMonitor dm = new DirectoryMonitor(m_MasterInputDir);
        Mapper mapper = getMasterMapper();
        mapper.setResolver(new MyResolver());

        dm.addFileChangeSetListener(mapper);

        /*
         * Create initial set up
         */
        File d = new File(m_MasterInputDir, "request-info");
        mkDirectory(d);
        File f = new File(d, "tabs.html");
        createFile(f);

        /* calls listeners */
        dm.monitorFolder();

        /* Directory and symbolic link should exist now */
        verifySymLinkDirForSecondSourceTest();

        /*
         * Now add a link that caused a problem in the manual test
         */
        String SYMLINK_NAME = "request-info.link";

        File linkFile = new File(m_MasterInputDir, SYMLINK_NAME);
        // wps://mba/request-info
        String LINK_CONTENT = "href=wps://" + MASTER_CONTEXT_PATH + "/" + "request-info";
        FileUtilities.writeString(linkFile, LINK_CONTENT);

        /* calls listeners */
        dm.monitorFolder();
        // mapper.monitorLinks();

        /*
         * Directory and symbolic link should not be any different. In
         * particular the directory 'request-info' should not contain
         * 'request-info'
         */
        verifySymLinkDirForSecondSourceTest();

        // fail("stopper");
    }

    private void verifySymLinkDirForSecondSourceTest() {
        File requestInfoDir = new File(m_MasterSymlinkDir, "request-info");
        assertTrue("Directory exists", requestInfoDir.exists());
        assertTrue("Directory was a directory", requestInfoDir.isDirectory());

        /* Check tabs.html is the only file within */
        String containedItems[] = requestInfoDir.list();
        assertEquals("'tabs.html' was the only item in the request-info directory", Arrays
                .asList(new String[] { "tabs.html" }), Arrays.asList(containedItems));

        File f = new File(requestInfoDir, "tabs.html");
        assertTrue("File exists", f.exists());
        assertTrue("File was a file a directory", f.isFile());

    }

    /* Accumulate FileListenerEvents with this class */
    private class EventLogger implements FileListener {

        FileChangeSet fcs = new FileChangeSet();

        public void fileDeleted(FileListenerEvent e) {
            fcs.add(e.getLocation(), FileChangeSet.DELETED);
        }

        public void fileAdded(FileListenerEvent e) {
            fcs.add(e.getLocation(), FileChangeSet.ADDED);
        }

        public void fileModified(FileListenerEvent e) {
            fcs.add(e.getLocation(), FileChangeSet.MODIFIED);
        }

        public void targetFileAdded(FileListenerEvent e) {
            fcs.add(e.getLocation(), FileChangeSet.TARGET_ADDED);
        }

        public void targetFileDeleted(FileListenerEvent e) {
            fcs.add(e.getLocation(), FileChangeSet.TARGET_DELETED);
        }

        FileChangeSet getChangeSet() {
            return fcs;
        }

        void reset() {
            fcs = new FileChangeSet();
        }

    }

    private EventLogger addEventLogger(Mapper mapper) {

        /* Listen for file events */
        EventLogger e = new EventLogger();

        /* Get all events */
        mapper.addFileListener(e, new FilePredicate() {
            public boolean accept(File f) {
                return true;
            }

            public boolean accept(String f) {
                return true;
            }
        });

        return e;
    }

    /* test creating a file in each of the special directories works as well */

    /*
     * Common methods
     */

    private Mapper getMasterMapper() throws DirectoryMonitorException, DirectoryPairException, MapperException {

        ContextPath cp = new ContextPath(MASTER_CONTEXT_PATH);
        DirectoryPair dp = new DirectoryPair(m_MasterInputDir, m_MasterSymlinkDir);

        return new Mapper(cp, dp);
    }

    private Mapper getCobrandMapper() throws DirectoryMonitorException, DirectoryPairException, MapperException {

        ContextPath cp = new ContextPath(COBRAND_CONTEXT_PATH);
        DirectoryPair dp = new DirectoryPair(m_CobrandInputDir, m_CobrandSymlinkDir);

        return new Mapper(cp, dp);
    }

    protected void createFile(File f) {
        try {
            if (!f.createNewFile())
                fail("Failed to create test fixture: create file: " + f);
        } catch (IOException e) {
            fail("Failed to create test fixture: create file: " + e);
        }

    }

    protected void mkDirectory(File f) {
        if (!f.mkdirs())
            fail("Failed to create test fixture: create dir(s): " + f);
    }

    protected void deleteFile(File f) {
        if (!f.delete())
            fail("Failed to create test fixture: delete file: " + f);
    }

    private class MyResolver implements Resolver {

        public File resolve(LinkFile linkFile) {

            if (!"mba".equals(linkFile.getHost()))
                throw new IllegalArgumentException("Can not resolve " + linkFile);

            return new File(m_MasterSymlinkDir, linkFile.getPath());
        }

    }

    /**
     * File.list returns files in non-deterministic order (at least to the
     * developer)
     */
    private List normalize(String[] strings) {

        List n = Arrays.asList(strings);
        Collections.sort(n);

        return n;
    }

    private void assertFileExists(String assertion, File f) {
        assertTrue(assertion + " [" + f.getAbsolutePath() + "]", f.exists());
    }
}
