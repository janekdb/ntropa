package org.ntropa.build.channel;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ntropa.utility.DateUtils;

/**
 * This class manages the String format of the build-status file, which looks
 * like this when it is stored in a file
 * <ul>
 * <li>last-change=2001-11-27T13:03:59+00:00</li>
 * <li>last-addition=2000-11-27T13:03:59+00:00</li>
 * <li>last-modification=2001-11-27T13:03:59+00:00</li>
 * <li>last-deletion=2001-06-27T13:03:59+00:00</li>
 * </ul>
 * last-change is always the most recent of last-addition, last-modification and
 * last-deletion.
 * 
 * @author jdb
 * 
 */
public class BuildStatus {

    private static final String CHANGE = "last-change";

    private static final String ADDITION = "last-addition";

    private static final String MODIFICATION = "last-modification";

    private static final String DELETION = "last-deletion";

    private static Set ACCEPTABLE_PROPERTY_NAMES = new HashSet();
    static {
        ACCEPTABLE_PROPERTY_NAMES.add(CHANGE);
        ACCEPTABLE_PROPERTY_NAMES.add(ADDITION);
        ACCEPTABLE_PROPERTY_NAMES.add(MODIFICATION);
        ACCEPTABLE_PROPERTY_NAMES.add(DELETION);
    }

    private static final String ORDERED_PROPERTY_NAMES[] = { CHANGE, ADDITION, MODIFICATION, DELETION };

    private static final String PROPERTY_TERMINATOR = "\n";

    /* ISO 8601, http://www.w3.org/TR/NOTE-datetime */
    /* YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00) */

    private static final String ISO8601_REG_EXP = "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d[+-]\\d\\d:\\d\\d";

    private static final Pattern ISO8601_PATTERN = Pattern.compile(ISO8601_REG_EXP);

    final Map properties = new HashMap();

    public BuildStatus(String externalForm) {
        String lines[] = externalForm.split(PROPERTY_TERMINATOR);
        if (lines.length != 4)
            throw new IllegalArgumentException("Property count was not 4: " + lines.length);

        for (int i = 0; i < lines.length; i++) {
            String parts[] = lines[i].split("=");
            if (parts.length != 2)
                throw new IllegalArgumentException("Malformed property line: '" + lines[i] + "'");
            String name = parts[0];
            if (!ACCEPTABLE_PROPERTY_NAMES.contains(name))
                throw new IllegalArgumentException("Invalid property name: '" + name + "'");
            String iso8601Date = parts[1];
            Matcher m = ISO8601_PATTERN.matcher(iso8601Date);
            if (!m.matches())
                throw new IllegalArgumentException("Date did not match pattern: '" + ISO8601_PATTERN.toString()
                        + "' was not matched by '" + iso8601Date + "'");
            if (properties.containsKey(name))
                throw new IllegalArgumentException("Duplicate property name: '" + name + "'");
            try {
                properties.put(name, DateUtils.fromISO8601Format(iso8601Date));
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }
        Date lastChange = (Date) properties.get(CHANGE);

        Date lastAddition = (Date) properties.get(ADDITION);
        if (lastAddition.after(lastChange))
            throw new IllegalStateException("last-addition > last-change");

        Date lastModification = (Date) properties.get(MODIFICATION);
        if (lastAddition.after(lastChange))
            throw new IllegalStateException("last-modification > last-change");

        Date lastDeletion = (Date) properties.get(DELETION);
        if (lastDeletion.after(lastChange))
            throw new IllegalArgumentException("last-deletion > last-change");

        /*
         * The last change date must be equal to at least one of the other
         * dates.
         */
        Date l = lastChange, a = lastAddition, m = lastModification, d = lastDeletion;
        if (!l.equals(a) && !l.equals(m) && !l.equals(d))
            throw new IllegalArgumentException(
                    "last-change was not equal to at least one of last-addition, last-modification and last-deletion");

    }

    public BuildStatus(Date date) {
        if (date == null)
            throw new IllegalArgumentException("date was null");
        for (int i = 0; i < ORDERED_PROPERTY_NAMES.length; i++) {
            String name = ORDERED_PROPERTY_NAMES[i];
            properties.put(name, date);
        }
    }

    public void setAdditionDate(Date additionDate) {
        if (additionDate == null)
            throw new IllegalArgumentException("Addition date was null");
        storeAndMaybeUpdateChange(ADDITION, additionDate);
    }

    public void setModificationDate(Date modificationDate) {
        if (modificationDate == null)
            throw new IllegalArgumentException("Modification date was null");
        storeAndMaybeUpdateChange(MODIFICATION, modificationDate);
    }

    public void setDeletionDate(Date deletionDate) {
        if (deletionDate == null)
            throw new IllegalArgumentException("Deletion date was null");
        storeAndMaybeUpdateChange(DELETION, deletionDate);
    }

    private void storeAndMaybeUpdateChange(String name, Date date) {
        properties.put(name, date);
        Date changeDate = (Date) properties.get(CHANGE);
        if (date.after(changeDate))
            properties.put(CHANGE, date);
    }

    private String getDate(String name) {
        return DateUtils.formatAsISO8601((Date) properties.get(name));
    }

    public String toExternalForm() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ORDERED_PROPERTY_NAMES.length; i++) {
            String name = ORDERED_PROPERTY_NAMES[i];
            String date = (String) getDate(name);
            sb.append(name + "=" + date + PROPERTY_TERMINATOR);
        }
        return sb.toString();
    }
}
