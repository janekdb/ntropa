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
 * LinkFileTest.java
 *
 * Created on 03 December 2002, 13:44
 */

package tests.org.ntropa.build.mapper;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.mapper.LinkFile;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.NtropaTestWorkDirHelper;

/**
 * 
 * @author Janek Bogucki
 * @version $Id: LinkFileTest.java,v 1.1 2002/12/03 14:42:09 jdb Exp $
 */
public class LinkFileTest extends TestCase {

    public LinkFileTest(String testName) {
        super(testName);
    }

    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests. It
     * runs a collection of test cases.
     */
    public static Test suite() {

        TestSuite suite = new TestSuite(LinkFileTest.class);
        return suite;
    }

    private File m_TestRoot;

    protected void setUp() throws IOException {

        m_TestRoot = new File(new NtropaTestWorkDirHelper().getWorkDir()
                + "/tests.org.ntropa.build.mapper.LinkFileTest");

        if (!m_TestRoot.mkdir())
            fail("Failed to created temporary directory for test.");
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        FileUtilities.killDirectory(m_TestRoot);
    }

    /**
     * Makes two objects that should succeed. Makes an object that should fail.
     */
    public void testConstructor() throws Exception {

        /* Write the fixture */

        File sourceFile = new File(m_TestRoot, "advice.html.link");
        FileUtilities.writeString(sourceFile, "href = wps://mba/advice/financial-advice.html");

        LinkFile lf = new LinkFile(sourceFile);

        assertEquals("Protocol was correct", "wps", lf.getProtocol());
        assertEquals("Host was correct", "mba", lf.getHost());
        assertEquals("Path was correct", "/advice/financial-advice.html", lf.getPath());

        /* Omitted host */
        FileUtilities.writeString(sourceFile, "href = wps:///advice/financial-advice.html");

        lf = new LinkFile(sourceFile);

        assertEquals("Protocol was correct", "wps", lf.getProtocol());
        assertEquals("Host was correct", null, lf.getHost());
        assertEquals("Path was correct", "/advice/financial-advice.html", lf.getPath());

        /* Omitted scheme. */
        FileUtilities.writeString(sourceFile, "href = mba/advice/financial-advice.html");

        try {
            lf = new LinkFile(sourceFile);
            fail("href with missing scheme was rejected");
        } catch (IllegalArgumentException e) {
            /* Expected */
        }
    }
}
