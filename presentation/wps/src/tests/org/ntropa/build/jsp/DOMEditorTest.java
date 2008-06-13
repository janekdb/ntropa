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
 * DOMEditorTest.java
 *
 * Created on 09 September 2002, 14:05
 */

package tests.org.ntropa.build.jsp;

import java.util.Iterator;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.html.MarkedUpHtmlException;
import org.ntropa.build.html.ParsedHtml;
import org.ntropa.build.jsp.DOMEditor;
import org.ntropa.build.jsp.PresentationFinder;
import org.ntropa.utility.DOMUtils;
import org.ntropa.utility.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * 
 * @author jdb
 * @version $Id: DOMEditorTest.java,v 1.3 2006/03/22 16:31:19 jdb Exp $
 */
public class DOMEditorTest extends TestCase implements Logger {

    public DOMEditorTest(String testName) {
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

        TestSuite suite = new TestSuite(DOMEditorTest.class);
        return suite;
    }

    public void testPlaceholdersAddedWhenTagsMissing() throws SAXException, MarkedUpHtmlException {

        String htmlString = "<html><head><title>Hi</title></head><body></body></html>";

        /*
         * 1. Create DOM tree
         */
        ParsedHtml parsedHtml = new ParsedHtml(htmlString);
        Node actualDom = parsedHtml.getDOM();

        DOMEditor domEditor = new DOMEditor(this);

        /*
         * Properties p = new Properties () ; p.setProperty ( "keywords", "Study
         * Abroad" ) ; p.setProperty ( "description", "Study Abroad with
         * StudyLink" ) ;
         * 
         * StandardFinderSet finderSet = new StandardFinderSet () ;
         * 
         * //finderSet.setApplicationFinder ( ) ;
         * finderSet.setPresentationFinder ( new MyPresentationFinder ( p ) ) ;
         */

        domEditor.insertPlaceholders(actualDom);

        String expectedHtml = "<html><head>" + "<title>Hi</title>" + "<!--meta-placeholder:content-type-tag-->"
                + "<!--meta-placeholder:keywords-tag-->" + "<!--meta-placeholder:description-tag-->"
                + "<!--meta-placeholder:refresh-tag-->" + "<!--meta-placeholder:cache-control-tag-->"
                + "</head><body></body></html>";

        Node expectedDom = (new ParsedHtml(expectedHtml)).getDOM();

        assertEquals("A placeholder was added for each missing tag", DOMUtils.toString(expectedDom), DOMUtils
                .toString(actualDom));

    }

    public void testHttpEquivContentTypePlaceholderAddedWhenTagNotMissing() throws SAXException, MarkedUpHtmlException {

        String htmlString = "<html><head>" + "<meta http-equiv='content-type' content='text/html;charset=ISO-8859-1'>"
                + "<title>Hi</title></head><body></body></html>";

        /*
         * 1. Create DOM tree
         */
        ParsedHtml parsedHtml = new ParsedHtml(htmlString);
        Node actualDom = parsedHtml.getDOM();

        DOMEditor domEditor = new DOMEditor(this);

        domEditor.insertPlaceholders(actualDom);

        String expectedHtml = "<html><head>" + "<title>Hi</title>" + "<!--meta-placeholder:content-type-tag-->"
                + "<!--meta-placeholder:keywords-tag-->" + "<!--meta-placeholder:description-tag-->"
                + "<!--meta-placeholder:refresh-tag-->" + "<!--meta-placeholder:cache-control-tag-->"
                + "</head><body></body></html>";

        Node expectedDom = (new ParsedHtml(expectedHtml)).getDOM();

        assertEquals("A placeholder was added for content-type, replacing the existing tag", DOMUtils
                .toString(expectedDom), DOMUtils.toString(actualDom));

    }

    public void _testHttpEquivContentTypePlaceholderInsertedAsEarlyAsPossible() throws SAXException,
            MarkedUpHtmlException {
        // TODO
        // The intention of this test to to check that the content-type meta
        // element appears as early as possible to signal the page encoding as
        // early as possible.
        fail("not written because meta elements must come after title and base elements and that was too tricky to do today");
    }

    public void _not_finished_testProductionIndexPageThatFailedToAddContentTypeTag() throws SAXException,
            MarkedUpHtmlException {

        String htmlString = "<html>\n" + "<head>\n"
                + "<title>StudyLink. For students wanting an international education</title>\n"
                + "<script type=\"text/javascript\" src=\"http://www.google-analytics.com/urchin.js\"></script>\n"
                + "<script type=\"text/javascript\">_uacct = \"UA-305694-1\";\n" + "urchinTracker();\n" + "</script>\n"
                + "<script type=\"text/javascript\" language=\"JavaScript\" src=\"/scripts/pop-up.js\"></script>\n"
                + "\n"
                + "<script type=\"text/javascript\" language=\"JavaScript\" src=\"/scripts/checklist.js\"></script>\n"
                + "<script type=\"text/javascript\" language=\"JavaScript\" src=\"/scripts/bookmark.js\"></script>\n"
                + "<link type=\"text/css\" href=\"/css/global.css\" rel=\"stylesheet\">\n" + "\n"
                + "<!-- the directory.css stylesheet is not used outside of the virtual directories -->\n" + "\n"
                + "\n" + "\n" + "</head>\n";
        // <meta http-equiv =" refresh" content =" 1740" >
        /*
         * 1. Create DOM tree
         */
        ParsedHtml parsedHtml = new ParsedHtml(htmlString);
        Node actualDom = parsedHtml.getDOM();

        DOMEditor domEditor = new DOMEditor(this);

        domEditor.insertPlaceholders(actualDom);

        String expectedHtml = "<html><head>"
                + "<title>StudyLink. For students wanting an international education</title>"
                + "<!--meta-placeholder:content-type-tag-->" + "<!--meta-placeholder:keywords-tag-->"
                + "<!--meta-placeholder:description-tag-->" + "<!--meta-placeholder:refresh-tag-->"
                + "<!--meta-placeholder:cache-control-tag-->" + "</head><body></body></html>";

        Node expectedDom = (new ParsedHtml(expectedHtml)).getDOM();

        assertEquals("A placeholder was added for content-type, replacing the existing tag", DOMUtils
                .toString(expectedDom), DOMUtils.toString(actualDom));

    }

    public void testDTDIsRemoved() throws SAXException, MarkedUpHtmlException {

        String htmlString = "<html><head><title>Hi</title></head><body></body></html>";

        /*
         * 1. Create DOM tree
         */
        ParsedHtml parsedHtml = new ParsedHtml(htmlString);
        Node actualDom = parsedHtml.getDOM();

        DOMEditor domEditor = new DOMEditor(this);

        domEditor.removeDoctype((Document) actualDom);

        assertEquals("The document type node was removed", null, ((Document) actualDom).getDoctype());

    }

    public void log(String message, Exception e) {
        fail("No messages were logged: " + message + "\n" + e.toString());
    }

    public void log(String message) {
        fail("No messages were logged: " + message + "\n");
    }

    private class MyPresentationFinder implements PresentationFinder {

        private Properties _map;

        public MyPresentationFinder(Properties map) {
            _map = new Properties();
            Iterator it = map.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                String value = map.getProperty(key);
                _map.setProperty(key, value);
            }
        }

        /**
         * 
         * @return A <code>String</code> with the keywords for the current
         *         page
         */
        public String getKeywords() {
            return _map.getProperty("keywords");
        }

        /**
         * 
         * @return A <code>String</code> with the page description for the
         *         current page
         */
        public String getDescription() {
            return _map.getProperty("description");
        }

        public String getDoctypePublicId() {
            throw new UnsupportedOperationException();
        }

        public String getDoctypeSystemId() {
            throw new UnsupportedOperationException();
        }

    }
}
