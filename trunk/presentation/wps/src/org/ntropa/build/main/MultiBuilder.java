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

/*
 * UpdateProcess.java
 *
 * Created on 30 October 2001, 17:31
 */

package org.ntropa.build.main;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ntropa.build.Constants;
import org.ntropa.build.ContextPath;
import org.ntropa.build.channel.ChannelMonitorException;
import org.ntropa.build.channel.CommonRootResolver;
import org.ntropa.build.channel.StandardChannelMonitor;
import org.ntropa.build.mapper.Resolver;

/**
 * This class is designed to run as a MBean with JBoss.
 * 
 * @author jdb
 * @version $Id: UpdateProcess.java,v 1.13 2002/12/11 14:34:10 jdb Exp $
 */
public class MultiBuilder implements Builder {

    private File m_WebDAVLocation;

    private File m_SymbolicFilesystemLocation;

    private File m_JSPLocation;

    private int m_SchedulePeriodSeconds;

    private Hashtable m_ChannelTable;

    private Hashtable m_EncodingTable;

    private Scheduler _scheduler;

    private Thread _schedulerThread;

    /**
     * The default value for the interval in seconds between checks for channels
     * that are ready to be checked for changes.
     */
    private static final int DEFAULT_UPDATE_CHECK_SECONDS = 1;

    /* How often a check that a channel update is required is done */
    private int _checkSeconds = DEFAULT_UPDATE_CHECK_SECONDS;

    public int getUpdateCheckSeconds() {
        System.out.println("getUpdateCheckSeconds: " + _checkSeconds);
        return _checkSeconds;
    }

    public void setUpdateCheckSeconds(int updateCheckSeconds) {
        System.out.println("setUpdateCheckSeconds: " + updateCheckSeconds);
        _checkSeconds = updateCheckSeconds > 0 ? updateCheckSeconds : DEFAULT_UPDATE_CHECK_SECONDS;
    }

    /**
     * Life cycle methods
     */

    public void stop() {
        _scheduler.stop();
        _schedulerThread.interrupt();
        /* wait for scheduler to finish work */
        /*
         * JBoss was sometimes taking a long time to shutdown, paused on this
         * method so it's disabled until I can find the problem.
         * 
         * Found the problem, I had thousands of files in a channel and the
         * channel was still being processed.
         */

        while (_schedulerThread.isAlive()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }

    }

    public void init(BuilderConfiguration bc) throws Exception {

        if (bc == null)
            throw new IllegalArgumentException("BuilderConfiguration was null");

        configure(bc);
        initObjects();

    }

    public void start() throws Exception {

        /**
         * Loop endlessly
         */
        _schedulerThread = new Thread(_scheduler);
        _schedulerThread.start();

    }

    /**
     * Configuration and check the input, link and output directories. Build a
     * table of context paths and encodings.
     * 
     * @throws Exception
     *             if any of the input, link or output directories are missing.
     */
    private void configure(BuilderConfiguration bc) throws Exception {

        /*
         * read WebDav directory location
         */
        m_WebDAVLocation = bc.inputDirectory();
        if (!m_WebDAVLocation.isDirectory()) {
            throw new Exception("WebDAV directory missing: " + m_WebDAVLocation.getAbsoluteFile());
        }

        /*
         * read symbolic filesystems location
         */
        m_SymbolicFilesystemLocation = bc.linkDirectory();
        if (!m_SymbolicFilesystemLocation.isDirectory()) {
            throw new Exception("Symbolic filesystem directory missing: "
                    + m_SymbolicFilesystemLocation.getAbsoluteFile());
        }

        /*
         * read JSP container web directory
         */
        m_JSPLocation = bc.outputDirectory();
        if (!m_JSPLocation.isDirectory()) {
            throw new Exception("JSP directory missing: " + m_JSPLocation.getAbsoluteFile());
        }

        m_ChannelTable = new Hashtable();
        m_EncodingTable = new Hashtable();

        String contextPaths[] = bc.contextPaths();
        for (int i = 0; i < contextPaths.length; i++) {
            ContextPath cp = new ContextPath(contextPaths[i]);

            m_EncodingTable.put(cp, bc.encoding(cp.getPath()));

            m_ChannelTable.put(cp, new ChannelData());
        }

        /* A minimum of 10 is enforced by the BuilderConfiguration object. */
        m_SchedulePeriodSeconds = bc.schedulerPeriod();

    }

    /**
     * Create objects and link them together.
     */
    private void initObjects() throws Exception {

        CommonRootResolver resolver = new CommonRootResolver(m_SymbolicFilesystemLocation, m_ChannelTable.keySet());

        /* Initialise in alpha order because it looks better in the log file */
        SortedSet<ContextPath> paths = new TreeSet<ContextPath>(m_ChannelTable.keySet());
        for (ContextPath cp : paths) {
            System.out.println("init: " + cp);
            initChannel(cp, resolver);
        }

        /*
         * Create a Scheduler object. The object will check every _checkSeconds
         * seconds for a channel which needs to be checked. This isn't the same
         * as checking a channel every _checkSeconds seconds.
         */
        _scheduler = new Scheduler(m_ChannelTable, m_SchedulePeriodSeconds, _checkSeconds);

    }

    /**
     * Create and and save a reference to a ChannelMonitor object for the
     * channel.
     * 
     * FIXME: is this needed?: Also record the upload directory so the relative
     * file path can be calculated later.
     * 
     */
    private void initChannel(ContextPath cp, Resolver resolver) throws Exception {

        Charset encoding = (Charset) m_EncodingTable.get(cp);
        if (encoding == null)
            throw new NullPointerException("encoding was null for " + cp);

        StandardChannelMonitor cm = new StandardChannelMonitor(cp, // ContextPath
                // cp,
                encoding, // Charset encoding
                new File(m_WebDAVLocation, cp.getPath()), // File inputFolder,
                new File(m_SymbolicFilesystemLocation, cp.getPath()), // File
                // intermediateFolder,
                new File(m_JSPLocation, cp.getPath()), // File outputFolder,
                Constants.getNonHtmlDirectoryNames(), Constants.getMirroredFileSuffixes(), resolver);

        try {
            cm.init();
        } catch (ChannelMonitorException e) {
            throw new Exception("initChannel: StandardChannelMonitorException for context path: " + cp + " " + e);
        }
        ChannelData cd = (ChannelData) m_ChannelTable.get(cp);
        cd.setChannelMonitor(cm);

    }

}
