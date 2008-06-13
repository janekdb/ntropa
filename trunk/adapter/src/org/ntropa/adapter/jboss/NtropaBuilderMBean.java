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

/**
 * This interface is to support a MBean with JBoss.
 * 
 * For details on JBoss and JMX consult 'JBoss Administration and Development'
 * available through http://www.flashline.com.
 * 
 * @author jdb
 * @version $Id: UpdateProcessMBean.java,v 1.3 2001/11/01 18:06:31 jdb Exp $
 */
public interface NtropaBuilderMBean extends org.jboss.util.ServiceMBean {

	public int getUpdateCheckSeconds();

	public void setUpdateCheckSeconds(int updateCheckSeconds);

}
