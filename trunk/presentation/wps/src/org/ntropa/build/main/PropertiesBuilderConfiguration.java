package org.ntropa.build.main;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PropertiesBuilderConfiguration implements BuilderConfiguration {

    private static final String CONTEXT_PATH_LIST = "context-path-list";

    private static final String LAYOUT_INPUT = "layout.input";

    private static final String LAYOUT_LINK = "layout.link";

    private static final String LAYOUT_OUTPUT = "layout.output";

    private static final String SCHEDULER_PERIOD = "scheduler.period";

    private static final String CONFIGURATION_VERSION = "configuration.version";

    private static final String EXPECTED_NAMES[] = { CONTEXT_PATH_LIST, LAYOUT_INPUT, LAYOUT_LINK, LAYOUT_OUTPUT,
            SCHEDULER_PERIOD, CONFIGURATION_VERSION };

    private static final String VALID_CONFIGURATION_VERSION = "1.0";

    /*
     * The directory monitoring has been witnessed using a large fraction of the
     * CPU on large collections of files. This threshold is enforced.
     */
    private static final int MINIMUM_SCHEDULER_PERIOD = 10;

    private final String DEFAULT_ENCODING = "UTF-8";

    /**
     * 
     * @param props
     *            A non-null <code>Properties</code> object containing these
     *            properties,
     *            <ul>
     *            <li>context-path-list</li>
     *            <li>layout.input</li>
     *            <li>layout.link</li>
     *            <li>layout.output</li>
     *            <li>scheduler.period</li>
     *            <li>configuration.version</li>
     *            </ul>
     *            For each context-path in the comma separated context path list
     *            (context-path-list) an encoding can be set using this syntax
     *            <p>
     *            context-path.&lt;context-path&gt;.encoding = &lt;encoding&gt;
     *            </p>
     *            <p>
     *            For example:
     *            </p>
     *            <ul>
     *            <li>context-path-list = intl-canada,intl-brazil
     *            <li>
     *            <li>context-path.intl-canada.encoding = iso-8859-1
     *            <li>
     *            <li>context-path.intl-brazil.encoding = UTF-8
     *            <li>
     *            <p>
     *            UTF-8 is the default encoding so does not need to be specified
     *            </p>
     */
    public PropertiesBuilderConfiguration(Properties props) {
        if (props == null)
            throw new IllegalArgumentException("props was null");
        for (int i = 0; i < EXPECTED_NAMES.length; i++) {
            String name = EXPECTED_NAMES[i];
            String value = props.getProperty(name);
            if (value == null)
                throw new BuilderConfigurationPropertyException("The expected property '" + name + "' was missing",
                        name);
            if (value.length() == 0)
                throw new BuilderConfigurationPropertyException("The property '" + name + "' was empty", name);
        }
        List tokens = new LinkedList();
        String cplv = props.getProperty(CONTEXT_PATH_LIST);
        String cpCandidates[] = cplv.split(",");
        for (int i = 0; i < cpCandidates.length; i++) {
            String cpCandidate = cpCandidates[i];

            // for (StringTokenizer st = new StringTokenizer(cplv, ",");
            // st.hasMoreTokens();) {
            // String contextPath = st.nextToken();
            if (tokens.contains(cpCandidate))
                throw new BuilderConfigurationContextPathException("There was a duplicate context path '" + cpCandidate
                        + "' was duplicated", cpCandidate);
            if (cpCandidate.length() == 0)
                throw new BuilderConfigurationContextPathException("A context path was empty: '" + cplv + "'", "");
            tokens.add(cpCandidate);
            // }
        }
        Collections.sort(tokens);
        this.contextPaths = (String[]) tokens.toArray(new String[0]);

        String version = props.getProperty(CONFIGURATION_VERSION);
        if (!"1.0".equals(version))
            throw new BuilderConfigurationPropertyException("The configuration version was not '"
                    + VALID_CONFIGURATION_VERSION + "'", CONFIGURATION_VERSION);
        this.configurationVersion = version;

        this.inputDirectory = props.getProperty(LAYOUT_INPUT);
        this.linkDirectory = props.getProperty(LAYOUT_LINK);
        this.outputDirectory = props.getProperty(LAYOUT_OUTPUT);

        String directories[] = { this.inputDirectory, this.linkDirectory, this.outputDirectory };
        Set c = new HashSet();
        for (int i = 0; i < directories.length; i++) {
            if (!c.add(directories[i]))
                throw new BuilderConfigurationDuplicateDirectoryException(directories[i]);
        }

        /* No configured directory should be contained in one of the others. */
        for (int i = 0; i < directories.length; i++) {
            for (int j = 0; j < directories.length; j++) {
                if (i == j)
                    continue;
                File containing = new File(directories[i]);
                File nested = new File(directories[j]);
                File nextParent = nested.getParentFile();
                while (nextParent != null) {
                    if (nextParent.equals(containing))
                        throw new BuilderConfigurationNestedDirectoryException(containing, nested);
                    nextParent = nextParent.getParentFile();
                }
            }
        }
        int candidateSp;
        String sppv = props.getProperty(SCHEDULER_PERIOD);
        try {
            candidateSp = Integer.parseInt(sppv);
        } catch (NumberFormatException e) {
            throw new BuilderConfigurationSchedulerPeriodException("Problem parsing scheduler period '" + sppv + "'",
                    sppv);
        }

        if (candidateSp < MINIMUM_SCHEDULER_PERIOD)
            throw new BuilderConfigurationSchedulerPeriodException(
                    "The scheduler period was less than the allowable minimum: " + candidateSp + " < "
                            + MINIMUM_SCHEDULER_PERIOD, candidateSp);

        this.schedulerPeriod = candidateSp;

        /* encodings */
        this.encodings = new HashMap();
        for (int i = 0; i < contextPaths.length; i++) {
            String contextPath = contextPaths[i];
            String encoding = props.getProperty("context-path." + contextPath + ".encoding", DEFAULT_ENCODING);
            try {
                this.encodings.put(contextPath, Charset.forName(encoding));
            } catch (UnsupportedCharsetException e) {
                throw new BuilderConfigurationContextPathEncodingException("Unsupported Charset for encoding '"
                        + encoding + "' for contextPath '" + contextPath + "'", contextPath, encoding);
            } catch (IllegalCharsetNameException e) {
                throw new BuilderConfigurationContextPathEncodingException("Illegal Charset  for encoding '" + encoding
                        + "' for contextPath '" + contextPath + "'", contextPath, encoding);
            }
        }
    }

    private final String contextPaths[];

    public String[] contextPaths() {
        String result[] = new String[contextPaths.length];
        System.arraycopy(contextPaths, 0, result, 0, contextPaths.length);
        return result;
    }

    private final String inputDirectory;

    public File inputDirectory() {
        return new File(inputDirectory);
    }

    private final String linkDirectory;

    public File linkDirectory() {
        return new File(linkDirectory);
    }

    private final String outputDirectory;

    public File outputDirectory() {
        return new File(outputDirectory);
    }

    private final int schedulerPeriod;

    public int schedulerPeriod() {
        return schedulerPeriod;
    }

    private final String configurationVersion;

    public String configurationVersion() {
        return configurationVersion;
    }

    private final Map encodings;

    public Charset encoding(String contextPath) {
        if (contextPath == null)
            throw new IllegalArgumentException("contextPath was null");

        Charset result = (Charset) encodings.get(contextPath);
        if (result == null)
            throw new IllegalArgumentException("contextPath was unknown: '" + contextPath + "'");
        return result;
    }

}
