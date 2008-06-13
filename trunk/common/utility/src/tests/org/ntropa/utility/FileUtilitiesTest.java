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
 * FileUtilitiesTest.java
 *
 * Created on September 12, 2001, 3:10 PM
 */

package tests.org.ntropa.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.NtropaTestWorkDirHelper;

/**
 * 
 * @author jdb
 * @version $Id: FileUtilitiesTest.java,v 1.6 2005/02/24 11:30:29 jdb Exp $
 */
public class FileUtilitiesTest extends TestCase {

    private String TEST_ROOT;

    public FileUtilitiesTest(String testName) {
        super(testName);
    }

    private File packageDir;

    protected void setUp() throws Exception, IOException {
        TEST_ROOT = new NtropaTestWorkDirHelper().getWorkDir();
        packageDir = new File(System.getProperty("utility.package.dir"));
    }

    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests. It
     * runs a collection of test cases.
     */
    public static Test suite() {

        TestSuite suite = new TestSuite(FileUtilitiesTest.class);
        return suite;
    }

    public void testPathElementCount() {

        String msg = "The returned count of path elements was wrong: ";

        String[] p = new String[11];
        int[] c = new int[11];

        p[0] = "";
        c[0] = 0;

        p[1] = "/";
        c[1] = 0;

        p[2] = "about";
        c[2] = 1;

        p[3] = "/about";
        c[3] = 1;

        p[4] = "about/";
        c[4] = 1;

        p[5] = "/about/";
        c[5] = 1;

        p[6] = "about/information";
        c[6] = 2;

        p[7] = "/about/information";
        c[7] = 2;

        p[8] = "about/information/";
        c[8] = 2;

        p[9] = "/about/information/";
        c[9] = 2;

        p[10] = "/1/2/3/4/5/6/7/8/9";
        c[10] = 9;

        for (int tIdx = 0; tIdx < p.length; tIdx++) {
            int pathCnt = FileUtilities.pathElementCount(p[tIdx]);
            if (pathCnt != c[tIdx])
                fail(msg + "\"" + p[tIdx] + "\"" + " (Received " + pathCnt + ")");
        }

    }

    public void testPathElements() {

        assertEquals("The file components were wrong", Collections.EMPTY_LIST, FileUtilities.pathElements("/"));

        assertEquals("The file components were wrong", Arrays.asList(new String[] { "a" }), FileUtilities
                .pathElements("a"));

        assertEquals("The file components were wrong", Arrays.asList(new String[] { "a", "b" }), FileUtilities
                .pathElements("a/b"));

        assertEquals("The file components were wrong", Arrays.asList(new String[] { "a", "b", "foobar" }),
                FileUtilities.pathElements("a/b/foobar"));

        assertEquals("The file components were wrong", Arrays.asList(new String[] { "a", "b", "foobar" }),
                FileUtilities.pathElements("a/b/foobar/"));
    }

    public void testMakeSymbolicLink() throws IOException {
        File dstDir = new File(TEST_ROOT + "/FileUtilitiesTest-" + new Date().getTime());
        if (!dstDir.mkdir())
            fail("Failed to create directory: " + dstDir);
        try {
            File source = new File(dstDir, "source-file");
            String SOURCE_CONTENT = "source-content";
            FileUtilities.writeString(source, SOURCE_CONTENT);
            File dest = new File(dstDir, "link-file");
            assertTrue(FileUtilities.makeSymbolicLink(source, dest));
            assertEquals(SOURCE_CONTENT, FileUtilities.readFile(dest));

            if (false) {
                // Each use of exec opens 3 files: stdin, stdout, stderr.
                // Try to reproduce
                // java.io.IOException: Too many open files
                // See: http://www.vnoel.com/content/view/51/49/
                // This error was encountered when running the wps on
                // SLINK,
                // Linux slink 2.6.8-2-686 #1 Tue Aug 16 13:22:48 UTC 2005 i686
                // GNU/Linux
                // Debian GNU/Linux 3.1 but this lengthy test failed to expose
                // it.
                for (int i = 1; i <= 10000; i++) {
                    if (i % 1000 == 1)
                        System.out.println("i: " + i);
                    FileUtilities.makeSymbolicLink(source, dest);
                }
            }
        } finally {
            FileUtilities.killDirectory(dstDir);
        }
    }

    public void testIsSymbolicLinkWithMissingTarget() throws IOException {
        File dstDir = new File(TEST_ROOT + "/FileUtilitiesTest-" + new Date().getTime());
        assertTrue(dstDir.mkdir());

        // case 1: A regular file: false
        File regularFile = new File(dstDir, "regular-file");
        FileUtilities.writeString(regularFile, "content");
        assertTrue(regularFile.exists());

        assertEquals("False for a regular file", false, FileUtilities.isSymbolicLinkWithMissingTarget(regularFile));

        // case 2: A directory: false
        File directory = new File(dstDir, "a-dir");
        assertTrue(directory.mkdir());

        assertEquals("False for a directory", false, FileUtilities.isSymbolicLinkWithMissingTarget(directory));

        // case 3: A symbolic link to an existing file: false
        File linkFile = new File(dstDir, "link-file");
        assertTrue(FileUtilities.makeSymbolicLink(regularFile, linkFile));

        assertEquals("False for a symbolic link to an existing file", false, FileUtilities
                .isSymbolicLinkWithMissingTarget(linkFile));

        // case 4: A symbolic link to a missing file: true
        assertTrue(regularFile.delete());
        assertEquals("True for a symbolic link to an missing file", true, FileUtilities
                .isSymbolicLinkWithMissingTarget(linkFile));

        FileUtilities.killDirectory(dstDir);
    }

    public void testExtractZip() {
        File src = new File(packageDir, "standardpresentationfindertestfs.zip");
        if (!src.exists())
            fail("File did not exist: " + src);
        File dstDir = new File(TEST_ROOT + "/FileUtilitiesTest-" + new Date().getTime());
        if (!dstDir.mkdir())
            fail("Failed to create directory: " + dstDir);
        assertTrue("Method completed normally", FileUtilities.extractZip(src, dstDir));
        String content[] = dstDir.list();
        assertEquals(1, content.length);
        assertEquals("standardpresentationfindertestfs", content[0]);

        FileUtilities.killDirectory(dstDir);
    }

    // This variation uses the same path as StandardPresentationFinderTest which
    // was hanging
    // in the unzip command
    public void testExtractZipWithLongDestinationPath() {
        File src = new File(packageDir, "standardpresentationfindertestfs.zip");
        if (!src.exists())
            fail("File did not exist: " + src);
        File dstDir = new File(TEST_ROOT + "/xl-test/StandardPresentationFinderTest.tmp/");
        // The path length was reduced and eventually it worked. Then the -q
        // option was added to the unzip invocation
        // File dstDir = new
        // File("/t mp/xl-test/StandardPresentationFinderTest.t/");
        // File dstDir = new
        // File("/t mp/xl-test/StandardPresentationFinderTest./");
        // File dstDir = new
        // File("/t mp/xl-test/StandardPresentationFinderTest");
        // File dstDir = new File("/t mp/xl-test/StandardPresentationFinderT/");
        if (!dstDir.mkdirs())
            fail("Failed to create directory: " + dstDir);
        assertTrue("Method completed normally", FileUtilities.extractZip(src, dstDir));
        String content[] = dstDir.list();
        assertEquals(1, content.length);
        assertEquals("standardpresentationfindertestfs", content[0]);

        FileUtilities.killDirectory(TEST_ROOT + "/xl-test");
    }

    public void testReadFile() throws IOException {
        // Sourced from http://google.cn.
        File src = new File(packageDir, "utf-8-file.txt");
        if (!src.exists())
            fail("File did not exist: " + src);

        StringBuffer sb = new StringBuffer();
        FileUtilities.readFile(src, sb, Charset.forName("UTF-8"));

        String s = sb.toString();
        assertTrue("'2006 Google' present", s.indexOf("2006 Google") > 0);
        // U+641C: seek, investigate
        // UTF-8: 0xE6 0x90 0x9C
        // Octal escaped UTF-8: \346\220\234
        // Decimal entity reference: &#25628;

        // U+7D22: to search, inquire
        // UTF-8: 0xE7 0xB4 0xA2
        // Octal escaped UTF-8: \347\264\242
        // Decimal entity reference: &#32034;

        assertTrue("'seek/search' present", s.indexOf("\u641C\u7D22") > 0);
    }

    public void testWriteString() throws IOException {
        File dest = new File(packageDir, "utf-8-file-write.txt");
        FileUtilities.writeString(dest, "\u7B90", Charset.forName("UTF-8"));
        if (!dest.exists())
            fail("File did not exist: " + dest);

        // U+7B90
        // UTF-8: 0xE7 0xAE 0x90
        // Octal escaped UTF-8: \347\256\220
        // Decimal entity reference: &#31632;

        FileInputStream in = new FileInputStream(dest);
        assertEquals(0xE7, in.read());
        assertEquals(0xAE, in.read());
        assertEquals(0x90, in.read());
    }

}
