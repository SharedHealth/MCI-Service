package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.util.BaseIntegrationTest;
import org.sharedhealth.mci.web.model.Facility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class FacilityRepositoryIT extends BaseIntegrationTest {

    private final String facilityName = "DMC";
    private final String facilityType = "DIST";
    private final String catchments = "101010";
    private final String location = "101010";
    private Facility facility;
    private String facilityId = "12345";

    @Autowired
    private FacilityRepository facilityRepository;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        facility = new Facility();
        facility.setId(facilityId);
        facility.setName(facilityName);
        facility.setType(facilityType);
        facility.setCatchments(catchments);
        facility.setLocation(location);
    }

    @Test
    public void returnFacilityByFacilityId() throws ExecutionException, InterruptedException {

        facilityRepository.save(facility, 86400);
        Facility savedFacility = facilityRepository.find(facilityId);

        assertThat(savedFacility, is(facility));
    }

    @Test
    public void shouldReturnNullIfFacilityNotFound() throws ExecutionException, InterruptedException {
        assertNull(facilityRepository.find("randomid"));
    }
}