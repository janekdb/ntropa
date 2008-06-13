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
 * SelectByPlaceholderReplacementIsEmpty.java
 *
 * Created on 13 May 2002, 15:57
 */

package org.ntropa.runtime.sao.util;

/**
 * This SAO chooses one of two elements based on the value of a placeholder replacement.
 * <p>
 * It will choose one of
 * <ul>
 * <li>empty
 * <li>not-empty
 * </ul>
 * <p>
 * Example: An empty should only be shown if the placeholder replacement value is not zero-length.
 * <p>
 * In example.html:
 * <pre>
 *
 * &lt;!-- name="cond" --&gt;
 * &lt;!-- element="not-empty" --&gt;&lt;h1&gt;$$my-placeholder-code$$&lt;/h1&gt;&lt;!-- element="/not-empty" --&gt;
 * &lt;!-- element="empty" --&gt;&lt;!-- element="/empty" --&gt;
 * &lt;!-- name="/cond" --&gt;
 * </pre>
 *
 * <p>
 * In application.properties
 * <p>
 * <pre>
 * sao.cond.class-name=org.ntropa.runtime.sao.util.SelectByPlaceholderReplacementIsEmpty
 * sao.cond.prop.placeholderCode=my-placeholder-code
 * </pre>
 * <p>
 * When the value for the placeholder code is zero-length the &lt;h1&gt; tags will not be rendered.
 * <p>
 * Note: The HTML optionally rendered does not need to include the placeholder code being tested.
 *
 * @author  jdb
 * @version $Id: SelectByPlaceholderReplacementIsEmpty.java,v 1.6 2004/10/08 15:08:12 jdb Exp $
 * @see RenderIfPlaceholderReplacementNotEmpty
 */
public class SelectByPlaceholderReplacementIsEmpty extends SelectByPlaceholderReplacementPropertySAO {
    
    
    private static final String IS_EMPTY_ELEMENT_NAME = "empty" ;
    private static final String IS_NOT_EMPTY_ELEMENT_NAME = "not-empty" ;
    
    /**
     * Return the name of an <code>Element</code> to render based on the value of
     * the placeholder replacement.
     * <p>
     * Subclasses should implement this method to provide specialised behaviour.
     *
     * @param rep The value of the placeholder to calculate the returned element name from
     * @return A <code>String</code> which is the name of an sub-element to render. By
     * convention the method should return &quot;default&quot; if no other return value
     * is appropriate.
     */
    public String getElementNameForPlaceholderReplacementValue (String rep) {
        
        return
        rep.length () == 0 ?
        IS_EMPTY_ELEMENT_NAME
        :
            IS_NOT_EMPTY_ELEMENT_NAME ;
    }
    
}
