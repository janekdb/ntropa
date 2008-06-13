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
 * Created on 11-Oct-2004
 *
 * DefaultingSelectByPlaceholderReplacementValueSAO
 */
package org.ntropa.runtime.sao.util;

/**
 * This SAO chooses an element with the same name as the value of a placeholder replacement, if
 * such an element exists, otherwise it will choose the default element 'default', if that element
 * exists, otherwise nothing will be choosen (rendered).
 * <p>
 * Example: A text string should be shown only if the integer value is > 0 and != 7. We're assuming the
 * integer is always >= 0.
 * <p>
 * To do this, in example.html use this marked up HTML:
 * <pre>
 *
 * &lt;!-- name="cond" --&gt;
 * &lt;!-- element="0" --&gt;&lt;!-- element="/0" --&gt;
 * &lt;!-- element="7" --&gt;&lt;!-- element="/7" --&gt;
 * &lt;!-- element="default" --&gt;My text string&lt;!-- element="/default" --&gt;
 * &lt;!-- name="/cond" --&gt;
 * </pre>
 *
 * <p>
 * In application.properties
 * <p>
 * <pre>
 * sao.cond.class-name=org.ntropa.runtime.sao.util.DefaultingSelectByPlaceholderReplacementValueSAO
 * sao.cond.prop.placeholderCode=variation-count
 * </pre>
 * <p>
 * The value for the placeholder code must be a string representation of an integer.
 * <p>
 * Note: The HTML optionally rendered does not need to include the placeholder code being tested.
 *
 * @author Janek Bogucki
 *
 * @version $Id: DefaultingSelectByPlaceholderReplacementValueSAO.java,v 1.1 2004/10/11 11:21:21 jdb Exp $
 */
public class DefaultingSelectByPlaceholderReplacementValueSAO extends SelectByPlaceholderReplacementPropertySAO {

	private static final String DEFAULT_ELEMENT_NAME = "default";

	public String getElementNameForPlaceholderReplacementValue(String rep) {

		if (childExists(rep))
			return rep;

		if (childExists(DEFAULT_ELEMENT_NAME))
			return DEFAULT_ELEMENT_NAME;

		return null;
	}

}
