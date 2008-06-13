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
 * ScriptWriter.java
 *
 * Created on 15 November 2001, 12:06
 */

package org.ntropa.build.jsp;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.ntropa.build.html.MarkedUpHtmlParser;

/**
 * 
 * @author jdb
 * @version $Id: ScriptWriter.java,v 1.25 2005/10/03 12:21:43 jdb Exp $
 */
public class ScriptWriter {

    // private static final String SERVER_ACTIVE_TYPE = "ServerActiveObject" ;

    private static final String VAR_PREFIX = "component_";

    private static final String ROOT_OBJECT_NAME = VAR_PREFIX + "1";

    private static final short PAGE_BUFFER_KB = 16;

    /*
     * By defining a value for '-invocation' in an application parameter file we
     * can use a debugging versions of the invocation bean at runtime
     */
    private static final String DEFAULT_INVOCATION_CLASS = "StandardInvocationContext";

    private static final String INVOCATION_NAME = "-invocation";

    private static final String INVOCATION_INTERFACE = "InvocationContext";

    private JspSerializable _rootObject;

    private int _nextName;

    private FinderSet _finderSet;

    /*
     * Prevent infinite recursion by failing when an object is handled a second
     * time.
     * 
     * Note: This is most definitely unexpected behaviour.
     */
    private List _watchDog;

    /* The debug level */
    private int _debug = 0;

    private final Charset _encoding;

    /**
     * Creates new ScriptWriter
     * 
     * @param rootObject
     *            The parent of the entire tree.
     * @param encoding
     *            The name of this <code>Charset</code> is used to set the
     *            charset for the contentType of the jsp
     * @param finderSet
     *            The <code>FinderSet</code> to use when invoking
     *            ServerActiveHtml, Element and Fragment objects.
     */
    public ScriptWriter(JspSerializable rootObject, Charset encoding, FinderSet finderSet) {

        _rootObject = rootObject;

        if (finderSet == null)
            throw new IllegalArgumentException("Attempt to construct a ScriptWriter from a null FinderSet");

        _finderSet = finderSet;

        if (encoding == null)
            throw new IllegalArgumentException("Attempt to construct a ScriptWriter from a null Charset");

        _encoding = encoding;
    }

    /**
     * Set the debug level 0 - 99
     * 
     * @param debug
     *            The required debug level in the range 0 - 99.
     */
    public void setDebugLevel(int debug) {
        /* A negative arg is a mistake; go large in response */
        _debug = debug >= 0 ? debug : 99;
    }

    /**
     * Walk the object tree and write out the JSP Script to use as the JSP.
     * 
     * PDL:
     * 
     * 1. Add import directives.
     * 
     * 2. Add instance declarations
     * 
     * 3. Add init<n> methods invoked by jspInit
     * 
     * 4. Add JSP initializer.
     * 
     * 5. Add invocation of root object.
     * 
     */
    public String getScript() throws ScriptWriterException {

        _watchDog = new LinkedList();

        _nextName = 1;
        /*
         * 1. Add import directives.
         */
        String bufferStatement = "<%@\npage buffer = \"" + PAGE_BUFFER_KB + "kb\"\n%>";
        String contentTypeStatement = "<%@\npage contentType = \"text/html;charset=" + _encoding.name() + "\"\n%>";
        // TODO: Revert to ntropa class names.
        if (false) {
            String importStatement = "<%@\npage import = \"org.ntropa.runtime.sao.*\"\n%>";
        }
        String importStatement = "<%@\npage import = \"com.studylink.sao.*\"\n%>";
        String pageDirectives = bufferStatement + contentTypeStatement + importStatement;
        /*
         * Calculate jspInit () prior to the declaration statements to get the
         * number of objects to declare.
         */
        StringBuilder declScript = new StringBuilder(getObjectCount() * 40);

        StringBuilder initScript = new StringBuilder();

        /*
         * 
         * public void jspInit() {
         * 
         * ...initialization code here ... }
         * 
         */

        /*
         * 2. Add instance declarations
         */

        /*
         * 3. Add init<n> methods invoked by jspInit. This is used to reduce
         * the chance of jspInit exceeding 64k. This is how jasper in tomcat
         * warns of an oversized jspInit method: "The code of method jspInit()
         * is exceeding the 65535 bytes limit"
         */
        declScript.append("<%!\n");
        declScript.append("private java.util.Properties pageProperties ;\n");
        initScript.append("<%!\n");
        int initMethodCount = getInitializerScripts(_rootObject, null, declScript, initScript, 0, 0);
        declScript.append("%>");
        initScript.append("%>");

        /*
         * 4. Add JSP initializer.
         */
        initScript.append("<%!\npublic void jspInit () {\n");
        getPageProperties(_finderSet.getApplicationFinder(), initScript);
        for (int i = 1; i <= initMethodCount; i++) {
            initScript.append(getStatementSplitterMethodName(i) + " () ;\n");
        }
        initScript.append("}\n%>");

        /*
         * 5. Add invocation of root object.
         */
        String invocationBeanClass = null; // _finderSet.getApplicationFinder
        // ().getClazzName ( INVOCATION_NAME
        // ) ;
        Properties saoData = _finderSet.getApplicationFinder().getSaoData(INVOCATION_NAME);
        if (saoData != null) {
            invocationBeanClass = saoData.getProperty(ApplicationFinder.CLASS_NAME_PROPNAME);
            if (invocationBeanClass == null)
                throw new ScriptWriterException(
                        "Failed to get a class name for the root object despite having a non-null Properties object for the sao data");
        } else
            invocationBeanClass = DEFAULT_INVOCATION_CLASS;

        StringBuilder invocation = new StringBuilder(1000);

        invocation.append("<%");

        invocation.append("\n" + INVOCATION_INTERFACE + " invocationBean = new " + invocationBeanClass + " () ;");
        invocation.append("\n");

        invocation.append("\ninvocationBean.setPageContext ( pageContext ) ;");
        invocation.append("\ninvocationBean.setHttpSession ( session ) ;");
        invocation.append("\ninvocationBean.setServletContext ( application ) ;");
        invocation.append("\ninvocationBean.setServletConfig ( config ) ;");
        invocation.append("\ninvocationBean.setJspWriter ( out ) ;");
        invocation.append("\ninvocationBean.setPage ( page ) ;");
        invocation.append("\ninvocationBean.setHttpServletRequest ( request ) ;");
        invocation.append("\ninvocationBean.setHttpServletResponse ( response ) ;");
        invocation.append("\n");
        invocation.append("\n/* While in the control phase no output should be written */");
        invocation.append("\ninvocationBean.enableControlPhase () ;");
        invocation.append("\n");
        // TODO: Revert to ntropa class names.
        if (false) {
            invocation
                    .append("\norg.ntrop.runtime.sao.util.PageProperties.jspService ( invocationBean, pageProperties ) ;");
        }
        invocation.append("\ncom.studylink.sao.util.PageProperties.jspService ( invocationBean, pageProperties ) ;");
        invocation.append("\n");
        invocation.append("\n" + ROOT_OBJECT_NAME + ".control ( invocationBean ) ;");
        invocation.append("\n");
        invocation.append("\nif ( invocationBean.getController ().proceed () ) {");
        invocation.append("\n    invocationBean.enableRenderPhase () ;");
        invocation.append("\n    " + ROOT_OBJECT_NAME + ".render ( invocationBean ) ;");
        invocation.append("\n}");
        invocation.append("\n");
        invocation.append("\ninvocationBean.disable () ;");
        invocation.append("\n");
        invocation.append("\n" + ROOT_OBJECT_NAME + ".recycle () ;");

        invocation.append("\n%>");

        /*
         * Add a JSP comment of the object hierarchy outline
         */
        StringBuilder outline = new StringBuilder();
        outline.append("<%--\nOutline of object tree:\n\n");

        MarkedUpHtmlParser.getObjectTree(_rootObject, outline);

        // outline.append ("\n\nWarning: This structural outline print out is
        // incomplete. It includes only the top level." ) ;
        // outline.append ("\nWarning: The actual JSP is complete and has been
        // created correctly.");
        outline.append("\n--%>");

        return pageDirectives + declScript.toString() + initScript.toString() + invocation.toString() + "\n" + outline;

    }

    private String getStatementSplitterMethodHeader(int i) {
        return "private void " + getStatementSplitterMethodName(i) + " () {";
    }

    private String getStatementSplitterMethodName(int i) {
        return "init" + i;
    }

    /* Methods larger than 65535 do not compile */
    private static final int IDEAL_MAX_METHOD_SIZE = 20000;

    private int _initScriptSizeAtStartOfInitNMethod;

    /**
     * This method creates the statements to initialize and link the
     * <code>ServerActiveObject</code>s which will be invoked to control the
     * page flow and write the HTML of the JSP.
     * 
     * PDL:
     * 
     * 1. For each object: Write object declaration and creation. Optionally
     * write code to initialise object: placeholders for server active objects
     * and elements property setters for server active objects values for
     * Fragment Add self to parent Write all children
     * 
     * @param obj
     *            The object to write the code for
     * 
     * @param parentObjectname
     *            The name of the object reference to use as the current
     *            object's parent.
     * 
     * @param declScript
     *            The <code>StringBuffer</code> to append the declaration
     *            script to
     * @param initScript
     *            The <code>StringBuffer</code> to append the initialisation
     *            script to
     * 
     * @return The number of init<n> methods that where created.
     */
    private int getInitializerScripts(JspSerializable obj, String parentObjectname, StringBuilder declScript,
            StringBuilder initScript, int invocationDepth, int initMethodCount) throws ScriptWriterException {
        
        // /* Check for cycles to stop the JVM from coming down */
        // Integer objId = new Integer ( System.identityHashCode ( obj ) ) ;
        // if ( _watchDog.contains ( objId ) )
        // throw new ScriptWriterException ( "The object tree had a cycle:\n" +
        // obj ) ;
        // _watchDog.add ( objId ) ;

        /* Check for cycles to stop the JVM from coming down */
        for (Iterator iter = _watchDog.iterator(); iter.hasNext();) {
            Object earlierObject = (Object) iter.next();
            /* Use identity comparison not value comparison */
            if (obj == earlierObject)
                throw new ScriptWriterException("The object tree had a cycle:\n" + obj);
        }
        _watchDog.add(obj);

        /*
         * This handles an object which is a List. An object of this type may be
         * added during the resolution of templates by a <code>MarkedUpHtmlParser</code>
         */
        /*
         * 02-3-5 if ( obj instanceof JspSerializable-List ) { throw new
         * ScriptWriterException ( "getInitializerScripts was invoked with an
         * instance of JspSerializable-List." + " This should not have
         * happened." ) ; /* Commented out after changes in MarkedUpHtmlParser
         * to no longer use a specialized List. Iterator it = (
         * (JspSerializable-List) obj).iterator () ; while ( it.hasNext () ) {
         * JspSerializable jspSer = (JspSerializable) it.next () ;
         * getInitializerScripts ( jspSer, parentObjectname, declScript,
         * initScript ) ; } return ; / }
         */

        if (invocationDepth == 0) {
            _initScriptSizeAtStartOfInitNMethod = initScript.length();
            initMethodCount++;
            initScript.append(getStatementSplitterMethodHeader(initMethodCount) + "\n");
        }

        /*
         * Write the initialiser for this object.
         */
        String objName = nextName();
        log(2, "Handling object name: " + objName);
        log(2, "Object: " + obj.getClass().getName() + ": " + System.identityHashCode(obj));

        /* private AbstractElement component_13 ; */
        declScript.append("private " + obj.getComponentTypeName() + " " + objName + " ;\n");
        log(2, "obj.getComponentTypeName() " + obj.getComponentTypeName());

        /*
         * At this point the binding between the name of the ServerActiveHtml
         * object and the name of the class implementing the SAH needs to be
         * resolved.
         * 
         * Because 'obj' could be a <code>ServerActiveHtml</code>, a <code>Element</code>
         * or a <code>Fragment</code> we pass all Finders via the FinderSet
         * object.
         */
        /* component_13 = new org.ntropa.build.sao.ChannelDate () ; */

        /*
         * Write the code to set up the object
         */
        obj.getSetUpCode(objName, initScript, _finderSet);
        if (_debug >= 2)
            log("obj.getSetUpCode() invoked (last 100 chars shown): ..."
                    + initScript.toString().substring(Math.max(0, initScript.length() - 100)) + "...");

        /*
         * Write the code to link the object to the current container.
         * 
         * The child will invoke 'getPlaceholderReplacement' on the container to
         * resolve any unhandled placeholder replacement messages at http-serve
         * time.
         * 
         * The container will invoke the child to get control and HTML rendering
         * services.
         */
        if (parentObjectname != null) {
            initScript.append(parentObjectname + ".addChild ( " + objName + " ) ;\n");
        }

        /*
         * Invoke self to write the code to add all the children
         */

        Iterator it = obj.getChildren().iterator();
        while (it.hasNext()) {
            Object childObj = it.next();
            // System.out.println ("[getInitializerScript] class: " +
            // childObj.getClass () +
            // " " + System.identityHashCode ( childObj ) );
            JspSerializable child = (JspSerializable) childObj;
            initMethodCount = getInitializerScripts(child, objName, declScript, initScript, invocationDepth + 1,
                    initMethodCount);

            if (initScript.length() - _initScriptSizeAtStartOfInitNMethod >= IDEAL_MAX_METHOD_SIZE) {
                _initScriptSizeAtStartOfInitNMethod = initScript.length();
                initScript.append("}\n");
                initMethodCount++;
                initScript.append(getStatementSplitterMethodHeader(initMethodCount) + "\n");
            }

        }

        /* Finish last init<n> method */
        if (invocationDepth == 0) {
            initScript.append("}\n");
        }

        return initMethodCount;
    }

    private String nextName() {
        return VAR_PREFIX + _nextName++;
    }

    /**
     * Returns the number of objects that code was written for.
     */
    private int getObjectCount() {
        return _nextName - 1;
    }

    /**
     * FIXME: use proper logger passed in at construction.
     */
    private void log(String msg) {
        System.out.println("[" + this.getClass().getName() + "] " + msg);
    }

    private void log(int level, String msg) {
        if (_debug >= level)
            log(msg);
    }

    private void getPageProperties(ApplicationFinder af, StringBuilder initScript) {

        initScript.append("pageProperties = new java.util.Properties () ;\n");

        if (af.isProxyCacheDisable())
            initScript.append("pageProperties.setProperty ( \"proxy.cache.disable\", \"yes\" ) ;\n");

    }
}
