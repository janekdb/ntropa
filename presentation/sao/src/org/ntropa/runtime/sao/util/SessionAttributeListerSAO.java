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

import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;
import org.ntropa.runtime.sao.PlaceholderReplacementStack;
import org.ntropa.utility.HtmlUtils;


/**
 * Supplied the attributes of the session as table rows.
 * 
 * @author jdb
 * 
 */
public class SessionAttributeListerSAO extends BaseServerActiveObject {

	public void controlSelf(InvocationContext icb) throws Exception {

		HttpSession session = icb.getHttpServletRequest().getSession();
		StringBuffer sb = new StringBuffer();
		for (Enumeration e = session.getAttributeNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			sb.append("<tr><td>" + HtmlUtils.convertToHtml(name) + "</td><td>"
					+ HtmlUtils.convertToHtml(session.getAttribute(name).toString()) + "</td></tr>\n");
		}

		PlaceholderReplacementStack stk = getPlaceholderReplacementStack();
		stk.push("attributes", sb.toString(), false);
	}
}
