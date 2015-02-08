package org.sharedhealth.mci.web.handler;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
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

import java.text.ParseException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.sharedhealth.mci.utils.FileUtil.asString;

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

    @Before
    public void setup() throws ParseException {
        cqlTemplate.execute("truncate lr_markers");
        cqlTemplate.execute("delete from locations where code = '80' and parent='00'");
        cqlTemplate.execute("update locations set active='1' where parent='00' and code='60';");
        cqlTemplate.execute("delete from locations where code = '80' and parent='30'");
        cqlTemplate.execute("delete from locations where code = '80' and parent='1004'");
        cqlTemplate.execute("delete from locations where code = '80' and parent='100428'");
        cqlTemplate.execute("delete from locations where code = '80' and parent='10040920'");
        cqlTemplate.execute("delete from locations where code = '80' and parent='1004099913'");
    }

    @Test
    public void shouldSyncLocationDataSuccessfully() throws Exception {

        mockApiCall(getCompleteUrl("division"), asString(LR_DIVISIONS_JSON), 1);
        mockApiCall(getCompleteUrl("district"), asString(LR_DISTRICTS_JSON), 2);
        mockApiCall(getCompleteUrl("upazila"), asString(LR_UPAZILAS_JSON), 3);
        mockApiCall(getCompleteUrl("paurasava"), asString(LR_PAURASAVAS_JSON), 4);
        mockApiCall(getCompleteUrl("union"), asString(LR_UNIONS_JSON), 5);
        mockApiCall(getCompleteUrl("ward"), asString(LR_WARDS_JSON), 6);

        locationDataSync.sync();

        // Divisions Test
        LocationData locationData = locationService.findByGeoCode("80");
        LocationData locationData1 = locationService.findByGeoCode("60");
        Assert.assertEquals("80", locationData.getCode());
        Assert.assertEquals("00", locationData.getParent());
        Assert.assertEquals("New Division", locationData.getName());
        Assert.assertEquals("0", locationData1.getActive());

        // Districts Test
        locationData = locationService.findByGeoCode("3080");
        Assert.assertEquals("80", locationData.getCode());
        Assert.assertEquals("30", locationData.getParent());
        Assert.assertEquals("New Gopalganj", locationData.getName());

        // Upazila Test
        locationData = locationService.findByGeoCode("100480");
        Assert.assertEquals("80", locationData.getCode());
        Assert.assertEquals("1004", locationData.getParent());
        Assert.assertEquals("New Bamna", locationData.getName());

        // Paurasava Test
        locationData = locationService.findByGeoCode("10042880");
        Assert.assertEquals("80", locationData.getCode());
        Assert.assertEquals("100428", locationData.getParent());
        Assert.assertEquals("New Barguna Paurasava", locationData.getName());

        // Union Test
        locationData = locationService.findByGeoCode("1004092080");
        Assert.assertEquals("80", locationData.getCode());
        Assert.assertEquals("10040920", locationData.getParent());
        Assert.assertEquals("New Urban Ward No-02", locationData.getName());

        // Ward Test
        locationData = locationService.findByGeoCode("100409991380");
        Assert.assertEquals("80", locationData.getCode());
        Assert.assertEquals("1004099913", locationData.getParent());
        Assert.assertEquals("New Ward No-02", locationData.getName());


    }

    private void mockApiCall(String url, String body, int priority) {
        stubFor(get(urlEqualTo(url)).atPriority(priority)
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