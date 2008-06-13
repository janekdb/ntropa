/*
 * Copyright 2001-2006 LEARNING INFORMATION SYSTEMS PTY LIMITED
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ntropa.adapter.jboss;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.util.ServiceMBeanSupport;
import org.ntropa.build.ContextPathException;
import org.ntropa.build.main.Builder;
import org.ntropa.build.main.BuilderConfiguration;
import org.ntropa.build.main.BuilderConfigurationException;
import org.ntropa.build.main.MultiBuilder;
import org.ntropa.build.main.PropertiesBuilderConfiguration;

/**
 * This class is designed to run as a MBean with JBoss.
 * 
 * @author jdb
 * @version $Id: UpdateProcess.java,v 1.13 2002/12/11 14:34:10 jdb Exp $
 */
// TODO: Rename to NtropaBuilder
public class NtropaBuilder extends ServiceMBeanSupport implements NtropaBuilderMBean {

    /**
     * This is the no-arg constructor required for a MBean just like a JavaBean.
     * 
     * It is present for clarity.
     */
    public NtropaBuilder() {
    }

    /**
     * Bean methods
     */

    public int getUpdateCheckSeconds() {
        return builder.getUpdateCheckSeconds();
    }

    public void setUpdateCheckSeconds(int updateCheckSeconds) {
        builder.setUpdateCheckSeconds(updateCheckSeconds);
    }

    public String getName() {
        return "NtropaBuilder(" + builder.getUpdateCheckSeconds() + ")";
    }

    /**
     * ServiceMBeanSupport overrides
     */

    private Builder builder = new MultiBuilder();

    private static final String CONFIGURATION_FILE = "ntropa.properties";

    /**
     * Locate the configuration data and configure from it.
     * 
     * The loading technique was taken from
     * org.jboss.configuration.ConfigurationService.
     * 
     * Extract configuration from property list. Later change this to reading a
     * DOM from an XML file.
     * 
     * @throws BuilderConfigurationException
     *             If there is any problem with the configuration values.
     * @throws ContextPathException
     *             if any context path is syntactically incorrect.
     */
    public void initService() throws Exception {

        // The class loader used to locate the configuration file
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        // Load config from properties file
        InputStream input = loader.getResourceAsStream(CONFIGURATION_FILE);
        if (input == null) {
            throw new IllegalStateException("The InputStream returned from the ClassLoader was null: Check '"
                    + CONFIGURATION_FILE + "' exists and is in the right place.");
        }

        Properties defaults = new Properties();
        defaults.put("scheduler.period", "10");

        Properties p = new Properties(defaults);

        try {
            p.load(input);
        } catch (FileNotFoundException e) {
            throw new Exception("Missing file: " + CONFIGURATION_FILE + "\n" + e);
        } catch (IOException e) {
            throw new Exception("Problem reading file: " + CONFIGURATION_FILE + "\n" + e);
        }

        BuilderConfiguration bc = new PropertiesBuilderConfiguration(p);

        builder.init(bc);

    }

    public void startService() throws Exception {

        builder.start();

    }

    public void stopService() {

        builder.stop();

    }
}
