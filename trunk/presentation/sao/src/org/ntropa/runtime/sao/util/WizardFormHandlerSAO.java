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
 * WizardFormHandlerSAO.java
 *
 * Created on 02 July 2002, 15:43
 */

package org.ntropa.runtime.sao.util;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.InvocationContext;
import org.ntropa.utility.StringUtilities;


/**
 * 
 * @author jdb
 * @version $Id: WizardFormHandlerSAO.java,v 1.10 2005/08/05 12:32:31 jdb Exp $
 */
abstract public class WizardFormHandlerSAO extends BaseServerActiveObject {

	private final static String PARAM_SECTION = "section";

	/*
	 * The name of the ThreadLocal value used to indicate the section of the
	 * form requested with the GET method.
	 */
	private static final String TL_SECTION_KEY = "section";

	/**
	 * Subclasses should override this method to return a list of sections the
	 * wizard is to enforce.
	 * <p>
	 * For example if your multi-section form has three sections
	 * <ul>
	 * <li>subjects
	 * <li>locations
	 * <li>keywords
	 * </ul>
	 * then the overriding method should return a reference to a list with these
	 * entries
	 * <ul>
	 * <li>&quot;subjects&quot;
	 * <li>&quot;locations&quot;
	 * <li>&quot;keywords&quot;
	 * </ul>
	 * The subclass may return a reference to an unmodifiable static member.
	 */
	abstract protected List getSectionList();

	/**
	 * The URL to redirect to at the end
	 */
	private String targetPage;

	/**
	 * Property setter for the target page to use in the wizard.
	 * <p>
	 * Application developers may set this property in the usual way by adding a
	 * property to application.properties similar to this
	 * <p>
	 * <code>sao.my-wizard.prop.targetPage=/results/results.html</code>
	 * <p>
	 * (or with arguments
	 * <p>
	 * <code>sao.my-wizard.prop.targetPage=/results/results.html>page=1</code>)
	 * <p>
	 * This results in a wizard configured to use the page /results/results.html
	 * as the location for the final redirect once all stages of the wizard have
	 * been completed
	 * 
	 * @param targetPage
	 *            The location to redirect to at the end of the wizard. The
	 *            value must start with a /.
	 */
	public void setTargetPage(String targetPage) {

		StringUtilities.validateNonZeroLength(targetPage, "targetPage");

		if (!targetPage.startsWith("/"))
			throw new IllegalArgumentException("targetPage did not begin with /");

		this.targetPage = targetPage;
	}

	protected String getTargetPage() {
		if (targetPage == null)
			throw new IllegalStateException("targetPage was not set");
		return targetPage;
	}

	private String targetPageQueryPart;

	/**
	 * Set the query part of the target URL or null if there should not be a
	 * query part.
	 * <p>
	 * 
	 * @param The
	 *            optional query part of the URL
	 */
	public void setTargetPageQueryPart(String queryPart) {
		targetPageQueryPart = queryPart;
	}

	/**
	 * Returns the query part of the target URL or null if there should not be a
	 * query part.
	 * <p>
	 * If the query part is non null it will be appended to the target page like
	 * this
	 * <p>
	 * <code> getTargetPage () + ? + getTargetPageQueryPart ()</code>
	 * <p>
	 * Subclasses should overide this method if the query part needs to be
	 * dynamically calculated.
	 * 
	 * @return The query part of the final redirection.
	 */
	public String getTargetPageQueryPart() {
		return targetPageQueryPart;
	}

	public void controlSelf(InvocationContext icb) throws Exception {

		/*
		 * Store a reference to the InvocationContext so the application log can
		 * be accessed in AbstractServerActiveObject.
		 * 
		 * This method does not invoke any child elements which might make a
		 * callback so it not neccessary to set the invocation context for any
		 * other reason than logging.
		 * 
		 * However storing the reference now means it is available with the
		 * render method which is invoked later.
		 */
		setInvocationContext(icb);

		// ServletContext application= icb.getServletContext();
		HttpServletRequest request = icb.getHttpServletRequest();
		// HttpServletResponse response = icb.getHttpServletResponse ();
		HttpSession session = request.getSession();

		String self = request.getRequestURI();
		/*
		 * Get the section and validate it.
		 * 
		 * This is applied for both POST and GET. Technically this allows the
		 * HTML form to omit the hidden 'section' field on the first form but
		 * this is not reccommended.
		 * 
		 * In distinction, missing out the 'section' arg on links is desirable
		 * because it allows the use of simpler href values.
		 */
		String section = request.getParameter(PARAM_SECTION);

		/* Allow for initial GET of first view of search form */
		if (section == null || section.equals(""))
			section = getFirstSection();

		boolean sectionIsValid = getSectionList().contains(section);

		/* redirect to first view */
		if (!sectionIsValid) {
			redirectToLocation(icb, self);
			return;
		}

		boolean redirectToFirst = !sectionAccessOrderIsOkay(section, getSectionSet(session));
		if (redirectToFirst) {
			redirectToLocation(icb, self + "?" + PARAM_SECTION + "=" + getFirstSection());
			return;
		}

		/*
		 * When a form is POSTed these actions take place
		 * 
		 * 1. The submission is tested for validity. 2. If the submission was
		 * invalid the browser is redirected to the same page 3. If the
		 * submission was valid the section is added to the set of sections
		 * completed and the browser is redirected to the next section or target
		 * page if the section is the last section.
		 * 
		 * On POST record this page as submitted. GET does not change this to
		 * prevent manual modification of the URL in the users browser from
		 * setting this value.
		 */
		// System.out.println("WizardFormHandlerSAO: controlSelf: 1:
		// request.getMethod ()" + request.getMethod () );
		if (request.getMethod().equals("POST")) {

			// System.out.println( "WizardFormHandlerSAO: POST: bug-id: " +
			// request.getParameter ( "bug-id" ) );
			/*
			 * This value is used when rendering in response to a GET method
			 * request. By nulling the cached section value we ensure the render
			 * phase will fail with a NPE. This is correct because the render
			 * phase should not be entered as the POST must result in a
			 * redirection.
			 */
			setThreadLocalValue(TL_SECTION_KEY, null);

			boolean acceptable = postedDataIsAcceptable(icb, section);

			if (!acceptable) {
				/*
				 * Extend me: When we have a form that validates and needs to
				 * render differently when there was an erroneous submission we
				 * may need to add some state info the URL here. That is a
				 * 'maybe' as there might be a better alternative which will
				 * only be apparent when the work is undertaken.
				 */
				redirectToLocation(icb, self + "?" + PARAM_SECTION + "=" + section);
				return;
			}

			acceptPostedData(icb, section);

			getSectionSet(session).add(section);

			String location = null;
			if (isLastSection(section)) {
				location = getTargetPage();
				String optionalQueryPart = getTargetPageQueryPart();
				if (optionalQueryPart != null)
					location += "?" + optionalQueryPart;
			} else {
				location = self + "?" + PARAM_SECTION + "=" + getNextSection(section);
			}

			// System.out.println( "WizardFormHandlerSAO: controlSelf: POST
			// redirect: bug-id: " + request.getParameter ( "bug-id" ) + ",
			// location: " + location);

			redirectToLocation(icb, location);
			return;
		}
		// System.out.println("WizardFormHandlerSAO: controlSelf: 2:
		// request.getMethod ()" + request.getMethod () );

		/*
		 * If we get here it means there was no redirection neccessary and we
		 * need to set up the context for the rendering phase. For this class
		 * the requirements are simple, we need to remember the section to
		 * render.
		 * 
		 * Thread reuse note: Eventually the thread executing this object will
		 * be reused and the value set here will still be present. This could
		 * lead to a bug if a developer assumes the cache will be empty each
		 * time the thread executes the object.
		 * 
		 * This method is not affected by this possible bug because the code
		 * that depends on the value of 'section' only ever executes after the
		 * value is set here.
		 * 
		 * (Improvement notes: The data getting stored in the thread local cache
		 * should have the same lifespan as the request object but not the same
		 * scope, as more than one object in the sao tree could use the same key
		 * for a different value. We could empty all thread local caches at the
		 * end of the page serve by invoking a 'clean-up' method on each server
		 * active object. )
		 */
		if (request.getMethod().equals("GET")) {
			setThreadLocalValue(TL_SECTION_KEY, section);
		}
	}

	private void redirectToLocation(InvocationContext icb, String location) throws IOException {
		icb.getController().sendRedirect(getNonNullOptionalRedirectionUriPrefix(icb) + location);
	}

	private String getNonNullOptionalRedirectionUriPrefix(InvocationContext icb) {
		String optionalPrefix = getOptionalRedirectionUriPrefix(icb);
		if (optionalPrefix == null)
			return "";

		if (!optionalPrefix.startsWith("/"))
			throw new RuntimeException("getOptionalRedirectionUriPrefix() return value did not start with '/': '"
					+ optionalPrefix + "'");
		return optionalPrefix;
	}

	/**
	 * Subclasses should override this if they want to provide a path component
	 * (that should start with a /) that will be prefixed to all URLs that are
	 * used to redirect the browser with.
	 * 
	 * @return null if no prefix or a non-null prefix, e.g. "/distance"
	 */
	protected String getOptionalRedirectionUriPrefix(InvocationContext icb) {
		return null;
	}

	/**
	 * Invoked inside the JSP to render content into the page buffer. All
	 * control logic has already been executed by the time this method is
	 * invoked. The method is responsible for rendering the view (HTML) from the
	 * model (session data) and nothing more. In particular no http redirection
	 * should be attempted during the rendering phase.
	 * <p>
	 * This method is should be overridden by a SAO with special set up / tear
	 * down requirements before and after the rendering of the child element.
	 * 
	 * @throws Exception,
	 *             otherwise it would have to throw every exception that every
	 *             method of every object could throw.
	 * 
	 * @param icb
	 *            A <code>InvocationContext</code> supplied by the JSP.
	 */
	public void render(InvocationContext icb) throws Exception {

		/*
		 * Render the Element with the same anme as the current section.
		 * 
		 * (A runtime exception will be thrown if the child is missing.)
		 */
		getChild(getCurrentSection()).render(icb);

		// This is handled at the framework level
		// clearThreadLocalCache () ;
	}

	/**
	 * Subclasses must provide an implementation of this method which returns
	 * true if the submitted data is acceptable for the section.
	 * <p>
	 * This method is a query method in the sense of Betrand Meyer's description
	 * of query methods. That is the method does not change the state of the
	 * model and can be invoked repeatedly and will always return the same
	 * result for the same model state.
	 * <p>
	 * For example, suppose the field 'email' must be present in a form
	 * submission. This implementation will test for that
	 * <p>
	 * 
	 * <pre>
	 * <code>
	 * postedDataIsAcceptable(InvocationContext icb, String section) {
	 * 	HttpServletRequest request = icb.getHttpServletRequest();
	 * 	String email = request.getParameter(&quot;email&quot;);
	 * 	return email != null &amp;&amp; !email.equals(&quot;&quot;);
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param icb
	 *            The <code>InvocationContext</code> for this request
	 * @param section
	 *            The value of the HTML form input field 'section'
	 * @return True is the data is acceptable.
	 */
	abstract protected boolean postedDataIsAcceptable(InvocationContext icb, String section) throws Exception;

	/**
	 * Subclasses must provide an implementation of this method which accepts
	 * the submitted data for this section.
	 * <p>
	 * This method is typically used to store or act on the submitted data.
	 * <p>
	 * postedDataIsAcceptable has already returned true before this method is
	 * invoked. Sometimes temporary data is generated during the validation of
	 * data and this data is useful in the acceptance of the data (for example
	 * transforming an input into canonical form). If this is the case then one
	 * of two strategies (or a combination) can be adopted
	 * <ul>
	 * <li>Recalculate the data in this method
	 * <li>Cache the data in the ThreadLocalCache in postedDataIsAcceptable and
	 * access that data in this method.
	 * </ul>
	 * This split will generally lead to better code than a single
	 * 'processPostedDataAndReturnTrueIfDataOkay' method. This factoring can
	 * lead to simpler code and more maintainable code.
	 * 
	 * @param icb
	 *            The <code>InvocationContext</code> for this request
	 * @param section
	 *            The value of the HTML form input field 'section'
	 */
	abstract protected void acceptPostedData(InvocationContext icb, String section) throws Exception;

	/**
	 * Return the section last requested with the GET method.
	 * <p>
	 * Typically this will be used by a subclass to determine which child
	 * elements to render.
	 */
	protected String getCurrentSection() {
		String section = (String) getThreadLocalValue(TL_SECTION_KEY);
		if (null == section)
			throw new IllegalStateException("Can not be invoked in POST");
		return section;
	}

	/**
	 * Return the logical starting section for the wizard
	 */
	protected String getFirstSection() {
		return (String) getSectionList().get(0);
	}

	/**
	 * @return The next logical section
	 */
	protected String getNextSection(String section) {
		int index = getSectionList().indexOf(section);
		if (index == -1 || (index == getSectionList().size() - 1))
			throw new IllegalStateException("Unknown section: " + section);
		return (String) getSectionList().get(index + 1);
	}

	/**
	 * @param section
	 *            The section to test
	 * @return True if the section is the last in the list of sections
	 */
	protected boolean isLastSection(String section) {
		return getSectionList().indexOf(section) == getSectionList().size() - 1;
	}

	/**
	 * Later pages can only be served if prior pages have been successfully
	 * submitted. If this contract is broken we redirect to the first page.
	 * <p>
	 * To do the check we dtermine the position of the page in the list of pages
	 * and ensure that each prior page is in the set of previously visted pages.
	 * 
	 * @param page
	 *            A <code>String</code> representing the page being requested
	 *            via GET.
	 * 
	 * @param pageSet
	 *            A <code>Set</code> of pages successfully submitted via POST.
	 */
	public boolean sectionAccessOrderIsOkay(String section, Set sectionSet) {

		int index = getSectionList().indexOf(section);
		if (index == -1)
			throw new IllegalArgumentException("Unknown section: " + section);

		/*
		 * All the list elements up to and excluding the element at this index
		 * must have been visited. (The loop body is not executed when index ==
		 * 0.)
		 */
		for (int i = 0; i < index; i++) {
			String form = (String) getSectionList().get(i);
			// System.out.println("i, form: " + i + ", " + form );
			if (!sectionSet.contains(form))
				return false;
		}

		return true;
	}

	/**
	 * Encapsulate the details of acquiring a valid object for the given
	 * session.
	 * <p>
	 * If the Set exists return it otherwise construct it and return it.
	 * 
	 * @return A <code>Set</code> representing the sections of the wizard
	 *         successfully completed.
	 */
	private Set getSectionSet(HttpSession session) {

		/* Get the search data object or create it */
		Set sectionSet = null;
		synchronized (session) {

			sectionSet = (Set) session.getAttribute(getDataKey());
			if (sectionSet == null) {
				sectionSet = new TreeSet();
				session.setAttribute(getDataKey(), sectionSet);
			}
		}
		return sectionSet;
	}

}
