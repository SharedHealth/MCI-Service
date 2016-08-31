package org.sharedhealth.mci.web.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.Lists;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.config.EnvironmentMock;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.web.infrastructure.security.IdentityServiceClient;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.model.MciHealthIdStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.sharedhealth.mci.utils.HttpUtil.*;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class HealthIdServiceIT {
    @Autowired
    private HealthIdService healthIdService;
    @Autowired
    private IdentityServiceClient identityServiceClient;
    @Autowired
    private MCIProperties mciProperties;
    @Autowired
    private MciHealthIdStore mciHealthIdStore;

    @Rule
    public WireMockRule idpService = new WireMockRule(9997);


    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        mciHealthIdStore.clear();
    }
//
//    @Test
//    public void shouldGetNextHealthId() throws Exception {
//        List<String> hidBlock = Lists.newArrayList("healthId1", "healthId2");
//        mciHealthIdStore.addMciHealthIds(hidBlock);
//        File hidLocalStorageFile = new File(mciProperties.getHidLocalStoragePath());
//        IOUtils.write(new Gson().toJson(hidBlock), new FileOutputStream(hidLocalStorageFile));
//
//        MciHealthId nextHealthId = healthIdService.getNextHealthId();
//
//        assertNotNull(nextHealthId);
//        assertEquals(1, mciHealthIdStore.noOfHIDsLeft());
//        List<String> hids = readHIDsFromFile();
//        assertEquals(1, hids.size());
//        assertFalse(hids.contains(nextHealthId.getHid()));
//    }
//
//    @Test
//    public void shouldAskHIDServiceToMarkAsUsedHID() throws Exception {
//        MciHealthId hid = new MciHealthId("hid");
//        setupStub("/healthIds/markUsed/hid", "Accepted");
//
//        healthIdService.markUsed(hid);
//
//        verify(1, putRequestedFor(urlMatching("/healthIds/markUsed/hid"))
//                        .withRequestBody(containing("\"used_at\":"))
//        );
//    }
//
//    @Test
//    public void shouldPutBackTheHIDToStoreAndFile() throws Exception {
//        List<String> hidBlock = Lists.newArrayList("healthId1");
//        mciHealthIdStore.addMciHealthIds(hidBlock);
//        File hidLocalStorageFile = new File(mciProperties.getHidLocalStoragePath());
//        IOUtils.write(new Gson().toJson(hidBlock), new FileOutputStream(hidLocalStorageFile));
//
//        healthIdService.putBack(new MciHealthId("healthId2"));
//
//        assertEquals(2, mciHealthIdStore.noOfHIDsLeft());
//        List<String> hids = readHIDsFromFile();
//        assertEquals(2, hids.size());
//        assertTrue(hids.containsAll(asList("healthId1", "healthId2")));
//    }

    //    @Test
//    public void shouldReplenishFromHIDServiceHIDCountReachesToThreshold() throws Exception {
//        List<String> healthIdBlock = Lists.newArrayList("healthId1", "healthId2");
//        mciHealthIdStore.addMciHealthIds(healthIdBlock);
//        IOUtils.write(new Gson().toJson(healthIdBlock), new FileOutputStream(new File(mciProperties.getHidLocalStoragePath())));
//
//        String nextHIDBlockUrl = String.format("/healthIds/nextBlock/mci/%s?blockSize=%s",
//                mciProperties.getIdpClientId(), mciProperties.getHealthIdReplenishBlockSize());
//        String hidResponse = getHidResponse();
//        setupStub(nextHIDBlockUrl, hidResponse);
//
//        healthIdService.replenishIfNeeded();
//
//        assertThat(mciHealthIdStore.noOfHIDsLeft(), is(2 + mciProperties.getHealthIdReplenishBlockSize()));
//        verify(1, postRequestedFor(urlMatching("/signin")));
//        verify(1, getRequestedFor(urlPathMatching("/healthIds")));
//
//        List<String> hids = readHIDsFromFile();
//        List<String> expectedHIDs = getHIDs();
//        expectedHIDs.addAll(healthIdBlock);
//        assertEquals(hids.size(), expectedHIDs.size());
//        assertTrue(hids.containsAll(expectedHIDs));
//    }
//
    @Test
    public void shouldNotReplenishIfTheThresholdIsNotReached() throws Exception {
        List<String> healthIdBlock = Lists.newArrayList("healthId1", "healthId2", "healthId3", "healthId4");
        mciHealthIdStore.addMciHealthIds(healthIdBlock);

        healthIdService.replenishIfNeeded();

        assertThat(mciHealthIdStore.noOfHIDsLeft(), is(4));
        verify(0, postRequestedFor(urlMatching("/signin")));
        verify(0, getRequestedFor(urlMatching("/healthIds/nextBlock")));
    }

    @Test
    public void shouldAskHIDServiceForTheFirstEverStartup() throws Exception {
        assertThat(mciHealthIdStore.noOfHIDsLeft(), is(0));
//        assertFalse(new File(mciProperties.getHidLocalStoragePath()).exists());

        String nextHIDBlockUrl = String.format("/healthIds/nextBlock/mci/%s?blockSize=%s",
                mciProperties.getIdpClientId(), mciProperties.getHealthIdBlockSize());
        String hidResponse = getHidResponse();
        setupStub(nextHIDBlockUrl, hidResponse);

        healthIdService.replenishIfNeeded();

        assertThat(mciHealthIdStore.noOfHIDsLeft(), is(mciProperties.getHealthIdBlockSize()));
        verify(1, postRequestedFor(urlMatching("/signin")));
        verify(1, getRequestedFor(urlMatching("/healthIds")));

//        List<String> hids = readHIDsFromFile();
//        List<String> expectedHIDs = getHIDs();
//        assertEquals(hids.size(), expectedHIDs.size());
//        assertTrue(hids.containsAll(expectedHIDs));
    }

//
//    @Test
//    public void shouldNotRequestHIDServiceWhenFileHasSufficientHIDsWhileInitialization() throws Exception {
//        List<String> healthIdBlock = Lists.newArrayList("healthId1", "healthId2", "healthId3", "healthId4");
//        IOUtils.write(new Gson().toJson(healthIdBlock), new FileOutputStream(new File(mciProperties.getHidLocalStoragePath())));
//
//        assertThat(mciHealthIdStore.noOfHIDsLeft(), is(0));
//        healthIdService.replenishIfNeeded();
//
//        assertThat(mciHealthIdStore.noOfHIDsLeft(), is(4));
//        verify(0, postRequestedFor(urlMatching("/signin")));
//        verify(0, getRequestedFor(urlPathMatching("/healthIds")));
//    }
//
//    @Test
//    public void shouldRequestHIDServiceWhenFileDoesNotHaveSufficientHIDsWhileInitialization() throws Exception {
//        List<String> healthIdBlock = Lists.newArrayList("healthId1", "healthId2");
//        IOUtils.write(new Gson().toJson(healthIdBlock), new FileOutputStream(new File(mciProperties.getHidLocalStoragePath())));
//        assertThat(mciHealthIdStore.noOfHIDsLeft(), is(0));
//
//        String nextHIDBlockUrl = String.format("/healthIds/nextBlock/mci/%s?blockSize=%s",
//                mciProperties.getIdpClientId(), mciProperties.getHealthIdReplenishBlockSize());
//        String hidResponse = getHidResponse();
//        setupStub(nextHIDBlockUrl, hidResponse);
//
//        healthIdService.replenishIfNeeded();
//
//        assertThat(mciHealthIdStore.noOfHIDsLeft(), is(2 + mciProperties.getHealthIdReplenishBlockSize()));
//        verify(1, postRequestedFor(urlMatching("/signin")));
//        verify(1, getRequestedFor(urlPathMatching("/healthIds")));
//
//        List<String> hids = readHIDsFromFile();
//        List<String> expectedHIDs = getHIDs();
//        expectedHIDs.addAll(healthIdBlock);
//        assertEquals(hids.size(), expectedHIDs.size());
//        assertTrue(hids.containsAll(expectedHIDs));
//    }
//
//    private List<String> readHIDsFromFile() throws IOException {
//        File hidLocalStorageFile = new File(mciProperties.getHidLocalStoragePath());
//        assertTrue(hidLocalStorageFile.exists());
//        String content = IOUtils.toString(new FileInputStream(hidLocalStorageFile), "UTF-8");
//        return asList(new ObjectMapper().readValue(content, String[].class));
//    }

    private void setupStub(String hidServiceUrl, String hidServiceResponse) {
        UUID token = UUID.randomUUID();
        String idpResponse = "{\"access_token\" : \"" + token.toString() + "\"}";

        stubFor(post(urlMatching("/signin"))
                .withHeader(AUTH_TOKEN_KEY, equalTo(mciProperties.getIdpAuthToken()))
                .withHeader(CLIENT_ID_KEY, equalTo(mciProperties.getIdpClientId()))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody(idpResponse)
                ));

        stubFor(get(urlPathMatching(hidServiceUrl))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody(hidServiceResponse)
                ));
    }

    private String getHidResponse() throws IOException {
        HashMap<String, Object> hidResponse = new HashMap<>();

        hidResponse.put("total", "10");
        hidResponse.put("hids", getHIDs());
        return new ObjectMapper().writeValueAsString(hidResponse);
    }

    private List<String> getHIDs() {
        return Lists.newArrayList("98000430630",
                "98000429756",
                "98000430531",
                "98000430507",
                "98000430341",
                "98000430564",
                "98000429145",
                "98000430911",
                "98000429061",
                "98000430333");
    }
}