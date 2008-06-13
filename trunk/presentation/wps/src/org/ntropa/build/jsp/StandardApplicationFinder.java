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
 * StandardApplicationFinder.java
 *
 * Created on 21 November 2001, 17:18
 */

package org.ntropa.build.jsp;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Pattern;

import org.ntropa.build.Constants;
import org.ntropa.build.channel.FileLocation;
import org.ntropa.utility.CollectionsUtilities;
import org.ntropa.utility.PathWalker;


/**
 * 
 * @author jdb
 * @version $Id: StandardApplicationFinder.java,v 1.10 2006/03/09 12:49:24 jdb
 *          Exp $
 */
public class StandardApplicationFinder implements ApplicationFinder {

	/* The directory to conduct application binding data finding within */
	private File _rootDirectory;

	private FileLocation _pageLocation;

	private int _debug = 0;

	/**
	 * Creates new StandardApplicationFinder
	 * 
	 * @param rootDirectory
	 *            The top level directory.
	 * 
	 * @param pageLocation
	 *            A <code>FileLocation</code> object representing the relative
	 *            location of the page for which we are finding the application
	 *            parameters
	 */
	public StandardApplicationFinder(File rootDirectory, FileLocation pageLocation) {
		if (rootDirectory == null)
			throw new IllegalArgumentException("Attempt to construct StandardApplicationFinder from null rootDirectory");

		if (pageLocation == null)
			throw new IllegalArgumentException("Attempt to construct StandardApplicationFinder from null pageLocation");

		_rootDirectory = rootDirectory.getAbsoluteFile();

		_pageLocation = (FileLocation) pageLocation.clone();

	}

	/** Prevent no-arg construction */
	private StandardApplicationFinder() {
	};

	/**
	 * Set the debug level 0 - 99
	 * 
	 * @param debug
	 *            The required debug level in the range 0 - 99.
	 */
	public void setDebugLevel(int debug) {
		/* A negative arg is a mistake; go large in response */
		_debug = debug >= 0 ? debug : 99;
	}

	/**
	 * <p>
	 * Return the data to use for a server active object based on it's name or
	 * null if the name look up failed.
	 * 
	 * @param name
	 *            A <code>String</code> representing the name of the
	 *            ServerActiveHtml object to look up the corressponding data
	 *            for. This name is set in the HTML page
	 *            <p>
	 * 
	 * <pre>
	 *            &lt;-- name = &quot;the-name&quot; --&gt;
	 *            other elements and HTML
	 *            &lt;-- name = &quot;/the-name&quot; --&gt;
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @return A <code>Properties</code> with all the data corresponding to
	 *         the given name. If no data is found return null.
	 */
	public Properties getSaoData(String name) {
		/*
		 * If there is at least one property with the prefix sao.<name>. check
		 * for the class name property. If that exists we've found the right
		 * data.
		 */
		return getAppData(convertSaoNameToPropertyNamePrefix(name), CLASS_NAME_PROPNAME);
	}

	/**
	 * Browser cache related items begin with this.
	 */
	private static final String BROWSER_CACHE_PREFIX = "browser.cache.";

	/**
	 * The stripped property name used to determine if the browser should be
	 * asked to not use it's local cache.
	 */
	private static final String BROWSER_CACHE_DISABLE = "disable";

	/**
	 * Return true if the browser should be instructed to not serve pages from
	 * it's local cache.
	 * 
	 * @return True if code should be generated to disable the browser's use of
	 *         it's local cache otherwise false.
	 */
	public boolean isBrowserCacheDisable() {

		Properties browserRelated = getAppData(BROWSER_CACHE_PREFIX, BROWSER_CACHE_DISABLE);
		if (browserRelated == null)
			return false;
		return browserRelated.getProperty(BROWSER_CACHE_DISABLE).equals("yes");
	}

	/**
	 * Proxy cache related items begin with this.
	 */
	private static final String PROXY_CACHE_PREFIX = "proxy.cache.";

	/**
	 * The stripped property name used to determine if the proxy should be asked
	 * to not use it's cache.
	 */
	private static final String PROXY_CACHE_DISABLE = "disable";

	/**
	 * Return true if the proxy should be instructed to not serve pages from
	 * it's cache.
	 * 
	 * @return True if headers should be generated to disable the proxy's use of
	 *         it's cache otherwise false.
	 */
	public boolean isProxyCacheDisable() {

		Properties proxyRelated = getAppData(PROXY_CACHE_PREFIX, PROXY_CACHE_DISABLE);
		if (proxyRelated == null)
			return false;
		return proxyRelated.getProperty(PROXY_CACHE_DISABLE).equals("yes");
	}

	/**
	 * Return the first set of application properties which
	 * <ul>
	 * <li>Start with the given prefix
	 * <li>and include the given property
	 * </ul>
	 * If these properties existed in the same application properties file
	 * <p>
	 * browser.ie.level = 5<br>
	 * browser.ie.option = 7<br>
	 * then invoking getProperties ( &quot;browser.ie&quot;, &quot;option&quot; )
	 * would result in this set of properties being returned
	 * <p>
	 * level = 5<br>
	 * option = 7<br>
	 * 
	 * @param propertyPrefix
	 *            The common prefix to look for and strip.
	 * 
	 * @param requiredPropertyName
	 *            The required property to look for.
	 * 
	 * @return A <code>Properties</code> object with a properties with the
	 *         required prefixed stripped that includes the given property name
	 *         or null.
	 */
	private Properties getAppData(String propertyPrefix, String requiredPropertyName) {

		if (propertyPrefix == null || propertyPrefix.length() == 0)
			return null; /*
							 * Not sure why this is not throw new
							 * IllegalArgumentException
							 */

		if (requiredPropertyName == null || requiredPropertyName.length() == 0)
			throw new IllegalArgumentException("requiredPropertyName was null or zero-length");

		/*
		 * Look for the application parameters in the _application directory in
		 * the same directory as the JSP being built. If not found look in the
		 * _application directory in the parent directory and so on until we get
		 * to /_application (context relative).
		 */

		PathWalker walker = new PathWalker(_pageLocation.getLocation());

		if (_debug >= 2)
			log("getSaoData: _pageLocation: " + _pageLocation);

		Iterator it = walker.iterator();

		/* drop last path element, the file name */
		if (it.hasNext())
			it.next();

		while (it.hasNext()) {

			String nextParentDir = (String) it.next();

			String relativeAppDir;
			if (nextParentDir != "")
				relativeAppDir = nextParentDir + File.separator + Constants.getApplicationDirectoryName();
			else
				relativeAppDir = Constants.getApplicationDirectoryName();

			/* does the directory exist? */
			File absoluteAppDir = new File(_rootDirectory, relativeAppDir);

			if (!absoluteAppDir.isDirectory())
				continue;

			File optionalAppParamFiles[] = absoluteAppDir.listFiles(getOptionalAppParamsFileFilter());

			File defaultAppParamFile = new File(absoluteAppDir, Constants.getApplicationParamFileName());
			if (!defaultAppParamFile.isFile())
				defaultAppParamFile = null;

			File appParamFiles[] = combine(optionalAppParamFiles, defaultAppParamFile);

			Properties appData = null;

			/* Check each param file in turn until the data is found */
			for (int appParamFileIdx = 0; appParamFileIdx < appParamFiles.length && appData == null; appParamFileIdx++) {

				File appParamFile = appParamFiles[appParamFileIdx];

				/*
				 * We have found an application parameter file. See if the
				 * stripped property name is defined in it. If present then we
				 * have the entire set of data applicable to this prefix
				 */
				try {
					Properties p = new Properties();
					p.load(new FileInputStream(appParamFile));
					Properties q = CollectionsUtilities.getPropertiesSubset(p, propertyPrefix);

					/*
					 * If there was at least one property with the prefix check
					 * for the named property. If that exists we've found the
					 * right data.
					 */
					if (q != null) {
						if (q.getProperty(requiredPropertyName) != null)
							appData = q;
					}
				} catch (IOException e) {
					log("getAppData" + e.toString());
					return null;
				}

				if (appData != null) {
					if (_debug >= 3) {
						log("getAppData: relativeAppDir: " + relativeAppDir);
						log("getAppData: absoluteAppDir:" + absoluteAppDir);
						log("getAppData: appParamFile: " + appParamFile);
						log("getAppData: appData: " + appData);
					}
				}
			}

			if (appData != null) {
				return appData;
			}
		}
		return null;
	}

	/**
	 * Combine a list of optional file with a possibly null default file into a
	 * single array.
	 * 
	 * The optional files are first in the array followed by the default, if the
	 * default exists.
	 * 
	 * @param optionalAppParamFiles
	 *            an unordered list of optional param files, possibly zero
	 *            length
	 * @param defaultAppParamFile
	 *            the default param files, possibly null
	 * @return An array of files in the order to check them.
	 */
	private File[] combine(File[] optionalAppParamFiles, File defaultAppParamFile) {
		Arrays.sort(optionalAppParamFiles, getFileComparator());

		File result[] = new File[optionalAppParamFiles.length + (defaultAppParamFile == null ? 0 : 1)];
		
		for (int i = 0; i < optionalAppParamFiles.length; i++)
			result[i] = optionalAppParamFiles[i];

		if (defaultAppParamFile != null)
			result[result.length - 1] = defaultAppParamFile;

		return result;
	}

	private final Comparator fileComparator = new Comparator() {

		public int compare(Object o1, Object o2) {
			String name1 = ((File) o1).getName();
			String name2 = ((File) o2).getName();
			return name1.compareTo(name2);
		}

	};

	private Comparator getFileComparator() {
		return fileComparator;

	}

	/*
	 * Reject "application.properties". Accept others matching regex. For
	 * example accept
	 * 
	 * news-feeds.properties login.properties register-v2.properties
	 */
	private final FileFilter optionalAppParamsFileFilter = new FileFilter() {

		Pattern acceptable = Pattern.compile("[a-z]+[-a-z0-9]*[a-z0-9]+\\.properties");

		public boolean accept(File pathname) {
			String name = pathname.getName();
			if (Constants.getApplicationParamFileName().equals(name))
				return false;

			if (!pathname.isFile())
				return false;

			return acceptable.matcher(name).matches();
		}
	};

	private FileFilter getOptionalAppParamsFileFilter() {
		return optionalAppParamsFileFilter;
	}

	/**
	 * Return the name of the class to use for a server active object or null if
	 * the name is not in the <code>Properties</code> object.
	 * 
	 * The queried <code>Properties</code> object will typically be obtained
	 * from getSaoData ().
	 * 
	 * @param saoData
	 *            A <code>Properties</code> object which may or may not
	 *            contain the class name
	 * 
	 * @return A <code>String</code> representing the name of the
	 *         ServerActiveHtml object to look up the corressponding class name
	 *         for.
	 */
	public String getClazzName(Properties saoData) {
		if (saoData == null)
			throw new IllegalArgumentException("Null Properties object (saoData)");
		return saoData.getProperty(CLASS_NAME_PROPNAME);
	}

	/**
	 * Return the name of the class to use for this name or null if the name
	 * look up failed.
	 * 
	 * @param name
	 *            A <code>String</code> representing the name of the
	 *            ServerActiveHtml object to look up the corressponding class
	 *            name for.
	 * 
	 * @return A <code>String</code> representing the class name
	 *         corressponding to the given name or null if no name match was
	 *         found.
	 */
	public String getClazzName_(String name) {
		if (name == null || name.length() == 0)
			return null;

		/*
		 * Look for the application parameters in the _application directory in
		 * the same directory as the JSP being built. If not found look in the
		 * _application directory in the parent directory and so on until we get
		 * to /_application (context relative).
		 */

		PathWalker p = new PathWalker(_pageLocation.getLocation());

		if (_debug >= 2)
			log("getClazzName: _pageLocation: " + _pageLocation);

		Iterator it = p.iterator();

		/* drop last path element, the file name */
		if (it.hasNext())
			it.next();

		while (it.hasNext()) {

			String nextParentDir = (String) it.next();

			String relativeAppDir;
			if (nextParentDir != "")
				relativeAppDir = nextParentDir + File.separator + Constants.getApplicationDirectoryName();
			else
				relativeAppDir = Constants.getApplicationDirectoryName();

			/* does the directory exist? */
			File absoluteAppDir = new File(_rootDirectory, relativeAppDir);

			if (!absoluteAppDir.isDirectory())
				continue;

			File appParamFile = new File(absoluteAppDir, Constants.getApplicationParamFileName());

			if (!appParamFile.isFile())
				continue;

			/*
			 * We have found an application parameter file. See if the name is
			 * defined in it
			 */
			String value = null;
			try {
				value = CollectionsUtilities.lookupName(appParamFile, convertSaoNameToClassNamePropertyName(name));
			} catch (IOException e) {
				log("lookupName" + e.toString());
				return null;
			}
			if (value != null) {
				if (_debug >= 3) {
					log("getClazzName: relativeAppDir: " + relativeAppDir);
					log("getClazzName: absoluteAppDir:" + absoluteAppDir);
					log("getClazzName: appParamFile: " + appParamFile);
				}
				return value;
			}
		}
		return null;
	}

	/**
	 * Given the name of a server active html return the name used in the
	 * Property files in the _application directories for the class name
	 * 
	 * @param saoName
	 *            The name a HTML designer uses for the SAO
	 * @return The property name used by this class to store the class name
	 *         under
	 */
	public static String convertSaoNameToClassNamePropertyName(String saoName) {
		return convertSaoNameToPropertyNamePrefix(saoName) + CLASS_NAME_PROPNAME;
	}

	/**
	 * Given the name of a server active html return the property name prefix
	 * used in the Property files in the _application directories for the named
	 * sao
	 * 
	 * @param saoName
	 *            The name a HTML designer uses for the SAO
	 * @return The property name prefix used by this class to store the
	 *         properties related to the name
	 */
	public static String convertSaoNameToPropertyNamePrefix(String saoName) {
		return "sao." + saoName + ".";
	}

	/**
	 * FIXME: use proper logger passed in at construction.
	 */
	private void log(String msg) {
		System.out.println("[" + this.getClass().getName() + "] " + msg);
	}

}