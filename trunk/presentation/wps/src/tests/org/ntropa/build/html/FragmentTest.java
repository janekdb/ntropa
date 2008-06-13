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
 * FragmentTest.java
 *
 * Created on 07 June 2002, 13:02
 */

package tests.org.ntropa.build.html;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.html.Fragment;

/**
 * 
 * @author jdb
 * @version $Id: FragmentTest.java,v 1.1 2002/06/07 13:35:26 jdb Exp $
 */
public class FragmentTest extends TestCase {

    /** Creates new FragmentTest */
    public FragmentTest(String testName) {
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

        TestSuite suite = new TestSuite(FragmentTest.class);
        return suite;
    }

    public void testSetUpCode() {

        Fragment f = new Fragment("abc");

        StringBuilder sb = new StringBuilder();

        f.getSetUpCode("NAME", sb, null);

        // TODO: Revert to ntropa class names.
        if (false) {
            String expected = "NAME = new org.ntropa.runtime.sao.StandardFragment () ;\n"
                    + "NAME.setHtml ( \"abc\" ) ;\n";
        }
        String expected = "NAME = new com.studylink.sao.StandardFragment () ;\n" + "NAME.setHtml ( \"abc\" ) ;\n";

        assertEquals("Setup code with simple html was correct", expected, sb.toString());

        f = new Fragment("abc<%123%>");
        sb = new StringBuilder();

        f.getSetUpCode("NAME", sb, null);

        // TODO: Revert to ntropa class names.
        if (false) {
            expected = "NAME = new org.ntropa.runtime.sao.StandardFragment () ;\n"
                    + "NAME.setHtml ( \"abc<\" + \"%123%\" + \">\" ) ;\n";
        }
        expected = "NAME = new com.studylink.sao.StandardFragment () ;\n"
                + "NAME.setHtml ( \"abc<\" + \"%123%\" + \">\" ) ;\n";

        assertEquals("Setup code with complex html was correct", expected, sb.toString());

    }
}
