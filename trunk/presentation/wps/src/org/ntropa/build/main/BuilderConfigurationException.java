package org.ntropa.build.main;

public abstract class BuilderConfigurationException extends RuntimeException {

    BuilderConfigurationException(String message) {
        super(message);
    }
}
