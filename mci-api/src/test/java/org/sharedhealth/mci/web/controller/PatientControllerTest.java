package org.sharedhealth.mci.web.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.mci.web.model.Address;
import org.sharedhealth.mci.web.model.Location;
import org.sharedhealth.mci.web.model.Patient;
import org.sharedhealth.mci.web.service.LocationService;
import org.sharedhealth.mci.web.service.PatientService;
import org.sharedhealth.mci.web.utils.concurrent.PreResolvedListenableFuture;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@RunWith(MockitoJUnitRunner.class)
public class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @Mock
    private LocationService locationService;

    @Mock
    private LocalValidatorFactoryBean localValidatorFactoryBean;

    private Patient patient;
    private Location location;
    private MockMvc mockMvc;
    private String nationalId = "1234567890123";
    private String birthRegistrationNumber = "12345678901234567";
    private String name = "Roni Kumar Saha";
    private String uid = "11111111111";
    public static final String API_END_POINT = "/api/v1/patients";
    public static final String GEO_CODE = "1004092001";

    @Before
    public void setup() {
        initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PatientController(patientService))
                .setValidator(validator())
                .build();

        patient = new Patient();
        patient.setNationalId(nationalId);
        patient.setBirthRegistrationNumber(birthRegistrationNumber);
        patient.setFirstName("Scott");
        patient.setLastName("Tiger");
        patient.setGender("1");
        patient.setDateOfBirth("2014-12-01");

        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazillaId("09");
        address.setCityCorporation("20");
        address.setVillage("10");
        address.setWard("01");
        address.setCountry("103");

        patient.setAddress(address);

        location = new Location();

        location.setGeoCode(GEO_CODE);
        location.setDivisionId("10");
        location.setDistrictId("04");
        location.setUpazillaId("09");
        location.setPaurashavaId("20");
        location.setUnionId("01");

    }

    @Test
    public void shouldCreatePatientAndReturnHealthId() throws Exception {
        String json = new ObjectMapper().writeValueAsString(patient);
        String healthId = "healthId-100";
        when(locationService.findByGeoCode(GEO_CODE)).thenReturn(new PreResolvedListenableFuture<>(location));
        when(patientService.create(patient)).thenReturn(new PreResolvedListenableFuture<>(healthId));

        mockMvc.perform(post(API_END_POINT).content(json).contentType(APPLICATION_JSON))
                .andExpect(request().asyncResult(new ResponseEntity<>(healthId, CREATED)));
        verify(patientService).create(patient);
    }

    @Test
    public void shouldFindPatientByHealthId() throws Exception {
        String healthId = "healthId-100";
        when(patientService.findByHealthId(healthId)).thenReturn(new PreResolvedListenableFuture<>(patient));
        mockMvc.perform(get(API_END_POINT + "/" + healthId))
                .andExpect(request().asyncResult(new ResponseEntity<>(patient, OK)));
        verify(patientService).findByHealthId(healthId);
    }

    @Test
    public void shouldFindPatientByNationalId() throws Exception {
        when(patientService.findByNationalId(nationalId)).thenReturn(new PreResolvedListenableFuture<>(patient));
        mockMvc.perform(get(API_END_POINT + "?nid=" + nationalId))
                .andExpect(request().asyncResult(new ResponseEntity<>(patient, OK)));
        verify(patientService).findByNationalId(nationalId);
    }

    @Test
    public void shouldFindPatientByBirthRegistrationNumber() throws Exception {
        when(patientService.findByBirthRegistrationNumber(birthRegistrationNumber)).thenReturn(new PreResolvedListenableFuture<>(patient));
        mockMvc.perform(get(API_END_POINT + "?bin_brn=" + birthRegistrationNumber))
                .andExpect(request().asyncResult(new ResponseEntity<>(patient, OK)));
        verify(patientService).findByBirthRegistrationNumber(birthRegistrationNumber);
    }

    @Test
    public void shouldFindPatientByUid() throws Exception {
        when(patientService.findByUid(uid)).thenReturn(new PreResolvedListenableFuture<>(patient));
        mockMvc.perform(get(API_END_POINT + "?uid=" + uid))
                .andExpect(request().asyncResult(new ResponseEntity<>(patient, OK)));
        verify(patientService).findByUid(uid);
    }

    @Test
    public void shouldFindPatientByName() throws Exception {
        when(patientService.findByName(name.toLowerCase())).thenReturn(new PreResolvedListenableFuture<>(patient));
        mockMvc.perform(get(API_END_POINT + "?name=" + name))
                .andExpect(request().asyncResult(new ResponseEntity<>(patient, OK)));
        verify(patientService).findByName(name.toLowerCase());
    }

    private LocalValidatorFactoryBean validator() {
        return localValidatorFactoryBean;
    }
}

