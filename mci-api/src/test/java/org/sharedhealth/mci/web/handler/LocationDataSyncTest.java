package org.sharedhealth.mci.web.handler;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.LocationData;
import org.sharedhealth.mci.web.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.CLIENT_ID_KEY;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class LocationDataSyncTest {


    private static final int DEFAULT_LIMIT = 100;
    public static final String LR_DIVISIONS_JSON = "jsons/lr/lr-divisions.json";
    public static final String LR_DISTRICTS_JSON = "jsons/lr/lr-districts.json";
    public static final String LR_UPAZILAS_JSON = "jsons/lr/lr-upazilas.json";
    public static final String LR_PAURASAVAS_JSON = "jsons/lr/lr-paurasavas.json";
    public static final String LR_UNIONS_JSON = "jsons/lr/lr-unions.json";
    public static final String LR_WARDS_JSON = "jsons/lr/lr-wards.json";
    @Autowired
    @Qualifier("MCICassandraTemplate")
    protected CassandraOperations cqlTemplate;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    LocationDataSync locationDataSync;

    @Autowired
    LocationService locationService;

    public static final String LR_DIVISION_URI_PATH = "/list/division";
    public static final String LR_DISTRICT_URI_PATH = "/list/district";
    public static final String LR_UPAZILA_URI_PATH = "/list/upazila";
    public static final String LR_PAURASAVA_PATH = "/list/paurasava";
    public static final String LR_UNION_URI_PATH = "/list/union";
    public static final String LR_WARD_URI_PATH = "/list/ward";

    public static final String DIVISION_TYPE = "DIVISION";
    public static final String DISTRICT_TYPE = "DISTRICT";
    public static final String UPAZILA_TYPE = "UPAZILA";
    public static final String PAURASAVA_TYPE = "PAURASAVA";
    public static final String UNION_TYPE = "UNION";
    public static final String WARD_TYPE = "WARD";

    @Test
    public void shouldSyncDivisionDataSuccessfully() throws Exception {

        mockApiCall(getCompleteUrl("division"), asString(LR_DIVISIONS_JSON), 1);
        mockApiCall(getCompleteUrl("district"), asString(LR_DISTRICTS_JSON), 2);
        mockApiCall(getCompleteUrl("upazila"), asString(LR_UPAZILAS_JSON), 3);
        mockApiCall(getCompleteUrl("paurasava"), asString(LR_PAURASAVAS_JSON), 4);
        mockApiCall(getCompleteUrl("union"), asString(LR_UNIONS_JSON), 5);
        mockApiCall(getCompleteUrl("ward"), asString(LR_WARDS_JSON), 6);

        locationDataSync.syncLRData(LR_DIVISION_URI_PATH, DIVISION_TYPE);
        locationDataSync.syncLRData(LR_DISTRICT_URI_PATH, DISTRICT_TYPE);
        locationDataSync.syncLRData(LR_UPAZILA_URI_PATH, UPAZILA_TYPE);
        locationDataSync.syncLRData(LR_PAURASAVA_PATH, PAURASAVA_TYPE);
        locationDataSync.syncLRData(LR_UNION_URI_PATH, UNION_TYPE);
        locationDataSync.syncLRData(LR_WARD_URI_PATH, WARD_TYPE);

        // Divisions Test
        LocationData locationData = locationService.findByGeoCode("80");
        LocationData locationData1 = locationService.findByGeoCode("60");
        assertEquals("80", locationData.getCode());
        assertEquals("00", locationData.getParent());
        assertEquals("New Division", locationData.getName());
        assertEquals("0", locationData1.getActive());

        // Districts Test
        locationData = locationService.findByGeoCode("3080");
        assertEquals("80", locationData.getCode());
        assertEquals("30", locationData.getParent());
        assertEquals("New Gopalganj", locationData.getName());

        // Upazila Test
        locationData = locationService.findByGeoCode("100480");
        assertEquals("80", locationData.getCode());
        assertEquals("1004", locationData.getParent());
        assertEquals("New Bamna", locationData.getName());

        // Paurasava Test
        locationData = locationService.findByGeoCode("10042880");
        assertEquals("80", locationData.getCode());
        assertEquals("100428", locationData.getParent());
        assertEquals("New Barguna Paurasava", locationData.getName());

        // Union Test
        locationData = locationService.findByGeoCode("1004092080");
        assertEquals("80", locationData.getCode());
        assertEquals("10040920", locationData.getParent());
        assertEquals("New Urban Ward No-02", locationData.getName());

        // Ward Test
        locationData = locationService.findByGeoCode("100409991380");
        assertEquals("80", locationData.getCode());
        assertEquals("1004099913", locationData.getParent());
        assertEquals("New Ward No-02", locationData.getName());

    }

    @Test
    public void shouldSyncSuccessfullyIfNoUpdateFound() throws Exception {

        mockApiCall(getCompleteUrl("division"), "[]", 1);
        mockApiCall(getCompleteUrl("district"), "[]", 2);
        mockApiCall(getCompleteUrl("upazila"), "[]", 3);
        mockApiCall(getCompleteUrl("paurasava"), "[]", 4);
        mockApiCall(getCompleteUrl("union"), "[]", 5);
        mockApiCall(getCompleteUrl("ward"), "[]", 6);

        assertTrue(locationDataSync.syncLRData(LR_DIVISION_URI_PATH, DIVISION_TYPE));
        assertTrue(locationDataSync.syncLRData(LR_DISTRICT_URI_PATH, DISTRICT_TYPE));
        assertTrue(locationDataSync.syncLRData(LR_UPAZILA_URI_PATH, UPAZILA_TYPE));
        assertTrue(locationDataSync.syncLRData(LR_PAURASAVA_PATH, PAURASAVA_TYPE));
        assertTrue(locationDataSync.syncLRData(LR_UNION_URI_PATH, UNION_TYPE));
        assertTrue(locationDataSync.syncLRData(LR_WARD_URI_PATH, WARD_TYPE));

    }

    private void mockApiCall(String url, String body, int priority) {
        stubFor(get(urlEqualTo(url))
                .withHeader(CLIENT_ID_KEY, equalTo("18554"))
                .withHeader(AUTH_TOKEN_KEY, equalTo("b43d2b284fa678fb8248b7cc3ab391f9c21e5d7f8e88f815a9ef4346e426bd33"))
                .atPriority(priority)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    private String getCompleteUrl(String type) {
        return "/api/1.0/locations/list/" + type + "?limit=" + DEFAULT_LIMIT;
    }

    @After
    public void teardown() {
        cqlTemplate.execute("truncate lr_markers");
        cqlTemplate.execute("delete from locations where code = '80' and parent='00'");
        cqlTemplate.execute("update locations set active='1' where parent='00' and code='60';");
        cqlTemplate.execute("delete from locations where code = '80' and parent='30'");
        cqlTemplate.execute("delete from locations where code = '80' and parent='1004'");
        cqlTemplate.execute("delete from locations where code = '80' and parent='100428'");
        cqlTemplate.execute("delete from locations where code = '80' and parent='10040920'");
        cqlTemplate.execute("delete from locations where code = '80' and parent='1004099913'");
    }
}