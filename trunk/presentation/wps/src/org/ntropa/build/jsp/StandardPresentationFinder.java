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
 * StandardPresentationFinder.java
 *
 * Created on 28 November 2001, 17:44
 */

package org.ntropa.build.jsp;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.ntropa.build.Constants;
import org.ntropa.build.channel.FileLocation;
import org.ntropa.utility.CollectionsUtilities;
import org.ntropa.utility.PathWalker;


/**
 * 
 * @author rj
 * @version $Id: StandardPresentationFinder.java,v 1.2 2001/11/29 16:03:50 jdb
 *          Exp $
 */
public class StandardPresentationFinder implements PresentationFinder {

	/* The directory to conduct presentation binding data finding within */
	private File _rootDirectory;

	private FileLocation _pageLocation;

	private int _debug = 0;

	/**
	 * Creates new StandardPresentationFinder
	 * 
	 * @param rootDirectory
	 *            The top level directory.
	 * 
	 * @param pageLocation
	 *            A <code>FileLocation</code> object representing the relative
	 *            location of the page for which we are finding the presentation
	 *            parameters
	 */
	public StandardPresentationFinder(File rootDirectory, FileLocation pageLocation) {
		if (rootDirectory == null)
			throw new IllegalArgumentException(
					"Attempt to construct StandardPresentationFinder from null rootDirectory");

		if (pageLocation == null)
			throw new IllegalArgumentException("Attempt to construct StandardPresentationFinder from null pageLocation");

		_rootDirectory = rootDirectory.getAbsoluteFile();

		_pageLocation = (FileLocation) pageLocation.clone();

	}

	/** Prevent no-arg construction */
	private StandardPresentationFinder() {
	};

	/**
	 * 
	 * @return A <code>String</code> with the keywords for the current page
	 */
	public String getKeywords() {
		return getProperty("keywords");
	}

	/**
	 * 
	 * @return A <code>String</code> with the page description for the current
	 *         page
	 */
	public String getDescription() {
		return getProperty("description");
	}

	/**
	 * @return A <code>String</code> with the public id for the current page
	 *         or null
	 */
	public String getDoctypePublicId() {
		return getProperty("doctype-public");
	}

	/**
	 * @return A <code>String</code> with the system id for the current page
	 *         or null
	 */
	public String getDoctypeSystemId() {
		return getProperty("doctype-system");
	}

	/**
	 * Return the value of the property to use for this name or null if the name
	 * look up failed.
	 * 
	 * @param name
	 *            A <code>String</code> representing the name of the
	 *            Presentation parameter to lookup.
	 * 
	 * @return A <code>String</code> representing the Presentation parameter
	 *         or null if no name match was found.
	 */
	private String getProperty(String name) {
		if (name == null || name.length() == 0)
			return null;

		/*
		 * Look for the presentation parameters in the _presentation directory
		 * in the same directory as the JSP being built. If not found look in
		 * the _presentation directory in the parent directory and so on until
		 * we get to /_presentation (context relative).
		 */

		PathWalker p = new PathWalker(_pageLocation.getLocation());

		if (_debug >= 2)
			log("getProperty: _pageLocation: " + _pageLocation);

		Iterator it = p.iterator();

		/* drop last path element, the file name */
		if (it.hasNext())
			it.next();

		while (it.hasNext()) {

			String nextParentDir = (String) it.next();

			String relativePresentationDir;
			if (nextParentDir != "")
				relativePresentationDir = nextParentDir + File.separator + Constants.getPresentationDirectoryName();
			else
				relativePresentationDir = Constants.getPresentationDirectoryName();

			/* does the directory exist? */
			File absolutePresentationDir = new File(_rootDirectory, relativePresentationDir);

			if (!absolutePresentationDir.isDirectory())
				continue;

			File presentationParamFile = new File(absolutePresentationDir, Constants.getPresentationParamFileName());

			if (!presentationParamFile.isFile())
				continue;

			/*
			 * We have found an presentation parameter file. See if the name is
			 * defined in it
			 */
			String value = null;
			try {
				value = CollectionsUtilities.lookupNameCaseInsensitive(presentationParamFile, name);
			} catch (IOException e) {
				log("lookupName" + e.toString());
				return null;
			}
			if (value != null) {
				if (_debug >= 3) {
					log("getProperty: relativePresentationDir: " + relativePresentationDir);
					log("getProperty: absolutePresentationDir:" + absolutePresentationDir);
					log("getProperty: presentationParamFile: " + presentationParamFile);
				}
				return value;
			}
		}
		return null;
	}

	/**
	 * FIXME: use proper logger passed in at construction.
	 */
	private void log(String msg) {
		System.out.println("[" + this.getClass().getName() + "] " + msg);
	}

}
