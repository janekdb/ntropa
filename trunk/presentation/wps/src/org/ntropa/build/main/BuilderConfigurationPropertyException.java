package org.ntropa.build.main;

public class BuilderConfigurationPropertyException extends BuilderConfigurationException {

    private final String propertyName;

    BuilderConfigurationPropertyException(String message, String propertyName) {
        super(message);
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
    
    
}
