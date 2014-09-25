package org.sharedhealth.mci.web.infrastructure.fr;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.Facility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.sharedhealth.mci.utils.FileUtil.asString;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class FacilityRegistryWrapperTest extends TestCase {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private FacilityRegistryWrapper fr;

    @Test
    public void shouldFetchAFacilityByFacilityId() throws Exception{
        String facilityId = "10010408";

        givenThat(get(urlEqualTo("/api/1.0/facilities/" + facilityId + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/facility.json"))));

        Facility facility = fr.getFacility(facilityId).get();
        assertThat(facility, is(notNullValue()));
        assertThat(facility.getId(), is(facilityId));
        assertThat(facility.getCatchments(), is(notNullValue()));
        assertThat(facility.getCatchments().size(), is(1));
        assertThat(facility.getCatchments().get(0), is("1004092005"));
    }
}