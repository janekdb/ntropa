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
 * IntlAuDigesterFactoryTest.java
 *
 * Created on 12 December 2001, 15:55
 */

package tests.org.ntropa.build ;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Entry point for all Collections tests.
 * @author Rodney Waldhoff
 * @version $Id: TestAll.java,v 1.1 2002/11/01 23:54:23 jdb Exp $
 */
public class TestAll extends TestCase {
    public TestAll(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(tests.org.ntropa.build.jsp.DOMEditorTest.suite());
        suite.addTest(tests.org.ntropa.build.html.ServerActiveHtmlTest.suite());
        suite.addTest(tests.org.ntropa.build.html.PlaceholderTest.suite());
        suite.addTest(tests.org.ntropa.build.ContextPathTest.suite());
        suite.addTest(tests.org.ntropa.build.jsp.JspUtilityTest.suite());
        suite.addTest(tests.org.ntropa.build.jsp.JSPBuilderTest.suite());
           suite.addTest(tests.org.ntropa.build.html.ParsedHtmlTest.suite());
           suite.addTest(tests.org.ntropa.build.html.FragmentTest.suite());
           suite.addTest(tests.org.ntropa.build.html.ElementTest.suite());
           suite.addTest(tests.org.ntropa.build.html.MarkedUpHtmlParserTest.suite());
           suite.addTest(tests.org.ntropa.build.jsp.ScriptWriterTest.suite());

           suite.addTest(tests.org.ntropa.build.jsp.StandardApplicationFinderTest.suite());
           suite.addTest(tests.org.ntropa.build.jsp.StandardPresentationFinderTest.suite());

           suite.addTest(tests.org.ntropa.build.channel.FileLocationTest.suite());
           suite.addTest(tests.org.ntropa.build.channel.StandardChannelMonitorTest.suite());
           suite.addTest(tests.org.ntropa.build.mapper.MapperTest.suite());
           suite.addTest(tests.org.ntropa.build.jsp.TemplateTest.suite());
           suite.addTest(tests.org.ntropa.build.html.ElementAttributesTest.suite());
           suite.addTest(tests.org.ntropa.build.html.ServerActiveHtmlAttributesTest.suite());
           suite.addTest(tests.org.ntropa.build.html.MarkUpAttributesTest.suite());
           suite.addTest(tests.org.ntropa.build.html.ElementTest.suite());
           suite.addTest(tests.org.ntropa.build.html.StandardFragmentTokenizerTest.suite());
           suite.addTest(tests.org.ntropa.build.DirectoryMonitorTest.suite());
           suite.addTest(tests.org.ntropa.build.ConstantsTest.suite());
           suite.addTest(tests.org.ntropa.build.DirectoryPairTest.suite());

        return suite;
    }
        
    public static void main(String args[]) {
        String[] testCaseName = { TestAll.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }
}
/*


           <!--
                This is disabled as a simpler method was used for the reading of
                application parameter files. See StandardApplicationFinder
                                This one might not be used any more.
                suite.addTest(tests.org.ntropa.build.html.MarkedUpHtmlTest.suite());

                Temporary halt on these classes:
                suite.addTest(tests.org.ntropa.build.DirectoryPairMapTest.suite());
                suite.addTest(tests.org.ntropa.build.FilePairTest.suite());
                suite.addTest(tests.org.ntropa.build.FileTreeTest.suite());
            -->

 */