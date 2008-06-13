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
 * If the "select" property is set to "true" the body of the SAO will be
 * rendered, if the same property is set to "false" nothing will be renderered.
 * In all other cases an IllegalStateException will be thrown.
 * </p>
 * <p>
 * Although this can only be statically configured this SAO is useful because a
 * site can define the property in /_application/site-options.properties while
 * using a library template. The same library template can be used in other
 * sites but with a different visibility.
 * </p>
 */
public class SelectIfTrueSAO extends BaseServerActiveObject {

	private static final String ERROR_MESSAGE = "The property \"select\" must be set to \"true\" or \"false\"";

	private Boolean select = null;

	public void setSelect(String value) {

		if ("true".equals(value))
			select = Boolean.TRUE;
		else if ("false".equals(value))
			select = Boolean.FALSE;
		else
			throw new IllegalStateException(ERROR_MESSAGE);

	}

	@Override
	public void render(InvocationContext icb) throws Exception {
		if (select == null)
			throw new IllegalStateException(ERROR_MESSAGE);
		if (select.booleanValue())
			getChild("-implicit").render(icb);
	}

}
