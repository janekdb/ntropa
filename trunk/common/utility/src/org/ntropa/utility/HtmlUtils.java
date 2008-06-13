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
 * HtmlUtils.java
 *
 * Created on 6 February 2002, 12:00
 */

package org.ntropa.utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.SequencedHashMap;

/**
 * Changes to be made:
 *
 * 4. Handle the remaining characters from the MacRoman charset. i.e. e acute etc. ( DON'T UNDERSTAND WHAT TO DO )
 *
 */

/**
 * &lt;Add a description here&gt;
 *
 * @author  Abhishek Verma
 * @version $Id: HtmlUtils.java,v 1.19 2004/11/15 16:15:37 jdb Exp $
 */
public class HtmlUtils {
	private static HashMap UnicodeCharacters = new HashMap();

	static {
		UnicodeCharacters.put("&", "&amp;");
		UnicodeCharacters.put("<", "&lt;");
		UnicodeCharacters.put(">", "&gt;");
		UnicodeCharacters.put("\"", "&quot;");
		UnicodeCharacters.put("\n", "<br>");
		UnicodeCharacters.put("à", "&agrave;");
		UnicodeCharacters.put("À", "&Agrave;");
		UnicodeCharacters.put("â", "&acirc;");
		UnicodeCharacters.put("Â", "&Acirc;");
		UnicodeCharacters.put("ä", "&auml;");
		UnicodeCharacters.put("Ä", "&Auml;");
		UnicodeCharacters.put("å", "&aring;");
		UnicodeCharacters.put("Å", "&Aring;");
		UnicodeCharacters.put("æ", "&aelig;");
		UnicodeCharacters.put("Æ", "&AElig;");
		UnicodeCharacters.put("ç", "&ccedil;");
		UnicodeCharacters.put("Ç", "&Ccedil;");
		UnicodeCharacters.put("é", "&eacute;");
		UnicodeCharacters.put("É", "&Eacute;");
		UnicodeCharacters.put("è", "&egrave;");
		UnicodeCharacters.put("È", "&Egrave;");
		UnicodeCharacters.put("ê", "&ecirc;");
		UnicodeCharacters.put("Ê", "&Ecirc;");
		UnicodeCharacters.put("ë", "&euml;");
		UnicodeCharacters.put("Ë", "&Euml;");
		UnicodeCharacters.put("ï", "&iuml;");
		UnicodeCharacters.put("Ï", "&Iuml;");
		UnicodeCharacters.put("ô", "&ocirc;");
		UnicodeCharacters.put("Ô", "&Ocirc;");
		UnicodeCharacters.put("ö", "&ouml;");
		UnicodeCharacters.put("Ö", "&Ouml;");
		UnicodeCharacters.put("ø", "&oslash;");
		UnicodeCharacters.put("Ø", "&Oslash;");
		UnicodeCharacters.put("ß", "&szlig;");
		UnicodeCharacters.put("ù", "&ugrave;");
		UnicodeCharacters.put("Ù", "&Ugrave;");
		UnicodeCharacters.put("û", "&ucirc;");
		UnicodeCharacters.put("Û", "&Ucirc;");
		UnicodeCharacters.put("ü", "&uuml;");
		UnicodeCharacters.put("Ü", "&Uuml;");
		UnicodeCharacters.put("®", "&reg;");
		UnicodeCharacters.put("©", "&copy;");
		UnicodeCharacters.put("\u20AC", "&euro;"); //unicode escape sequence
	}

	public static String convertToHtml(String s) {
		StringBuffer strb = new StringBuffer();

		for (int x = 0; x <= s.length() - 1; x++) {

			if (UnicodeCharacters.get(s.substring(x, x + 1)) != null) {
				strb.append((String) UnicodeCharacters.get(s.substring(x, x + 1)));
			} else {
				strb.append(s.substring(x, x + 1));
			}
		}

		return strb.toString();
	} // End of method convert

	/**
	 * Replaces a HTML document head section and keeps the body section
	 * @param newHtmlDoc String containing the new head.
	 * @param oldHtmlDoc String containing the original head and body
	 * @return String containing new head and the original body
	 */

	public static String replaceHead(String newHtmlDoc, String oldHtmlDoc) {

		String tag = "</head>";

		int newHeadLoc = newHtmlDoc.toLowerCase().indexOf(tag);

		if (newHeadLoc == -1) {
			throw new IllegalArgumentException("New HTML document did not contain a head closing tag: " + tag);
		}

		int oldHeadLoc = oldHtmlDoc.toLowerCase().indexOf(tag);

		if (oldHeadLoc == -1) {
			throw new IllegalArgumentException("Old HTML document did not contain a head closing tag: " + tag);
		}

		return newHtmlDoc.substring(0, newHeadLoc + tag.length()) + oldHtmlDoc.substring(oldHeadLoc + tag.length());

	}

	public static String replaceHead(String newHtmlDoc, StringBuffer oldHtmlDoc) {

		return replaceHead(newHtmlDoc, oldHtmlDoc.toString());
	}

	public static String replaceHead(StringBuffer newHtmlDoc, String oldHtmlDoc) {

		return replaceHead(newHtmlDoc.toString(), oldHtmlDoc);
	}

	public static String replaceHead(StringBuffer newHtmlDoc, StringBuffer oldHtmlDoc) {

		return replaceHead(newHtmlDoc.toString(), oldHtmlDoc.toString());
	}

	/**
	 * @return A &lt;optgroup&gt; tag with label as the value of the label attribute
	 */
	public static String optgroupOpen(String label) {

		if (label == null)
			throw new IllegalArgumentException("label was null");

		return "<optgroup label=\"" + label + "\">";
	}

	/**
	 * @return A &lt;/optgroup&gt; tag.
	 */
	public static String optgroupClose() {

		return "</optgroup>";
	}

	/*
	 * Return a rendered HTML Select List
	 * 
	 * Probably not the correct place to put this, but it would be handy to have this helper method
	 * somewhere :-)
	 */
	public static String renderSelectList(SequencedHashMap itemsMap, String selected, String name) {

		StringBuffer buf = new StringBuffer();
		String itemsKey = "";
		String itemsValue = "";

		buf.append("<select name=\"" + name + "\">\n");

		buf.append(makeOptionList(itemsMap, selected));

		buf.append("</select>");

		return buf.toString();
	}

	/**
	  * Return the text of the options for a HTML Select List
	  * 
	  */
	public static String makeOptionList(SequencedHashMap itemsMap, String selected) {

		return makeOptionList(itemsMap, new String[] { selected });

	}

	/**
	  * Return the text of the options for a HTML Select List
	  * 
	  */
	public static String makeOptionList(SequencedHashMap itemsMap, String selected[]) {

		StringBuffer buf = new StringBuffer();
		String itemsKey = "";
		String itemsValue = "";

		Set selectedSet = new HashSet();
		for (int i = 0; i < selected.length; i++) {
			selectedSet.add(selected[i]);
		}

		for (Iterator it = itemsMap.iterator(); it.hasNext();) {
			itemsKey = (String) it.next();
			itemsValue = (String) itemsMap.get(itemsKey);
			buf.append("<option value=\"" + itemsKey + "\"");
			if (selectedSet.contains(itemsKey))
				buf.append(" selected");
			buf.append(">" + itemsValue + "</option>\n");
		}

		return buf.toString();
	}

}
