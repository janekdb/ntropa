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

package org.ntropa.runtime.sao.util;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;


/**
 * <p>
 * This SAO renders it's elements in the order given by the order property.
 * </p>
 * <p>
 * The order property is comma separated list of elements. For example
 * <ul>
 * <li>head,body,footer-2 </li>
 * <li>body </li>
 * <li>comment,footer </li>
 * <li></li>
 * </ul>
 * </p>
 * It is an error to list an element that does not exist in the corresponding
 * HTML.
 */
public class SelectInListedOrderSAO extends BaseServerActiveObject {

	private String elements[] = null;

	public void setElements(String elementList) {
		String list = elementList.trim();
		// String.split creates one empty item of the empty string
		if (list.length() == 0) {
			elements = new String[0];
		} else
			elements = list.split(",");
		// System.out.println("size: " + elements.length + ": " +
		// Arrays.asList(elements));
	}

	@Override
	public void render(InvocationContext icb) throws Exception {

		if (elements == null)
			throw new IllegalStateException(
					"The 'elements' property must be set to either the empty string or a comma separated list of element names.");

		for (String element : elements)
			getChild(element).render(icb);
	}

}
