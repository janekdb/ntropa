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
 * StandardPresentationFinderTest.java
 *
 * Created on 28 November 2001, 15:52
 */

package tests.org.ntropa.build.jsp;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.channel.FileLocation;
import org.ntropa.build.channel.FileLocationException;
import org.ntropa.build.jsp.StandardPresentationFinder;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.NtropaTestWorkDirHelper;


/**
 *
 * @author  rj
 * @version $Id: StandardPresentationFinderTest.java,v 1.4 2005/02/23 15:40:37 jdb Exp $
 */
public class StandardPresentationFinderTest extends TestCase {

	
	private File _topFolder;
	private File _fixtureRoot;

	/** Creates new MarkedUpHtmlParserTest */
	public StandardPresentationFinderTest(String testName) {
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

		TestSuite suite = new TestSuite(StandardPresentationFinderTest.class);
		return suite;
	}

	protected void setUp() throws Exception, IOException {

        String TOP_FOLDER = new NtropaTestWorkDirHelper().getWorkDir() + "/tests.org.ntropa.build.jsp.StandardPresentationFinderTest";

        _topFolder = new File(TOP_FOLDER);
		if (!_topFolder.mkdirs())
			throw new Exception("Failed to create folder or folder already existed: " + TOP_FOLDER);

		String zipPath = System.getProperty("standardpresentationfinder.zippath");

		if (zipPath == null)
			fail("Failed to get path to zip file.");

		File zipFile = new File(zipPath);

		if (!zipFile.exists())
			fail("Zip file does not exist: " + zipPath);

		// Extract the test directory into the test root.
		if (!FileUtilities.extractZip(zipFile, _topFolder))
			fail("Failed to extract zip file.");

		_fixtureRoot = new File(_topFolder, "standardpresentationfindertestfs");

		if (!_fixtureRoot.isDirectory())
			fail("Problem with fixture: standardpresentationfindertestfs was not a directory: " + _fixtureRoot);

		int numTests = 1;
		for (int i = 1; i <= numTests; i++) {
			File f = new File(_fixtureRoot, "test-" + i);
			if (!f.isDirectory())
				fail("Problem with fixture: missing test directory: 'test-" + i + "'");
		}
	}

	/**
	 * Tears down the fixture, for example, close a network connection.
	 * This method is called after a test is executed.
	 */
	protected void tearDown() throws Exception {

		FileUtilities.killDirectory(_topFolder);
	}

	public void testLookup() throws FileLocationException {

		/* Test lookup with in empty directory */
		StandardPresentationFinder spf =
			new StandardPresentationFinder(new File(_fixtureRoot, "test-1"), new FileLocation("anywhere.html"));
		assertEquals("Fault with test-1-Keywords", null, spf.getKeywords());
		assertEquals("Fault with test-1-Description", null, spf.getDescription());

		/* lookup not expected to succeed */
		spf =
			new StandardPresentationFinder(
				new File(_fixtureRoot, "test-1"),
				new FileLocation("deep/a/b/c/d/e/f/g/anywhere.html"));
		assertEquals("Fault with test-1-Keywords", null, spf.getKeywords());
		assertEquals("Fault with test-1-Description", null, spf.getDescription());

		/* lookup of known name */
		spf = new StandardPresentationFinder(new File(_fixtureRoot, "test-2"), new FileLocation("anywhere.html"));
		assertEquals("Fault with test-2-Keywords", "postgraduate, education, learning", spf.getKeywords());
		assertEquals("Fault with test-2-Description", "Postgraduate learning channel", spf.getDescription());

		/* lookup of known name with different values in different directories */
		spf = new StandardPresentationFinder(new File(_fixtureRoot, "test-3"), new FileLocation("anywhere.html"));
		assertEquals("Fault with test-3-Keywords", "keywords level-0", spf.getKeywords());
		assertEquals("a Fault with test-3-Description", "desc level-0", spf.getDescription());

		spf = new StandardPresentationFinder(new File(_fixtureRoot, "test-3"), new FileLocation("about/anywhere.html"));
		assertEquals("Fault with test-3-Keywords", "keywords level-1", spf.getKeywords());
		assertEquals("Fault with test-3-Description", "desc level-1", spf.getDescription());

		spf =
			new StandardPresentationFinder(
				new File(_fixtureRoot, "test-3"),
				new FileLocation("a/b/c/d/e/f/g/anywhere.html"));
		assertEquals("Fault with test-3-Keywords", "keywords level-0", spf.getKeywords());
		assertEquals("b Fault with test-3-Description", "desc level-0", spf.getDescription());

		spf =
			new StandardPresentationFinder(
				new File(_fixtureRoot, "test-3"),
				new FileLocation("about/a/b/c/d/e/f/g/anywhere.html"));
		assertEquals("Fault with test-3-Keywords", "keywords level-1", spf.getKeywords());
		assertEquals("Fault with test-3-Description", "desc level-1", spf.getDescription());

		spf = new StandardPresentationFinder(new File(_fixtureRoot, "test-4"), new FileLocation("anywhere.html"));
		assertEquals("Fault with test-4-Keywords", "Undergrad, Learning, school", spf.getKeywords());
		assertEquals("Fault with test-4-Description", "Long description for page", spf.getDescription());
	}
}
