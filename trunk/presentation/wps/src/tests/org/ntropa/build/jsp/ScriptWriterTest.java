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
 * ScriptWriterTest.java
 *
 * Created on 23 November 2001, 17:56
 */

package tests.org.ntropa.build.jsp;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.html.MarkedUpHtmlException;
import org.ntropa.build.html.MarkedUpHtmlParser;
import org.ntropa.build.jsp.ApplicationFinder;
import org.ntropa.build.jsp.FinderSet;
import org.ntropa.build.jsp.JspSerializable;
import org.ntropa.build.jsp.ScriptWriter;
import org.ntropa.build.jsp.ScriptWriterException;
import org.ntropa.build.jsp.StandardApplicationFinder;
import org.ntropa.build.jsp.StandardFinderSet;
import org.ntropa.utility.CollectionsUtilities;

/**
 * 
 * @author jdb
 * @version $Id: ScriptWriterTest.java,v 1.22 2003/03/19 23:17:16 jdb Exp $
 */
public class ScriptWriterTest extends TestCase {

    public ScriptWriterTest(String testName) {
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

        TestSuite suite = new TestSuite(ScriptWriterTest.class);
        return suite;
    }

    /*
     * protected void setUp () throws Exception, IOException {}
     */

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    /*
     * protected void tearDown () throws Exception {
     */

    /**
     * Make sure the most complex part is generated correctly
     */
    public void testBasicScriptCreation() {

        StandardFinderSet finderSet = new StandardFinderSet();
        Properties p = new Properties();
        p.setProperty("sao.base.class-name", "base.class.name");
        finderSet.setApplicationFinder(new ApplicationPropertyFinder(p));

        String script = getScript("<!-- name=\"base\" --><!-- name=\"/base\" -->", finderSet);
        // System.out.println ( "SCRIPT:\n" + script ) ;

        String expected = "<%!\n" + "private void init1 () {\n" + "component_1 = new base.class.name () ;\n" + "}\n"
                + "%>" + "<%!\n" + "public void jspInit () {\n" + "pageProperties = new java.util.Properties () ;\n"
                + "init1 () ;\n" + "}\n" + "%>";

        if (script.indexOf(expected) < 0)
            fail("The expected jspInit () method was either missing or wrong.\nExpected fragment:\n" + expected
                    + "\nActual script:\n" + script);
    }

    /**
     * Make sure the most complex part is generated correctly
     */
    public void testMoreComplexScriptCreation() {

        StandardFinderSet finderSet = new StandardFinderSet();
        Properties p = new Properties();
        p.setProperty("sao.base.class-name", "base.class.name");
        p.setProperty("sao.child.class-name", "child.class.name");
        finderSet.setApplicationFinder(new ApplicationPropertyFinder(p));

        StringBuffer htmlInput = new StringBuffer();
        htmlInput.append("<!-- name=\"base\" -->");
        htmlInput.append("<!-- name=\"child\" -->");
        htmlInput.append("Hello World!");
        htmlInput.append("<!-- name=\"/child\" -->");
        htmlInput.append("<!-- name=\"/base\" -->");

        String script = getScript(htmlInput.toString(), finderSet);
        // System.out.println ( "SCRIPT:\n" + script ) ;

        // TODO: Revert to ntropa class names.
        if (false) {
            String initExpected = "<%!\n" + "private java.util.Properties pageProperties ;\n"
                    + "private org.ntropa.runtime.sao.AbstractServerActiveObject component_1 ;\n"
                    + "private org.ntropa.runtime.sao.AbstractElement component_2 ;\n"
                    + "private org.ntropa.runtime.sao.AbstractServerActiveObject component_3 ;\n"
                    + "private org.ntropa.runtime.sao.AbstractElement component_4 ;\n"
                    + "private org.ntropa.runtime.sao.Fragment component_5 ;\n" + "%><%!\n"
                    + "public void jspInit () {\n" + "pageProperties = new java.util.Properties () ;\n"
                    + "component_1 = new base.class.name () ;\n"
                    + "component_2 = new org.ntropa.runtime.sao.StandardElement () ;\n"
                    + "component_2.setName ( \"-implicit\" ) ;\n" + "component_1.addChild ( component_2 ) ;\n"
                    + "component_3 = new child.class.name () ;\n" + "component_2.addChild ( component_3 ) ;\n"
                    + "component_4 = new org.ntropa.runtime.sao.StandardElement () ;\n"
                    + "component_4.setName ( \"-implicit\" ) ;\n" + "component_3.addChild ( component_4 ) ;\n"
                    + "component_5 = new org.ntropa.runtime.sao.StandardFragment () ;\n"
                    + "component_5.setHtml ( \"Hello World!\" ) ;\n" + "component_4.addChild ( component_5 ) ;\n"
                    + "}\n" + "%>";
        }

        String initExpected = "<%!\n" + "private java.util.Properties pageProperties ;\n"
                + "private com.studylink.sao.AbstractServerActiveObject component_1 ;\n"
                + "private com.studylink.sao.AbstractElement component_2 ;\n"
                + "private com.studylink.sao.AbstractServerActiveObject component_3 ;\n"
                + "private com.studylink.sao.AbstractElement component_4 ;\n"
                + "private com.studylink.sao.Fragment component_5 ;\n" + "%><%!\n" + "private void init1 () {\n"

                + "component_1 = new base.class.name () ;\n"
                + "component_2 = new com.studylink.sao.StandardElement () ;\n"
                + "component_2.setName ( \"-implicit\" ) ;\n" + "component_1.addChild ( component_2 ) ;\n"
                + "component_3 = new child.class.name () ;\n" + "component_2.addChild ( component_3 ) ;\n"
                + "component_4 = new com.studylink.sao.StandardElement () ;\n"
                + "component_4.setName ( \"-implicit\" ) ;\n" + "component_3.addChild ( component_4 ) ;\n"
                + "component_5 = new com.studylink.sao.StandardFragment () ;\n"
                + "component_5.setHtml ( \"Hello World!\" ) ;\n" + "component_4.addChild ( component_5 ) ;\n"

                + "}\n" + "%><%!\n" + "public void jspInit () {\n" + "pageProperties = new java.util.Properties () ;\n"

                + "init1 () ;\n" + "}\n" + "%>";

        if (script.indexOf(initExpected) < 0)
            fail("The expected jspInit () method was either missing or wrong.\nExpected fragment:\n" + initExpected
                    + "\nActual script:\n" + script);

        // TODO: Revert to ntropa class names.
        if (false) {
            String serveExpected = "<%\n" + "InvocationContext invocationBean = new StandardInvocationContext () ;\n"
                    + "\n" + "invocationBean.setPageContext ( pageContext ) ;\n"
                    + "invocationBean.setHttpSession ( session ) ;\n"
                    + "invocationBean.setServletContext ( application ) ;\n"
                    + "invocationBean.setServletConfig ( config ) ;\n" + "invocationBean.setJspWriter ( out ) ;\n"
                    + "invocationBean.setPage ( page ) ;\n" + "invocationBean.setHttpServletRequest ( request ) ;\n"
                    + "invocationBean.setHttpServletResponse ( response ) ;\n" + "\n"
                    + "/* While in the control phase no output should be written */\n"
                    + "invocationBean.enableControlPhase () ;\n" + "\n"
                    + "org.ntropa.runtime.sao.util.PageProperties.jspService ( invocationBean, pageProperties ) ;\n"
                    + "\n" + "component_1.control ( invocationBean ) ;\n" + "\n"
                    + "if ( invocationBean.getController ().proceed () ) {\n"
                    + "    invocationBean.enableRenderPhase () ;\n" + "    component_1.render ( invocationBean ) ;\n"
                    + "}\n" + "\n" + "invocationBean.disable () ;\n" + "\n" + "component_1.recycle () ;\n" + "%>";

        }

        String serveExpected = "<%\n" + "InvocationContext invocationBean = new StandardInvocationContext () ;\n"
                + "\n" + "invocationBean.setPageContext ( pageContext ) ;\n"
                + "invocationBean.setHttpSession ( session ) ;\n"
                + "invocationBean.setServletContext ( application ) ;\n"
                + "invocationBean.setServletConfig ( config ) ;\n" + "invocationBean.setJspWriter ( out ) ;\n"
                + "invocationBean.setPage ( page ) ;\n" + "invocationBean.setHttpServletRequest ( request ) ;\n"
                + "invocationBean.setHttpServletResponse ( response ) ;\n" + "\n"
                + "/* While in the control phase no output should be written */\n"
                + "invocationBean.enableControlPhase () ;\n" + "\n"
                + "com.studylink.sao.util.PageProperties.jspService ( invocationBean, pageProperties ) ;\n" + "\n"
                + "component_1.control ( invocationBean ) ;\n" + "\n"
                + "if ( invocationBean.getController ().proceed () ) {\n"
                + "    invocationBean.enableRenderPhase () ;\n" + "    component_1.render ( invocationBean ) ;\n"
                + "}\n" + "\n" + "invocationBean.disable () ;\n" + "\n" + "component_1.recycle () ;\n" + "%>";

        if (script.indexOf(serveExpected) < 0)
            fail("The expected serve-time script was either missing or wrong.\nExpected fragment:\n" + serveExpected
                    + "\nActual script:\n" + script);
    }

    /**
     * Test property setters
     */
    public void testPropertySetterCreation() {

        StandardFinderSet finderSet = new StandardFinderSet();
        Properties p = new Properties();
        p.setProperty("sao.base.class-name", "base.class.name");
        p.setProperty("sao.base.prop.errorPage", "../error/index.html");
        p.setProperty("sao.base.prop.webmasterEmail", "webmaster@studylink.com");
        p.setProperty("sao.base.prop.alpha", "the-value-of-alpha\"");
        p.setProperty("sao.base.prop.jspMarkup", "<%%>");
        finderSet.setApplicationFinder(new ApplicationPropertyFinder(p));

        StringBuffer htmlInput = new StringBuffer();
        htmlInput.append("<!-- name=\"base\" -->");
        htmlInput.append("<!-- name=\"/base\" -->");

        String script = getScript(htmlInput.toString(), finderSet);
        // System.out.println ( "SCRIPT:\n" + script ) ;

        String expected = "<%!\n" + "private void init1 () {\n" + "component_1 = new base.class.name () ;\n"
                + "( ( base.class.name ) component_1 ).setAlpha ( \"the-value-of-alpha\\\"\" ) ;\n"
                + "( ( base.class.name ) component_1 ).setErrorPage ( \"../error/index.html\" ) ;\n"
                + "( ( base.class.name ) component_1 ).setJspMarkup ( \"<\" + \"%%\" + \">\" ) ;\n"
                + "( ( base.class.name ) component_1 ).setWebmasterEmail ( \"webmaster@studylink.com\" ) ;\n" + "}\n"
                + "%><%!\n" + "public void jspInit () {\n" + "pageProperties = new java.util.Properties () ;\n"
                + "init1 () ;\n" + "}\n" + "%>";

        if (script.indexOf(expected) < 0)
            fail("The expected jspInit () method was either missing or wrong.\nExpected fragment:\n" + expected
                    + "\nActual script:\n" + script);

    }

    /**
     * Make sure the application properties which are intended to be passed to
     * PageProperties are added to the Properties object.
     */
    public void testApplicationPropertiesAreCollectedForPageProperties() {

        StandardFinderSet finderSet = new StandardFinderSet();
        Properties p = new Properties();
        p.setProperty("proxy.cache.disable", "yes");
        p.setProperty("proxy.cache.unplannedforproperty", "some-value");
        p.setProperty("sao.base.class-name", "base.class.name");
        finderSet.setApplicationFinder(new ApplicationPropertyFinder(p));

        StringBuffer htmlInput = new StringBuffer();
        htmlInput.append("<!-- name=\"base\" -->");
        htmlInput.append("<!-- name=\"/base\" -->");

        String script = getScript(htmlInput.toString(), finderSet);
        // System.out.println ( "SCRIPT:\n" + script ) ;

        String expected = "<%!\n" + "private void init1 () {\n" + "component_1 = new base.class.name () ;\n" + "}\n"
                + "%><%!\n" + "public void jspInit () {\n" + "pageProperties = new java.util.Properties () ;\n"
                + "pageProperties.setProperty ( \"proxy.cache.disable\", \"yes\" ) ;\n" + "init1 () ;\n";

        if (script.indexOf(expected) < 0)
            fail("The expected jspInit () method was either missing or wrong.\nExpected fragment:\n" + expected
                    + "\nActual script:\n" + script);
    }

    /**
     * Test placeholder setter generation
     */
    public void testPlaceholderGeneration() {

        StandardFinderSet finderSet = new StandardFinderSet();
        Properties p = new Properties();
        p.setProperty("sao.base.class-name", "baseClassName");
        finderSet.setApplicationFinder(new ApplicationPropertyFinder(p));

        StringBuffer htmlInput = new StringBuffer();
        htmlInput
                .append("<!-- name=\"base\" placeholder-a=\"value-of-a\" placeholder-b=\"value-of-b\" placeholder-c=\"<%%>\"-->");
        htmlInput.append("<!-- name=\"/base\" -->");

        String script = getScript(htmlInput.toString(), finderSet);
        // System.out.println ( "SCRIPT:\n" + script ) ;

        String expected = "<%!\n" + "private void init1 () {\n" + "component_1 = new baseClassName () ;\n"
                + "component_1.setPlaceholder ( \"a\", \"value-of-a\" ) ;\n"
                + "component_1.setPlaceholder ( \"b\", \"value-of-b\" ) ;\n"
                + "component_1.setPlaceholder ( \"c\", \"<\" + \"%%\" + \">\" ) ;\n" + "}\n" + "%><%!\n"
                + "public void jspInit () {\n" + "pageProperties = new java.util.Properties () ;\n" + "init1 () ;\n"
                + "}\n" + "%>";

        if (script.indexOf(expected) < 0)
            fail("The expected jspInit () method was either missing or wrong.\nExpected fragment:\n" + expected
                    + "\nActual script:\n" + script);

    }

    public void testMoreThanOneSplitterMethodIsCreatedWhenTheInputDocumentIsLarge() {

        StandardFinderSet finderSet = new StandardFinderSet();
        Properties p = new Properties();
        p.setProperty("sao.base.class-name", "base.class.name");
        p.setProperty("sao.child.class-name", "child.class.name");
        finderSet.setApplicationFinder(new ApplicationPropertyFinder(p));

        StringBuilder doc = new StringBuilder();
        final int SAO_COUNT = 200;

        for (int i = 1; i <= SAO_COUNT; i++) {
            doc.append("<!-- name=\"child\" -->" + get1KString(i) + "<!-- name=\"/child\" -->");
        }

        String script = getScript("<!-- name=\"base\" -->" + doc.toString() + "<!-- name=\"/base\" -->", finderSet);

        {
            final String INIT_1_DECL = "private void init1 () {";

            if (script.indexOf(INIT_1_DECL) < 0)
                fail("Init<n> method was missing: " + INIT_1_DECL);
        }
        {
            final String INTI_1_INV = "\ninit1 () ;";
            if (script.indexOf(INTI_1_INV) < 0)
                fail("Init<n> method invocation was missing: " + INTI_1_INV);

        }
        
        {
            final String INIT_2_DECL = "}\nprivate void init2 () {";

            if (script.indexOf(INIT_2_DECL) < 0)
                fail("Init<n> method was missing: " + INIT_2_DECL);
        }
        {
            final String INTI_2_INV = "\ninit2 () ;";
            if (script.indexOf(INTI_2_INV) < 0)
                fail("Init<n> method invocation was missing: " + INTI_2_INV);

        }
        
        {
            final String INIT_3_DECL = "}\nprivate void init3 () {";

            if (script.indexOf(INIT_3_DECL) < 0)
                fail("Init<n> method was missing: " + INIT_3_DECL);
        }
        {
            final String INTI_3_INV = "\ninit3 () ;";
            if (script.indexOf(INTI_3_INV) < 0)
                fail("Init<n> method invocation was missing: " + INTI_3_INV);

        }

    }

    private String get1KString(int i) {
        String s = String.valueOf(i);
        StringBuilder sb = new StringBuilder(1000);
        while (sb.length() < 1000) {
            sb.append(s);
        }
        sb.setLength(1000);
        return sb.toString();
    }

    private final Charset ISO_8859_1_CS = Charset.forName("ISO-8859-1");

    private String getScript(String htmlInput, FinderSet finderSet) {

        MarkedUpHtmlParser parser = new MarkedUpHtmlParser(htmlInput);

        try {
            parser.parse();
            /* We aren't testing template resolution */
            // parser.resolveTemplates ( finderSet.getTemplateFinder () ) ;
        } catch (MarkedUpHtmlException e) {
            fail("There was a problem encountered while parsing the page:\n" + e.getMessage());
        }

        List tree = parser.getGrove();

        if (tree.size() != 1)
            fail("The page was parsed but did not have exactly one root object");

        /*
         * If we need to handle more than one object (which would happen if we
         * stopped using the default ServerActiveHtml which wraps the page) then
         * we need to use a loop here.
         */
        ScriptWriter scWr;
        String script = "";
        try {
            JspSerializable rootObject = (JspSerializable) tree.get(0);
            scWr = new ScriptWriter(rootObject, ISO_8859_1_CS, finderSet);

            // MarkedUpHtmlParser.printObjectTree( tree.get( 0 ) ) ;
            script = scWr.getScript();
        } catch (ScriptWriterException e) {
            fail("Failed to create script from object tree:\n" + e.toString());
        }

        return script;
    }

    private class ApplicationPropertyFinder implements ApplicationFinder {

        Properties _p;

        public ApplicationPropertyFinder(Properties p) {
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
         *   &lt;-- name = &quot;the-name&quot; --&gt;
         *   other elements and HTML
         *   &lt;-- name = &quot;/the-name&quot; --&gt;
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
            String appProp = _p.getProperty("proxy.cache.disable");
            if (appProp == null)
                return false;
            else if (appProp.equals("yes"))
                return true;
            else
                return false;
        }

    }
}
