package org.sharedhealth.mci.web.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.config.EnvironmentMock;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.model.Patient;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.model.IdentityStore;
import org.sharedhealth.mci.web.model.MciHealthIdStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.utils.HttpUtil.*;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class HealthIdServiceIT {
    @Autowired
    private HealthIdService healthIdService;
    @Autowired
    private IdentityStore identityStore;
    @Autowired
    private MCIProperties mciProperties;
    @Autowired
    private MciHealthIdStore mciHealthIdStore;
    @Autowired
    private MciHealthIdStore id;
    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cqlTemplate;

    @Rule
    public WireMockRule idpService = new WireMockRule(9997);

    private String CHECK_HID_PATH = "/healthIds/checkAvailability/hid";
    private String NEXT_BLOCK_PATH = "/healthIds/nextBlock/mci";
    private final String SIGN_IN_PATH = "/signin";
    private final String MARK_USED_PATH = "/healthIds/markUsed/hid";

    @After
    public void tearDown() throws Exception {
        identityStore.clearIdentityToken();
        mciHealthIdStore.clear();
        File file = new File(mciProperties.getHidLocalStoragePath());
        if (file.exists()) {
            file.delete();
        }

    }

    @Test
    public void shouldGetNextHealthId() throws Exception {
        List<String> hidBlock = Lists.newArrayList("healthId1", "healthId2");
        mciHealthIdStore.addMciHealthIds(hidBlock);
        writeHIDBlockToFile(hidBlock);

        String nextHealthId = healthIdService.getNextHealthId();

        assertNotNull(nextHealthId);
        assertEquals(1, mciHealthIdStore.noOfHIDsLeft());
        assertFalse(mciHealthIdStore.getAll().contains(nextHealthId));
        List<String> hids = readHIDsFromFile();
        assertEquals(2, hids.size());
    }

    @Test
    public void shouldAskHIDServiceToMarkAsUsedHID() throws Exception {
        UUID token = UUID.randomUUID();
        String idpResponse = "{\"access_token\" : \"" + token.toString() + "\"}";
        setUpIDPStub(idpResponse);
        setUpMarkUsedStub(token);
        healthIdService.markUsed("hid");

        verify(1, putRequestedFor(urlMatching(MARK_USED_PATH))
                .withRequestBody(containing("\"used_at\":"))
        );
    }

    @Test
    public void shouldPutBackTheHIDToStoreAndFile() throws Exception {
        List<String> hidBlock = Lists.newArrayList("healthId1");
        mciHealthIdStore.addMciHealthIds(hidBlock);

        healthIdService.putBackHealthId("healthId2");

        assertEquals(2, mciHealthIdStore.noOfHIDsLeft());
        assertTrue(mciHealthIdStore.getAll().containsAll(asList("healthId1", "healthId2")));
    }

    @Test
    public void shouldAskHIDServiceForTheFirstEverStartup() throws Exception {
        assertThat(mciHealthIdStore.noOfHIDsLeft(), is(0));
        assertFalse(new File(mciProperties.getHidLocalStoragePath()).exists());

        UUID token = UUID.randomUUID();
        String idpResponse = "{\"access_token\" : \"" + token.toString() + "\"}";
        setUpIDPStub(idpResponse);
        setupNextBlockStub(token);
        healthIdService.replenishIfNeeded();

        assertThat(mciHealthIdStore.noOfHIDsLeft(), is(mciProperties.getHealthIdBlockSize()));
        verify(1, postRequestedFor(urlMatching(SIGN_IN_PATH)));
        verify(1, getRequestedFor(urlPathMatching(NEXT_BLOCK_PATH)));

        List<String> hids = readHIDsFromFile();
        List<String> expectedHIDs = getHIDs();
        assertEquals(hids.size(), expectedHIDs.size());
        assertTrue(hids.containsAll(expectedHIDs));
    }

    @Test
    public void shouldNotReplenishIfTheThresholdIsNotReached() throws Exception {
        List<String> healthIdBlock = Lists.newArrayList("healthId1", "healthId2", "healthId3", "healthId4");
        mciHealthIdStore.addMciHealthIds(healthIdBlock);

        healthIdService.replenishIfNeeded();

        assertThat(mciHealthIdStore.noOfHIDsLeft(), is(4));
        verify(0, postRequestedFor(urlMatching(SIGN_IN_PATH)));
        verify(0, getRequestedFor(urlMatching(NEXT_BLOCK_PATH)));
    }

    @Test
    public void shouldReplenishFromHIDServiceHIDCountReachesToThreshold() throws Exception {
        List<String> healthIdBlock = Lists.newArrayList("healthId1", "healthId2");
        mciHealthIdStore.addMciHealthIds(healthIdBlock);
        writeHIDBlockToFile(healthIdBlock);

        UUID token = UUID.randomUUID();
        String idpResponse = "{\"access_token\" : \"" + token.toString() + "\"}";
        setUpIDPStub(idpResponse);
        setupNextBlockStub(token);
        healthIdService.replenishIfNeeded();

        assertThat(mciHealthIdStore.noOfHIDsLeft(), is(2 + mciProperties.getHealthIdBlockSize()));
        verify(1, postRequestedFor(urlMatching(SIGN_IN_PATH)));
        verify(1, getRequestedFor(urlPathMatching(NEXT_BLOCK_PATH)));

        List<String> hids = readHIDsFromFile();
        List<String> expectedHIDs = getHIDs();
        expectedHIDs.addAll(healthIdBlock);
        assertEquals(hids.size(), expectedHIDs.size());
        assertTrue(hids.containsAll(expectedHIDs));
    }

    @Test
    public void shouldClearTheTokenWhenUnauthorized() throws Exception {
        UUID token = UUID.randomUUID();
        String idpResponse = "{\"access_token\" : \"" + token.toString() + "\"}";
        setUpIDPStub(idpResponse);

        assertEquals(0, mciHealthIdStore.noOfHIDsLeft());

        stubFor(get(urlPathMatching(NEXT_BLOCK_PATH))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_UNAUTHORIZED))
        );

        healthIdService.replenishIfNeeded();

        assertEquals(0, mciHealthIdStore.noOfHIDsLeft());
        assertFalse(identityStore.hasIdentityToken());
    }

    @Test
    public void shouldTellWhetherAOrgHealthIdIsValid() throws Exception {
        UUID token = UUID.randomUUID();
        String idpResponse = "{\"access_token\" : \"" + token.toString() + "\"}";
        String checkHidResponse = "{\"availability\" : true}";
        setUpIDPStub(idpResponse);
        setupCheckHIDStub(token, checkHidResponse);

        Map result = healthIdService.validateHIDForOrg("hid", "12345");
        assertTrue((Boolean) result.get("availability"));

        verify(1, postRequestedFor(urlMatching(SIGN_IN_PATH)));
        verify(1, getRequestedFor(urlPathMatching(CHECK_HID_PATH)));
    }

    @Test
    public void shouldInitializeHealthIdStoreWithUnusedHIDsFromFile() throws Exception {
        String healthId1 = "healthId1";
        String healthId2 = "healthId2";
        String healthId3 = "healthId3";
        String healthId4 = "healthId4";
        List<String> healthIdBlock = Lists.newArrayList(healthId1, healthId2, healthId3, healthId4);
        writeHIDBlockToFile(healthIdBlock);

        Patient patient = new Patient();
        patient.setHealthId(healthId2);
        cqlTemplate.insert(patient);
        Patient patient2 = new Patient();
        patient2.setHealthId(healthId3);
        cqlTemplate.insert(patient2);

        assertEquals(0, mciHealthIdStore.noOfHIDsLeft());

        healthIdService.populateHidStore();

        assertEquals(2, mciHealthIdStore.noOfHIDsLeft());
        assertTrue(Lists.newArrayList(healthId1, healthId4).containsAll(mciHealthIdStore.getAll()));
    }

    private void setUpMarkUsedStub(UUID token) {
        stubFor(put(urlPathMatching(MARK_USED_PATH))
                .withHeader(AUTH_TOKEN_KEY, equalTo(token.toString()))
                .withHeader(CLIENT_ID_KEY, equalTo(mciProperties.getIdpClientId()))
                .withHeader(FROM_KEY, equalTo(mciProperties.getIdpClientEmail()))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody("Accepted")
                ));
    }

    private void setupNextBlockStub(UUID token) throws IOException {
        stubFor(get(urlPathMatching(NEXT_BLOCK_PATH))
                .withHeader(AUTH_TOKEN_KEY, equalTo(token.toString()))
                .withHeader(CLIENT_ID_KEY, equalTo(mciProperties.getIdpClientId()))
                .withHeader(FROM_KEY, equalTo(mciProperties.getIdpClientEmail()))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody(getHidResponse())
                ));
    }

    private void setupCheckHIDStub(UUID token, String checkHidResponse) throws IOException {
        stubFor(get(urlPathMatching(CHECK_HID_PATH))
                .withHeader(AUTH_TOKEN_KEY, equalTo(token.toString()))
                .withHeader(CLIENT_ID_KEY, equalTo(mciProperties.getIdpClientId()))
                .withHeader(FROM_KEY, equalTo(mciProperties.getIdpClientEmail()))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody(checkHidResponse)
                ));
    }

    private void setUpIDPStub(String idpResponse) {
        stubFor(post(urlMatching(SIGN_IN_PATH))
                .withHeader(AUTH_TOKEN_KEY, equalTo(mciProperties.getIdpAuthToken()))
                .withHeader(CLIENT_ID_KEY, equalTo(mciProperties.getIdpClientId()))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody(idpResponse)
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

    private List<String> readHIDsFromFile() throws IOException {
        File hidLocalStorageFile = new File(mciProperties.getHidLocalStoragePath());
        assertTrue(hidLocalStorageFile.exists());
        return IOUtils.readLines(new FileInputStream(hidLocalStorageFile), "UTF-8");
    }

    private void writeHIDBlockToFile(List<String> healthIdBlock) throws IOException {
        IOUtils.writeLines(healthIdBlock, IOUtils.LINE_SEPARATOR_UNIX, new FileOutputStream(new File(mciProperties.getHidLocalStoragePath())), "UTF-8");
    }

}