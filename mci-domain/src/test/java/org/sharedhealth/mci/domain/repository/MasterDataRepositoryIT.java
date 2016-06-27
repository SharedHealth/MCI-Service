package org.sharedhealth.mci.domain.repository;

import org.junit.Before;
import org.junit.Test;
import org.sharedhealth.mci.domain.model.MasterData;
import org.sharedhealth.mci.domain.util.BaseRepositoryIT;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class MasterDataRepositoryIT extends BaseRepositoryIT {

    @Autowired
    private MasterDataRepository masterDataRepository;

    private MasterData masterData;
    private String type = "relations";
    private String key = "FTH";

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        String value = "father";
        masterData = new MasterData(type, key, value);
    }

    @Test
    public void shouldReturnNull_IfDataDoesNotExistForGivenKeyForValidType() throws ExecutionException, InterruptedException {
        assertNull(masterDataRepository.findDataByKey(type, "random string"));
    }

    @Test
    public void shouldFindDataWithMatchingKeyType() throws ExecutionException, InterruptedException {
        final MasterData m = masterDataRepository.findDataByKey(type, key);

        assertNotNull(m);
        assertEquals(masterData, m);
    }
}