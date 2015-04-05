package org.sharedhealth.mci.web.infrastructure.registry;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.FacilityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.sharedhealth.mci.utils.FileUtil.asString;
import static org.sharedhealth.mci.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.mci.utils.HttpUtil.CLIENT_ID_KEY;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class FacilityRegistryClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private FacilityRegistryClient frClient;

    @Test
    public void shouldFetchAFacilityByFacilityIdWhenFacilityCatersToOneCatchment() throws Exception {
        String facilityId = "10000059";

        givenThat(get(urlEqualTo("/api/1.0/facilities/" + facilityId + ".json"))
                .withHeader(CLIENT_ID_KEY, equalTo("18554"))
                .withHeader(AUTH_TOKEN_KEY, equalTo("b43d2b284fa678fb8248b7cc3ab391f9c21e5d7f8e88f815a9ef4346e426bd33"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/facility.json"))));

        FacilityResponse facility = frClient.find(facilityId);

        assertThat(facility, is(notNullValue()));
        assertThat(facility.getId(), is(facilityId));
        assertThat(facility.getCatchments(), is(notNullValue()));
        assertThat(facility.getCatchments().size(), is(1));
        assertThat(facility.getCatchments().get(0), is("302614"));
    }

    @Test
    public void shouldFetchAFacilityByFacilityIdWhenFacilityCatersToMultipleCatchment() throws Exception {
        String facilityId = "10000069";

        givenThat(get(urlEqualTo("/api/1.0/facilities/" + facilityId + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/facility-multiple-catchments.json"))));

        FacilityResponse facility = frClient.find(facilityId);

        assertThat(facility, is(notNullValue()));
        assertThat(facility.getId(), is(facilityId));
        assertThat(facility.getCatchments(), is(notNullValue()));
        assertThat(facility.getCatchments().size(), is(3));
        assertThat(facility.getCatchments().get(0), is("302614"));
        assertThat(facility.getCatchments().get(1), is("302615"));
        assertThat(facility.getCatchments().get(2), is("302616"));
    }
}