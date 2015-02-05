package org.sharedhealth.mci.utils;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.sharedhealth.mci.utils.DateUtil.parseDate;
import static org.sharedhealth.mci.utils.DateUtil.toIsoFormat;
import static org.sharedhealth.mci.utils.TimeUid.fromString;

public class DateUtilTest {

    @Test
    public void shouldAbleToParseSupportedDateFormats() throws Exception {
        long utcTime1 = 1451557200000L;
        long utcTime2 = 1451557230000L;
        long utcTime3 = 1451557230444L;

        //assertDate(parseDate("2015-12-31T10:20:30Z"), utcTime2);
        //assertDate(parseDate("2015-12-31T10:20:30.444Z"), utcTime3);

        assertDate(parseDate("2015-12-31T10:20+0000"), utcTime1);
        assertDate(parseDate("2015-12-31T10:20:30+0000"), utcTime2);
        assertDate(parseDate("2015-12-31T10:20:30.444+0000"), utcTime3);

        assertDate(parseDate("2015-12-31T10:20+00:00"), utcTime1);
        assertDate(parseDate("2015-12-31T10:20:30+00:00"), utcTime2);
        assertDate(parseDate("2015-12-31T10:20:30.444+00:00"), utcTime3);

        //assertDate(parseDate("2015-12-31"), 0);
        //assertDate(parseDate("2015-12-31 10:20:30"), 0);
    }

    private void assertDate(Date date, long time) {
        assertNotNull(date);
        assertEquals(time, date.getTime());
    }

    @Test
    public void shouldFormatTimeUuidToIsoDateString() throws Exception {
        UUID uuid = fromString("6d713100-a7a3-11e4-8319-5fcb9978cb86");
        String isoDate = toIsoFormat(uuid);
        assertEquals("2015-01-29T10:41:48.560Z", isoDate);
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
}