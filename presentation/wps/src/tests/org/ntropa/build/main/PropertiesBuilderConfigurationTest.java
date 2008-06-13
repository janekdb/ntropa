package tests.org.ntropa.build.main;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.build.main.BuilderConfiguration;
import org.ntropa.build.main.BuilderConfigurationContextPathEncodingException;
import org.ntropa.build.main.BuilderConfigurationContextPathException;
import org.ntropa.build.main.BuilderConfigurationDuplicateDirectoryException;
import org.ntropa.build.main.BuilderConfigurationNestedDirectoryException;
import org.ntropa.build.main.BuilderConfigurationPropertyException;
import org.ntropa.build.main.BuilderConfigurationSchedulerPeriodException;
import org.ntropa.build.main.PropertiesBuilderConfiguration;

public class PropertiesBuilderConfigurationTest extends TestCase {

    public PropertiesBuilderConfigurationTest(String testName) {
        super(testName);
    }

    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests. It
     * runs a collection of test cases.
     */
    public static Test suite() {

        TestSuite suite = new TestSuite(PropertiesBuilderConfigurationTest.class);
        return suite;
    }

    public void testNullPropertiesIsRejected() {
        try {
            BuilderConfiguration bc = new PropertiesBuilderConfiguration(null);
            fail("Null Properties was rejected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void testAnyMissingExpectedPropertyIsRejected() {
        Set names = defaults().keySet();
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            Properties props = defaults();
            props.remove(name);
            try {
                BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
                fail("Properties were rejected when the property '" + name + "' was missing");
            } catch (BuilderConfigurationPropertyException e) {
                // Expected.
                assertEquals(name, e.getPropertyName());
            }
        }
    }

    public void testAnyEmptyExpectedPropertyIsRejected() {
        Set names = defaults().keySet();
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            Properties props = defaults();
            props.setProperty(name, "");
            try {
                BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
                fail("Properties were rejected when the property '" + name + "' was zero length");
            } catch (BuilderConfigurationPropertyException e) {
                // Expected.
                assertEquals(name, e.getPropertyName());
            }
        }

    }

    public void testContextPathsAreSorted() {
        {
            Properties props = new Properties(defaults());
            String name = "context-path-list";
            props.setProperty(name, "abc,def,ghi");
            BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
            assertEquals("The context paths were correctly ordered", Arrays
                    .asList(new String[] { "abc", "def", "ghi" }), Arrays.asList(bc.contextPaths()));
        }
        {
            Properties props = new Properties(defaults());
            String name = "context-path-list";
            props.setProperty(name, "xyz,def,ghi");
            BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
            assertEquals("The context paths were correctly ordered", Arrays
                    .asList(new String[] { "def", "ghi", "xyz" }), Arrays.asList(bc.contextPaths()));
        }
    }

    public void testEmptyContextPathIsRejected() {
        Properties props = new Properties(defaults());
        String name = "context-path-list";
        props.setProperty(name, "abc,,ghi");
        try {
            BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
            fail("Empty item in context path list was rejected");
        } catch (BuilderConfigurationContextPathException e) {
            // Expected
        }
    }

    public void testDuplicateContextPathsRejected() {
        Properties props = new Properties(defaults());
        String name = "context-path-list";
        props.setProperty(name, "site-a,site-b,master,site-a");
        try {
            BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
            fail("Duplicate context paths were rejected");
        } catch (BuilderConfigurationContextPathException e) {
            // Expected.
            assertEquals("site-a", e.getContextPath());
        }
    }

    public void testEmptyContextPathRejected() {
        Properties props = new Properties(defaults());
        String name = "context-path-list";
        props.setProperty(name, "");
        try {
            BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
            fail("Empty context path was rejected");
        } catch (BuilderConfigurationPropertyException e) {
            // Expected.
            assertEquals(name, e.getPropertyName());
        }
    }

    public void testUnparsableSchedulerPeriodRejected() {
        Properties props = new Properties(defaults());
        String name = "scheduler.period";
        props.setProperty(name, "u89d");
        try {
            BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
            fail("Unparsable scheduler period was rejected");
        } catch (BuilderConfigurationSchedulerPeriodException e) {
            // Expected.
            assertEquals("u89d", e.getPropertyValue());
        }
    }

    public void testTooLowSchedulerPeriodRejected() {
        Properties props = new Properties(defaults());
        String name = "scheduler.period";
        props.setProperty(name, "9");
        try {
            BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
            fail("Too low scheduler period was rejected");
        } catch (BuilderConfigurationSchedulerPeriodException e) {
            // Expected.
            /*
             * String comparison to avoid a message about assertEquals(int,int)
             * being ambiguous in Eclipse 3.2 with junit 3.8.x.
             */
            assertEquals("9", String.valueOf(e.getSchedulerPeriod()));
        }
    }

    public void testAnyConfigurationVersionExceptOneOhIsRejected() {
        Properties props = new Properties(defaults());
        String name = "configuration.version";
        props.setProperty(name, "2.0");
        try {
            BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
            fail("Non 1.0 configuration version was rejected");
        } catch (BuilderConfigurationPropertyException e) {
            // Expected.
            assertEquals(name, e.getPropertyName());
        }
    }

    public void testEncodingPropertiesAreParsedCorrectly() {
        Properties props = new Properties(defaults());
        props.setProperty("context-path.site-a.encoding", "UTF-8");
        props.setProperty("context-path.site-b.encoding", "ISO-8859-1");
        /* master not set to test default. */
        BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);

        assertEquals("The encoding for site-a was correctly found", Charset.forName("UTF-8"), bc.encoding("site-a"));
        assertEquals("The encoding for site-b was correctly found", Charset.forName("ISO-8859-1"), bc
                .encoding("site-b"));
        assertEquals("The encoding was defaulted correctly", Charset.forName("UTF-8"), bc.encoding("master"));

        try {
            Charset foo = bc.encoding(null);
            fail("Null context path was rejected");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            Charset foo = bc.encoding("no-such-context");
            fail("Unknown context path was rejected");
        } catch (IllegalArgumentException e) {
            // Expected
        }

    }

    public void testBadEncodingCauseConstuctorToFail() {
        {
            Properties props = new Properties(defaults());
            props.setProperty("context-path.site-a.encoding", "UNSUPPORTED-ENCODING-NAME");
            try {
                BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
                fail("Unsupported encoding was rejected");
            } catch (BuilderConfigurationContextPathEncodingException e) {
                assertEquals("site-a", e.getContextPath());
                assertEquals("UNSUPPORTED-ENCODING-NAME", e.getEncoding());
            }
        }
        {
            Properties props = new Properties(defaults());
            props.setProperty("context-path.site-a.encoding", "&ILLEGAL-ENCODING-NAME");
            try {
                BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
                fail("Illegal encoding was rejected");
            } catch (BuilderConfigurationContextPathEncodingException e) {
                assertEquals("site-a", e.getContextPath());
                assertEquals("&ILLEGAL-ENCODING-NAME", e.getEncoding());
            }
        }
    }

    public void testDuplicateTopDirectoriesRejected() {
        Properties props = new Properties(defaults());
        String dirs[] = { props.getProperty("layout.input"), props.getProperty("layout.link"),
                props.getProperty("layout.output") };
        for (int i = 0; i < dirs.length; i++) {
            String input = dirs[i];
            for (int j = 0; j < dirs.length; j++) {
                String link = dirs[j];
                for (int k = 0; k < dirs.length; k++) {
                    String output = dirs[k];
                    Set c = new HashSet();
                    c.add(input);
                    c.add(link);
                    c.add(output);

                    boolean atLeastOneDuplicate = c.size() < 3;

                    props.setProperty("layout.input", input);
                    props.setProperty("layout.link", link);
                    props.setProperty("layout.output", output);
                    try {
                        BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
                        if (atLeastOneDuplicate)
                            fail("Duplicate directory was rejected");
                    } catch (BuilderConfigurationDuplicateDirectoryException e) {
                        if (!atLeastOneDuplicate)
                            fail("Unique directories were not rejected");
                    }
                }
            }
        }

    }

    public void testOverlappingPathsAreRejected() {
        Properties props = new Properties(defaults());
        String dirs[] = { props.getProperty("layout.input"), props.getProperty("layout.link"),
                props.getProperty("layout.output") };

        /* Non-overlapping paths should not be rejected. */
        for (int i = 0; i < dirs.length; i++) {
            String non[] = (String[]) dirs.clone();
            /* Make a path that has the preceeding path as a prefix. */
            non[(i + 1) % non.length] = non[i] + "-2";

            props.setProperty("layout.input", non[0]);
            props.setProperty("layout.link", non[1]);
            props.setProperty("layout.output", non[2]);
            BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
        }

        /* Overlapping paths should be rejected. */
        for (int i = 0; i < dirs.length; i++) {
            String overlapping[] = (String[]) dirs.clone();
            /* Make a path a sub-path of the preceeding path. */
            String containingDir = overlapping[i];
            String nestedDir = containingDir + File.separator + "dir";
            overlapping[(i + 1) % overlapping.length] = nestedDir;
            System.out.println(Arrays.asList(overlapping));
            props.setProperty("layout.input", overlapping[0]);
            props.setProperty("layout.link", overlapping[1]);
            props.setProperty("layout.output", overlapping[2]);
            try {
                BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
                fail("A sub-path was rejected: " + Arrays.asList(overlapping));
            } catch (BuilderConfigurationNestedDirectoryException e) {
                // Expected.
                assertEquals(new File(containingDir), e.getContainingDir());
                assertEquals(new File(nestedDir), e.getNestedDir());
            }
        }
    }

    public void testInitializationFromValidProperties() {
        Properties props = new Properties();
        props.setProperty("context-path-list", "master,site-a,site-b");
        props.setProperty("layout.input", "/opt/ntropa/input");
        props.setProperty("layout.link", "/opt/ntropa/link");
        props.setProperty("layout.output", "/opt/ntropa/output");
        props.setProperty("scheduler.period", "17");
        props.setProperty("configuration.version", "1.0");

        BuilderConfiguration bc = new PropertiesBuilderConfiguration(props);
        String[] actualContextPaths = bc.contextPaths();
        assertEquals("The context paths were correctly parsed", Arrays.asList(new String[] { "master", "site-a",
                "site-b" }), Arrays.asList(actualContextPaths));
        assertEquals(new File("/opt/ntropa/input"), bc.inputDirectory());
        assertEquals(new File("/opt/ntropa/link"), bc.linkDirectory());
        assertEquals(new File("/opt/ntropa/output"), bc.outputDirectory());
        assertEquals(17, bc.schedulerPeriod());
        assertEquals("1.0", bc.configurationVersion());

    }

    private Properties defaults() {
        Properties result = new Properties();
        result.setProperty("context-path-list", "site-a,site-b,master");
        result.setProperty("layout.input", "/opt/ntropa/input");
        result.setProperty("layout.link", "/opt/ntropa/link");
        result.setProperty("layout.output", "/opt/ntropa/output");
        result.setProperty("scheduler.period", "17");
        result.setProperty("configuration.version", "1.0");

        return result;
    }
}
