package org.sharedhealth.mci.web.config;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.sharedhealth.mci.web.config.MCIConfig.getSupportedRequestUris;
import static org.sharedhealth.mci.web.config.MCIConfig.getSupportedServletMappings;

public class MCIConfigTest {

    @Test
    public void shouldGetSupportedServletMappings() {
        List<String> mappings1 = getSupportedServletMappings("v2", false);
        assertNotNull(mappings1);
        assertEquals(2, mappings1.size());
        assertTrue(mappings1.contains("/api/v2/default/*"));
        assertTrue(mappings1.contains("/api/v2/*"));

        List<String> mappings2 = getSupportedServletMappings("v2", true);
        assertNotNull(mappings2);
        assertEquals(4, mappings2.size());
        assertTrue(mappings2.contains("/api/v2/default/*"));
        assertTrue(mappings2.contains("/api/v2/*"));
        assertTrue(mappings2.contains("/api/default/*"));
        assertTrue(mappings2.contains("/api/*"));
    }

    @Test
    public void shouldGetSupportedRequestUris() {
        List<String> mappings1 = getSupportedRequestUris("v2", false);
        assertNotNull(mappings1);
        assertEquals(2, mappings1.size());
        assertTrue(mappings1.contains("/api/v2/default"));
        assertTrue(mappings1.contains("/api/v2"));

        List<String> mappings2 = getSupportedRequestUris("v2", true);
        assertNotNull(mappings2);
        assertEquals(4, mappings2.size());
        assertTrue(mappings2.contains("/api/v2/default"));
        assertTrue(mappings2.contains("/api/v2"));
        assertTrue(mappings2.contains("/api/default"));
        assertTrue(mappings2.contains("/api"));
    }
}