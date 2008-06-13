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
 * Created on 25-Nov-2004
 *
 * ExceptionThrowerSAO
 */
package org.ntropa.runtime.sao.debug;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;

/**
 * Always throws an exception.
 * 
 * @author Janek Bogucki
 *
 * @version $Id: ExceptionThrowerSAO.java,v 1.2 2004/11/26 12:36:45 jdb Exp $
 */
public class ExceptionThrowerSAO extends BaseServerActiveObject {

	public void controlSelf(InvocationContext icb) throws Exception {
		/* See what chained exceptions look like */
		throw new RuntimeException(new Exception(new IllegalArgumentException()));
	}

}
