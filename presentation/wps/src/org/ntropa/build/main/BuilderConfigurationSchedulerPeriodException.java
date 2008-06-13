package org.ntropa.build.main;

public class BuilderConfigurationSchedulerPeriodException extends BuilderConfigurationException {

    BuilderConfigurationSchedulerPeriodException(String message, String propertyValue) {
        super(message);
        this.propertyValue = propertyValue;
        this.schedulerPeriod = null;
    }

    BuilderConfigurationSchedulerPeriodException(String message, int schedulerPeriod) {
        super(message);
        this.propertyValue = null;
        this.schedulerPeriod = new Integer(schedulerPeriod);
    }

    private final String propertyValue;

    public String getPropertyValue() {
        return propertyValue;
    }

    private final Integer schedulerPeriod;

    public Integer getSchedulerPeriod() {
        return schedulerPeriod;
    }

}
