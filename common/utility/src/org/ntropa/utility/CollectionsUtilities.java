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
 * CollectionsUtilities.java
 *
 * Created on 29 November 2001, 12:57
 */

package org.ntropa.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections.Transformer;

/**
 *
 * @author  jdb
 * @version $Id: CollectionsUtilities.java,v 1.20 2005/07/15 12:06:49 jdb Exp $
 */
public class CollectionsUtilities {

	public static final int CASE_INSENSITIVE = 1;

	/**
	 * Lookup the value of a name.
	 *
	 * The current format is ultra-simple.
	 *
	 * @param paramFile a <code>File</code> object to get the properties from
	 * @param name The name of the property to find
	 */
	public static String lookupName(File paramFile, String name) throws IOException {

		Properties p = new Properties();
		p.load(new FileInputStream(paramFile));

		return p.getProperty(name);
	}

	/**
	 * Lookup the value of a name.
	 *
	 * The current format is ultra-simple.
	 *
	 * @param paramFile a <code>File</code> object to get the properties from
	 * @param name The name of the property to find
	 * @param flags A set of options.
	 */
	// public static String lookupName ( File paramFile, String name, int flags ) throws IOException {
	public static String lookupNameCaseInsensitive(File paramFile, String name) throws IOException {
		/*
		  if ( flags == CASE_INSENSITIVE )
		      throw new IllegalArgumentException ( "Bad option: " + flags ) ;
		 */
		Properties p = new Properties();
		p.load(new FileInputStream(paramFile));

		Iterator it = p.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();

			if (key.equalsIgnoreCase(name))
				return p.getProperty(key);

		}
		return null;
	}

	/**
	 * Given a <code>Properties</code> object return a new <code>Properties</code> object
	 * which contains a subset of the super set with the prefix removed from the key name.
	 *
	 * Given
	 *  a-1: val-1
	 *  a-12: val-2
	 *  b-ff: val-3
	 *
	 * and the prefix 'a-' the returned <code>Properties</code> object will have these entries
	 *
	 *  1: val-1
	 *  12: val-2
	 *
	 * Given
	 *  af: val-1
	 *
	 * and the prefix 'af', null with be returned.
	 *
	 * If no suitably prefixed keys are found null is return
	 *
	 * @param superset The <code>Properties</code> object to look for prefixed keys in
	 * @param prefix The prefix to look for
	 * @return A new <code>Properties</code> object or null
	 */
	public static Properties getPropertiesSubset(Properties superset, String prefix) {

		if (superset == null)
			throw new IllegalArgumentException("Null Properties argument (superset)");

		if (prefix == null)
			throw new IllegalArgumentException("Null string argument (prefix)");

		if (prefix.length() == 0)
			throw new IllegalArgumentException("Zero-length string argument (prefix)");

		Properties subset = null;
		Iterator it = superset.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			if (key.length() <= prefix.length())
				continue;
			if (!key.startsWith(prefix))
				continue;

			if (subset == null)
				subset = new Properties();
			subset.setProperty(StringUtilities.removePrefix(key, prefix), superset.getProperty(key));

		}

		return subset;
	}

	/**
	 * @param c A <code>Collection<code> to test
	 * @return true if every element is a non zero length string
	 */
	public static boolean allElementsAreNonZeroLengthStrings(Collection c) {

		if (c.size() == 0)
			return true;

		Iterator it = c.iterator();
		while (it.hasNext()) {
			try {
				String s = (String) it.next();

				if (s == null)
					return false;

				if (s.length() == 0)
					return false;

			} catch (ClassCastException e) {
				return false;
			}

		}
		return true;

	}

	/**
	 * Given a list of items split it into one or more sublists according to how many other lists
	 * each item is contained in.
	 * <p>
	 * Each item in targetList is tested for containment by each of the lists in listOfContainerLists.
	 * The count of containing lists for the item determines which sublist the item is appended to. The
	 * partitioning by containment count is stable in the sense that the items in each sublist occur in the same
	 * order as they appeared in the targetList.
	 * <p>
	 * For example:
	 * <pre>
	 * targetList: { 1, 2, 3, 4, 5, 6 }
	 * listOfContainerLists: { { 1, 2, 3, 4 }, { 3, 4, 5 }, { 3, 5 } }
	 * result: { { 6 }, { 1, 2 }, { 4, 5 }, { 3 } }
	 * </pre>
	 * In this example 6 is in zero lists, 1, 2 are in exactly one list, 4, 5 are in exactly two lists,
	 * and 3 is in exactly three lists.
	 * <p>
	 * The returned list always has n + 1 items where n is the size of listOfContainerLists.
	 * Example with gap:
	 * <pre>
	 * targetList: { A, B }
	 * listOfContainerLists: { { A, B }, { B }, { B } }
	 * result: { {}, { A }, {}, { B } }
	 * </pre>
	 * In this example there are no items which do not occur in any list and no items which
	 * occur in exactly 2 lists.
	 * @param targetList A <code>List</code> of items to partition
	 * @param listOfContainerLists A <code>List</code> of lists to use for determining which sublist
	 * to put each item in.
	 * @return A <code>List</code> of <code>List</code>s where the list at index 0 contains all the items
	 * of targetList which are contained in 0 of the lists in listOfContainerLists, and where the list
	 * at index n contains all the items of targetList which are contained in n of the lists on listOfContainerLists.
	 */
	public static List makePartitionedListByContainmentCount(List targetList, List listOfContainerLists) {

		if (targetList == null)
			throw new IllegalArgumentException("targetList was null");

		if (listOfContainerLists == null)
			throw new IllegalArgumentException("listOfContainerLists was null");

		ArrayList[] result = new ArrayList[listOfContainerLists.size() + 1];
		for (int i = 0; i <= listOfContainerLists.size(); i++)
			result[i] = new ArrayList();

		/* Convert container lists to HashSet for containment test speed */
		Set[] containers = new Set[listOfContainerLists.size()];
		for (int i = 0; i < listOfContainerLists.size(); i++) {
			List c = (List) listOfContainerLists.get(i);
			if (c == null)
				throw new IllegalArgumentException("The container list at index " + i + " was null");
			containers[i] = new HashSet(c);
		}

		/* Do the partitioning */
		int count;
		for (Iterator itemIt = targetList.iterator(); itemIt.hasNext();) {
			Object item = itemIt.next();
			count = 0;

			for (int containerIX = 0; containerIX < containers.length; containerIX++)
				if (containers[containerIX].contains(item))
					count++;

			result[count].add(item);
		}

		for (int i = 0; i < result.length; i++)
			result[i].trimToSize();

		return Arrays.asList(result);
	}

	/**
	 * Make a list from a Map of Lists, an Iterator of keys for the Map and a List
	 * of objects to restrict to.
	 * <p>
	 * This method is useful for ranking result lists of ids.
	 * <p>
	 * <pre>
	 * 'HashSet' note.
	 *
	 * Tested contains on a 5000 element collections
	 *
	 *      THashSet (trove)
	 *      HashSet
	 *      TreeSet
	 *
	 * HashSet gave the best performance.
	 *
	 * Initially the contains test was done on the course id list.
	 * This was a school boy error and switching to a set dramatically
	 * reduced ranking time.
	 *
	 * Ranking times in milliseconds for 10007 courses
	 *
	 * ArrayList         HashSet
	 *
	 *  19908               106
	 *  17545                87
	 *  17822                57
	 * </pre>
	 *
	 * @param mapOfLists a <code>Map</code> of <code>Lists</code>s.
	 * @param keyIterator an <code>Iterator</code> used to obtain the Lists in mapOfLists in a certain order. It is
	     * an error for <code>keyIterator</code> to provide a key which is not in <code>mapOfLists</code>.
	 * @param restrictionList a <code>List</code> of objects. Every object encountered in a List from the Map
	 * must be in this List in order to be included in the resulting List.
	 * @return A <code>List</code> of objects which
	 * <ul>
	 * <li>Are in the restrictionList
	 * <li>Are in the same order as the order obtained by iterating over each List in the mapOfLists in the order
	 * of Lists given by keyIterator
	 */
	public static List makeListFromMapOfListsAndKeyIteratorWithRestrictingSublist(
		Map mapOfLists,
		Iterator keyIterator,
		List restrictionList) {
		/*
		 * See 'HashSet' note in class documentation.
		 */
		Set restrictionSet = new HashSet(restrictionList);

		ArrayList returnList = new ArrayList(restrictionList.size());

		for (; keyIterator.hasNext();) {

			List aList = (List) mapOfLists.get(keyIterator.next());

			for (Iterator o = aList.iterator(); o.hasNext();) {

				Object candidateObject = o.next();
				if (restrictionSet.contains(candidateObject))
					returnList.add(candidateObject);

			}
		}

		returnList.trimToSize();

		return returnList;
	}

	public static String convertToCommaSeperatedList(Collection coll) {
		return convertToSeperatedList(coll.iterator(), ",");
	}

	public static String convertToSeperatedList(Collection coll, String seperator) {

		if (coll == null)
			throw new IllegalArgumentException("Collection was null");

		return convertToSeperatedList(coll.iterator(), seperator);
	}

	public static String convertToCommaSeperatedList(Iterator it) {
		return convertToSeperatedList(it, ",");
	}

	public static String convertToSeperatedList(Iterator it, String seperator) {

		if (it == null)
			throw new IllegalArgumentException("Iterator was null");

		String item = "";

		StringBuffer itemList = new StringBuffer();

		while (it.hasNext()) {

			item = (String) it.next();
			if (item.indexOf(seperator) != -1)
				throw new IllegalArgumentException("item contained seperator (" + seperator + "): " + item);
			itemList.append(item);

			itemList.append(seperator);
		}

		if (itemList.length() > 0)
			itemList.delete(itemList.length() - seperator.length(), itemList.length());

		return itemList.toString();

	}

	/**
	 * Return a string like
	 * 
	 * 	item1
	 *  item1 or item2
	 *  item1, item2 or item3
	 *
	 * @param coll
	 * @param separator The string to use for all separators other than the final separator.
	 * @param finalSeparator The string to use as the final separator
	 * @return
	 */
	public static String convertToSeparatedList(Collection coll, String separator, String finalSeparator) {

		if (coll == null)
			throw new IllegalArgumentException("Collection was null");

		if (coll.size() == 1)
			return "" + coll.iterator().next();

		int last = coll.size();
		int current = 1;
		StringBuffer result = new StringBuffer();
		for (Iterator iter = coll.iterator(); iter.hasNext(); current++) {
			String item = (String) iter.next();
			result.append(item);
			if (current < last)
				result.append(current == last - 1 ? finalSeparator : separator);
		}
		return result.toString();
	}

	/**
	 * Return a new set containing the distinguishing prefixes of a set of <code>String</code>
	 * <p>
	 * The distinguishing prefixes of a set of strings are defined as the shortest prefixes of
	 * the set of strings that are pairwise different.
	 */
	public static SortedSet getDistinguishingPrefixes(Set inSet) {

		SortedSet result = new TreeSet();

		if (inSet.size() <= 1) {
			result.addAll(inSet);
			return result;
		}

		Iterator in = new TreeSet(inSet).iterator();
		String last = (String) in.next();
		result.add(last);
		do {
			String current = (String) in.next();
			if (current.startsWith(last))
				continue;

			last = current;
			result.add(last);
		} while (in.hasNext());

		return result;
	}

	/**
	 * Given a list and a way to change each item into 2 or more items, change every item in the list until
	 * no more changes can be done.
	 * <p>
	 * Example. Suppose the transformer changes strings which are two or more characters long into two strings,
	 * one which is the intial character and a second which is the remainder of the string. Suppose the list items
	 * has one item &quot;ABCD&quot; .Then invoking
	 * expandList ( items, transformer ) results in this list:
	 * <p>
	 * <pre>
	 * A
	 * B
	 * C
	 * D
	 * </pre>
	 * <p>
	 * The method works by repeatedly transforming the first item in the list until the <code>Transformer</code>
	 * returns null. Then a sublist, excluding the first item is processed recursively.
	 *
	 * @param itemList A <code>List</code> of items which can be changed into two or more items by itemTransformer
	 * @param itemTransformer A <code>Transformer</code> which knows how to change an item into two or more new items.
	 * If the item can not be changed then itemTransformer should return null otherwise a <code>List</code> with
	 * the new items
	 */
	public static void expandList(List itemList, Transformer itemTransformer) {

		if (itemList == null)
			throw new IllegalArgumentException("itemList was null");

		if (itemTransformer == null)
			throw new IllegalArgumentException("itemTransformer was null");

		if (itemList.size() == 0)
			return;

		for (List changed = (List) itemTransformer.transform(itemList.get(0));
			changed != null;
			changed = (List) itemTransformer.transform(itemList.get(0))) {
			/* replace first element with new items */
			itemList.remove(0);

			for (int newItemsIX = changed.size() - 1; newItemsIX >= 0; newItemsIX--)
				itemList.add(0, changed.get(newItemsIX));

		}

		if (itemList.size() > 1)
			expandList(itemList.subList(1, itemList.size()), itemTransformer);

	}
}
