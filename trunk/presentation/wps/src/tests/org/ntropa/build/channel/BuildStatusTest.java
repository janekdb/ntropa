/**
 * 
 */
package tests.org.ntropa.build.channel;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.channel.BuildStatus;
import org.ntropa.utility.DateUtils;

/**
 * @author jdb
 * 
 */
public class BuildStatusTest extends TestCase {

    public BuildStatusTest(String testName) {
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

        TestSuite suite = new TestSuite(BuildStatusTest.class);
        return suite;
    }

    private static final String CHANGE = "last-change=";

    private static final String ADDITION = "last-addition=";

    private static final String MODIFICATION = "last-modification=";

    private static final String DELETION = "last-deletion=";

    private static final String DATE_1 = "2001-11-27T13:03:59+00:00";

    private static final String DATE_2 = "2002-11-27T13:03:59+00:00";

    private static final String DATE_3 = "2003-11-27T13:03:59+00:00";

    private static final String DATE_4 = "2004-11-27T13:03:59+00:00";

    private static final String DATE_REMOTE_PAST = "1970-07-23T06:21:00+00:00";

    private static final String DATE_REMOTE_FUTURE = "2039-07-23T06:21:00+00:00";

    public void testSyntacticallyIncorrectInitializationDataRejected() {
        try {
            BuildStatus bs = new BuildStatus("xx-bad-data");
            fail("Bad data was rejected");
        } catch (Exception e) {
            // Expected.
        }

        String input = CHANGE + DATE_4 + "\n";
        input += ADDITION + DATE_2 + "\n";
        input += MODIFICATION + DATE_3 + "\n";

        try {
            String badInput = input;
            badInput += "incorrect-line\n";
            BuildStatus bs = new BuildStatus(badInput);
            fail("Bad line was rejected");
        } catch (Exception e) {
            // Expected.
        }

        try {
            String badInput = input;
            badInput += "incorrect-name=" + DATE_1 + "\n";
            BuildStatus bs = new BuildStatus(badInput);
            fail("Bad property name was rejected");
        } catch (Exception e) {
            // Expected.
        }

        try {
            String badInput = input;
            String MALFORMED_DATE = "2004-11-27T13:03:5+00:00";
            badInput += DELETION + MALFORMED_DATE + "\n";
            BuildStatus bs = new BuildStatus(badInput);
            fail("Bad date was rejected");
        } catch (Exception e) {
            // Expected.
        }

        try {
            String badInput = CHANGE + DATE_2 + "\n";
            badInput += ADDITION + DATE_2 + "\n";
            badInput += MODIFICATION + DATE_3 + "\n";
            badInput += MODIFICATION + DATE_3 + "\n";
            BuildStatus bs = new BuildStatus(badInput);
            fail("Duplicate property was rejected");
        } catch (Exception e) {
            // Expected
        }

        String goodInput = input;
        goodInput += DELETION + DATE_4 + "\n";
        BuildStatus bs = new BuildStatus(goodInput);
        assertEqualsAfterParsingToDates("The external data matched the input data", goodInput, bs.toExternalForm());
    }

    /**
     * Avoid this test failure when executing with TZ = GMT+11
     * 
     * <pre>
     *      The external data matched the input data,
     *      
     *      expected:
     *      last-change=2004-11-27T13:03:59+00:00
     *      last-addition=2002-11-27T13:03:59+00:00
     *      last-modification=2003-11-27T13:03:59+00:00
     *      last-deletion=2004-11-27T13:03:59+00:00
     *      
     *      but was:
     *      last-change=2004-11-28T00:03:59+11:00
     *      last-addition=2002-11-28T00:03:59+11:00
     *      last-modification=2003-11-28T00:03:59+11:00
     *      last-deletion=2004-11-28T00:03:59+11:00
     * 
     * </pre>
     * 
     */
    private void assertEqualsAfterParsingToDates(String assertion, String expected, String actual) {
        String expectedLines[] = expected.split("\n");
        String actualLines[] = actual.split("\n");
        assertEquals(assertion, expectedLines.length, actualLines.length);
        for (int i = 0; i < expectedLines.length; i++) {
            String expectedNameValue[] = expectedLines[i].split("=");
            if (expectedNameValue.length != 2)
                fail("Syntax error: " + expectedLines[i]);

            String actualNameValue[] = actualLines[i].split("=");
            if (actualNameValue.length != 2)
                fail("Syntax error: " + actualLines[i]);

            assertEquals(assertion, expectedNameValue[0], actualNameValue[0]);
            try {
                assertEquals(assertion, DateUtils.fromISO8601Format(expectedNameValue[1]), DateUtils
                        .fromISO8601Format(actualNameValue[1]));
            } catch (ParseException e) {
                fail(e.getMessage());
            }
        }
    }

    public void testInconsistentInitializationDateRejected() {
        /*
         * The last-change date must be >= all other dates and equal to at least
         * one of the other dates.
         */

        try {
            String input = CHANGE + DATE_REMOTE_PAST + "\n";
            input += ADDITION + DATE_2 + "\n";
            input += MODIFICATION + DATE_3 + "\n";
            input += DELETION + DATE_4 + "\n";
            BuildStatus bs = new BuildStatus(input);
            fail("Early last-change was rejected");
        } catch (Exception e) {
            // Expected
        }

        try {
            String input = CHANGE + DATE_REMOTE_FUTURE + "\n";
            input += ADDITION + DATE_2 + "\n";
            input += MODIFICATION + DATE_3 + "\n";
            input += DELETION + DATE_4 + "\n";
            BuildStatus bs = new BuildStatus(input);
            fail("Late last-change was rejected");
        } catch (Exception e) {
            // Expected
        }

        /* A change date equal to any of the others should be accepted. */
        {
            String input = CHANGE + DATE_4 + "\n";
            input += ADDITION + DATE_4 + "\n";
            input += MODIFICATION + DATE_3 + "\n";
            input += DELETION + DATE_2 + "\n";
            BuildStatus bs = new BuildStatus(input);
        }
        {
            String input = CHANGE + DATE_4 + "\n";
            input += ADDITION + DATE_2 + "\n";
            input += MODIFICATION + DATE_4 + "\n";
            input += DELETION + DATE_3 + "\n";
            BuildStatus bs = new BuildStatus(input);
        }
        {
            String input = CHANGE + DATE_4 + "\n";
            input += ADDITION + DATE_2 + "\n";
            input += MODIFICATION + DATE_3 + "\n";
            input += DELETION + DATE_4 + "\n";
            BuildStatus bs = new BuildStatus(input);
        }

    }

    public void testLastChangeAlwaysMatchesMostRecentOther() {
        BuildStatus bs = new BuildStatus(new Date());
        /* Add file. */
        Date additionDate = getYearsFromNow(10);
        bs.setAdditionDate(additionDate);
        assertPropertiesNotEqual(bs, ADDITION, MODIFICATION);
        assertPropertiesEqual(bs, CHANGE, ADDITION);
        /* Modify file. */
        Date modificationDate = getYearsFromNow(20);
        bs.setModificationDate(modificationDate);
        assertPropertiesNotEqual(bs, MODIFICATION, DELETION);
        assertPropertiesEqual(bs, CHANGE, MODIFICATION);
        /* Delete file. */
        Date deletionDate = getYearsFromNow(30);
        bs.setDeletionDate(deletionDate);
        assertPropertiesNotEqual(bs, DELETION, ADDITION);
        assertPropertiesEqual(bs, CHANGE, DELETION);
    }

    private void assertPropertiesEqual(BuildStatus bs, String name1, String name2) {
        Map m = getAsMap(bs);
        assertNotNull(m.get(name1));
        assertNotNull(m.get(name2));
        assertEquals("Properties were equal", m.get(name1), m.get(name2));
    }

    private void assertPropertiesNotEqual(BuildStatus bs, String name1, String name2) {
        Map m = getAsMap(bs);
        assertNotNull(m.get(name1));
        assertNotNull(m.get(name2));
        assertTrue("Properties were not equal", !m.get(name1).equals(m.get(name2)));
    }

    /* keys include trailing = */
    private Map getAsMap(BuildStatus bs) {
        String lines[] = bs.toExternalForm().split("\n");
        Map m = new HashMap();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String parts[] = line.split("=");
            String name = parts[0];
            String value = parts[1];
            m.put(name + "=", value);
        }
        return m;
    }

    private Date getYearsFromNow(int count) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, count);
        return cal.getTime();
    }

}
