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
 * DOMEditor.java
 *
 * Created on 09 September 2002, 12:55
 */

package org.ntropa.build.jsp;

import java.nio.charset.Charset;

import org.ntropa.build.html.MarkedUpHtmlException;
import org.ntropa.utility.DOMUtils;
import org.ntropa.utility.HtmlUtils;
import org.ntropa.utility.Logger;
import org.ntropa.utility.StringUtilities;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is used to edit the &lt;head&gt; section of a HTML page.
 * 
 * @author jdb
 * @version $Id: DOMEditor.java,v 1.2 2006/03/22 13:50:55 jdb Exp $
 */
public class DOMEditor {

    private final int debug = 0;

    // public static int debug = 0;

    private final Logger logger;

    /* HTML related constants */
    private static final String META_CONTENT_ATTR = "content";

    private static final String META_NAME_ATTR = "name";

    private static final String META_HTTP_EQUIV_ATTR = "http-equiv";

    private static final String META_PRAGMA = "Pragma";

    private static final String META_NO_CACHE = "no-cache";

    /*
     * The data of the comments inserted into the head section for later
     * replacement
     */
    static final String META_CONTENT_TYPE_PLACEHOLDER = "meta-placeholder:content-type-tag";

    private static final String META_KEYWORDS_PLACEHOLDER = "meta-placeholder:keywords-tag";

    private static final String META_DESCRIPTION_PLACEHOLDER = "meta-placeholder:description-tag";

    private static final String META_REFRESH_PLACEHOLDER = "meta-placeholder:refresh-tag";

    private static final String META_CACHE_CONTROL_PLACEHOLDER = "meta-placeholder:cache-control-tag";

    /**
     * Creates new DOMEditor
     */
    public DOMEditor(Logger logger) {

        if (logger == null)
            throw new IllegalArgumentException("logger was null");

        this.logger = logger;
    }

    /**
     * Do edits to the DOM.
     * <p>
     * If a tag is not found then insert a placeholder so the WPS can later add
     * the tag if required by the values found in presentation.properties and
     * application.properties, or delete the placeholder.
     * 
     * @param document
     *            The DOM to edit by adding placeholder comments.
     */
    public void insertPlaceholders(Node document) throws MarkedUpHtmlException {

        if (document == null)
            throw new IllegalArgumentException("document was null");

        if (debug >= 3)
            logger.log("DOM passed to editDOM: " + DOMUtils.toString(document));

        Document doc = (Document) document;

        /* docElement will be null for the DOM created from a empty HTML file */
        Element docElement = doc.getDocumentElement();
        if (docElement == null) {
            logger.log("editDOM: error getting document element: " + document);
            throw new NullPointerException("editDOM: error getting document element: " + document);
        }

        /*
         * Get a list of meta tags and see which ones of our required set are
         * missing
         */
        NodeList metaTags = docElement.getElementsByTagName("meta");

        if (debug >= 3) {
            logger.log("editDOM: metaTags:" + metaTags);
            logger.log("editDOM: metaTags.getLength:" + metaTags.getLength());
            for (int i = 0; i < metaTags.getLength(); i++)
                logger.log("editDOM: metaTags.item (" + i + ")\n" + DOMUtils.toString(metaTags.item(i)));
        }

        boolean flag_refresh = false;
        boolean flag_keywords = false;
        boolean flag_description = false;

        /*
         * Browser cache control. Testing with IE 5/Windows 200 Pro over the LAN
         * and Mozilla 2001090111 running over the Internet (running in Sydney,
         * web server in London) showed
         * 
         * <meta http-equiv="Pragma" content="no-cache">
         * 
         * to be a reliable way of ensuring a page was reserved from the web
         * server. This even worked when IE was set to 'Never check for newer
         * versions of stored pages'.
         * 
         * This tag does not interact with web proxies (in general, some may
         * read it with special configuration). It operates at the browser level
         * and relates to the local web page cache maintained by the browser.
         */
        boolean flagBrowserNoCache = false;

        Node contentType = null;

        for (int x = 0; x < metaTags.getLength(); x++) {
            Node child = metaTags.item(x);

            if (hasAttributeWithValue(child, "http-equiv", "content-type"))
                contentType = child;

            if (hasAttributeWithValue(child, "name", "keywords"))
                flag_keywords = true;

            if (hasAttributeWithValue(child, "name", "description"))
                flag_description = true;

            if (hasAttributeWithValue(child, "http-equiv", "refresh"))
                flag_refresh = true;

            /*
             * Browser cache control
             */
            if (hasAttributeWithValue(child, "http-equiv", "Pragma")
                    && hasAttributeWithValue(child, "content", "no-cache"))
                flagBrowserNoCache = true;

        }

        // Delete in preparation for replacement.
        if (contentType != null) {
            contentType.getParentNode().removeChild(contentType);
        }

        /* Get a reference to the <head> element so we can add children to it */
        org.w3c.dom.NodeList headElements = doc.getElementsByTagName("head");
        if (debug >= 4) {
            logger.log("editDOM: headElements:" + headElements);
            logger.log("editDOM: headElements.getLength():" + headElements.getLength());
            for (int i = 0; i < headElements.getLength(); i++)
                logger.log("editDOM: headElements.item (" + i + ")\n" + DOMUtils.toString(headElements.item(i)));
        }

        /* The HTML went through jtidy; there must be a head element */
        if (headElements.getLength() != 1)
            throw new MarkedUpHtmlException("insertPlaceholders: There was not exactly 1 <head> element:\n"
                    + DOMUtils.toString(document));

        Node headElement = headElements.item(0);

        Comment contentTypePlaceholder = doc.createComment(META_CONTENT_TYPE_PLACEHOLDER);
        headElement.appendChild(contentTypePlaceholder);

        if (!flag_keywords) {
            Comment placeholder = doc.createComment(META_KEYWORDS_PLACEHOLDER);
            headElement.appendChild(placeholder);
        }

        if (!flag_description) {
            Comment placeholder = doc.createComment(META_DESCRIPTION_PLACEHOLDER);
            headElement.appendChild(placeholder);
        }

        if (!flag_refresh) {
            Comment placeholder = doc.createComment(META_REFRESH_PLACEHOLDER);
            headElement.appendChild(placeholder);
        }

        /*
         * If there is no browser cache control meta tag add one if necessary.
         */
        if (!flagBrowserNoCache) {
            Comment placeholder = doc.createComment(META_CACHE_CONTROL_PLACEHOLDER);
            headElement.appendChild(placeholder);
        }

        if (debug >= 3)
            logger.log("DOM at end of editDOM: " + DOMUtils.toString(document));

    }

    /**
     * Return true if at least one attribute and value pair match the arguments.
     * <p>
     * The matching is case insensitive.
     * <p>
     * For example the method will return true these arguments
     * <ul>
     * <li> child = &lt;meta content=&quot;2; URL=course-info.html&quot;
     * http-equiv=&quot;Refresh&quot;&gt;
     * <li> attrName = http-equiv
     * <li> attrValue = refresh
     * </ul>
     * 
     * @return true is the Node is an Element and has at least one attribute
     *         with the required name and value.
     */
    private static boolean hasAttributeWithValue(Node child, String attrName, String attrValue) {
        if (child.getNodeType() == child.ELEMENT_NODE) {
            NamedNodeMap nnm = child.getAttributes();
            for (int c = 0; c < nnm.getLength(); c++) {
                Node attr = nnm.item(c);
                if (attrName.equalsIgnoreCase(attr.getNodeName()) && attrValue.equalsIgnoreCase(attr.getNodeValue()))
                    return true;
            }
        }
        return false;
    } // End of method hasAttributeWithValue

    private static final String COMMENT_OPEN = "<!--";

    private static final String COMMENT_CLOSE = "-->";

    /**
     * Replace each placeholder inserted by {@link #insertPlaceholders} with
     * either a corresponding tag or the empty string.
     */
    public String replacePlaceholders(String html, FinderSet finderSet, Charset encoding) {

        PresentationFinder pf = finderSet.getPresentationFinder();
        if (pf == null)
            throw new NullPointerException("pf was null");

        ApplicationFinder af = finderSet.getApplicationFinder();
        if (af == null)
            throw new NullPointerException("af was null");

        if (encoding == null)
            throw new NullPointerException("encoding was null");

        String contentType = "text/html; charset=" + encoding.name();

        // #insertPlaceholders ensures this placeholder is present.
        if (html.indexOf(META_CONTENT_TYPE_PLACEHOLDER) != -1) {
            StringBuffer sb = new StringBuffer();
            makeMetaTagForJsp(META_HTTP_EQUIV_ATTR, "content-type", META_CONTENT_ATTR, contentType, sb);
            html = StringUtilities.replace(html, COMMENT_OPEN + META_CONTENT_TYPE_PLACEHOLDER + COMMENT_CLOSE, sb
                    .toString());
        } else {
            // This error occurs when a full page template is referenced and the
            // template is missing.
            // In this case the html provided to this method looks like this
            // component_3 = new com.studylink.sao.StandardFragment () ;
            // component_3.setHtml ( "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML
            // 4.01 Transitional//EN\"\n
            // \"http://www.w3.org/TR/html4/loose.dtd\">\n<b>Warning: missing
            // template: &quot;no-such-template&quot;</b>" ) ;

            /*
             * When the tag is missing because a reference to a missing full
             * page template could not be resolved do not throw an exception.
             * This is because this situation is understood and some output must
             * be provided to the user. When a new situation is encountered
             * throw an exception.
             * 
             * TODO: Make JSPBuilder write an error page with any thrown
             * exception included.
             */
            boolean knownSituation = isConsistentWithMissingFullPageTemplate(html);
            if (debug >= 3)
                logger.log("isConsistentWithMissingFullPageTemplate: " + knownSituation);

            if (!knownSituation)
                throw new IllegalArgumentException("'" + META_CONTENT_TYPE_PLACEHOLDER
                        + "' was not present in the HTML:" + html);
        }

        if (html.indexOf(META_KEYWORDS_PLACEHOLDER) != -1) {
            String keywords = pf.getKeywords();
            StringBuffer sb = new StringBuffer();
            if (keywords != null) {
                makeMetaTagForJsp(META_NAME_ATTR, "keywords", META_CONTENT_ATTR, HtmlUtils.convertToHtml(keywords), sb);
            }
            html = StringUtilities.replace(html, COMMENT_OPEN + META_KEYWORDS_PLACEHOLDER + COMMENT_CLOSE, sb
                    .toString());
        }

        if (html.indexOf(META_DESCRIPTION_PLACEHOLDER) != -1) {
            String description = pf.getDescription();
            StringBuffer sb = new StringBuffer();
            if (description != null) {
                makeMetaTagForJsp(META_NAME_ATTR, "description", META_CONTENT_ATTR, HtmlUtils
                        .convertToHtml(description), sb);
            }
            html = StringUtilities.replace(html, COMMENT_OPEN + META_DESCRIPTION_PLACEHOLDER + COMMENT_CLOSE, sb
                    .toString());
        }

        if (html.indexOf(META_REFRESH_PLACEHOLDER) != -1) {
            StringBuffer sb = new StringBuffer();
            makeMetaTagForJsp(META_HTTP_EQUIV_ATTR, "refresh", META_CONTENT_ATTR,
            /* Session timeout default in Tomcat is 1800 seconds */
            "1740", sb);
            html = StringUtilities
                    .replace(html, COMMENT_OPEN + META_REFRESH_PLACEHOLDER + COMMENT_CLOSE, sb.toString());
        }

        if (html.indexOf(META_CACHE_CONTROL_PLACEHOLDER) != -1) {
            StringBuffer sb = new StringBuffer();
            if (af.isBrowserCacheDisable()) {
                makeMetaTagForJsp(META_HTTP_EQUIV_ATTR, META_PRAGMA, META_CONTENT_ATTR, META_NO_CACHE, sb);
            }
            html = StringUtilities.replace(html, COMMENT_OPEN + META_CACHE_CONTROL_PLACEHOLDER + COMMENT_CLOSE, sb
                    .toString());
        }

        return html;
    }

    /**
     * 
     * @param jsp
     * @return true iff html, head and body open and close tags are not present
     *         and a missing template warning is present. The consistency test
     *         would improve if it were even more conservative.
     */
    private boolean isConsistentWithMissingFullPageTemplate(String jsp) {
        jsp = jsp.toLowerCase();
        if (debug >= 3)
            logger.log("isConsistentWithMissingFullPageTemplate: jsp: " + jsp);
        String missingTags[] = { "html", "head", "body" };
        for (int i = 0; i < missingTags.length; i++) {
            String tag = missingTags[i];
            if (jsp.indexOf("<" + tag) > 0)
                return false;
            if (jsp.indexOf("</" + tag) > 0)
                return false;
            if (debug >= 3)
                logger.log("isConsistentWithMissingFullPageTemplate: tag: " + tag);
        }

        // component_3.setHtml ( "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML
        // 4.01 Transitional//EN\"\n
        // \"http://www.w3.org/TR/html4/loose.dtd\">\n<b>Warning: missing
        // template: &quot;no-such-template&quot;</b>" ) ;

        if (debug >= 3)
            logger.log("isConsistentWithMissingFullPageTemplate: indexOf: "
                    + jsp.indexOf("<b>Warning: missing template:".toLowerCase()));
        if (jsp.indexOf("<b>Warning: missing template:".toLowerCase()) < 0)
            return false;

        return true;
    }

    private void makeMetaTagForJsp(String attrOneName, String attrOneValue, String attrTwoName, String attrTwoValue,
            StringBuffer result) {
        /*
         * escape quotes, http-equiv=\"Pragma\", as the string is an argument to
         * a method.
         */
        result.append("<meta ");
        result.append(attrOneName);
        result.append(" = \\\"");
        result.append(attrOneValue);
        result.append("\\\" ");
        result.append(attrTwoName);
        result.append(" = \\\"");
        result.append(attrTwoValue);
        result.append("\\\" >");
    }

    /**
     * Safely remove the documentType node.
     * 
     * This method was added to allow the correct DTD to be set in ParsedHtml
     * 
     * @param document
     *            If the documentType element is present it will be removed
     */
    public void removeDoctype(Document document) {

        DocumentType d = document.getDoctype();
        if (d != null)
            document.removeChild(d);

    }

    /**
     * Do edits to the DOM.
     * <p>
     * If a tag is not found then insert a placeholder so the WPS can later add
     * the tag if required by the values found in presentation.properties and
     * application.properties, or delete the placeholder.
     * 
     * @param document
     *            The DOM to edit
     * @param pageType
     *            The type of the page (Nasty hack)
     * @page The <code>FinderSet</code> to locate / public void editDOM ( Node
     *       document, int pageType, FinderSet finderSet ) throws
     *       MarkedUpHtmlException {
     * 
     * if ( document == null ) throw new IllegalArgumentException ( "document
     * was null" ) ;
     * 
     * if ( finderSet == null ) throw new IllegalArgumentException ( "finderSet
     * was null" ) ; /* FIXME: Remove nasty hacks on pageType - it's
     * undesirable. * / if ( pageType != Constants.PageType.PUBLIC_HTML ) return ;
     * 
     * if ( debug >= 3 ) logger.log ( "DOM passed to editDOM: " +
     * DOMUtils.toString ( document ) ) ;
     * 
     * Document doc = (Document) document ; /* gelement will be null for the DOM
     * created from a empty HTML file * / org.w3c.dom.Element gelement =
     * doc.getDocumentElement (); if ( gelement == null ) { logger.log (
     * "editDOM: error getting document element: " + document ) ; throw new
     * NullPointerException ( "editDOM: error getting document element: " +
     * document ) ; } /* Get a list of meta tags and see which ones of our
     * required set are missing / NodeList metaTags =
     * gelement.getElementsByTagName ( "meta" );
     * 
     * if ( debug >= 3 ) { logger.log ("editDOM: metaTags:" + metaTags );
     * logger.log ("editDOM: metaTags.getLength:" + metaTags.getLength () ); for (
     * int i = 0 ; i < metaTags.getLength () ; i++ ) logger.log ( "editDOM:
     * metaTags.item (" + i + ")\n" + DOMUtils.toString ( metaTags.item ( i ) ) ) ; }
     * 
     * boolean flag_refresh = false ; boolean flag_keywords = false ; boolean
     * flag_description = false; /* Browser cache control. Testing with IE
     * 5/Windows 200 Pro over the LAN and Mozilla 2001090111 running over the
     * Internet (running in Sydney, web server in London) showed
     * 
     * <meta http-equiv="Pragma" content="no-cache">
     * 
     * to be a reliable way of ensuring a page was reserved from the web server.
     * This even worked when IE was set to 'Never check for newer versions of
     * stored pages'.
     * 
     * This tag does not interact with web proxies (in general, some may read it
     * with special configuration). It operates at the browser level and relates
     * to the local web page cache maintained by the browser. / boolean
     * flagBrowserNoCache = false ;
     * 
     * for ( int x = 0; x < metaTags.getLength (); x++ ) { Node child =
     * metaTags.item (x);
     * 
     * if ( hasAttributeWithValue (child, "name", "keywords")) flag_keywords =
     * true;
     * 
     * if ( hasAttributeWithValue (child, "name", "description") )
     * flag_description = true;
     * 
     * if ( hasAttributeWithValue (child, "http-equiv", "refresh" ) )
     * flag_refresh = true; /* Browser cache control / if (
     * hasAttributeWithValue (child, "http-equiv", "Pragma" ) &&
     * hasAttributeWithValue (child, "content", "no-cache") ) flagBrowserNoCache =
     * true; }
     * 
     * 
     * PresentationFinder pf = finderSet.getPresentationFinder () ; if ( pf ==
     * null ) throw new NullPointerException ( "pf was null" ) ;
     * 
     * ApplicationFinder af = finderSet.getApplicationFinder () ; if ( af ==
     * null ) throw new NullPointerException ( "af was null" ) ; /* Get a
     * reference to the <head> element so we can add children to it * /
     * org.w3c.dom.NodeList headElements = doc.getElementsByTagName ( "head" );
     * if ( debug >= 4 ) { logger.log ("editDOM: headElements:" + headElements );
     * logger.log ("editDOM: headElements.getLength():" + headElements.getLength () );
     * for ( int i = 0 ; i < headElements.getLength () ; i++ ) logger.log (
     * "editDOM: headElements.item (" + i + ")\n" + DOMUtils.toString (
     * headElements.item ( i ) ) ) ; } /* The HTML went through jtidy; there
     * must be a head element * / if ( headElements.getLength () != 1 ) throw
     * new MarkedUpHtmlException ( "editDOM: There was not exactly 1 <head>
     * element:\n" + DOMUtils.toString ( document ) ) ;
     * 
     * Node headElement = headElements.item ( 0 ) ;
     * 
     * if ( !flag_keywords ) { String keywords = pf.getKeywords () ; if (
     * keywords != null ) { Element element = doc.createElement ("meta");
     * //020416 jdb element.setAttributeNode(doc.createAttribute("name"));
     * element.setAttribute ( META_NAME_ATTR, "keywords"); element.setAttribute (
     * META_CONTENT_ATTR, keywords);
     * 
     * headElement.appendChild ( element ) ; } }
     * 
     * if ( !flag_description ) { String description = pf.getDescription () ; if (
     * description != null ) { Element element = doc.createElement ("meta"); if (
     * debug >= 4 ) logger.log ("editDOM: flag_description: element: " + element );
     * //020416 jdb element.setAttributeNode(doc.createAttribute("name"));
     * element.setAttribute ( META_NAME_ATTR, "description");
     * element.setAttribute ( META_CONTENT_ATTR, description );
     * 
     * headElement.appendChild ( element ) ; } }
     * 
     * if ( !flag_refresh ) { Element element = doc.createElement ("meta"); if (
     * debug >= 4 ) logger.log ("editDOM: flag_refresh: element: " + element );
     * //020416 jdb element.setAttributeNode(doc.createAttribute("http-equiv"));
     * element.setAttribute (META_HTTP_EQUIV_ATTR, "refresh"); /* Session
     * timeout default in Tomcat is 1800 seconds * / element.setAttribute (
     * META_CONTENT_ATTR, "1740" );
     * 
     * headElement.appendChild ( element ) ; } /* If there is no browser cache
     * control meta tag add one if necessary. / if ( ! flagBrowserNoCache ) { if (
     * af.isBrowserCacheDisable () ) { Element element = doc.createElement
     * ("meta"); if ( debug >= 4 ) logger.log ("editDOM: flagBrowserNoCache:
     * element: " + element ); //element.setAttributeNode (doc.createAttribute
     * (??)); element.setAttribute (META_HTTP_EQUIV_ATTR, META_PRAGMA );
     * element.setAttribute ( META_CONTENT_ATTR, META_NO_CACHE );
     * 
     * headElement.appendChild ( element ) ; } }
     * 
     * if ( debug >= 3 ) logger.log ( "DOM at end of editDOM: " +
     * DOMUtils.toString ( document ) ) ; if ( debug >= 4 ) logger.log
     * ("editDOM: end of method" ); }
     */

}
