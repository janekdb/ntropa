package org.ntropa.adapter.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.ntropa.build.main.Builder;
import org.ntropa.build.main.BuilderConfiguration;
import org.ntropa.build.main.MultiBuilder;
import org.ntropa.build.main.PropertiesBuilderConfiguration;

public class NtropaBuilder implements ServletContextListener {

    private static final String DEFAULT_CONF_FILE_PROPERTY_NAME = "ntropa.configuration-file";

    private static final String ALTERNATIVE_CONF_FILE_PROPERTY_NAME = "ntropa.configuration-file-alternative";

    private final Builder builder = new MultiBuilder();

    private boolean builderStarted = false;

    public void contextInitialized(ServletContextEvent sce) {
        final ServletContext context = sce.getServletContext();

        final String defaultConfFile = System.getProperty(DEFAULT_CONF_FILE_PROPERTY_NAME);
        if (defaultConfFile == null)
            throw new IllegalStateException("The property defining the default configuration file ("
                    + DEFAULT_CONF_FILE_PROPERTY_NAME + ") was null");
        context.log("NtropaBuilder: Default configuration file: '" + defaultConfFile + "'");

        final String alternativeConfFile = System.getProperty(ALTERNATIVE_CONF_FILE_PROPERTY_NAME);
        context.log("NtropaBuilder: Alternative configuration file: '" + alternativeConfFile + "'");

        final String selectedConfFile = alternativeConfFile != null ? alternativeConfFile : defaultConfFile;
        context.log("NtropaBuilder: Selected configuration file: '" + selectedConfFile + "'");
        if (selectedConfFile == null)
            throw new IllegalStateException("No configuration file was configured.");

        File confFile = new File(selectedConfFile);

        Properties p = new Properties();
        try {
            p.load(new FileInputStream(confFile));
        } catch (FileNotFoundException e) {
            String msg = "NtropaBuilder: File was not found: '" + confFile.getAbsolutePath() + "'";
            context.log(msg);
            throw new RuntimeException(msg, e);
        } catch (IOException e) {
            String msg = "NtropaBuilder: File could not be read: '" + confFile.getAbsolutePath() + "'";
            context.log(msg);
            throw new RuntimeException(msg, e);
        }

        BuilderConfiguration bc = new PropertiesBuilderConfiguration(p);

        try {
            builder.init(bc);
        } catch (Exception e) {
            String msg = "NtropaBuilder: problem initializing Builder";
            context.log(msg);
            throw new RuntimeException(msg, e);
        }
        try {
            builder.start();
            builderStarted = true;
        } catch (Exception e) {
            String msg = "NtropaBuilder: problem starting Builder";
            context.log(msg);
            throw new RuntimeException(msg, e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        /*
         * If there was an exception in #contextDestroyed tomcat will destroy
         * the context. If the builder had not been started #stop will throw a
         * NPE.
         */
        if (builderStarted)
            builder.stop();
    }

}
