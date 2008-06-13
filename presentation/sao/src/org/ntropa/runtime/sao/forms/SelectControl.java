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
 * SelectControl.java
 *
 * Created on 31 March 2003, 21:31
 */

package org.ntropa.runtime.sao.forms;

import java.util.Iterator;
import java.util.Map;

import org.ntropa.runtime.sao.AbstractElement;
import org.ntropa.runtime.sao.InvocationContext;
import org.ntropa.runtime.sao.PlaceholderReplacementStack;
import org.ntropa.utility.HtmlUtils;

/**
 * This class encapsulates the logic for rendering a select control which can
 * use &lt;optGroup&gt;.
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
 * @version $Id: SelectControl.java,v 1.7 2006/03/01 15:03:58 jdb Exp $
 */
public class SelectControl extends SetChoice {

	private AbstractElement optionElement;

	private PlaceholderReplacementStack stack;

	/**
	 * Creates new SelectControl
	 */
	public SelectControl(AbstractElement optionElement, PlaceholderReplacementStack stack) {

		if (optionElement == null)
			throw new IllegalArgumentException("optionElement was null");

		this.optionElement = optionElement;

		if (stack == null)
			throw new IllegalArgumentException("stack was null");

		this.stack = stack;
	}

	private boolean isOptgroup = false;

	/**
	 * @param isOptgroup
	 *            set to true if the browser is capable of rendering the HTML
	 *            &lt;optGroup&gt; element. By default this property is assumed
	 *            to be false
	 */
	public void setIsOptGroup(boolean isOptgroup) {

		this.isOptgroup = isOptgroup;
	}

	String prompt = "- please select -";

	/**
	 * Set the prompt.
	 * <p>
	 * This is used as the label of the first option. The default is <br>
	 * <br> - please select -
	 * </p>
	 * <p>
	 * The argument should not be HTML encoded
	 * 
	 * @param prompt
	 *            The prompt to use
	 */
	public void setChoosePrompt(String prompt) {

		if (prompt == null)
			throw new IllegalArgumentException("prompt was null");

		this.prompt = prompt;

	}

	private String anythingLabel = "Anything";

	private String anythingValue = "any";

	/**
	 * Set the label and value for the option normally equivalent to not making
	 * a choice.
	 * 
	 * @param label
	 *            The string to use for the label of the 'void' choice after the
	 *            prompt. The default is "Anything".
	 * @param value
	 *            The string to use for the value of the 'void' choice after the
	 *            prompt. The default is "any".
	 */
	public void setAnythingLabelAndValue(String label, String value) {

		if (label == null)
			throw new IllegalArgumentException("label was null");

		if (value == null)
			throw new IllegalArgumentException("value was null");

		this.anythingLabel = label;

		this.anythingValue = value;
	}

	private boolean isRenderAnythingOption = true;

	/**
	 * Set if the label and value for the option normallly equivalent to not
	 * making a choice is rendered or not.
	 * <p>
	 * The default value for this property is <code>true</code>
	 * 
	 * @param isAnythingOption
	 *            if true render the option.
	 */
	public void setRenderAnythingOption(boolean render) {
		isRenderAnythingOption = render;
	}

	private Map breaks;

	/**
	 * Set a map of values of the value list to introduce a break just before.
	 * <p>
	 * The keys of the map should be values that are found in the list set in
	 * {@link #setValueList} and the values of the map are the labels to use for
	 * the breaks
	 * 
	 * @param breaks
	 *            A <code>Map</code> of potential breaks
	 */
	public void setBreaks(Map breaks) {

		if (breaks == null)
			throw new IllegalArgumentException("breaks was null");

		this.breaks = breaks;
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

		if (breaks == null)
			throw new IllegalStateException("breaks had not been set");

		if (isOptgroup) {

			/*
			 * <optgroup label="- please select -"> <option value="any">Anything
			 * </optgroup>
			 */

			icb.getJspWriter().write(HtmlUtils.optgroupOpen(prompt) + "\n");

			/*
			 * The 'any' option is normally the same as selecting none.
			 */
			if (isRenderAnythingOption) {
				stack.mark();

				stack.push(PLACEHOLDER_OPTION_VALUE, anythingValue);
				stack.push(PLACEHOLDER_OPTION_LABEL, anythingLabel);
				stack.push(PLACEHOLDER_OPTION_IS_SELECTED, "");

				optionElement.render(icb);

				stack.popToMark();
			}

			icb.getJspWriter().write(HtmlUtils.optgroupClose() + "\n");

		} else {

			/*
			 * <option value="">- please select where you want to study -"
			 * <option value="any">Anywhere <option value="">-
			 */
			stack.mark();

			stack.push(PLACEHOLDER_OPTION_VALUE, "");
			stack.push(PLACEHOLDER_OPTION_LABEL, prompt);
			stack.push(PLACEHOLDER_OPTION_IS_SELECTED, "");

			optionElement.render(icb);

			stack.popToMark();

			/*
			 * The 'any' option is normally the same as selecting none.
			 */
			if (isRenderAnythingOption) {
				stack.mark();

				stack.push(PLACEHOLDER_OPTION_VALUE, anythingValue);
				stack.push(PLACEHOLDER_OPTION_LABEL, anythingLabel);
				stack.push(PLACEHOLDER_OPTION_IS_SELECTED, "");

				optionElement.render(icb);

				stack.popToMark();
			}

			/* break */
			stack.mark();

			stack.push(PLACEHOLDER_OPTION_VALUE, "");
			stack.push(PLACEHOLDER_OPTION_LABEL, "-");
			stack.push(PLACEHOLDER_OPTION_IS_SELECTED, "");

			optionElement.render(icb);

			stack.popToMark();

		}

		boolean optgroupOpen = false;

		for (Iterator it = getValueList().iterator(); it.hasNext();) {

			String value = (String) it.next();

			if (breaks.containsKey(value)) {
				String breakLabel = (String) breaks.get(value);
				if (isOptgroup) {

					/*
					 * Write </optgroup> at end of previous group
					 */
					if (optgroupOpen)
						icb.getJspWriter().write(HtmlUtils.optgroupClose() + "\n");

					optgroupOpen = true;

					icb.getJspWriter().write(HtmlUtils.optgroupOpen(breakLabel) + "\n");
				} else {
					/* Extra emphasis for non-optgroup */
					stack.mark();

					stack.push(PLACEHOLDER_OPTION_VALUE, "");
					stack.push(PLACEHOLDER_OPTION_LABEL, "- " + breakLabel + " -");
					stack.push(PLACEHOLDER_OPTION_IS_SELECTED, "");

					optionElement.render(icb);

					stack.popToMark();

				}
			}

			stack.mark();

			stack.push(PLACEHOLDER_OPTION_VALUE, value);
			stack.push(PLACEHOLDER_OPTION_LABEL, (String) getLabels().get(value));
			stack.push(PLACEHOLDER_OPTION_IS_SELECTED, getPredicate().evaluate(value) ? "selected" : "");

			optionElement.render(icb);

			stack.popToMark();

		}

		if (isOptgroup && optgroupOpen)
			icb.getJspWriter().write(HtmlUtils.optgroupClose() + "\n");

	}
}
