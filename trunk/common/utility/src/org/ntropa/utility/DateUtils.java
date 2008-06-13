package org.ntropa.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static SimpleDateFormat ISO8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    /**
     * Return the date formatted as ISO8601 with whole second granularity, for
     * example: 2006-12-09T22:06:52+00:00.
     */
    public static String formatAsISO8601(Date date) {
        if (date == null)
            throw new IllegalArgumentException("date was null");
        String result;
        synchronized (ISO8601_FORMAT) {
            result = ISO8601_FORMAT.format(date);
        }
        /*
         * Add a colon between the hours and minutes in the time zone offset to
         * match ISO8601
         */
        result = result.substring(0, result.length() - 2) + ":" + result.substring(result.length() - 2);
        return result;

    }

    private static String EXAMPLE = "2006-12-09T22:06:52+00:00";

    /**
     * Return a <code>Date</code> from an ISO8601 formatted string, for
     * example "2006-12-09T22:06:52+00:00". Only dates formatted as returned by
     * #formatAsISO8601 can be parsed.
     * 
     * @throws ParseException
     */
    public static Date fromISO8601Format(String date) throws ParseException {
        if (date == null)
            throw new IllegalArgumentException("date was null");
        if (date.length() < EXAMPLE.length())
            throw new IllegalArgumentException("date was too short: '" + date + "'");
        if (date.length() > EXAMPLE.length())
            throw new IllegalArgumentException("date was too long: '" + date + "'");

        /* Remove the : in the time zone adjustment. */
        int pos = date.length() - 1 - 2;

        if (':' != date.charAt(pos))
            throw new IllegalArgumentException("date did not have : in time zone: '" + date + "'");

        date = date.substring(0, pos) + date.substring(pos + 1);

        Date result;
        synchronized (ISO8601_FORMAT) {
            result = ISO8601_FORMAT.parse(date);
        }
        return result;
    }
}
