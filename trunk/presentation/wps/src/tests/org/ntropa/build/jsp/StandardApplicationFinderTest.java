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
 * StandardApplicationFinderTest.java
 *
 * Created on 22 November 2001, 14:22
 */

package tests.org.ntropa.build.jsp;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.channel.FileLocation;
import org.ntropa.build.channel.FileLocationException;
import org.ntropa.build.jsp.StandardApplicationFinder;
import org.ntropa.utility.FileUtilities;
import org.ntropa.utility.NtropaTestWorkDirHelper;


/**
 * 
 * @author jdb
 * @version $Id: StandardApplicationFinderTest.java,v 1.3 2005/02/23 15:40:37
 *          jdb Exp $
 */
public class StandardApplicationFinderTest extends TestCase {

	private File _topFolder;

	private File _fixtureRoot;

	/** Creates new MarkedUpHtmlParserTest */
	public StandardApplicationFinderTest(String testName) {
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

		TestSuite suite = new TestSuite(StandardApplicationFinderTest.class);
		return suite;
	}

	protected void setUp() throws Exception, IOException {

        final String TOP_FOLDER = new NtropaTestWorkDirHelper().getWorkDir() + "/tests.org.ntropa.build.jsp.StandardApplicationFinderTest";
        
		_topFolder = new File(TOP_FOLDER);
		if (!_topFolder.mkdirs())
			throw new Exception("Failed to create folder or folder already existed: " + TOP_FOLDER);

		String zipPath = System.getProperty("standardapplicationfinder.zippath");

		if (zipPath == null)
			fail("Failed to get path to zip file.");

		File zipFile = new File(zipPath);

		if (!zipFile.exists())
			fail("Zip file does not exist: " + zipPath);

		// Extract the test directory into the test root.
		if (!FileUtilities.extractZip(zipFile, _topFolder))
			fail("Failed to extract zip file.");

		_fixtureRoot = new File(_topFolder, "standardapplicationfinderfs");

		if (!_fixtureRoot.isDirectory())
			fail("Problem with fixture: standardapplicationfinderfs was not a directory: " + _fixtureRoot);

		int numTests = 1;
		for (int i = 1; i <= numTests; i++) {
			File f = new File(_fixtureRoot, "test-" + i);
			if (!f.isDirectory())
				fail("Problem with fixture: missing test directory: 'test-" + i + "'");
		}
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() throws Exception {

		FileUtilities.killDirectory(_topFolder);
	}

	public void testGetSaoData() throws FileLocationException {

		/* Test lookup with an empty directory */
		StandardApplicationFinder saf = new StandardApplicationFinder(new File(_fixtureRoot, "test-1"),
				new FileLocation("anywhere.html"));
		assertEquals("Fault with test-1", null, saf.getSaoData("anything"));

		/* Test lookup with an empty directory */
		saf = new StandardApplicationFinder(new File(_fixtureRoot, "data-1"), new FileLocation("anywhere.html"));
		Properties expected = new Properties();
		expected.setProperty("class-name", "my-class-name");
		expected.setProperty("prop.email", "yan@studylink.com");
		expected.setProperty("prop.newsFeedId", "hurst@welles.studylink.com");

		assertEquals("Fault with data-1", expected, saf.getSaoData("real-name"));

	}

	public void testLookup() throws FileLocationException {

		/* lookup of known name */
		StandardApplicationFinder saf = new StandardApplicationFinder(new File(_fixtureRoot, "test-2"),
				new FileLocation("anywhere.html"));
		assertEquals("Fault with test-2", "org.ntropa.runtime.sao.ResultList", saf.getClazzName(saf
				.getSaoData("result-list")));

		/* lookup of known name with different values in different directories */
		saf = new StandardApplicationFinder(new File(_fixtureRoot, "test-3"), new FileLocation("anywhere.html"));
		assertEquals("Fault with test-3", "level-0", saf.getClazzName(saf.getSaoData("the-param")));

		saf = new StandardApplicationFinder(new File(_fixtureRoot, "test-3"), new FileLocation("about/anywhere.html"));
		assertEquals("Fault with test-3", "level-1", saf.getClazzName(saf.getSaoData("the-param")));

		saf = new StandardApplicationFinder(new File(_fixtureRoot, "test-3"), new FileLocation(
				"a/b/c/d/e/f/g/anywhere.html"));
		assertEquals("Fault with test-3", "level-0", saf.getClazzName(saf.getSaoData("the-param")));

		saf = new StandardApplicationFinder(new File(_fixtureRoot, "test-3"), new FileLocation(
				"about/a/b/c/d/e/f/g/anywhere.html"));
		assertEquals("Fault with test-3", "level-1", saf.getClazzName(saf.getSaoData("the-param")));

		/* lookup not expected to succeed */
		saf = new StandardApplicationFinder(new File(_fixtureRoot, "test-3"), new FileLocation(
				"about/a/b/c/d/e/f/g/anywhere.html"));
		assertEquals("Fault with test-3", null, saf.getSaoData("some-bogus-value"));

		/* lookup expected to succeed */
		saf = new StandardApplicationFinder(new File(_fixtureRoot, "test-3"), new FileLocation(
				"about/a/b/c/d/e/f/g/anywhere.html"));
		assertEquals("Fault with test-3", "it's monkey boy!", saf.getClazzName(saf.getSaoData("not-the-param")));

	}

	/*
	 * Check a match in a file other than 'application.properties' takes
	 * precedence
	 */
	public void testOverrideOfApplicationProperties() throws FileLocationException {

		/* lookup of known name */
		StandardApplicationFinder saf = new StandardApplicationFinder(new File(_fixtureRoot, "test-4"),
				new FileLocation("anywhere.html"));
		assertEquals("Fixture for test-4 was okay", "name-1-from-application-properties", saf
				.getClazzName(saf.getSaoData("name-1")));

		/* name-common is defined in options.properties */
		assertEquals("name-common data was taken from options.properties",
				"common-from-options-properties", saf.getClazzName(saf.getSaoData("common")));

	}

	public void testOptionalParamFileWithDigitsIsUsed() throws FileLocationException{
		/* lookup of known name in with-123-digits-7.properties*/
		StandardApplicationFinder saf = new StandardApplicationFinder(new File(_fixtureRoot, "test-4"),
				new FileLocation("anywhere.html"));
		assertEquals("digits were allowed in optional file names", "digits-from-with-123-digits-7-properties", saf
				.getClazzName(saf.getSaoData("digits")));
	}
	
	public void testOptionalParamFileCheckedInAlphanumericOrder() throws FileLocationException{
		StandardApplicationFinder saf = new StandardApplicationFinder(new File(_fixtureRoot, "test-5"),
				new FileLocation("anywhere.html"));
		assertEquals("name-1 was okay", "name-1-from-abc", saf
				.getClazzName(saf.getSaoData("name-1")));
		assertEquals("name-2 was okay", "name-2-from-abc", saf
				.getClazzName(saf.getSaoData("name-2")));
		assertEquals("name-3 was okay", "name-3-from-def", saf
				.getClazzName(saf.getSaoData("name-3")));
		assertEquals("name-4 was okay", "name-4-from-xyz", saf
				.getClazzName(saf.getSaoData("name-4")));


	}
	
}
