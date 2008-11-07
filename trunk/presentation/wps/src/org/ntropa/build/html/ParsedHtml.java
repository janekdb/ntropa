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
 * ParsedHTML.java
 *
 * Created on 03 October 2001, 15:54
 */

/* FIXME
 * The output of the serializer includes this
 * <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN"
 *                      "http://www.w3.org/TR/WD-html-in-xml/DTD/xhtml1-strict.dtd">
 *
 * Shouldn't this be the HTML 3.2 DOCTYPE? Why the XHTML DOCTYPE?
 * jdb
 */

package org.ntropa.build.html;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.xml.serialize.BaseMarkupSerializer;
import org.apache.xml.serialize.HTMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.ntropa.utility.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

/*
 * 
 * Note about versions of Tidy.jar.
 * 
 * With the version of Tidy.jar downloaded from the jtidy site on sourceforge I
 * had this error:
 * 
 * Testcase: testBasicFileEventBehaviour took 0.541 sec Caused an ERROR try to
 * access field org.w3c.tidy.ParserImpl._parseHead from class
 * org.w3c.tidy.ParserImpl$ParseHTML java.lang.IllegalAccessError: try to access
 * field org.w3c.tidy.ParserImpl._parseHead from class
 * org.w3c.tidy.ParserImpl$ParseHTML at
 * org.w3c.tidy.ParserImpl$ParseHTML.parse(ParserImpl.java) at
 * org.w3c.tidy.ParserImpl.parseDocument(ParserImpl.java) at
 * org.w3c.tidy.Tidy.parse(Tidy.java) at org.w3c.tidy.Tidy.parseDOM(Tidy.java)
 * at org.ntropa.build.html.ParsedHtml.getDOM(ParsedHtml.java:85) at
 * org.ntropa.build.html.ParsedHtml.<init>(ParsedHtml.java:66) at
 * org.ntropa.build.jsp.JSPBuilder.build(JSPBuilder.java:460)
 * 
 * The error went away with the version of Tidy.jar that comes with httpunit
 * (sourceforge).
 * 
 * File sizes.
 * 
 * httpunit, Tidy.jar: 166960 jtidy, Tidy.jar : 147859
 * 
 */
/**
 * This class manages a DOM representation of a HTML page.
 * 
 * The class started as a copy of com.meterware.httpunit.ReceivedPage.java, see
 * copyright notice elsewhere.
 * 
 * @author jdb
 * @version $Id: ParsedHtml.java,v 1.14 2006/03/22 16:40:10 jdb Exp $
 */
public class ParsedHtml {

    private Document _rootNode;

    private PrintWriter _errorOut;

    /*
     * This was set to "iso-8859-1" in one place in the httpunit sources. As I
     * don't need to deal in more than one character set yet I'm leaving it out
     * for simplicity.
     */
    // private String _characterSet;
    /*
     * public ReceivedPage( URL url, String parentTarget, String pageText,
     * String characterSet ) throws SAXException { super( url, parentTarget,
     * getDOM( pageText ), characterSet ); setBaseAttributes(); }
     */
    public ParsedHtml(String pageText) throws SAXException {

        if (pageText == null)
            throw new NullPointerException("pageText was null");

        _rootNode = getDOM(pageText);

        // setBaseAttributes ();
    }

    public ParsedHtml(String pageText, PrintWriter errorOut) throws SAXException {

        if (pageText == null)
            throw new NullPointerException("pageText was null");

        if (errorOut == null)
            throw new NullPointerException("errorOut was null");

        _errorOut = errorOut;
        _rootNode = getDOM(pageText);
        /*
         * This flush is essential if the PrintWriter has been created like this
         * 
         * ByteArrayOutputStream baos = new ByteArrayOutputStream () ;
         * PrintWriter errorOut = new PrintWriter ( baos ) ;
         * 
         * Without the invocation the backing array may be empty even if text
         * was written to the PrintWriter. This was happening for the
         * BROKEN_HTML page in ParsedHtmlTest.
         */
        errorOut.flush();

    }

    // /**
    // * Returns the title of the page.
    // */
    // public String getTitle() throws SAXException {
    // NodeList nl = getDOM().getElementsByTagName("title");
    // if (nl.getLength() == 0)
    // return "";
    // if (!nl.item(0).hasChildNodes())
    // return "";
    // return nl.item(0).getFirstChild().getNodeValue();
    // }

    private int _parseErrorCount = 0;

    /**
     * Tidy mixes warnings in with errors in the text it sends to _errorOut so
     * we provide a way for the client to check if any 'serious' errors
     * occurred.
     * 
     * @return The number of parse errors encountered in the last parse
     */
    public int getParseErrorCount() {
        return _parseErrorCount;
    }

    private Document getDOM(String pageText) throws SAXException {
        try {
            Tidy tidy = getParser();
            Document doc = tidy.parseDOM(new ByteArrayInputStream(pageText.getBytes(getUTFEncodingName())), null);
            maybeRemoveTitleElement(doc, pageText);
            _parseErrorCount = tidy.getParseErrors();

            return doc;
            // return getParser ().parseDOM ( new ByteArrayInputStream (
            // pageText.getBytes ( getUTFEncodingName () ) ), null );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding failed");
        }
    }

    private void maybeRemoveTitleElement(Document doc, String pageText) {
        /*
         * If the title element is empty it might have been added by Tidy. If
         * Tidy added the title element remove it
         */

        // System.out.println("maybeRemoveTitleElement:\n" +
        // DOMUtils.toString(doc));
        /*
         * PDL: If an empty title exists remove it unless the input text
         * included an empty title. This is cleaner than checking the input text
         * for no title element and then removing the title element from the
         * Node.
         */
        NodeList heads = doc.getElementsByTagName("head");
        if (heads.getLength() != 1) {
            /*
             * A bad input page might not have exactly one head node. This
             * should be ignored because this method is only concerned with
             * handling valid Documents.
             */
            return;
        }
        Element head = (Element) heads.item(0);
        NodeList titles = head.getElementsByTagName("title");
        if (titles.getLength() != 1) {
            /*
             * Ignore for the same reason that the not exactly one head
             * condition was ignored.
             */
            return;
        }
        Element title = (Element) titles.item(0);
//        System.out.println("maybeRemoveTitleElement: title:\n" + DOMUtils.toString(title));
        boolean hasContent = title.hasChildNodes() && title.getFirstChild().getNodeValue().length() > 0;
//        System.out.println("maybeRemoveTitleElement: title.hasContent: " + hasContent);
        if (hasContent) {
            return;
        }
        
        /* Do not remove the empty title if it was present in the input text */
        boolean emptyTitleWasDesired = pageText.toLowerCase().indexOf("<title></title>") != -1;
        if(emptyTitleWasDesired){
            return;
        }
        head.removeChild(title);
    }

    /**
     * Returns a copy of the domain object model associated with this page.
     */
    public Document getDOM() {
        return (Document) _rootNode.cloneNode( /* deep */true);
    }

    /**
     * Saves a copy of the passed in domain object model. The association with
     * the initial page may no longer be valid.
     */
    public void setDOM(Document rootNode) {
        _rootNode = (Document) rootNode.cloneNode( /* deep */true);
    }

    // ---------------------------------- private members
    // --------------------------------

    private static String _utfEncodingName;

    private static String getUTFEncodingName() {
        if (_utfEncodingName == null) {
            String versionNum = System.getProperty("java.version");
            if (versionNum.startsWith("1.1"))
                _utfEncodingName = "UTF8";
            else
                _utfEncodingName = "UTF-8";
        }
        return _utfEncodingName;
    }

    /*
     * private void setBaseAttributes () throws SAXException { NodeList nl =
     * ((Document) getDOM ()).getElementsByTagName ( "base" ); if (nl.getLength () ==
     * 0) return; try { applyBaseAttributes ( NodeUtils.getNodeAttribute (
     * nl.item (0), "href" ), NodeUtils.getNodeAttribute ( nl.item (0), "target" ) ); }
     * catch (MalformedURLException e) { throw new RuntimeException ( "Unable to
     * set document base: " + e ); } }
     * 
     * 
     * private void applyBaseAttributes ( String baseURLString, String
     * baseTarget ) throws MalformedURLException { if (baseURLString.length () >
     * 0) { this.setBaseURL ( new URL ( baseURLString ) ); } if
     * (baseTarget.length () > 0) { this.setBaseTarget ( baseTarget ); } }
     */

    private/* static */Tidy getParser() {
        Tidy tidy = new Tidy();
        tidy.setCharEncoding(org.w3c.tidy.Configuration.UTF8);

        /*
         * No 'Parsing X', guessing DTD or summary.
         */
        tidy.setQuiet(true);

        /*
         * TidyMark - if true add meta element indicating tidied doc
         */
        tidy.setTidyMark(false);

        // tidy.setShowWarnings ( HttpUnitOptions.getParserWarningsEnabled () );
        // 01-12-21 tidy.setShowWarnings ( false );
        /*
         * Changing this to true allowed _errorOut to get warnings. (More likely
         * it was the lack of a PrintWriter.flush that we were experiencing.)
         */
        tidy.setShowWarnings(false);

        // System.out.println("getOnlyErrors: " + tidy.getOnlyErrors () );
        /*
         * 02-4-14, try to stop cast bobby. Didn't work, warning about missing
         * alt come through
         */
        tidy.setOnlyErrors(true);

        /* Use stream for errors if available */
        if (_errorOut != null)
            tidy.setErrout(_errorOut);

        return tidy;
    }

    /*
     * 02-4-13 jdb private Tidy getParser () { Tidy tidy = new Tidy ();
     * tidy.setCharEncoding ( org.w3c.tidy.Configuration.UTF8 ); tidy.setQuiet (
     * true ); /* TidyMark - if true add meta element indicating tidied doc * /
     * tidy.setTidyMark ( false );
     * 
     * //tidy.setShowWarnings ( HttpUnitOptions.getParserWarningsEnabled () );
     * //01-12-21 tidy.setShowWarnings ( false ); /* Changing this to true
     * allowed _errorOut to get warnings * / tidy.setShowWarnings ( true ); //
     * tidy.setOnlyErrors ( true ); /* Use stream for errors if available * / if (
     * _errorOut != null ) tidy.setErrout ( _errorOut ) ;
     * 
     * return tidy; }
     */

    private static final String DTD_PUBLIC_DEFAULT = "-//W3C//DTD HTML 4.01 Transitional//EN";

    private static final String DTD_SYSTEM_DEFAULT = "http://www.w3.org/TR/html4/loose.dtd";

    private String publidId = DTD_PUBLIC_DEFAULT;

    /**
     * @param publicId
     *            The string to use for the public id. Defaults to
     *            {@value DTD_PUBLIC_DEFAULT}. Null is accepted.
     */
    public void setPublicId(String publicId) {
        this.publidId = publicId;
    }

    private String getPublicId() {
        return publidId;
    }

    private String systemId = DTD_SYSTEM_DEFAULT;

    /**
     * @param systemId
     *            The string to use for the system id. Defaults to
     *            {@value DTD_SYSTEM_DEFAULT}. Null is accepted.
     */
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    private String getSystemId() {
        return systemId;
    }

    /**
     * source: http://dcb.sun.com/technology/xml/qa_archive/qa061501.html
     */
    public String toString() {

        Document doc = (Document) _rootNode;

        BaseMarkupSerializer serializer = new HTMLSerializer();
        // 01-10-3 serializer= new org.apache.xml.serialize.XHTMLSerializer ();

        java.io.StringWriter writer = new java.io.StringWriter();

        serializer.setOutputCharStream(writer);

        OutputFormat format = new OutputFormat(doc);
        // What encoding? The default is UTF-8.

        /*
         * System.out.println("OutputFormat.getEncoding (): " +
         * format.getEncoding() ); System.out.println("OutputFormat.getIndenting
         * (): " + format.getIndenting() );
         * System.out.println("OutputFormat.getIndent (): " + format.getIndent() );
         * System.out.println("OutputFormat.getLineWidth (): " +
         * format.getLineWidth() );
         * System.out.println("OutputFormat.getPreserveEmptyAttributes (): " +
         * format.getPreserveEmptyAttributes() );
         */

        format.setIndenting(true);
        format.setIndent(1);
        format.setLineWidth(120);

        // http://xerces.apache.org/xerces2-j/javadocs/other/index.html
        // DocumentType dtd = doc.getDoctype();

        // The DTD seemed to be unspecified:
        //
        // System.out.println("DTD PUBLIC: "+ dtd.getPublicId());
        // System.out.println("DTD SYSTEM: "+ dtd.getSystemId());
        // [junit] DTD PUBLIC: null
        // [junit] DTD SYSTEM: null
        //
        // but it was serializing as
        // "-//W3C//DTD HTML 4.01//EN"
        // "http://www.w3.org/TR/html4/strict.dtd"

        // The DTD needs to be removed for #setDoctype to take effect
        // doc.removeChild(dtd);

        // See XLM-241
        format.setDoctype(getPublicId(), getSystemId());

        /*
         * Tried this to stop attributes being transformed from <input is-chkd>
         * to <input is-chkd="is-chkd"> format.setPreserveEmptyAttributes ( true ) ;
         * result: Warning: unknown attribute "is-chkd"
         */
        serializer.setOutputFormat(format);
        try {
            serializer.serialize(doc);
            writer.close();
        } catch (java.io.IOException e) {
            return null;
        }
        // System.out.println("[ParsedHtml]: \n" +
        // writer.toString().substring(0, 200));
        return writer.toString();
    }
}

// Copyright notice from original com.meterware.httpunit.ReceivedPage.java
/*******************************************************************************
 * ID: ReceivedPage.java,v 1.12 2001/02/02 14:34:29 russgold Exp
 * 
 * Copyright (c) 2000, Russell Gold
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 ******************************************************************************/
