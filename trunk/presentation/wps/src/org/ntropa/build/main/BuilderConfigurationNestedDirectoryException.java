package org.ntropa.build.main;

import java.io.File;

public class BuilderConfigurationNestedDirectoryException extends BuilderConfigurationException {

    private final File containingDir;

    private final File nestedDir;

    BuilderConfigurationNestedDirectoryException(File containingDir, File nestedDir) {
        super("'" + nestedDir.getAbsoluteFile() + "' was nested in '" + containingDir.getAbsolutePath() + "'");
        this.containingDir = containingDir;
        this.nestedDir = nestedDir;
    }

    public File getContainingDir() {
        return containingDir;
    }

    public File getNestedDir() {
        return nestedDir;
    }
}
