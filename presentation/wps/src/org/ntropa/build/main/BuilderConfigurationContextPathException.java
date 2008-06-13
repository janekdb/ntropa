package org.ntropa.build.main;

public class BuilderConfigurationContextPathException extends BuilderConfigurationException {

    BuilderConfigurationContextPathException(String message, String contextPath) {
        super(message);
        this.contextPath = contextPath;
    }

    private final String contextPath;

    public String getContextPath() {
        return contextPath;
    }

}
