package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.utils.UidGenerator;
import org.sharedhealth.mci.web.exception.HealthIDExistException;
import org.sharedhealth.mci.web.exception.PatientNotFoundException;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.handler.PatientFilter;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.model.*;
import org.sharedhealth.mci.web.utils.concurrent.SimpleListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;

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
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public PatientRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations) {
        super(cassandraOperations);
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

        Patient p = getEntityFromPatientMapper(patientData);

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
            return patient.convert();
        }
        throw new PatientNotFoundException("No patient found with health id: " + healthId);
    }

    private PatientData getPatientFromRow(Row r) {
        DatabaseRow row = new DatabaseRow(r);
        PatientData patientData = new PatientData();

        try {
            final String string = row.getString(RELATIONS);
            patientData.setRelations(getRelationsList(string));
        } catch (Exception e) {
            logger.debug(" Relations: [" + e.getMessage() + "]");
        }
        patientData.setHealthId(row.getString(HEALTH_ID));
        patientData.setNationalId(row.getString(NATIONAL_ID));
        patientData.setUid(row.getString(UID));
        patientData.setPlaceOfBirth(row.getString(PLACE_OF_BIRTH));

        patientData.setReligion(row.getString(RELIGION));
        patientData.setBloodGroup(row.getString(BLOOD_GROUP));
        patientData.setNameBangla(row.getString(FULL_NAME_BANGLA));
        patientData.setBirthRegistrationNumber(row.getString(BIN_BRN));
        patientData.setGivenName(row.getString(GIVEN_NAME));
        patientData.setSurName(row.getString(SUR_NAME));
        patientData.setDateOfBirth(row.getDateAsString(DATE_OF_BIRTH));
        patientData.setGender(row.getString(GENDER));
        patientData.setOccupation(row.getString(OCCUPATION));
        patientData.setEducationLevel(row.getString(EDU_LEVEL));
        patientData.setNationality(row.getString(NATIONALITY));
        patientData.setDisability(row.getString(DISABILITY));
        patientData.setEthnicity(row.getString(ETHNICITY));
        patientData.setIsAlive(row.getString(IS_ALIVE));
        patientData.setMaritalStatus(row.getString(MARITAL_STATUS));

        patientData.setPrimaryContact(row.getString(PRIMARY_CONTACT));

        Address address = new Address();
        address.setAddressLine(row.getString(ADDRESS_LINE));
        address.setDivisionId(row.getString(DIVISION_ID));
        address.setDistrictId(row.getString(DISTRICT_ID));
        address.setUpazillaId(row.getString(UPAZILLA_ID));
        address.setUnionId(row.getString(UNION_ID));
        address.setHoldingNumber(row.getString(HOLDING_NUMBER));
        address.setStreet(row.getString(STREET));
        address.setAreaMouja(row.getString(AREA_MOUJA));
        address.setVillage(row.getString(VILLAGE));
        address.setPostOffice(row.getString(POST_OFFICE));
        address.setPostCode(row.getString(POST_CODE));
        address.setWardId(row.getString(WARD));
        address.setThanaId(row.getString(THANA));
        address.setCityCorporationId(row.getString(CITY_CORPORATION));
        address.setCountryCode(row.getString(COUNTRY));
        patientData.setAddress(address);

        Address permanentaddress = new Address();
        permanentaddress.setAddressLine(row.getString(PERMANENT_ADDRESS_LINE));
        permanentaddress.setDivisionId(row.getString(PERMANENT_DIVISION_ID));
        permanentaddress.setDistrictId(row.getString(PERMANENT_DISTRICT_ID));
        permanentaddress.setUpazillaId(row.getString(PERMANENT_UPAZILLA_ID));
        permanentaddress.setUnionId(row.getString(PERMANENT_UNION_ID));
        permanentaddress.setHoldingNumber(row.getString(PERMANENT_HOLDING_NUMBER));
        permanentaddress.setStreet(row.getString(PERMANENT_STREET));
        permanentaddress.setAreaMouja(row.getString(PERMANENT_AREA_MOUJA));
        permanentaddress.setVillage(row.getString(PERMANENT_VILLAGE));
        permanentaddress.setPostOffice(row.getString(PERMANENT_POST_OFFICE));
        permanentaddress.setPostCode(row.getString(PERMANENT_POST_CODE));
        permanentaddress.setWardId(row.getString(PERMANENT_WARD));
        permanentaddress.setThanaId(row.getString(PERMANENT_THANA));
        permanentaddress.setCityCorporationId(row.getString(PERMANENT_CITY_CORPORATION));
        permanentaddress.setCountryCode(row.getString(PERMANENT_COUNTRY));

        PhoneNumber phoneNumber = new PhoneNumber();
        PhoneNumber primaryContactNumber = new PhoneNumber();

        phoneNumber.setNumber(row.getString(PHONE_NO));
        phoneNumber.setAreaCode(row.getString(PHONE_NUMBER_AREA_CODE));
        phoneNumber.setCountryCode(row.getString(PHONE_NUMBER_COUNTRY_CODE));
        phoneNumber.setExtension(row.getString(PHONE_NUMBER_EXTENSION));

        primaryContactNumber.setNumber(row.getString(PRIMARY_CONTACT_NO));
        primaryContactNumber.setAreaCode(row.getString(PRIMARY_CONTACT_NUMBER_AREA_CODE));
        primaryContactNumber.setCountryCode(row.getString(PRIMARY_CONTACT_NUMBER_COUNTRY_CODE));
        primaryContactNumber.setExtension(row.getString(PRIMARY_CONTACT_NUMBER_EXTENSION));

        if (primaryContactNumber.getNumber() != null) {
            patientData.setPrimaryContactNumber(primaryContactNumber);
        }

        if (phoneNumber.getNumber() != null) {
            patientData.setPhoneNumber(phoneNumber);
        }

        if (permanentaddress.getCountryCode() != null) {
            if (permanentaddress.getCountryCode() == "050" && permanentaddress.getDistrictId() != null) {
                patientData.setPermanentAddress(permanentaddress);
            }

            if (permanentaddress.getCountryCode() != "050") {
                patientData.setPermanentAddress(permanentaddress);
            }
        }

        patientData.setCreatedAt(row.getDate("created_at"));
        patientData.setUpdatedAt(row.getDate("updated_at"));

        return patientData;
    }

    private List<Relation> getRelationsList(String string) throws IOException {
        return mapper.readValue(string, mapper.getTypeFactory().constructCollectionType(List.class, Relation.class));
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
                    dataList.add(patient.convert());
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

        Patient patient = getEntityFromPatientData(patientToSave, existingPatient);
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

    public ListenableFuture<List<PatientData>> findAllByLocations(List<String> locations, String start, Date since) {

        final SettableFuture<List<PatientData>> result = SettableFuture.create();
        List<PatientData> patients = new ArrayList<>();

        int limit = PER_PAGE_LIMIT;

        if (locations != null && locations.size() > 0) {
            String locationPointer = getLocationPointer(locations, start, null);

            for (String catchment : locations) {
                if (patients.size() == 0 && !isLocationBelongsToCatchment(locationPointer, catchment)) {
                    continue;
                }

                ListenableFuture<List<PatientData>> res = this.findAllByLocation(catchment, start, limit, since);
                try {
                    List<PatientData> temp = res.get();
                    patients.addAll(temp);

                    if (patients.size() < PER_PAGE_LIMIT) {
                        start = null;
                        limit = PER_PAGE_LIMIT - patients.size();
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

        result.set(patients);
        return getPatientListListenableFuture(result);
    }

    public ListenableFuture<List<PatientData>> findAllByLocation(String location, String start, int limit, Date since) {

        Select select = select().from("patient");

        if (StringUtils.isBlank(location)) {
            final SettableFuture<List<PatientData>> result = SettableFuture.create();
            return getPatientListListenableFuture(result);
        }

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

        return getPatientListListenableFuture(select);
    }

    private ListenableFuture<List<PatientData>> getPatientListListenableFuture(final Select select) {
        return new SimpleListenableFuture<List<PatientData>, ResultSet>(
                cassandraOperations.queryAsynchronously(select)) {
            @Override
            protected List<PatientData> adapt(ResultSet resultSet) throws ExecutionException {
                List<PatientData> dataList = new ArrayList<>();
                for (Row result : resultSet.all()) {
                    PatientData data = getPatientFromRow(result);
                    dataList.add(data);
                }

                return dataList;
            }
        };
    }

    private ListenableFuture<List<PatientData>> getPatientListListenableFuture(final SettableFuture<List<PatientData>> result) {
        return new SimpleListenableFuture<List<PatientData>, List<PatientData>>(result) {
            @Override
            protected List<PatientData> adapt(List<PatientData> p) throws ExecutionException {
                return p;
            }
        };
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

    public Patient getEntityFromPatientMapper(PatientData p) {
        return getEntityFromPatientData(p, new PatientData());
    }

    public Patient getEntityFromPatientData(PatientData data, PatientData existing) {

        Patient patient = new Patient();

        prepareRelationBlock(data, existing, patient);

        Address address = data.getAddress();
        Address permanentAddress = data.getPermanentAddress();

        PhoneNumber phoneNumber = data.getPhoneNumber();
        PhoneNumber primaryContactNumber = data.getPrimaryContactNumber();

        patient.setHealthId(data.getHealthId());
        patient.setNationalId(data.getNationalId());
        patient.setBirthRegistrationNumber(data.getBirthRegistrationNumber());
        patient.setFullNameBangla(StringUtils.trim(data.getNameBangla()));
        patient.setGivenName(StringUtils.trim(data.getGivenName()));
        if (data.getGivenName() != null) {
            patient.setLowerGivenName(StringUtils.trim(data.getGivenName()).toLowerCase());
        }
        patient.setSurName(StringUtils.trim(data.getSurName()));
        if (data.getSurName() != null) {
            patient.setLowerSurName(StringUtils.trim(data.getSurName()).toLowerCase());
        }
        patient.setDateOfBirth(data.getDateOfBirth());
        patient.setGender(data.getGender());
        patient.setOccupation(data.getOccupation());
        patient.setEducationLevel(data.getEducationLevel());

        patient.setUid(data.getUid());
        patient.setPlaceOfBirth(StringUtils.trim(data.getPlaceOfBirth()));
        patient.setReligion(data.getReligion());
        patient.setBloodGroup(data.getBloodGroup());
        patient.setNationality(StringUtils.trim(data.getNationality()));
        patient.setDisability(data.getDisability());
        patient.setEthnicity(data.getEthnicity());
        patient.setIsAlive(data.getIsAlive());
        patient.setMaritalStatus(data.getMaritalStatus());

        if (address != null) {
            patient.setAddressLine(address.getAddressLine());
            patient.setDivisionId(address.getDivisionId());
            patient.setDistrictId(address.getDistrictId());
            patient.setUpazillaId(address.getUpazillaId());
            patient.setUnionId(address.getUnionId());
            patient.setHoldingNumber(StringUtils.trim(address.getHoldingNumber()));
            patient.setStreet(StringUtils.trim(address.getStreet()));
            patient.setAreaMouja(StringUtils.trim(address.getAreaMouja()));
            patient.setVillage(StringUtils.trim(address.getVillage()));
            patient.setPostOffice(StringUtils.trim(address.getPostOffice()));
            patient.setPostCode(address.getPostCode());
            patient.setWardId(address.getWardId());
            patient.setThanaId(address.getThanaId());
            patient.setCityCorporationId(address.getCityCorporationId());
            patient.setCountryCode(address.getCountryCode());
        }

        if (permanentAddress != null) {
            patient.setPermanentAddressLine(permanentAddress.getAddressLine());
            patient.setPermanentDivisionId(permanentAddress.getDivisionId());
            patient.setPermanentDistrictId(permanentAddress.getDistrictId());
            patient.setPermanentUpazillaId(permanentAddress.getUpazillaId());
            patient.setPermanentUnionId(permanentAddress.getUnionId());
            patient.setPermanentHoldingNumber(StringUtils.trim(permanentAddress.getHoldingNumber()));
            patient.setPermanentStreet(StringUtils.trim(permanentAddress.getStreet()));
            patient.setPermanentAreaMouja(StringUtils.trim(permanentAddress.getAreaMouja()));
            patient.setPermanentVillage(StringUtils.trim(permanentAddress.getVillage()));
            patient.setPermanentPostOffice(StringUtils.trim(permanentAddress.getPostOffice()));
            patient.setPermanentPostCode(permanentAddress.getPostCode());
            patient.setPermanentWardId(permanentAddress.getWardId());
            patient.setPermanentThanaId(permanentAddress.getThanaId());
            patient.setPermanentCityCorporationId(permanentAddress.getCityCorporationId());
            patient.setPermanentCountryCode(permanentAddress.getCountryCode());
        }

        if (phoneNumber != null) {
            patient.setCellNo(phoneNumber.getNumber());
            patient.setPhoneNumberAreaCode(phoneNumber.getAreaCode());
            patient.setPhoneNumberCountryCode(phoneNumber.getCountryCode());
            patient.setPhoneNumberExtension(phoneNumber.getExtension());
        }

        if (primaryContactNumber != null) {
            patient.setPrimaryCellNo(primaryContactNumber.getNumber());
            patient.setPrimaryContactNumberAreaCode(primaryContactNumber.getAreaCode());
            patient.setPrimaryContactNumberCountryCode(primaryContactNumber.getCountryCode());
            patient.setPrimaryContactNumberExtension(primaryContactNumber.getExtension());
        }

        patient.setPrimaryContact(StringUtils.trim(data.getPrimaryContact()));

        return patient;
    }

    private void prepareRelationBlock(PatientData data, PatientData existingData, Patient patient) {

        List<Relation> relations = data.getRelations();

        if (relations == null) {
            return;
        }

        removeDuplicateRelationBlock(relations);
        appendExistingRelationBlock(data, existingData);
        handleRelationRemovalRequest(relations);
        populateRelationId(relations);

        prepareFathersInfo(data, patient);
        prepareMothersInfo(data, patient);

        try {
            patient.setRelations(mapper.writeValueAsString(data.getRelations()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void removeDuplicateRelationBlock(List<Relation> r) {
        Set<Relation> uniqueRelations = new LinkedHashSet<>(r);
        r.clear();
        r.addAll(uniqueRelations);
    }

    private void handleRelationRemovalRequest(List<Relation> relations) {
        if (relations == null) {
            return;
        }

        List<Relation> relationsToRemoved = new ArrayList<>();

        for (Relation relation : relations) {
            if (relation.isEmpty()) {
                relationsToRemoved.add(relation);
            }
        }

        for (Relation relation : relationsToRemoved) {
            relations.remove(relation);
        }
    }

    private void prepareMothersInfo(PatientData data, Patient patient) {
        Relation mother = data.getRelationOfType("MTH");

        if (mother != null) {
            patient.setMothersNameBangla(StringUtils.trim(mother.getNameBangla()));
            patient.setMothersGivenName(StringUtils.trim(mother.getGivenName()));
            patient.setMothersSurName(StringUtils.trim(mother.getSurName()));
            patient.setMothersBrn(mother.getBirthRegistrationNumber());
            patient.setMothersNid(mother.getNationalId());
            patient.setMothersUid(mother.getUid());
        } else {
            patient.setMothersNameBangla("");
            patient.setMothersGivenName("");
            patient.setMothersSurName("");
            patient.setMothersBrn("");
            patient.setMothersNid("");
            patient.setMothersUid("");
        }
    }

    private void prepareFathersInfo(PatientData data, Patient patient) {
        Relation father = data.getRelationOfType("FTH");
        if (father != null) {
            patient.setFathersNameBangla(StringUtils.trim(father.getNameBangla()));
            patient.setFathersGivenName(StringUtils.trim(father.getGivenName()));
            patient.setFathersSurName(StringUtils.trim(father.getSurName()));
            patient.setFathersBrn(father.getBirthRegistrationNumber());
            patient.setFathersNid(father.getNationalId());
            patient.setFathersUid(father.getUid());
        } else {
            patient.setFathersNameBangla("");
            patient.setFathersGivenName("");
            patient.setFathersSurName("");
            patient.setFathersBrn("");
            patient.setFathersNid("");
            patient.setFathersUid("");
        }
    }

    private Boolean isValidRelationBlock(List<Relation> relations, List<Relation> existing) {

        for (Relation relation : relations) {
            if (isNotBlank(relation.getId()) && !relationExistWithId(existing, relation.getId())) {
                return false;
            }
        }

        return true;
    }

    private void appendExistingRelationBlock(PatientData patient, PatientData existingPatient) {

        List<Relation> r = patient.getRelations();

        if (r == null) {
            r = new ArrayList<>();
        }

        List<Relation> relations = existingPatient.getRelations();

        if (!isValidRelationBlock(r, relations)) {
            DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patient, "patient");
            bindingResult.addError(new FieldError("patient", "relations", "1004"));
            throw new ValidationException(bindingResult);
        }

        if (relations == null) {
            return;
        }

        for (Relation relation : relations) {
            if (r.contains(relation)) {
                r.get(r.indexOf(relation)).setId(relation.getId());
            } else if (patient.getRelationById(relation.getId()) == null) {
                r.add(relation);
            }
        }
    }

    private void populateRelationId(List<Relation> r) {
        int y = r.size();
        for (int x = 0; x < y; x = x + 1) {
            if (StringUtils.isBlank(r.get(x).getId())) {
                r.get(x).setId(UUID.randomUUID().toString());
            }
        }
    }

    public Boolean relationExistWithId(List<Relation> relations, String id) {

        if (relations == null) {
            return false;
        }

        for (Relation relation : relations) {

            if (relation.getId() != null && relation.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }
}
