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
 * RenderIfPlaceholderReplacementNotEmpty.java
 *
 * Created on 06 November 2002, 14:10
 */

package org.ntropa.runtime.sao.util;

/**
 * This SAO renders it's implicit element if the value of a placeholder replacement is longer
 * then zero.
 * <p>
 * Example: Some HTML should only be shown if the placeholder replacement value is not zero-length.
 * <p>
 * In example.html:
 * <pre>
 *
 * &lt;!-- name="cond" --&gt;
 * &lt;h1&gt;$$my-placeholder-code$$&lt;/h1&gt;
 * &lt;!-- name="/cond" --&gt;
 * </pre>
 *
 * <p>
 * In application.properties
 * <p>
 * <pre>
 * sao.cond.class-name=org.ntropa.runtime.sao.util.RenderIfPlaceholderReplacementNotEmpty
 * sao.cond.prop.placeholderCode=my-placeholder-code
 * </pre>
 * <p>
 * When the value for the placeholder code is zero-length the &lt;h1&gt; tags will not be rendered.
 * <p>
 * Note: The HTML optionally rendered does not need to include the placeholder code being tested.
 *
 * @author  jdb
 * @version $Id: RenderIfPlaceholderReplacementNotEmpty.java,v 1.2 2004/10/08 15:08:12 jdb Exp $
 * @see SelectByPlaceholderReplacementIsEmpty
 */
public class RenderIfPlaceholderReplacementNotEmpty extends SelectByPlaceholderReplacementPropertySAO {
    
    private static final String IMPLICIT_ELEMENT_NAME = "-implicit" ;
    
    /**
     * Return the name of an <code>Element</code> to render based on the value of
     * the placeholder replacement.
     * <p>
     *
     * @param rep The value of the placeholder to calculate the returned element name from
     * @return null if the placeholder replacement value is the zero-length string otherwise
     * return the name of the implicit element.
     */
    public String getElementNameForPlaceholderReplacementValue (String rep) {
        
        return rep.length () == 0 ? null : IMPLICIT_ELEMENT_NAME ;
    }
    
}