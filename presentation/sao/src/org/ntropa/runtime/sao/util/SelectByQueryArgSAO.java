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
 * SelectByQueryArgSAO.java
 *
 * Created on 20 March 2002, 14:56
 */

package org.ntropa.runtime.sao.util;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;

/**
 * This server active object selects a child element to render
 * based on the value of a query argument. The value is used
 * directly. If the element does not exist the default element
 * is used.
 * <p>
 * To use this sao markup the HTML with a list of elements
 * <p>
 * <pre>
 *      &lt;-- name = &quot;my-selector&gt; --&gt;
 *      &lt;-- element = &quot;on&gt; --&gt;
 *          the qwon feature is available
 *      &lt;-- element = &quot;/on&gt; --&gt;
 *      &lt;-- element = &quot;offline&gt; --&gt;
 *          &lt;font color=red&gt;Warning! The qwon feature is unavailable&lt;/font&gt;
 *      &lt;-- element = &quot;/offline&gt; --&gt;
 *      &lt;-- name = &quot;/my-selector&gt; --&gt;
 * </pre>
 * <p>
 * Set the name of the query argument to inspect and the name of the default element
 * in the application.properties
 * file
 * <p>
 * <pre>
 * sao.my-selector.class-name = org.ntropa.runtime.sao.util.SelectByQueryArgSAO
 * sao.my-selector.prop.parameterName = foodtype
 * sao.my-selector.prop.defaultElement = halalFood
 * </pre>
 * <p>
 * Link to the page using this sao like this
 * <p>
 * <pre>
 *  http://idp.studylink.com.au/index.html?foodtype=bacon
 * </pre>
 * <p>
 * If the element 'bacon' exists it is rendered otherwise the element 'halalFood' is
 * rendered.
 *<p>
 *Linking to the page without the query argument like this
 * <p>
 * <pre>
 *  http://idp.studylink.com.au/index.html
 * </pre>
 * <p>
 * results in the element 'halalFood' being rendered.
 * <p>
 *
 * @author  rj
 * @version $Id: SelectByQueryArgSAO.java,v 1.11 2004/09/16 19:37:52 jdb Exp $
 */
public class SelectByQueryArgSAO extends BaseServerActiveObject {

	/*
	 * Bean getters/setters
	 */

	/**
	 * The name of the query argument to inspect to get
	 * the name of the child element to render.
	 */
	private String parameterName = null;

	/**
	 * Set the name of the request argument to inspect
	 */
	public void setParameterName(String argumentName) {
		this.parameterName = argumentName;
	}

	private String getParameterName() {
		if (parameterName == null)
			throw new IllegalStateException("'parameterName' had not been set");
		return parameterName;
	}

	/*
	 * The name of the default element
	 */
	private String defaultElement = null;

	public void setDefaultElement(String defaultElement) {
		this.defaultElement = defaultElement;
	}

	private String getDefaultElement() {
		if (defaultElement == null)
			throw new IllegalStateException("'defaultElement' had not been set");
		return defaultElement;
	}

	public void render(InvocationContext icb) throws Exception {

		/*
		 * Store a reference to the InvocationContext so the application
		 * log can be accessed in AbstractServerActiveObject.
		 *
		 * This method does not invoke any child elements which might make
		 * a callback so it not neccessary to set the invocation context
		 * for any other reason than logging.
		 */
		setInvocationContext(icb);

		String parameterValue = (String) icb.getHttpServletRequest().getParameter(getParameterName());

		if (getDebug() >= 1)
			log("parameterName: " + parameterName + ", parameterValue: " + parameterValue);

		/* Checks value whatever later code path to warn developer asap */
		String def = getDefaultElement();

		/*
		 * getChild will throw an exception if the named element does not exist.
		 */
		if (childExists(parameterValue)) {
			getChild(parameterValue).render(icb);
		} else {
			getChild(def).render(icb);
		}
	}

}
