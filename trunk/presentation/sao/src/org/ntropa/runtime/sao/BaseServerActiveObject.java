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
 * BaseServerActiveObject.java
 *
 * Created on 16 November 2001, 16:08
 */

package org.ntropa.runtime.sao;

import java.util.Iterator;

/**
 * This class provides a base class which implements all the abstract methods required
 * by it's superclass and it can be used as a base class for other server active objects
 * which do not need to change the implementation of the implemented methods.
 *
 * @author  jdb
 * @version $Id: BaseServerActiveObject.java,v 1.6 2004/11/05 16:39:19 jdb Exp $
 */
public class BaseServerActiveObject extends AbstractServerActiveObject {

	/**
	 * This method should be overridden by a subclass to do application
	 * specific controlling logic.
	 *
	 * @throws Exception, otherwise it would have to throw every exception that every
	 * method of every object could throw.
	 *
	 * @param icb A <code>InvocationContext</code> supplied by the JSP.
	 */
	public void controlSelf(InvocationContext icb) throws Exception {
		/* No controlling logic exists for this base class */
	}

	/**
	 * This method can be overridden by a subclass to do application
	 * specific conditional invocation of each child's control method. Typically
	 * this base implementation is acceptable as it implements the early exit
	 * required when Controller.proceed () is false.
	 *
	 */
	public void controlChildren(InvocationContext icb) throws Exception {
		Iterator it = getChildren().iterator();
		while (it.hasNext()) {
			if (icb.getController().proceed()) {
				String childName = (String) it.next();
				//icb.getServletContext().log(getChild(childName).getClass() + ":" + getChild(childName).getName());
				getChild(childName).control(icb);
			} else
				break;
		}
	}

	/**
	 * Invoked inside the JSP to render content into the page buffer. All control
	 * logic has already been executed by the time this method is invoked. The method
	 * is responsible for rendering the view (HTML) from the model (session data) and
	 * nothing more. In particular no http redirection should be attempted during
	 * the rendering phase.
	 *
	 * @throws Exception, otherwise it would have to throw every exception that every
	 * method of every object could throw.
	 *
	 * @param icb A <code>InvocationContext</code> supplied by the JSP.
	 */
	public void render(InvocationContext icb) throws Exception {
		Iterator it = getChildren().iterator();
		while (it.hasNext()) {
			String childName = (String) it.next();
			getChild(childName).render(icb);
		}
	}

	/*
	 * Bean setter methods.
	 *
	 * A server active object can be configured for use (and reuse) by adding entries
	 * to the application parameter files stored in an application paramter files. The
	 * WPS treats files called 'application.properties' in directories called '_application'
	 * as application paramter files and will configure a SAO from the data found within such
	 * a file.
	 *
	 * Example.
	 *
	 * This is the file system
	 *
	 * /_application/application.properties
	 * /index.html
	 *
	 * In index.html ther is a section of marked up HTML:
	 *
	 * <!-- name = "news-feed" -->
	 * The news feed goes here
	 * <!-- name = "/news-feed" -->
	 *
	 * In application.properties there is server active configuration data:
	 *
	 * sao.news-feed.class-name = org.ntropa.runtime.sao.idp.UserInfoSAO
	 * sao.news-feed.prop.okPage = ok.html
	 *
	 * The WPS will use this to write code to create an object of type UserInfoSAO and
	 * invoke the following bean property setter on it:
	 *
	 *      object.setOkPage ( "ok.html" ) ;
	 *
	 * The responsibility of ensuring a method with a matching signature exists in the configurer,
	 * the WPS makes no attempt to introspect on the messaged object. This is a future feature.
	 */

	/*
	 * JavaBean property setter/getter methods defined.
	 */
	// no methods defined.

}
