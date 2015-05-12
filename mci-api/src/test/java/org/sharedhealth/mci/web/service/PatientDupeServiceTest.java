package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientDupeRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientDupeData;
import org.sharedhealth.mci.web.model.PatientDupe;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.asSet;

public class PatientDupeServiceTest {

    @Mock
    private PatientDupeRepository dupeRepository;

    private PatientDupeService dupeService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        dupeService = new PatientDupeService(dupeRepository);
    }

    @Test
    public void shouldFindAllByCatchment() {
        Catchment catchment = new Catchment("102030");
        when(dupeRepository.findAllByCatchment(catchment)).thenReturn(buildPatientDupes());
        List<PatientDupeData> dupeDataList = dupeService.findAllByCatchment(catchment);

        assertTrue(isNotEmpty(dupeDataList));
        assertEquals(5, dupeDataList.size());
    }

    private List<PatientDupe> buildPatientDupes() {
        List<PatientDupe> dupes = new ArrayList<>();
        dupes.add(new PatientDupe("A102030", "99001", "99002", asSet("nid", "phoneNo"), timeBased()));
        dupes.add(new PatientDupe("A102030", "99003", "99004", asSet("phoneNo"), timeBased()));
        dupes.add(new PatientDupe("A102030", "99005", "99006", asSet("nid"), timeBased()));
        dupes.add(new PatientDupe("A102030", "99007", "99008", asSet("nid"), timeBased()));
        dupes.add(new PatientDupe("A102030", "99009", "99010", asSet("nid", "phoneNo"), timeBased()));
        return dupes;
    }
}