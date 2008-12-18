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

/*	Program Name : JSPBuilder
 *	Author : Abhishek Verma, Janek D. Bogucki
 *	Purpose : To read a HTML file and convert it into a JSP File.
 *	Usage :	[non-command line usage] used by the XL Project
 */

package org.ntropa.build.jsp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ntropa.build.Constants;
import org.ntropa.build.ContextPath;
import org.ntropa.build.DirectoryPair;
import org.ntropa.build.FileListener;
import org.ntropa.build.FileListenerEvent;
import org.ntropa.build.channel.FileLocation;
import org.ntropa.build.channel.FileLocationException;
import org.ntropa.build.html.MarkedUpHtmlException;
import org.ntropa.build.html.MarkedUpHtmlParser;
import org.ntropa.build.html.ParsedHtml;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.HtmlUtils;
import org.ntropa.utility.Logger;
import org.ntropa.utility.StandardFilePredicate;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class responds to messages about files that have been added, modified or
 * deleted by creating a JSP or deleting a JSP.
 * 
 * There are two stages in processing the HTML to become the JSP.
 * 
 * Stage 1: DOM manipulations. After the HTML have been parsed to a DOM tree the
 * structure is edited. Example modifications include:
 * 
 * i) Adding missing meta tags. eg keywords and description tags ii) Adding
 * missing refresh tags iii) Adding any other common tags, maybe ratings
 * information iv) Adding a Server Active HTML section named "application" which
 * provide common application level processing
 * 
 * Stage 2: The DOM is serialised to a text stream and parsing of Server Active
 * HTML happens.
 * 
 * ----------------------------------------------------------------------------
 * Server Active HTML description.
 * 
 * Server Active HTML is a sequence of HTML tags marked up with comment tags.
 * Here is a HTML page with SAH. All text is literal, ie if it says
 * 'the-link-goes-here' that is exactly what is in the page. One of the main
 * points of SAH is to allow designers to work in a natural way and this
 * includes the use of placeholders to allow production of well-formed HTML and
 * demos of static versions of sites.
 * 
 * <html><head><title>Example of Server Active HTML</title></head> <body>
 * <h1>This is the news page</h1>
 * <!-- name="news" --> <!-- element="header" --> <table border=1>
 * <tr>
 * <td colspan="2">The news today</td>
 * </tr>
 * <!-- element="/header" --> <!-- element="news-item" placeholder="time:17:55
 * hrs" placeholder="link:the-link-goes-here" placeholder="title:A debt-free
 * student was found in Cornwall today authorities report" -->
 * <tr>
 * <td class="news-link"> <a href="stories.html?the-link-goes-here"> A debt-free
 * student was found in Cornwall today authorities report </a> </td>
 * <td>17:55 hrs</td>
 * </tr>
 * <!-- element="/news-item" --> <!-- element="footer" --> </table> <!--
 * element="/footer" --> <!-- name="/news" --> </body> </html>
 * 
 * The purpose of each markup tag is described here.
 * 
 * <!-- name="news" --> ... <!-- name="/news" -->
 * 
 * This pair encloses the HTML to replace with dynamic HTML, calculated at HTTP
 * serve time. In this case the named Server Active HTML encloses a template to
 * use in making the replacement HTML.
 * 
 * The template is composed of HTML marked up with element tags. The HTML inside
 * each element tag may be further marked up though the use of non-invasive
 * placeholder attributes. In these simple cases the placeholder attribute value
 * is a literal string to replace with dynamic content.
 * 
 * Each element is analysed here.
 * 
 * ----------------------------------------------------------------------------
 * <!-- element="header" --> <table border=1>
 * <tr>
 * <td colspan="2">The news today</td>
 * </tr>
 * <!-- element="/header" -->
 * 
 * The server process has access to this string as a result of parsing this
 * element
 * 
 * header = "<table border=1>\n" + "
 * <tr>
 * <td colspan=\"2\">The news today</td>
 * </tr>
 * \n" +
 * 
 * ----------------------------------------------------------------------------
 * <!-- element="news-item" placeholder="time:17:55 hrs"
 * placeholder="link:the-link-goes-here" placeholder="title:A debt-free student
 * was found in Cornwall today authorities report" -->
 * <tr>
 * <td class="news-link"> <a href="stories.html?the-link-goes-here"> A debt-free
 * student was found in Cornwall today authorities report </a> </td>
 * <td>17:55 hrs</td>
 * </tr>
 * <!-- element="/news-item" -->
 * 
 * The server process has access to these strings as a result of parsing this
 * element
 * 
 * news-item = "
 * <tr>
 * <td class=\"news-link\">\n" + "<a
 * href=\"stories.html?the-link-goes-here\">\n" + "A debt-free student was found
 * in Cornwall today authorities report\n" + "</a>\n" + "</td>
 * <td>17:55 hrs</td>
 * </tr>
 * \n"
 * 
 * placeholders:
 * 
 * time = "17:55 hrs" link = "the-link-goes-here" title = "A debt-free student
 * was found in Cornwall today authorities report"
 * 
 * (Implementation note: this can be optimised by using the placeholders to
 * break the HTML into a sequence and assemble the string at serve time:
 * 
 * news-item [ 0 ] ="
 * <tr>
 * <td class=\"news-link\">\n<a href=\"stories.html?" news-item [ 1 ] =
 * "\">\n" news-item [ 2 ] = "</a>\n</td>
 * <td>" news-item [ 3 ] = " </td>
 * </tr>
 * \n"
 * 
 * and then the HTML is assembled like this
 * 
 * html = news-item [ 0 ] + mTheLink + news-item [ 1 ] + theTitle + news-item [
 * 2 ] + theTimeStamp + news-item [ 3 ]
 * 
 * This will save a bit of serve time but isn't a priority.)
 * 
 * ----------------------------------------------------------------------------
 * <!-- element="footer" --> </table> <!-- element="/footer" -->
 * 
 * The server process has access to these strings as a result of parsing this
 * element
 * 
 * footer = </table>
 * 
 * ----------------------------------------------------------------------------
 * The above was an example of a embedded template. The HTML between these tags
 * was the template
 * 
 * <!-- name="news" --> ... <!-- name="/news" -->
 * 
 * In this example a linked template is shown. The HTML between the tags is a
 * design note between designers and is stripped from the HTML during the SAH
 * processing stage.
 * 
 * <html><head><title>Example of Server Active HTML</title></head> <body>
 * <h1>This is the news page</h1>
 * <!-- name="news" template="general-news-tmpl" -->
 * <h1>The news goes here when the site is live.</h1>
 * The layout is defined in the template "general-news-tmpl". It still needs to
 * be checked in Opera - Dru <!-- name="/news" --> </body> </html>
 * 
 * The process searches in the HTML pages in the _include directories to find
 * the template called "general-news-tmpl". The search starts in the _include
 * directory in the same directory as the HTML page being processed and if no
 * match is found the process moves to the parent and continues the search.
 * 
 * Let's say about.html has SAH which links to the template "contact" and the
 * template "contact" is defined in /_include/templates.html. / _include/
 * logon.html templates.html index.html about/ _include/ headers.html
 * templates.html about.html
 * 
 * The process searches in this order until the template "contact" is found.
 * 
 * about/_include/headers.html (No match) about/_include/templates.html (No
 * match) /_include/logon.html (No match) /_include/templates.html (Match)
 * 
 * Templates are stored in regular HTML files in _include directory. Because
 * they are standard HTML pages they can be worked on with HTML editors such as
 * Dreamweaver.
 * 
 * ----------------------------------------------------------------------------
 * This is a HTML page with two templates defined.
 * 
 * <html><head><title>My Templates</title></head> <body>
 * 
 * <h1>This is the news template</h1>
 * Last edit: Liz , 27-Sept-2003<br>
 * 
 * <!-- template="general-news-tmpl" --> <!-- element="header" --> <table
 * border=1>
 * <tr>
 * <td colspan="2">The news today</td>
 * </tr>
 * <!-- element="/header" --> <!-- element="news-item" placeholder="time:17:55
 * hrs" placeholder="link:the-link-goes-here" placeholder="title:A debt-free
 * student was found in Cornwall today authorities report" -->
 * <tr>
 * <td class="news-link"> <a href="stories.html?the-link-goes-here"> A debt-free
 * student was found in Cornwall today authorities report </a> </td>
 * <td>17:55 hrs</td>
 * </tr>
 * <!-- element="/news-item" --> <!-- element="footer" --> </table> <!--
 * element="/footer" --> <!-- template="/general-news-tmpl"-->
 * 
 * <h1>This is the contact template</h1>
 * Last edit: Liz , 27-Sept-2003
 * 
 * <!-- template="contact" --> <table border=1>
 * <tr>
 * <td colspan="2">Contact us!</td>
 * </tr>
 * <tr>
 * <td class="contact"> Tel:</td>
 * <td>077 000 111 444 ext 322</td>
 * </tr>
 * </table> <!-- template="/contact"-->
 * 
 * </body> </html>
 * 
 * 
 * ----------------------------------------------------------------------------
 * Now that the template has been located and parsed, the process looks for a
 * definition of the name of the SAH in the application data.
 * 
 * For example the SAH is
 * 
 * <!-- name="search-results" template="learning-agent-results-tmpl" --> The
 * search results would go here in the live site, this is just for Tuesdays demo -
 * Ralf <!-- name="/search-results" -->
 * 
 * The process looks for an application object with the name "search-results"
 * defined at this point in the web space (ie at the point in the file layout,
 * much like the search of a named template, and looks up the name of the Java
 * Bean to use for the JSP. The bean is initialised in the standard way for
 * beans. (Map '-' to '_').
 * 
 * The JSP is embedded directly in the page being constructed.
 * 
 * If there is no application object named "search-result" the process saves the
 * contents of the template into the same directory as it was found in as a JSP
 * suitable for inclusion with a <jsp:include> tag. The HTML included is the
 * concatenation of the elements of the template. Placeholder information is
 * discarded.
 * 
 * The different locations for each type of processed SAH is based on this
 * 
 * a) If the SAH does not have a corresponding application object it is
 * essentially static, included HTML so it makes sense to store it in a shared
 * location.
 * 
 * b) Of the SAH does have a corresponding application object it simplifies the
 * storage of more than one use of the SAH with potentially different
 * application objects by embedding the JSP in the page.
 * 
 * Unhandled option: we could have a 'parameterised' SAH like a link table with
 * five links when depending on the section the link table is used in, one link
 * is inactive. Instead of embedding the resultant JSP tags in the page we could
 * use a <jsp:include> with some request parameters. The parameters would come
 * of the original SAH maybe like this
 * 
 * <!-- name="menu" share="true" location="about-section" template="menu" -->
 * <!-- name="/menu" -->
 * 
 * In fact we could do this with implicit parameters like 'browser-group",
 * "DHTML-level", and "location"
 * 
 * <!-- template="menu" --> <!-- element="about-link" location = "/about/*" -->
 * <b>about</b> <!-- element="/about-link" --> <!-- element="about-link" --> <a
 * href="/about/index.html">about</a> <!-- element="/about-link" --> <br>
 * <!-- element="search-link" location = "/search/*" --> <b>search</b> <!--
 * element="/search-link" --> <!-- element="search-link" --> <a
 * href="/search/index.html">search</a> <!-- element="/search-link" --> <!--
 * template="/menu" -->
 * 
 * The most specifically matched element is selected at serve time thus when
 * 'location' was '/about/index.html' we would serve
 * 
 * <b>about</b> <br>
 * <a href="/search/index.html">search</a>
 * 
 * When 'location' was '/search/index.html' we would serve
 * 
 * <a href="/about/index.html">about</a> <br>
 * <b>search</b>
 * 
 * This doesn't need to be attempted for the IDP site. We will need some way of
 * handling conditional HTML inclusion, eg on a results page either the result
 * list or a no results message.
 * 
 * ----------------------------------------------------------------------------
 * A diagram showing the combinations of Server Active HTML with application
 * objects and template locations
 * 
 * Template location ------------------------------ | Embedded | Linked
 * ----------------------------------------------- Application | does't exist |
 * a | b object | | | ----------------------------------------------- | does
 * exist | c | d | | |
 * 
 * Notes
 * 
 * a) This is a fairly pointless combination. The process will parse the
 * template and inline the resulting HTML
 * 
 * b) The process will locate the nearest template as named in the template
 * attribute and insert a <jsp:include> action tag in the page
 * 
 * c) The embedded template is parsed and handled to the application object
 * somehow. (options: custom action tag, jsp:include to servlet with params)
 * 
 * d) The same as c) except the linked template is located first.
 * 
 * ----------------------------------------------------------------------------
 * Useful diagram: http://wiki.xl.studylink.com/docs/img/jsp-updation.png
 * 
 * ----------------------------------------------------------------------------
 * 
 * @author abhishek, jdb
 * @version $Id: JSPBuilder.java,v 1.58 2006/03/22 16:31:19 jdb Exp $
 */
public class JSPBuilder implements FileListener, Logger {

    /*
     * The separator to use when creating file names for templates. '#' won't
     * ever be used in a name for a HTML file.
     */
    public static final String TEMPLATE_SEPARATOR = "#";

    public static final String TEMPLATE_EXTENSION = ".template";

    private static StandardFilePredicate _isTemplatePage;

    protected static String INCLUDE_DIR;

    private static final String ERROR_HTML_OPEN = "<html><head><title>Build Error</title>" + "<!--"
            + DOMEditor.META_CONTENT_TYPE_PLACEHOLDER + "-->" + "</head>"
            + "<body style='background-color: green'><div style='border: 2px solid red'><pre>";

    private static final String ERROR_HTML_CLOSE = "</pre></div></body></html>";

    /* The context path of the channel the object is responsible for */
    private ContextPath m_contextPath;

    /* The folder for reading HTML files and presentation parameters from */
    private File m_SourceDir;

    /* The folder for writing JSPs to and deleting JSPs from */
    private File m_DestinationDir;

    /* This class is not threadsafe due to these fields */
    private String _relativeLocation;

    /* current source file */
    private File _sourceFile;

    /* current destination file */
    private File _destFile;

    /* The debug level */
    private int _debug = 0;

    /** The object to edit the head section of all handled HTML pages */
    private DOMEditor domEditor = new DOMEditor(this);

    /* static initialiser */
    static {
        INCLUDE_DIR = Constants.getIncludeDirectoryName();

        _isTemplatePage = new StandardFilePredicate();
        _isTemplatePage.setIncludeDirectoryNames(INCLUDE_DIR);

    }

    /**
     * This constructor supplies all the information the JSPBuilder object will
     * need to respond to FileListenerEvents.
     * 
     * @param contextPath
     *            The context path of the channel this JSPBuilder is responsible
     *            for.
     * @param ap
     *            A directory pair where the source is the root directory of the
     *            filesystem containing the HTML files, the HTML repositories,
     *            the application parameters, and the presentation parameters.
     *            The object will have already checked the validity of the
     *            source and destination directories in its constructor.
     * 
     * @param encoding
     *            The encoding that HTML files will be read and written to/from
     *            disk with.
     */
    public JSPBuilder(ContextPath contextPath, DirectoryPair dp, Charset encoding) {

        /* getAbsoluteFile () makes a new object */
        m_SourceDir = dp.getSource().getAbsoluteFile();
        m_DestinationDir = dp.getDestination().getAbsoluteFile();

        /* ContextPath is immutable */
        m_contextPath = contextPath;

        this.encoding = encoding;

    }

    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("m_contextPath", m_contextPath).append(
                "m_SourceDir", m_SourceDir).append("m_DestinationDir", m_DestinationDir).append("_sourceFile",
                _sourceFile).append("_destFile", _destFile).append("_debug", _debug).append("domEditor", domEditor)
                .toString();

    }

    /**
     * Set the debug level 0 - 99
     * 
     * @param debug
     *            The required debug level in the range 0 - 99.
     */
    public void setDebugLevel(int debug) {
        /* A negative arg is a mistake; go large in response */
        _debug = debug >= 0 ? debug : 99;
    }

    private final Charset encoding;

    /**
     * 
     * @return The encoding for all files handled by this
     */
    private Charset getEncoding() {
        return encoding;
    }

    private void build() {

        /**
         * PDL: A. Read source file
         * 
         * B. Determine type of HTML page. If the file is a template repository
         * (it's in an _include directory) hand the processing to a Template
         * object. Otherwise handle the page because it's a servable page, ie a
         * page that will be sent to a browser.
         * 
         * If we are handling the page: 1. Create DOM tree 2. DOM manipulations
         * 3. DOCTYPE configuration 4. Marked up HTML manipulations 5. Write to
         * disk.
         */

        /*
         * Should the refferring object split the _include/ files or the file
         * itself?
         */

        if (!_sourceFile.exists()) {
            log("build: source file did not exist: " + _sourceFile);
            return;
        }
        /*
         * A. Read source file
         */
        StringBuffer inHtml = new StringBuffer();
        try {
            FileUtilities.readFile(_sourceFile, inHtml, getEncoding());
        } catch (IOException e) {
            log("build: error reading source file: " + _sourceFile + "\n" + e);
            return;
        }

        /*
         * B. Determine type of HTML page.
         */
        int pageType = _isTemplatePage.accept(_sourceFile) ? Constants.PageType.TEMPLATE
                : Constants.PageType.PUBLIC_HTML;

        /*
         * 1. Create DOM tree
         */
        ParsedHtml parsedHtml = null;

        // 02-4-13 jdb
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter errStream = new PrintWriter(baos);

        try {
            parsedHtml = new ParsedHtml(inHtml.toString(), errStream);
        } catch (SAXException e) {
            log("build: error parsing source file: " + _sourceFile + "\n" + e);
            return;
        }

        StandardFinderSet finderSet = new StandardFinderSet();
        try {
            finderSet.setTemplateFinder(new StandardTemplateFinder(m_DestinationDir,
                    new FileLocation(_relativeLocation), getEncoding()));
            finderSet.setApplicationFinder(new StandardApplicationFinder(m_SourceDir, new FileLocation(
                    _relativeLocation)));
            finderSet.setPresentationFinder(new StandardPresentationFinder(m_SourceDir, new FileLocation(
                    _relativeLocation)));
        } catch (FileLocationException e) {
            log("build: error creating file location object: " + _relativeLocation + "\n" + e);
            return;
        }
        /*
         * 2. DOM manipulations
         */
        try {
            Document dom = parsedHtml.getDOM();
            // domEditor.editDOM ( dom, pageType, finderSet ) ;
            domEditor.insertPlaceholders(dom);
            // The DTD must be removed to allow a different DTD to be set
            // by ParsedHtml#toString
            domEditor.removeDoctype(dom);
            parsedHtml.setDOM(dom);
        } catch (Exception e) {
            log("build: error while editing DOM: " + _destFile + "\n" + e);
            return;
        }

        if (_debug >= 3)
            log("After insertPlaceholders: " + parsedHtml.toString());

        /*
         * 3. DOCTYPE configuration
         */
        String publicId = finderSet.getPresentationFinder().getDoctypePublicId();
        String systemId = finderSet.getPresentationFinder().getDoctypeSystemId();

        /*
         * Don't set the ids to null because that will stop the default ids from
         * applying.
         * 
         * Convert an empty string into a null to remove the public or system
         * id.
         */
        if (publicId != null) {
            parsedHtml.setPublicId("".equals(publicId) ? null : publicId);
        }
        if (systemId != null) {
            parsedHtml.setSystemId("".equals(systemId) ? null : systemId);
        }

        /*
         * 4. Marked up HTML manipulations
         */
        /* FIXME: Remove nasty hacks on pageType - it's undesirable. */

        // toString() has an important side effect of counting the parse errors.
        String hybridHtml = HtmlUtils.replaceHead(parsedHtml.toString(), inHtml);

        String html = "";
        switch (pageType) {
        case Constants.PageType.TEMPLATE:

            try {
                String baseName = _sourceFile.getName();
                Template t = new Template(baseName, hybridHtml, _destFile.getParentFile(), getEncoding());
                t.build();
            } catch (Exception e) {
                log("build: error building from template page: " + _sourceFile + "\n" + e);
                return;
            }
            /* Nasty hack continued */
            return;
            // break ;

        case Constants.PageType.PUBLIC_HTML:
            try {

                /*
                 * If any parse errors where encountered then pass all the
                 * errors and warnings, otherwise do not pass just warnings. The
                 * warnings generated by jtidy include warnings about missing
                 * table summary attributes and missing alt text for images
                 * which we are not interested in.
                 */
                html = editMarkedUpHtml(hybridHtml, finderSet, parsedHtml.getParseErrorCount() > 0 ? baos.toString()
                        : "");
            } catch (Exception e) {
                String msg = "build: error while editing the marked up HTML: " + _destFile + "\n" + e.getMessage()
                        + "\n" + e.toString();
                log(msg);
                html = ERROR_HTML_OPEN + msg + ERROR_HTML_CLOSE;
                e.printStackTrace();
                // return ;
            }
            break;

        default:

        }

        /*
         * 5. Write to disk if changed. Writing a unchanged file advances the modification
         * time causing pointless further updates.
         */
        try {
            String jsp = domEditor.replacePlaceholders(html, finderSet, getEncoding());

            boolean exists = _destFile.exists();

            if ((!exists) || (!FileUtilities.readFile(_destFile, getEncoding()).equals(jsp)))
                FileUtilities.writeString(_destFile, jsp, getEncoding());

        } catch (IOException e) {
            log("build: error writing destination file: " + _destFile + "\n" + e);
            return;
        }

    }

    /*
     * ----------------------------------------------------------------------------
     * A diagram showing the combinations of Server Active HTML with application
     * objects and template locations
     * 
     * Template location ------------------------------ | Embedded | Linked
     * ----------------------------------------------- Application | does't
     * exist | a | b object | | |
     * ----------------------------------------------- | does exist | c | d | | |
     * 
     * Notes
     * 
     * a) This is a fairly pointless combination. The process will parse the
     * template and inline the resulting HTML
     * 
     * b) The process will locate the nearest template as named in the template
     * attribute and insert a <jsp:include> action tag in the page
     * 
     * c) The embedded template is parsed and handled to the application object
     * somehow. (options: custom action tag, jsp:include to servlet with params)
     * 
     * d) The same as c) except the linked template is located first.
     */

    /*
     * FIXME: Note: we handle the simplest case possible first. We'll return to
     * the other more demanding cases later.
     * 
     * The simplest case is either no server active html or server active html
     * were every server active html is a reference to a static html template.
     * 
     * This allows me to not think about the requirement that a server active
     * html section resolve recursively.
     */
    /**
     * Do edits to the HTML in text format.
     * 
     * PDL:
     * 
     * 1. Wrap the page in a default ServerActiveHtml object for handling
     * unhandled placeholders such as the globally applicable "date" and
     * "opportunity-count" values. 2. Parse the HTML into a list of object
     * trees. Because we wrapped the page this will be exactly one object tree
     * with the deafult ServerActiveHtml object at the top. 3. For each object
     * tree emit the corresponding JSP script, resolving application code
     * bindings relative to the current page. Supply a default application
     * binding where no binding is found.
     * 
     * @param html
     *            The HTML to parse
     * @param finderSet
     *            The set of finders to do configuration look-ups with
     */
    protected String editMarkedUpHtml(String html, FinderSet finderSet, String errors) throws MarkedUpHtmlException {

        /*
         * Insert errors
         */
        if (errors.length() > 0) {

            /* The HTML went through jtidy; there must be a body element */
            int endOfbody = html.indexOf(">", html.toLowerCase().indexOf("<body"));

            html = html.substring(0, endOfbody + 1) + "\n<table border=1 bgcolor=red><tr><td><pre>"
                    + HtmlUtils.convertToHtml(errors) + "</pre></td></tr></table>\n" + html.substring(endOfbody + 1);
        }

        /*
         * 1. Wrap the page.
         */

        StringBuilder wrappedHtml = new StringBuilder(html.length() + 100);
        wrappedHtml.append("<!-- name=\"-base\" -->");
        wrappedHtml.append(html);
        wrappedHtml.append("<!-- name=\"/-base\" -->");

        MarkedUpHtmlParser parser = new MarkedUpHtmlParser(wrappedHtml.toString());

        try {
            /*
             * These two statements perform a similar job to
             * MarkedUpHtmlParser.resolveTemplates( Object, Set, TemplateFinder,
             * Element) except they omit the code which performs 'use-element'
             * replacements on the top level list of objects. Since the html is
             * wrapped in a non-template ServerActiveHtml in this method the
             * lack of this code is not a problem so we omit the replacement of
             * Placeholder with the corresponding Elements here.
             */
            parser.parse();
            parser.resolveTemplates(finderSet.getTemplateFinder());
        } catch (MarkedUpHtmlException e) {
            throw new MarkedUpHtmlException("There was a problem encountered while parsing the page:\n"
                    + e.getMessage());
        }

        List tree = parser.getGrove();

        if (tree.size() != 1)
            throw new MarkedUpHtmlException("The page was parsed but did not have exactly one root object");

        /*
         * If we need to handle more than one object (which would happen if we
         * stopped using the default ServerActiveHtml which wraps the page) then
         * we need to use a loop here.
         */
        try {
            JspSerializable rootObject = (JspSerializable) tree.get(0);
            
            ScriptWriter scWr = new ScriptWriter(rootObject, getEncoding(), finderSet);
            return scWr.getScript();
            
        } catch (ScriptWriterException e) {
            throw new MarkedUpHtmlException("Failed to create script from object tree:\n" + e.toString());
        }
    }

    /**
     * FIXME: use proper logger passed in at construction.
     */
    public void log(String msg) {
        System.out.println("[" + this.getClass().getName() + "] " + msg);
    }

    public void log(String message, Exception e) {
        log(message + "\n" + e.toString());
    }

    /**
     * Any errors must be handled within this class as the object we are
     * listening to can not have error handling specific to a JSPBuilder object
     * in it.
     * 
     * Both fileAdded and fileDeleted assume the files represented by the
     * received events are in 'add-order' or 'delete-order'. This allows new
     * files to be created without checking for the existence of the parent
     * directory. As a JSPBuilder handles only files the order is not critical
     * for fileDeleted events.
     * 
     * If these files and directory are deleted
     * 
     * about/ about/index.html about/info.html
     * 
     * then fileDeleted receives the files in this order (note lack of
     * directory)
     * 
     * about/index.html about/info.html
     * 
     * Similarly if these files have been added
     * 
     * /1/2/index.html /1/about.html index.html
     * 
     * then fileAdded receives the files in this order (note lack of
     * directories)
     * 
     * index.html /1/about.html /1/2/index.html
     * 
     * FIXME: add proper logger
     */
    public void fileAdded(FileListenerEvent e) {
        fileAddedInternal(e);
    }

    private void fileAddedInternal(FileListenerEvent e) {
        File sourceFile = new File(m_SourceDir, e.getLocation());
        File destFile = new File(m_DestinationDir, e.getLocation());

        if (_debug >= 1)
            log("fileAdded: [" + m_contextPath + "] " + e.getLocation());

        if (!sourceFile.isFile()) {
            log("Source was not a file: " + sourceFile);
            return;
        }

        if (destFile.exists())
            log("Directory or file already existed. Continued processing anyway: " + destFile);

        _relativeLocation = e.getLocation();
        _sourceFile = sourceFile;
        _destFile = destFile;
        build();
    }

    public void fileModified(FileListenerEvent e) {
        File sourceFile = new File(m_SourceDir, e.getLocation());
        File destFile = new File(m_DestinationDir, e.getLocation());

        if (_debug >= 1)
            log("fileModified: [" + m_contextPath + "] " + e.getLocation());

        if (!sourceFile.isFile()) {
            log("Source was not a file: " + sourceFile);
            return;
        }

        _relativeLocation = e.getLocation();
        _sourceFile = sourceFile;
        _destFile = destFile;
        build();
    }

    public void fileDeleted(FileListenerEvent e) {
        fileDeletedInternal(e);
    }

    private void fileDeletedInternal(FileListenerEvent e) {
        File sourceFile = new File(m_SourceDir, e.getLocation());
        File destFile = new File(m_DestinationDir, e.getLocation());

        if (_debug >= 1)
            log("fileDeleted: [" + m_contextPath + "] " + e.getLocation());

        /*
         * If the deleted file was a template file we must ask a Template object
         * to handle the deletion.
         */
        if (_isTemplatePage.accept(destFile)) {
            try {
                String baseName = destFile.getName();
                Template t = new Template(baseName, destFile.getParentFile(), getEncoding());
                t.delete();
            }
            /* Use ex to not mask e */
            catch (Exception ex) {
                log("build: error deleting templates for file: " + sourceFile + "\n" + e);
            }
            return;
        }

        if (!destFile.isFile()) {
            log("Destination was not a file: " + destFile);
            return;
        }

        if (!destFile.delete())
            log("Failed to delete file: " + destFile);

    }

    public void targetFileAdded(FileListenerEvent e) {
        fileAddedInternal(e);
    }

    public void targetFileDeleted(FileListenerEvent e) {
        fileDeletedInternal(e);
    }

}