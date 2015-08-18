package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.repository.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static java.util.UUID.fromString;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.sharedhealth.mci.domain.repository.MarkerRepositoryQueryBuilder.buildUpdateMarkerBatch;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

@Component
public class PatientSearchMappingRepository extends BaseRepository {

    @Autowired
    public PatientSearchMappingRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }


    public UUID findLatestMarker() {
        String searchMappingMarkerQuery = select().from(CF_MARKER).where(eq(TYPE, SEARCH_MAPPING_MARKER_TYPE)).limit(1).toString();
        List<Marker> markers = cassandraOps.select(searchMappingMarkerQuery, Marker.class);
        if (isEmpty(markers)) {
            return null;
        }
        return fromString(markers.get(0).getMarker());
    }


    public void saveMappings(PatientData patientData) {
        Batch batch = QueryBuilder.batch();

        String healthId = patientData.getHealthId();
        CassandraConverter converter = cassandraOps.getConverter();

        buildCreateMappingStmt(healthId, patientData.getNationalId(), CF_NID_MAPPING, converter, batch);
        buildCreateMappingStmt(healthId, patientData.getBirthRegistrationNumber(), CF_BRN_MAPPING, converter, batch);
        buildCreateMappingStmt(healthId, patientData.getUid(), CF_UID_MAPPING, converter, batch);
        buildCreateMappingStmt(healthId, patientData.getHouseholdCode(), CF_HOUSEHOLD_CODE_MAPPING, converter, batch);
        if (patientData.getPhoneNumber() != null) {
            buildCreateMappingStmt(healthId, patientData.getPhoneNumber().getNumber(), CF_PHONE_NUMBER_MAPPING, converter, batch);
        }

        buildCreateNameMappingStmt(patientData, converter, batch);
        buildCreateCatchmentMappingsStmt(patientData.getCatchment(), patientData.getUpdatedAt(), patientData.getHealthId(), converter, batch);

        cassandraOps.execute(batch);
    }

    public void updateMarkerTable(PatientUpdateLog lastUpdateLog) {
        String marker = lastUpdateLog.getEventId().toString();
        Batch batch = batch();
        buildUpdateMarkerBatch(SEARCH_MAPPING_MARKER_TYPE, marker, cassandraOps.getConverter(), batch);
        cassandraOps.execute(batch);
    }

    private void buildCreateMappingStmt(String healthId, String id, String columnFamily, CassandraConverter converter, Batch batch) {
        if (isNotBlank(id)) {
            Object objectToSave = buildObjectToSave(id, healthId, columnFamily);
            batch.add(createInsertQuery(columnFamily, objectToSave, null, converter));
        }
    }

    private static Object buildObjectToSave(String id, String healthId, String columnFamily) {
        Object objectToSave = null;
        switch (columnFamily) {
            case CF_NID_MAPPING:
                objectToSave = new NidMapping(id, healthId);
                break;
            case CF_BRN_MAPPING:
                objectToSave = new BrnMapping(id, healthId);
                break;
            case CF_UID_MAPPING:
                objectToSave = new UidMapping(id, healthId);
                break;
            case CF_PHONE_NUMBER_MAPPING:
                objectToSave = new PhoneNumberMapping(id, healthId);
                break;
            case CF_HOUSEHOLD_CODE_MAPPING:
                objectToSave = new HouseholdCodeMapping(id, healthId);
                break;
        }
        return objectToSave;
    }

    private static void buildCreateNameMappingStmt(PatientData patientData, CassandraConverter converter, Batch batch) {
        String healthId = patientData.getHealthId();
        String divisionId = patientData.getAddress().getDivisionId();
        String districtId = patientData.getAddress().getDistrictId();
        String upazilaId = patientData.getAddress().getUpazilaId();
        String givenName = patientData.getGivenName();
        String surname = patientData.getSurName();

        if (isNotBlank(healthId) && isNotBlank(divisionId) && isNotBlank(districtId) && isNotBlank(upazilaId)
                && isNotBlank(givenName) && isNotBlank(surname)) {
            NameMapping mapping = new NameMapping(divisionId, districtId, upazilaId, givenName.toLowerCase(),
                    surname.toLowerCase(), patientData.getHealthId());
            batch.add(createInsertQuery(CF_NAME_MAPPING, mapping, null, converter));
        }

    }

    private static void buildCreateCatchmentMappingsStmt(Catchment catchment, UUID lastUpdated, String healthId,
                                                         CassandraConverter converter, Batch batch) {
        for (String catchmentId : catchment.getAllIds()) {
            CatchmentMapping mapping = new CatchmentMapping(catchmentId, lastUpdated, healthId);
            batch.add(createInsertQuery(CF_CATCHMENT_MAPPING, mapping, null, converter));
        }
    }
}
