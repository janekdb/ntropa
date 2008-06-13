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
 * CollectionsUtilitiesTest.java
 *
 * Created on 18 January 2002, 11:14
 */

package tests.org.ntropa.utility;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.collections.Transformer;
import org.ntropa.utility.CollectionsUtilities;


/**
 *
 * @author  jdb
 * @version $Id: CollectionsUtilitiesTest.java,v 1.10 2004/11/12 15:52:09 jdb Exp $
 */
public class CollectionsUtilitiesTest extends TestCase {

	public CollectionsUtilitiesTest(String testName) {
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
	public static Test suite() {

		TestSuite suite = new TestSuite(CollectionsUtilitiesTest.class);
		return suite;
	}

	public void testGetPropertiesSubset() {

		try {
			CollectionsUtilities.getPropertiesSubset(null, "");
			fail("Null arg not detected (1)");
		} catch (IllegalArgumentException e) {
		}
		try {
			CollectionsUtilities.getPropertiesSubset(new Properties(), null);
			fail("Null arg not detected (2)");
		} catch (IllegalArgumentException e) {
		}
		try {
			CollectionsUtilities.getPropertiesSubset(new Properties(), "");
			fail("Zero-length arg not detected (3)");
		} catch (IllegalArgumentException e) {
		}

		Properties fixture = new Properties();

		Properties expected = null;

		assertEquals(
			"The degenerative case was not correct",
			expected,
			CollectionsUtilities.getPropertiesSubset(fixture, "a"));

		fixture.setProperty("a", "1");
		assertEquals(
			"The only-property-is-the-prefix case was not correct",
			expected,
			CollectionsUtilities.getPropertiesSubset(fixture, "a"));

		fixture = new Properties();
		fixture.setProperty("prefix-A", "A-val");
		fixture.setProperty("prefix-B", "B-val");
		expected = new Properties();
		expected.setProperty("A", "A-val");
		expected.setProperty("B", "B-val");
		assertEquals(
			"The minimum interesting case was not correct",
			expected,
			CollectionsUtilities.getPropertiesSubset(fixture, "prefix-"));

		fixture = new Properties();
		fixture.setProperty("prefix-A", "A-val");
		fixture.setProperty("prefix-B", "B-val");
		fixture.setProperty("prefix-A-A", "A-A-val");
		fixture.setProperty("prefix-B-B", "B-B-val");
		fixture.setProperty("prefi-A", "Not seen");
		fixture.setProperty("other-B", "Not seen");
		expected = new Properties();
		expected.setProperty("A", "A-val");
		expected.setProperty("B", "B-val");
		expected.setProperty("A-A", "A-A-val");
		expected.setProperty("B-B", "B-B-val");
		assertEquals(
			"The medium interesting case was not correct",
			expected,
			CollectionsUtilities.getPropertiesSubset(fixture, "prefix-"));

	}

	public void testAllElementsAreNonZeroLengthStrings() {

		if (!CollectionsUtilities.allElementsAreNonZeroLengthStrings(Collections.EMPTY_LIST))
			fail("EMPTY_LIST was okay");
		if (!CollectionsUtilities.allElementsAreNonZeroLengthStrings(Collections.EMPTY_SET))
			fail("EMPTY_SET was okay");

		List l = Arrays.asList(new String[] { "A", "B", "C" });
		if (!CollectionsUtilities.allElementsAreNonZeroLengthStrings(l))
			fail("EMPTY_LIST was okay");

		l = Arrays.asList(new String[] { "A", "", "C" });
		if (CollectionsUtilities.allElementsAreNonZeroLengthStrings(l))
			fail("empty string caused rejection");

		l = Arrays.asList(new String[] { "A", null, "C" });
		if (CollectionsUtilities.allElementsAreNonZeroLengthStrings(l))
			fail("null string caused rejection");

		l = new LinkedList();
		l.add(new Integer(5));
		if (CollectionsUtilities.allElementsAreNonZeroLengthStrings(l))
			fail("non String type caused rejection");

	}

	public void testMakePartitionedListByContainmentCount() {

		List targetList = Collections.EMPTY_LIST;

		List listOfLists = Collections.EMPTY_LIST;

		try {
			CollectionsUtilities.makePartitionedListByContainmentCount((List) null, (List) null);
			fail("null targetList and null listOfLists was rejected");
		} catch (IllegalArgumentException e) {
		}

		try {
			CollectionsUtilities.makePartitionedListByContainmentCount(targetList, (List) null);
			fail("null listOfLists was rejected");
		} catch (IllegalArgumentException e) {
		}

		try {
			CollectionsUtilities.makePartitionedListByContainmentCount((List) null, listOfLists);
			fail("null targetList was rejected");
		} catch (IllegalArgumentException e) {
		}

		List actual = CollectionsUtilities.makePartitionedListByContainmentCount(targetList, listOfLists);
		List expected = Collections.singletonList(Collections.EMPTY_LIST);

		assertEquals("Empty target list and empty list of lists gave list of empty list", expected, actual);

		/* All items in list 0 */
		targetList = Arrays.asList(new String[] { "A", "B", "C" });
		listOfLists = Collections.EMPTY_LIST;

		actual = CollectionsUtilities.makePartitionedListByContainmentCount(targetList, listOfLists);
		expected = new LinkedList();
		expected.add(Arrays.asList(new String[] { "A", "B", "C" }));

		assertEquals("3 item target list was partitioned into list 0)", expected, actual);

		/* All items in list 1 */
		targetList = Arrays.asList(new String[] { "A", "B", "C" });
		listOfLists = new LinkedList();
		listOfLists.add(Arrays.asList(new String[] { "A", "B", "C" }));

		actual = CollectionsUtilities.makePartitionedListByContainmentCount(targetList, listOfLists);
		expected = new LinkedList();
		expected.add(Collections.EMPTY_LIST);
		expected.add(Arrays.asList(new String[] { "A", "B", "C" }));

		assertEquals("3 item target was partitioned into list 1", expected, actual);

		/* All items in list 1 and 2 */
		targetList = Arrays.asList(new String[] { "A", "B", "C" });
		listOfLists = new LinkedList();
		listOfLists.add(Arrays.asList(new String[] { "B", "C" }));
		listOfLists.add(Arrays.asList(new String[] { "A", "B" }));

		actual = CollectionsUtilities.makePartitionedListByContainmentCount(targetList, listOfLists);
		expected = new LinkedList();
		expected.add(Collections.EMPTY_LIST);
		expected.add(Arrays.asList(new String[] { "A", "C" }));
		expected.add(Arrays.asList(new String[] { "B" }));

		assertEquals("3 item target was partitioned into list 1 and 2", expected, actual);

		/* All items in list 1 and 3 */
		targetList = Arrays.asList(new String[] { "A", "B", "C" });
		listOfLists = new LinkedList();
		listOfLists.add(Arrays.asList(new String[] { "A", "B", "C" }));
		listOfLists.add(Arrays.asList(new String[] { "A", "B" }));
		listOfLists.add(Arrays.asList(new String[] { "A", "B", "Z" }));

		actual = CollectionsUtilities.makePartitionedListByContainmentCount(targetList, listOfLists);
		expected = new LinkedList();
		expected.add(Collections.EMPTY_LIST);
		expected.add(Arrays.asList(new String[] { "C" }));
		expected.add(Collections.EMPTY_LIST);
		expected.add(Arrays.asList(new String[] { "A", "B" }));

		assertEquals("3 item target was partitioned into list 1 and 3", expected, actual);

	}

	public void testMakeListFromMapOfListsAndKeyIteratorWithRestrictingSublist() {

		Map mapOfLists = new Hashtable();

		mapOfLists.put("key-1", Arrays.asList(new String[] { "1-A", "1-B", "1-C" }));
		mapOfLists.put("key-2", Arrays.asList(new String[] { "2-A", "2-B", "2-C" }));
		mapOfLists.put("key-3", Arrays.asList(new String[] { "3-A", "3-B", "3-C" }));

		List restrictionList = Arrays.asList(new String[] { "1-A", "1-B", "1-C", "2-A", "2-B" });

		Iterator keyIterator = new MyKeyIterator(Arrays.asList(new String[] { "key-2", "key-1" }));

		List actualList =
			CollectionsUtilities.makeListFromMapOfListsAndKeyIteratorWithRestrictingSublist(
				mapOfLists,
				keyIterator,
				restrictionList);

		assertEquals(
			"The assembled List matched the expected List",
			Arrays.asList(new String[] { "2-A", "2-B", "1-A", "1-B", "1-C" }),
			actualList);
	}

	private static class MyKeyIterator implements Iterator {

		List elements;
		int nextIX = 0;

		MyKeyIterator(List elements) {
			this.elements = elements;
		}

		public boolean hasNext() {
			return nextIX < elements.size();
		}

		public Object next() {
			return elements.get(nextIX++);
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public void testConvertToCommaSeperatedList() {

		Set set = new TreeSet();

		assertEquals(
			"Empty Set was converted to empty string",
			"",
			CollectionsUtilities.convertToCommaSeperatedList(set));

		set.add("item1");

		assertEquals(
			"Single item was converted as expected",
			"item1",
			CollectionsUtilities.convertToCommaSeperatedList(set));

		set.add("item2");
		set.add("item3");

		assertEquals(
			"Single item was converted as expected",
			"item1,item2,item3",
			CollectionsUtilities.convertToCommaSeperatedList(set));

		set.add("item,4");
		try {
			CollectionsUtilities.convertToCommaSeperatedList(set);
			fail("String element with comma was rejected");
		} catch (IllegalArgumentException e) {
		}

	}

	public void testConvertToSeperatedListWithCompositeSeparator() {

		Set set = new TreeSet();
		String SEPARATOR = " or ";

		assertEquals(
			"Empty Set was converted to empty string",
			"",
			CollectionsUtilities.convertToSeperatedList(set, SEPARATOR));

		set.add("item1");

		assertEquals(
			"Single item was converted as expected",
			"item1",
			CollectionsUtilities.convertToSeperatedList(set, SEPARATOR));

		set.add("item2");
		set.add("item3");

		assertEquals(
			"Three items were converted as expected",
			"item1 or item2 or item3",
			CollectionsUtilities.convertToSeperatedList(set, SEPARATOR));

	}

	public void testConvertToSeparatedListWithDifferentFinalSeparator() {

		Set set = new TreeSet();
		String SEPARATOR = ", ";
		String FINAL_SEPARATOR = " or ";

		assertEquals(
			"Empty Set was converted to empty string",
			"",
			CollectionsUtilities.convertToSeparatedList(set, SEPARATOR, FINAL_SEPARATOR));

		set.add("item1");

		assertEquals(
			"Single item was converted as expected",
			"item1",
			CollectionsUtilities.convertToSeparatedList(set, SEPARATOR, FINAL_SEPARATOR));

		set.add("item2");

		assertEquals(
			"Two items were converted as expected",
			"item1 or item2",
			CollectionsUtilities.convertToSeparatedList(set, SEPARATOR, FINAL_SEPARATOR));

		set.add("item3");

		assertEquals(
			"Three items were converted as expected",
			"item1, item2 or item3",
			CollectionsUtilities.convertToSeparatedList(set, SEPARATOR, FINAL_SEPARATOR));

	}

	public void testDistinguishingPrefixes() {

		Set input = new HashSet();
		Set expected = new HashSet();

		assertEquals("Case 1", expected, CollectionsUtilities.getDistinguishingPrefixes(input));

		input.add("a");
		expected.add("a");

		assertEquals("Case 2", expected, CollectionsUtilities.getDistinguishingPrefixes(input));

		input.add("b");
		expected.add("b");

		assertEquals("Case 3", expected, CollectionsUtilities.getDistinguishingPrefixes(input));

		input.add("ab");

		assertEquals("Case 4", expected, CollectionsUtilities.getDistinguishingPrefixes(input));

		input.add("ba");

		assertEquals("Case 5", expected, CollectionsUtilities.getDistinguishingPrefixes(input));

		input.add("zjkl");
		expected.add("zjkl");

		assertEquals("Case 6", expected, CollectionsUtilities.getDistinguishingPrefixes(input));

	}

	public void testListExpansion() {

		List a;

		a = new LinkedList();
		CollectionsUtilities.expandList(a, new MyTransformer());
		assertEquals("The empty list was expanded correctly", Collections.EMPTY_LIST, a);

		a = new LinkedList();
		a.add("S");
		CollectionsUtilities.expandList(a, new MyTransformer());
		assertEquals("The abcde list was expanded correctly", Collections.singletonList("S"), a);

		a = new LinkedList();
		a.add("abcde");
		CollectionsUtilities.expandList(a, new MyTransformer());
		assertEquals(
			"The abcde list was expanded correctly",
			Arrays.asList(new String[] { "a", "b", "c", "d", "e" }),
			a);

		a = new LinkedList();
		a.add("ABC");
		a.add("12");
		CollectionsUtilities.expandList(a, new MyTransformer());
		assertEquals(
			"The abcde list was expanded correctly",
			Arrays.asList(new String[] { "A", "B", "C", "1", "2" }),
			a);

	}

	private class MyTransformer implements Transformer {

		public Object transform(Object obj) {

			String s = (String) obj;

			if (s.length() == 1)
				return null;

			return Arrays.asList(new String[] { s.substring(0, 1), s.substring(1)});
		}

	}
}
