package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.utils.DateUtil;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientDupeRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientDupeData;
import org.sharedhealth.mci.web.model.PatientDupe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PatientDupeService {

    private PatientDupeRepository dupeRepository;

    @Autowired
    public PatientDupeService(PatientDupeRepository dupeRepository) {
        this.dupeRepository = dupeRepository;
    }

    public List<PatientDupeData> findAllByCatchment(Catchment catchment) {
        List<PatientDupe> dupes = dupeRepository.findAllByCatchment(catchment);
        return buildDupeData(dupes);
    }

    private List<PatientDupeData> buildDupeData(List<PatientDupe> dupes) {
        List<PatientDupeData> dupeDataList = new ArrayList<>();
        for (PatientDupe dupe : dupes) {
            dupeDataList.add(buildDupeData(dupe));
        }
        return dupeDataList;
    }

    private PatientDupeData buildDupeData(PatientDupe dupe) {
        PatientDupeData dupeData = new PatientDupeData();
        dupeData.setHealthId1(dupe.getHealth_id1());
        dupeData.setHealthId2(dupe.getHealth_id2());
        dupeData.setReasons(dupe.getReasons());
        dupeData.setCreatedAt(DateUtil.toIsoFormat(dupe.getCreated_at()));
        return dupeData;
    }
}
