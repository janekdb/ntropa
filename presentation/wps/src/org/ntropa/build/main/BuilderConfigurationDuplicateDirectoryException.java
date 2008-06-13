package org.ntropa.build.main;

public class BuilderConfigurationDuplicateDirectoryException extends BuilderConfigurationException {

    BuilderConfigurationDuplicateDirectoryException(String dir) {
        super("Duplicate directory: '" + dir + "'");
    }
}
