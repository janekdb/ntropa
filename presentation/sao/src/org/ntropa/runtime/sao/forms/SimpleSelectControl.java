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
 * This class encapsulates the logic for rendering a select control (which does not
 * use &lt;optGroup&gt;).
 * <p>
 * The object uses an <code>Element</code> which contains the HTML for an
 * &lt;option value="xxx"&gt; element. This element should contain these
 * placeholders for these placeholder codes:
 * <ul>
 * <li>option-label
 * <li>option-is-selected
 * <li>option-value
 * </ul>
 * </p>
 * <p>
 * This element is sufficient: <code>
 * <pre>
 *   &lt;option value=&quot;$$option-value$$&quot; $$option-is-selected$$&gt;$$option-label
 * </pre>
 * </code>
 * <p>
 * The client of this class takes responsibility for rendering the opening and
 * closing &lt;select&gt; tags.
 * 
 * @author Janek Bogucki
 * @version $Id: SimpleSelectControl.java,v 1.1 2006/03/05 13:01:00 jdb Exp $
 */
public class SimpleSelectControl extends SetChoice {

	private AbstractElement optionElement;

	private PlaceholderReplacementStack stack;

	/**
	 * Creates new SelectControl
	 */
	public SimpleSelectControl(AbstractElement optionElement, PlaceholderReplacementStack stack) {

		if (optionElement == null)
			throw new IllegalArgumentException("optionElement was null");

		this.optionElement = optionElement;

		if (stack == null)
			throw new IllegalArgumentException("stack was null");

		this.stack = stack;
	}

	private static final String PLACEHOLDER_OPTION_LABEL = "option-label";

	private static final String PLACEHOLDER_OPTION_IS_SELECTED = "option-is-selected";

	private static final String PLACEHOLDER_OPTION_VALUE = "option-value";

	public void render(InvocationContext icb) throws Exception {

		if (getValueList() == null)
			throw new IllegalStateException("valueList had not been set");

		if (getLabels() == null)
			throw new IllegalStateException("labels had not been set");

		if (getPredicate() == null)
			throw new IllegalStateException("predicate had not been set");


		for (Iterator it = getValueList().iterator(); it.hasNext();) {

			String value = (String) it.next();

			stack.mark();

			stack.push(PLACEHOLDER_OPTION_VALUE, value);
			stack.push(PLACEHOLDER_OPTION_LABEL, (String) getLabels().get(value));
			stack.push(PLACEHOLDER_OPTION_IS_SELECTED, getPredicate().evaluate(value) ? "selected" : "");

			optionElement.render(icb);

			stack.popToMark();

		}

	}

}
