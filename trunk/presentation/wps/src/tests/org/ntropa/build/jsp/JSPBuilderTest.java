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
 * JSPBuilderTest.java
 *
 * Created on 31 August 2001, 16:43
 */

package tests.org.ntropa.build.jsp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.Constants;
import org.ntropa.build.ContextPath;
import org.ntropa.build.ContextPathException;
import org.ntropa.build.DirectoryMonitorException;
import org.ntropa.build.DirectoryPair;
import org.ntropa.build.DirectoryPairException;
import org.ntropa.build.FileListenerEvent;
import org.ntropa.build.channel.FileLocation;
import org.ntropa.build.channel.FileLocationException;
import org.ntropa.build.jsp.JSPBuilder;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.NtropaTestWorkDirHelper;

/**
 * 
 * @author jdb
 * @version $Id: JSPBuilderTest.java,v 1.28 2006/03/22 16:31:19 jdb Exp $
 */
public class JSPBuilderTest extends TestCase {

    final String SOURCE_DIR_NAME = "html-source";

    final String DESTINATION_DIR_NAME = "jsp-destination";

    private File m_TopFolder;

    private File m_SourceFolder;

    private File m_DestinationFolder;

    // The split formatting of the directives prevents the extraneous white
    // space
    // that would result from this
    // <%@ page buffer = \"16kb\" %>
    // <%@ page contentType = \"ISO-8859-1\" %>

    final String BUFFER_PAGE_DIRECTIVE = "page buffer = \"16kb\"";

    final String ISO_8859_1_PAGE_DIRECTIVE = "page contentType = \"text/html;charset=ISO-8859-1\"";

    final String UTF_8_PAGE_DIRECTIVE = "page contentType = \"text/html;charset=UTF-8\"";

    public JSPBuilderTest(String testName) {
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

        TestSuite suite = new TestSuite(JSPBuilderTest.class);
        return suite;
    }

    protected void setUp() throws Exception, IOException {
        String TEST_ROOT = new NtropaTestWorkDirHelper().getWorkDir() + "/tests.org.ntropa.build.jsp.JSPBuilderTest";

        m_TopFolder = new File(TEST_ROOT);
        if (!m_TopFolder.mkdirs())
            throw new Exception("Failed to create folder or folder already existed");

        m_SourceFolder = new File(m_TopFolder, SOURCE_DIR_NAME);
        if (!m_SourceFolder.mkdir())
            throw new Exception("Failed to create folder or folder already existed");

        m_DestinationFolder = new File(m_TopFolder, DESTINATION_DIR_NAME);
        if (!m_DestinationFolder.mkdir())
            throw new Exception("Failed to create folder or folder already existed");
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() throws Exception {

        FileUtilities.killDirectory(m_TopFolder);
    }

    public void _testDumpProperties() {
        System.out.println("LANG: " + System.getenv("LANG"));
        System.getProperties().list(System.out);
        fail("stopper");
    }

    /**
     * Exercise toString () method. Does not test returned value.
     */
    public void testToString() {
        JSPBuilder jspb = getJSPBuilder();
        String s = jspb.toString();
        if (s.startsWith("java.lang.Object"))
            fail("toString () overide missing");
        // System.out.println( s );
    }

    public void testBasicFileEventBehaviour() throws IOException, FileLocationException, Exception,
            ContextPathException {

        doBasicFileEventBehaviour();
    }

    public void testEditDOMWithOneFile() throws IOException, FileLocationException, Exception, ContextPathException {
        JSPBuilder jspb = getJSPBuilder();

        jspb.setDebugLevel(0);

        Properties p = new Properties();
        p.put("keywords", "my-keywords");
        p.put("description", "my-description");
        writePresentationParameters(p);

        String htmlString = "<html><head><title>testFileAddedEvent</title></head><body></body></html>";

        String s = addFile(jspb, "DOM-EDITS.html", htmlString);

        /*
         * This is basic test for the meta tags. They should be in the head
         * section: <head> <meta contents="my-keywords" name="keywords" > <meta
         * contents="my-description" name="description" > <meta contents="1740"
         * http-equiv="refresh" > </head>
         * 
         * The test simply checks the bits of text are in the right order. It
         * makes an assummption about the order of the meta tags.
         * 
         * Note: when extending this test remember that the HTML in the JSP will
         * be escaped, i.e. " will be \".
         */

        String[] expectedOrder = { "<html>", "<head>", "<meta", "my-keywords", "my-description", "1740", "</head>",
                "<body", "</body>", "</html>" };

        checkStringsInOrder(expectedOrder, s);
    }

    public void testEditDOM() throws IOException, FileLocationException, Exception, ContextPathException {

        /* Alter the code paths in JSPBuilder by adding presentation parameters */
        Properties p = new Properties();
        p.put("keywords", "my-keywords");
        p.put("description", "my-description");
        writePresentationParameters(p);
        doBasicFileEventBehaviour();
    }

    /**
     * A basic check to see if new JSPs result from the addition of HTML files.
     * A basic check to see if JSPs are deleted when HTML files are deleted.
     */
    public void doBasicFileEventBehaviour() throws IOException, FileLocationException, Exception, ContextPathException {

        JSPBuilder jspb = getJSPBuilder();

        jspb.setDebugLevel(0);

        /*
         * Trying to use the debugger, the pause is to set a breakpoint, no
         * success so far try { Thread.sleep ( 200 * 1000 ) ; } catch (
         * InterruptedException e ) {}
         */
        int fileCnt = 10;
        // Create files and send events.
        String htmlString = "<html><head><title>testFileAddedEvent</title></head><body></body></html>";
        // htmlString =
        // "<html><head><meta name=\"x\" attr2=\"y\"><meta name=\"keywords\"
        // contents=\"key1, key2\">" +
        // "<title>testFileAddedEvent</title></head><body></body></html>" ;
        for (int fileIDX = 1; fileIDX <= fileCnt; fileIDX++) {
            String fileName = "page-" + fileIDX + ".html";
            File f = new File(m_SourceFolder, fileName);
            FileWriter fw = new FileWriter(f);
            fw.write(htmlString);
            fw.close();

            /*
             * Send a message to the JSPBuilder. The JSPBuilder knows what
             * channel it is responsible for so the relative path in the
             * FileLocation constructor is desirable.
             */
            FileLocation fileLocation = new FileLocation(fileName);
            FileListenerEvent e = new FileListenerEvent(fileLocation);

            if ((fileIDX & 1) == 0)
                jspb.fileModified(e);
            else
                jspb.fileAdded(e);

            File newJSPFile = new File(m_DestinationFolder, fileName);
            if (!newJSPFile.exists())
                fail("JSPBuilder failed to create the file for source file: " + fileName);
        }

        // Delete files and send events
        for (int fileIDX = 1; fileIDX <= fileCnt; fileIDX++) {
            String fileName = "page-" + fileIDX + ".html";
            File f = new File(m_SourceFolder, fileName);

            if (!f.delete())
                throw new Exception("Failure in fixture: failed to delete file");

            FileLocation fileLocation = new FileLocation(fileName);
            FileListenerEvent e = new FileListenerEvent(fileLocation);

            jspb.fileDeleted(e);

            File deletedJSPFile = new File(m_DestinationFolder, fileName);
            if (deletedJSPFile.exists())
                fail("JSPBuilder failed to delete the file for source file: " + fileName);

        }

    }

    /*
     * Check a JSP is not written to disk if it is the same as the current file
     * on disk.
     */
    public void testNoWriteIfNotChanged() throws IOException, FileLocationException {
        JSPBuilder jspb = getJSPBuilder();

        jspb.setDebugLevel(0);

        /* Create file and send events. */
        String htmlString = "<html><head><title>testNoWriteIfNotChanged</title></head><body></body></html>";

        String fileName = "index.html";
        File f = new File(m_SourceFolder, fileName);
        FileWriter fw = new FileWriter(f);
        fw.write(htmlString);
        fw.close();

        /*
         * Send a message to the JSPBuilder. The JSPBuilder knows what channel
         * it is responsible for so the relative path in the FileLocation
         * constructor is desirable.
         */
        FileLocation fileLocation = new FileLocation(fileName);
        FileListenerEvent e = new FileListenerEvent(fileLocation);

        jspb.fileAdded(e);

        File newJSPFile = new File(m_DestinationFolder, fileName);
        if (!newJSPFile.exists())
            fail("JSPBuilder created the file for source file: " + fileName);

        /*
         * Avoid having to wait >= 1 second as Linux file mod times are reported
         * to second resolution.
         * 
         * 00:00:02 GMT, January 1, 1970
         */
        long DAWN_OF_TIME = 2000;
        newJSPFile.setLastModified(DAWN_OF_TIME);

        jspb.fileModified(e);

        assertEquals("The JSP was not written to disk when it was the same as the existing file", DAWN_OF_TIME,
                newJSPFile.lastModified());

        /* sanity test */
        htmlString = "<html><head><title>* * * * CHANGED * * * *</title></head><body></body></html>";

        File fChanged = new File(m_SourceFolder, fileName);
        FileWriter fwChanged = new FileWriter(fChanged);
        fwChanged.write(htmlString);
        fwChanged.close();

        jspb.fileModified(e);

        if (newJSPFile.lastModified() == DAWN_OF_TIME)
            fail("The JSP was written to disk when it was different to the existing file");

    }

    /**
     * Test the first directive in a JSP
     */
    // <%@
    // page buffer = "16kb"
    // %><%@
    // page contentType = "ISO-8859-1"
    public void testInitialPageDirectivesISO_8859_1() throws IOException, FileLocationException, ContextPathException {

        String lines[] = new String[4];
        getInitialLines(Charset.forName("ISO-8859-1"), lines);

        assertEquals("Opening directive tag was present", "<%@", lines[0]);

        assertEquals("The buffer page directive was correct", BUFFER_PAGE_DIRECTIVE, lines[1]);

        assertEquals("Closing then opening directive tags were present", "%><%@", lines[2]);

        assertEquals("The contentType page directive was correct", ISO_8859_1_PAGE_DIRECTIVE, lines[3]);

    }

    // <%@
    // page buffer = "16kb"
    // %><%@
    // page contentType = "UTF-8"
    public void testInitialPageDirectivesUTF_8() throws IOException, FileLocationException, ContextPathException {

        String lines[] = new String[4];
        getInitialLines(Charset.forName("UTF-8"), lines);

        assertEquals("Opening directive tag was present", "<%@", lines[0]);

        assertEquals("The buffer page directive was correct", BUFFER_PAGE_DIRECTIVE, lines[1]);

        assertEquals("Closing then opening directive tags were present", "%><%@", lines[2]);

        assertEquals("The contentType page directive was correct", UTF_8_PAGE_DIRECTIVE, lines[3]);

    }

    private void getInitialLines(Charset encoding, String[] lines) throws IOException, FileLocationException {

        JSPBuilder jspb = getJSPBuilder(encoding);

        String htmlString = "<html><head><title>testInitialPageDirectives</title></head><body></body></html>";

        String fileName = "page-directive.html";
        File f = new File(m_SourceFolder, fileName);
        FileUtilities.writeString(f, htmlString, encoding);

        /*
         * Send a message to the JSPBuilder. The JSPBuilder knows what channel
         * it is responsible for so the relative path in the FileLocation
         * constructor is desirable.
         */
        FileLocation fileLocation = new FileLocation(fileName);
        FileListenerEvent e = new FileListenerEvent(fileLocation);

        jspb.fileAdded(e);

        /*
         * Read the first line of the new JSP and check it starts with the
         * appropriate page directive.
         */
        File newJSPFile = new File(m_DestinationFolder, fileName);
        if (!newJSPFile.exists())
            fail("JSPBuilder failed to create the destination file: " + fileName);

        BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(newJSPFile), encoding));

        for (int i = 0; i < lines.length; i++)
            lines[i] = b.readLine();

        b.close();
    }

    /**
     * Added for XLM-241
     */
    public void testDTDIsTransitional() throws IOException, FileLocationException, ContextPathException {

        JSPBuilder jspb = getJSPBuilder();

        String htmlString = "<html><head><title>testDTDIsTransitional</title></head><body></body></html>";

        String fileName = "page-dtd.html";
        File f = new File(m_SourceFolder, fileName);
        FileWriter fw = new FileWriter(f);
        fw.write(htmlString);
        fw.close();

        /*
         * Send a message to the JSPBuilder. The JSPBuilder knows what channel
         * it is responsible for so the relative path in the FileLocation
         * constructor is desirable.
         */
        FileLocation fileLocation = new FileLocation(fileName);
        FileListenerEvent e = new FileListenerEvent(fileLocation);

        jspb.fileAdded(e);

        // the double \s are because the target string is escaped
        String PUBLIC = "-//W3C//DTD HTML 4.01 Transitional//EN";
        String SYSTEM = "http://www.w3.org/TR/html4/loose.dtd";

        checkPUBLICAndSYSTEMIds(new File(m_DestinationFolder, fileName), PUBLIC, SYSTEM);
    }

    public void testDOCTYPEConfigurationChangesPUBLICAndSYSTEMIds() throws IOException, FileLocationException,
            ContextPathException {

        JSPBuilder jspb = getJSPBuilder();

        String htmlString = "<html><head><title>testDOCTYPEConfigurationChangesPUBLICAndSYSTEMIds</title></head><body></body></html>";

        String fileName = "page-dtd-configured.html";
        File f = new File(m_SourceFolder, fileName);
        FileWriter fw = new FileWriter(f);
        fw.write(htmlString);
        fw.close();

        String PUBLIC = "-//W3C//DTD XHTML 1.0 Strict//EN";
        String SYSTEM = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";
        Properties p = new Properties();
        p.put("doctype-public", PUBLIC);
        p.put("doctype-system", SYSTEM);
        writePresentationParameters(p);

        /*
         * Send a message to the JSPBuilder. The JSPBuilder knows what channel
         * it is responsible for so the relative path in the FileLocation
         * constructor is desirable.
         */
        FileLocation fileLocation = new FileLocation(fileName);
        FileListenerEvent e = new FileListenerEvent(fileLocation);

        jspb.fileAdded(e);

        checkPUBLICAndSYSTEMIds(new File(m_DestinationFolder, fileName), PUBLIC, SYSTEM);

    }

    public void testEmptyDTDSystemResultsInNoSYSTEMElement() throws IOException, FileLocationException,
            ContextPathException {

        JSPBuilder jspb = getJSPBuilder();

        String htmlString = "<html><head><title>testEmptyDTDSystemResultsInNoSYSTEMElement</title></head><body></body></html>";

        String fileName = "page-dtd-configured-with-no-system-id.html";
        File f = new File(m_SourceFolder, fileName);
        FileWriter fw = new FileWriter(f);
        fw.write(htmlString);
        fw.close();

        String PUBLIC = "-//W3C//DTD XHTML 1.0 Strict//EN";
        String SYSTEM = "";
        Properties p = new Properties();
        p.put("doctype-public", PUBLIC);
        p.put("doctype-system", SYSTEM);
        writePresentationParameters(p);

        /*
         * Send a message to the JSPBuilder. The JSPBuilder knows what channel
         * it is responsible for so the relative path in the FileLocation
         * constructor is desirable.
         */
        FileLocation fileLocation = new FileLocation(fileName);
        FileListenerEvent e = new FileListenerEvent(fileLocation);

        jspb.fileAdded(e);

        checkPUBLICAndSYSTEMIds(new File(m_DestinationFolder, fileName), PUBLIC, null);
    }

    public void testEmptyDTDPublicResultsInNoPUBLICElement() throws IOException, FileLocationException,
            ContextPathException {

        JSPBuilder jspb = getJSPBuilder();

        String htmlString = "<html><head><title>testEmptyDTDPublicResultsInNoPUBLICElement</title></head><body></body></html>";

        String fileName = "page-dtd-configured-with-no-public-id.html";
        File f = new File(m_SourceFolder, fileName);
        FileWriter fw = new FileWriter(f);
        fw.write(htmlString);
        fw.close();

        String PUBLIC = "";
        String SYSTEM = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";
        Properties p = new Properties();
        p.put("doctype-public", PUBLIC);
        p.put("doctype-system", SYSTEM);
        writePresentationParameters(p);

        /*
         * Send a message to the JSPBuilder. The JSPBuilder knows what channel
         * it is responsible for so the relative path in the FileLocation
         * constructor is desirable.
         */
        FileLocation fileLocation = new FileLocation(fileName);
        FileListenerEvent e = new FileListenerEvent(fileLocation);

        jspb.fileAdded(e);

        checkPUBLICAndSYSTEMIds(new File(m_DestinationFolder, fileName), null, SYSTEM);
    }

    public void testEmptyDTDPublicAndSystemDoesNotResultInFailure() throws IOException, FileLocationException,
            ContextPathException {

        JSPBuilder jspb = getJSPBuilder();

        String htmlString = "<html><head><title>testEmptyDTDPublicAndSystemDoesNotResultInFailure</title></head><body></body></html>";

        String fileName = "page-dtd-configured-with-no-public-id-and-no-system-id.html";
        File f = new File(m_SourceFolder, fileName);
        FileWriter fw = new FileWriter(f);
        fw.write(htmlString);
        fw.close();

        String PUBLIC = "";
        String SYSTEM = "";
        Properties p = new Properties();
        p.put("doctype-public", PUBLIC);
        p.put("doctype-system", SYSTEM);
        writePresentationParameters(p);

        /*
         * Send a message to the JSPBuilder. The JSPBuilder knows what channel
         * it is responsible for so the relative path in the FileLocation
         * constructor is desirable.
         */
        FileLocation fileLocation = new FileLocation(fileName);
        FileListenerEvent e = new FileListenerEvent(fileLocation);

        jspb.fileAdded(e);

        File newJsp = new File(m_DestinationFolder, fileName);

        if (!newJsp.exists())
            fail("JSPBuilder failed to create the destination file: " + newJsp.getAbsolutePath());
        // No more detailed test than that.
    }

    /**
     * 
     * @param jsp
     * @param publicId
     *            This can be null, in which the test for the publicId will be
     *            skipped.
     * @param systemId
     *            This can be null, in which the test for the systemId will be
     *            skipped.
     * @throws IOException
     */
    private void checkPUBLICAndSYSTEMIds(File jsp, String publicId, String systemId) throws IOException {
        /*
         * Read the the new JSP and check it have the PUBLIC and SYSTEM ids
         */
        if (!jsp.exists())
            fail("JSPBuilder failed to create the destination file: " + jsp.getAbsolutePath());

        BufferedReader b = new BufferedReader(new FileReader(jsp));

        String DOCTYPEDECLSTART = "<!DOCTYPE HTML";

        boolean found = false;
        String lastDOCTYPE = "";
        for (String line = b.readLine(); line != null; line = b.readLine()) {
            // System.out.println(line);

            // [Fragment] is written as part of the outline at the end of the
            // jsp
            // This is not wanted.
            if (line.indexOf("DOCTYPE") != -1 && line.indexOf("[Fragment]") == -1)
                lastDOCTYPE = line;

            if (line.indexOf(DOCTYPEDECLSTART) == -1)
                continue;
            if (publicId != null && line.indexOf(publicId) == -1)
                continue;
            if (systemId != null && line.indexOf(systemId) == -1)
                continue;

            found = true;

            // If a null system id is specified then a zero length system id
            // ("") should not be present, Nothing should be present. The string
            // below
            // escapes the escaping present in the JSP hence the numerousity of
            // the backslashes.
            if (systemId == null && line.indexOf("\\\"\\\"") != -1)
                fail("A null system id did not result in an empty string");

            if (publicId == null) {
                if (line.indexOf(DOCTYPEDECLSTART + " SYSTEM") == -1)
                    fail("A null public id resulted in a DOCTYPE without a PUBLIC id	");
            }

            break;
        }

        assertTrue("The correct DTD was present.\nExpected: '" + publicId + "', '" + systemId + "'\nLast DOCTYPE '"
                + lastDOCTYPE + "'", found);

    }

    /**
     * Test the reaction to bad pages.
     * 
     * 1. For an empty HTML page we do not want a JSP created as it can not be
     * correct.
     * 
     * 2. <add more bad pages>
     */
    public void testBadpages() throws IOException, FileLocationException, ContextPathException {

        JSPBuilder jspb = getJSPBuilder();

        /* the page is bad because it's empty */
        String htmlString = "";

        String fileName = "bad.html";
        File f = new File(m_SourceFolder, fileName);
        FileWriter fw = new FileWriter(f);
        fw.write(htmlString);
        fw.close();

        /*
         * Send a message to the JSPBuilder. The JSPBuilder knows what channel
         * it is responsible for so the relative path in the FileLocation
         * constructor is desirable.
         */
        FileLocation fileLocation = new FileLocation(fileName);
        FileListenerEvent e = new FileListenerEvent(fileLocation);

        jspb.fileAdded(e);

        /*
         * Check the JSP was not created.
         */
        File newJSPFile = new File(m_DestinationFolder, fileName);
        if (newJSPFile.exists())
            fail("JSPBuilder created the destination file from an empty HTML file: " + fileName);

    }

    /**
     * See XLM-616
     * 
     * @throws FileLocationException
     * @throws IOException
     */
    public void testErrorPageIsWrittenWhenMarkupIsIncorrect() throws IOException, FileLocationException {
        JSPBuilder jspb = getJSPBuilder();
        jspb.setDebugLevel(0);

        // This is bad because template are only allowed in _include directories
        String html = "<html><head><title>Bad Markup</title></head><body>"
                + "<!-- template = \"a\" -->T<!-- template = \"/a\" -->" + "</body></html>";

        String fileName = "bad-markup.html";

        String jspString = addFile(jspb, fileName, html);
        // System.out.println("jspString: " + jspString);
        assertErrorPage(jspString);
    }

    /**
     * TODO: See: XLM-???
     * 
     * @throws FileLocationException
     * @throws IOException
     * 
     */
    public void testErrorPageIsWrittenWhenFullPageTemplateIsMissing() throws IOException, FileLocationException {
        JSPBuilder jspb = getJSPBuilder();
        jspb.setDebugLevel(0);

        // This is bad because the full page template is missing
        String html = "<!-- use-template = \"no-such-template\" -->\n" + "<html>\n" + "<head>\n"
                + "<title>Untitled Document</title>\n" + "</head>\n" + "<body>\n" + "</body>\n" + "</html>\n"
                + "<!-- use-template = \"/no-such-template\" -->";

        String fileName = "missing-full-page-template.html";

        String jspString = addFile(jspb, fileName, html);
        System.out.println("jspString: " + jspString);
        assertMissingFullPageTemplatePage(jspString);
    }

    private void assertErrorPage(String pageContent) {
        checkStringsInOrder(new String[] { "<html>", "<head>", "<title>Build Error</title>", "</head>",
                "<body style='background-color: green'>", "<pre>", "</pre>", "</body>", "</html>" }, pageContent);

    }

    private void assertMissingFullPageTemplatePage(String pageContent) {
        checkStringsInOrder(new String[] { "StandardFragment", "setHtml", "<b>Warning: missing template:", "</b>",
                "addChild", "StandardInvocationContext" }, pageContent);

    }

    /**
     * Test that the cache-busting "Pragma", "no-cache" meta tag is added based
     * on the content of application.properties.
     */
    public void testMetaTagAdditionBasedOnApplicationProperties() throws IOException, FileLocationException, Exception,
            ContextPathException {
        JSPBuilder jspb = getJSPBuilder();

        jspb.setDebugLevel(0);

        Properties p = new Properties();
        p.put("browser.cache.disable", "yes");
        writeApplicationParameters(p);

        String htmlString = "<html><head><title>testMetaTagAdditionBasedOnApplicationProperties</title></head><body></body></html>";

        String s = addFile(jspb, "DOM-EDITS-App.html", htmlString);

        // System.out.println("testMetaTagAdditionBasedOnApplicationProperties:\n"
        // + s);

        /*
         * Look for: http-equiv=\"Pragma\" as the string is an argument to a
         * method. Same for next assertion.
         */
        String HTTP_EQUIV = "http-equiv = \\\"Pragma\\\"";
        if (s.indexOf(HTTP_EQUIV) == -1)
            fail(HTTP_EQUIV + " was present");

        String CONTENT = "content = \\\"no-cache\\\"";
        if (s.indexOf(CONTENT) == -1)
            fail(CONTENT + " was present");

    }

    /**
     * Test that a meta tag detection is working for http-equiv="refresh".
     * 
     * The original bug: Matching for meta tags in JSPBuilder is case sensitive.
     * If the head section has this
     * 
     * <meta content="2; URL=course-info.html" http-equiv="Refresh"> then the
     * WPS will add another resulting in <meta content="2; URL=course-info.html"
     * http-equiv="Refresh"> <meta content="1740" http-equiv="refresh">
     * 
     * In IE 4.5 Mac this results in the page not auto-refreshing as desired.
     * 
     * The cause of the bug was not a case insensitive match. JSPBuilder was
     * testing for the meta tag by looking for this attribute
     * 
     * refresh="http-equiv"
     * 
     * instead of the correct attribute
     * 
     * http-equiv="refresh"
     * 
     * This incorrect test always decided that the tag was missing and so added
     * a new one.
     * 
     * The two meta elements that should be present are "refresh" and
     * "content-type"
     */
    public void testRefreshHttpEquivMetaTagDetection() throws IOException, FileLocationException, Exception,
            ContextPathException {
        JSPBuilder jspb = getJSPBuilder();

        jspb.setDebugLevel(0);

        String htmlString = "<html><head><title>testMetaTagDetectionIsCaseInsensitive</title>"
                + "<meta content=\"2; URL=course-info.html\" http-equiv=\"Refresh\">" + "</head><body></body></html>";

        String s = addFile(jspb, "Meta-Tag-Detection-Case-Insensitive.html", htmlString);

        // System.out.println("testMetaTagDetectionIsCaseInsensitive:\n" + s);

        /*
         * Look for: http-equiv
         */
        String HTTP_EQUIV_ATTR_NAME = "http-equiv";
        int firstPos = s.indexOf(HTTP_EQUIV_ATTR_NAME);
        if (firstPos == -1)
            fail(HTTP_EQUIV_ATTR_NAME + " was present at least once");

        String remainder = s.substring(firstPos + HTTP_EQUIV_ATTR_NAME.length());

        // System.out.println("testMetaTagDetectionIsCaseInsensitive:\n" +
        // remainder);

        /*
         * Look for: http-equiv again
         */
        int secondPos = remainder.indexOf(HTTP_EQUIV_ATTR_NAME);
        if (secondPos == -1)
            fail(HTTP_EQUIV_ATTR_NAME + " was present at least twice");

        remainder = remainder.substring(secondPos + HTTP_EQUIV_ATTR_NAME.length());

        /*
         * Look for: http-equiv again
         */
        int thirdPos = remainder.indexOf(HTTP_EQUIV_ATTR_NAME);
        if (thirdPos != -1)
            fail(HTTP_EQUIV_ATTR_NAME + " was present exactly twice");

    }

    /**
     * Jtidy will detect some unknown tags. Check the Jtidy error message is
     * inserted and in the right place.
     * 
     * This test went in when a case sensitivity bug was find. This bug arose
     * when the output was changed from using the entire jtidy output to just
     * using the head section (and the <html> tag at the start.)
     */
    public void testErrorMessageInsertion() throws IOException, FileLocationException, Exception, ContextPathException {
        JSPBuilder jspb = getJSPBuilder();

        jspb.setDebugLevel(0);

        String htmlString = "<html><head><title>testErrorMessageInsertion</title></head><body>007<date_long></body></html>";

        checkStringsInOrder(new String[] { "<html>", "<head>", "testErrorMessageInsertion", "</head>", "<body",
                "<table", "<tr", "<td", "Error: &lt;date_long&gt; is not recognized!", "</body>", "</html>" }, addFile(
                jspb, "jtidy-error-msg.html", htmlString));

        /* Upper case BODY */
        htmlString = "<html><head><title>testErrorMessageInsertion</title></head><BODY>007<date_long></BODY></html>";

        checkStringsInOrder(new String[] { "<html>", "<head>", "testErrorMessageInsertion", "</head>", "<BODY",
                "<table", "<tr", "<td", "Error: &lt;date_long&gt; is not recognized!", "</BODY>", "</html>" }, addFile(
                jspb, "jtidy-error-msg-UPPER.html", htmlString));

    }

    /**
     * Now that the <head> section of a page may be entirely replaced with the
     * <head> section of a template a test is needed the correct edits to the
     * <head> section are being done.
     */
    public void testDOMEditWithHeadFromTemplate() throws IOException, FileLocationException, Exception,
            ContextPathException {

        JSPBuilder jspb = getJSPBuilder();
        jspb.setDebugLevel(0);

        Properties p = new Properties();
        p.put("keywords", "my-keywords");
        p.put("description", "my-description");
        writePresentationParameters(p);

        String templateHtml = "<!-- template = \"full-page\" -->\n"
                + "<html><head><title>Template Page Title</title></head><body>Template Page Body</body></html>\n"
                + "<!-- template = \"/full-page\" -->\n";

        addDirectory(jspb, "_include");
        addFile(jspb, "_include/my-full-page-template.html", templateHtml);

        String htmlString = "<!-- use-template = \"full-page\" -->\n"
                + "<html><head><title>Main Page Title</title></head><body>Main Page Body</body></html>\n"
                + "<!-- use-template = \"/full-page\" -->\n";

        String jspString = addFile(jspb, "uses-full-page-template.html", htmlString);
        // System.out.println( jspString );

        checkStringsInOrder(
                new String[] { "<html>", "<head>", "<title>", "Template Page Title", "</title>", "<meta",
                        "my-keywords", "my-description", "1740", "</head>", "<body", "Template Page Body", "</body>",
                        "</html>" }, jspString);

    }

    public void testApplicatonPropertiesBasedTemplateHeadEdits() throws IOException, FileLocationException {

        JSPBuilder jspb = getJSPBuilder();
        jspb.setDebugLevel(0);

        Properties p = new Properties();
        p.put("browser.cache.disable", "yes");
        writeApplicationParameters(p);

        String templateHtml = "<!-- template = \"full-page\" -->\n"
                + "<html><head><title>Template Page Title</title></head><body>Template Page Body</body></html>\n"
                + "<!-- template = \"/full-page\" -->\n";

        addDirectory(jspb, "_include");
        addFile(jspb, "_include/my-full-page-template.html", templateHtml);

        String htmlString = "<!-- use-template = \"full-page\" -->\n"
                + "<html><head><title>Main Page Title</title></head><body>Main Page Body</body></html>\n"
                + "<!-- use-template = \"/full-page\" -->\n";

        String jspString = addFile(jspb, "uses-full-page-template.html", htmlString);

        checkStringsInOrder(new String[] { "<html>", "<head>", "<title>", "Template Page Title", "</title>", "1740",
                "<meta http-equiv = \\\"Pragma\\\" content = \\\"no-cache\\\" >", "</head>", "<body",
                "Template Page Body", "</body>", "</html>" }, jspString);

    }

    /*
     * This test disabled as making it run is too hard, with too little benefit.
     * 02-9-9 jdb. The test does work.
     */
    public void _testErrorMessagesInsertedWhenEntirePageIsReplacedByATemplate() throws IOException,
            FileLocationException {

        JSPBuilder jspb = getJSPBuilder();
        jspb.setDebugLevel(0);

        String templateHtml = "<!-- template = \"full-page\" -->\n"
                + "<html><head><title>Template Page Title</title></head><body>" + "<date_long>"
                + "Template Page Body</body></html>\n" + "<!-- template = \"/full-page\" -->\n";

        addDirectory(jspb, "_include");
        addFile(jspb, "_include/my-full-page-template.html", templateHtml);

        String htmlString = "<!-- use-template = \"full-page\" -->\n"
                + "<html><head><title>Main Page Title</title></head><body>Main Page Body</body></html>\n"
                + "<!-- use-template = \"/full-page\" -->\n";

        String jspString = addFile(jspb, "uses-full-page-template.html", htmlString);

        checkStringsInOrder(new String[] { "<html>", "<head>", "<title>", "Template Page Title", "</title>", "</head>",
                "<body", "<table border=1 bgcolor=red><tr><td><pre>",
                // opening part of the error message markup
                "</pre></td></tr></table>", // closing part of the error message
                // markup
                "Template Page Body", "</body>", "</html>" }, jspString);
    }

    /**
     * Now that a JSPBuilder can have an encoding associated with it test the
     * default behaviour.
     * 
     * @throws FileLocationException
     * @throws IOException
     */
    public void testDefaultFileEncoding() throws IOException, FileLocationException {

        JSPBuilder jspb = getJSPBuilder();
        jspb.setDebugLevel(0);
        String chars224to230 = "\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6";

        String iso88591Html = "<html><head><title>ISO-8859-1</title></head><body>ISO-8859-1 \u0045\u006E\u0063\u006F\u0064\u0065\u0064 text:"
                + chars224to230 + "</body></html>\n";

        String jspString = addFile(jspb, "iso-8859-1.html", iso88591Html);

        checkStringsInOrder(new String[] { "<html>", "<head>", "<title>", "ISO-8859-1", "</title>", "<body",
                "ISO-8859-1 Encoded text:", chars224to230, "</body>", "</html>" }, jspString);
    }

    private static final String UTF_8 = "UTF-8";

    private static final Charset UTF_8_CS = Charset.forName(UTF_8);

    public void testUTF8FileEncoding() throws IOException, FileLocationException {

        JSPBuilder jspb = getJSPBuilder(Charset.forName(UTF_8));
        jspb.setDebugLevel(0);

        // rock land, a hammer, pelican, FA6A
        String multibyteChars = "\u354B\u3B59\u9DA6\uFA6A";

        String multibyteWhenUTF8Html = "<html><head><title>UTF-8</title></head><body>UTF-8 \u0045\u006E\u0063\u006F\u0064\u0065\u0064 text:"
                + multibyteChars + "</body></html>\n";
        // System.out.println(multibyteWhenUTF8Html);

        String jspString = addFile(jspb, "utf-8.html", multibyteWhenUTF8Html, UTF_8);
        // System.out.println( jspString );

        checkStringsInOrder(new String[] { "<html>", "<head>", "<title>", "UTF-8", "</title>", "<body",
                "UTF-8 Encoded text:", multibyteChars, "</body>", "</html>" }, jspString);
    }

    /**
     * It was discovered that StandardTemplateFinder was not using the encoding.
     * 
     * @throws FileLocationException
     * @throws IOException
     */
    public void testUTF8FileEncodingInTemplates() throws IOException, FileLocationException {
        JSPBuilder jspb = getJSPBuilder(Charset.forName(UTF_8));
        jspb.setDebugLevel(0);

        // rock land, a hammer, pelican, FA6A
        String multibyteChars = "[\u354B\u3B59\u9DA6\uFA6A]";

        String templateUsingPage = "<html><head><title>UTF-8 in Templates</title></head><body>"
                + "Template:<!-- use-template=\"utf-8\" --><!-- use-template =\"/utf-8\" -->" + "</body></html>\n";
        // System.out.println(multibyteWhenUTF8Html);

        String templateHtml = "<html><head><title>UTF-8 Templates</title></head><body>" + "<!-- template=\"utf-8\" -->"
                + multibyteChars + "<!-- template =\"/utf-8\" -->" + "</body></html>";

        addDirectory(jspb, "_include");
        addFile(jspb, "_include/utf-8-template.html", templateHtml, UTF_8);

        String jspString = addFile(jspb, "utf-8-in-templates.html", templateUsingPage, UTF_8);
        System.out.println(jspString);

        checkStringsInOrder(new String[] { "<html>", "<head>", "<title>", "UTF-8 in Templates", "</title>", "<body",
                "Template:", multibyteChars, "</body>", "</html>" }, jspString);
    }

    /**
     * It was discovered that Template was not using the encoding. Because the
     * default encoding on the system development was taking place on had
     * LANG=en_GB.UTF-8 this bug did not become apparent until running the test
     * {@link #testUTF8FileEncodingInTemplates()} on a system with LANG=<empty>.
     * It was also possible to make the test fail by setting
     * LANG=en_GB.ISO-8859-1 on the development machine. To expose this error
     * independent of the platform encoding this ISO-8859-1 based test was
     * added. If the file encoding of the JSPBuilder was not honoured then, if
     * the platform encoding is UTF-8 this test would fail, if the platform
     * encoding is not UTF-8 the test above would fail.
     * 
     * @throws FileLocationException
     * @throws IOException
     */
    public void testISO88591FileEncodingInTemplates() throws IOException, FileLocationException {
        JSPBuilder jspb = getJSPBuilder(Charset.forName(ISO_8859_1));
        jspb.setDebugLevel(0);

        // a grave, a circumflex, a umlaut, c cedilla
        String isoChars = "[\u00E0\u00E2\u00E4\u00E7]";

        String templateUsingPage = "<html><head><title>ISO-8859-1 in Templates</title></head><body>"
                + "Template:<!-- use-template=\"iso-8859-1\" --><!-- use-template =\"/iso-8859-1\" -->"
                + "</body></html>\n";
        // System.out.println(isoChars);

        String templateHtml = "<html><head><title>ISO-8859-1 Templates</title></head><body>"
                + "<!-- template=\"iso-8859-1\" -->" + isoChars + "<!-- template =\"/iso-8859-1\" -->"
                + "</body></html>";

        addDirectory(jspb, "_include");
        addFile(jspb, "_include/iso-88591-1-template.html", templateHtml, ISO_8859_1);

        String jspString = addFile(jspb, "iso-8859-1-in-templates.html", templateUsingPage, ISO_8859_1);
        System.out.println(jspString);

        checkStringsInOrder(new String[] { "<html>", "<head>", "<title>", "ISO-8859-1 in Templates", "</title>",
                "<body", "Template:", isoChars, "</body>", "</html>" }, jspString);
    }

    /*
     * This tests the case where the file that is linked to has been deleted but
     * the link file (and therefore the symlink in sym/) is still present.
     * Compare this with case FLTD in MapperTest.testDeleteTargetFile
     */
    public void testJspDeletedWhenTargetOfLinkIsDeletedThenAddedWhenTargetOfLinkIsAdded()
            throws DirectoryMonitorException, IOException, FileLocationException {

        JSPBuilder jspb = getJSPBuilder();
        final String MASTER_NAME = "master.html";
        final String MASTER_HTML = "<html><head><title></title></head><body></body></html>";
        addFile(jspb, MASTER_NAME, MASTER_HTML);
        final String LINK_NAME = "link.html";
        addSymLink(jspb, MASTER_NAME, LINK_NAME);

        File masterJSPFile = new File(m_DestinationFolder, MASTER_NAME);
        assertTrue("The master jsp was created", masterJSPFile.exists());

        File linkJSPFile = new File(m_DestinationFolder, LINK_NAME);
        assertTrue("The link jsp was created", linkJSPFile.exists());

        /* Reality checkpoint */
        assertEquals(FileUtilities.readFile(masterJSPFile, UTF_8_CS), FileUtilities.readFile(linkJSPFile, UTF_8_CS));

        /*
         * Now delete master and send a targetFileDeleted event, then check both
         * jsps are gone.
         */
        deleteTargetOfLink(jspb, LINK_NAME);
        // The master jsp does not get deleted because deleteTargetOfLink does
        // not send an event to the JSPBuilder.
        // assertTrue("The master jsp was deleted", !masterJSPFile.exists());
        assertTrue("The link jsp was deleted", !linkJSPFile.exists());

        /*
         * Now re-add the deleted target and notify the jsp builder.
         */

        addTargetOfLink(jspb, MASTER_HTML, LINK_NAME, MASTER_NAME);

        assertTrue("The link jsp was recreated when the target of the link was restored", linkJSPFile.exists());

        /* Reality checkpoint */
        assertEquals(FileUtilities.readFile(masterJSPFile, UTF_8_CS), FileUtilities.readFile(linkJSPFile, UTF_8_CS));

    }

    public void _testJspDirectoryDeletedWhenTargetOfLinkIsDeletedThenAddedWhenTargetOfLinkIsAdded() {
        /*
         * To create the fixture for this test: a) Create a directory, b) Create
         * a .link file to this directory (this step create a symbolic link in
         * the link/xx directory), c) Delete the target directory (The output
         * directory DOES NOT get deleted at this point), d) Recreate the target
         * directory (The content of the target directory WILL reappear in the
         * linked directory).
         */
        fail("When the target of a symlink to a directory is deleted or added the directory in the output directory should be deleted and readded.");
        fail("Confirm this test is being added to the correct class");
    }

    /**
     * This test was added because the code coverage report showed that template
     * deletion in {@link JSPBuilder#fileDeletedInternal} was not covered.
     * 
     * @throws FileLocationException
     * @throws IOException
     */
    public void testTemplateArtifactsAreDeletedWhenTemplateDeleted() throws IOException, FileLocationException {

        JSPBuilder jspb = getJSPBuilder(Charset.forName(UTF_8));
        addDirectory(jspb, "_include");
        final String TEMPLATES_HTML = "<html><head><title>Templates</title></head><body>"
                + "<!-- template=\"a\" -->a<!-- template=\"/a\" -->"
                + "<!-- template=\"b\" -->b<!-- template=\"/b\" -->" + "</body></html>";

        final String TEMPLATES_HTML_1 = "<!-- template = \"full-page\" -->\n"
                + "<html><head><title>Template Page Title</title></head><body>" + "<date_long>"
                + "Template Page Body</body></html>\n" + "<!-- template = \"/full-page\" -->\n";

        final String TEMPLATES_HTML_2 = "<!-- template = 'a' -->\n"
                + "<html><head><title>Template Page Title</title></head><body>" + "<date_long>"
                + "Template Page Body</body></html>\n" + "<!-- template = '/a' -->\n";

        final String TEMPLATES_NAME = "_include/templates.html";
        addFile(jspb, TEMPLATES_NAME, TEMPLATES_HTML, UTF_8);

        /* This is a weaker but clearer check than the check done in addFile. */
        File jspIncludeDirectory = new File(m_DestinationFolder, "_include");
        assertEquals(2, jspIncludeDirectory.list().length);

        deleteFile(jspb, TEMPLATES_NAME);
        assertEquals(0, jspIncludeDirectory.list().length);
    }

    /* shared methods */

    private static final String ISO_8859_1 = "ISO-8859-1";

    private String addFile(JSPBuilder jspb, String fileName, String htmlString) throws IOException,
            FileLocationException {
        return addFile(jspb, fileName, htmlString, ISO_8859_1);
    }

    /**
     * Utility to simplify writing, processing and reading of the resultant JSP
     * for a single input file.
     * 
     * @param jspb
     *            The <code>JSPBuilder</code> to use
     * @param fileName
     *            A <code>String</code> for the name of the file, without a
     *            leading slash.
     * @param htmlString
     *            The source HTML, as a designer would upload.
     * @param encoding
     *            The encoding to use when writing and reading the file
     */
    private String addFile(JSPBuilder jspb, String fileName, String htmlString, String encoding) throws IOException,
            FileLocationException {

        Charset charset = Charset.forName(encoding);

        //
        // File f = new File(m_SourceFolder, fileName);
        // OutputStreamWriter w = new OutputStreamWriter(new
        // FileOutputStream(f), charset);
        // w.write(htmlString);
        // w.close();

        writeHtmlToSourceFolder(fileName, htmlString, charset);

        /*
         * Send a message to the JSPBuilder. The JSPBuilder knows what channel
         * it is responsible for so the relative path in the FileLocation
         * constructor is desirable.
         */
        FileLocation fileLocation = new FileLocation(fileName);
        FileListenerEvent e = new FileListenerEvent(fileLocation);

        boolean isTemplate = isTemplate(fileLocation);

        /* /jsp-destination/_include/my-full-page-template.html */
        /* Disallow use when not templates file. */
        File templatesBase = null;
        int preAddTemplateCount = -1;
        if (isTemplate) {
            templatesBase = new File(m_DestinationFolder, fileName);
            preAddTemplateCount = countTemplates(templatesBase);
            assertEquals("No template artifacts existed before fileAdded was invoked", 0, preAddTemplateCount);
        }

        jspb.fileAdded(e);

        /*
         * Template files do not have a 1-1 mapping between source and
         * destination file
         */
        File newJSPFile = new File(m_DestinationFolder, fileName);
        if (!isTemplate) {
            if (!newJSPFile.exists())
                fail("JSPBuilder failed to create the file for source file: " + fileName);
            return FileUtilities.readFile(newJSPFile, charset);
        }

        /*
         * Check that at least one template file was created. It is possible for
         * a template file to contain no templates but that does not happen in
         * these tests.
         */
        int postAddTemplateCount = countTemplates(templatesBase);

        assertTrue("When a template file was added at least one template was created", postAddTemplateCount
                - preAddTemplateCount > 0);

        return "";
    }

    /**
     * Utility to simplify deleting, processing and verifying the removal of the artifacts for a single input file.
     * 
     * @param jspb
     *            The <code>JSPBuilder</code> to use
     * @param fileName
     *            A <code>String</code> for the name of the file, without a
     *            leading slash.
     * @throws FileLocationException 
     */
    private void deleteFile(JSPBuilder jspb, String fileName) throws FileLocationException {

        deleteFileFromSourceFolder(fileName);

        /*
         * Send a message to the JSPBuilder. The JSPBuilder knows what channel
         * it is responsible for so the relative path in the FileLocation
         * constructor is desirable.
         */
        FileLocation fileLocation = new FileLocation(fileName);
        FileListenerEvent e = new FileListenerEvent(fileLocation);

        boolean isTemplate = isTemplate(fileLocation);

        /* /jsp-destination/_include/my-full-page-template.html */
        /* Disallow use when not templates file. */
        File templatesBase = null;
        if (isTemplate) {
            templatesBase = new File(m_DestinationFolder, fileName);
        }

        jspb.fileDeleted(e);

        /*
         * Template files do not have a 1-1 mapping between source and
         * destination file
         */
        File newJSPFile = new File(m_DestinationFolder, fileName);
        if (isTemplate) {
            if (newJSPFile.exists())
                fail("JSPBuilder failed to delete the file for source file: " + fileName);
            return;
        }

        /*
         * Check no template files remain.
         */
        int postDeleteTemplateCount = countTemplates(templatesBase);

        assertEquals("When a template file was deleted all templates were deleted",0, postDeleteTemplateCount);

        fail("Implementation of deleteFile not completed");
    }

    private boolean isTemplate(FileLocation fileLocation){
        return fileLocation.getLocation().indexOf("_include/") != -1;
    }
    
    /**
     * 
     * @param templatesBase
     *            e.g. /jsp-destination/_include/my-full-page-template.html
     * @return The number of templates that exist for this source of templates.
     */
    private int countTemplates(File templatesBase) {
        /* /jsp-destination/_include/my-full-page-template.html */
        assertTrue(!templatesBase.exists());

        /* /jsp-destination/_include */
        File templatesDir = templatesBase.getParentFile();

        final String templatesPrefix = templatesBase.getAbsolutePath() + "#";
        File templates[] = templatesDir.listFiles(new FileFilter() {
            /* /jsp-destination/_include/my-full-page-template.html#full-page.template */
            public boolean accept(File pathname) {
                String absPath = pathname.getAbsolutePath();
                return absPath.startsWith(templatesPrefix) && absPath.endsWith(".template");
            }
        });

        return templates.length;
    }

    /**
     * 
     * @param fileName
     *            A file name relative to the source folder, e.g. "master.html"
     * @param htmlString
     *            The text to write to the file.
     * @param charset
     * @throws IOException
     */
    private void writeHtmlToSourceFolder(String fileName, String htmlString, Charset charset) throws IOException {

        File f = new File(m_SourceFolder, fileName);
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f), charset);
        w.write(htmlString);
        w.close();
    }

    private void writeHtmlToSourceFolder(String fileName, String htmlString) throws IOException {
        Charset charset = Charset.forName("ISO-8859-1");
        writeHtmlToSourceFolder(fileName, htmlString, charset);
    }

    private void writeHtmlToFile(File f, String content) throws IOException {
        Charset charset = Charset.forName("ISO-8859-1");
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f), charset);
        w.write(content);
        w.close();
    }

    private void 
    deleteFileFromSourceFolder(String fileName){
        File f = new File(m_SourceFolder, fileName);
assertTrue("Input file "+fileName+" existed before it was deleted", f.exists());
assertTrue("Input file was in fact a file", f.isFile()) ;
assertTrue(f.delete());
    }

    /**
     * Utility to simplify adding symlinks and notifying the JSP builder of the
     * addition.
     * 
     * @param jspb
     * @param source
     * @param dest
     * @throws FileLocationException
     */
    private void addSymLink(JSPBuilder jspb, String sourceName, String destName) throws FileLocationException {
        File source = new File(m_SourceFolder, sourceName);
        assertTrue(source.exists());
        File dest = new File(m_SourceFolder, destName);
        assertTrue(!dest.exists());
        assertTrue(FileUtilities.makeSymbolicLink(source, dest));

        FileLocation fileLocation = new FileLocation(destName);
        FileListenerEvent e = new FileListenerEvent(fileLocation);

        jspb.fileAdded(e);

    }

    /**
     * Utility to simplify locating and deleting the target of a link file and
     * notifying the JSP builder of the deleted target.
     * 
     * @param jspb
     * @param linkName
     * @throws IOException
     * @throws FileLocationException
     */
    private void deleteTargetOfLink(JSPBuilder jspb, String linkName) throws IOException, FileLocationException {
        File linkFile = new File(m_SourceFolder, linkName);
        assertTrue(linkFile.exists());
        File targetFile = linkFile.getCanonicalFile();
        assertTrue(targetFile.exists());
        assertTrue(!linkFile.getPath().equals(targetFile.getPath()));
        assertTrue(targetFile.delete());

        FileLocation fl = new FileLocation(linkName);
        FileListenerEvent e = new FileListenerEvent(fl);

        jspb.targetFileDeleted(e);
    }

    /**
     * 
     * @param jspb
     * @param targetHtml
     * @param linkName
     * @param targetName
     *            Because linkFile.getCanonicalPath does not return the path to
     *            the target file it is necessary to specify it.
     * @throws IOException
     * @throws FileLocationException
     */
    private void addTargetOfLink(JSPBuilder jspb, String targetHtml, String linkName, String targetName)
            throws IOException, FileLocationException {
        File linkFile = new File(m_SourceFolder, linkName);
        assertTrue(FileUtilities.isSymbolicLinkWithMissingTarget(linkFile));
        File targetFile = new File(m_SourceFolder, targetName);
        assertTrue(!targetFile.exists());
        System.out.println("linkFile.getPath(): " + linkFile.getPath());
        System.out.println("targetFile.getPath(): " + targetFile.getPath());
        assertTrue(!linkFile.getPath().equals(targetFile.getPath()));

        writeHtmlToFile(targetFile, targetHtml);

        FileLocation fl = new FileLocation(linkName);
        FileListenerEvent e = new FileListenerEvent(fl);

        jspb.targetFileAdded(e);
    }

    /**
     * Utility to add a directory and check the addition.
     * 
     * @param jspb
     *            The <code>JSPBuilder</code> to use
     * @param dirName
     *            A <code>String</code> for the name of the directory, without
     *            a leading slash. All parent directories must exist.
     * @param htmlString
     *            The source HTML, as a designer would upload.
     */
    private void addDirectory(JSPBuilder jspb, String dirName) throws IOException, FileLocationException {

        File f = new File(m_SourceFolder, dirName);
        if (!f.mkdir())
            fail("Created directory okay: " + f.getAbsolutePath());

        File newJSPDirectory = new File(m_DestinationFolder, dirName);
        if (!newJSPDirectory.mkdir())
            fail("Created directory okay: " + newJSPDirectory.getAbsoluteFile());

    }

    /**
     * Check the strings in the array of strings occur in the string in order
     * 
     * @param expectedSubstrings
     *            An array of expected substrings
     * @param targetString
     *            The string to check
     */
    private void checkStringsInOrder(String[] expectedSubstrings, String targetString) {
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

    /**
     * Return a general purpose JSPBuilder with an encoding of ISO-8859-1
     */
    private JSPBuilder getJSPBuilder() throws ContextPathException {
        return getJSPBuilder(Charset.forName(ISO_8859_1));
    }

    /**
     * Return a general purpose JSPBuilder
     */
    protected JSPBuilder getJSPBuilder(Charset encoding) throws ContextPathException {
        ContextPath cp = new ContextPath("some-channel");
        DirectoryPair dp = null;
        try {
            dp = new DirectoryPair(m_SourceFolder, m_DestinationFolder);
        } catch (DirectoryPairException e) {
            fail("Failure in fixture: failed to create DirectoryPair.");
        }

        JSPBuilder jspb = new JSPBuilder(cp, dp, encoding);

        jspb.setDebugLevel(0);

        return jspb;
    }

    private void writePresentationParameters(Properties p) throws IOException {
        File ppdir = new File(m_SourceFolder, Constants.getPresentationDirectoryName());
        if (!ppdir.mkdir())
            fail("Failed to create directory: " + ppdir);
        OutputStream os = new FileOutputStream(new File(ppdir, Constants.getPresentationParamFileName()));
        p.store(os, null);
        os.close();
    }

    private void writeApplicationParameters(Properties p) throws IOException {
        File ppdir = new File(m_SourceFolder, Constants.getApplicationDirectoryName());
        if (!ppdir.mkdir())
            fail("Failed to create directory: " + ppdir);
        OutputStream os = new FileOutputStream(new File(ppdir, Constants.getApplicationParamFileName()));
        p.store(os, null);
        os.close();
    }

}
