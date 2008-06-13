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
 * MarkedUpHtmlParserTest.java
 *
 * Created on 09 November 2001, 17:49
 */

package tests.org.ntropa.build.html;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.html.Deserializable;
import org.ntropa.build.html.Element;
import org.ntropa.build.html.Fragment;
import org.ntropa.build.html.MarkedUpHtmlException;
import org.ntropa.build.html.MarkedUpHtmlParser;
import org.ntropa.build.html.Placeholder;
import org.ntropa.build.html.ServerActiveHtml;
import org.ntropa.build.html.ServerActiveHtmlException;
import org.ntropa.build.jsp.TemplateFinder;
import org.ntropa.build.jsp.TemplateFinderException;




/**
 *
 * @author  jdb
 * @version $Id: MarkedUpHtmlParserTest.java,v 1.22 2002/11/01 23:54:23 jdb Exp $
 */
public class MarkedUpHtmlParserTest extends TestCase {
    
    
    private static final int FRAGMENT = 1 ;
    private static final int SERVER_ACTIVE_HTML = 2 ;
    private static final int ELEMENT = 3 ;
    
    /* SAH = Server Active HTML */
    
    private static int _nextName = 1 ;
    
    private HtmlFactory htmlFactory = new HtmlFactory() ;
    
    private Random _r = new Random( 18443 ) ;
    
    private List _expectedObjectTree ;
    
    /** Creates new MarkedUpHtmlParserTest */
    public MarkedUpHtmlParserTest( String testName ) {
        super(testName);
    }
    
    
    /* Comments copied from junit.framework.TestSuite. */
    
    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests.
     * It runs a collection of test cases.
     *
     * This constructor creates a suite with all the methods
     * starting with "test" that take no arguments.
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite( MarkedUpHtmlParserTest.class );
        return suite;
    }
    
    /*
    protected void setUp () throws Exception {}
     */
    
    /*
    protected void tearDown () throws Exception {}
     */
    
    /*
     * Delete this method
     */
    public void _testTemplateResolutionInFullPageTemplatePromoted() {
        testTemplateResolutionInFullPageTemplate() ;
    }
    
    /**
     * Check the values of keys are case insensitive.
     */
    public void testCaseInsensitive() throws ServerActiveHtmlException {
        
        String MIXED_CASE =
        "<!-- name=\"MIXED-case\" placeholder-DATE=\"14-Nov-2001\" PLACEholder-op-cnt=\"2000\" -->" +
        "<!-- Element=\"MIXED-case-EL\" placeholder-DATE=\"14-Nov-2001\" PLACEholder-op-cNt=\"2000\" -->" +
        "<!-- eleMENT=\"/mixed-CASE-el\" -->" +
        "<!-- naMe=\"/mixed-CASE\" -->" ;
        
        ServerActiveHtml expectedSah = new ServerActiveHtml( "mixed-case" );
        expectedSah.setPlaceholder( "date", "14-Nov-2001" ) ;
        expectedSah.setPlaceholder( "op-cnt", "2000" ) ;
        
        Element expectedElement = new Element( "mixed-case-el" ) ;
        expectedElement.setPlaceholder( "date", "14-Nov-2001" ) ;
        expectedElement.setPlaceholder( "op-cnt", "2000" ) ;
        
        expectedSah.add( expectedElement ) ;
        
        MarkedUpHtmlParser p = new MarkedUpHtmlParser( MIXED_CASE ) ;
        
        try {
            p.parse() ;
        }
        catch ( MarkedUpHtmlException e ) {
            fail( "parse () Exception: " + e.toString() ) ;
        }
        
        ServerActiveHtml actualSah = (ServerActiveHtml) p.getGrove().get( 0 ) ;
        /*
        MarkedUpHtmlParser.printObjectTree ( actualSah ) ;
        MarkedUpHtmlParser.printObjectTree ( expectedSah ) ;
         */
        
        assertEquals(
        "The deserialized ServerActiveHtml object was wrong",
        expectedSah,
        actualSah
        ) ;
        
        /* This was already tested via recursion above but hey... */
        assertEquals(
        "The deserialized Element object was wrong",
        expectedElement,
        actualSah.getElement( "mixed-case-el" )
        ) ;
        
    }
    
    /**
     * Check the placeholders are deserialized correctly.
     */
    public void testPlaceholders() throws ServerActiveHtmlException {
        
        String SAH_WITH_PLACEHOLDERS =
        "<html>" +
        "<!-- name=\"it-has-placeholders\" placeholder-date=\"14-Nov-2001\" placeholder-op-cnt=\"2000\" -->" +
        "<!-- name=\"/it-has-placeholders\" -->" +
        "</html>" ;
        
        ServerActiveHtml expectedSah = new ServerActiveHtml( "it-has-placeholders" );
        expectedSah.setPlaceholder( "date", "14-Nov-2001" ) ;
        expectedSah.setPlaceholder( "op-cnt", "2000" ) ;
        
        MarkedUpHtmlParser p = new MarkedUpHtmlParser( SAH_WITH_PLACEHOLDERS ) ;
        
        try {
            p.parse() ;
        }
        catch ( MarkedUpHtmlException e ) {
            fail( "parse () Exception: " + e.toString() ) ;
        }
        
        ServerActiveHtml actualSah = (ServerActiveHtml) p.getGrove().get( 1 ) ;
        
        assertEquals( "The deserialized ServerActiveHtml object was wrong", expectedSah, actualSah ) ;
        
        /*
        System.out.println ("testPlaceholders:\n" + expectedSah );
        System.out.println ("testPlaceholders:\n" + expectedSah.getPlaceholders () );
        System.out.println ("testPlaceholders:\n" + actualSah );
        System.out.println ("testPlaceholders:\n" + actualSah.getPlaceholders () );
         */
        
        /* Add an Element with placeholders */
        
        String ELEMENT_WITH_PLACEHOLDERS =
        "<html>" +
        "<!-- name=\"it-has-placeholders\" placeholder-date=\"14-Nov-2001\" placeholder-op-cnt=\"2000\" -->" +
        "<!-- element=\"more-placeholders\" placeholder-starT=\"Egypt\" placeholder-End=\"Africa\"-->" +
        "<!-- -->" +
        "<!-- element=\"/more-placeholders\" -->" +
        "<!-- name=\"/it-has-placeholders\" -->" +
        "</html>" ;
        
        Element expectedElement = new Element( "more-placeholders" ) ;
        expectedElement.setPlaceholder( "start", "Egypt" ) ;
        expectedElement.setPlaceholder( "end", "Africa" ) ;
        expectedElement.add( new Fragment( "<!-- -->" ) ) ;
        
        expectedSah.add( expectedElement ) ;
        
        p = new MarkedUpHtmlParser( ELEMENT_WITH_PLACEHOLDERS ) ;
        
        try {
            p.parse() ;
        }
        catch ( MarkedUpHtmlException e ) {
            fail( "parse () Exception: " + e.toString() ) ;
        }
        
        actualSah = (ServerActiveHtml) p.getGrove().get( 1 ) ;
        
        assertEquals( "The deserialized Element object was probably wrong", expectedSah, actualSah ) ;
        
    }
    
    
    public void testPages() throws ServerActiveHtmlException {
        MarkedUpHtmlParser p ;
        
        String SIMPLE_SERVER_ACTIVE_HTML = "<!-- name=\"news\" -->\nAA\nBB\n<!-- name=\"/news\" -->" ;
        
        List expectedObjectTree = new LinkedList() ;
        
        ServerActiveHtml sah = new ServerActiveHtml( "news" ) ;
        sah.add( new Fragment( "\nAA\nBB\n" ) ) ;
        expectedObjectTree.add( sah ) ;
        
        checkObjectTree( "SIMPLE_SERVER_ACTIVE_HTML", expectedObjectTree, SIMPLE_SERVER_ACTIVE_HTML ) ;
        
        /* Same as above but with superfluous spaces */
        String EXTRA_SPACES = "<!-- name = \"news\" -->\nAA\nBB\n<!-- name=  \"/news\" -->" ;
        checkObjectTree( "EXTRA_SPACES", expectedObjectTree, EXTRA_SPACES ) ;
        
        
        /* Two SAHs */
        String TWO_SERVER_ACTIVE_HTML =
        "<!-- name=\"sah-1\" -->Value 1<!-- name=\"/sah-1\" -->" +
        "<!-- name=\"sah-2\" -->Value 2<!-- name=\"/sah-2\" -->" ;
        
        expectedObjectTree = new LinkedList() ;
        
        sah = new ServerActiveHtml( "sah-1" ) ;
        sah.add( new Fragment( "Value 1" ) ) ;
        expectedObjectTree.add( sah ) ;
        
        sah = new ServerActiveHtml( "sah-2" ) ;
        sah.add( new Fragment( "Value 2" ) ) ;
        expectedObjectTree.add( sah ) ;
        
        checkObjectTree( "TWO_SERVER_ACTIVE_HTML", expectedObjectTree, TWO_SERVER_ACTIVE_HTML ) ;
        
        /* Fragment only */
        String FRAGMENT_ONLY = "<html><head><title>Fragment Only</title></head></html>" ;
        expectedObjectTree = new LinkedList() ;
        expectedObjectTree.add( new Fragment( FRAGMENT_ONLY ) ) ;
        
        checkObjectTree( "FRAGMENT_ONLY", expectedObjectTree, FRAGMENT_ONLY ) ;
        
        String NO_SERVER_ACTIVE_HTML =
        /* HTML */
        "<html><head><title>Main Page</title></head>\n" +
        "<body bgcolor=\"#336666\">\n" +
        "<hr>\n" +
        "<p>Welcome to Everville</p>\n" +
        "</body>\n" +
        "</html>" ;
        expectedObjectTree = new LinkedList() ;
        expectedObjectTree.add( new Fragment( NO_SERVER_ACTIVE_HTML ) ) ;
        
        checkObjectTree( "NO_SERVER_ACTIVE_HTML", expectedObjectTree, NO_SERVER_ACTIVE_HTML ) ;
        
        /* SAH with Element */
        
        String SAH_WITH_ELEMENT =
        "<!-- name = \"my-sah\" -->" +
        "<!-- element = \"header\" --><h1>HEADER</h1><!-- element = \"/header\" -->" +
        "<!-- name = \"/my-sah\" -->" ;
        
        expectedObjectTree = new LinkedList() ;
        
        Element el = new Element( "header" ) ;
        el.add( new Fragment( "<h1>HEADER</h1>" ) ) ;
        
        sah = new ServerActiveHtml( "my-sah" ) ;
        sah.add( el ) ;
        expectedObjectTree.add( sah ) ;
        
        checkObjectTree( "SAH_WITH_ELEMENT", expectedObjectTree, SAH_WITH_ELEMENT ) ;
        
        /* A SAH with a template attribute ignores all contained Elements, Fragments and ServerActiveHtmls */
        // This need to change to allow Element for 'merging templates'
        
        String SAH_WITH_TEMPLATE =
        "<html><head><title>A ServerActiveHtml with a template attribute</title></head><body>\n" +
        "<!-- name = \"sah-with-template\" template = \"my-template\" --->" +
        "<!-- element = \"header\" --><h1>HEADER</h1><!-- element = \"/header\" -->" +
        "<!-- name = \"contained-sah\" --><h1>HEADER</h1><!-- name = \"/contained-sah\" -->" +
        "\nNot ignored HTML\n"+
        "<!-- name = \"/sah-with-template\" -->" +
        "</body></html>" ;
        
        expectedObjectTree = new LinkedList() ;
        
        expectedObjectTree.add( new Fragment(
        "<html><head><title>A ServerActiveHtml with a template attribute</title></head><body>\n"
        ) ) ;
        
        sah = new ServerActiveHtml( "sah-with-template", "my-template" ) ;
        expectedObjectTree.add( sah ) ;
        
        Element header = new Element( "header" ) ;
        header.add( new Fragment( "<h1>HEADER</h1>" ) ) ;
        sah.add( header ) ;
        
        ServerActiveHtml containedSah = new ServerActiveHtml( "contained-sah" ) ;
        containedSah.add( new Fragment( "<h1>HEADER</h1>" ) ) ;
        sah.add( containedSah ) ;
        
        sah.add( new Fragment( "\nNot ignored HTML\n" ) ) ;
        
        expectedObjectTree.add( new Fragment( "</body></html>" ) ) ;
        
        checkObjectTree( "SAH_WITH_TEMPLATE", expectedObjectTree, SAH_WITH_TEMPLATE ) ;
        
    }
    
    
    /**
     * Test the newer syntax for template replacement works.
     *
     */
    public void testUseTemplateMarkup() {
        
        String SAH_WITH_USE_TEMPLATE =
        "<html><head><title>A ServerActiveHtml with a use-template attribute</title></head><body>\n" +
        "<!-- use-template = \"my-use-template\" --->" +
        "<!-- element = \"header\" --><h1>HEADER</h1><!-- element = \"/header\" -->" +
        "\nNot ignored HTML\n"+
        "<!-- use-template = \"/my-use-template\" -->" +
        "</body></html>" ;
        
        List expectedObjectTree = new LinkedList() ;
        
        expectedObjectTree.add( new Fragment(
        "<html><head><title>A ServerActiveHtml with a use-template attribute</title></head><body>\n"
        ) ) ;
        
        ServerActiveHtml sah = new ServerActiveHtml( "-use-template", "my-use-template" ) ;
        expectedObjectTree.add( sah ) ;
        
        Element header = new Element( "header" ) ;
        header.add( new Fragment( "<h1>HEADER</h1>" ) ) ;
        sah.add( header ) ;
        
        sah.add( new Fragment( "\nNot ignored HTML\n" ) ) ;
        
        expectedObjectTree.add( new Fragment( "</body></html>" ) ) ;
        
        checkObjectTree( "SAH_WITH_USE_TEMPLATE", expectedObjectTree, SAH_WITH_USE_TEMPLATE ) ;
        
    }
    
    
    
    /**
     * Test bad markup. Check ill-formed server active HMTL is not accepted.
     *
     */
    public void testBadMarkUp() {
        
        String MISSING_CLOSING_MAP = "<!-- name=\"news\" -->Alpha Beta Gamma" ;
        checkBadHtml( "MISSING_CLOSING_MAP", MISSING_CLOSING_MAP ) ;
        
        String WRONG_CLOSING_MAP = "<!-- name=\"news\" -->Alpha Beta Gamma<!-- name=\"/result-list\" -->" ;
        checkBadHtml( "WRONG_CLOSING_MAP", WRONG_CLOSING_MAP ) ;
        
        String PREMATURE_CLOSING_MAP = "<!-- name=\"/too-soon\" --><font color=red>" ;
        checkBadHtml( "PREMATURE_CLOSING_MAP", PREMATURE_CLOSING_MAP ) ;
        
        String DUPLICATE_NAMED_ELEMENT =
        "<!-- name=\"a\" -->" +
        "<!-- element=\"dup\" -->text one<!-- element=\"/dup\" -->" +
        "<!-- element=\"dup\" -->text two<!-- element=\"/dup\" -->" +
        "<!-- name=\"/a\" -->" ;
        checkBadHtml( "DUPLICATE_NAMED_ELEMENT", DUPLICATE_NAMED_ELEMENT ) ;
        
    }
    
    /**
     * Test inappropriate use
     */
    public void testMisuse() throws MarkedUpHtmlException {
        
        MarkedUpHtmlParser p = new MarkedUpHtmlParser( new Fragment( "<html></html>" ) ) ;
        
        try {
            p.getGrove() ;
            fail( "getGrove () was invokable before parsing" ) ;
        }
        catch ( IllegalStateException e ) {}
        
        p = new MarkedUpHtmlParser( new Fragment( "<html></html>" ) ) ;
        try {
            p.resolveTemplates( null ) ;
            fail( "resolveTemplates () was invokable before parsing" ) ;
        }
        catch ( IllegalStateException e ) {}
        
    }
    
    /**
     * Test the resolution of templates is working
     */
    public void testResolution() throws MarkedUpHtmlException {
        
        final String REFERENCED_TEMPLATE =
        /* HTML */
        "<html><head><title>Main Page</title></head>\n" +
        "<body bgcolor=\"#336666\">\n" +
        
        /* Server Active HTML #1 */
        "<!-- name=\"news\" template=\"news-tmpl\" -->\n" +
        "Note from Dru to Liz: The news goes here.<br>\n" +
        "Add the template in /_include/templates.html later.<br>\n" +
        "Could we use miniburst.gif for the icon?\n" +
        "<!-- name=\"/news\" -->\n" +
        
        /* HTML */
        "<hr>\n" +
        
        /* Server Active HTML #2 */
        "<!-- name=\"contact\" -->" +
        "<!-- element=\"body\" placeholder-telno=\"tel:xxx\" -->" +
        "For enquiries about this channel call toll-free:xxx.<br>" +
        "<!-- element=\"/body\" -->" +
        "<!-- name=\"/contact\" -->" +
        
        /* HTML */
        "</body>\n" +
        "</html>" ;
        
        List expectedObjectTree = new LinkedList() ;
        
        // Fragments
        /*
         * There are two Fragments here as the resolveTemplates invocation
         * merges Fragments which would otherwise be consecutive.
         */
        expectedObjectTree.add(
        new Fragment(
        "<html><head><title>Main Page</title></head>\n" +
        "<body bgcolor=\"#336666\">\n" +
        //) ) ;
        //expectedObjectTree.add ( new Fragment (
        "NEWS_TMPL_START-<b>HI</b>NEWS_TMPL_END"
        ) ) ;
        expectedObjectTree.add( new Fragment(
        "\n" +
        "<hr>\n"
        ) ) ;
        
        
        // ServerActiveHtml
        ServerActiveHtml sah = new ServerActiveHtml( "contact" ) ;
        Element element = new Element( "body" ) ;
        element.add( new Fragment(
        "For enquiries about this channel call toll-free:xxx.<br>"
        ) ) ;
        element.setPlaceholder( "telno", "tel:xxx" ) ;
        sah.add( element ) ;
        
        expectedObjectTree.add( sah ) ;
        
        // Fragment
        expectedObjectTree.add( new Fragment(
        "</body>\n" +
        "</html>"
        ) ) ;
        
        
        Properties props = new Properties() ;
        props.setProperty( "news-tmpl", "NEWS_TMPL_START-<b>HI</b>NEWS_TMPL_END" ) ;
        
        checkObjectTree(
        "[testResolution] REFERENCED_TEMPLATE" ,
        expectedObjectTree,
        REFERENCED_TEMPLATE,
        new MyTemplateFinder( props )
        ) ;
        
        /* A SAH referencing a SAH referencing a SAH */
        
        String MULTIPLE_REFERENCES =
        "<!-- name = \"sah-1\" template = \"template-1\" -->Hi<!-- name =  \"/sah-1\" -->" ;
        
        props = new Properties() ;
        for ( int i = 1 ; i <= 100 ; i++ ) {
            String name = "sah-" + ( i + 1 ) ;
            String nextTemplate = "template-" + ( i + 1 ) ;
            props.setProperty(
            "template-" + i ,
            "<!-- name = \"" + name + "\" template = \"" + nextTemplate + "\" -->Hi" +
            "<!-- name =  \"/" + name + "\" -->"
            ) ;
        }
        props.setProperty(
        "template-101",
        "The Summit Has Been Reached"
        ) ;
        
        expectedObjectTree = new LinkedList() ;
        expectedObjectTree.add( new Fragment( "The Summit Has Been Reached" ) ) ;
        
        checkObjectTree(
        "[testResolution] MULTIPLE_REFERENCES" ,
        expectedObjectTree,
        MULTIPLE_REFERENCES,
        new MyTemplateFinder( props )
        ) ;
        
        /*
         * Cycles should not result in infinite recursion. A designer could
         * easily intoduce a cycle.
         */
        
        String SELF_REFERENTIAL =
        "<!-- name = \"H\" template = \"refers-to-self\" -->Hi<!-- name = \"/H\" -->" ;
        
        props = new Properties() ;
        props.setProperty(
        "refers-to-self",
        SELF_REFERENTIAL
        ) ;
        
        ParserRunner parserRunner = new ParserRunner(
        new MarkedUpHtmlParser( new Fragment( SELF_REFERENTIAL ) ),
        new MyTemplateFinder( props )
        ) ;
        
        Thread prThread = new Thread( parserRunner ) ;
        prThread.start() ;
        
        long startTime = System.currentTimeMillis() ;
        /* Give it at most 10 seconds */
        while ( System.currentTimeMillis() - startTime < 10 * 1000 ) {
            try {
                Thread.sleep( 50 ) ;
            }
            catch ( InterruptedException e ) {
                fail( "Fixture broken by InterruptedException: " + e.toString() ) ;
            }
            if ( ! prThread.isAlive() )
                break ;
        }
        
        if ( prThread.isAlive() ) {
            prThread.interrupt () ;
            fail( "The self referential template case was still executing after 10 seconds;" +
            " infinite recursion suspected" ) ;
        }
        
        if ( ! parserRunner._succeeded )
            fail( "The self referential template case failed with an Exception:\n" + parserRunner._e ) ;
        
        /* Indirect cycle */
        
        String INDIRECT_CYCLE =
        "<!-- name = \"I\" template = \"ref-1\" -->Hi<!-- name = \"/I\" -->" ;
        
        props = new Properties() ;
        props.setProperty(
        "ref-1",
        "<!-- name = \"I\" template = \"ref-2\" -->Hi<!-- name = \"/I\" -->"
        ) ;
        props.setProperty(
        "ref-2",
        "<!-- name = \"I\" template = \"ref-1\" -->Hi<!-- name = \"/I\" -->"
        ) ;
        
        parserRunner = new ParserRunner(
        new MarkedUpHtmlParser( new Fragment( INDIRECT_CYCLE ) ),
        new MyTemplateFinder( props )
        ) ;
        
        prThread = new Thread( parserRunner ) ;
        prThread.start() ;
        
        startTime = System.currentTimeMillis() ;
        /* Give it at most 10 seconds */
        while ( System.currentTimeMillis() - startTime < 10 * 1000 ) {
            try {
                Thread.sleep( 50 ) ;
            }
            catch ( InterruptedException e ) {
                fail( "Fixture broken by InterruptedException: " + e.toString() ) ;
            }
            if ( ! prThread.isAlive() )
                break ;
        }
        
        if ( prThread.isAlive() ) {
            prThread.interrupt () ;
            fail( "The indirect cycle template case was still executing after 10 seconds;" +
            " infinite recursion suspected" ) ;
        }
        
        if ( ! parserRunner._succeeded )
            fail( "The indirect cycle template case failed with an Exception:\n" + parserRunner._e ) ;
        
    }
    
    /**
     * Test warning inserted for missing template.
     * <p>
     * This is a warning and not an error as a page and the page containing the
     * template referenced may be uploaded in either order. If the template containing
     * page is uploaded second then for a period of time the template using page
     * will include the warning.
     */
    public void testWarningForMissingTemplate() throws MarkedUpHtmlException {
        
        
        String MISSING_TEMPLATE =
        "<!-- name=\"base\" -->" +
        "<!-- name=\"child\" template=\"no-such-tmpl\" -->" +
        "Disregarded text" +
        "<!-- name=\"/child\" -->" +
        "<!-- name=\"/base\" -->" ;
        
        List expectedObjectTree = new LinkedList() ;
        
        
        ServerActiveHtml sah = new ServerActiveHtml( "base" ) ;
        expectedObjectTree.add( sah ) ;
        
        sah.add( new Fragment( "<b>Warning: missing template: &quot;no-such-tmpl&quot;</b>" ) ) ;
        
        checkObjectTree(
        "[testWarningForMissingTemplate] MISSING_TEMPLATE" ,
        expectedObjectTree,
        MISSING_TEMPLATE,
        /* A TemplateFinder which always returns null */
        new MyTemplateFinder( new Properties() )
        ) ;
        
    }
    
    
    
    /**
     * This test was added after the non-detection of system placeholders
     * in text in templates without server active html markup in it was
     * noticed.
     *
     * The situation requires a page with a template reference
     *
     * <html>
     *
     * <!-- name="whatever" template = "my-tmpl" -->
     * Discarded text
     * <!-- name="/whatever" -->
     *
     * </html>
     *
     * and a page with a template in it which includes system placeholders
     *
     * <html>
     *
     * <!-- template = "my-tmpl" -->
     * The current page is $$Page$$.
     * <!-- template = "/my-tmpl" -->
     *
     * </html>
     *
     * Because the Fragment was not being added to an Element it's text was
     * not scanned for system placeholders. This test checks for the expected
     * placeholder method invocations on the parent Element of the Fragment
     * with text "\nThe current page is $$Page$$.\n".
     */
    public void testSystemPlaceholdersScannedForInTemplateReferences()
    throws MarkedUpHtmlException {
        
        final String TEMPLATE_USER =
        /* Server Active HTML #1 */
        "<!-- name = \"preferences\" -->\n" +
        "Implict text\n" +
        /* mark this up inside an explicit element to make checking easier */
        "<!-- element = \"radio\" -->" +
        "<!-- name=\"radio-grp\" template=\"radio-grp-tmpl\" -->\n" +
        "This design-time text is discarded when the page is\n" +
        "placed under management by the WPS.\n" +
        "<!-- name=\"/radio-grp\" -->" +
        "<!-- element = \"/radio\" -->" +
        "<!-- name = \"/preferences\" -->" ;
        
        List expectedObjectTree = new LinkedList() ;
        /*
         * [sah preferences]
         *  [element -implicit] \nImplict text\n
         *  [element radio, placeholders: question = $$Question$$, answer = $$Answer$$
         */
        
        // ServerActiveHtml
        ServerActiveHtml sah = new ServerActiveHtml( "preferences" ) ;
        sah.add( new Fragment( "\nImplict text\n" ) ) ;
        
        Element radioElement = new Element( "radio" ) ;
        radioElement.add( new Fragment(
        "Q:$$Question$$. A:$$Answer$$."
        ) ) ;
        
        sah.add( radioElement ) ;
        
        expectedObjectTree.add( sah ) ;
        
        Properties props = new Properties() ;
        props.setProperty( "radio-grp-tmpl", "Q:$$Question$$. A:$$Answer$$." ) ;
        
        checkObjectTree(
        "[testSystemPlaceholdersScannedForInTemplateReferences] TEMPLATE_USER" ,
        expectedObjectTree,
        TEMPLATE_USER,
        new MyTemplateFinder( props )
        ) ;
    }
    
    /**
     * This test may test a similar or identical scenario as a previous
     * test - I haven't looked. It was added to exercise a resolution
     * scenario that relates directly to the changes made when MarkedUpHtmlParser
     * was changed to use an Element object in place of a List for the top level
     * list of objects. At the same time JspSerializable-List was being dropped
     * from the resolution code so it seemed like a good idea to test or reimplement
     * a test case for this.
     */
    public void testResolutionOfTemplatesInTemplates() throws ServerActiveHtmlException {
        
        final String TEMPLATE_USER =
        "<!-- name =\"user\" template = \"k-tmpl\" -->" +
        "<!-- name =\"/user\" -->" ;
        
        /*
        <!-- template = "k-tmpl" -->
        Hello
        <!-- name = "n" template = "n-tmpl" --><!-- name = "/n" -->
        Goodbye
        <!-- template = "/k-tmpl" -->
         
        <!-- template = "n-tmpl" -->
        <b>h</b>
        <!-- name = "a" --><!-- name = "/a" -->
        <i>i</i>
        <!-- template = "/n-tmpl" -->
         
        k-tmpl parses to
         
        Fragment: Hello
        Sah n, template n-tmpl
        Fragment: Goodbye
         
        when Sah n is resolved it needs to replace itself with 3 new objects.
         */
        
        List expectedObjectGrove = new LinkedList() ;
        /*
         * Notice how the Fragments have been merged. This is due to the
         * use of an Element to assemble the object tree into.
         * Fragment\nHello\n\n<b>h</b>\n
         * sah [ a ]
         * Fragment \n<i>i</i>\n\nGoodbye\n
         */
        
        expectedObjectGrove.add( new Fragment( "\nHello\n\n<b>h</b>\n" ) ) ;
        expectedObjectGrove.add( new ServerActiveHtml( "a" ) ) ;
        expectedObjectGrove.add( new Fragment( "\n<i>i</i>\n\nGoodbye\n" ) ) ;
        
        Properties props = new Properties() ;
        props.setProperty( "k-tmpl",
        "\nHello\n" +
        "<!-- name = \"n\" template = \"n-tmpl\" --><!-- name = \"/n\" -->\n" +
        "Goodbye\n" ) ;
        props.setProperty( "n-tmpl",
        "\n<b>h</b>\n" +
        "<!-- name = \"a\" --><!-- name = \"/a\" -->\n" +
        "<i>i</i>\n" ) ;
        
        checkObjectTree(
        "[testResolutionOfTemplatesInTemplates] TEMPLATE_USER" ,
        expectedObjectGrove,
        TEMPLATE_USER,
        new MyTemplateFinder( props )
        ) ;
        
    }
    
    /**
     * This class allows us to run a parse in a separate Thread and bail
     * if it takes too long.
     */
    private class ParserRunner implements Runnable {
        
        MarkedUpHtmlParser _parser ;
        TemplateFinder _templateFinder ;
        Exception _e ;
        
        boolean _succeeded ;
        
        public void run() {
            _succeeded = false ;
            try {
                _parser.parse() ;
                _parser.resolveTemplates( _templateFinder ) ;
                _succeeded = true ;
            }
            catch ( MarkedUpHtmlException e ) {
                System.out.println("Caught MarkedUpHtmlException " + e.toString() );
                if ( e.getMessage().startsWith( "Maximum invocation depth reached" ) )
                    _succeeded = true ;
                else
                    _e = e ;
            }
            catch ( Exception e ) {
                _e = e ;
            }
        }
        
        public ParserRunner( MarkedUpHtmlParser parser, TemplateFinder templateFinder ) {
            _parser = parser ;
            _templateFinder = templateFinder ;
        }
    }
    /**
     * Test many programmatically generated sequences
     */
    public void testSequences() {
        
        int SEQ_CNT = 40 ;
        
        for ( int seqNum = 1 ; seqNum <= SEQ_CNT ; seqNum++ ) {
            
            StringBuffer html = new StringBuffer() ;
            
            _expectedObjectTree = new LinkedList() ;
            
            Deserializable listWrapper = new Deserializable() {
                
                /*
                 * If the last object was also a Fragment we need to append this
                 * new Fragment to the previous one.
                 */
                public void add( Fragment child ) {
                    
                    boolean add = false ;
                    if ( _expectedObjectTree.size() == 0 )
                        add = true ;
                    else {
                        if ( ! ( _expectedObjectTree.get( _expectedObjectTree.size() - 1 )
                        instanceof Fragment)
                        )
                            add = true ;
                    }
                    if ( add )
                        _expectedObjectTree.add( new Fragment( child ) ) ;
                    else {
                        Fragment f = (Fragment) _expectedObjectTree.get( _expectedObjectTree.size() - 1 ) ;
                        f.add( new Fragment( child ) ) ;
                    }
                    
                }
                public void add( ServerActiveHtml child ) {
                    _expectedObjectTree.add( child ) ;
                }
                public void add( Element child ) {
                    throw new UnsupportedOperationException() ;
                }
                public void add( Placeholder child ) {
                    throw new UnsupportedOperationException() ;
                }
            } ;
            
            int TOP_LEVEL_OBJECT_CNT = _r.nextInt( 12 ) + 1 ;
            for ( int objNum = 1 ; objNum <= TOP_LEVEL_OBJECT_CNT ; objNum++ ) {
                
                /* At the top level we can have SAHs and Fragments */
                
                int objType = _r.nextInt( 2 ) == 0 ? FRAGMENT : SERVER_ACTIVE_HTML ;
                
                try {
                    addObject( objType, listWrapper, html ) ;
                }
                catch ( Exception e ) {
                    fail( "Exception while creating fixture:\n" + e.toString() ) ;
                }
            }
            //System.out.println ("[testSequences] " + seqNum + " " + TOP_LEVEL_OBJECT_CNT );
            checkObjectTree( "[testSequences] " + seqNum , _expectedObjectTree, html.toString() ) ;
            if (false) {
                MarkedUpHtmlParser.printObjectTree( _expectedObjectTree ) ;
                
                /* Introduce new lines to make it readable */
                System.out.println( html.toString().replace( '>', '\n' ) );
            }
        }
        
    }
    
    
    private void addObject( int objType, Deserializable parentObject, StringBuffer html )
    throws ServerActiveHtmlException {
        addObject( objType, parentObject, html, 1 ) ;
    }
    /**
     * Add an object of the requested type to the list of expected objects and add the
     * HTML that would make the object to the html buffer.
     *
     */
    private void addObject( int objType, Deserializable parentObject, StringBuffer html, int depth )
    throws ServerActiveHtmlException {
        
        String name ;
        
        int childCnt, childNum ;
        
        int MAX_DEPTH = 6 ;
        switch ( objType ) {
            
            case FRAGMENT :
                String fragment = htmlFactory.next() ;
                parentObject.add( new Fragment( fragment ) ) ;
                html.append( fragment ) ;
                break ;
                
            case SERVER_ACTIVE_HTML  :
                name = nextName() ;
                ServerActiveHtml sah = new ServerActiveHtml( name ) ;
                html.append( "<!-- name=\"" + name + "\" -->" ) ;
                parentObject.add( sah ) ;
                /*
                 * Add some children.
                 *
                 * A ServerActiveHtml may have Fragments, Elements, and other ServerActiveHtmls
                 * for children.
                 */
                if ( depth <= MAX_DEPTH + 1 ) {
                    childCnt = _r.nextInt( 10 ) - 3  ; /* -3 -> + 6; Allow children sometimes */
                    if ( childCnt > 0 ) {
                        for ( childNum = 1 ; childNum <= childCnt ; childNum++ ) {
                            addObject( _r.nextInt( 3 ) + 1, sah, html, depth + 1 ) ;
                        }
                    }
                }
                html.append( "<!-- name=\"/" + name + "\" -->" ) ;
                break ;
                
            case ELEMENT :
                name = nextName() ;
                Element element = new Element( name ) ;
                html.append( "<!-- element=\"" + name + "\" -->" ) ;
                parentObject.add( element ) ;
                /*
                 * Add some children.
                 *
                 * An Element may have Fragments, ServerActiveHtmls
                 * for children.
                 */
                if ( depth <= MAX_DEPTH ) {
                    childCnt = _r.nextInt( 10 ) - 3  ; /* -3 -> + 6; Allow children sometimes */
                    if ( childCnt > 0 ) {
                        for ( childNum = 1 ; childNum <= childCnt ; childNum++ ) {
                            addObject(
                            _r.nextInt( 2 ) == 0 ? FRAGMENT : SERVER_ACTIVE_HTML,
                            element, html, depth + 1 ) ;
                        }
                    }
                }
                html.append( "<!-- element=\"/" + name + "\" -->" ) ;
                break ;
                
        }
        
    }
    
    // ------------------------------------------------------------- tests for 'reverse templates'
    
    
    public void testPlaceholderAddedInOrderWithFragments() throws ServerActiveHtmlException {
        
        /*
         * HTML that would found in /_include directory between <!-- template = "x" --> comments
         * except it has a surrounding SAH added to fit into the testing apparatus.
         *
         * This is a use of the pattern in Example B of the package documentation.
         */
        String TEMPLATE_HTML_WITH_SAO =
        "<!-- name = \"K\" -->" +
        "Frag-A<!-- use-element = \"UE-1\" --><!-- use-element = \"/UE-1\" -->" +
        "Frag-B<!-- use-element = \"UE-2\" --><!-- use-element = \"/UE-2\" -->" +
        "Frag-C<!-- use-element = \"UE-3\" --><!-- use-element = \"/UE-3\" -->" +
        "<!-- name = \"/K\" -->" ;
        
        List expectedObjectTree = new LinkedList() ;
        
        ServerActiveHtml sah = new ServerActiveHtml( "K" ) ;
        sah.add( new Fragment    ( "Frag-A" ) ) ;
        sah.add( new Placeholder( "UE-1" ) ) ;
        
        sah.add( new Fragment    ( "Frag-B" ) ) ;
        sah.add( new Placeholder( "UE-2" ) ) ;
        
        sah.add( new Fragment    ( "Frag-C" ) ) ;
        sah.add( new Placeholder( "UE-3" ) ) ;
        
        expectedObjectTree.add( sah ) ;
        
        setTemplate( true ) ;
        checkObjectTree(
        "[testPlaceholderAddedInOrderWithFragments] TEMPLATE_HTML_WITH_SAO",
        expectedObjectTree,
        TEMPLATE_HTML_WITH_SAO,
        null //new MyTemplateFinder ( props )
        ) ;
        
        
    }
    
    
    public void testPlaceholdersRejectOrIgnoreChildren() throws ServerActiveHtmlException, MarkedUpHtmlException {
        
        /*
         * Ignore Fragment
         */
        String USE_ELEMENT_WITH_FRAGMENT=
        "<!-- name = \"k\" -->" +
        "Frag-A" +
        "<!-- use-element = \"UE-1\" -->" +
        "Design note" +
        "<!-- use-element = \"/UE-1\" -->" +
        "<!-- name = \"/k\" -->" ;
        
        List expectedObjectTree = new LinkedList() ;
        
        ServerActiveHtml sah = new ServerActiveHtml( "k" ) ;
        sah.add( new Fragment    ( "Frag-A" ) ) ;
        sah.add( new Placeholder( "UE-1" ) ) ;
        
        expectedObjectTree.add( sah ) ;
        
        setTemplate( true ) ;
        checkObjectTree(
        "[testPlaceholdersRejectOrIgnoreChildren] USE_ELEMENT_WITH_FRAGMENT",
        expectedObjectTree,
        USE_ELEMENT_WITH_FRAGMENT,
        null //new MyTemplateFinder ( props )
        ) ;
        
        
        /*
         * Reject Element
         */
        String USE_ELEMENT_WITH_ELEMENT=
        "<!-- name = \"k\" -->" +
        "Frag-A" +
        "<!-- use-element = \"UE-1\" -->" +
        "<!-- element=\"e\" -->My Element<!-- element=\"/e\" -->" +
        "<!-- use-element = \"/UE-1\" -->" +
        "<!-- name = \"/k\" -->" ;
        
        MarkedUpHtmlParser p = new MarkedUpHtmlParser( USE_ELEMENT_WITH_ELEMENT ) ;
        p.setTemplate( true ) ;
        try {
            p.parse() ;
            fail("Element was rejected by Placeholder") ;
        }
        catch ( UnsupportedOperationException e ) {}
        
    }
    
    
    /**
     * The <!-- use-element = "x" --> tag only allowed in templates.
     *
     * This test checks an exception is throw if the markup is used
     * outside of a template.
     */
    public void testPlaceholderRejectedWhenNotInTemplate() {
        
        String USE_ELEMENT_NOT_IN_TEMPLATE =
        "<!-- name = \"L\" -->" +
        "<!-- use-element = \"E\" --><!-- use-element = \"/E\" -->" +
        "<!-- name = \"/L\" -->"
        ;
        
        MarkedUpHtmlParser p = new MarkedUpHtmlParser( USE_ELEMENT_NOT_IN_TEMPLATE ) ;
        try {
            p.parse() ;
            fail("Placeholder used outside of template was rejected") ;
        }
        catch ( Exception e ) {
            if ( e.toString().indexOf( "'use-element' is only allowed in templates" ) == -1 )
                fail(
                "Correct exception thrown when there was an attempt to use a Placeholder outside of a template" ) ;
        }
        
    }
    
    
    public void testPlaceholderReplacement() {
        
        
        String TEMPLATE =
        "Frag-A" +
        "<!-- use-element= \"e1\" -->" +
        "Users of this template must provider an Element named e1" +
        "<!-- use-element = \"/e1\" -->" +
        "Frag-B"
        ;
        
        String TEMPLATE_USER =
        "<!-- use-template = \"t1\" -->" +
        "<!-- element = \"e1\" -->My Content!<!-- element = \"/e1\" -->" +
        "<!-- use-template = \"/t1\" -->"
        ;
        
        List expectedObjectGrove = new LinkedList() ;
        
        /* The Fragments will merge into the first */
        expectedObjectGrove.add( new Fragment( "Frag-A" + "My Content!" + "Frag-B" ) ) ;
        
        Properties props = new Properties() ;
        props.setProperty( "t1", TEMPLATE ) ;
        
        checkObjectTree(
        "[testPlaceholderReplacement] TEMPLATE_USER" ,
        expectedObjectGrove,
        TEMPLATE_USER,
        new MyTemplateFinder( props )
        ) ;
        
    }
    
    /**
     * The WPS does not do deep placeholder replacement yet
     */
    public void _testDeepPlaceholderReplacement() throws ServerActiveHtmlException {
        
        /*
         * FRAG: Frag-A
         * SAH cal
         *     ELEMENT header
         *         PLACEHOLDER shallow
         *     ELEMENT content
         *         SAH subcal
         *             PLACEHOLDER deep
         * FRAG: Frag-B
         */
        String TEMPLATE =
        "Frag-A" +
        "<!-- name = \"cal\" -->" +
        
        "<!-- element = \"header\" -->" +
        "<!-- use-element= \"shallow\" -->" +
        "Users of this template must provider an Element named shallow" +
        "<!-- use-element = \"/shallow\" -->" +
        "<!-- element = \"/header\" -->" +
        
        "<!-- element = \"content\" -->" +
        "<!-- name = \"subcal\" -->" +
        "<!-- use-element= \"deep\" -->" +
        "Users of this template must provider an Element named deep" +
        "<!-- use-element = \"/deep\" -->" +
        "<!-- name = \"/subcal\" -->" +
        "<!-- element = \"/content\" -->" +
        
        "<!-- name = \"/cal\" -->" +
        "Frag-B"
        ;
        
        /* should resolve to
         *
         * FRAG: Frag-A
         * SAH cal
         *     ELEMENT header
         *         FRAG: SHALLOW FRAGMENT
         *     ELEMENT content
         *         SAH subcal
         *             ELEMENT -implicit
         *                 FRAG: DEEP FRAGMENT
         * FRAG: Frag-B
         */
        String TEMPLATE_USER =
        "<!-- use-template = \"t1\" -->" +
        "<!-- element = \"shallow\" -->SHALLOW FRAGMENT<!-- element = \"/shallow\" -->" +
        "<!-- element = \"deep\" -->DEEP FRAGMENT<!-- element = \"/deep\" -->" +
        "<!-- use-template = \"/t1\" -->"
        ;
        
        
        List expectedObjectGrove = new LinkedList() ;
        
        expectedObjectGrove.add( new Fragment( "Frag-A" ) ) ;
        
        ServerActiveHtml cal = new ServerActiveHtml( "cal" ) ;
        Element header = new Element( "header" ) ;
        header.add( new Fragment( "SHALLOW FRAGMENT" ) ) ;
        cal.add( header ) ;
        Element content = new Element( "content" ) ;
        ServerActiveHtml subcal = new ServerActiveHtml( "subcal" ) ;
        subcal.add( new Fragment( "DEEP FRAGMENT" ) ) ;
        content.add( subcal ) ;
        cal.add( content ) ;
        expectedObjectGrove.add( cal ) ;
        
        expectedObjectGrove.add( new Fragment( "Frag-B" ) ) ;
        
        Properties props = new Properties() ;
        props.setProperty( "t1", TEMPLATE ) ;
        
        checkObjectTree(
        "[testDeepPlaceholderReplacement] TEMPLATE_USER" ,
        expectedObjectGrove,
        TEMPLATE_USER,
        new MyTemplateFinder( props )
        ) ;
        
    }
    
    public void testPlaceholderPreservationOnPlaceholderReplacement() throws ServerActiveHtmlException {
        
        String TEMPLATE =
        "<!-- use-element = \"x\" --><!-- use-element = \"/x\" -->" ;
        
        String TEMPLATE_USER =
        "<!-- use-template = \"t\" -->" +
        "<!-- element = \"x\" placeholder-p1 = \"p1-text\" placeholder-p2 = \"p2-text\"  placeholder-p3 = \"p3-text\" -->" +
        "<ol><li>p1-text<li>p2-text<li>p3-text</ol>" +
        "<!-- element = \"/x\" -->" +
        "<!-- use-template = \"/t\" -->"
        ;
        
        List expectedObjectGrove = new LinkedList() ;
        ServerActiveHtml sah = new ServerActiveHtml( "-replacement" ) ;
        sah.setPlaceholder( "p1", "p1-text" ) ;
        sah.setPlaceholder( "p2", "p2-text" ) ;
        sah.setPlaceholder( "p3", "p3-text" ) ;
        sah.add( new Fragment( "<ol><li>p1-text<li>p2-text<li>p3-text</ol>" ) ) ;
        expectedObjectGrove.add( sah ) ;
        
        Properties props = new Properties() ;
        props.setProperty( "t", TEMPLATE ) ;
        
        checkObjectTree(
        "[testPlaceholderPreservationOnPlaceholderReplacement] TEMPLATE_USER" ,
        expectedObjectGrove,
        TEMPLATE_USER,
        new MyTemplateFinder( props )
        ) ;
        
    }
    
    /**
     * base uses A, B, C
     * intermediate defines A, uses B, C
     * user defines B, C
     *
     * So template 'intermediate' effectively provides a default element for template 'base'
     */
    public void testElementChaining() throws ServerActiveHtmlException {
        
        String BASE_TEMPLATE =
        "<!-- use-element = \"a\" --><!-- use-element = \"/a\" -->" +
        "<!-- use-element = \"b\" --><!-- use-element = \"/b\" -->" +
        "<!-- use-element = \"c\" --><!-- use-element = \"/c\" -->"
        ;
        
        String INTERMEDIATE_TEMPLATE =
        "<!-- use-template = \"base\" -->" +
        "<!-- element = \"a\" --><!-- name = \"sao-a\" -->CONTENT A<!-- name = \"/sao-a\" --><!-- element = \"/a\" -->" +
        
        "<!-- element = \"b\" -->" +
        "<!-- use-element = \"b\" --><!-- use-element = \"/b\" -->" +
        "<!-- element = \"/b\" -->" +
        
        "<!-- element = \"c\" -->" +
        "<!-- use-element = \"c\" --><!-- use-element = \"/c\" -->" +
        "<!-- element = \"/c\" -->" +
        
        "<!-- use-template = \"/base\" -->"
        ;
        
        
        String TEMPLATE_USER =
        "<!-- use-template = \"intermediate\" -->" +
        "<!-- element = \"b\" --><!-- name = \"sao-b\" -->CONTENT B<!-- name = \"/sao-b\" --><!-- element = \"/b\" -->" +
        "<!-- element = \"c\" --><!-- name = \"sao-c\" -->CONTENT C<!-- name = \"/sao-c\" --><!-- element = \"/c\" -->" +
        "<!-- use-template = \"/intermediate\" -->"
        ;
        
        List expectedObjectGrove = new LinkedList() ;
        
        ServerActiveHtml sah_a = new ServerActiveHtml( "sao-a" ) ;
        sah_a.add( new Fragment( "CONTENT A" ) ) ;
        expectedObjectGrove.add( sah_a ) ;
        
        
        ServerActiveHtml sah_b= new ServerActiveHtml( "sao-b" ) ;
        sah_b.add( new Fragment( "CONTENT B" ) ) ;
        expectedObjectGrove.add( sah_b ) ;
        
        ServerActiveHtml sah_c = new ServerActiveHtml( "sao-c" ) ;
        sah_c.add( new Fragment( "CONTENT C" ) ) ;
        expectedObjectGrove.add( sah_c ) ;
        
        
        Properties props = new Properties() ;
        props.setProperty( "base", BASE_TEMPLATE ) ;
        props.setProperty( "intermediate", INTERMEDIATE_TEMPLATE ) ;
        
        checkObjectTree(
        "[testElementChaining] TEMPLATE_USER" ,
        expectedObjectGrove,
        TEMPLATE_USER,
        new MyTemplateFinder( props )
        ) ;
        
    }
    
    
    /**
     * On 02-10-4 A bug was found in the WPS when using full page templates.
     *
     * This test reproduces the bug.
     *
     * The setup is
     *
     * index.html
     *
     * <!-- use-template = "base-template" -->
     * <html>
     * <head>
     * <title>TITLE</title>
     * </head>
     * <body>
     * <!--
     *    element = "section-body"
     * --><!--
     *    use-template = "k"
     * -->Should be replaced with template "k"<!--
     *    use-template = "/k"
     * --><!--
     *    element = "/section-body"
     * -->
     * </body>
     * </html>
     * <!-- use-template = "/base-template" -->
     *
     * _include/base-template.html
     *
     * <!-- template = "base-template" -->
     * <html>
     * <head>
     * <title>BASE TITLE</title>
     * </head>
     * <body>
     * <!--
     *     use-element = "section-body"
     * -->
     * use element "section-body"
     * <!--
     *     use-element = "/section-body"
     * -->
     * </body>
     * </html>
     * <!-- template = "/base-template" -->
     *
     * _include/small-templates.html
     *
     * <html>
     * <head>
     * <title>Templates</title>
     * </head>
     * <body>
     * <!--
     *     template = "k"
     * -->Content of template "k"<!--
     *     template = "/k"
     *-->
     * </body>
     * </html>
     *
     * _application/application.properties
     * <empty>
     *
     * The bug was that the template reference to "k" was not getting resolved and the
     * served page included the text 'Should be replaced with template "k"'.
     *
     */
    public void testTemplateResolutionInFullPageTemplate() {
        
        String INDEX_HTML =
        "<!-- use-template = \"base-template\" -->\n" +
        "<html>\n" +
        "<head>\n" +
        "<title>TITLE</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "<!--\n" +
        "   element = \"section-body\"\n" +
        "--><!--\n" +
        "   use-template = \"k\"\n" +
        "-->Should be replaced with template \"k\"<!--\n" +
        "   use-template = \"/k\"\n" +
        "--><!--\n" +
        "   element = \"/section-body\"\n" +
        "-->\n" +
        "</body>\n" +
        "</html>\n" +
        "<!-- use-template = \"/base-template\" -->"
        ;
        
        String BASE_TEMPLATE =
        "<html>\n" +
        "<head>\n" +
        "<title>BASE TITLE</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "<!--\n" +
        "    use-element = \"section-body\"\n" +
        "-->\n" +
        "use element \"section-body\"\n" +
        "<!--\n" +
        "    use-element = \"/section-body\"\n" +
        "-->\n" +
        "</body>\n" +
        "</html>\n"
        ;
        
        String K_TEMPLATE =
        "Content of template \"k\""
        ;
        
        
        String INDEX_HTML_ =
        "<!-- use-template = \"base-template\" -->\n" +
        "<html>\n" +
        "<head>\n" +
        "<title>TITLE</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "<!--\n" +
        "   element = \"section-body\"\n" +
        "--><!--\n" +
        "   name = \"some-sao\"\n" +
        "-->Should be replaced with sao\"k\"<!--\n" +
        "   name = \"/some-sao\"\n" +
        "--><!--\n" +
        "   element = \"/section-body\"\n" +
        "-->\n" +
        "</body>\n" +
        "</html>\n" +
        "<!-- use-template = \"/base-template\" -->"
        ;
        
        Properties props = new Properties() ;
        props.setProperty( "base-template", BASE_TEMPLATE ) ;
        props.setProperty( "k", K_TEMPLATE ) ;
        
        List expectedObjectGrove = new LinkedList() ;
        expectedObjectGrove.add(
        new Fragment(
        "<html>\n" +
        "<head>\n" +
        "<title>BASE TITLE</title>\n" +
        "</head>\n" +
        "<body>\n" +
        K_TEMPLATE +
        "\n" +
        "</body>\n" +
        "</html>\n"
        )
        ) ;
        
        
        
        checkObjectTree(
        "[testTemplateResolutionInFullPageTemplate] INDEX_HTML" ,
        expectedObjectGrove,
        INDEX_HTML,
        new MyTemplateFinder( props )
        ) ;
        
    }
    
    
    /* Shared methods */
    
    
    private String nextName() {
        
        return "name-" + _nextName++ ;
        
    }
    
    /**
     * Check the parsing of the HTML resulted in the correct object tree.
     */
    private void checkObjectTree( String name, List expectedObjectTree, String html ) {
        checkObjectTree( name, expectedObjectTree, html, null ) ;
    }
    
    /**
     * When true the parser used in checkObjectTree will assume the html is for a template.
     * Check the source of MarkedUpHtmlParser for the effect of this.
     */
    private boolean isTemplate = false ;
    private void    setTemplate( boolean isTemplate ) { this.isTemplate = isTemplate ; }
    private boolean isTemplate() { return isTemplate ; }
    
    /**
     * Check the parsing of the HTML resulted in the correct object tree.
     */
    private void checkObjectTree( String name, List expectedObjectTree, String html, TemplateFinder templateFinder ) {
        
        MarkedUpHtmlParser p = new MarkedUpHtmlParser( html ) ;
        p.setTemplate( isTemplate() ) ;
        
        try {
            p.parse() ;
            if ( templateFinder != null )
                p.resolveTemplates( templateFinder ) ;
        }
        catch ( MarkedUpHtmlException e ) {
            fail( "parse () Exception: " + e.toString() ) ;
        }
        
        List actualTree = p.getGrove() ;
        
        assertEquals( "The number of top level objects was the same (" + name + ")" +
        "\n\nExpected:\n" + MarkedUpHtmlParser.objectTreeToString( expectedObjectTree ) +
        "\n\nActual:\n" + MarkedUpHtmlParser.objectTreeToString( actualTree ),
        expectedObjectTree.size(),
        actualTree.size()
        ) ;
        
        for ( int objIdx = 0 ; objIdx < expectedObjectTree.size() ; objIdx++ ) {
            assertEquals( "The objects at index " + objIdx + " were equal (" + name + ")" +
            "\n\nExpected:\n" + MarkedUpHtmlParser.objectTreeToString( expectedObjectTree ) +
            "\n\nActual:\n" + MarkedUpHtmlParser.objectTreeToString( actualTree ),
            expectedObjectTree.get( objIdx ),
            actualTree.get( objIdx ) ) ;
        }
        
    }
    
    /**
     * Check that ill-formed HTML is handled by throwing a MarkedUpHtmlException exception
     */
    private void checkBadHtml( String name, String html ) {
        
        MarkedUpHtmlParser p = new MarkedUpHtmlParser( html ) ;
        
        try {
            p.parse() ;
        }
        catch ( MarkedUpHtmlException e ) {
            //System.out.println ( e.toString () ) ;
            return ;
        }
        
        fail( "Exception not thrown for bad html (" + name + ") :\n" + html ) ;
        
    }
    
    private class HtmlFactory {
        
        int _next = -1 ;
        
        String [] html = new String [] {
            "<b><i>Hi</i></b>\n" ,
            "<!-- the news section goes in here -->",
            "<script language=\"javascript\" type=\"text/html\"><!--\nif ( true )" +
            "\r\ndocument.writeln ( \"<b>It's all true</b>\" ) ; \n//-->\r\n",
            "<!-- --><font color=\"red\">Warning!</font>",
            "<b>Back in Black</b>\n"
        } ;
        
        public String next() {
            
            _next = ( _next + 1 ) % html.length ;
            
            return html [ _next ] ;
            
        }
        
    }
    
    private class MyTemplateFinder implements TemplateFinder {
        
        private Properties _map ;
        
        public MyTemplateFinder( Properties templateMap ) {
            _map = new Properties() ;
            Iterator it = templateMap.keySet().iterator() ;
            while ( it.hasNext() ) {
                String key = (String) it.next() ;
                String value = templateMap.getProperty( key ) ;
                //System.out.println ("MyTemplateFinder:\nkey: " + key + "\nvalue: " + value );
                _map.setProperty( key, value ) ;
            }
        }
        
        public Fragment getTemplate( String templateName ) throws TemplateFinderException {
            String template = _map.getProperty( templateName ) ;
            if ( template == null )
                return null ;
            return new Fragment( template ) ;
        }
    }
    /*
     * These test Strings were moved from ServerActiveHtmlTest when parsing of HTML
     * was removed from that class.
     *
     * The matching initialiser follows.
     *
     *
     */
    final String BASIC_TEMPLATE =
    "<!-- element=\"header\" -->\n" +
    "<table border=1\n" +  // line break edit
    "valign=\"top\">\n" +
    "<tr><tr>Todays Education News</td></tr>\n" +
    "<!-- element=\"/header\" -->\n" +
    "<!-- element=\"item-start\" -->\n" +
    "<tr><td valign=\"top\" class=\"news\">\n" +
    "<!-- element=\"/item-start\" -->\n" +
    
    /* Discarded HTML */
    "This is some sample news from the world of education.\n" +
    
    "<!-- element=\"item-end\" -->\n" +
    "</td></tr>\n" +
    "<!-- element=\"/item-end\" -->\n" +
    "<!-- element=\"footer\" -->\n" +
    "</table>\n" +
    "<!-- element=\"/footer\" -->" ;
    
    protected HashMap BASIC_TEMPLATE_EXPECTED_ELEMENTS ;
    
    final String BASIC_TEMPLATE_WITH_EMBEDDED_COMMENTS =
    "<!-- element=\"header\" -->\n" +
    "<table border=1\n" +  // line break edit
    "valign=\"top\">\n" +
    /* Embedded comment */
    "<!-- comment between element tags -->" +
    "<tr><tr>Todays Education News</td></tr>\n" +
    "<!-- element=\"/header\" -->\n" +
    "<!-- element=\"item-start\" -->\n" +
    "<tr><td valign=\"top\" class=\"news\">\n" +
    "<!-- element=\"/item-start\" -->\n" +
    
    /* Discarded HTML */
    "This is some sample news from the world of education.\n" +
    
    "<!-- element=\"item-end\" -->\n" +
    "</td></tr>\n" +
    "<!-- element=\"/item-end\" -->\n" +
    "<!-- element=\"footer\" -->\n" +
    "</table>\n" +
    "<!-- element=\"/footer\" -->" ;
    
    protected HashMap BASIC_TEMPLATE_WITH_EMBEDDED_COMMENTS_EXPECTED_ELEMENTS ;
    
    final String ADVANCED_TEMPLATE =
    "<!-- element=\"header\" -->\n" +
    "<table border=1\n" +  // line break edit
    "valign=\"top\">\n" +
    "<tr><td colspan=2>Todays Education News</td></tr>\n" +
    "<!-- element=\"/header\" -->\n" +
    
    /* element with placeholders */
    "<!-- " +
    "element=\"news-item\" " +
    "placeholder-time=\"17:55 hrs\" " +
    "placeholder-link=\"the-link-goes-here\" " +
    "placeholder-title=\"A debt-free student was found in Cornwall today authorities report\" " +
    "-->" +
    "<tr><td class=\"news-link\">" +
    "<a href=\"stories.html?the-link-goes-here\">" +
    "A debt-free student was found in Cornwall today authorities report" +
    "</a>" +
    "</td><td>17:55 hrs</td></tr>" +
    "<!-- element=\"/news-item\" -->" +
    
    /* Unmarked up */
    "\n\n" +
    "<h1>My loose HTML</h1>" +
    
    "<!-- element=\"footer\" -->\n" +
    "</table>\n" +
    "<!-- element=\"/footer\" -->" +
    
    /* Unmarked up */
    "\n" ;
    
    //protected HashMap ADVANCED_TEMPLATE_EXPECTED_ELEMENTS ;
    
    
}