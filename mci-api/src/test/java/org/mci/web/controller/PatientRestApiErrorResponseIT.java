package org.mci.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mci.web.config.EnvironmentMock;
import org.mci.web.config.WebMvcConfig;
import org.mci.web.model.Address;
import org.mci.web.model.Patient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientRestApiErrorResponseIT {

    public static final String PATIENT_REST_URL = "http://localhost:8080/patient";
    private static HttpClient httpClient;
    private Patient patient;

    @BeforeClass
    public static void init() {
        httpClient = HttpClientBuilder.create().build();
    }

    @Before
    public void setup() {
        patient = new Patient();
        patient.setFirstName("Scott");
        patient.setLastName("Tiger");
        patient.setGender("1");
        patient.setDateOfBirth("2014-12-01");

        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("1020");
        address.setUpazillaId("102030");
        address.setUnionId("10203040");
        patient.setAddress(address);
    }

    @Test
    public void shouldReturnBadRequestErrorForInvalidRequestData() throws Exception {
        patient.getAddress().setAddressLine("h");
        String json = new ObjectMapper().writeValueAsString(patient);

        HttpPost post = new HttpPost(PATIENT_REST_URL);
        StringEntity entity = new StringEntity(json);
        entity.setContentType("application/json");
        post.setEntity(entity);

        HttpResponse response = httpClient.execute(post);
        Assert.assertEquals(400, response.getStatusLine().getStatusCode());
        Assert.assertEquals("{\"code\":400,\"message\":\"invalid.request\"}", EntityUtils.toString(response.getEntity()));
    }
}
