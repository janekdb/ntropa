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
 * ServerActiveHtmlTest.java
 *
 * Created on 15 October 2001, 16:27
 */

package tests.org.ntropa.build.html;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.html.Element;
import org.ntropa.build.html.Fragment;
import org.ntropa.build.html.MarkedUpHtmlParser;
import org.ntropa.build.html.ServerActiveHtml;
import org.ntropa.build.html.ServerActiveHtmlException;
import org.ntropa.build.jsp.ApplicationFinder;
import org.ntropa.build.jsp.StandardApplicationFinder;
import org.ntropa.build.jsp.StandardFinderSet;
import org.ntropa.utility.CollectionsUtilities;

/**
 * 
 * @author jdb
 * @version $Id: ServerActiveHtmlTest.java,v 1.14 2002/09/06 15:34:35 jdb Exp $
 */
public class ServerActiveHtmlTest extends TestCase {

    final String ANY_NAME = "any-name";

    /** Creates new ServerActiveHtmlTest */
    public ServerActiveHtmlTest(String testName) {
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

        TestSuite suite = new TestSuite(ServerActiveHtmlTest.class);
        return suite;
    }

    public void testConstructors() throws ServerActiveHtmlException {

        ServerActiveHtml sah;

        try {
            sah = new ServerActiveHtml(null);
            fail("Constructor disallowed with null name");
        } catch (IllegalArgumentException e) {
        }

        try {
            sah = new ServerActiveHtml("");
            fail("Constructor disallowed with zero length name");
        } catch (IllegalArgumentException e) {
        }

        try {
            sah = new ServerActiveHtml(ANY_NAME, null);
            fail("Constructor disallowed with null template name");
        } catch (IllegalArgumentException e) {
        }

        try {
            sah = new ServerActiveHtml(ANY_NAME, "");
            fail("Constructor disallowed with zero length name");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testPlaceholders() throws ServerActiveHtmlException {

        ServerActiveHtml sah = new ServerActiveHtml("my-sah");

        Properties p = new Properties();
        p.setProperty("date", "01-11-14");

        sah.setPlaceholders(p);

        assertEquals("The placeholders were not set correctly", p, sah.getPlaceholders());

    }

    /**
     * Check an attempt to add an Element with the same name as an existing
     * Element results in an Exception.
     */
    public void testDuplicateNamedElementDisallowed() throws ServerActiveHtmlException {

        ServerActiveHtml sah = new ServerActiveHtml("news-reel");

        Element element1 = new Element("the-name");
        sah.add(element1);

        Element element2 = new Element("the-name");

        try {
            sah.add(element2);
            fail("Duplicate named Element was rejected");
        } catch (IllegalArgumentException e) {
        }

    }

    public void testClassNameLookup() throws ServerActiveHtmlException {

        ServerActiveHtml sah = new ServerActiveHtml("news-reel");

        Properties props = new Properties();
        props.setProperty("sao.news-reel.class-name", "org.ntropa.runtime.soa.NewsReel");

        StandardFinderSet fs = new StandardFinderSet();
        fs.setApplicationFinder(new _appFinder(props));

        StringBuilder sb = new StringBuilder();
        sah.getSetUpCode("component_1", sb, fs);
        if (sb.toString().indexOf("new org.ntropa.runtime.soa.NewsReel () ;") < 0)
            fail("Setup code omitted object creation");

        /*
         * assertEquals ( "Wrong class name for name",
         * "org.ntropa.runtime.soa.NewsReel", sah.getComponentClassName ( fs ) ) ;
         */

        /* No corressponding class name */
        props = new Properties();

        fs = new StandardFinderSet();
        fs.setApplicationFinder(new _appFinder(props));

        sb = new StringBuilder();
        sah.getSetUpCode("component_1", sb, fs);
        // TODO: Revert to ntropa class names.
        if (false) {
            if (sb.toString().indexOf("new org.ntropa.runtime.sao.BaseServerActiveObject () ;") < 0)
                fail("Setup code omitted object creation");
        }
        if (sb.toString().indexOf("new com.studylink.sao.BaseServerActiveObject () ;") < 0)
            fail("Setup code omitted object creation");

        /*
         * We expect property setters to be added
         */
        props = new Properties();
        props.setProperty("sao.news-reel.class-name", "org.ntropa.runtime.soa.NewsReel");
        props.setProperty("sao.news-reel.prop.help", "http://help.studylink.com:42/guru.jil?kid=1987");
        props.setProperty("sao.news-reel.prop.rapper", "DEL THE FUNKY HOMOSAPIEN");
        props.setProperty("sao.news-reel.prop.dancer", "Leonard \"T\" Grungensten Jr");
        props.setProperty("sao.news-reel.prop.jspScript", "<%=");

        fs = new StandardFinderSet();
        fs.setApplicationFinder(new _appFinder(props));

        sb = new StringBuilder();
        sah.getSetUpCode("component_1", sb, fs);

        String expected = "component_1 = new org.ntropa.runtime.soa.NewsReel () ;\n"
                + "( ( org.ntropa.runtime.soa.NewsReel ) component_1 ).setDancer ( \"Leonard \\\"T\\\" Grungensten Jr\" ) ;\n"
                + "( ( org.ntropa.runtime.soa.NewsReel ) component_1 ).setHelp ( \"http://help.studylink.com:42/guru.jil?kid=1987\" ) ;\n"
                + "( ( org.ntropa.runtime.soa.NewsReel ) component_1 ).setJspScript ( \"<\" + \"%=\" ) ;\n"
                + "( ( org.ntropa.runtime.soa.NewsReel ) component_1 ).setRapper ( \"DEL THE FUNKY HOMOSAPIEN\" ) ;\n";

        assertEquals("The setup code was wrong", expected, sb.toString());
        // System.out.println(sb.toString ());

    }

    /**
     * Test breadth-first search for Element
     */
    public void testFindElement() throws ServerActiveHtmlException {
        /*
         * [junit] [ServerActiveHtml root] [junit] [Element root-1] [junit]
         * [ServerActiveHtml root-1-1] [junit] [Element root-1-1-1] [junit]
         * [Fragment] ROOT 111 CORRECT [junit] [Element root-1-1-2] [junit]
         * [Fragment] ROOT 112 WRONG [junit] [Element root-2] [junit] [Fragment]
         * ROOT 2 CORRECT [junit] [Element root-1-1-2] [junit] [Fragment] ROOT
         * 112 CORRECT [junit] [Element root-3] [junit] [ServerActiveHtml
         * root-3-1] [junit] [Element root-3-1-1] [junit] [Element root-1-1-1]
         * [junit] [Fragment] ROOT 111 WRONG
         */
        /*
         * SAH root ELEMENT root-1 SAH root-1-1 ELEMENT root-1-1-1 <- B: tests
         * breadth-first FRAG "ROOT 111 CORRECT" ELEMENT root-1-1-2 <- A: tests
         * breadth-first FRAG "ROOT 112 WRONG" ELEMENT root-2 <- tests find FRAG
         * "ROOT 2 CORRECT" ELEMENT root-1-1-2 <- A: tests breadth-first FRAG
         * "ROOT 112 CORRECT" ELEMENT root-3 SAH root-3-1 ELEMENT root-3-1-1
         * ELEMENT root-1-1-1 <- B: tests breadth-first FRAG "ROOT 111 WRONG"
         */

        /* In reverse order of above */

        Fragment root111WrongFrag = new Fragment("ROOT 111 WRONG");
        Element root111Wrong = new Element("root-1-1-1");
        root111Wrong.add(root111WrongFrag);

        Element root311 = new Element("root-3-1-1");
        ServerActiveHtml root31 = new ServerActiveHtml("root-3-1");
        root31.add(root311);
        root31.add(root111Wrong);

        Element root3 = new Element("root-3");
        root3.add(root31);

        Fragment root112CorrectFrag = new Fragment("ROOT 112 CORRECT");
        Element root112Correct = new Element("root-1-1-2");
        root112Correct.add(root112CorrectFrag);

        Fragment root2CorrectFrag = new Fragment("ROOT 2 CORRECT");
        Element root2Correct = new Element("root-2");
        root2Correct.add(root2CorrectFrag);

        Fragment root112WrongFrag = new Fragment("ROOT 112 WRONG");
        Element root112Wrong = new Element("root-1-1-2");
        root112Wrong.add(root112WrongFrag);

        Fragment root111CorrectFrag = new Fragment("ROOT 111 CORRECT");
        Element root111Correct = new Element("root-1-1-1");
        root111Correct.add(root111CorrectFrag);

        ServerActiveHtml root11 = new ServerActiveHtml("root-1-1");
        root11.add(root111Correct);
        root11.add(root112Wrong);

        Element root1 = new Element("root-1");
        root1.add(root11);

        ServerActiveHtml root = new ServerActiveHtml("root");
        root.add(root1);
        root.add(root2Correct);
        root.add(root112Correct);
        root.add(root3);

        /*
         * Use this to dump the object tree System.out.println(
         * MarkedUpHtmlParser.objectTreeToString( root ) ) ;
         */

        assertEquals("Non-existent Element was not found", null, root.findElement("no-such-element"));

        Element expected = new Element("root-2");
        expected.add(new Fragment("ROOT 2 CORRECT"));

        assertEquals("root-2 was found", MarkedUpHtmlParser.objectTreeToString(expected), MarkedUpHtmlParser
                .objectTreeToString(root.findElement("root-2")));

        expected = new Element("root-1-1-2");
        expected.add(new Fragment("ROOT 112 CORRECT"));

        assertEquals("root-1-1-2 was found", MarkedUpHtmlParser.objectTreeToString(expected), MarkedUpHtmlParser
                .objectTreeToString(root.findElement("root-1-1-2")));

        expected = new Element("root-1-1-1");
        expected.add(new Fragment("ROOT 111 CORRECT"));

        assertEquals("root-1-1-2 was found", MarkedUpHtmlParser.objectTreeToString(expected), MarkedUpHtmlParser
                .objectTreeToString(root.findElement("root-1-1-1")));

    }

    // -------------------------------------------------------------------------
    // shared methods

    private class _appFinder implements ApplicationFinder {

        Properties _p;

        public _appFinder(Properties p) {
            _p = p;
        }

        /**
         * <p>
         * Return the data to use for a server active object based on it's name
         * or null if the name look up failed.
         * 
         * @param name
         *            A <code>String</code> representing the name of the
         *            ServerActiveHtml object to look up the corressponding data
         *            for. This name is set in the HTML page
         *            <p>
         * 
         * <pre>
         *  &lt;-- name = &quot;the-name&quot; --&gt;
         *  other elements and HTML
         *  &lt;-- name = &quot;/the-name&quot; --&gt;
         * </pre>
         * 
         * </p>
         * 
         * @return A <code>Properties</code> with all the data corresponding
         *         to the given name. If no data is found return null.
         */
        public Properties getSaoData(String name) {
            return CollectionsUtilities.getPropertiesSubset(_p, StandardApplicationFinder
                    .convertSaoNameToPropertyNamePrefix(name));

        }

        /**
         * Return the name of the class to use for this name or null if the name
         * is not in the <code>Properties</code> object.
         * 
         * 
         * @param saoData
         *            A <code>Properties</code> object which may or may not
         *            contain the class name
         * 
         * @return A <code>String</code> representing the name of the
         *         ServerActiveHtml object to look up the corressponding class
         *         name for.
         */
        public String getClazzName(Properties saoData) {
            return saoData.getProperty(ApplicationFinder.CLASS_NAME_PROPNAME);
        }

        /**
         * Return true if the browser should be instructed to not serve pages
         * from it's local cache.
         * 
         * @return True if code should be generated to disable the browser's use
         *         of it's local cache otherwise false.
         */
        public boolean isBrowserCacheDisable() {
            throw new UnsupportedOperationException();
        }

        /**
         * Return true if the proxy should be instructed to not serve pages from
         * it's cache.
         * 
         * @return True if headers should be generated to disable the proxy's
         *         use of it's cache otherwise false.
         */
        public boolean isProxyCacheDisable() {
            return false;
        }
    }

}
