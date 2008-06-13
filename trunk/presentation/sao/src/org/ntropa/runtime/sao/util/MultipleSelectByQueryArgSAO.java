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
 * Created on 17-Sep-2004
 *
 * MultipleSelectByQueryArgSAO
 */
package org.ntropa.runtime.sao.util;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;

/**
 * This server active object selects zero or more child elements to render
 * based on the value of a query argument which can be multi-valued. The value is used
 * directly. There is no default element.
 * <p>
 * To use this sao markup the HTML with a list of elements
 * <p>
 * <pre>
 *      &lt;-- name = &quot;errors&gt; --&gt;
 * 
 *      &lt;-- element = &quot;email-misssing&gt; --&gt;
 *          Please enter an email
 *      &lt;-- element = &quot;/email-misssing&gt; --&gt;
 * 
 *      &lt;-- element = &quot;first-name-missing&gt; --&gt;
 *          &lt;font color=red&gt;Warning! First name is required&lt;/font&gt;
 *      &lt;-- element = &quot;/first-name-missing&gt; --&gt;
 * 
 *      &lt;-- element = &quot;last-name-missing&gt; --&gt;
 *          &lt;font color=red&gt;Warning! Last name is required&lt;/font&gt;
 *      &lt;-- element = &quot;/last-name-missing&gt; --&gt;
 * 
 *      &lt;-- name = &quot;/errors&gt; --&gt;
 * </pre>
 * <p>
 * Set the name of the query argument to inspect in the application.properties file.
 * <p>
 * <pre>
 * sao.errors.class-name = org.ntropa.runtime.sao.util.MultipleSelectByQueryArgSAO
 * sao.errors.prop.parameterName = error
 * </pre>
 * <p>
 * Link to the page using this sao like this
 * <p>
 * <pre>
 *  http://idp.studylink.com.au/index.html?error=email-misssing&error=last-name-missing
 * </pre>
 * <p>
 * If the elements 'email-misssing' and 'last-name-missing' exist they are rendered otherwise nothing is
 * rendered.
 *<p>
 *Linking to the page without the query argument like this
 * <p>
 * <pre>
 *  http://idp.studylink.com.au/index.html
 * </pre>
 * <p>
 * results in nothing being rendered.
 * <p>
 *
 * @author Janek Bogucki
 *
 * @version $Id: MultipleSelectByQueryArgSAO.java,v 1.1 2004/09/17 19:32:09 jdb Exp $
 */
public class MultipleSelectByQueryArgSAO extends BaseServerActiveObject {

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

		String parameterValues[] = icb.getHttpServletRequest().getParameterValues(getParameterName());

		if (null == parameterValues)
			return;

		for (int i = 0; i < parameterValues.length; i++) {
			String child = parameterValues[i];
			/*
			 * getChild will throw an exception if the named element does not exist.
			 */
			if (childExists(child))
				getChild(child).render(icb);

		}

	}
}