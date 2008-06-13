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
 * HtmlUtilsTest.java
 *
 * Created on 6 February 2002, 12:00
 */

package tests.org.ntropa.utility;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.collections.SequencedHashMap;

import org.ntropa.utility.HtmlUtils;

/**
 * Test suite for HtmlUtils.
 *
 * @author  Abhishek Verma
 * @author  jdb
 * @version $Id: HtmlUtilsTest.java,v 1.6 2004/11/15 16:15:37 jdb Exp $
 */
public class HtmlUtilsTest extends TestCase {

	public HtmlUtilsTest(String testName) {
		super(testName);
	}

	/* Comments copied from junit.framework.TestSuite. */

	/**
	 * A <code>TestSuite</code> is a <code>Composite</code> of Tests.
	 * It runs a collection of test cases. Here is an example using
	 * the dynamic test definition.
	 * <pre>
	 * TestSuite suite= new TestSuite();
	 * suite.addTest(new MathTest("testAdd"));
	 * suite.addTest(new MathTest("testDivideByZero"));
	 * </pre>
	 * Alternatively, a TestSuite can extract the tests to be run automatically.
	 * To do so you pass the class of your TestCase class to the
	 * TestSuite constructor.
	 * <pre>
	 * TestSuite suite= new TestSuite(MathTest.class);
	 * </pre>
	 * This constructor creates a suite with all the methods
	 * starting with "test" that take no arguments.
	 *
	 * @see Test
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(HtmlUtilsTest.class);

		return suite;
	}

	public void testBasicConversions() {

		assertEquals("Returned string was not zero-length", "", HtmlUtils.convertToHtml(""));

		assertEquals("Returned string was not correct", "&amp;", HtmlUtils.convertToHtml("&"));

		assertEquals("Returned string was not correct", "&lt;", HtmlUtils.convertToHtml("<"));

		assertEquals("Returned string was not correct", "&ecirc;", HtmlUtils.convertToHtml("ê"));

		/* SPACE becomes SPACE not non breaking space! */
		assertEquals("Returned string was not correct", "&amp; &gt;", HtmlUtils.convertToHtml("& >"));

		/* SPACE SPACE becomes SPACE SPACE not anything else including SPACE */
		assertEquals("Returned string was not correct", "  ", HtmlUtils.convertToHtml("  "));

		assertEquals(
			"Returned string was not correct",
			"This is a test &amp; also nothing",
			HtmlUtils.convertToHtml("This is a test & also nothing"));

		assertEquals("Returned string was not correct", "&amp;copy;", HtmlUtils.convertToHtml("&copy;"));

	}

	public void testHeadReplacement() {

		String oldHtmlDoc = "<html><head><title>Old Html Header</title></head><body>Old Body Section</body></html>";
		String newHtmlDoc = "<html><head><title>New Html Header</title></head><body>New Body Section</body></html>";

		assertEquals(
			"New head section replaced OK.",
			"<html><head><title>New Html Header</title></head><body>Old Body Section</body></html>",
			HtmlUtils.replaceHead(newHtmlDoc, oldHtmlDoc));

		oldHtmlDoc = "<html><head><title>Old Html Header</title></HEAD><body>Old Body Section</body></html>";
		newHtmlDoc = "<html><head><title>New Html Header</title></HeAd><body>New Body Section</body></html>";

		assertEquals(
			"New head section replaced OK.",
			"<html><head><title>New Html Header</title></HeAd><body>Old Body Section</body></html>",
			HtmlUtils.replaceHead(newHtmlDoc, oldHtmlDoc));

	}

	public void testRenderSelectList() {

		SequencedHashMap entries = new SequencedHashMap();
		entries.put("AFG", "AFGHANISTAN");
		entries.put("ALB", "ALBANIA");

		String selectList = HtmlUtils.renderSelectList(entries, "AFG", "country-of-origin");
		assertEquals(
			"<select name=\"country-of-origin\">\n<option value=\"AFG\" selected>AFGHANISTAN</option>\n<option value=\"ALB\">ALBANIA</option>\n</select>",
			selectList);

	}

	public void testOptionList() {

		SequencedHashMap entries = new SequencedHashMap();
		entries.put("AFG", "AFGHANISTAN");
		entries.put("ALB", "ALBANIA");

		String optionList = HtmlUtils.makeOptionList(entries, "AFG");
		assertEquals(
			"<option value=\"AFG\" selected>AFGHANISTAN</option>\n<option value=\"ALB\">ALBANIA</option>\n",
			optionList);

	}

	public void testOptionListWithSetOfCurrentOptions() {

		SequencedHashMap entries = new SequencedHashMap();
		entries.put("AFG", "AFGHANISTAN");
		entries.put("ALB", "ALBANIA");
		entries.put("AUS", "AUSTRALIA");

		String current[] = new String[] { "AFG", "AUS" };

		String optionList = HtmlUtils.makeOptionList(entries, current);
		assertEquals(
			"<option value=\"AFG\" selected>AFGHANISTAN</option>\n<option value=\"ALB\">ALBANIA</option>\n<option value=\"AUS\" selected>AUSTRALIA</option>\n",
			optionList);

	}
}
