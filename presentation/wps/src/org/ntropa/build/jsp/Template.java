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
 * Template.java
 *
 * Created on 19 October 2001, 14:46
 */

package org.ntropa.build.jsp;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.ntropa.utility.FileUtilities;

/**
 * A template should be treated in the same way as a HTML page to allow
 * recursion.
 * 
 * (This has been achieved in MarkedUpHtmlParser)
 * 
 * @author jdb
 * @version $Id: Template.java,v 1.7 2001/11/20 18:12:07 jdb Exp $
 */
public class Template {

    private String _baseName;

    private String _html;

    private File _parentDirectory;

    private final Charset encoding;

    /**
     * 
     * @return The encoding for all files handled by this Template
     */
    private Charset getEncoding() {
        return encoding;
    }

    /*
     * Support for workaround for Jasper bug/non-useful feature in Tomcat 4.0
     * 
     * Bug: If a JSP which is never served out is included with <jsp:include
     * page="x.jsp" flush="true" /> is modified Jasper never recompiles it.
     * 
     * In version 1.7 of this class the default was switched to false as the
     * scheme for including templates changed to embedding the referenced
     * template and (re)reading it every time the consumer page is (re)created.
     */
    private static final SimpleDateFormat _formatter = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");

    private static boolean _tomcat4BugWorkaround = false;

    private static final PatternMatcher _matcher;

    private static Pattern _pattern;

    private static MalformedPatternException _patternException;

    private static final String TEMPLATE_PAIR_PATTERN =
    /*
     * <!-- template="header" -->...<!-- template="/header" -->
     */
    // ORIG: "<!--\\s*template\\s*=\\s*\"([\\w\\-]+)\".*-->" +
    "<!--\\s*template\\s*=\\s*\"([\\w\\-]+)\"\\s*[^>]*-->" +

    /*
     * enclosed content
     */
    "(.*)" +

    /*
     * <!-- template="/footer" -->
     */
    "<!--\\s*template\\s*=\\s*\"/\\1\"\\s*-->";
    /* Static initialiser */
    static {

        _matcher = new Perl5Matcher();

        PatternCompiler compiler = new Perl5Compiler();

        try {
            _pattern = compiler.compile(TEMPLATE_PAIR_PATTERN,
            /*
             * SINGLELINE_MASK. This option allows (.*) instead of (\s|.)*)
             * which is expensive wrt backtracking.
             */
            Perl5Compiler.SINGLELINE_MASK | Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.READ_ONLY_MASK);
        } catch (MalformedPatternException e) {
            /* how can an exception be thrown from a static initialiser? */
            _patternException = e;
        }
    }

    /**
     * Creates new Template
     * 
     * @param baseName
     *            The name of the source file, ie if the source file is
     *            _include/footers.html then the name will be footers.html.
     * 
     * @param html
     *            The file content.
     * 
     * @param parentDirectory
     *            The directory to delete earlier templates from and save new
     *            templates in.
     * @param encoding
     *            The encoding that HTML files will be read and written to/from
     *            disk with.
     */
    public Template(String baseName, String html, File parentDirectory, Charset encoding) throws TemplateException {

        if (baseName == null)
            throw new TemplateException("baseName was null");
        if (baseName.equals(""))
            throw new TemplateException("baseName was empty");

        if (html == null)
            throw new TemplateException("html was null");
        /* empty html is acceptable */

        if (!parentDirectory.isDirectory())
            throw new TemplateException("parentDirectory was not a directory");

        _baseName = baseName;
        _html = html;
        _parentDirectory = parentDirectory;

        this.encoding = encoding;

    }

    /**
     * Creates new Template. The main reason to use this constructor is to be
     * able to call delete () on the object.
     * 
     * @param baseName
     *            The name of the source file, ie if the source file is
     *            _include/footers.html then the name will be footers.html.
     * 
     * @param parentDirectory
     *            The directory to delete earlier templates from and save new
     *            templates in.
     * 
     * TODO: the encoding is not needed when deleting so rework this class to
     * not require it in this constructor and ensure it is non-null when it is
     * needed. Maybe a different class to do the deletion.
     * @param encoding
     *            The encoding that HTML files will be read and written to/from
     *            disk with.
     */
    public Template(String baseName, File parentDirectory, Charset encoding) throws TemplateException {

        if (baseName == null)
            throw new TemplateException("baseName was null");
        if (baseName.equals(""))
            throw new TemplateException("baseName was empty");

        if (!parentDirectory.isDirectory())
            throw new TemplateException("parentDirectory was not a directory");

        _baseName = baseName;
        _parentDirectory = parentDirectory;
        this.encoding = encoding;

    }

    /**
     * Delete any JSPs for this template
     */
    public void delete() throws TemplateException {
        File[] oldJsps = _parentDirectory.listFiles(new PrefixFilter(_baseName));

        if (oldJsps == null)
            throw new TemplateException("The files in the parent directory could not be listed");

        for (int i = 0; i < oldJsps.length; i++)
            if (!oldJsps[i].delete())
                throw new TemplateException("Failed to delete file: " + oldJsps[i]);
    }

    /**
     * Split the template into individual files, one per template. Delete any
     * files from previous builds.
     */
    public void build() throws TemplateException {

        /* this can be true if the wrong constructor was used */
        if (_html == null)
            throw new IllegalStateException();

        if (_patternException != null)
            throw new TemplateException(_patternException.toString());

        delete();

        /* for each template found use the name and content to create a file */
        Map<String, String> templates = getTemplates(_html);

        for (Map.Entry<String, String> template : templates.entrySet()) {

            String name = template.getKey();
            String content = template.getValue();

            File f = getFile(name);
            try {
                FileUtilities.writeString(f, content, getEncoding());
            } catch (IOException e) {
                throw new TemplateException("Error writing file: " + f + "\n" + e);
            }
        }
    }

    private/* static synchronized */Map<String, String> getTemplates(String html) {

        // Map<String, String> templates = templatesCache.get(html);
        // if (templates != null) {
        // return templates;
        // }

        Map<String, String> templates = new HashMap<String, String>();

        PatternMatcherInput input = new PatternMatcherInput(html);

        MatchResult result;
        while (_matcher.contains(input, _pattern)) {
            result = _matcher.getMatch();

            String name = result.group(1).toLowerCase();
            String content = result.group(2);
            templates.put(name, content);
        }

        // templatesCache.put(html, templates);

        return templates;
    }

    // java.util.regex pattern matching was nearly as fast as
    // org.apache.oro.text.regex.* but not fast enough to replace it.
    //
    // private final java.util.regex.Pattern PAT =
    // java.util.regex.Pattern.compile(TEMPLATE_PAIR_PATTERN,
    // java.util.regex.Pattern.CASE_INSENSITIVE|java.util.regex.Pattern.DOTALL);
    //
    // private Map<String, String> getTemplates(String html) {
    //
    // Map<String, String> templates = new HashMap<String, String>();
    //
    // Matcher m = PAT.matcher(html);
    //        
    // while (m.find()) {
    //
    // String name = m.group(1).toLowerCase();
    // String content = m.group(2);
    // templates.put(name, content);
    // }
    //
    // return templates;
    // }

    /*
     * Average time for scan when editing a file that caused 52 modifications in
     * each of 3 sites
     * 
     * Before using a cache: 36 seconds
     * 
     * After using HashMap to cache: 28.5 seconds
     * 
     * A reduction to 79% (21% trimmed off). Your Java Profiler measured the
     * regex code taking 24% so this is consistent.
     * 
     * However when HashMap was changed to WeakHashMap this improvement
     * disappeared.
     * 
     * Since editing a template repeated between two versions is an unlikely
     * use-case caching was dropped.
     */
    // /* A cache will be emptied when memory is low */
    // private static final Map<String, Map<String, String>> templatesCache =
    // new HashMap<String, Map<String, String>>();
    /**
     * Return the <code>File</code> to use for the template.
     * 
     * To workaround a bug/non-useful feature in Jasper (the JSP compiler used
     * on Tomcat 4) we insert a date slug.
     * 
     * Description of Tomcat bug.
     * 
     * These are our file:
     * 
     * /template-user.html /_include/templates.html
     * 
     * When templates.html is modified the derived JSPs are created okay but
     * Jasper never recompiles them.
     * 
     * @param template
     *            The name of the template to generate a file name for.
     */
    protected File getFile(String template) {

        /*
         * structure.html#footer.template
         * structure.html#2001.11.5-17.30.18#footer.template
         */

        String name = _baseName + JSPBuilder.TEMPLATE_SEPARATOR +
        /* Insert date slug */
        (_tomcat4BugWorkaround ? _formatter.format(new Date()) + JSPBuilder.TEMPLATE_SEPARATOR : "") + template
                + JSPBuilder.TEMPLATE_EXTENSION;

        return new File(_parentDirectory, name);
    }

    /**
     * Set the use of the Tomcat bug workaround.
     * 
     * Unsetting this makes it easier to test the class.
     * 
     * @param flag
     *            If true the workaround is used.
     */
    static public void setTomcat4BugWorkaround(boolean flag) {
        _tomcat4BugWorkaround = flag;
    }

    /**
     * Search the given directory for a template file matching the template
     * name. If more than one file matches then the alphanumeric first is
     * returned.
     * 
     * @param searchDirectory
     *            The directory to seach in.
     * @param templateName
     *            The name of the template to find.
     */
    public static String findTemplateJsp(File searchDirectory, String templateName) {
        return findJsp(searchDirectory, templateName);
    }

    public static String findJsp(File searchDirectory, String templateName) {
        String[] files = searchDirectory.list(new SuffixFilter(templateName));
        if ((files == null) || (files.length == 0))
            return null;

        Arrays.sort(files);
        return files[0];
    }

    public static class PrefixFilter implements FilenameFilter {
        protected String _requiredPrefix;

        PrefixFilter(String baseName) {
            _requiredPrefix = baseName + JSPBuilder.TEMPLATE_SEPARATOR;
        }

        public boolean accept(File dir, String name) {
            return name.startsWith(_requiredPrefix);
        }
    }

    public static class SuffixFilter implements FilenameFilter {
        protected String _requiredSuffix;

        SuffixFilter(String suffix) {
            _requiredSuffix = JSPBuilder.TEMPLATE_SEPARATOR + suffix + JSPBuilder.TEMPLATE_EXTENSION;
        }

        public boolean accept(File dir, String name) {
            return name.endsWith(_requiredSuffix);
        }
    }

}
