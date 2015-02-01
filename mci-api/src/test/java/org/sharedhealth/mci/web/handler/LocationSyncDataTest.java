package org.sharedhealth.mci.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.LocationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.sharedhealth.mci.utils.FileUtil.asString;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class LocationSyncDataTest {


    @Autowired
    @Qualifier("MCICassandraTemplate")
    protected CassandraOperations cqlTemplate;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    LocationDataSync locationDataSync;

    @Before
    public void setup() throws ParseException {
        cqlTemplate.execute("truncate lr_markers");
    }

    @Test
    public void shouldSyncDivisionDataSuccessfullyForValidLRData() throws Exception {

        String type = "DIVISION";
        String uri = "/list/division";
        String dataFileName = "jsons/lr/lr-divisions.json";
        String updatedSince = getCurrentDateTime();

        String url = "/api/1.0/locations" + uri +
                locationDataSync.getExtraFilter(updatedSince.replace(" ", "%20"), 100);
        String body = asString(dataFileName);

        LocationData[] locationDataArr = getAddressHierarchyEntries(dataFileName);
        mockApiCall(url, body);

        locationDataSync.setUpdatedSince(updatedSince);
        List<LocationData> divisions = locationDataSync.syncLRData(uri, type);

        assertEquals(locationDataArr.length, divisions.size());
        assertEquals(locationDataArr[0].getName(), divisions.get(0).getName());
        assertEquals(locationDataArr[6].getActive(), divisions.get(divisions.size() - 2).getActive());
    }

    @Test
    public void shouldSyncDistrictDataSuccessfullyForValidLRData() throws Exception {

        String type = "DISTRICT";
        String uri = "/list/district";
        String dataFileName = "jsons/lr/lr-districts.json";
        String updatedSince = getCurrentDateTime();

        String url = "/api/1.0/locations" + uri +
                locationDataSync.getExtraFilter(updatedSince.replace(" ", "%20"), 100);
        String body = asString(dataFileName);

        LocationData[] locationDataArr = getAddressHierarchyEntries(dataFileName);
        mockApiCall(url, body);

        locationDataSync.setUpdatedSince(updatedSince);
        List<LocationData> districts = locationDataSync.syncLRData(uri, type);

        assertEquals(locationDataArr.length, districts.size());
        assertEquals(locationDataArr[0].getName(), districts.get(0).getName());
        assertEquals(locationDataArr[0].getParent(), districts.get(0).getParent());
    }

    @Test
    public void shouldSyncUpazilaDataSuccessfullyForValidLRData() throws Exception {

        String type = "UPAZILA";
        String uri = "/list/upazila";
        String dataFileName = "jsons/lr/lr-upazilas.json";
        String updatedSince = getCurrentDateTime();

        String url = "/api/1.0/locations" + uri +
                locationDataSync.getExtraFilter(updatedSince.replace(" ", "%20"), 100);
        String body = asString(dataFileName);

        LocationData[] locationDataArr = getAddressHierarchyEntries(dataFileName);
        mockApiCall(url, body);
        locationDataSync.setUpdatedSince(updatedSince);
        List<LocationData> upazilas = locationDataSync.syncLRData(uri, type);

        assertEquals(locationDataArr.length, upazilas.size());
        assertEquals(locationDataArr[0].getName(), upazilas.get(0).getName());
        assertEquals(locationDataArr[0].getParent(), upazilas.get(0).getParent());
    }

    @Test
    public void shouldSyncPaurasavaDataSuccessfullyForValidLRData() throws Exception {

        String type = "PAURASAVA";
        String uri = "/list/paurasava";
        String dataFileName = "jsons/lr/lr-paurasavas.json";
        String updatedSince = getCurrentDateTime();

        String url = "/api/1.0/locations" + uri +
                locationDataSync.getExtraFilter(updatedSince.replace(" ", "%20"), 100);
        String body = asString(dataFileName);

        LocationData[] locationDataArr = getAddressHierarchyEntries(dataFileName);
        mockApiCall(url, body);
        locationDataSync.setUpdatedSince(updatedSince);
        List<LocationData> paurasavas = locationDataSync.syncLRData(uri, type);

        assertEquals(locationDataArr.length, paurasavas.size());
        assertEquals(locationDataArr[0].getName(), paurasavas.get(0).getName());
        assertEquals(locationDataArr[0].getParent(), paurasavas.get(0).getParent());
    }

    @Test
    public void shouldSyncUnionDataSuccessfullyForValidLRData() throws Exception {

        String type = "UNION";
        String uri = "/list/union";
        String dataFileName = "jsons/lr/lr-unions.json";
        String updatedSince = getCurrentDateTime();

        String url = "/api/1.0/locations" + uri +
                locationDataSync.getExtraFilter(updatedSince.replace(" ", "%20"), 100);
        String body = asString(dataFileName);

        LocationData[] locationDataArr = getAddressHierarchyEntries(dataFileName);
        mockApiCall(url, body);
        locationDataSync.setUpdatedSince(updatedSince);
        List<LocationData> unions = locationDataSync.syncLRData(uri, type);

        assertEquals(locationDataArr.length, unions.size());
        assertEquals(locationDataArr[0].getName(), unions.get(0).getName());
        assertEquals(locationDataArr[0].getParent(), unions.get(0).getParent());
    }

    @Test
    public void shouldSyncWardDataSuccessfullyForValidLRData() throws Exception {

        String type = "WARD";
        String uri = "/list/ward";
        String dataFileName = "jsons/lr/lr-wards.json";
        String updatedSince = getCurrentDateTime();

        String url = "/api/1.0/locations" + uri +
                locationDataSync.getExtraFilter(updatedSince.replace(" ", "%20"), 100);
        String body = asString(dataFileName);

        LocationData[] locationDataArr = getAddressHierarchyEntries(dataFileName);
        mockApiCall(url, body);
        locationDataSync.setUpdatedSince(updatedSince);
        List<LocationData> wards = locationDataSync.syncLRData(uri, type);

        assertEquals(locationDataArr.length, wards.size());
        assertEquals(locationDataArr[0].getName(), wards.get(0).getName());
        assertEquals(locationDataArr[0].getParent(), wards.get(0).getParent());
    }

    private void mockApiCall(String url, String body) {
        givenThat(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    public LocationData[] getAddressHierarchyEntries(String responseFileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = asString(responseFileName);
        return mapper.readValue(json, LocationData[].class);
    }

    protected String getCurrentDateTime() {
        return new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date());
    }

}