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
 * DefaultingSelectByRequestAttributeSAOTest.java
 *
 * Created on 12 September 2002, 14:19
 */

package tests.org.ntropa.runtime.sao.util;

import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.runtime.sao.AbstractElement;
import org.ntropa.runtime.sao.InvocationContext;
import org.ntropa.runtime.sao.StandardElement;
import org.ntropa.runtime.sao.StandardInvocationContext;
import org.ntropa.runtime.sao.util.DefaultingSelectByRequestAttributeSAO;

import com.mockobjects.servlet.MockHttpServletRequest;

/**
 *
 * @author  jdb
 * @version $Id: DefaultingSelectByRequestAttributeSAOTest.java,v 1.1 2002/09/12 15:30:55 jdb Exp $
 */
public class DefaultingSelectByRequestAttributeSAOTest extends TestCase {
    
    /** Creates new DefaultingSelectByRequestAttributeSAOTest */
    public DefaultingSelectByRequestAttributeSAOTest ( String testName ) {
        super(testName);
    }
    
    
    /* Comments copied from junit.framework.TestSuite. */
    
    /**
     * A <code>TestSuite</code> is a <code>Composite</code> of Tests.
     * It runs a collection of test cases.
     *
     * This constructor creates a suite with all the methods
     * starting with "test" that take no arguments.
     */
    public static Test suite () {
        
        TestSuite suite = new TestSuite ( DefaultingSelectByRequestAttributeSAOTest.class );
        return suite;
    }
    
    public void testElementSelectionBehaviour () throws Exception {
        
        DefaultingSelectByRequestAttributeSAO controller = new DefaultingSelectByRequestAttributeSAO () ;
        
        controller.setAttributeName ( "level" ) ;
        
        List record = new LinkedList () ;
        
        AbstractElement elTop = new MyElement ( record ) ;
        elTop.setName ( "top" ) ;
        controller.addChild ( elTop ) ;
        
        AbstractElement elMiddle = new MyElement ( record ) ;
        elMiddle.setName ( "middle" ) ;
        controller.addChild ( elMiddle) ;
        
        InvocationContext icb = new StandardInvocationContext () ;
        icb.enableRenderPhase () ;
        
        MyMockHttpServletRequest request = new MyMockHttpServletRequest () ;
        
        //out.setExpectedData ( expected ) ;
        
        icb.setHttpServletRequest ( request ) ;
        
        /* Kick it off */
        request.setValue ( "top" ) ;
        controller.render ( icb ) ;
        
        List expected = new LinkedList () ;
        expected.add ( "top" ) ;
        assertEquals ( "top was rendered",  expected, record ) ;
        
        request.setValue ( "middle" ) ;
        controller.render ( icb ) ;
        expected.add ( "middle" ) ;
        assertEquals ( "middle was rendered",  expected, record ) ;
        
        request.setValue ( "no-such-element" ) ;
        controller.render ( icb ) ;
        assertEquals ( "When request attribute had no corresponding element nothing was rendered",  expected, record ) ;
        
        AbstractElement elDefault = new MyElement ( record ) ;
        elDefault.setName ( "default" ) ;
        controller.addChild ( elDefault ) ;
        
        request.setValue ( "no-such-element" ) ;
        controller.render ( icb ) ;
        expected.add ( "default" ) ;
        assertEquals ( "When request attribute had no corresponding element 'default' was rendered",  expected, record ) ;
        
    }
    
    
    private class MyElement extends StandardElement {
        
        List list ;
        
        MyElement ( List list ) {
            super () ;
            this.list = list ;
        }
        
        public void render ( InvocationContext icb ) {
            list.add ( getName () ) ;
        }
        
    }
    
    private class MyMockHttpServletRequest extends MockHttpServletRequest  {
        
        String value ;
        
        void setValue ( String value ) {
            this.value = value ;
        }
        
        public Object getAttribute ( String attributeName ) {
            return value ;
        }
    }
    
}
