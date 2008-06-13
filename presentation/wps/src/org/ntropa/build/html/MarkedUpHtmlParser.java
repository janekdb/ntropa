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
 * MarkedUpHtmlParser.java
 *
 * Created on 09 November 2001, 16:53
 */

package org.ntropa.build.html;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.ntropa.build.Constants;
import org.ntropa.build.jsp.TemplateFinder;
import org.ntropa.build.jsp.TemplateFinderException;

/**
 * A <code>MarkedUpHtmlParser</code> parses a section of HTML and
 * creates a list of trees of objects (sometimes called a 'grove'). Each object is
 * one of
 * <ul>
 * <li><code>ServerActiveHtml</code>
 * <li><code>Element</code> object.
 * <li><code>Fragment</code>
 * </ul>
 *
 * Examples:
 *
 * This HTML
 * <!-- name="result-list" -->
 * These are the results
 * <!-- name="/results-list" -->
 *
 * results in this object tree
 * SAH ( "results-list" )
 *     Element ( "-implicit" )
 *         \nThese are the results\n
 *
 * This HTML
 * <!-- name="alpha" -->
 * Hello<hr>
 * <!-- element="beta" -->
 * <table border=10><tr><td>Interferring</td></tr></table>
 * <!-- name="gamma" -->
 * <!-- element="part-1" -->
 * PART 1
 * <!-- element="/part-1" -->
 * <!-- element="part-2" -->
 * PART 2
 * <!-- element="/part-2" -->
 * <!-- element="part-3" -->
 * PART 3
 * <!-- element="/part-3" -->
 * <!-- name="/gamma" -->
 * <!-- element="/beta" -->
 * <!-- name="/alpha" -->
 *
 * results in this object tree
 * SAH ( "alpha" )
 *     Element ( "-implicit" )
 *         \nHello<hr>\n\n
 *     Element ( "beta" )
 *         \n<table border=10><tr><td>Interferring</td></tr></table>\n
 *         SAH ( "gamma" )
 *             Element ( "-implicit" )
 *                 \n\n\n\n
 *             Element ( "part-1" )
 *                 \nPART 1\n
 *             Element ( "part-2" )
 *                 \nPART 2\n
 *             Element ( "part-3" )
 *                 \nPART 3\n
 * This HTML
 * <html><head><title>Global Imaging Corporation</title></head>
 * <body>
 * <!-- name="alpha" -->
 * Hello<br>
 * Greetings<br>
 * <!-- name="/alpha" -->
 * <!-- name="zodiac" -->
 * Hello<br>
 * Greetings<br>
 * <!-- element="cobra" -->
 * Visions of Domino
 * <!-- element="/cobra" -->
 * <!-- name="/zodiac" -->
 * </body>
 * </html>
 *
 * results in this list of object trees (newlines omitted from this example)
 *
 * Fragment
 *     <html><head><title>Global Imaging Corporation</title></head>
 *     <body>
 * SAH ( "alpha" )
 *     Element ( "-implicit" )
 *         Hello<br>
 *         Greetings<br>
 * SAH ( "zodiac" )
 *     Element ( "-implicit" )
 *         Hello<br>
 *         Greetings<br>
 *     Element ( "cobra" )
 *         Visions of Domino
 * Fragment
 *     </body>
 *     </html>
 *
 * FIXME: what is the case.
 * (Note: The HTML stored by Elements are Fragments and ServerActiveHtml objects)
 *
 * Currently <code>Element</code>s may not have sub-<code>Element</code>s. This can be
 * added if needed.
 *
 * @author  jdb
 * @version $Id: MarkedUpHtmlParser.java,v 1.23 2002/12/20 16:44:43 jdb Exp $
 */
public class MarkedUpHtmlParser {
    
    private FragmentTokenizer _tokenizer ;
    
    private Element _topLevelObjectList ;
    
    private Stack _objectStack ;
    private Stack _expectedCloseStack ;
    
    private Deserializable _currentObject ;
    
    /*
     * The count of how many instances of MarkedUpHtmlParser have led to this
     * instance being created
     */
    private int _invocationDepth = 0 ;
    
    /* To protect against infinite recursion we bail at this depth */
    private static final int MAX_INVOCATION_DEPTH = 200 ;
    
    /**
     * Creates new MarkedUpHtmlParser
     *
     * @param fragment The <code>Fragment</code> to parse
     */
    public MarkedUpHtmlParser( Fragment fragment ) {
        
        if ( fragment == null )
            throw new IllegalArgumentException( "Attempt to construct MarkedUpHtmlParser from null Fragment" ) ;
        
        init( fragment ) ;
    }
    
    /**
     * Creates new MarkedUpHtmlParser
     *
     * @param fragment The <code>Fragment</code> to parse
     * @param invocationDepth The number of <code>MarkedUpHtmlParser</code> objects
     * which where created prior to this instance.
     */
    public MarkedUpHtmlParser( Fragment fragment, int invocationDepth ) {
        
        if ( fragment == null )
            throw new IllegalArgumentException( "Attempt to construct MarkedUpHtmlParser from null Fragment" ) ;
        
        _invocationDepth = invocationDepth ;
        
        init( fragment ) ;
    }
    
    /**
     * Creates new MarkedUpHtmlParser
     *
     * @param fragment The <code>Fragment</code> to parse
     */
    public MarkedUpHtmlParser( String fragment ) {
        
        if ( fragment == null )
            throw new IllegalArgumentException( "Attempt to construct MarkedUpHtmlParser from null String" ) ;
        
        init( new Fragment( fragment ) ) ;
    }
    
    
    /**
     * Common initialization.
     *
     * @param fragment The <code>Fragment</code> to parse.
     */
    protected void init( Fragment fragment ) {
        _tokenizer = new StandardFragmentTokenizer( fragment ) ;
    }
    
    
    /**
     * When true the Fragment being parsed came from a template
     */
    private boolean isTemplate = false ;
    
    
    /**
     * @param isTemplate when true the parser will assume the Fragment to be
     * parsed is a template
     */
    public void setTemplate( boolean isTemplate ) {
        
        this.isTemplate = isTemplate ;
    }
    
    
    private boolean isTemplate() {
        
        return isTemplate ;
    }
    
    /**
     * Parse the fragment to build a list of trees of objects.
     *
     * For each opening comment (see note) push the current object onto the stack, remember
     * what the closing comment should be and create a new current object.
     *
     * For each closing comment check the close is expected, if it is then finish the
     * current object and pop the previous object from the stack.
     * Add the finished object to the object just popped from the stack.
     *
     * For each <code>Fragment</code> object pass it to the current object.
     * A <code>ServerActiveHtml</code> will append the HTML in the <code>Fragment</code>
     * to the implicit <code>Element</code>. An <code>Element</code> will add the <code>Fragment</code>
     * as a child.
     *
     * (Note: An opening comment is represented as a <code>MarkUpAttributes</code> with
     * one of these properties where the value does not start with "/":
     *
     *  name             (ServerActiveHtml)
     *  element          (Element)
     *  use-template     (ServerActiveHtml)
     *
     * For example
     *      <!-- name = "my-sao" -->
     *
     * For example
     *      <!-- element = "my-list-of-checkboxes" -->
     *
     * For example
     *      <!-- use-template = "full-page-template" -->
     *
     * A closing comment is represented in the same way except that the value should
     * start with "/".
     * )
     */
    public void parse() throws MarkedUpHtmlException {
        
        /*
         * This stack contains all objects which have been deferred
         * while a child object is constructed.
         */
        _objectStack = new Stack() ;
        
        /*
         * This stack contains <code>MarkUpAttributes</code> objects
         * which are constructed when an opening map is encountered.
         * When a closing map is encountered it is compared with the
         * object on the top of this stack which is the expected value
         * for the encountered closing map.
         *
         * Nomenclature:
         * opening map = <!-- name="newobject" -->
         * closing map = <!-- name="/newobject" -->
         */
        _expectedCloseStack = new Stack() ;
        
        /*
         * This Element maintains a list of the top level objects which
         * have been encountered. The Element never becomes part of the
         * final grove of parsed object trees. An Element has all the
         * properties of an object needed to work in the parsing process
         * including replace ( ServerActiveHtml, List ) so we avoid
         * introducing a specialized List subclass as an earlier implementation
         * did.
         *
         * (If we enclose the entire page in a default
         * ServerActiveHtml in order to implement default handling
         * for certain 'system' placeholders then this will always
         * be a list with exactly one element. When we are parsing
         * the HTML of a referenced template then this will be
         * a list of one or more objects.)
         */
        _topLevelObjectList = new Element( "top-level-list" ) ;
        
        _currentObject = _topLevelObjectList ;
        
        while ( _tokenizer.hasNext() ) {
            Object o = _tokenizer.next() ;
            
            checkType( o ) ;
            
            if ( o instanceof Fragment ) {
                _currentObject.add( (Fragment) o ) ;
                continue ;
            }
            
            MarkUpAttributes m = (MarkUpAttributes) o ;
            validate( m ) ;
            
            /*
             * At this point we know we have a valid MarkUpAttributes object
             * and need to either finish the current object or start a new one
             */
            
            if ( m.isServerActiveHtml() )
                handleServerActiveHtml( m ) ;
            else if ( m.isElement() )
                handleElement( m ) ;
            else if ( m.isPlaceholder() ) {
                if ( ! isTemplate() )
                    throw new MarkedUpHtmlException( "'use-element' is only allowed in templates" ) ;
                handlePlaceholder( m ) ;
            }
            else
                throw new MarkedUpHtmlException( "Unhandled map: " + m ) ;
            
            
        }
        
        /* It's an error to have any expected closing maps left at the end of parsing */
        if ( _expectedCloseStack.size() > 0 )
            throw new MarkedUpHtmlException( "Missing closing maps:\n" + _expectedCloseStack ) ;
    }
    
    
    /**
     * Handle a map of attributes which represent part of a ServerActiveHtml object.
     *
     * ie. an opening map or a closing map.
     *
     * @param m A <code>MarkUpAttributes</code>
     */
    private void handleServerActiveHtml( MarkUpAttributes m ) throws MarkedUpHtmlException {
        
        //System.out.println("[handleServerActiveHtml] called: " + m );
        
        /* Remember before convertUseTemplate */
        boolean isUseTemplate = m.getAttribute( Constants.MarkUp.USE_TEMPLATE_ATTRIBUTE ) != null ;
        
        ServerActiveHtmlAttributes sahAttributes ;
        try {
            m.convertUseTemplateFudge() ;
            sahAttributes = new ServerActiveHtmlAttributes( m ) ;
        }
        catch ( IllegalArgumentException e ) {
            throw new MarkedUpHtmlException( "Problem constructing ServerActiveHtml object from " +
            "MarkUpAttributes:\n" + m.toString() + "\n" + e.toString() ) ;
        }
        
        if ( sahAttributes.isOpening() ) {
            
            /* Remember the map we should encounter on the way back */
            MarkUpAttributes expectedClosingMap = new MarkUpAttributes() ;
            if ( isUseTemplate )
                expectedClosingMap.setAttribute(
                Constants.MarkUp.SERVER_ACTIVE_HTML_ATTRIBUTE, /* because of convertUseTemplateFudge */
                "/-use-template"
                ) ;
            else
                expectedClosingMap.setAttribute(
                Constants.MarkUp.SERVER_ACTIVE_HTML_ATTRIBUTE,
                "/" + sahAttributes.getName()
                ) ;
            
            _expectedCloseStack.push( expectedClosingMap ) ;
            
            ServerActiveHtml sah ;
            
            if ( sahAttributes.usesTemplate() )
                sah = new ServerActiveHtml( sahAttributes.getName(), sahAttributes.getTemplate() ) ;
            else
                sah = new ServerActiveHtml( sahAttributes.getName() ) ;
            
            sah.setPlaceholders( m.getPlaceholders() ) ;
            
            _currentObject.add( sah ) ;
            
            _objectStack.push( _currentObject ) ;
            
            _currentObject = sah ;
            
        }
        else {
            handleClosingMap( m ) ;
        }
    }
    
    
    
    /**
     * Handle a map of attributes which represent part of an Element object.
     *
     * ie. an opening map or a closing map.
     *
     * @param m A <code>MarkUpAttributes</code>
     */
    private void handleElement( MarkUpAttributes m ) throws MarkedUpHtmlException {
        
        //System.out.println("[handleElement] called");
        
        ElementAttributes elementAttributes ;
        try {
            elementAttributes = new ElementAttributes( m ) ;
        }
        catch ( IllegalArgumentException e ) {
            throw new MarkedUpHtmlException( "Problem constructing ElementAttributes object from " +
            "MarkUpAttributes:\n" + m.toString() + "\n" + e.toString() ) ;
        }
        
        if ( elementAttributes.isOpening() ) {
            
            /* Remember the map we should encounter on the way back */
            MarkUpAttributes expectedClosingMap = new MarkUpAttributes() ;
            expectedClosingMap.setAttribute(
            Constants.MarkUp.ELEMENT_ATTRIBUTE,
            "/" + elementAttributes.getName()
            ) ;
            
            _expectedCloseStack.push( expectedClosingMap ) ;
            
            Element element ;
            
            element = new Element( elementAttributes.getName() ) ;
            
            element.setPlaceholders( m.getPlaceholders() ) ;
            
            /*
             * A IllegalArgumentException can be thrown if the current ServerActiveHtml already has
             * an Element with the name this Element has.
             */
            try {
                _currentObject.add( element ) ;
            }
            catch ( IllegalArgumentException e ) {
                throw new MarkedUpHtmlException( e.toString() ) ;
            }
            
            _objectStack.push( _currentObject ) ;
            
            _currentObject = element ;
        }
        else {
            handleClosingMap( m ) ;
        }
        
    }
    
    
    /**
     * Handle a map of attributes which represent part of a Placeholder object.
     *
     * ie. an opening map or a closing map.
     *
     * @param m A <code>MarkUpAttributes</code>
     */
    private void handlePlaceholder( MarkUpAttributes m ) throws MarkedUpHtmlException {
        
        PlaceholderAttributes attrs ;
        try {
            attrs = new PlaceholderAttributes( m ) ;
        }
        catch ( IllegalArgumentException e ) {
            throw new MarkedUpHtmlException( "Problem constructing PlaceholderAttributes object from " +
            "MarkUpAttributes:\n" + m.toString() + "\n" + e.toString() ) ;
        }
        
        if ( attrs.isOpening() ) {
            
            /* Remember the map we should encounter on the way back */
            MarkUpAttributes expectedClosingMap = new MarkUpAttributes() ;
            expectedClosingMap.setAttribute(
            Constants.MarkUp.USE_ELEMENT_ATTRIBUTE,
            "/" + attrs.getUseElement()
            ) ;
            
            _expectedCloseStack.push( expectedClosingMap ) ;
            
            Placeholder placeholder ;
            
            placeholder = new Placeholder( attrs.getUseElement() ) ;
            
            /*
             * A UnsupportedOperationException can be thrown if the current object does
             * not allow the addition of a Placeholder
             */
            try {
                _currentObject.add( placeholder ) ;
            }
            catch ( IllegalArgumentException e ) {
                throw new MarkedUpHtmlException( e.toString() ) ;
            }
            
            _objectStack.push( _currentObject ) ;
            
            _currentObject = placeholder ;
        }
        else {
            handleClosingMap( m ) ;
        }
        
        
    }
    
    /**
     * Perform common actions on encountering a closing map.
     * ie when a map deserialised from <!-- element="/x" --> or <!-- name="/y" -->
     * is encountered.
     *
     * @param m A <code>MarkUpAttributes</code>
     */
    private void handleClosingMap( MarkUpAttributes m ) throws MarkedUpHtmlException {
        
        /*
         * Check that the closing map is in sequence.
         */
        if ( _expectedCloseStack.size() < 1 )
            throw new MarkedUpHtmlException( "Too many closing maps. Attempted close for: " + m ) ;
        
        MarkUpAttributes expectedClosingMap = (MarkUpAttributes) _expectedCloseStack.pop() ;
        
        if ( ! m.toLowerCase().equals( expectedClosingMap.toLowerCase() ) )
            throw new MarkedUpHtmlException( "Unexpected closing map." +
            "\nExpected:\n" + expectedClosingMap +
            "\nActual:\n" + m
            ) ;
        
        /* We've finished cleanly so switch back to the previous current object */
        
        _currentObject = (Deserializable) _objectStack.pop() ;
        
    }
    
    
    private void checkType( Object object ) throws MarkedUpHtmlException {
        
        if ( object instanceof MarkUpAttributes )
            return ;
        
        if ( object instanceof Fragment )
            return ;
        
        throw new MarkedUpHtmlException( "Unhandled type: " + object.getClass().getName() ) ;
    }
    
    
    private void validate( MarkUpAttributes m ) throws MarkedUpHtmlException {
        
        if ( m.size() == 0 )
            throw new MarkedUpHtmlException( "MarkUpAttributes object had no attributes" ) ;
        
        boolean containsName        = m.containsKey( Constants.MarkUp.SERVER_ACTIVE_HTML_ATTRIBUTE ) ;
        boolean containsElement     = m.containsKey( Constants.MarkUp.ELEMENT_ATTRIBUTE ) ;
        boolean containsUseTemplate = m.containsKey( Constants.MarkUp.USE_TEMPLATE_ATTRIBUTE ) ;
        boolean containsUseElement  = m.containsKey( Constants.MarkUp.USE_ELEMENT_ATTRIBUTE ) ;
        
        int attrCount =
        ( containsName ? 1 : 0 ) + ( containsElement ? 1 : 0 ) +
        ( containsUseTemplate ? 1 : 0 ) + ( containsUseElement ? 1 : 0 ) ;
        if ( attrCount > 1 )
            throw new MarkedUpHtmlException(
            "MarkUpAttributes object had an identity crisis; it contained more than one of 'name', 'element', 'use-template', 'use-element' attributes:"
            + "\nattributes: " + m
            ) ;
        
        if (  attrCount == 0 )
            throw new MarkedUpHtmlException(
            "MarkUpAttributes object had an identity crisis; it contained none of 'name', 'element', 'use-template', 'use-element' attributes"
            + "\nattributes: " + m
            ) ;
        
        /* This could be changed to a JDK 1.4 assertion */
        if ( attrCount != 1 )
            throw new RuntimeException( "attrCount was not 1" ) ;
        
        if ( containsName &&
        ( m.getAttribute( Constants.MarkUp.SERVER_ACTIVE_HTML_ATTRIBUTE ).length() == 0 ) )
            throw new MarkedUpHtmlException(
            "MarkUpAttributes 'name' attribute was zero length" ) ;
        
        if ( containsElement &&
        ( m.getAttribute( Constants.MarkUp.ELEMENT_ATTRIBUTE ).length() == 0 ) )
            throw new MarkedUpHtmlException(
            "MarkUpAttributes 'element' attribute was zero length" ) ;
        
        if ( containsUseTemplate &&
        ( m.getAttribute( Constants.MarkUp.USE_TEMPLATE_ATTRIBUTE ).length() == 0 ) )
            throw new MarkedUpHtmlException(
            "MarkUpAttributes 'use-template' attribute was zero length" ) ;
        
        if ( containsUseElement &&
        ( m.getAttribute( Constants.MarkUp.USE_ELEMENT_ATTRIBUTE ).length() == 0 ) )
            throw new MarkedUpHtmlException(
            "MarkUpAttributes 'use-element' attribute was zero length" ) ;
    }
    
    
    /**
     * Return the list of object trees
     */
    public List getGrove() {
        
        if ( _topLevelObjectList == null )
            throw new IllegalStateException( "Attempt to access grove before it had been parsed" ) ;
        
        return _topLevelObjectList.getChildren() ;
    }
    
    
    /**
     * This method walks a list of trees of objects and recursivley resolves all <code>ServerActiveHtml</code>s
     * which use a template.
     *
     * Not finding a template is not neccessarily an error as a page which uses a template may be added
     * before the corresponding template is added.
     *
     */
    public void resolveTemplates( TemplateFinder templateFinder ) throws MarkedUpHtmlException {
        
        /* Prevent cycles */
        if ( _invocationDepth >= MAX_INVOCATION_DEPTH )
            throw new MarkedUpHtmlException(
            "Maximum invocation depth reached (" + MAX_INVOCATION_DEPTH + ");" +
            " possible cyclic template resolution" ) ;
        
        if ( _topLevelObjectList == null )
            throw new IllegalStateException( "Attempt to resolve grove before it had been parsed" ) ;
        
        if ( templateFinder == null )
            throw new IllegalArgumentException( "Attempt to resolve grove with null TemplateFinder" ) ;
        
        //System.out.println("resolveTemplates: _topLevelObjectList before resolveTemplates:\n");
        //printObjectTree( _topLevelObjectList ) ;
        
        /*
         * Walk the grove by recursion.
         *
         */
        
        resolveTemplates( _topLevelObjectList, new TreeSet(), templateFinder, null ) ;
        
    }
    
    /**
     * Continue the resolution of templates.
     *
     *@param obj The object to handle. There are four cases corresponding to the four
     *           types of object. The action for each type are summarized here.
     *           <table border=1>
     *           <tr><td>Type               <td>Action</tr>
     *           <tr><td>Fragment           <td>Do nothing</tr>
     *           <tr><td>ServerActiveHtml   <td>If this is a template using sah perform template
     *                                      lookup and replacement otherwise recurse over children</tr>
     *           <tr><td>Element            <td>Recurse over children</tr>
     *           <tr><td>Placeholder        <td>Do nothing</tr>
     *           </table>
     *
     *@param watchDog A Set used to collect identity hash codes of objects encountered
     *                as a defense against cycles.
     *
     *@param templateFinder A <code>TemplateFinder</code> to locate templates with.
     *
     *@param parentElement This is used when a child object needs to replace itself in
     *                     it's parent Element's list of children. This will happen when a
     *                     <code>ServerActiveHtml</code> object resolves itself by it's
     *                     template reference to a list of new objects which need to be
     *                     added to the parent Element in place of the sah.
     */
    private void resolveTemplates(
    Object obj,
    Set watchDog,
    TemplateFinder templateFinder,
    Element parentElement
    ) throws MarkedUpHtmlException {
        
        /* Check for cycles */
        Integer objId = new Integer( System.identityHashCode( obj ) ) ;
        if ( watchDog.contains( objId ) ) {
            /*
             * No Exception thrown as resolveTemplates can only be invoked
             * after parse which checks for cycles.
             */
            return ;
        }
        watchDog.add( objId ) ;
        
        if ( Thread.currentThread ().isInterrupted () )
            throw new MarkedUpHtmlException ( "Thread was interrupted" ) ;
        
        if ( obj instanceof Fragment ) {
            /* nothing */
        }
        else if ( obj instanceof ServerActiveHtml ) {
            
            ServerActiveHtml sah = (ServerActiveHtml) obj ;
            //System.out.println("resolveTemplates: ServerActiveHtml: " + sah.getName() );
            //System.out.println("resolveTemplates: ServerActiveHtml: getTemplateName : " + sah.getTemplateName() );
            
            /*
             * The sah may or may not use a template .
             *
             * If the sah does not use a template then templates are resolved as
             * a matter of course.
             *
             * If the sah does uses a template then we still need to resolve templates
             * because a child placeholder element of this sah may search the children
             * of this sah for a replacement element.
             */
            Iterator it = sah.getChildren().iterator() ;
            while ( it.hasNext() ) {
                /*
                 * A ServerActiveHtml object can not have a ServerActiveHtml object
                 * as a child therefore the list of children in this ServerActiveHtml
                 * will not need to be changed by the child so no parent argument
                 * is passed.
                 */
                resolveTemplates( it.next(), watchDog, templateFinder, null ) ;
            }
            
            if ( sah.usesTemplate() ) {
                /*
                 * Ask our TemplateFinder to locate the referenced template.
                 * If it is found then create an object tree to use in place of this object otherwise
                 * remove the current ServerActiveHtml object.
                 */
                Fragment template ;
                try {
                    template = templateFinder.getTemplate( sah.getTemplateName() ) ;
                }
                catch ( TemplateFinderException e ) {
                    throw new MarkedUpHtmlException( "Problem getting template: " + e.toString() ) ;
                }
                /*
                 * Reminder: When a sah is inside a sah as bb is inside aa
                 *
                 * <!-- name="aa" --><!-- name="bb" -->Ingo<!-- name="/bb" --><!-- name="/aa" -->
                 *
                 * bb is added to the implicit Element of aa.
                 *
                 */
                List subObjects = null ;
                if ( template != null ) {
                    /* Get the replacement object tree */
                    MarkedUpHtmlParser parser = new MarkedUpHtmlParser( template, _invocationDepth + 1 ) ;
                    /* Set this flag to allow "use-element" mark up*/
                    parser.setTemplate( true ) ;
                    parser.parse() ;
                    parser.resolveTemplates( templateFinder ) ;
                    /* Could be a List with more than one element
                     * i.e. the objects resulting from parsing
                     *
                     * <!-- template = "kaydou" -->
                     * <!-- name="a" --><!-- name="/a" -->
                     * <!-- name="b" --><!-- name="/b" -->
                     * <!-- use-element = "content" -->The content goes here*<!-- use-element = "/content" -->
                     * My word is that the $$time$$!
                     * <!-- template = "/kaydou" -->
                     *
                     * are
                     *
                     *  fragment \n
                     *  sah ( a )
                     *  fragment \n
                     *  sah ( b )
                     *  fragment \n
                     *  placeholder ( content )
                     *  fragment \nMy word is that the $$time$$!\n
                     *
                     * The list has these properties
                     *
                     * 1. All items are of type ServerActiveHtml, Fragment or Placeholder
                     * 2. All ServerActiveHtmls are resolved
                     *
                     * 2. means that the items need no further processing i.e. all template
                     * resolution has already been done.
                     *
                     * (It is possible for this list to contain consecutive Fragments as this
                     *  example shows
                     *
                     *  index.html
                     *  sah k, template k-tmpl
                     *
                     *  _include/templates.html
                     *  template k-tmpl
                     *      H
                     *      sah a, template l-tmpl
                     *      I
                     *
                     *  template l-tmpl
                     *      Q
                     *
                     * Q is inserted into k-tmpl, k-tmpl is inserted into sah-k as a List with
                     * two Fragments as elements. The two fragments are
                     *
                     *   H + Q
                     *   I
                     *
                     * where H + Q means Q has been merged into H
                     */
                    subObjects = parser.getGrove() ;
                    
                    /*
                     * We now need to replace any placeholders in the subObjects list
                     * with the children of the Element found in the ServerActiveHtml
                     * which is using the current template.
                     *
                     * To illustrate things, here are the the subObjects list and the
                     * ServerActiveHtml which result from this template and marked up
                     * html
                     *
                     * TEMPLATE
                     * <!-- template = "t1" -->
                     * "Frag-A" +
                     * "<!-- use-element= \"e1\" -->" +
                     * "Users of this template must provider an Element named e1" +
                     * "<!-- use-element = \"/e1\" -->" +
                     * "Frag-B"
                     * <!-- template = "/t1" -->
                     *
                     * PAGE
                     * "<!-- use-template = \"t1\" -->" +
                     * "<!-- element = \"e1\" -->My Content!<!-- element = \"/e1\" -->" +
                     * "<!-- use-template = \"/t1\" -->"
                     *
                     *
                     * ServerActiveHtml:
                     * [ServerActiveHtml -use-template]
                     *    [Element e1]
                     *        [Fragment] My Content!
                     *
                     * subObjects
                     * [List]
                     *    [Fragment] Frag-A
                     *    [Placeholder] e1
                     *    [Fragment] Frag-B
                     *
                     * FIXME: Make sure any placeholders (of the placeholder-pid = "pid-" kind)
                     * in the Element e1 are preserved in the substitution.
                     */
                    if ( false ) {
                        System.out.println("After resolveTemplates:");
                        System.out.println("Original ServerActiveHtml:");
                        printObjectTree( sah ) ;
                        System.out.println("New subObjects");
                        printObjectTree( subObjects ) ;
                    }
                    
                    List k = new LinkedList() ;
                    
                    for ( Iterator objects = subObjects.iterator() ; objects.hasNext() ; ) {
                        Object object = objects.next() ;
                        if ( object instanceof Placeholder ) {
                            List replacementObjects = ( ( Placeholder ) object ).getReplacement( sah ) ;
                            k.addAll( replacementObjects ) ;
                        }
                        else
                            k.add( object ) ;
                    }
                    subObjects = k ;
                }
                else {
                    /* The template is missing; maybe it wil be added later */
                    /* FIXME: Add a delete method to Element and dispatch with this dummy object */
                    subObjects = new LinkedList() ;
                    subObjects.add(
                    new Fragment( "<b>Warning: missing template: &quot;" + sah.getTemplateName() + "&quot;</b>" )
                    ) ;
                }
                if ( parentElement == null )
                    throw new MarkedUpHtmlException(
                    "The parent Element of a ServerActiveHtml which used a template was null." ) ;
                parentElement.replace( sah, subObjects ) ;
            }
        }
        else if ( obj instanceof Element ) {
            
            Element element = (Element) obj ;
            //System.out.println("resolveTemplates: Element: " + element.getName() );
            /*
             * Get an Iterator from a shallow copy of the list of children to
             * avoid a ConcurrentModificationException while modifying the Element's
             * list of children via replace ( ServerActiveHtml, List ).
             */
            Iterator it = ( new LinkedList( element.getChildren() ) ).iterator() ;
            while ( it.hasNext() ) {
                /*
                 * A Element object can have a ServerActiveHtml object
                 * as a child therefore the list of children in this Element
                 * may need to be changed by the child it it is a template referencing
                 * ServerActiveHtml object so a reference to the parent is passed.
                 */
                resolveTemplates( it.next(), watchDog, templateFinder, element ) ;
            }
        }
        else if ( obj instanceof Placeholder ) {
            /* nothing */
        }
        else
            throw new MarkedUpHtmlException( "Unexpected object type: " + obj.getClass().getName() ) ;
    }
    
    /**
     * Debugging methods. Get and print the object tree outline.
     */
    static public void printObjectTree( Object obj ) {
        StringBuilder sb = new StringBuilder() ;
        getObjectTree( obj, sb ) ;
        System.out.println( sb.toString() );
    }
    
    /**
     * Return an outline of the object tree in a <code>String</code>
     *
     * @param obj The root object
     *@return A <code>String</code> representing the outline of the tree.
     */
    static public String objectTreeToString( Object obj ) {
        StringBuilder sb = new StringBuilder() ;
        getObjectTree( obj, sb ) ;
        return sb.toString() ;
    }
    
    /**
     * Create an outline of the object tree in a <code>StringBuffer</code>
     *
     * @param obj The root object
     * @param sb The <code>StringBuilder</code> to build the outline into
     */
    static public void getObjectTree( Object obj, StringBuilder sb ) {
        Set watchDog = new TreeSet() ;
        
        getObjectTree( obj, "", sb, watchDog ) ;
    }
    
    /**
     * Debugging method. Gets the object tree outline.
     *
     * This is a fine example of procedural code...
     */
    static public void getObjectTree( Object obj, String indent, StringBuilder sb, Set watchDog ) {
        
        final String CHILD_INDENT = indent + "    " ;
        
        /* Check for cycles to stop the JVM from coming down */
        Integer objId = new Integer( System.identityHashCode( obj ) ) ;
        if ( watchDog.contains( objId ) ) {
            sb.append( "*** Error: Cycle detected ***\n" ) ;
            return ;
        }
        watchDog.add( objId ) ;
        
        if ( obj instanceof List ) {
            
            sb.append( indent + "[List]\n" ) ;
            for ( Iterator it = ( (List) obj ).iterator() ; it.hasNext() ; )
                getObjectTree( it.next(), CHILD_INDENT, sb, watchDog ) ;
        }
        else  if ( obj instanceof Fragment ) {
            String value = ( ( Fragment ) obj ).getValue() ;
            String snippet = value ;
            if ( value.length() > 40 ) {
                snippet = value.substring( 0, 20 ) ;
                snippet += "..." ;
                snippet += value.substring( value.length() - 20 ) ;
            }
            
            snippet = snippet.replace( '\n', ' ' ).replace( '\r', ' ' ) ;
            sb.append( indent + "[Fragment] " + snippet + "\n" ) ;
        }
        else if ( obj instanceof ServerActiveHtml ) {
            
            ServerActiveHtml sah = (ServerActiveHtml) obj ;
            sb.append( indent + "[ServerActiveHtml " + sah.getName() ) ;
            if ( sah.usesTemplate() )
                sb.append( " template=\"" + sah.getTemplateName() + "\"" ) ;
            sb.append( "]" ) ;
            if ( ! sah.getPlaceholders().isEmpty() )
                sb.append( " placeholders: " + sah.getPlaceholders().size() ) ;
            sb.append( "\n" ) ;
            
            if ( sah.hasImplicitElement() )
                getObjectTree( sah.getImplicitElement(), CHILD_INDENT, sb, watchDog ) ;
            for ( Iterator it = sah.getElements().iterator() ; it.hasNext() ; )
                getObjectTree( it.next(), CHILD_INDENT, sb, watchDog ) ;
        }
        else if ( obj instanceof Element ) {
            
            Element element = (Element) obj ;
            sb.append( indent + "[Element " + element.getName() + "]" ) ;
            if ( ! element.getPlaceholders().isEmpty() )
                sb.append( " placeholders: " + element.getPlaceholders().size() ) ;
            sb.append( "\n" ) ;
            
            for ( Iterator it = element.getChildren().iterator() ; it.hasNext() ; )
                getObjectTree( it.next(), CHILD_INDENT, sb, watchDog ) ;
        }
        else if ( obj instanceof Placeholder ) {
            String useElementName = ( ( Placeholder ) obj ).getUseElement() ;
            sb.append( indent + "[Placeholder] " + useElementName + "\n" ) ;
        }
        else
            throw new RuntimeException( "Unhandled object type: " + obj.getClass().getName() ) ;
        
    }
    
}

/* Example of a ServerActiveHtml.
 *
 * <!-- name="news-table" placeholder-date="-Thursday" -->
 * <!-- element="header" -->
 * <table border=1>
 * <tr><td colspan="2">The news today on -Thursday </td></tr>
 * <!-- element="/header" -->
 * <!--
 *     element="news-item"
 *     placeholder-time="17:55 hrs"
 *     placeholder-link="the-link-goes-here"
 *     placeholder-title="A debt-free student was found in Cornwall today authorities report"
 * -->
 * <tr><td class="news-link">
 * <a href="stories.html?the-link-goes-here">
 * A debt-free student was found in Cornwall today authorities report
 * </a>
 * </td><td>17:55 hrs</td></tr>
 * <!-- element="/news-item" -->
 * <!-- element="footer" -->
 * </table>
 * <!-- element="/footer" -->
 * <!-- name="/news-table" -->
 *
 */

/**
 * Examples of Elements.
 *
 * Examples:
 *
 * --------------------------------------------------------------------------------
 * <!-- element="header" -->
 * <table border=1>
 * <tr><td colspan="2">The news today</td></tr>
 * <!-- element="/header" -->
 *
 * name = "header"
 * value = "<table border=1>\n<tr><td colspan=\"2\">The news today</td></tr>" ;
 *
 * --------------------------------------------------------------------------------
 * <!--
 *     element="news-item"
 *     placeholder-time="17:55 hrs"
 *     placeholder-link="the-link-goes-here"
 *     placeholder-title="A debt-free student was found in Cornwall today authorities report"
 * -->
 * <tr><td class="news-link">
 * <a href="stories.html?the-link-goes-here">
 * A debt-free student was found in Cornwall today authorities report
 * </a>
 * </td><td>17:55 hrs</td></tr>
 * <!-- element="/news-item" -->
 *
 * name = "news-item"
 * value = "<tr><td class=\"news-link\">\n" +
 *         "<a href=\"stories.html?the-link-goes-here\">\n" +
 *         "A debt-free student was found in Cornwall today authorities report\n" +
 *         "</a>\n" +
 *         "</td><td>17:55 hrs</td></tr>\n"
 * placeholders:
 *     time = "17:55 hrs"
 *     link="the-link-goes-here"
 *     title="A debt-free student was found in Cornwall today authorities report"
 *
 * --------------------------------------------------------------------------------
 * <!-- element="footer" -->
 * </table>
 * <!-- element="/footer" -->
 *
 * name = "footer"
 * value = "</table>"
 *
 */
