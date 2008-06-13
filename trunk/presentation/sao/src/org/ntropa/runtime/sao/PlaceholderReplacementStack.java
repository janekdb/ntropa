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
 * PlaceholderReplacementStack.java
 *
 * Created on 24 March 2003, 15:22
 */

package org.ntropa.runtime.sao;

import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;

import org.ntropa.utility.HtmlUtils;
import org.ntropa.utility.StringUtilities;


/**
 * This class maintains a stack of placeholder codes and replacement values.
 * <p>
 * An object of this class is useful within the render method of server active
 * objects. It is intended to be used as an alternative to using static code in
 * the {@link AbstractServerActiveObject#getPlaceholderReplacement} method. A
 * typical use goes like this:
 * </p>
 * <p>
 * 
 * <pre>
 * <code>
 * 
 *   void render ( InvocationContext icb ) {
 * 
 *     getPlaceholderReplacementStack ().push ( &quot;label&quot;, &quot;HEADER replacement value for label&quot; ) ;
 * 
 *     getChild ( &quot;data-header&quot; ).render ( icb ) ) ;
 *     for ( int i = 1 ; i&lt;=10 ; i++ )
 *       renderRow ( icb, i ) ;
 *     getChild ( &quot;data-footer&quot; ).render ( icb ) ) ;
 * 
 *     getPlaceholderReplacementStack ().pop () ;
 * 
 *   }
 * 
 *   void renderRow ( InvocationContext icb, int rowIDX ) {
 * 
 *     getPlaceholderReplacementStack ().mark () ;
 * 
 *     getPlaceholderReplacementStack ().push ( &quot;label&quot;, &quot;ROW replacement value for label&quot; ) ;
 *     getPlaceholderReplacementStack ().push ( &quot;datum&quot;, &quot;datum: &quot; + rowIDX ) ;
 * 
 *     getChild ( &quot;data-row&quot; ).render ( icb ) ;
 * 
 *     getPlaceholderReplacementStack ().popToMark () ;
 * 
 *   }
 * 
 * </code>
 * </pre>
 * 
 * </p>
 * <p>
 * In this example the replacement value for placeholder code "label" is set in
 * two different places and due to the stack nature of the collection of
 * placeholder replacements the value set in the method <code>renderRow</code>
 * is used when the child element "data-row" is rendered, while the the initial
 * value is used for the rendition of elements "data-header" and "data-footer".
 * </p>
 * <p>
 * By using the instance of <code>PlaceholderReplacementStack</code> available
 * in each <code>AbstractServerActiveObject</code> the coding of replacement
 * values for placeholder codes becomes easier to manage because the code that
 * sets up the replacement valuse is local to the code that depends on the
 * replacement values.
 * </p>
 * <p>
 * The stack is used on a most-recently-pushed, first-consulted basis. So in
 * this example the value for placeholder code "p" will be "2":
 * </p>
 * <p>
 * 
 * <pre>
 * <code>
 * 
 * getPlaceholderReplacementStack().push(&quot;p&quot;, &quot;1&quot;);
 * getPlaceholderReplacementStack().push(&quot;p&quot;, &quot;2&quot;);
 * 
 * </code>
 * </pre>
 * 
 * </p>
 * <p>
 * The replacement value will be converted to HTML with
 * {@link HtmlUtils#convertToHtml} before being returned. If this is not
 * required the overload of the method can be used
 * {@link #push(String,String,boolean)}
 * </p>
 * 
 * @author Janek Bogucki
 * @version $Id: PlaceholderReplacementStack.java,v 1.7 2003/05/04 23:24:49 jdb
 *          Exp $
 */
public class PlaceholderReplacementStack {

	List replacements = new LinkedList();

	/**
	 * Push a placeholder code and replacement value onto the stack.
	 * 
	 * @param name
	 *            The placeholder code to provider a replacement value for
	 * @param replacementValue
	 *            The replacement value
	 */
	public void push(String name, String replacementValue) {

		push(name, replacementValue, true);
	}

	/**
	 * Push a placeholder code and replacement value onto the stack with
	 * optional conversion to HTML.
	 * 
	 * @param name
	 *            The placeholder code to provider a replacement value for
	 * @param replacementValue
	 *            The replacement value
	 * @param convertToHtml
	 *            if true <code>replacementValue</code> is converted to HTML
	 *            otherwise the value is used as is.
	 */
	public void push(String name, String replacementValue, boolean convertToHtml) {

		StringUtilities.validateNonZeroLength(name);
		if (replacementValue == null)
			throw new IllegalArgumentException("replacementValue was null for '" + name + "'");

		replacements.add(new String[] { name,
				convertToHtml ? HtmlUtils.convertToHtml(replacementValue) : replacementValue });
	}

	/**
	 * Pop the most recently pushed placeholder code and replacement value from
	 * the stack.
	 * <p>
	 * After popping a placeholder code and replacement value from the stack any
	 * previously pushed pair becomes effective
	 * </p>
	 * 
	 * @throws EmptyStackException
	 *             if this stack is empty.
	 */
	public void pop() throws EmptyStackException {

		if (replacements.size() == 0)
			throw new EmptyStackException();

		replacements.remove(replacements.size() - 1);

	}

	/**
	 * Invoke {@link #pop()} multiply.
	 * 
	 * @param count
	 *            The number of times to pop from the stack.
	 * @throws EmptyStackException
	 *             if a pop is attempted on an empty stack
	 */
	public void pop(int count) throws EmptyStackException {

		for (int i = count; i > 0; i--)
			pop();

	}

	private List marks = null;

	/**
	 * Mark the current stack size
	 * <p>
	 * Use this is combination with {@link #popToMark}
	 * </p>
	 */
	public void mark() {

		if (marks == null)
			marks = new LinkedList();

		marks.add(new Integer(replacements.size()));

	}

	/**
	 * Remove items from the stack until the stack is the size it was when
	 * {@link #mark} was last invoked.
	 * 
	 * @throws IllegalStateException
	 *             if there was no corresponding invocation of {@link #mark}
	 */
	public void popToMark() {

		if ((marks == null) || (marks.size() == 0))
			throw new IllegalStateException("Unbalanced invocation of popToMark");

		int previousStackSize = ((Integer) marks.remove(marks.size() - 1)).intValue();

		pop(replacements.size() - previousStackSize);
	}

	public String getPlaceholderReplacement(String name) {

		for (int i = replacements.size() - 1; i >= 0; i--) {
			String[] pair = (String[]) replacements.get(i);
			if (pair[0].equals(name))
				return pair[1];
		}

		return null;
	}

	public void clear() {
		replacements.clear();
	}
}
