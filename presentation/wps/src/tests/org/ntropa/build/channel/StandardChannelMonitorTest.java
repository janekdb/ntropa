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
 * StandardChannelMonitorTest.java
 *
 * Created on 23 October 2001, 12:36
 */

package tests.org.ntropa.build.channel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.Constants;
import org.ntropa.build.ContextPath;
import org.ntropa.build.ContextPathException;
import org.ntropa.build.DirectoryMonitor;
import org.ntropa.build.DirectoryMonitorException;
import org.ntropa.build.FileChangeSet;
import org.ntropa.build.channel.ChannelMonitor;
import org.ntropa.build.channel.ChannelMonitorException;
import org.ntropa.build.channel.StandardChannelMonitor;
import org.ntropa.build.jsp.Template;
import org.ntropa.build.mapper.LinkFile;
import org.ntropa.build.mapper.Mapper;
import org.ntropa.build.mapper.Resolver;
import org.ntropa.utility.FilePredicate;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.NtropaTestWorkDirHelper;

/**
 * 
 * @author jdb
 * @version $Id: StandardChannelMonitorTest.java,v 1.21 2003/10/02 10:51:50 jdb
 *          Exp $
 */
public class StandardChannelMonitorTest extends TestCase {

    private String TOP_FOLDER ;

    private File _topFolder;

    private File _davDir;

    private File _symDir;

    private File _jspDir;

    private File _fixtureRoot;

    public StandardChannelMonitorTest(String testName) {
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

        TestSuite suite = new TestSuite(StandardChannelMonitorTest.class);
        return suite;
    }

    protected void setUp() throws Exception, IOException {

        TOP_FOLDER = new NtropaTestWorkDirHelper().getWorkDir() + "/tests.org.ntropa.build.channel.StandardChannelMonitorTest";

        _topFolder = new File(TOP_FOLDER);
        if (!_topFolder.mkdirs())
            throw new Exception("Failed to create folder or folder already existed: " + TOP_FOLDER);

        /*
         * the length of this directory name is longer than 3 in order to flush
         * a bug with path prefix removal. When the paths were all the same
         * length the bug was hidden.
         */
        _davDir = new File(_topFolder, "webdav");
        if (!_davDir.mkdir())
            throw new Exception("Failed to create folder or folder already existed: " + _davDir);

        _symDir = new File(_topFolder, "sym");
        if (!_symDir.mkdir())
            throw new Exception("Failed to create folder or folder already existed: " + _symDir);

        _jspDir = new File(_topFolder, "jsp");
        if (!_jspDir.mkdir())
            throw new Exception("Failed to create folder or folder already existed: " + _jspDir);

        String zipPath = System.getProperty("standardchannelmonitor.zippath");

        if (zipPath == null)
            fail("Failed to get path to zip file.");

        File zipFile = new File(zipPath);

        if (!zipFile.exists())
            fail("Zip file does not exist: " + zipPath);

        // Extract the test directory into the test root.
        if (!FileUtilities.extractZip(zipFile, _topFolder))
            fail("Failed to extract zip file.");

        _fixtureRoot = new File(_topFolder, "standardchannelmonitorfs");

        if (!_fixtureRoot.isDirectory())
            fail("Problem with fixture: standardchannelmonitorfs was not a directory: " + _fixtureRoot);

        /* Turn off Tomcat workaround to make testing easier */
        Template.setTomcat4BugWorkaround(false);

        /* Crappy hack, see class for details */
        Mapper.setDefaultDebugLevel(0);

    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() throws Exception {

        FileUtilities.killDirectory(TOP_FOLDER);
    }

    // private FileChangeSet getBaseExpectedFCS() {
    // FileChangeSet result = new FileChangeSet();
    // result.add(new File(_jspDir, BUILD_STATUS), FileChangeSet.ADDED);
    // return result;
    // }

    /**
     * When the writing of the build status file was added this was added to
     * allow the existing tests to be modified in a straightforward fashion.
     * 
     * Everything other than the build status file is accepted.
     */
    private FilePredicate getBuildStatusExcluderFilter() {
        return new FilePredicate() {

            public boolean accept(File file) {
                return !BUILD_STATUS.equals(file.getName());
            }

            public boolean accept(String file) {
                return accept(new File(file));
            }

        };
    }

    /**
     * For each different type of file or directory test the basic behaviour of
     * the monitor.
     */
    public void testSingleFileChanges() throws DirectoryMonitorException {

        ChannelMonitor cm = getStandardMonitor();

        /* track the changes through a DirectoryMonitor */
        DirectoryMonitor outputMonitor = new DirectoryMonitor(_jspDir, getBuildStatusExcluderFilter());
        FileChangeSet actualFcs, expectedFcs;
        File fixtureFile, targetFile, sourceFile;

        /* Create a HTML file and check the corresponding JSP is created */
        fixtureFile = new File(_fixtureRoot, "index.html");
        copy(fixtureFile, _davDir);
        cm.update();

        sourceFile = new File(_davDir, "index.html");
        targetFile = new File(_jspDir, "index.html");

        expectedFcs = new FileChangeSet();
        expectedFcs.add(targetFile, FileChangeSet.ADDED);
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /* Delete a HTML file and check the corresponding JSP is deleted */

        // 02-12-3 delete ( targetFile ) ;
        delete(sourceFile);
        cm.update();
        expectedFcs = new FileChangeSet();
        expectedFcs.add(targetFile, FileChangeSet.DELETED);
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /*
         * Create a tree of directories and check the corresponding tree in the
         * JSP file system
         */
        copy(new File(_fixtureRoot, "about"), _davDir);
        copy(new File(_fixtureRoot, "international"), _davDir);
        cm.update();
        List dirs = Arrays.asList(new String[] { "about", "international", "international/usa", "international/aus",
                "international/gbr", "international/gbr/scotland", "international/gbr/wales",
                "international/gbr/england", "international/gbr/northern-ireland", "international/asia",
                "international/asia/_include" });
        expectedFcs = new FileChangeSet();
        Iterator it = dirs.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.ADDED);
        }
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        // System.out.println ("Expected changes: " + expectedFcs );
        // System.out.println ("Actual changes: " + actualFcs );

        /*
         * Delete a tree of directories and check the corresponding tree in the
         * JSP file system is deleted
         */
        /* - - - international/gbr - - - */
        FileUtilities.killDirectory(new File(_davDir.getAbsolutePath() + File.separator + "international/gbr"));
        cm.update();

        dirs = Arrays.asList(new String[] { "international/gbr", "international/gbr/scotland",
                "international/gbr/wales", "international/gbr/england", "international/gbr/northern-ireland", });
        expectedFcs = new FileChangeSet();
        it = dirs.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.DELETED);
        }
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /* - - - international and about- - - */
        FileUtilities.killDirectory(new File(_davDir.getAbsolutePath() + File.separator + "international"));
        FileUtilities.killDirectory(new File(_davDir.getAbsolutePath() + File.separator + "about"));
        cm.update();

        dirs = Arrays.asList(new String[] { "about", "international", "international/usa", "international/aus",
                "international/asia", "international/asia/_include" });
        expectedFcs = new FileChangeSet();
        it = dirs.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.DELETED);
        }
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /*
         * Check the FileIgnorer is ignoring files in the non html directories
         * and the directories themselves are not being created
         */
        copy(new File(_fixtureRoot, "non-html-dirs"), _davDir);
        cm.update();
        dirs = Arrays.asList(new String[] { "non-html-dirs", "non-html-dirs/showcase",
                "non-html-dirs/showcase/index.html" });
        expectedFcs = new FileChangeSet();
        it = dirs.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.ADDED);
        }
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /* Delete the source files */
        FileUtilities.killDirectory(new File(_davDir, "non-html-dirs"));
        cm.update();

        dirs = Arrays.asList(new String[] { "non-html-dirs", "non-html-dirs/showcase",
                "non-html-dirs/showcase/index.html" });
        expectedFcs = new FileChangeSet();
        it = dirs.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.DELETED);
        }
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /*
         * Check each of the mirrored file types is copied through and deleted
         * okay
         */
        copy(new File(_fixtureRoot, "mirrorred-files"), _davDir);
        cm.update();

        dirs = Arrays.asList(new String[] { "mirrorred-files", "mirrorred-files/authentica.jpeg",
                "mirrorred-files/authentica.jpg", "mirrorred-files/data.zip", "mirrorred-files/document.pdf",
                "mirrorred-files/image.gif", "mirrorred-files/screen-shot.bmp", "mirrorred-files/kde-common.css",
                "mirrorred-files/script.js", "mirrorred-files/site.ico", "mirrorred-files/robots.txt",
                "mirrorred-files/flash-movie.swf",
                "mirrorred-files/sitemap.xml.gz" });
        expectedFcs = new FileChangeSet();
        it = dirs.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.ADDED);
        }
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);
        // System.out.println("actualFcs: " + actualFcs );
        // fail ( "" ) ;

        /* Delete the source files */
        FileUtilities.killDirectory(new File(_davDir, "mirrorred-files"));
        cm.update();

        expectedFcs = new FileChangeSet();
        it = dirs.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.DELETED);
        }
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

    }

    /**
     * Take this file system created by a designer (ie before any automatic
     * processing)
     * 
     * /_include/headers.html /index.html /about/about.html /about/contact.html
     * 
     * If /_include/headers.html is modified then these files may be out of
     * date:
     * 
     * /index.html /about/about.html /about/contact.html
     * 
     * Therefore an addition, modification or deletion of anything in a _include
     * directory should cause an update of every file not in one of the special
     * directories that can depend on these files.
     * 
     * (special directories are _include, _application, _data, and
     * _presentation. At a later time we may want to review weather files in
     * _include directories remain excluded from the cascading update as these
     * files may themselves be dependent on other files. For the moment we
     * implement the minimum requirements.)
     */
    public void testModificationOfDependentFiles() throws DirectoryMonitorException {

        ChannelMonitor cm = getStandardMonitor();

        /* track the changes through a DirectoryMonitor */
        DirectoryMonitor outputMonitor = new DirectoryMonitor(_jspDir, getBuildStatusExcluderFilter());
        FileChangeSet actualFcs, expectedFcs;
        File fixtureFile;
        Iterator it;

        /* Create a HTML file and check the corresponding JSP is created */
        fixtureFile = new File(_fixtureRoot, "dependencies");
        copy(fixtureFile, _davDir);
        cm.update();
        actualFcs = outputMonitor.monitorFolder();

        /*
         * Adding /_include/linktables.html should trigger an update to
         * /index.html and other dependent JSPs.
         * 
         * All the files use a template defined in templates.html. Because
         * linktables.html is alphabetically earlier than templates.html and
         * contains a template with the same name as the depended upon template
         * in templates.html all four pages are modified.
         */
        // System.out.println
        // ("************************************ADD************************");
        /* Add a new template file to /_include */
        copy(new File(_fixtureRoot.getAbsolutePath() + File.separator + "dependencies-extra" + File.separator
                + "_include" + File.separator + "linktables.html"), new File(_davDir.getAbsolutePath() + File.separator
                + "dependencies" + File.separator + "_include"));
        /* ensure modifications can be measured */
        sleep();
        cm.update();

        expectedFcs = new FileChangeSet();
        List modifiedList = Arrays.asList(new String[] { "dependencies/index.html",
                "dependencies/only-depends-on-linktables.html", "dependencies/about/about.html",
                "dependencies/about/contact.html" });
        it = modifiedList.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.MODIFIED);
        }
        /* not interested in these additions but must include for the check */
        List addedList = Arrays.asList(new String[] { "dependencies/_include/linktables.html#sidebar.template",
                "dependencies/_include/linktables.html#sidebar-2.template",
                "dependencies/_include/linktables.html#sidebar-3.template",
                "dependencies/_include/linktables.html#sidebar-4.template" });
        it = addedList.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.ADDED);
        }

        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /*
         * Modifying /_include/templates.html should trigger an update to
         * /index.html and other dependent JSPs.
         */
        // System.out.println
        // ("************************************MODIFY************************");
        File templatesFile = new File(_davDir.getAbsolutePath() + File.separator + "dependencies" + File.separator
                + "_include" + File.separator + "templates.html");
        // if ( ! FileUtilities.touch ( templatesFile) )
        // fail ( "Failed to modify file" ) ;

        copy(new File(_fixtureRoot.getAbsolutePath() + File.separator + "dependencies-extra" + File.separator
                + "_include" + File.separator + "templates-modified.html"), templatesFile);

        /* ensure modifications can be measured */
        sleep();
        cm.update();

        expectedFcs = new FileChangeSet();
        modifiedList = Arrays.asList(new String[] { "dependencies/index.html",
                "dependencies/only-depends-on-templates.html", "dependencies/about/about.html",
                "dependencies/about/contact.html", "dependencies/_include/templates.html#archive.template",
                "dependencies/_include/templates.html#recent-posts.template" });
        it = modifiedList.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.MODIFIED);
        }

        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /*
         * Deleting /_include/templates.html should trigger an update to
         * /index.html and other dependent JSPs.
         */
        // System.out.println
        // ("************************************DELETE************************");
        // System.out.println ("templatesFile: " + templatesFile );
        if (!templatesFile.delete())
            fail("Failed to delete file");

        /* ensure modifications can be measured */
        sleep();
        cm.update();

        expectedFcs = new FileChangeSet();
        modifiedList = Arrays.asList(new String[] { "dependencies/index.html",
                "dependencies/only-depends-on-templates.html", "dependencies/about/about.html",
                "dependencies/about/contact.html" });
        it = modifiedList.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.MODIFIED);
        }
        List deleteList = Arrays.asList(new String[] { "dependencies/_include/templates.html#archive.template",
                "dependencies/_include/templates.html#recent-posts.template" });
        it = deleteList.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.DELETED);
        }

        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /*
         * Another ADD test, this time in a sub-directory.
         */
        /* Add a new template file to /dependencies/about/_include */
        copy(new File(_fixtureRoot.getAbsolutePath() + "/dependencies-extra/about-extra/_include"), new File(_davDir
                .getAbsolutePath()
                + "/dependencies/about"));

        /* ensure modifications can be measured */
        sleep();
        cm.update();

        expectedFcs = new FileChangeSet();
        modifiedList = Arrays
                .asList(new String[] { "dependencies/about/about.html", "dependencies/about/contact.html" });
        it = modifiedList.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.MODIFIED);
        }
        addedList = Arrays.asList(new String[] { "dependencies/about/_include",
                "dependencies/about/_include/contacts.html#contact-details.template" });
        it = addedList.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.ADDED);
        }

        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /*
         * Another DELETE test, this time in a sub-directory.
         */
        FileUtilities
                .killDirectory(new File(_davDir.getAbsolutePath() + File.separator + "dependencies/about/_include"));
        /* ensure modifications can be measured */
        sleep();
        cm.update();

        expectedFcs = new FileChangeSet();
        modifiedList = Arrays
                .asList(new String[] { "dependencies/about/about.html", "dependencies/about/contact.html" });
        it = modifiedList.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.MODIFIED);
        }
        deleteList = Arrays.asList(new String[] { "dependencies/about/_include",
                "dependencies/about/_include/contacts.html#contact-details.template" });
        it = deleteList.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.DELETED);
        }

        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /*
         * Test that adding a file in each of the other system directories also
         * triggers an update
         */

        /* --- _application --- */

        File appDir = new File(_davDir.getAbsolutePath() + File.separator + "dependencies/about/_application");
        if (!appDir.mkdir())
            fail("Failed to create fixture for _application directory test");
        copy(new File(_fixtureRoot.getAbsolutePath() + File.separator
                + "dependencies-extra/_application/application.properties"), new File(_davDir.getAbsolutePath()
                + File.separator + "dependencies/about/_application/application.properties"));

        /* ensure modifications can be measured */
        sleep();
        cm.update();

        expectedFcs = new FileChangeSet();
        modifiedList = Arrays.asList(new String[] { "dependencies/about/about.html"
        // "dependencies/about/contact.html" <-- does not use "my-sao" defined
                // in application.properties
                });
        it = modifiedList.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.MODIFIED);
        }
        /*
         * The new directory and file do not make it to the jsp directory
         * addedList = Arrays.asList ( new String [] {
         * "dependencies/about/_application",
         * "dependencies/about/_application/about.html" } ) ; it =
         * addedList.iterator () ; while ( it.hasNext () ) { String fileName =
         * (String) it.next () ; expectedFcs.add ( _jspDir + File.separator +
         * fileName, FileChangeSet.ADDED ) ; }
         */

        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /* --- _presentation --- */

        // File presDir =
        // new File ( _davDir.getAbsolutePath () + File.separator +
        // "dependencies/about/_presentation" ) ;
        // if ( ! presDir.mkdir () )
        // fail ( "Failed to create fixture for _presentation directory test" )
        // ;
        copy(new File(_fixtureRoot.getAbsolutePath() + File.separator
                + "dependencies-extra/_presentation/presentation.properties"), new File(_davDir.getAbsolutePath()
                + File.separator + "dependencies/about/_presentation/"));

        /* ensure modifications can be measured */
        sleep();
        cm.update();

        expectedFcs = new FileChangeSet();
        modifiedList = Arrays
                .asList(new String[] { "dependencies/about/about.html", "dependencies/about/contact.html" });
        it = modifiedList.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            expectedFcs.add(_jspDir + File.separator + fileName, FileChangeSet.MODIFIED);
        }

        /* The new file does not make it to the jsp directory */

        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /* --- _data --- */
        /*
         * disabled after no write if no change improvement to JSPBuilder.
         * Nothing currently depends on the _data directory contents.
         */
        if (false) {
            // File dataDir =
            // new File ( _davDir.getAbsolutePath () + File.separator +
            // "dependencies/about/_data" ) ;
            // if ( ! dataDir.mkdir () )
            // fail ( "Failed to create fixture for _data directory test" ) ;
            copy(new File(_davDir.getAbsolutePath() + File.separator + "dependencies/about/about.html"), new File(
                    _davDir.getAbsolutePath() + File.separator + "dependencies/about/_data/showcase.xml"));

            /* ensure modifications can be measured */
            sleep();
            cm.update();

            /* The changes should be the same as for _application */
            // expectedFcs = new FileChangeSet () ;
            /* The new directory and file do not make it to the jsp directory */

            actualFcs = outputMonitor.monitorFolder();
            check(expectedFcs, actualFcs);
        }
    }

    public void _testAddFilesInDifferentOrders() {
        /* check API for combination class Collections.shuffle () */
        fail("test not written");
    }

    // ------------------------------------------------------------- Link file
    // testing

    /**
     * Test:
     * 
     * Adding a link file Modifying the file the link file links to Deleting the
     * link file
     */
    public void testBasicLinkFileBehaviour() throws Exception {

        /* Add the file the link will refer to */
        File mbaWebDAVDir = new File(_topFolder, "mba-webdav");
        if (!mbaWebDAVDir.mkdir())
            fail("Created directory okay: " + mbaWebDAVDir);

        File mbaSymDir = new File(_topFolder, "mba-sym");
        if (!mbaSymDir.mkdir())
            fail("Created directory okay: " + mbaSymDir);

        File fixtureFile = new File(_fixtureRoot, "link-targets/advice.html");
        copy(fixtureFile, mbaWebDAVDir);
        /*
         * Make the same symbolic link Mapper would. This saves having to make a
         * second ChannelMonitor and use it to create the 'master' channel
         * although that would be a more correct way.
         */
        FileUtilities.makeSymbolicLink(new File(mbaWebDAVDir, "advice.html"), new File(mbaSymDir, "advice.html"));

        File altFixtureFile = new File(_fixtureRoot, "link-targets/advice-alt.html");
        copy(altFixtureFile, mbaWebDAVDir);
        /*
         * Make the same symbolic link Mapper would. This saves having to make a
         * second ChannelMonitor and use it to create the 'master' channel
         * although that would be a more correct way.
         */
        File altAdvice = new File(mbaWebDAVDir, "advice-alt.html");
        FileUtilities.makeSymbolicLink(altAdvice, new File(mbaSymDir, "advice-alt.html"));

        ChannelMonitor cm = getStandardMonitor(new MyResolver(mbaSymDir));

        /* track the changes through a DirectoryMonitor */
        DirectoryMonitor outputMonitor = new DirectoryMonitor(_jspDir, getBuildStatusExcluderFilter());
        FileChangeSet actualFcs, expectedFcs;
        File linkFile, targetFile;

        /*
         * Add a link file
         */

        /* Create a link file and check the corresponding JSP is created */
        linkFile = new File(_davDir, "advice-for-mbas.html.link");
        FileUtilities.writeString(linkFile, "href=wps://mba/advice.html");
        cm.update();

        targetFile = new File(_jspDir, "advice-for-mbas.html");

        expectedFcs = new FileChangeSet();
        expectedFcs.fileAdded(targetFile);
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /* Do a sanity check on the JSP content */
        assertTrue("The generated JSP contained some expected text", FileUtilities.readFile(targetFile).indexOf(
                "Select a school appropriate to your needs") > 0);

        /*
         * Modify a link file
         */
        /* Modify a link file and check the corresponding JSP is recreated */
        FileUtilities.writeString(linkFile, "href=wps://mba/advice-alt.html");
        cm.update();

        expectedFcs = new FileChangeSet();
        expectedFcs.fileModified(targetFile);
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /* Do a sanity check on the JSP content */
        assertTrue("The re-generated JSP contained some expected text", FileUtilities.readFile(targetFile).indexOf(
                "Financial advice") > 0);

        /*
         * Modify the target of the link, we rely on knowing the modification
         * date of the symbolic link appears changed when accessed via Java.
         */
        FileUtilities
                .writeString(altAdvice, "<html><head><title></title></head><body>Alternative advice</body></html>");

        cm.update();

        expectedFcs = new FileChangeSet();
        expectedFcs.fileModified(targetFile);
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        /* Do a sanity check on the JSP content */
        assertTrue("The re-generated JSP contained some expected text", FileUtilities.readFile(targetFile).indexOf(
                "Alternative advice") > 0);

        /*
         * Delete a link file
         */

        /* Delete a link file and check the corresponding JSP is deleted */
        delete(linkFile);
        cm.update();
        expectedFcs = new FileChangeSet();
        expectedFcs.fileDeleted(targetFile);
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

    }

    private static final String BUILD_STATUS = "build-status.properties";

    public void testFileAdditionWithTheSameNameAsBuildStatusIsIgnored() throws DirectoryMonitorException {

        ChannelMonitor cm = getStandardMonitor();

        /* track the changes through a DirectoryMonitor */
        DirectoryMonitor outputMonitor = new DirectoryMonitor(_jspDir);
        FileChangeSet actualFcs, expectedFcs;
        File fixtureFile;

        /*
         * Add a file with same name as the build status file and confirm the
         * status file is not created.
         */
        fixtureFile = new File(_fixtureRoot, BUILD_STATUS);
        copy(fixtureFile, _davDir);
        cm.update();

        expectedFcs = new FileChangeSet();
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);
        assertEquals(0, actualFcs.size());
    }

    /*
     * Plan: add a DirectoryMonitor that monitors the output directory for
     * changes. It should ignore 'build-status.properties'.
     */
    public void testBuildStatusFileIsCreatedOnChanges() throws DirectoryMonitorException, IOException {

        ChannelMonitor cm = getStandardMonitor();

        /* track the changes through a DirectoryMonitor */
        DirectoryMonitor outputMonitor = new DirectoryMonitor(_jspDir);
        FileChangeSet actualFcs, expectedFcs;
        File fixtureFile, statusFile, indexOutputFile;

        /* Add a HTML file and confirm the status file is created. */
        fixtureFile = new File(_fixtureRoot, "index.html");
        copy(fixtureFile, _davDir);
        assertEquals(1, cm.update().size());

        statusFile = new File(_jspDir, BUILD_STATUS);
        indexOutputFile = new File(_jspDir, "index.html");

        expectedFcs = new FileChangeSet();
        expectedFcs.add(statusFile, FileChangeSet.ADDED);
        expectedFcs.add(indexOutputFile, FileChangeSet.ADDED);
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        checkStatusFileContent(statusFile);

        /*
         * The newly created build status file should not be reported as a
         * modification on the next cm update. Tip: use a build-status file
         * ignoring predicate.
         */
        FileChangeSet cs = cm.update();

        assertEquals("The newly created build status file was not reported as a modification on the next update.",
                new FileChangeSet(), cs);

        /*
         * Delete the status file, delete the input file and confirm the status
         * file is created.
         */
        assertTrue(statusFile.delete());
        assertEquals(1, outputMonitor.monitorFolder().size());
        File indexInputFile = new File(_davDir, "index.html");
        assertTrue(indexInputFile.delete());
        assertEquals("Deleting the JSP resulted in one change being reported", 1, cm.update().size());

        expectedFcs = new FileChangeSet();
        expectedFcs.add(statusFile, FileChangeSet.ADDED);
        expectedFcs.add(indexOutputFile, FileChangeSet.DELETED);
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        checkStatusFileContent(statusFile);

        /*
         * Add a HTML file and update the channel. Delete the status file,
         * modify the input file, update the channel and confirm the status file
         * is created.
         */
        copy(fixtureFile, _davDir);
        assertEquals(1, cm.update().size());

        assertTrue(statusFile.delete());
        assertEquals(2, outputMonitor.monitorFolder().size());

        /* Modify the input file by copying a different sized file over it. */
        copy(new File(_fixtureRoot, "build-status/index.html"), _davDir);
        assertEquals("Modifying the input file resulted in one change being reported", 1, cm.update().size());

        expectedFcs = new FileChangeSet();
        expectedFcs.add(statusFile, FileChangeSet.ADDED);
        expectedFcs.add(indexOutputFile, FileChangeSet.MODIFIED);
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        checkStatusFileContent(statusFile);
    }

    public void testBuildStatusFileIsPartiallyUpdatedOnFileDeletionAdditionOrModification()
            throws DirectoryMonitorException, IOException {
        /*
         * Add a file, update the channel, remember the build status, edit the
         * build status file so that the last-deletion and last-change date is
         * in the remote past, delete a file, check that only the last-change
         * and last-deletion properties actually change. Do the same for
         * addition and modification.
         */

        ChannelMonitor cm = getStandardMonitor();

        /* track the changes through a DirectoryMonitor */
        DirectoryMonitor outputMonitor = new DirectoryMonitor(_jspDir);
        FileChangeSet actualFcs, expectedFcs;

        /* Case: Deletion. */

        /* Add a HTML file and confirm the status file is created. */
        final File fixtureFile = new File(_fixtureRoot, "index.html");
        copy(fixtureFile, _davDir);
        assertEquals(1, cm.update().size());

        final File statusFile = new File(_jspDir, BUILD_STATUS);
        final File indexOutputFile = new File(_jspDir, "index.html");

        expectedFcs = new FileChangeSet();
        expectedFcs.add(statusFile, FileChangeSet.ADDED);
        expectedFcs.add(indexOutputFile, FileChangeSet.ADDED);
        actualFcs = outputMonitor.monitorFolder();
        check(expectedFcs, actualFcs);

        checkStatusFileContent(statusFile);

        writeRemotePastBuildStatus(statusFile);
        checkStatusFileContent(statusFile);

        /* Clear. */
        /*
         * The size of the change set is not checked because the modification
         * time of the build status file is likely to be within the same second
         * as the previous modification.
         */
        outputMonitor.monitorFolder();
        outputMonitor = null;

        /* Delete the input file. */
        File indexInputFile = new File(_davDir, "index.html");
        assertTrue(indexInputFile.delete());

        /* Update the channel. */
        assertEquals("Deleting the JSP resulted in one change being reported", 1, cm.update().size());
        checkStatusFileContent(statusFile);
        {
            String postDeletionBuildStatus[] = FileUtilities.readFile(statusFile).split("\n");
            /* change and deletion should have been updated; nothing else. */
            for (int i = 0; i < postDeletionBuildStatus.length; i++) {
                String p = postDeletionBuildStatus[i];
                if (p.startsWith("last-change="))
                    assertTrue(!CHANGE_REMOTE_PAST.equals(p));
                else if (p.startsWith("last-deletion="))
                    assertTrue(!DELETION_REMOTE_PAST.equals(p));
                else if (p.startsWith("last-addition="))
                    assertEquals(ADDITION_REMOTE_PAST, p);
                else if (p.startsWith("last-modification="))
                    assertEquals(MODIFICATION_REMOTE_PAST, p);
                else
                    fail("All cases were handled");
            }
        }

        /* Case: Addition. */
        /*
         * Re-add the HTML file and confirm the status file is partially
         * updated.
         */
        copy(fixtureFile, _davDir);

        writeRemotePastBuildStatus(statusFile);
        checkStatusFileContent(statusFile);

        /* Update the channel. */
        assertEquals("Adding the JSP resulted in one change being reported", 1, cm.update().size());
        checkStatusFileContent(statusFile);
        {
            String postAdditionBuildStatus[] = FileUtilities.readFile(statusFile).split("\n");
            /* change and addition should have been updated; nothing else. */
            for (int i = 0; i < postAdditionBuildStatus.length; i++) {
                String p = postAdditionBuildStatus[i];
                if (p.startsWith("last-change="))
                    assertTrue(!CHANGE_REMOTE_PAST.equals(p));
                else if (p.startsWith("last-deletion="))
                    assertEquals(DELETION_REMOTE_PAST, p);
                else if (p.startsWith("last-addition="))
                    assertTrue(!ADDITION_REMOTE_PAST.equals(p));
                else if (p.startsWith("last-modification="))
                    assertEquals(MODIFICATION_REMOTE_PAST, p);
                else
                    fail("All cases were handled");
            }
        }

        /* Case: Modification. */
        /*
         * Modify the index file by making it longer and confirm the status file
         * is partially updated.
         */
        writeRemotePastBuildStatus(statusFile);
        checkStatusFileContent(statusFile);

        copy(new File(_fixtureRoot, "build-status/index.html"), _davDir);

        /* Update the channel. */
        assertEquals("Modifying the JSP resulted in one change being reported", 1, cm.update().size());
        checkStatusFileContent(statusFile);
        {
            String postModificationBuildStatus[] = FileUtilities.readFile(statusFile).split("\n");
            /* change and modification should have been updated; nothing else. */
            for (int i = 0; i < postModificationBuildStatus.length; i++) {
                String p = postModificationBuildStatus[i];
                if (p.startsWith("last-change="))
                    assertTrue(!CHANGE_REMOTE_PAST.equals(p));
                else if (p.startsWith("last-deletion="))
                    assertEquals(DELETION_REMOTE_PAST, p);
                else if (p.startsWith("last-addition="))
                    assertEquals(ADDITION_REMOTE_PAST, p);
                else if (p.startsWith("last-modification="))
                    assertTrue(!MODIFICATION_REMOTE_PAST.equals(p));
                else
                    fail("All cases were handled");
            }
        }
    }

    private static final String REMOTE_PAST = "1901-12-07T10:51:50+00:00";

    private static final String CHANGE_REMOTE_PAST = "last-change=" + REMOTE_PAST;

    private static final String ADDITION_REMOTE_PAST = "last-addition=" + REMOTE_PAST;

    private static final String MODIFICATION_REMOTE_PAST = "last-modification=" + REMOTE_PAST;

    private static final String DELETION_REMOTE_PAST = "last-deletion=" + REMOTE_PAST;

    private void writeRemotePastBuildStatus(File statusFile) throws IOException {
        String initialBuildStatus[] = FileUtilities.readFile(statusFile).split("\n");

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < initialBuildStatus.length; i++) {
            String p = initialBuildStatus[i];
            if (p.startsWith("last-change="))
                sb.append(CHANGE_REMOTE_PAST + "\n");
            else if (p.startsWith("last-deletion="))
                sb.append(DELETION_REMOTE_PAST + "\n");
            else if (p.startsWith("last-addition="))
                sb.append(ADDITION_REMOTE_PAST + "\n");
            else if (p.startsWith("last-modification="))
                sb.append(MODIFICATION_REMOTE_PAST + "\n");
            else
                fail("All cases were handled");
        }
        FileUtilities.writeString(statusFile, sb.toString());
    }

    public void _testBuildStatusFileIsUpdatedOnChanges() {
        fail("not written: all modification permutations.");
    }

    public void testBuildStatusFileIsNotUpdatedWhenTheInputChangesButDoesNotChangeAnyJSP() throws IOException {
        /*
         * Add an index.html file, update the channel, add an include directory,
         * update the channel, there should not be build status change. Add an
         * template file in the include directory (that contains unused
         * templates), update the channel. The should not be any build status
         * changes.
         */

        ChannelMonitor cm = getStandardMonitor();

        /* Add a HTML file and confirm the status file is created. */
        final File fixtureFile = new File(_fixtureRoot, "index.html");
        copy(fixtureFile, _davDir);
        assertEquals(1, cm.update().size());

        final File statusFile = new File(_jspDir, BUILD_STATUS);
        assertTrue(statusFile.exists());
        checkStatusFileContent(statusFile);
        assertTrue(statusFile.delete());

        final File includeDirectory = new File(_davDir, "_include");
        assertTrue(includeDirectory.mkdir());
        assertEquals(0, cm.update().size());
        assertTrue("The status file was not created when an empty include directory was added", !statusFile.exists());

        /*
         * Add a templates file that does not change /index.html and confirm the
         * status file is not created.
         */
        copy(new File(_fixtureRoot, "build-status/unused-templates.html"), includeDirectory);
        assertEquals(0, cm.update().size());
        assertTrue("The status file was not created when a templates file with only unused templates was added",
                !statusFile.exists());

    }

    public void _testBuildStatusFileIsUpdatedOnMirroredFileChanges() {
        fail("not written");
    }

    public void _testAdditionOfUnusedTemplateDoesNotResultInAChangeBeingRecorded() {
        /*
         * This is a test that changes restricted to _include do not trigger a
         * change being recorded.
         */
        fail("not written");
    }

    public void testChangesUnderWEBINFDoNotResultInAChangeBeingRecorded() throws IOException {
        /*
         * Update an empty channel, set the build-status to the remote past, add
         * WEB-INF, update the channel, check the build-status has not changed.
         * Add web.xml under WEB-INF, update the channel, check the build-status
         * has not changed.
         */

        ChannelMonitor cm = getStandardMonitor();
        /* Add a HTML file and confirm the status file is created. */
        /*
         * writeRemotePastBuildStatus depends on the pre-existance of
         * build-status.properties.
         */
        final File fixtureFile = new File(_fixtureRoot, "index.html");
        copy(fixtureFile, _davDir);
        assertEquals(1, cm.update().size());

        final File statusFile = new File(_jspDir, BUILD_STATUS);
        writeRemotePastBuildStatus(statusFile);
        checkStatusFileContent(statusFile);
        String statusFileContent = FileUtilities.readFile(statusFile);

        final File webInf = new File(_jspDir, "WEB-INF");
        assertTrue(webInf.mkdir());
        assertEquals(0, cm.update().size());
        assertEquals("The build status properties did not change after adding WEB-INF", statusFileContent,
                FileUtilities.readFile(statusFile));

        /* Make a modification under WEB-INF. */
        copy(new File(_fixtureRoot, "build-status/web.xml"), webInf);
        assertEquals(0, cm.update().size());
        assertEquals("The build status properties did not change after adding WEB-INF", statusFileContent,
                FileUtilities.readFile(statusFile));

    }

    /* ISO 8601, http://www.w3.org/TR/NOTE-datetime */
    /* YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00) */

    private static String ISO8601_REG_EXP = "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d[+-]\\d\\d:\\d\\d";

    private static Pattern STATUS_PATTERNS[] = { Pattern.compile("last-change=" + ISO8601_REG_EXP),
            Pattern.compile("last-addition=" + ISO8601_REG_EXP),
            Pattern.compile("last-modification=" + ISO8601_REG_EXP),
            Pattern.compile("last-deletion=" + ISO8601_REG_EXP) };

    private void checkStatusFileContent(File statusFile) throws IOException {
        assertNotNull(statusFile);
        String content[] = FileUtilities.readFile(statusFile).split("\n");
        assertEquals("The build status file had the correct number of properties", 4, content.length);
        for (int i = 0; i < STATUS_PATTERNS.length; i++) {
            Pattern pat = STATUS_PATTERNS[i];
            Matcher m = pat.matcher(content[i]);
            assertTrue("Line " + (i + 1) + " of the build status file matched regex: '" + content[i] + "', '"
                    + pat.toString() + "'", m.matches());
        }
    }

    private class MyResolver implements Resolver {

        File mbaSymDir;

        MyResolver(File mbaSymDir) {
            this.mbaSymDir = mbaSymDir;
        }

        public File resolve(LinkFile linkFile) {

            if (!"mba".equals(linkFile.getHost()))
                throw new IllegalArgumentException("Can not resolve " + linkFile);

            return new File(mbaSymDir, linkFile.getPath());
        }

    }

    protected void check(FileChangeSet expectedFcs, FileChangeSet actualFcs) {

        assertEquals("The expected set of changes was different to the actual set", expectedFcs, actualFcs);
    }

    protected void copy(File sourceFile, File destFile) {

        if (!FileUtilities.copy(sourceFile, destFile))
            fail("Failed to copy file:\n" + sourceFile + "\n" + destFile);

    }

    protected void delete(File file) {

        if (!file.delete())
            fail("Failed to delete file: " + file);

    }

    /**
     * Create and return an instance of a StandardChannelMonitor.
     */
    protected ChannelMonitor getStandardMonitor() {

        return getStandardMonitor((Resolver) null);

    }

    /**
     * Create and return an instance of a StandardChannelMonitor.
     */
    protected ChannelMonitor getStandardMonitor(Resolver resolver) {

        ContextPath cp = null;
        try {
            cp = new ContextPath("test-channel");
        } catch (ContextPathException e) {
            fail("Failed to create fixture: " + e);
        }

        StandardChannelMonitor cm = new StandardChannelMonitor(cp, // ContextPath
                // cp,
                Charset.forName("ISO-8859-1"), // Charset encoding
                _davDir, // File inputFolder,
                _symDir, // File intermediateFolder,
                _jspDir, // File outputFolder,
                Constants.getNonHtmlDirectoryNames(), Constants.getMirroredFileSuffixes(), resolver);

        try {
            cm.init();
        } catch (ChannelMonitorException e) {
            fail("StandardChannelMonitorException for context path: " + cp + " " + e);
        }

        /* Any exception should result in a test failure. */
        cm.setExceptionListener(new StandardChannelMonitor.ExceptionListener() {
            public void exception(Exception e) throws RuntimeException {
                throw new RuntimeException(e);
            }
        });

        return cm;
    }

    /**
     * wait 1 seconds to allow file modification times to differ.
     */
    protected void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail("sleep interrupted");
        }
    }

}
