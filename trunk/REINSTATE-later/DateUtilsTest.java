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
 * DateUtilsTest.java
 *
 * Created on 10 May 2002, 12:55
 */

package tests.com.studylink.utility;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.studylink.utility.DateUtils;

/**
 *
 * @author  jdb
 * @version $Id: DateUtilsTest.java,v 1.2 2004/11/26 12:19:42 jdb Exp $
 */
public class DateUtilsTest extends TestCase {

	/** Creates new DateUtilsTest */
	public DateUtilsTest(String testName) {
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

		TestSuite suite = new TestSuite(DateUtilsTest.class);
		return suite;
	}

	public void testDateSlug() {

		Date ymd = (new GregorianCalendar(2002, Calendar.NOVEMBER, 27)).getTime();
		assertEquals("Date slug was correct", 20021127, DateUtils.getDateSlugAsInt(ymd));

		/* Check month padding */
		ymd = (new GregorianCalendar(2002, Calendar.MAY, 10)).getTime();
		assertEquals("Date slug was correct", 20020510, DateUtils.getDateSlugAsInt(ymd));

		/* Check day padding */
		ymd = (new GregorianCalendar(2002, Calendar.MAY, 1)).getTime();
		assertEquals("Date slug was correct", 20020501, DateUtils.getDateSlugAsInt(ymd));
	}

	public void testISOLikeDateSlugWithTZ() {
		Date d = (new GregorianCalendar(2002, Calendar.NOVEMBER, 27, 14, 5, 30)).getTime();
		String expected = "2002-11-27 14-05-30";
		assertEquals(
			"Date slug was correct",
			expected,
			DateUtils.getISOLikeDateSlugWithTZ(d).substring(0, expected.length()));
	}
}
