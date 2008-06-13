package org.ntropa.utility;

import java.io.File;

/**
 * This class is a helper class for running the Ntropa unit tests. It is not a
 * general use. The only reason it is in this package and not a test is so test
 * in other layers can use it with adding the test classes to the path.
 * 
 * TODO: Move this into a test support package.
 * 
 * @author jdb
 * 
 */
public class NtropaTestWorkDirHelper {

    private final String WORK_DIR_PROP_NAME = "work.dir";

    /**
     * 
     * @return The non-null, non-empty value of "work.dir"
     */
    public String getWorkDir() {
        String result = System.getProperty(WORK_DIR_PROP_NAME);
        if (result == null)
            throw new IllegalStateException("The system property '" + WORK_DIR_PROP_NAME + "' was null");
        if ("".equals(result))
            throw new IllegalStateException("The system property '" + WORK_DIR_PROP_NAME + "' was empty");

        if (new File(result).isDirectory() == false)
            throw new IllegalStateException("The directory did not exist: '" + result + "'");
        return result;
    }

}
