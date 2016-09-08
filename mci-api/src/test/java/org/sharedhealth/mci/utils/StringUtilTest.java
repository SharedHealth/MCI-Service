package org.sharedhealth.mci.utils;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.sharedhealth.mci.domain.util.StringUtil.ensureSuffix;

public class StringUtilTest {

    @Test
    public void shouldEnsureSuffix() throws Exception {
        assertEquals("abc/", ensureSuffix("abc/", "/"));
        assertEquals("abc/", ensureSuffix("abc", "/"));
    }
}