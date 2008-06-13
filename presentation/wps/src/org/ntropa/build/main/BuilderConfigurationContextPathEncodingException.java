package org.ntropa.build.main;

public class BuilderConfigurationContextPathEncodingException extends BuilderConfigurationContextPathException {
    
    BuilderConfigurationContextPathEncodingException(String message, String contextPath, String encoding){
        super(message, contextPath);
        this.encoding = encoding;
    }

    private final String encoding;

    public String getEncoding() {
        return encoding;
    }
    
}
