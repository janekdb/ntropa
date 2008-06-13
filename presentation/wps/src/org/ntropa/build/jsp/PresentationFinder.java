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
 * PresentationFinder.java
 *
 * Created on 28 November 2001, 17:50
 */

package org.ntropa.build.jsp;

/**
 * An <code>ApplicationFinder</code> looks up a class name for a
 * <code>ServerActiveHtml</code> based on the given name.
 * 
 * @author rj
 * @version $Id: PresentationFinder.java,v 1.3 2006/03/22 16:31:19 jdb Exp $
 */
public interface PresentationFinder {

	/**
	 * 
	 * @return A <code>String</code> with the keywords for the current page
	 */
	String getKeywords();

	/**
	 * 
	 * @return A <code>String</code> with the page description for the current
	 *         page
	 */
	String getDescription();

	/**
	 * @return A <code>String</code> with the public id for the current page or null
	 */
	String getDoctypePublicId();
	
	/**
	 * @return A <code>String</code> with the system id for the current page or null
	 */
	String getDoctypeSystemId();
	
	
}
