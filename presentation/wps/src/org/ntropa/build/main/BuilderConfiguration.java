package org.ntropa.build.main;

import java.io.File;
import java.nio.charset.Charset;

public interface BuilderConfiguration {

    /**
     * An array of directories names to look for in each of the input, link and
     * output directories. For example,
     * <ul>
     * <li>site-a</li>
     * <li>site-b</li>
     * <li>master</li>
     * </ul>
     * 
     * @return An array of directories names to look for in each of the input,
     *         link and output directories.
     */
    String[] contextPaths();

    /**
     * The full path to the directory that contains the directories that are
     * monitored for changes. For example,
     * <p>
     * /var/ntropa/input
     * </p>
     * 
     * @return The full path to the directory that contains the directories that
     *         are monitored for changes.
     */
    File inputDirectory();

    /**
     * The full path to the directory that contains the directories that
     * symbolic link files are created (and deleted) in. For example,
     * <p>
     * /var/ntropa/link
     * </p>
     * 
     * @return The full path to the directory that contains the directories that
     *         symbolic link files are created (and deleted) in.
     */
    File linkDirectory();

    /**
     * The full path to the directory that contains the directories that will
     * have JSPs created (and deleted) in. For example,
     * <p>
     * /var/ntropa/jsp
     * </p>
     * 
     * @return The full path to the directory that contains the directories that
     *         will have JSPs created (and deleted) in.
     */
    File outputDirectory();

    /**
     * The number of seconds to wait between scanning directories that have not
     * changed recently. For example,
     * <p>
     * 60
     * </p>
     * 
     * @return The number of seconds to wait between scanning directories that
     *         have not changed recently.
     */
    int schedulerPeriod();

    /**
     * A string representing the version of the configuration file. For example,
     * <p>
     * 1.0
     * </p>
     * 
     * @return A string representing the version of the configuration file.
     */
    String configurationVersion();

    /**
     * @param contextPath
     *            A non-null context path.
     * @return The encoding to read input files with and to write output files
     *         with.
     * @throws IllegalArgumentException
     *             if <code>contextPath</code> is not from the list returned
     *             by {@link #contextPaths()}
     */
    Charset encoding(String contextPath);
}
