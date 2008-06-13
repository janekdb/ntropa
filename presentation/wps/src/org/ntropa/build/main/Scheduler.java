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
 * Scheduler.java
 *
 * Created on September 10, 2001, 5:12 PM
 */

package org.ntropa.build.main;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.ntropa.build.ContextPath;
import org.ntropa.build.FileChangeSet;
import org.ntropa.build.channel.ChannelMonitor;

/**
 * The JSP Update Process scheduler. (Copied from
 * org.ntropa.runtime.main.JUPScheduler)
 * 
 * @author jdb
 * @version $Id: Scheduler.java,v 1.5 2002/12/09 18:05:14 jdb Exp $
 */
public class Scheduler implements Runnable {

    private List<Pair> pairs;

    private int m_period;

    private int m_checkSeconds;

    private boolean _continue;

    private static class Pair {

        final ContextPath contextPath;

        final ChannelData channelData;

        Pair(ContextPath contextPath, ChannelData channelData) {
            this.contextPath = contextPath;
            this.channelData = channelData;
        }
    }

    /**
     * @param channelTBL
     *            A map of channel related data.
     * @param period
     *            The numbers of seconds allowed to elapse between checks on a
     *            channel.
     * @param checkSeconds
     *            The number of seconds to wait between checking for channels
     *            that need a check.
     */
    public Scheduler(Map<ContextPath, ChannelData> channelTBL, int period, int checkSeconds) {

        if (period > 0)
            m_period = period;
        else
            m_period = 10;

        if (checkSeconds > 0)
            m_checkSeconds = checkSeconds;
        else
            m_checkSeconds = 1;

        /*
         * Convert the map to a list so the channels can be scanned in alpha
         * order
         */
        pairs = new LinkedList<Pair>();
        for (ContextPath cp : new TreeSet<ContextPath>(channelTBL.keySet())) {
            pairs.add(new Pair(cp, channelTBL.get(cp)));
        }
    }

    /**
     * After calling this method the scheduler will stop looking for changed
     * channels.
     * 
     * (This was added to stop the scheduler from UpdateProcess.)
     */
    public void stop() {
        _continue = false;
    }

    /**
     * Loop looking for channels that are ready to be checked. Check for
     * channels that need to be checked every few seconds. The channel is
     * checked if it was last checked more than or equal to m_period seconds
     * ago.
     */
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        _continue = true;

        /*
         * This class needs to be changed to fit into JBoss (if JBoss has
         * service-like support).
         */
        while (_continue) {

            // Look for channels that have timed out.
            long curMillis = System.currentTimeMillis();

            for (Pair p : pairs) {
                ContextPath cp = p.contextPath;
                ChannelData cd = p.channelData;

                ChannelMonitor cm = cd.getChannelMonitor();

                if (cd.isHighAlert(curMillis) || (cd.getLastCheckTime() <= curMillis - m_period * 1000)) {

                    // System.out.println ("Checking: " + cid );

                    /*
                     * monitorFolder () notifies its listeners. These listeners
                     * notify their own listeners.
                     * 
                     * (monitorFolder () returns an object; we just use it to
                     * find the number of changes.
                     */
                    FileChangeSet fcs = cm.update();

                    if (fcs.size() > 0) {
                        System.out.println("Changes for " + cp + ": added: " + +fcs.getAdded().size() + ", modified: "
                                + fcs.getModified().size() + ", deleted: " + fcs.getDeleted().size());
                        cd.setHighAlert(curMillis);
                    }

                    /*
                     * Update after the work is done to ensure the next update
                     * is actually in the future.
                     */
                    cd.setLastCheckTime(System.currentTimeMillis());
                }

            }

            statistics(System.currentTimeMillis() - curMillis);

            /*
             * Wait some time before checking all channels again.
             */
            try {
                Thread.sleep(m_checkSeconds * 1000);
            } catch (InterruptedException e) {
            }

        }

    }

    private final List<Long> scanTimes = new LinkedList<Long>();

    private void statistics(long scanTime) {
        scanTimes.add(scanTime);
        long total = 0;
        for (long t : scanTimes) {
            total += t;
        }

        System.out.println("Millis for scan:     " + scanTime);
        System.out.println("Average scan millis: " + (total / scanTimes.size()));

        if (scanTimes.size() >= 10) {
            scanTimes.remove(0);
        }

    }
}
