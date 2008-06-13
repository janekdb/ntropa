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
 * TemplateTest.java
 *
 * Created on 19 October 2001, 15:37
 */

package tests.org.ntropa.build.jsp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.jsp.JSPBuilder;
import org.ntropa.build.jsp.Template;
import org.ntropa.build.jsp.TemplateException;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.NtropaTestWorkDirHelper;


/**
 *
 * @author  jdb
 * @version $Id: TemplateTest.java,v 1.6 2001/11/20 18:12:07 jdb Exp $
 */
public class TemplateTest extends TestCase {
    
    private File m_TopFolder ;
    
    private static final String NO_TEMPLATES =
    "<html>" +
    "<body>" +
    "<h1>No templates on this page</h1>\n\n" +
    "</body></html>" ;
    
    private static final String ONE_TEMPLATE =
    "<html>\n" +
    "<head>\n" +
    "<title>NineMSN templates</title>\n" +
    "</head>\n" +
    "<body>\n" +
    "\n" +
    "<h1>This is the template for the news box</h1>\n" +
    "\n" +
    "<!-- template=\"news-tmpl\" -->\n" +
    "<table border=1>\n" +
    "<tr><tr>Todays Education News</td></tr>\n" +
    "<tr><td valign=\"top\" class=\"news\">\n" +
    "This is some sample news from the world of education.\n" +
    "</td></tr>\n" +
    "</table>\n" +
    "<!-- template=\"/news-tmpl\" -->\n" +
    "\n" +
    "</body>\n" +
    "</html>\n" ;
    
    private static final String TWO_TEMPLATES =
    "<html>\n" +
    "<head>\n" +
    "<title>NineMSN templates</title>\n" +
    "</head>\n" +
    "<body>\n" +
    "\n" +
    "<h1>This is the general header</h1>\n" +
    "\n" +
    "<!-- template=\"header\" -->\n" +
    "<a href=\"main.html\">Main Page</a> | \n" +
    "<a href=\"search.html\">Search Page</a> | \n" +
    "<a href=\"about.html\">About Page</a> \n" +
    "<!-- template=\"/header\" -->\n" +
    "\n" +
    "<h1>This is the general footer</h1>\n" +
    "\n" +
    "<!-- template=\"footer\" -->\n" +
    "Copyright &copy; 2001 Learning Information Systems\n" +
    "<!-- template=\"/footer\" -->\n" +
    "\n" +    "</body>\n" +
    "</html>\n" ;
    
    private static final String MIXEDCASE_TEMPLATE_NAME =
    "<html>\n" +
    "<head>\n" +
    "<title>NineMSN templates</title>\n" +
    "</head>\n" +
    "<body>\n" +
    "\n" +
    "<h1>This is the template for the news box</h1>\n" +
    "\n" +
    "<!-- template=\"CHeckCONvertedTOLOWERcASE\" -->\n" +
    "<table border=1>\n" +
    "<tr><tr>Todays Education News</td></tr>\n" +
    "<tr><td valign=\"top\" class=\"news\">\n" +
    "This is some sample news from the world of education.\n" +
    "</td></tr>\n" +
    "</table>\n" +
    "<!-- template=\"/CHeckCONvertedtolowercASE\" -->\n" +
    "\n" +
    "</body>\n" +
    "</html>\n" ;
    
    private static final String EMBEDDED_COMMENTS =
    "<html>\n" +
    "<head>\n" +
    "<title>NineMSN templates</title>\n" +
    "</head>\n" +
    "<body>\n" +
    "\n" +
    "<h1>This is the template for the news box</h1>\n" +
    "\n" +
    "<!-- template=\"comments\" -->\n" +
    "before the comment" +
    "<!-- my comment -->" +
    "after the comment" +
    "<!-- template=\"/comments\" -->\n" +
    "\n" +
    "</body>\n" +
    "</html>\n" ;
    
    private static final String EMBEDDED_COMMENTS_TEMPLATE =
    "before the comment" +
    "<!-- my comment -->" +
    "after the comment" ;
    
    public TemplateTest( String testName ) {
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
        
        TestSuite suite = new TestSuite( TemplateTest.class );
        return suite;
    }
    
    protected void setUp() throws Exception, IOException {
        
        String TOP_FOLDER = new NtropaTestWorkDirHelper().getWorkDir() + "/tests.org.ntropa.build.jsp.TemplateTest";

        m_TopFolder = new File( TOP_FOLDER ) ;
        if ( ! m_TopFolder.mkdirs() )
            throw new Exception( "Failed to create folder or folder already existed" ) ;
        
        /* Turn off Tomcat workaround to make testing easier */
        Template.setTomcat4BugWorkaround ( false ) ;
    }
    
    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        
        FileUtilities.killDirectory( m_TopFolder ) ;
    }
    
    private static Charset FOO_CHARSET = Charset.forName("UTF-8");

    public void testNoTemplates() throws TemplateException {
        
        Template t = new Template( "shared.html", NO_TEMPLATES, m_TopFolder, FOO_CHARSET ) ;
        t.build() ;
        
        check( new String [] {}, m_TopFolder.list() ) ;
    }
    
    public void testOneTemplate() throws TemplateException {
        
        Template t = new Template( "shared.html", ONE_TEMPLATE, m_TopFolder, FOO_CHARSET ) ;
        t.build() ;
        
        check( new String [] { "shared.html#news-tmpl.template" }, m_TopFolder.list() ) ;
    }
    
    public void testTwoTemplates() throws TemplateException {
        
        Template t = new Template( "shared.html", TWO_TEMPLATES, m_TopFolder, FOO_CHARSET ) ;
        t.build() ;
        
        check( new String [] { "shared.html#header.template", "shared.html#footer.template" }, m_TopFolder.list() ) ;
    }
    
    public void testMixedCase() throws TemplateException {
        
        Template t = new Template( "shared.html", MIXEDCASE_TEMPLATE_NAME, m_TopFolder, FOO_CHARSET ) ;
        t.build() ;
        
        check( new String [] { "shared.html#checkconvertedtolowercase.template" }, m_TopFolder.list() ) ;
    }
    
    public void testEmbeddedComments() throws TemplateException, IOException {
        
        Template t = new Template( "shared.html", EMBEDDED_COMMENTS, m_TopFolder, FOO_CHARSET ) ;
        t.build() ;
        
        check( new String [] { "shared.html#comments.template" }, m_TopFolder.list() ) ;
        
        /* examine the file contents */
        StringBuffer sb = new StringBuffer() ;
        FileUtilities.readFile( new File( m_TopFolder, "shared.html#comments.template" ), sb ) ;
        //System.out.println( sb.toString() );
        String contents = sb.toString() ;
        if ( contents.indexOf( EMBEDDED_COMMENTS_TEMPLATE ) == -1 )
            fail ( "Template was not parsed correctly:\nExpected:\n" + EMBEDDED_COMMENTS_TEMPLATE +
            "\nActual:\n" + contents ) ;
        
    }
    
    public void testCleanup() throws IOException, TemplateException {
        
        for ( int i = 1 ; i < 50 ; i++ ) {
            File f = new File( m_TopFolder, "logos.html" + JSPBuilder.TEMPLATE_SEPARATOR + i ) ;
            FileUtilities.writeString( f, "foobar" ) ;
        }
        Template t = new Template( "logos.html", NO_TEMPLATES, m_TopFolder, FOO_CHARSET ) ;
        t.build() ;
        
        check( new String [] {}, m_TopFolder.list() ) ;
        
        for ( int i = 1 ; i < 50 ; i++ ) {
            File f = new File( m_TopFolder, "logos.html" + JSPBuilder.TEMPLATE_SEPARATOR + i ) ;
            FileUtilities.writeString( f, "foobar" ) ;
        }
        t = new Template( "logos.html", m_TopFolder, FOO_CHARSET ) ;
        t.delete() ;
        
        check( new String [] {}, m_TopFolder.list() ) ;
        
    }
    
    public void testFindJsp() throws IOException {
        
        String templateName = "news" ;
        
        String floc ;
        
        floc = Template.findTemplateJsp( m_TopFolder, templateName ) ;
        if ( floc != null )
            fail( "False positive" ) ;
        
        File f = new File(
        m_TopFolder, "logos.html" + JSPBuilder.TEMPLATE_SEPARATOR + "some-other-template" + JSPBuilder.TEMPLATE_EXTENSION ) ;
        FileUtilities.writeString( f, "foobar" ) ;
        
        floc = Template.findTemplateJsp( m_TopFolder, templateName ) ;
        if ( floc != null )
            fail( "False positive" ) ;
        
        String targetTemplateFileName = "logos.html" + JSPBuilder.TEMPLATE_SEPARATOR + templateName + JSPBuilder.TEMPLATE_EXTENSION ;
        f = new File( m_TopFolder, targetTemplateFileName ) ;
        FileUtilities.writeString( f, "foobar" ) ;
        
        floc = Template.findTemplateJsp( m_TopFolder, templateName ) ;
        assertEquals( "Wrong template found",
        targetTemplateFileName,
        floc
        ) ;
        
        /* this should not be matched */
        f = new File(
        m_TopFolder, "zz-logos.html" + JSPBuilder.TEMPLATE_SEPARATOR + templateName + JSPBuilder.TEMPLATE_EXTENSION ) ;
        FileUtilities.writeString( f, "foobar" ) ;
        
        floc = Template.findTemplateJsp( m_TopFolder, templateName ) ;
        assertEquals( "Wrong template found",
        targetTemplateFileName,
        floc
        ) ;
        
        /* this should be matched */
        targetTemplateFileName = "aa-logos.html" + JSPBuilder.TEMPLATE_SEPARATOR + templateName + JSPBuilder.TEMPLATE_EXTENSION ;
        f = new File( m_TopFolder, targetTemplateFileName ) ;
        FileUtilities.writeString( f, "foobar" ) ;
        
        floc = Template.findTemplateJsp( m_TopFolder, templateName ) ;
        assertEquals( "Wrong template found",
        targetTemplateFileName,
        floc
        ) ;
        
    }
    /* shared methods */
    
    protected void check( String [] expectedNames, String [] actualNames ) {
        
        if ( actualNames == null )
            fail( "File list was null" ) ;
        
        /* order from File.list () is psuedo-random */
        
        Set expected = new TreeSet( Arrays.asList( expectedNames ) ) ;
        Set actual = new TreeSet( Arrays.asList( actualNames ) ) ;
        
        assertEquals( "The list of actual files was different to the expected list", expected, actual ) ;
    }
}
