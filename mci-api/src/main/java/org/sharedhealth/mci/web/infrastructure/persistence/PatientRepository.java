package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.utils.UidGenerator;
import org.sharedhealth.mci.web.exception.HealthIDExistException;
import org.sharedhealth.mci.web.exception.PatientNotFoundException;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.handler.PatientFilter;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.SearchQuery;
import org.sharedhealth.mci.web.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;
import static org.springframework.data.cassandra.core.CassandraTemplate.toUpdateQuery;

@Component
public class PatientRepository extends BaseRepository {

    protected static final Logger logger = LoggerFactory.getLogger(PatientRepository.class);
    private static final UidGenerator uid = new UidGenerator();
    private PatientMapper mapper;

    @Autowired
    public PatientRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations, PatientMapper mapper) {
        super(cassandraOperations);
        this.mapper = mapper;
    }

    public MCIResponse create(PatientData patientData) {
        if (!StringUtils.isBlank(patientData.getHealthId())) {
            DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patientData, "patient");
            bindingResult.addError(new FieldError("patient", "hid", "3001"));
            throw new HealthIDExistException(bindingResult);
        }

        PatientData existingPatient = getExistingPatient(patientData);
        if (existingPatient != null) {
            return update(patientData, existingPatient.getHealthId());
        }

        Patient p = mapper.map(patientData, new PatientData());
        p.setHealthId(uid.getId());
        p.setCreatedAt(new Date());
        p.setUpdatedAt(new Date());
        p.setSurName(patientData.getSurName());

        cassandraOperations.execute(buildSaveBatch(p));
        return new MCIResponse(p.getHealthId(), HttpStatus.CREATED);
    }

    private PatientData getExistingPatient(PatientData mapper) {
        if (!mapper.containsMultipleIdentifier()) {
            return null;
        }
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setNid(mapper.getNationalId());
        searchQuery.setBin_brn(mapper.getBirthRegistrationNumber());
        searchQuery.setUid(mapper.getUid());
        List<PatientData> mappers = findAllByQuery(searchQuery);
        if (isNotEmpty(mappers)) {
            return mappers.get(0);
        }
        return null;
    }

    private Batch buildSaveBatch(Patient patient) {
        String healthId = patient.getHealthId();
        Batch batch = QueryBuilder.batch();
        CassandraConverter converter = cassandraOperations.getConverter();

        batch.add(createInsertQuery(CF_PATIENT, patient, null, converter));

        String nationalId = patient.getNationalId();
        if (isNotBlank(nationalId)) {
            batch.add(createInsertQuery(CF_NID_MAPPING, new NidMapping(nationalId, healthId), null, converter));
        }

        String brn = patient.getBirthRegistrationNumber();
        if (isNotBlank(brn)) {
            batch.add(createInsertQuery(CF_BRN_MAPPING, new BrnMapping(brn, healthId), null, converter));
        }

        String uid = patient.getUid();
        if (isNotBlank(uid)) {
            batch.add(createInsertQuery(CF_UID_MAPPING, new UidMapping(uid, healthId), null, converter));
        }

        String phoneNumber = patient.getCellNo();
        if (isNotBlank(phoneNumber)) {
            batch.add(createInsertQuery(CF_PHONE_NUMBER_MAPPING,
                    new PhoneNumberMapping(phoneNumber, healthId), null, converter));
        }

        String divisionId = patient.getDivisionId();
        String districtId = patient.getDistrictId();
        String upazilaId = patient.getUpazillaId();
        String givenName = patient.getGivenName();
        String surname = patient.getSurName();
        if (isNotBlank(divisionId) && isNotBlank(districtId) && isNotBlank(upazilaId) && isNotBlank(givenName) && isNotBlank(surname)) {
            NameMapping mapping = new NameMapping(divisionId, districtId, upazilaId, givenName.toLowerCase(), surname.toLowerCase(), healthId);
            batch.add(createInsertQuery(CF_NAME_MAPPING, mapping, null, converter));
        }
        return batch;
    }

    public PatientData findByHealthId(final String healthId) {
        Patient patient = cassandraOperations.selectOneById(Patient.class, healthId);
        if (patient != null) {
            return mapper.map(patient);
        }
        throw new PatientNotFoundException("No patient found with health id: " + healthId);
    }

    public List<PatientData> findAllByQuery(SearchQuery searchQuery) {
        return filterPatients(findProbables(searchQuery), searchQuery);
    }

    private List<PatientData> findProbables(SearchQuery searchQuery) {
        List<PatientData> dataList = new ArrayList<>();
        String query = null;

        if (isNotBlank(searchQuery.getNid())) {
            query = buildFindByNidQuery(searchQuery.getNid());

        } else if (isNotBlank(searchQuery.getBin_brn())) {
            query = buildFindByBrnQuery(searchQuery.getBin_brn());

        } else if (isNotBlank(searchQuery.getUid())) {
            query = buildFindByUidQuery(searchQuery.getUid());

        } else if (isNotBlank(searchQuery.getPhone_no())) {
            query = buildFindByPhoneNumberQuery(searchQuery.getPhone_no());

        } else if (isNotBlank(searchQuery.getPresent_address()) && isNotBlank(searchQuery.getGiven_name())) {
            query = buildFindByNameQuery(searchQuery.getDivisionId(), searchQuery.getDistrictId(),
                    searchQuery.getUpazilaId(), searchQuery.getGiven_name(), searchQuery.getSur_name());
        }

        if (isNotBlank(query)) {
            List<String> healthIds = cassandraOperations.queryForList(query, String.class);
            if (isNotEmpty(healthIds)) {
                String[] values = healthIds.toArray(new String[healthIds.size()]);
                List<Patient> patients = cassandraOperations.select(buildFindByHidsQuery(values), Patient.class);
                for (Patient patient : patients) {
                    dataList.add(mapper.map(patient));
                }
            }
        }
        return dataList;
    }

    private List<PatientData> filterPatients(List<PatientData> patients, SearchQuery searchQuery) {
        List<PatientData> result = new ArrayList<>();
        for (PatientData patient : patients) {
            if (isMatchingPatient(patient, searchQuery)) {
                result.add(patient);
            }
        }
        return result;
    }

    private boolean isMatchingPatient(PatientData p, SearchQuery searchQuery) {
        if (isNotBlank(searchQuery.getNid()) && !p.getNationalId().equals(searchQuery.getNid())) {
            return false;
        }
        if (isNotBlank(searchQuery.getBin_brn()) && !searchQuery.getBin_brn().equals(p.getBirthRegistrationNumber())) {
            return false;
        }
        if (isNotBlank(searchQuery.getUid()) && !searchQuery.getUid().equals(p.getUid())) {
            return false;
        }
        if (isNotBlank(searchQuery.getArea_code()) && !searchQuery.getArea_code().equals(p.getPhoneNumber().getAreaCode())) {
            return false;
        }
        if (isNotBlank(searchQuery.getPhone_no()) && !searchQuery.getPhone_no().equals(p.getPhoneNumber().getNumber())) {
            return false;
        }
        Address address = p.getAddress();
        if (isNotBlank(searchQuery.getDivisionId()) && !searchQuery.getDivisionId().equals(address.getDivisionId())) {
            return false;
        }
        if (isNotBlank(searchQuery.getDistrictId()) && !searchQuery.getDistrictId().equals(address.getDistrictId())) {
            return false;
        }
        if (isNotBlank(searchQuery.getUpazilaId()) && !searchQuery.getUpazilaId().equals(address.getUpazillaId())) {
            return false;
        }
        if (isNotBlank(searchQuery.getGiven_name()) && !searchQuery.getGiven_name().equalsIgnoreCase(p.getGivenName())) {
            return false;
        }
        if (isNotBlank(searchQuery.getSur_name()) && !searchQuery.getSur_name().equalsIgnoreCase(p.getSurName())) {
            return false;
        }
        return true;
    }

    public MCIResponse update(PatientData patientData, final String hid) {
        if (patientData.getHealthId() != null && !StringUtils.equals(patientData.getHealthId(), hid)) {
            DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patientData, "patient");
            bindingResult.addError(new FieldError("patient", "hid", "1004"));
            throw new ValidationException(bindingResult);
        }
        PatientData existingPatient;
        try {
            existingPatient = findByHealthId(hid);
        } catch (Exception e) {
            throw new PatientNotFoundException("No patient found with health id: " + hid);
        }
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("approvalFeilds.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read approval property file.", e);
        }
        //TODO : move to mapper
        PatientData patientToSave = new PatientData();
        patientToSave.setHealthId(hid);
        patientData.setHealthId(hid);
        PatientFilter patientFilter = new PatientFilter(properties, existingPatient, patientData, patientToSave);

        Approval approval = patientFilter.filter();

        String fullName = "";

        if (patientData.getGivenName() != null) {
            fullName = patientData.getGivenName();
        }

        if (patientData.getSurName() != null) {
            fullName = fullName + " " + patientData.getSurName();
        }

        Patient patient = mapper.map(patientToSave, existingPatient);
        patient.setHealthId(hid);
        patient.setFullName(fullName);
        patient.setUpdatedAt(new Date());

        CassandraConverter converter = cassandraOperations.getConverter();
        Batch batch = QueryBuilder.batch();
        batch.add(toUpdateQuery("patient", patient, null, converter));
        if (approval != null) {
            batch.add(toUpdateQuery("approval", approval, null, converter));
        }
        cassandraOperations.execute(batch);
        return new MCIResponse(patient.getHealthId(), HttpStatus.ACCEPTED);
    }

    public List<PatientData> findAllByLocations(List<String> locations, String start, Date since) {
        List<PatientData> dataList = new ArrayList<>();
        int limit = PER_PAGE_LIMIT;

        if (locations != null && locations.size() > 0) {
            String locationPointer = getLocationPointer(locations, start, null);

            for (String catchment : locations) {
                if (dataList.size() == 0 && !isLocationBelongsToCatchment(locationPointer, catchment)) {
                    continue;
                }
                try {
                    dataList.addAll(this.findAllByLocation(catchment, start, limit, since));
                    if (dataList.size() < PER_PAGE_LIMIT) {
                        start = null;
                        limit = PER_PAGE_LIMIT - dataList.size();
                        locationPointer = null;
                    } else {
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            return this.findAllByLocation(null, start, limit, since);
        }
        return dataList;
    }

    List<PatientData> findAllByLocation(String location, String start, int limit, Date since) {
        Select select = select().from("patient");
        select.where(QueryBuilder.eq(getAddressHierarchyField(location.length()), location));

        if (isNotBlank(start)) {
            select.where(QueryBuilder.gt(QueryBuilder.token("health_id"), QueryBuilder.raw("token('" + start + "')")));
        }
        if (since != null) {
            select.where(QueryBuilder.gt("updated_at", since));
            select.allowFiltering();
        }

        if (limit > 0) {
            select.limit(limit);
        }
        return mapper.map(cassandraOperations.select(select, Patient.class));
    }

    private String getLocationPointer(List<String> locations, String start, String d) {
        if (locations.size() > 1 && isNotBlank(start)) {
            PatientData p = findByHealthId(start);
            return p.getAddress().getGeoCode();
        }
        return d;
    }

    private boolean isLocationBelongsToCatchment(String location, String catchment) {
        return StringUtils.isBlank(location) || location.startsWith(catchment);
    }

    private String getAddressHierarchyField(int length) {
        return "location_level" + (length / 2);
    }
}
