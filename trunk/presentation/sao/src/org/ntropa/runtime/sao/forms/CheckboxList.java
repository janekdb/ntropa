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

package org.ntropa.runtime.sao.forms;

import java.util.Iterator;

import org.ntropa.runtime.sao.AbstractElement;
import org.ntropa.runtime.sao.InvocationContext;
import org.ntropa.runtime.sao.PlaceholderReplacementStack;


/**
 * This class encapsulates the logic for rendering a list of checkboxes
 * <p>
 * The object uses an <code>Element</code> which contains the HTML for an
 * &lt;input type=checkbox value="xxx"&gt; element. This element should contain
 * these placeholders for these placeholder codes:
 * <ul>
 * <li>checkbox-label
 * <li>checkbox-is-checked
 * <li>checkbox-value
 * </ul>
 * </p>
 * <p>
 * This element is sufficient: <code>
 * <pre>
 *     &lt;input name=&quot;study-location-preference&quot; value=&quot;$$checkbox-value$$&quot; type=&quot;checkbox&quot; $$checkbox-is-checked$$&gt;
 * </pre>
 * </code>
 * 
 * @author Janek Bogucki
 * @version $Id: CheckboxList.java,v 1.1 2006/03/01 15:03:38 jdb Exp $
 */
public class CheckboxList extends SetChoice {

	private AbstractElement checkboxElement;

	private PlaceholderReplacementStack stack;

	/**
	 * Creates new SelectControl
	 */
	public CheckboxList(AbstractElement checkboxElement, PlaceholderReplacementStack stack) {

		if (checkboxElement == null)
			throw new IllegalArgumentException("checkboxElement was null");

		this.checkboxElement = checkboxElement;

		if (stack == null)
			throw new IllegalArgumentException("stack was null");

		this.stack = stack;
	}

	private static final String PLACEHOLDER_CHECKBOX_LABEL = "checkbox-label";

	private static final String PLACEHOLDER_CHECKBOX_IS_CHECKED = "checkbox-is-checked";

	private static final String PLACEHOLDER_CHECKBOX_VALUE = "checkbox-value";

	public void render(InvocationContext icb) throws Exception {
		
		if (getValueList ()== null)
			throw new IllegalStateException("valueList had not been set");

		if (getLabels ()== null)
			throw new IllegalStateException("labels had not been set");

		if (getPredicate ()== null)
			throw new IllegalStateException("predicate had not been set");

		for (Iterator it = getValueList().iterator(); it.hasNext();) {

			String value = (String) it.next();

			stack.mark();

			stack.push(PLACEHOLDER_CHECKBOX_VALUE, value);
			stack.push(PLACEHOLDER_CHECKBOX_LABEL, (String) getLabels().get(value));
			stack.push(PLACEHOLDER_CHECKBOX_IS_CHECKED, getPredicate().evaluate(value) ? "checked" : "");

			checkboxElement.render(icb);

			stack.popToMark();

		}
	}

}
