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
 * DateUtils.java
 *
 * Created on 10 May 2002, 13:20
 */

package com.studylink.utility;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Various Date utilities.
 *
 * @author  jdb
 * @version $Id: DateUtils.java,v 1.6 2006/06/16 11:35:49 jdb Exp $
 */
public class DateUtils {

	/** Prevent instatiation of DateUtils */
	private DateUtils() {
	}

	static final protected SimpleDateFormat slugFormatter = new SimpleDateFormat("yyyyMMdd");

	/**
	 * Given a <code>Date</code> object return an int representing it.
	 * <p>
	 * The conversion is designed to support FastObjects which can store but not query
	 * fields of type Date. Given the date "2002-May-10" this method returns 20020510.
	 * @param date The date is return an int for
	 * @return An int which sorts in the same order as the date.
	 */
	public static int getDateSlugAsInt(Date date) {

		if (date == null)
			throw new IllegalArgumentException("date was null");

		return Integer.parseInt(slugFormatter.format(date));
	}

	static final protected SimpleDateFormat standardDateFormatter = new SimpleDateFormat("dd/MM/yyyy");

	static final protected SimpleDateFormat extendedDateFormatter = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");

	static final protected SimpleDateFormat isoLikeWithTZDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss Z");

	public static DateFormat getStandardDateFormat() {

		return standardDateFormatter;
	}

	public static DateFormat getExtendedDateFormat() {

		return extendedDateFormatter;
	}

	/**
	 * @param date The date to format as a <code>String</code>
	 * @return A <code>Date</code> formatted as "dd/MM/yyyy"
	 */
	public static String getStandardDate(Date date) {
		if (date == null)
			throw new IllegalArgumentException("date was null");

		return standardDateFormatter.format(date);
	}

	/**
	 * @param date The date to format as a <code>String</code>
	 * @return A <code>Date</code> formatted as "dd-MM-yyyy-HH-mm-ss"
	 */
	public static String getExtendedDate(Date date) {
		if (date == null)
			throw new IllegalArgumentException("date was null");

		return extendedDateFormatter.format(date);
	}

	static final protected SimpleDateFormat standardTimeFormatter = new SimpleDateFormat("HH:mm:ss");

	public static DateFormat getStandardTimeFormat() {

		return standardTimeFormatter;
	}

	/**
	 * @param date The date to format as a <code>String</code>
	 * @return A <code>Date</code> formatted as "HH:mm:ss"
	 */
	public static String getStandardTime(Date date) {
		if (date == null)
			throw new IllegalArgumentException("date was null");

		return standardTimeFormatter.format(date);
	}

	/**
	 * Given a <code>Date</code> object return it in an ISO-like format with a time zone.
	 * <p>
	 * For example<br>
	 *     2004-11-25 11:48:33 -8:00
	 * </p>
	 */
	public static String getISOLikeDateSlugWithTZ(Date date) {

		if (date == null)
			throw new IllegalArgumentException("date was null");

		return isoLikeWithTZDateFormatter.format(date);
	}
    
    private static String[] months = { "January", "February", "March", "April", "May", "June", "July", "August",
            "September", "October", "November", "December" };

    public static String[] months() {
        return (String[]) months.clone();
    }
}
