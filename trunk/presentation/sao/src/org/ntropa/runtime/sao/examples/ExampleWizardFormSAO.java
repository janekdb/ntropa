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
 * ExampleWizardFormSAO.java
 *
 * Created on 03 July 2002, 16:07
 */

package org.ntropa.runtime.sao.examples;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ntropa.runtime.sao.InvocationContext;
import org.ntropa.runtime.sao.util.WizardFormHandlerSAO;


/**
 *
 * @author  jdb
 * @version $Id: ExampleWizardFormSAO.java,v 1.1 2002/07/03 17:30:25 jdb Exp $
 */
public class ExampleWizardFormSAO extends WizardFormHandlerSAO {
    
    
    private static List sectionList ;
    
    static {
        
        sectionList = Collections.unmodifiableList (
        Arrays.asList ( new String [] { "section-1", "section-2", "section-3" } )
        ) ;
    }
    
    /**
     * Subclasses should override this method to return a list of sections
     * the wizard is to enforce.
     * <p>
     * For example if your multi-section form has three sections
     * <ul>
     * <li>subjects
     * <li>locations
     * <li>keywords
     * </ul>
     * then the overriding method should return a reference to a list with
     * these entries
     * <ul>
     * <li>&quot;subjects&quot;
     * <li>&quot;locations&quot;
     * <li>&quot;keywords&quot;
     * </ul>
     * The subclass may return a reference to an unmodifiable static member.
     */
    protected List getSectionList () {
        return sectionList ;
    }
    
    /**
     * Subclasses must provide an implementation of this method which returns true if the submitted
     * data is acceptable for the section.
     * <p>
     * This method is a query method in the sense of Betrand Meyer's description of query methods.
     * That is the method does not change the state of the model and can be invoked repeatedly and will
     * always return the same result for the same model state.
     * <p>
     * For example, suppose the field 'email' must be present in a form submission. This implementation
     * will test for that
     * <p>
     * <pre>
     * <code>
     * postedDataIsAcceptable ( InvocationContext icb, String section ) {
     * HttpServletRequest request = icb.getHttpServletRequest () ;
     * String email = request.getParameter ( "email" ) ;
     * return email != null && ! email.equals ( "" ) ;
     * }
     * </code>
     * </pre>
     * @param icb The <code>InvocationContext</code> for this request
     * @param section The value of the HTML form inout field 'section'
     * @return True is the data is acceptable.
     */
    protected boolean postedDataIsAcceptable (InvocationContext icb, String section) throws Exception {
        log ( "postedDataIsAcceptable: section = " + section ) ;
        return true ;
    }
    
    /**
     * Subclasses must provide an implementation of this method which accepts the submitted
     * data for this section.
     * <p>
     * This method is typically used to store or act on the submitted data.
     * <p>
     * postedDataIsAcceptable has already returned true before this
     * method is invoked. Sometimes temporary data is generated during the validation of data and this data is
     * useful in the acceptance of the data (for example transforming an input into canonical form). If this is
     * the case then one of two strategies (or a combination) can be adopted
     * <ul>
     * <li>Recalculate the data in this method
     * <li>Cache the data in the ThreadLocalCache in postedDataIsAcceptable and access that
     * data in this method.
     * </ul>
     * This split will generally lead to better code than a single 'processPostedDataAndReturnTrueIfDataOkay'
     * method. This factoring can lead to simpler code and more maintainable code.
     *
     * @param icb The <code>InvocationContext</code> for this request
     * @param section The value of the HTML form inout field 'section'
     */
    protected void acceptPostedData (InvocationContext icb, String section) throws Exception {
        log ( "acceptPostedData: section = " + section ) ;
    }
    
}
