package tests.org.ntropa.utility;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.utility.DateUtils;

public class DateUtilsTest extends TestCase {

    public DateUtilsTest(String testName) {
        super(testName);
    }

    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests. It
     * runs a collection of test cases.
     */
    public static Test suite() {

        TestSuite suite = new TestSuite(DateUtilsTest.class);
        return suite;
    }

    public void testFormatAsISO8601() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
        cal.set(2006, 11, 9, 22, 6, 52);
        // 2006-12-10T09:06:52+11:00
        assertEqualsIgnoringTZDifferences("2006-12-09T22:06:52+00:00", DateUtils.formatAsISO8601(cal.getTime()));

        TimeZone tz = TimeZone.getTimeZone("GMT+11");
        cal = Calendar.getInstance(tz);
        cal.set(1994, 0, 4, 13, 3, 4);
        assertEqualsIgnoringTZDifferences("1994-01-04T02:03:04+00:00", DateUtils.formatAsISO8601(cal.getTime()));
    }

    /**
     * 2006-12-09T22:06:52+00:00 == 2006-12-10T09:06:52+11:00
     * 
     * @param expected
     * @param actual
     */
    private void assertEqualsIgnoringTZDifferences(String expected, String actual) {

        assertEquals(expected.substring(0, "2006-12-".length()), actual.substring(0, "2006-12-".length()));
        assertEquals(expected.substring("2006-12-09T22:".length() - 1, "2006-12-09T22:06:52".length()), actual
                .substring("2006-12-09T22:".length() - 1, "2006-12-09T22:06:52".length()));
        assertEquals(expected.substring("2006-12-09T22:06:52+00:".length() - 1), actual
                .substring("2006-12-09T22:06:52+00:".length() - 1));

    }

    public void testISO8601Parse() throws ParseException {
        {
            String input = "2006-12-09T22:06:52+00:00";
            Date actual = DateUtils.fromISO8601Format(input);

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
            cal.clear(); // zero milliseconds.
            cal.set(2006, 11, 9, 22, 6, 52);
            assertEquals(cal.getTime().getTime(), actual.getTime());
        }
        {
            String input = "2006-12-09T22:06:52+11:00";
            Date actual = DateUtils.fromISO8601Format(input);

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+11"));
            cal.clear(); // zero milliseconds.
            cal.set(2006, 11, 9, 22, 6, 52);
            assertEquals(cal.getTime().getTime(), actual.getTime());

        }
        {
            String input = "2006-12-09T02:06:52+11:00";
            Date actual = DateUtils.fromISO8601Format(input);

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
            cal.clear(); // zero milliseconds.
            cal.set(2006, 11, 8, 15, 6, 52);
            assertEquals(cal.getTime().getTime(), actual.getTime());

        }
    }

    public void testRoundTrip() throws ParseException {
        {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
            cal.set(2006, 11, 9, 22, 6, 52);
            // date -> string -> date
            Date roundTripped = DateUtils.fromISO8601Format(DateUtils.formatAsISO8601(cal.getTime()));
            assertEquals(cal.getTime().toString(), roundTripped.toString());
        }
        {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
            cal.set(2006, 11, 9, 22, 6, 52);
            // date -> string -> date
            Date roundTripped = DateUtils.fromISO8601Format(DateUtils.formatAsISO8601(cal.getTime()));
            assertEquals(cal.getTime().toString(), roundTripped.toString());
        }

    }

    public void _testReminder() {
        fail("What does #testISO8601ToString return when the time zone is Australia/Perth? Run on NAVDEV.");
    }
}
