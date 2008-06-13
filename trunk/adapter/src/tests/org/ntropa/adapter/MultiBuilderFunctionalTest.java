package tests.org.ntropa.adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.utility.FileUtilities;

public class MultiBuilderFunctionalTest extends TestCase {

    public MultiBuilderFunctionalTest(String testName) {
        super(testName);
    }

    public static Test suite() {

        TestSuite suite = new TestSuite(MultiBuilderFunctionalTest.class);
        return suite;
    }

    private File MASTER_IN;

    private File SITE_A_IN;

    private File SITE_B_IN;

    private File MASTER_OUT;

    private File MASTER_BUILD_STATUS;

    private File SITE_A_OUT;

    private File SITE_A_BUILD_STATUS;

    private File SITE_B_OUT;

    private File SITE_B_BUILD_STATUS;

    protected void setUp() throws Exception {
        final String BUILD_STATUS_PROPERTIES_NAME = "build-status.properties";

        {
            File inputDir = validatedDirectory(validatedProperty("ntropa.input-dir"));
            FileUtilities.ensureDirectoryIsWritable(inputDir);

            /* Input directories should match set up in /build.xml */
            File inputDirs[] = inputDir.listFiles();
            assertNotNull(inputDirs);
            ensureDirectoriesAreWritable(inputDirs);

            Map inputSites = validatedDirMap(inputDirs);

            MASTER_IN = (File) inputSites.get("master");
            SITE_A_IN = (File) inputSites.get("site-a");
            SITE_B_IN = (File) inputSites.get("site-b");
        }
        {
            File outputDir = validatedDirectory(validatedProperty("ntropa.output-dir"));
            FileUtilities.ensureDirectoryIsReadable(outputDir);

            /* Output directories should match set up in /build.xml */
            File outputDirs[] = outputDir.listFiles();
            assertNotNull(outputDirs);
            ensureDirectoriesAreWritable(outputDirs);

            Map outputSites = validatedDirMap(outputDirs);

            MASTER_OUT = (File) outputSites.get("master");
            MASTER_BUILD_STATUS = new File(MASTER_OUT, BUILD_STATUS_PROPERTIES_NAME);
            SITE_A_OUT = (File) outputSites.get("site-a");
            SITE_A_BUILD_STATUS = new File(SITE_A_OUT, BUILD_STATUS_PROPERTIES_NAME);
            SITE_B_OUT = (File) outputSites.get("site-b");
            SITE_B_BUILD_STATUS = new File(SITE_B_OUT, BUILD_STATUS_PROPERTIES_NAME);

        }

        // System.out.println("Listing " + SITE_A_OUT);
        // String f[] = SITE_A_OUT.list();
        // for (int i = 0; i < f.length; i++) {
        // System.out.println(f[i]);
        //        }
    }

    protected void tearDown() throws Exception {
        File dirs[] = new File[] { SITE_A_IN, SITE_B_IN, MASTER_IN };
        for (int i = 0; i < dirs.length; i++) {
            File dir = dirs[i];
            File list[] = dir.listFiles();
            for (int j = 0; j < list.length; j++) {
                File t = list[j];
                if (t.isFile())
                    t.delete();
                else
                    FileUtilities.killDirectory(t);
            }
        }

        /*
         * Wait for builder process to notice changes and delete output files
         * and directories.
         */
        Thread.sleep(2 * 1000);

        File outdirs[] = new File[] { SITE_A_OUT, SITE_B_OUT, MASTER_OUT };
        for (int i = 0; i < outdirs.length; i++) {
            File dir = outdirs[i];
            String list[] = dir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return !name.equals("build-status.properties");
                }
            });

            if (list.length > 0) {
                for (int f = 0; f < list.length; f++)
                    System.out.println("Not deleted: '" + list[f] + "'");
                throw new RuntimeException("Failed to remove all output files (apart from build-status.properties).");
            }
        }
    }

    private Map validatedDirMap(File dirs[]) {
        List expectedSiteList = Arrays.asList(new String[] { "master", "site-a", "site-b" });
        SortedMap actualSites = dirMap(dirs);
        assertEquals("This list of sites was correct", expectedSiteList, new LinkedList(actualSites.keySet()));
        return actualSites;
    }

    // master -> build/builder-test/input/master
    private SortedMap dirMap(File dirs[]) {
        SortedMap result = new TreeMap();
        for (int i = 0; i < dirs.length; i++) {
            result.put(dirs[i].getName(), dirs[i]);
        }
        return result;
    }

    private void ensureDirectoriesAreWritable(File[] dirs) throws FileNotFoundException, IOException {
        for (int i = 0; i < dirs.length; i++) {
            File dir = dirs[i];
            FileUtilities.ensureDirectoryIsWritable(dir);
        }
    }

    private String validatedProperty(String key) {
        String result = System.getProperty(key);
        if (result == null)
            throw new IllegalStateException("No value defined for key: '" + key + "'");
        return result;
    }

    private File validatedDirectory(String path) {
        if (path == null)
            throw new IllegalArgumentException("path was null");
        if (path.length() == 0)
            throw new IllegalArgumentException("path was empty");
        File result = new File(path);
        return result;
    }

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final String SIMPLE_HTML_CONTENT = "<html><head><title>My Simple Html File</title></head>"
            + "<body><h1>The body content</body></html>";

    private void initialDelayWhileMultiBuilderLeavesLowAlertMode() throws InterruptedException {
        Thread.sleep(11 * 1000);
    }

    public void testSimpleHtmlDocumentBuildsOkay() throws Exception {

        /*
         * PDL
         * 
         * 1. Write an simple html file to the ntropa input directory.
         * 
         * 2. Wait a short time.
         * 
         * 3. Check a corresponding JSP has been created in the output
         * directory.
         */
        FileUtilities.writeString(new File(SITE_A_IN, "simple.html"), SIMPLE_HTML_CONTENT, UTF_8);
        FileUtilities.writeString(new File(SITE_B_IN, "simple.html"), SIMPLE_HTML_CONTENT, UTF_8);
        FileUtilities.writeString(new File(MASTER_IN, "simple.html"), SIMPLE_HTML_CONTENT, UTF_8);

        initialDelayWhileMultiBuilderLeavesLowAlertMode();

        File expectedOutFileA = new File(SITE_A_OUT, "simple.html");
        assertTrue("The output file was created", expectedOutFileA.exists());
        assertTrue("The build status file was created", SITE_A_BUILD_STATUS.exists());

        File expectedOutFileB = new File(SITE_B_OUT, "simple.html");
        assertTrue("The output file was created", expectedOutFileB.exists());
        assertTrue("The build status file was created", SITE_B_BUILD_STATUS.exists());

        File expectedOutFileM = new File(MASTER_OUT, "simple.html");
        assertTrue("The output file was created", expectedOutFileM.exists());
        assertTrue("The build status file was created", MASTER_BUILD_STATUS.exists());

    }

    public void testLinkFileEffective() throws IOException {
        /*
         * PDL
         * 
         * 1. Write a file to the master directory
         * 
         * 2. Add a link file in site a
         * 
         * 3. Wait 2 seconds
         * 
         * 4. Confirm site a contains the same file as master.
         */
        FileUtilities.writeString(new File(MASTER_IN, "simple.html"), SIMPLE_HTML_CONTENT, UTF_8);
        FileUtilities.writeString(new File(SITE_A_IN, "linked.html.link"), "href=wps://master/simple.html");
        pause();
        File expected = new File(SITE_A_OUT, "linked.html");
        assertTrue("linked.html was created as a result of adding linked.html.link", expected.exists());
        checkStringsInOrder(FileUtilities.readFile(expected, UTF_8), new String[] { "<html>", "<head>",
                "My Simple Html File", "</body>", "</html>" });
    }

    private void pause() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check the strings in the array of strings occur in the string in order
     * 
     * @param expectedSubstrings
     *            An array of expected substrings
     * @param targetString
     *            The string to check
     */
    private void checkStringsInOrder(String targetString, String[] expectedSubstrings) {
        int lastOffset = 0;
        for (int i = 0; i < expectedSubstrings.length; i++) {
            int offset = targetString.indexOf(expectedSubstrings[i], lastOffset);
            if (offset == -1)
                fail("The String does not have the string '" + expectedSubstrings[i] + "' in it.");
            if (offset < lastOffset)
                fail("The String does not have the string '" + expectedSubstrings[i] + "' in the right position.");
            lastOffset = offset;
        }
    }
}
