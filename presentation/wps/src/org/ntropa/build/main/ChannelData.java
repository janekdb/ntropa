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
 * ChannelData.java
 *
 * Created on September 10, 2001, 5:45 PM
 */

package org.ntropa.build.main ;

import org.ntropa.build.channel.ChannelMonitor;

/**
 * ChannelData
 *
 * This class contains all data for the channel used by the JSP update process.
 * FileListener is used instead of JSPBuilder to reflect the way the JSPBuilder
 * is used, we send three types of event to it only. Less is simpler, simpler is better.
 *
 * @author  jdb
 * @version $Id: ChannelData.java,v 1.2 2002/03/07 17:38:43 jdb Exp $
 */
public class ChannelData {
    
    private ChannelMonitor _cm ;
    
    private String _uploadPrefix ;
    
    private long _lastCheckTime ;
    
    /**
     * Millis to stay in high alert for.
     *
     * (80 mins; just longer than a lunch hour.)
     */
    private long HIGH_ALERT_MILLIS = 80 * 60 * 1000 ;
    
    /**
     * When the current system time in millis is greater then
     * this then the related channel is no longer in a state of
     * high alert.
     */
    private long _highAlertExpiryTime = 0 ;
    
    /** Creates new ChannelData */
    public ChannelData() {
        
        _cm = null ;
        
        _lastCheckTime = 0 ;
        
        _uploadPrefix = null ;
    }
    
    public void setChannelMonitor( ChannelMonitor cm ) {
        _cm = cm ;
    }
    
    public ChannelMonitor getChannelMonitor() {
        return _cm ;
    }
    
    
    /* moved to ChannelMonitor */
    /*
    public void setDirectoryMonitor ( DirectoryMonitor d ) {
        m_dm = d ;
    }
     
    public DirectoryMonitor getDirectoryMonitor () {
        return m_dm ;
    }
     */
    
    public void setUploadPrefix( String inPrefix ) {
        _uploadPrefix = inPrefix ;
    }
    
    public String getUploadPrefix() {
        return _uploadPrefix ;
    }
    
    public long getLastCheckTime() {
        return  _lastCheckTime ;
    }
    
    public void setLastCheckTime( long t ) {
        _lastCheckTime = t ;
    }
    
    /**
     * Invoke this to set the related channel to high alert.
     * The Scheduler checks channels which are in the high
     * alert state every time through the main loop instead
     * of only after a certain elapsed time since the last
     * check.
     *
     * The Scheduler puts a channel into high alert every time
     * there is a change to the channel. This means there is
     * a low latency while the channel is in use.
     *
     * @param millis The invoker should supply the current system millis.
     * This avoids multiple invocations of System.currentTimeMillis (),
     * apparently an expense method according to the author of WebMacro.
     */
    public void setHighAlert( long millis ) {
        _highAlertExpiryTime = millis + HIGH_ALERT_MILLIS ;
    }
    
    /**
     * Returns true if the related channel is in a state of high alert.
     *
     * @param millis The invoker should supply the current system millis.
     * This avoids multiple invocations of System.currentTimeMillis (),
     * apparently an expense method according to the author of WebMacro.
     */
    public boolean isHighAlert( long millis ) {
        return millis < _highAlertExpiryTime ;
    }
}

