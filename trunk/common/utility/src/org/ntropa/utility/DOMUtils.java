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
 * DOMUtils.java
 *
 * Created on 14 December 2001, 11:41
 */

package org.ntropa.utility;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Utitlities for DOM.
 *
 * @author  jdb
 * @version $Id: DOMUtils.java,v 1.2 2002/09/09 15:52:47 jdb Exp $
 */
public class DOMUtils {
    
    private static final String INDENT = "  " ;
    
    /**
     * Returns out a DOM tree
     *
     * @param theNode The <tt>Node</tt> to print out.
     * @return the DOM tree as a <tt>String<tt> suitable for human comprehension.
     */
    public static String toString ( Node theNode ) {
        StringBuffer sb = new StringBuffer () ;
        toString ( theNode, "", sb ) ;
        return sb.toString () ;
    }
    /**
     * Prints out a DOM tree
     *
     * @param theNode The <tt>Node</tt> to print out.
     * @param sb A <tt>StringBuffer</tt> to accumulate the result in.
     */
    public static void toString ( Node theNode, String indent, StringBuffer sb ) {
        
        switch ( theNode.getNodeType () ) {
            
            case Node.DOCUMENT_TYPE_NODE:
                DocumentType docType = (DocumentType) theNode ;
                
                sb.append (
                "\n" + indent + "Document type node: " + docType.getName () ) ; // notations etc missed out
                break ;
                
            // Process a Document node
            case Node.DOCUMENT_NODE:
                Document doc = (Document) theNode ;
                
                sb.append (
                indent + "Document node: " + doc.getNodeName () +
                "\n" + indent + "Root element: " +
                doc.getDocumentElement ().getNodeName () );
                processChildNodes ( doc.getChildNodes (), indent + INDENT, sb ) ;
                break ;
                
            case Node.COMMENT_NODE:
                Comment comment = (Comment) theNode ;
                sb.append ("\n" + indent + "Comment: " + comment.getData () );
                break ;
                
                // Process an Element node
            case Node.ELEMENT_NODE:
                sb.append (
                "\n" + indent + "Element node: " + theNode.getNodeName () );
                NamedNodeMap attributeNodes = theNode.getAttributes () ;
                
                for ( int i = 0; i < attributeNodes.getLength () ; i++ ) {
                    Attr attribute = (Attr) attributeNodes.item ( i ) ;
                    
                    sb.append (
                    "\n" + indent + "Attribute: " + attribute.getNodeName () +
                    " ; Value = " +  attribute.getNodeValue ()
                    );
                }
                
                processChildNodes ( theNode.getChildNodes (), indent + INDENT, sb ) ;
                break ;
                
                // process a text node and a CDATA section
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                Text text = (Text) theNode ;
                
                if ( ! theNode.getNodeValue ().trim ().equals ( "" ) )
                    sb.append ("\n" + indent + "Text: " + text.getNodeValue ()  );
                break ;
                
            default:
                sb.append ("\n" + indent + "Unhandled Node type: " + theNode.getClass ().getName () + ": " + theNode.getNodeValue () );
                break ;
                
                
        } // switch
    }
    
    private static void processChildNodes ( NodeList children, String indent, StringBuffer sb ) {
        if ( children.getLength () != 0 )
            for ( int i = 0 ; i < children.getLength () ; i++ )
                toString ( children.item ( i ), indent, sb ) ;
    }
    
}
