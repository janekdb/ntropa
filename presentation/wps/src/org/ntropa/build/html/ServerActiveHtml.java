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
 * ServerActiveHtml.java
 *
 * Created on 11 October 2001, 16:44
 */

package org.ntropa.build.html;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ntropa.build.jsp.ApplicationFinder;
import org.ntropa.build.jsp.FinderSet;
import org.ntropa.build.jsp.JspSerializable;
import org.ntropa.build.jsp.JspUtility;
import org.ntropa.utility.CollectionsUtilities;
import org.ntropa.utility.StringUtilities;

/**
 * This class is represents a parsed block of Server Active HTML.
 *
 * @author  jdb
 * @version $Id: ServerActiveHtml.java,v 1.24 2002/11/30 23:03:09 jdb Exp $
 */
public class ServerActiveHtml extends GenericDeserializable
implements Deserializable, JspSerializable {
    
    private String _name ;
    
    private String _templateName ;
    
    private List _elements ;
    
    private Element _implicitElement ;
    
    public static final String IMPLICIT_ELEMENT_NAME = "-implicit" ;
    
    // TODO: Revert to ntropa class names.
    //private static final String DEFAULT_COMPONENT_CLASS = "org.ntropa.runtime.sao.BaseServerActiveObject" ;
    private static final String DEFAULT_COMPONENT_CLASS = "com.studylink.sao.BaseServerActiveObject" ;
    
    // TODO: Revert to ntropa class names.
    //private static final String DEFAULT_COMPONENT_TYPE = "org.ntropa.runtime.sao.AbstractServerActiveObject" ;
    private static final String DEFAULT_COMPONENT_TYPE = "com.studylink.sao.AbstractServerActiveObject" ;
    
    /**
     * Creates new ServerActiveHtml.
     *
     * @param name the name of the server active HTML section
     * @param templateName the name of the template to search for
     */
    public ServerActiveHtml( String name, String templateName ) {
        
        if ( name == null )
            throw new IllegalArgumentException( "Attempt to construct ServerActiveHtml with a null 'name' argument" ) ;
        if ( name.equals( "" ) )
            throw new IllegalArgumentException( "Attempt to construct ServerActiveHtml with an empty 'name' argument" ) ;
        
        if ( templateName == null )
            throw new IllegalArgumentException( "Attempt to construct ServerActiveHtml with a null 'templateName' argument" ) ;
        if ( templateName.equals( "" ) )
            throw new IllegalArgumentException( "Attempt to construct ServerActiveHtml with an empty 'templateName' argument" ) ;
        
        _name = name.toLowerCase() ;
        
        _templateName = templateName.toLowerCase() ;
        
    }
    
    /**
     * Creates new ServerActiveHtml.
     *
     * @param name the name of the server active HTML section
     */
    public ServerActiveHtml( String name ) throws ServerActiveHtmlException {
        
        if ( name == null )
            throw new IllegalArgumentException( "Attempt to construct ServerActiveHtml with a null name argument" ) ;
        if ( name.equals( "" ) )
            throw new IllegalArgumentException( "Attempt to construct ServerActiveHtml with an empty 'name' argument" ) ;
        
        _name = name.toLowerCase() ;
        
        _templateName = null ;
        
    }
    
    /** Prevent no-arg construction */
    private ServerActiveHtml() {}
    
    
    /**
     * Return the name of this <code>ServerActiveHtml</code>
     *
     * @return String the name of this <code>ServerActiveHtml</code>
     */
    public String getName() {
        return _name ;
    }
    
    
    /**
     * @return Return true if a template is linked to
     */
    public boolean usesTemplate() {
        return getTemplateName() != null ;
    }
    
    /**
     * Return the name of the template linked to
     */
    public String getTemplateName() {
        return _templateName ;
    }
    
    
    /**
     * Check for the implicit Element.
     *
     * @return Returns true if the implicit Element exists
     */
    public boolean hasImplicitElement() {
        return _implicitElement != null ;
    }
    
    
    /**
     * Return the implicit Element.
     *
     * @return Returns the implicit Element if it exists otherwise
     * returns null.
     */
    public Element getImplicitElement() {
        return _implicitElement ;
    }
    
    
    /**
     * @return Returns the implicit Element, creating it if it does not exist
     */
    private Element getImplicitElementCreateIfNull() {
        if ( _implicitElement == null )
            _implicitElement = new Element( IMPLICIT_ELEMENT_NAME ) ;
        
        return _implicitElement ;
    }
    
    
    /**
     * Return the count of <code>Element</code>s.
     *
     * Note: This excludes the implicit <code>Element</code>
     */
    public int getElementCount( ) {
        
        if ( _elements == null )
            throw new IllegalStateException( "Called before template text had been set" ) ;
        
        return _elements.size() ;
    }
    
    
    /**
     * Check for the named <code>Element</code>.
     *
     * Note: Does not test for the implicit object.
     *
     * @return Returns true if the named <code>Element</code> is
     * in the list of <code>Element</code>s maintained ny this
     * <code>ServerActiveHtml</code>
     */
    public boolean hasElement( String name ) {
        
        if ( _elements == null )
            return false ;
        
        Iterator it = _elements.iterator() ;
        while ( it.hasNext() )
            if ( ((Element) it.next() ).getName().equals( name ) )
                return true ;
        
        return false ;
        
    }
    
    
    /**
     * Return the named <code>Element</code>. If the <code>Element</code> is
     * missing return null.
     *
     * Note: Does not test for the implicit object.
     *
     * @param name The name of the element to return.
     *
     * @return Return the Element if found otherwise returns null.
     */
    public Element getElement( String name ) {
        
        if ( _elements == null )
            return null ;
        
        Iterator it = _elements.iterator() ;
        while ( it.hasNext() ) {
            Element element = (Element) it.next() ;
            if ( element.getName().equals( name ) )
                return element ;
        }
        
        return null ;
    }
    
    
    /**
     * Return the list of <code>Element</code>s.
     */
    public List getElements() {
        
        if ( _elements == null )
            return Collections.EMPTY_LIST ;
        
        return _elements ;
    }
    
    
    /**
     * Return the first <code>ELement</code> found in a breadth-first traversal
     * of this object's <code>Elements</code> and all descendant <code>Elements</code>.
     *
     * @param name The name of the element to find
     * @return Return the first <code>Element</code> with named name or null. The search
     * is breadth-first.
     */
    public Element findElement( String name ) {
        
        if ( name == null )
            throw new IllegalArgumentException( "name was null" ) ;
        
        Element found = null ;
        for ( List candidates = getChildren() ; candidates.size() > 0 ; ) {
            found = findElementFromCandiates( candidates, name ) ;
            if ( found != null )
                break ;
            
            /* The new candidates are the Elements of the ServerActiveHtmls of the current Elements */
            candidates = getNextLevelOfElements ( candidates ) ;
        }
        return found ;
    }
    
    
    private Element findElementFromCandiates( List candidates, String name ) {
        
        for ( Iterator elements = candidates.iterator() ; elements.hasNext() ; ) {
            Element e = ( Element ) elements.next() ;
            if ( e.getName().equals( name ) )
                return  e ;
        }
        return null ;
    }
    
    
    private List getNextLevelOfElements( List elementList ) {
        
        List result = new LinkedList() ;
        for ( Iterator elements = elementList.iterator() ; elements.hasNext() ; ) {
            for ( Iterator sahs = ( ( Element ) elements.next() ).getServerActiveHtmls().iterator() ; sahs.hasNext() ; ) {
                result.addAll( ( ( ServerActiveHtml ) sahs.next() ).getChildren() ) ;
            }
        }
        return result ;
    }
    
    
    /* ---- Implementation of JspSerializable --- */
    
    /**
     * As this will be used to write out the JSP we need to return
     * all <code>Element</code>s including the implicit <code>Element</code>
     */
    public List getChildren() {
        /* Copy the list so the original does not change */
        List children = new LinkedList( getElements() ) ;
        if ( hasImplicitElement() )
            children.add( getImplicitElement() ) ;
        return children ;
    }
    
    
    public String getComponentTypeName() {
        //FIXME: temporary.
        return DEFAULT_COMPONENT_TYPE ;
    }
    
    
    /**
     * Create the code to set up this object. See interface JspSerializable for details.
     *
     * @param objName The name of the object to create and initialise.
     *
     * @param buffer A <code>StringBuilder</code> to append set up code to
     *
     * @param finderSet A <code>FinderSet</code> used to look up information based
     * on a certain context. The context is expected to be the current page location.
     */
    public void getSetUpCode( String objName, StringBuilder buffer, FinderSet finderSet ) {
        
        if ( finderSet == null )
            throw new IllegalArgumentException(
            "Attempt to invoke ServerActiveHtml.getSetUpCode with null finderSet" ) ;
        
        ApplicationFinder appFinder = finderSet.getApplicationFinder() ;
        
        if ( appFinder == null )
            throw new IllegalArgumentException(
            "Attempt to invoke ServerActiveHtml.getSetUpCode with null appFinder" ) ;
        
        /*
         * Ask the application data finder for the data for this named server active object.
         */
        Properties appData = appFinder.getSaoData( getName() ) ;
        String clazzName = DEFAULT_COMPONENT_CLASS ;
        if ( appData != null ) {
            clazzName = appFinder.getClazzName( appData ) ;
        }
        JspUtility.getObjectCreationCode( objName, clazzName, buffer ) ;
        /*
         * Now add the property setters.
         *
         * Note: This is not type safe. It's the responsibilty of the developer while
         * configuring the sao to make sure a corresponding method signature exists.
         *
         * Currently we assume the argument is a String. Adding this property
         *
         *   sao.my-sao-name.prop.newsFeedId = hurst@welles.studylink.com
         *
         * will generate this code
         *
         *  (DEFAULT_COMPONENT_CLASS) component_1.setNewsFeedId ( "hurst@welles.studylink.com" ) ;
         */
        if ( appData != null ) {
            Properties props = CollectionsUtilities.getPropertiesSubset( appData, ApplicationFinder.PROPERTY_NAMES_PREFIX ) ;
            /* Order the properties for unit testing */
            if ( props != null ) {
                SortedSet keys = new TreeSet( props.keySet() ) ;
                Iterator it = keys.iterator() ;
                while ( it.hasNext() ) {
                    String prop = (String) it.next() ;
                    String val = props.getProperty( prop ) ;
                    buffer.append(
                    "( ( " + clazzName + " ) " + objName + " ).set" + StringUtilities.capitaliseFirstLetter( prop ) +
                    //" ( \"" + JspUtility.jspEscape ( StringUtilities.escapeString ( val ) ) + "\" ) ;\n"
                    " ( " + JspUtility.makeSafeQuoted( val ) + " ) ;\n"
                    ) ;
                }
            }
        }
        super.getSetUpCode( objName, buffer, finderSet ) ;
    }
    
    
    /**
     * The complier selects the overloaded method to invoke at compile time so
     * this will not invoke add ( Fragment ) but will invoke add ( Object )
     *
     *  Object frag = new Fragment () ;
     *  sah.add ( frag ) ;
     *
     * This is awkward when using the return of Iterator.next () ;
     *
     * We solve this Java feature with this convenience method.
     *
     * @param deserializable an instance of <code>Deserializable</code>
     */
    public void add( Deserializable deserializable ) {
        
        if ( deserializable instanceof Fragment )
            add( ( Fragment ) deserializable ) ;
        else if ( deserializable instanceof ServerActiveHtml )
            add( ( ServerActiveHtml ) deserializable ) ;
        else if( deserializable instanceof Element )
            add( ( Element ) deserializable ) ;
        else if( deserializable instanceof Placeholder )
            add( ( Fragment ) deserializable ) ;
        else
            throw new IllegalArgumentException( "Unhandled type: " + deserializable.getClass().getName() ) ;
    }
    
    /* --- Implementation of Deserializable --- */
    
    /**
     * A method to append a <code>Fragment</code> to an object,
     * typically used in the deserialization of that object.
     *
     * When a <code>ServerActiveHtml</code> is asked to append a <code>Fragment</code>
     * it delegates this to an implicit <code>Element</code> which is created on demand.
     */
    public void add( Fragment fragment ) {
        //System.out.println ("[ServerActiveHtml] add Fragment" );
        
        /*
         * If this <code>ServerActiveHtml</code> uses a template nothing
         * it contains is relevant so we ignore it.
         */
        // TEMP if ( usesTemplate () )
        // TEMP    return ;
        
        getImplicitElementCreateIfNull().add( fragment ) ;
    }
    
    
    /**
     * A method to add an <code>ServerActiveHtml</code> object to a
     * <code>ServerActiveHtml</code> object.
     *
     * In this case we add the <code>ServerActiveHtml</code> to the implicit <code>Element</code>
     *
     */
    public void add( ServerActiveHtml child ) {
        //System.out.println ("[ServerActiveHtml] add ServerActiveHtml" );
        
        /*
         * If this <code>ServerActiveHtml</code> uses a template nothing
         * it contains is relevant so we ignore it.
         * 02-9-5 jdb. No longer true, the children may be used when replacing Placeholders
         */
        // TEMP if ( usesTemplate () )
        // TEMP     return ;
        
        getImplicitElementCreateIfNull().add( child ) ;
    }
    
    
    /**
     * A method to add an <code>Element</code> object to a
     * <code>ServerActiveHtml</code> object.
     *
     * In this case we add the <code>Element</code> to the list of
     * Elements.
     *
     * @param child The <code>Element</code> to add. If a child with the same
     * name exists a <code>IllegalArgumentException</code> is thrown.
     *
     * @throws A <code>IllegalArgumentException</code> is thrown if this object
     * already has an <code>Element</code> with the same name.
     */
    public void add( Element child ) {
        //System.out.println ("[ServerActiveHtml] add Element" );
        
        /*
         * If this <code>ServerActiveHtml</code> uses a template nothing
         * it contains is relevant so we ignore it.
         * 02-9-5 jdb. No longer true, the children may be used when replacing Placeholders
         */
        // TEMP if ( usesTemplate () )
        // TEMP     return ;
        
        if ( _elements == null )
            _elements = new LinkedList() ;
        
        /* Disallow duplicates (for now) */
        if ( hasElement( child.getName() ) )
            throw new IllegalArgumentException("[ServerActiveHtml]: Attempt to add duplicate Element: " + child.getName() );
        
        _elements.add( child ) ;
        
    }
    
    /**
     * A method to add a <code>Placeholder</code> to a deserializable object,
     * used in the deserialization of objects from marked up html.
     * <p>
     * FIXME: Disallow this operation when the ServerActiveHtml has not been created
     * for use in the parsing of a template.
     * <p>
     * In the majority of cases a Placeholder will be added directly to an Element
     * because this is how MarkedUpHtmlParser operates when parsing a template. However
     * in the use case of a intermediate template being used to provide a default for
     * some elements then a Placeholder may be added. See Example B in the package documentation
     * for details.
     *
     * @param placeholder The <code>Placeholder</code> to add.
     */
    public void add( Placeholder placeholder ) {
        
        getImplicitElementCreateIfNull().add( placeholder ) ;
    }
    
    
    public int hashCode() {
        return 37 * getName().hashCode() + ( usesTemplate() ? 1 : 0 ) ;
    }
    
    
    /**
     * Return true if the objects are the same class and
     * have the same content.
     */
    public boolean equals( Object obj ) {
        
        
        if ( obj == null )
            return false ;
        
        if ( ! ( obj instanceof ServerActiveHtml ) )
            return false ;
        
        ServerActiveHtml sah = (ServerActiveHtml) obj ;
        
        if ( ! sah.getName().equals( getName() ) )
            return false ;
        
        
        if ( sah.usesTemplate() != usesTemplate() )
            return false ;
        
        if ( usesTemplate() )
            if ( ! sah.getTemplateName().equals( getTemplateName() ) )
                return false ;
        /* both null or equal if we get here */
        
        
        if ( _implicitElement != null ) {
            if ( ! _implicitElement.equals( sah._implicitElement ) )
                return false ;
        }
        else if ( sah._implicitElement != null ) {
            return false ;
        }
        /* both null or equal if we get here */
        
        
        if ( _elements != null ) {
            if ( ! _elements.equals( sah._elements ) )
                return false ;
        }
        else if ( sah._elements != null ) {
            return false ;
        }
        /* both null or equal if we get here */
        
        
        if ( ! sah.getPlaceholders().equals( getPlaceholders() ) )
            return false ;
        
        return true ;
    }

    
    public String toString() {

        return new ToStringBuilder (this, ToStringStyle.MULTI_LINE_STYLE ).
        append ( "_name", _name).
        append ( "_templateName", _templateName).
        append ( "_elements", _elements ).
        append ( "_implicitElement", _implicitElement ).
        append ( "placeholders", getPlaceholders ()).
        toString ();
        
    }
    
}
