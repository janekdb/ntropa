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
 * AbstactContainerTest.java
 *
 * Created on 26 November 2001, 17:28
 */

package tests.org.ntropa.runtime.sao;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ntropa.runtime.sao.AbstractContainer;
import org.ntropa.runtime.sao.AbstractElement;
import org.ntropa.runtime.sao.AbstractServerActiveObject;
import org.ntropa.runtime.sao.BaseServerActiveObject;
import org.ntropa.runtime.sao.StandardElement;
import org.ntropa.runtime.sao.StandardFragment;


/**
 *
 * @author  jdb
 * @version $Id: AbstactContainerTest.java,v 1.5 2003/03/24 17:00:22 jdb Exp $
 */
public class AbstactContainerTest extends TestCase {
    
    /** Creates new AbstactContainerTest */
    public AbstactContainerTest ( String testName ) {
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
        
        TestSuite suite = new TestSuite ( AbstactContainerTest.class );
        return suite;
    }
    
    /**
     * Test a parent container sets the placeholders of a child
     */
    public void testPlaceholderInheiratance () {
        
        /*
         * Test a BaseServerActiveObject adds its placeholders to a child
         * when the child is a StandardElement
         */
        AbstractServerActiveObject sao = new BaseServerActiveObject () ;
        sao.setPlaceholder ( "p1", "v1" ) ;
        
        AbstractElement el = new StandardElement () ;
        el.setName ( "element-1" ) ;
        
        sao.addChild ( el ) ;
        
        Properties expectedPlaceholders = new Properties () ;
        expectedPlaceholders.setProperty ( "p1", "v1" ) ;
        
        assertEquals (
        "The inherited placeholders were wrong",
        expectedPlaceholders,
        el.getPlaceholders ()
        ) ;
        
        
        /*
         * Test a StandardElement adds its placeholders to a child
         * when the child is a BaseServerActiveObject
         */
        el = new StandardElement () ;
        el.setName ( "element-2" ) ;
        el.setPlaceholder ( "p2", "v2" ) ;
        
        sao = new BaseServerActiveObject () ;
        
        el.addChild ( sao ) ;
        
        /* Throw a frgament in the mix */
        el.addChild ( new StandardFragment () ) ;
        
        expectedPlaceholders = new Properties () ;
        expectedPlaceholders.setProperty ( "p2", "v2" ) ;
        
        assertEquals ( "The inherited placeholders were wrong", expectedPlaceholders, sao.getPlaceholders () ) ;
        
        /* --- Deep test --- */
        AbstractServerActiveObject topSao = new BaseServerActiveObject () ;
        /* This is what we expect to be inherited */
        topSao.setPlaceholder ( "date", "2001-11-26" ) ;
        expectedPlaceholders = new Properties () ;
        expectedPlaceholders.setProperty ( "date", "2001-11-26" ) ;
        AbstractContainer lastContainer = topSao ;
        
        //System.out.println("Placeholder for topSao: " + topSao.getPlaceholders () );
        for ( int i = 1 ; i <= 100 ; i++ ) {
            //System.out.println ("[AbstactContainerTest] level: " + i );
            
            AbstractElement eldeep = new StandardElement () ;
            eldeep.setName ( "element-" + i ) ;
            
            lastContainer.addChild ( eldeep ) ;
            lastContainer = eldeep ;
            
            AbstractServerActiveObject saodeep = new BaseServerActiveObject () ;
            lastContainer.addChild ( saodeep ) ;
            lastContainer = saodeep ;
            
            //System.out.println("eldeep: " + eldeep );
            //System.out.println("Placeholder for eldeep: " + eldeep.getPlaceholders () );
            //System.out.println("saodeep: " + saodeep );
            //System.out.println("Placeholder for saodeep: " + saodeep.getPlaceholders () );
        }
        
        assertEquals ( "The inherited placeholders were wrong", expectedPlaceholders, lastContainer.getPlaceholders () ) ;
        
    }
    
    /**
     * A child is allowed to override a parents placeholder values when the
     * placeholder already exists in the child.
     */
    public void testChildSpecialization () {
        
        /* Test a BaseServerActiveObject allows specialization by adding a StandardElement to it */
        AbstractServerActiveObject sao = new BaseServerActiveObject () ;
        sao.setPlaceholder ( "p1", "v1" ) ;
        
        AbstractElement el = new StandardElement () ;
        el.setName ( "element-1" ) ;
        el.setPlaceholder ( "p1", "specialized-value-for-otherwise-inherited-value" ) ;
        
        sao.addChild ( el ) ;
        
        Properties expectedPlaceholders = new Properties () ;
        expectedPlaceholders.setProperty ( "p1", "specialized-value-for-otherwise-inherited-value" ) ;
        
        assertEquals (
        "The placeholder specialization was wrong for el in sao",
        expectedPlaceholders,
        el.getPlaceholders ()
        ) ;
        
        /* Test a StandardElement allows specialization by adding a BaseServerActiveObject to it */
        el = new StandardElement () ;
        el.setName ( "element-2" ) ;
        el.setPlaceholder ( "p2", "v2" ) ;
        
        sao = new BaseServerActiveObject () ;
        sao.setPlaceholder ( "p2", "specialized-value-for-otherwise-inherited-value-sao-in-el" ) ;
        
        el.addChild ( sao ) ;
        
        expectedPlaceholders = new Properties () ;
        expectedPlaceholders.setProperty ( "p2", "specialized-value-for-otherwise-inherited-value-sao-in-el" ) ;
        
        assertEquals (
        "The placeholder specialization was wrong for sao in el",
        expectedPlaceholders,
        sao.getPlaceholders ()
        ) ;
    }
    
}
