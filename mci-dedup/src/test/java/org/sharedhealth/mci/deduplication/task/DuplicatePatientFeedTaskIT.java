package org.sharedhealth.mci.deduplication.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.deduplication.model.DuplicatePatient;
import org.sharedhealth.mci.deduplication.repository.DuplicatePatientRepository;
import org.sharedhealth.mci.domain.model.Address;
import org.sharedhealth.mci.domain.model.Catchment;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.PhoneNumber;
import org.sharedhealth.mci.domain.repository.PatientRepository;
import org.sharedhealth.mci.domain.util.BaseIntegrationTest;
import org.sharedhealth.mci.searchmapping.repository.PatientSearchMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.sharedhealth.mci.deduplication.rule.DuplicatePatientRule.*;
import static org.sharedhealth.mci.domain.util.DateUtil.parseDate;

@RunWith(SpringJUnit4ClassRunner.class)
public class DuplicatePatientFeedTaskIT extends BaseIntegrationTest {

    @Autowired
    private DuplicatePatientRepository duplicatePatientRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientSearchMappingRepository searchMappingRepository;

    @Autowired
    private DuplicatePatientFeedTask duplicatePatientFeedTask;

    @Test
    public void shouldCreateDuplicatesForAddressAndGivenName() throws Exception {
        String healthId = patientRepository.create(buildPatient()).getId();
        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId));

        duplicatePatientFeedTask.execute();
        List<DuplicatePatient> byCatchment = duplicatePatientRepository.findByCatchment(new Catchment("1020"), null, null, 25);
        assertEquals(0, byCatchment.size());

        String healthId2 = patientRepository.create(buildPatient()).getId();
        assertNotNull(healthId2);
        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId2));

        duplicatePatientFeedTask.execute();
        byCatchment = duplicatePatientRepository.findByCatchment(new Catchment("1020"), null, null, 25);
        assertEquals(1, byCatchment.size());
    }

    @Test
    public void shouldCreateDuplicatesForAddressAndFullName() throws Exception {
        PatientData patient1 = buildPatient();
        patient1.setSurName("Rotter");
        String healthId1 = patientRepository.create(patient1).getId();
        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId1));


        PatientData patient2 = buildPatient();
        patient2.setSurName("rOTter");
        String healthId2 = patientRepository.create(patient2).getId();
        assertNotNull(healthId2);
        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId2));

        duplicatePatientFeedTask.execute();
        List<DuplicatePatient> byCatchment = duplicatePatientRepository.findByCatchment(new Catchment("1020"), null, null, 25);
        assertEquals(1, byCatchment.size());
        assertDuplicateReason(byCatchment, healthId1, healthId2, DUPLICATE_REASON_NAME_ADDRESS);
    }

    @Test
    public void shouldCreateDuplicatesForMultipleReasons() throws Exception {
        String nationalId = "123456";
        String birthRegistrationNumber = "987654";

        PatientData patient1 = buildPatient();
        patient1.setSurName("Rotter");
        patient1.setNationalId(nationalId);
        patient1.setBirthRegistrationNumber(birthRegistrationNumber);
        String healthId1 = patientRepository.create(patient1).getId();
        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId1));

        PatientData patient2 = buildPatient();
        patient2.setBirthRegistrationNumber(birthRegistrationNumber);
        patient2.setNationalId(nationalId);
        patient2.setSurName("rOTter");
        String healthId2 = patientRepository.create(patient2).getId();
        assertNotNull(healthId2);
        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId2));

        duplicatePatientFeedTask.execute();
        List<DuplicatePatient> byCatchment = duplicatePatientRepository.findByCatchment(new Catchment("1020"), null, null, 25);
        assertEquals(1, byCatchment.size());
        assertDuplicateReason(byCatchment, healthId1, healthId2, DUPLICATE_REASON_NID);
        assertDuplicateReason(byCatchment, healthId1, healthId2, DUPLICATE_REASON_NAME_ADDRESS);
        assertDuplicateReason(byCatchment, healthId1, healthId2, DUPLICATE_REASON_BRN);
    }

    @Test
    public void shouldCreateMultipleDuplicates() throws Exception {
        String nationalId = "123456";
        String birthRegistrationNumber = "987654";

        PatientData patient1 = buildPatient();
        patient1.setNationalId(nationalId);
        String healthId1 = patientRepository.create(patient1).getId();
        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId1));
        duplicatePatientFeedTask.execute();

        PatientData patient2 = buildPatient();
        patient2.setBirthRegistrationNumber(birthRegistrationNumber);
        patient2.setNationalId(nationalId);
        patient2.setGivenName("Sad");
        String healthId2 = patientRepository.create(patient2).getId();
        assertNotNull(healthId2);
        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId2));

        duplicatePatientFeedTask.execute();
        List<DuplicatePatient> byCatchment = duplicatePatientRepository.findByCatchment(new Catchment("1020"), null, null, 25);
        assertEquals(1, byCatchment.size());

        PatientData patient3 = buildPatient();
        patient3.setBirthRegistrationNumber(birthRegistrationNumber);
        patient3.setGivenName("Unhappy");
        String healthId3 = patientRepository.create(patient3).getId();
        assertNotNull(healthId3);
        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId3));

        duplicatePatientFeedTask.execute();
        byCatchment = duplicatePatientRepository.findByCatchment(new Catchment("1020"), null, null, 25);
        assertEquals(2, byCatchment.size());

        PatientData patient4 = buildPatient();
        String healthId4 = patientRepository.create(patient4).getId();
        searchMappingRepository.saveMappings(patientRepository.findByHealthId(healthId4));
        duplicatePatientFeedTask.execute();
        byCatchment = duplicatePatientRepository.findByCatchment(new Catchment("1020"), null, null, 25);
        assertEquals(3, byCatchment.size());

        assertDuplicateReason(byCatchment, healthId2, healthId1, DUPLICATE_REASON_NID);
        assertDuplicateReason(byCatchment, healthId3, healthId2, DUPLICATE_REASON_BRN);
        assertDuplicateReason(byCatchment, healthId4, healthId1, DUPLICATE_REASON_NAME_ADDRESS);
    }

    private void assertDuplicateReason(List<DuplicatePatient> byCatchment, String healthId1, String healthId2, String duplicateReason) {
        for (DuplicatePatient duplicatePatient : byCatchment) {
            if (duplicatePatient.getHealth_id1() == healthId1 && duplicatePatient.getHealth_id2() == healthId2) {
                assertTrue(duplicatePatient.getReasons().contains(duplicateReason));
            }
        }
    }

    private PatientData buildPatient() {
        PatientData patient = new PatientData();
        patient.setHealthId(String.valueOf(new Date().getTime()));
        patient.setGivenName("Happy");
        patient.setDateOfBirth(parseDate("2014-12-01"));
        patient.setGender("M");
        patient.setOccupation("01");
        patient.setEducationLevel("01");
        PhoneNumber phone = new PhoneNumber();
        phone.setNumber("22334455");
        patient.setPhoneNumber(phone);
        patient.setHouseholdCode("12345");
        patient.setAddress(new Address("10", "20", "30"));
        patient.setPermanentAddress(new Address("10", "20", "30"));
        patient.setRequester("Bahmni", null);
        return patient;
    }
}