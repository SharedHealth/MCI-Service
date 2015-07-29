package org.sharedhealth.mci.utils;

import org.joda.time.DateTime;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.sharedhealth.mci.utils.DateUtil.*;
import static org.sharedhealth.mci.utils.TimeUid.fromString;

public class DateUtilTest {

    @Test
    public void shouldAbleToParseSupportedDateFormatsInUTC() throws Exception {
        long utcTime1 = 1451557200000L;
        long utcTime2 = 1451557230000L;
        long utcTime3 = 1451557230400L;

        assertDate(parseDate("2015-12-31T10:20Z"), utcTime1);
        assertDate(parseDate("2015-12-31T10:20:30Z"), utcTime2);
        assertDate(parseDate("2015-12-31T10:20:30.400Z"), utcTime3);

        assertDate(parseDate("2015-12-31T10:20+0000"), utcTime1);
        assertDate(parseDate("2015-12-31T10:20:30+0000"), utcTime2);
        assertDate(parseDate("2015-12-31T10:20:30.400+0000"), utcTime3);

        assertDate(parseDate("2015-12-31T10:20+00:00"), utcTime1);
        assertDate(parseDate("2015-12-31T10:20:30+00:00"), utcTime2);
        assertDate(parseDate("2015-12-31T10:20:30.400+00:00"), utcTime3);
    }

    @Test
    public void shouldAbleToParseSupportedDateFormatsInLocalTimeZone() throws Exception {
        long istTime1 = 1451537400000L;
        long istTime2 = 1451537430000L;
        long istTime3 = 1451537430400L;

        assertDate(parseDate("2015-12-31T10:20+0530"), istTime1);
        assertDate(parseDate("2015-12-31T10:20:30+0530"), istTime2);
        assertDate(parseDate("2015-12-31T10:20:30.400+0530"), istTime3);

        assertDate(parseDate("2015-12-31T10:20+05:30"), istTime1);
        assertDate(parseDate("2015-12-31T10:20:30+05:30"), istTime2);
        assertDate(parseDate("2015-12-31T10:20:30.400+05:30"), istTime3);

        long bstTime1 = 1451535600000L;
        long bstTime2 = 1451535630000L;
        long bstTime3 = 1451535630400L;

        assertDate(parseDate("2015-12-31T10:20+06"), bstTime1);
        assertDate(parseDate("2015-12-31T10:20:30+06"), bstTime2);
        assertDate(parseDate("2015-12-31T10:20:30.400+06"), bstTime3);

        assertNotNull(parseDate("2015-12-31"));
    }

    private void assertDate(Date date, long time) {
        assertNotNull(date);
        assertEquals(time, date.getTime());
    }

    @Test
    public void shouldFormatTimeUuidToIsoDateString() throws Exception {
        UUID uuid = fromString("6d713100-a7a3-11e4-8319-5fcb9978cb86");
        String isoDate = toIsoMillisFormat(uuid);

        DateFormat dateFormat = new SimpleDateFormat(ISO_DATE_TIME_TILL_MILLIS_FORMAT3);
        assertEquals(dateFormat.format(parseDate("2015-01-29T10:41:48.560Z")), isoDate);
    }

    @Test
    public void shouldReturnNullForInvalidDateOrFormat() {
        assertNull(parseDate(null));
        assertNull(parseDate(""));
        assertNull(parseDate("2015-01-29", "dd-MM"));
        assertNull(parseDate("2015-01-29", "Invalid"));
        assertNull(parseDate("invalid", "dd-MM-yyyy"));
    }

    @Test
    public void shouldReturnNullForInvalidDate() {
        assertNull(parseDate(""));
        assertNull(parseDate("invalid"));
        assertNull(parseDate("20150129"));
    }

    @Test
    public void shouldReturnCurrentYear() throws Exception {
        DateTime date = new DateTime(new Date());
        assertEquals(DateUtil.getCurrentYear(), date.getYear());
    }

    @Test
    public void shouldIgnoreMillisecondsWhileComparingDates() throws Exception {
        Date date1 = parseDate("2015-01-29T10:41:48.560+06:00");
        Date date2 = parseDate("2015-01-29T10:41:47.401+06:00");
        assertFalse(DateUtil.isEqualTo(date1, date2));

        date1 = parseDate("2015-01-29T10:41:48.560+06:00");
        date2 = parseDate("2015-01-29T10:41:48.401+06:00");
        assertTrue(DateUtil.isEqualTo(date1, date2));

        date1 = parseDate("2015-01-29T10:41:48.560+06:00");
        date2 = null;
        assertFalse(DateUtil.isEqualTo(date1, date2));

        date1 = null;
        date2 = null;
        assertTrue(DateUtil.isEqualTo(date1, date2));
    }
}