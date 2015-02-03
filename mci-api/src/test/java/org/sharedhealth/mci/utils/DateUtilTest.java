package org.sharedhealth.mci.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class DateUtilTest {

    private static final int UNIX_TIME_VALUE = 1325376000;

    private static final String UTC_DATE_TILL_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String UTC_DATE_TILL_SECS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String ISO_DATE_TILL_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String ISO_DATE_TILL_SECS_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String ISO_DATE_TILL_MINS_FORMAT = "yyyy-MM-dd'T'HH:mmZ";
    private static final String RFC_DATE_TILL_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final String RFC_DATE_TILL_SECS_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String RFC_DATE_TILL_MINS_FORMAT = "yyyy-MM-dd'T'HH:mmXXX";
    private static final String SIMPLE_DATE_WITH_SECS_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String SIMPLE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";

    private static final String[] DATE_FORMATS = new String[]{
            ISO_DATE_TILL_MILLIS_FORMAT, ISO_DATE_TILL_SECS_FORMAT,
            ISO_DATE_TILL_MINS_FORMAT, UTC_DATE_TILL_MILLIS_FORMAT,
            UTC_DATE_TILL_SECS_FORMAT, SIMPLE_DATE_WITH_SECS_FORMAT,
            SIMPLE_DATE_FORMAT, SIMPLE_DATE_TIME_FORMAT,
            RFC_DATE_TILL_MINS_FORMAT, RFC_DATE_TILL_SECS_FORMAT,
            RFC_DATE_TILL_MILLIS_FORMAT};

    @Test
    public void shouldReturnCurrentYear() throws Exception {
        DateTime date = new DateTime(new Date());
        assertEquals(DateUtil.getCurrentYear(), date.getYear());
    }

    @Test
    public void shouldAbleToParseSupportedDateFormats() throws Exception {
        Date date = new Date(UNIX_TIME_VALUE);

        for (String format : DATE_FORMATS) {
            assertDateEquality(date, format);

        }
    }

    @Test
    public void shouldReturnAsFormattedDateString() throws Exception {
        final Date date = new Date(UNIX_TIME_VALUE);
        DateTime dateTime = new DateTime(date);

        String formatStart = String.format("%d-%02d-%02d", dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());

        for (String format : DATE_FORMATS) {
            assertTrue(DateUtil.toDateString(date, format).startsWith(formatStart));
        }
    }

    @Test
    public void shouldGetDateFromUUID() throws Exception {
        final UUID uuid = TimeUid.fromString("6d713100-a7a3-11e4-8319-5fcb9978cb86");
        final String isoDate = DateUtil.toIsoFormat(uuid);

        assertTrue(isoDate.startsWith("2015-01-29T"));
        assertThat(isoDate.length(), is(28));
    }

    private void assertDateEquality(Date date, String format) {
        DateTime datetime = new DateTime(date);
        DateTime parsedDate = new DateTime(DateUtil.parseDate(toDateString(date, format)));

        assertEquals(datetime.getYear(), parsedDate.getYear());
        assertEquals(datetime.getMonthOfYear(), parsedDate.getMonthOfYear());
        assertEquals(datetime.getDayOfMonth(), parsedDate.getDayOfMonth());
    }

    private static String toDateString(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }
}