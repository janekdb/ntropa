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
 * ParsedHtmlTest.java
 *
 * Created on 03 October 2001, 16:24
 */

package tests.org.ntropa.build.html;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.ntropa.build.html.ParsedHtml;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author jdb
 * @version $Id: ParsedHtmlTest.java,v 1.10 2006/03/24 11:05:00 jdb Exp $
 */
public class ParsedHtmlTest extends TestCase {

    final String SIMPLE_HTML_PAGE = "<html><head><title>A Simple HTML Page</title></head>\n" + "<body>\n"
            + "<img alt=\"alt-text\" src=\"logo.gif\">\n" + "<ul>\n<li>Item one\n<li>Item two\n<li>Item three\n</ul>\n"
            + "<div align=\"center\">" + "Copyright &copy; 2001 Learning Information Systems" + "</div>"
            + "</body></html>";

    final List<String> SimpleHtmlPageTagList = Arrays.asList(new String[] { "!DOCTYPE", "html", "head", "title",
            "/title", "/head", "body", "img", "ul", "li", "li", "li", "/ul", "div", "/div", "/body", "/html" });

    final String SCRIPT_HTML_PAGE = "<html><head><title>A HTML Page with Script Elements</title>"
            + "<script language=\"Javascript\" src=\"http://www.studylink.com/rollover.js\">" + "</script>"
            + "<script language=\"Javascript\">\n" + "var circle = { x:0, y:0, radius:2 }\n" + "var home = {\n"
            + "       name: \"Homer Simpson\",\n" + "       age: 34,\n" + "       married: true,\n"
            + "       occupation: \"plant operator\",\n" + "       email: homer@simpsons.com\n" + "       };\n"
            + "</script>" + "</head>\n" + "<body>\n<ul>\n<li>Item one\n<li>Item two\n<li>Item three\n</ul>\n"
            + "<div align=\"center\">" + "Copyright &copy; 2001 Learning Information Systems" + "</div>"
            + "</body></html>";

    final List<String> ScriptHtmlPageTagList = Arrays.asList(new String[] { "!DOCTYPE", "html", "head", "title",
            "/title", "script", "/script", "script", "/script", "/head", "body", "ul", "li", "li", "li", "/ul", "div",
            "/div", "/body", "/html" });

    final String SERVER_ACTIVE_HTML_HTML_PAGE = "<html><head><title>A Server Active HTML Page</title></head>\n"
            + "<body>\n<!-- name=\"news\" --><!-- element=\"header\" --><ul>\n<!-- element=\"/header\" -->"
            + "<!-- element=\"news-item\" placeholder=\"news-item:Item one\" -->" + "<li>Item one\n"
            + "<!-- element=\"/news-item\" -->"
            + "<!-- element=\"footer\" --></ul>\n<!-- element=\"/footer\" --><!-- name=\"/news\" -->\n"
            + "<div align=\"center\">" + "Copyright &copy; 2001 Learning Information Systems" + "</div>"
            + "</body></html>";

    final List<String> ServerActiveHtmlHtmlPageTagList = Arrays.asList(new String[] { "!DOCTYPE", "html", "head",
            "title", "/title", "/head", "body", "!--", "!--", "ul", "!--", "!--", "li", "!--", "!--", "/ul", "!--",
            "!--", "div", "/div", "/body", "/html" });

    final String TITLELESS_HTML_PAGE = "<html><head></head><body></body></html>";

    final List<String> titlelessHtmlPageTagList = Arrays.asList(new String[] { "!DOCTYPE", "html", "head", "/head",
            "body", "/body", "/html" });

    public ParsedHtmlTest(String testName) {
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

        TestSuite suite = new TestSuite(ParsedHtmlTest.class);
        return suite;
    }

    /*
     * protected void setUp () throws Exception, IOException { }
     */

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    /*
     * protected void tearDown () throws Exception {
     * 
     * FileUtilities.killDirectory ( m_TopFolder ) ; }
     */

    /*
     * Sanity check Make sure Jakarta-ORO is still behaving in the same way as
     * the first time it was added to the project.
     */
    public void testJakartaORO() {
        PatternMatcher matcher;
        PatternCompiler compiler;
        Pattern pattern = null;
        PatternMatcherInput input;
        MatchResult result;

        compiler = new Perl5Compiler();
        matcher = new Perl5Matcher();

        String somePatternString = "\\w*";
        try {
            pattern = compiler.compile(somePatternString);
        } catch (MalformedPatternException e) {
            fail(e.toString());
        }
        String someStringInput = "Larry Curly Moe abc";
        // someStringInput = "10" ;

        input = new PatternMatcherInput(someStringInput);

        String expectedMatchList = "Larry/0/5/Curly/6/11/Moe/12/15/abc/16/19/";
        String actualMatchList = "";
        while (matcher.contains(input, pattern)) {
            result = matcher.getMatch();
            // Perform whatever processing on the result you want.
            // Here we just print out all its elements to show how its
            // methods are used.

            actualMatchList += result.toString() + "/" + result.beginOffset(0) + "/" + result.endOffset(0) + "/";

        }
        assertEquals("The match failed", expectedMatchList, actualMatchList);

    }

    /**
     * Exercise toString () method. Does not test returned value.
     */
    public void testBasicUse() throws SAXException {

        String html = "";

        ParsedHtml ph = new ParsedHtml(SIMPLE_HTML_PAGE);
        html = "" + ph;
        // System.out.println ("ParsedHtml.toString () :\n" + html );
        compareTagOrder(html, SimpleHtmlPageTagList);

        ph = new ParsedHtml(SCRIPT_HTML_PAGE);
        html = "" + ph;
        // System.out.println ("ParsedHtml.toString () :\n" + html );
        compareTagOrder(html, ScriptHtmlPageTagList);

        ph = new ParsedHtml(SERVER_ACTIVE_HTML_HTML_PAGE);
        html = "" + ph;
        // System.out.println ("ParsedHtml.toString () :\n" + html );
        compareTagOrder(html, ServerActiveHtmlHtmlPageTagList);

    }

    /**
     * Use a ParsedHtml with a errorStream.
     */
    public void testErrorStream() throws SAXException {

        String BROKEN_HTML = "<html><date_long></html>";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter errStream = new PrintWriter(baos);

        ParsedHtml ph = new ParsedHtml(BROKEN_HTML, errStream);
        // errStream.flush () ;
        if (baos.size() > 0) {
            // System.out.println ( "[testErrorStream] BROKEN_HTML: there were
            // warnings\n" + baos ) ;
        } else
            fail("[testErrorStream] BROKEN_HTML: no warnings");

        baos.reset();

        ph = new ParsedHtml(SIMPLE_HTML_PAGE, errStream);
        if (baos.size() > 0) {
            fail("[testErrorStream] SIMPLE_HTML_PAGE: there were warnings\n" + baos);
        } else
            // System.out.println ( "[testErrorStream] SIMPLE_HTML_PAGE: no
            // warnings" ) ;
            ;
    }

    /*
     * Added for XLM-241.
     * 
     * This bogus DTD was being removed
     * 
     * <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN"
     * "http://www.w3.org/TR/WD-html-in-xml/DTD/xhtml1-strict.dtd">
     * 
     * This test established that DTD was not being produced here on
     * bonecracker, NAVDEV and NAVTEST, but it was being produced on XENOLITH.
     * Presumably this is because of an older version of XERCES on XENOLITH.
     */
    public void _testDTDHasNotChangedSinceLastPrintedToStdout() throws SAXException {

        ParsedHtml ph = new ParsedHtml(SIMPLE_HTML_PAGE);
        String html = ph.toString();
        // System.out.println("ParsedHtml.toString () :\n" + html);

        String PUBLIC_1 = "\"-//W3C//DTD HTML 4.01//EN\"";
        String SYSTEM_1 = "\"http://www.w3.org/TR/html4/strict.dtd\">";

        assertTrue("PUBLIC ID was sane", html.indexOf(PUBLIC_1) != -1);
        assertTrue("SYSTEM ID was same", html.indexOf(SYSTEM_1) != -1);

    }

    public void testConfigurationOfDTD() throws SAXException {

        ParsedHtml ph = new ParsedHtml(SIMPLE_HTML_PAGE);

        String PUBLIC = "-//MS//DTD HTML 5.01//EN";
        String SYSTEM = "http://dtd.microsoft/UNI/html5/office-plus.dtd";

        ph.setPublicId(PUBLIC);
        ph.setSystemId(SYSTEM);

        // This is required to allow the configured doctype to take effect.
        // In JSPBuilder it is DOMEditor that does this work.
        removeDoctype(ph);

        String html = ph.toString();
        // System.out.println("ParsedHtml.toString () :\n" + html);
        assertTrue("The PUBLIC id was present", html.indexOf(PUBLIC) != -1);
        assertTrue("The SYSTEM id was present", html.indexOf(SYSTEM) != -1);

    }

    public void testConfigurationOfDTDWithNulls() {

    }

    public void testEmptyPageIsParsed() throws SAXException {
        ParsedHtml ph = new ParsedHtml("");
        assertEquals("", ph.toString()) ;
    }

    public void testTitleElementNotDefaulted() throws SAXException {
        ParsedHtml ph = new ParsedHtml(TITLELESS_HTML_PAGE);

        String html = "" + ph;
        System.out.println("ParsedHtml.toString () :\n" + html);
        compareTagOrder(html, titlelessHtmlPageTagList);

    }

    public void testEmptyTitleElementIsRetained() {
        fail("Not finished");
    }


    public void testReminder() {
        fail("        consider passing only the head to this class in JSPBuilder to reduce parse time.");
    }

    private void removeDoctype(ParsedHtml ph) {
        Document doc = ph.getDOM();
        doc.removeChild(doc.getDoctype());
        ph.setDOM(doc);
    }

    /*
     * Shared methods
     */

    /*
     * Extract the tags and compare to a supplied list of tags.
     */
    protected void compareTagOrder(String HTMLPage, List tagList) {
        int groups;
        PatternMatcher matcher;
        PatternCompiler compiler;
        Pattern pattern = null;
        PatternMatcherInput input;
        MatchResult result;

        compiler = new Perl5Compiler();
        matcher = new Perl5Matcher();

        // Worked:
        // <(\\w*)>
        // <(/?\\w*)([^>]*)>
        String patternString = "<(!--|!DOCTYPE|/?\\w*)([^>]*)>";

        try {
            pattern = compiler.compile(patternString);
        } catch (MalformedPatternException e) {
            System.out.println("Bad pattern.");
            System.out.println(e.getMessage());
            fail("Failed to create test fixture.");
        }

        input = new PatternMatcherInput(HTMLPage);

        List actualTagList = new LinkedList();

        while (matcher.contains(input, pattern)) {
            result = matcher.getMatch();

            // This is the entire matched string.
            // System.out.println ("Match: " + result.toString ());
            groups = result.groups();

            /*
             * Sample output
             * 
             * Match: <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN"
             * "http://www.w3.org/TR/WD-html-in-xml/DTD/xhtml1-strict.dtd">
             * Number of Groups: 3 Group index 1: Group index 2: !DOCTYPE HTML
             * PUBLIC "-//W3C//DTD HTML 4.0//EN"
             * "http://www.w3.org/TR/WD-html-in-xml/DTD/xhtml1-strict.dtd"
             * 
             * Match: <html> Number of Groups: 3 Group index 1: html Group index
             * 2:
             * 
             * Match: </title> Number of Groups: 3 Group index 1: /title Group
             * index 2:
             * 
             * Match: <script src="http://www.studylink.com/rollover.js"
             * language="Javascript" type="text/javascript"> Number of Groups: 3
             * Group index 1: script Group index 2:
             * src="http://www.studylink.com/rollover.js" language="Javascript"
             * type="text/javascript"
             * 
             */

            /*
             * group 0 is the entire matched string. We are interested in the
             * tag name which is group 1
             * 
             */
            if (groups > 1) {
                String tag = result.group(1);
                actualTagList.add(tag);

                // System.out.println ("Number of Groups: " + groups);

                // Start at 1 because we just printed out group 0
                /*
                 * for(int group = 1; group < groups; group++) {
                 * System.out.println ("Group index " + group + ": " +
                 * result.group (group)); //System.out.println ("Begin: " +
                 * result.begin (group)); //System.out.println ("End: " +
                 * result.end (group)); }
                 */
            }

        }

        // Look for mismatch
        if (!tagList.equals(actualTagList)) {

            Iterator it;
            it = tagList.iterator();
            while (it.hasNext()) {
                String tag = (String) it.next();
                System.out.println("Expected: " + tag);
            }

            it = actualTagList.iterator();
            while (it.hasNext()) {
                String tag = (String) it.next();
                System.out.println("Actual: " + tag);
            }

            fail("The list of tags from the parsed document did not match the list of expected tags.");
        }
    }
    /*
     * Test <script></script> elements are preserved
     */

    /*
     * Example code ofr using the jakarta-oro library.
     */
    /*
     * protected boolean testORO ( ) { int groups; PatternMatcher matcher;
     * PatternCompiler compiler; Pattern pattern; PatternMatcherInput input;
     * MatchResult result;
     * 
     * compiler = new Perl5Compiler (); matcher = new Perl5Matcher ();
     * 
     * String somePatternString = "\\w*" ; try { pattern = compiler.compile
     * (somePatternString); } catch(MalformedPatternException e) {
     * System.out.println ("Bad pattern."); System.out.println (e.getMessage
     * ()); return false; } String someStringInput = "Larry Curly Moe abc" ;
     * //someStringInput = "10" ;
     * 
     * input = new PatternMatcherInput (someStringInput);
     * 
     * while(matcher.contains (input, pattern)) { result = matcher.getMatch (); //
     * Perform whatever processing on the result you want. // Here we just print
     * out all its elements to show how its // methods are used.
     * 
     * System.out.println ("Match: " + result.toString ()); System.out.println
     * ("Length: " + result.length ()); groups = result.groups ();
     * System.out.println ("Groups: " + groups); System.out.println ("Begin
     * offset: " + result.beginOffset (0)); System.out.println ("End offset: " +
     * result.endOffset (0));
     * 
     * 
     * if ( groups > 1 ) { System.out.println ("Saved Groups: "); // Start at 1
     * because we just printed out group 0 for(int group = 1; group < groups;
     * group++) { System.out.println (group + ": " + result.group (group));
     * System.out.println ("Begin: " + result.begin (group)); System.out.println
     * ("End: " + result.end (group)); } } else { System.out.println ("No
     * Additional Saved Groups"); } } return false ; }
     */
}
