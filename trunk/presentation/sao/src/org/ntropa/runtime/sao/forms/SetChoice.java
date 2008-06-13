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

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Predicate;


/**
 * This class contains common support for classes that render a set of choices from
 * a set of values.
 * 
 * The common elements are
 * <ul>
 * <li>List of values to choice from
 * <li>List of labels for the values
 * <li>A way to decide if a value is currently choosen or not.
 * </ul>
 * 
 * @author jdb
 *
 */
abstract public class SetChoice {
	


	private List valueList;

	/**
	 * @param valueList The list of values to use to build the list of option elements
	 * from. Each item in valueList becomes a 'value' attribute in an option element. For
	 * example this list produces this list of options:
	 * <p>
	 * { "A", "B", "C" }
	 * </p>
	 * <p>
	 * &lt;option value="A" &gt;<br>
	 * &lt;option value="B" &gt;<br>
	 * &lt;option value="C" &gt;<br>
	 * </p>
	 */
	public void setValueList(List valueList) {

		if (valueList == null)
			throw new IllegalArgumentException("valueList was null");

		this.valueList = valueList;
	}
	
	protected List getValueList(){
		return valueList;
	}

	private Map labels;
	
	/**
	 * Set the map of value to labels
	 * @param labels A <code>Map</code> of values to labels. For example
	 * <p>
	 * <table>
	 * <tr><td>key</td><td>value</td></tr>
	 * <tr><td>x-e-uk-en</td><td>England</td></tr>
	 * <tr><td>x-e-uk-st</td><td>Scotland</td></tr>
	 * <tr><td>x-e-uk-wl</td><td>Wales</td></tr>
	 * <tr><td>x-e-uk-ni</td><td>Northen Ireland</td></tr>
	 *</table>
	 * </p>
	 * This is used to get the label for each item of the list of values
	 */
	public void setLabels(Map labels) {

		if (labels == null)
			throw new IllegalArgumentException("labels was null");

		this.labels = labels;
	}

	protected Map getLabels(){
		return labels;
	}
	
	private Predicate predicate;
	
	/**
	 * Set the object responsible for determining if an option should be rendered
	 * as selected or not.
	 * <p>
	 * The object will have it's evaluate method invoked with the argument equal to
	 * the current value. The method should return true if the option should be
	 * selected.
	 * @param predicate An object which implements <code>Predicate</code>
	 */
	public void setPredicate(Predicate predicate) {

		if (predicate == null)
			throw new IllegalArgumentException("predicate was null");

		this.predicate = predicate;
	}
	
	protected Predicate getPredicate(){
		return predicate;
	}
}
