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
        int offset = 0;
        String updatedSince = "0000-00-00%2000:00:00";
        LocationData[] locationDataArr = getAddressHierarchyEntries("lr-divisions.json");
        givenThat(get(urlEqualTo("/api/1.0/locations" + uri + locationDataSync.getExtraFilter(offset, updatedSince)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/lr-divisions.json"))));
        List<LocationData> divisions = locationDataSync.syncLRData(uri, type);

        assertEquals(locationDataArr.length, divisions.size());
    }

    @Test
    public void shouldSyncDistrictDataSuccessfullyForValidLRData() throws Exception {

        String type = "DISTRICT";
        String uri = "/list/district";
        int offset = 0;
        String updatedSince = "0000-00-00%2000:00:00";
        LocationData[] locationDataArr = getAddressHierarchyEntries("lr-districts.json");
        givenThat(get(urlEqualTo("/api/1.0/locations" + uri + locationDataSync.getExtraFilter(offset, updatedSince)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/lr-districts.json"))));
        List<LocationData> districts = locationDataSync.syncLRData(uri, type);

        assertEquals(locationDataArr.length, districts.size());
    }

    @Test
    public void shouldSyncUpazilaDataSuccessfullyForValidLRData() throws Exception {

        String type = "UPAZILA";
        String uri = "/list/upazila";
        int offset = 0;
        String updatedSince = "0000-00-00%2000:00:00";
        LocationData[] locationDataArr = getAddressHierarchyEntries("lr-upazilas.json");
        givenThat(get(urlEqualTo("/api/1.0/locations" + uri + locationDataSync.getExtraFilter(offset, updatedSince)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/lr-upazilas.json"))));
        List<LocationData> upazilas = locationDataSync.syncLRData(uri, type);

        assertEquals(locationDataArr.length, upazilas.size());
    }

    @Test
    public void shouldSyncPaurasavaDataSuccessfullyForValidLRData() throws Exception {

        String type = "PAURASAVA";
        String uri = "/list/paurasava";
        int offset = 0;
        String updatedSince = "0000-00-00%2000:00:00";
        LocationData[] locationDataArr = getAddressHierarchyEntries("lr-paurasavas.json");
        givenThat(get(urlEqualTo("/api/1.0/locations" + uri + locationDataSync.getExtraFilter(offset, updatedSince)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/lr-paurasavas.json"))));
        List<LocationData> paurasavas = locationDataSync.syncLRData(uri, type);

        assertEquals(locationDataArr.length, paurasavas.size());
    }

    @Test
    public void shouldSyncUnionDataSuccessfullyForValidLRData() throws Exception {

        String type = "UNION";
        String uri = "/list/union";
        int offset = 0;
        String updatedSince = "0000-00-00%2000:00:00";
        LocationData[] locationDataArr = getAddressHierarchyEntries("lr-unions.json");
        givenThat(get(urlEqualTo("/api/1.0/locations" + uri + locationDataSync.getExtraFilter(offset, updatedSince)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/lr-unions.json"))));
        List<LocationData> unions = locationDataSync.syncLRData(uri, type);

        assertEquals(locationDataArr.length, unions.size());
    }

    @Test
    public void shouldSyncWardDataSuccessfullyForValidLRData() throws Exception {

        String type = "WARD";
        String uri = "/list/ward";
        int offset = 0;
        String updatedSince = "0000-00-00%2000:00:00";
        LocationData[] locationDataArr = getAddressHierarchyEntries("lr-wards.json");
        givenThat(get(urlEqualTo("/api/1.0/locations" + uri + locationDataSync.getExtraFilter(offset, updatedSince)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/lr-wards.json"))));
        List<LocationData> wards = locationDataSync.syncLRData(uri, type);

        assertEquals(locationDataArr.length, wards.size());
    }

    public LocationData[] getAddressHierarchyEntries(String responseFileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = asString("jsons/" + responseFileName);
        return mapper.readValue(json, LocationData[].class);
    }

}