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

    public MCIResponse create(PatientMapper patientMapper) {
        if (!StringUtils.isBlank(patientMapper.getHealthId())) {
            DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patientMapper, "patient");
            bindingResult.addError(new FieldError("patient", "hid", "3001"));
            throw new HealthIDExistException(bindingResult);
        }

        PatientMapper existingPatient = getExistingPatient(patientMapper);
        if (existingPatient != null) {
            return update(patientMapper, existingPatient.getHealthId());
        }

        Patient p = getEntityFromPatientMapper(patientMapper);

        p.setHealthId(uid.getId());
        p.setCreatedAt(new Date());
        p.setUpdatedAt(new Date());
        p.setSurName(patientMapper.getSurName());

        cassandraOperations.execute(buildSaveBatch(p));
        return new MCIResponse(p.getHealthId(), HttpStatus.CREATED);
    }

    private PatientMapper getExistingPatient(PatientMapper mapper) {
        if (!mapper.containsMultipleIdentifier()) {
            return null;
        }
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setNid(mapper.getNationalId());
        searchQuery.setBin_brn(mapper.getBirthRegistrationNumber());
        searchQuery.setUid(mapper.getUid());
        List<PatientMapper> mappers = findAllByQuery(searchQuery);
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

    public PatientMapper findByHealthId(final String healthId) {
        String cql = String.format(getFindByHealthIdQuery(), healthId);
        logger.debug("Find patient by health id CQL: [" + cql + "]");
        ResultSet resultSet = cassandraOperations.query(cql);

        if (resultSet != null) {
            Row row = resultSet.one();
            if (row != null) {
                return getPatientFromRow(row);
            }
        }
        throw new PatientNotFoundException("No patient found with health id: " + healthId);
    }

    private PatientMapper getPatientFromRow(Row r) {
        DatabaseRow row = new DatabaseRow(r);
        PatientMapper patientMapper = new PatientMapper();

        try {
            final String string = row.getString(RELATIONS);
            patientMapper.setRelations(getRelationsList(string));
        } catch (Exception e) {
            logger.debug(" Relations: [" + e.getMessage() + "]");
        }
        patientMapper.setHealthId(row.getString(HEALTH_ID));
        patientMapper.setNationalId(row.getString(NATIONAL_ID));
        patientMapper.setUid(row.getString(UID));
        patientMapper.setPlaceOfBirth(row.getString(PLACE_OF_BIRTH));

        patientMapper.setReligion(row.getString(RELIGION));
        patientMapper.setBloodGroup(row.getString(BLOOD_GROUP));
        patientMapper.setNameBangla(row.getString(FULL_NAME_BANGLA));
        patientMapper.setBirthRegistrationNumber(row.getString(BIN_BRN));
        patientMapper.setGivenName(row.getString(GIVEN_NAME));
        patientMapper.setSurName(row.getString(SUR_NAME));
        patientMapper.setDateOfBirth(row.getDateAsString(DATE_OF_BIRTH));
        patientMapper.setGender(row.getString(GENDER));
        patientMapper.setOccupation(row.getString(OCCUPATION));
        patientMapper.setEducationLevel(row.getString(EDU_LEVEL));
        patientMapper.setNationality(row.getString(NATIONALITY));
        patientMapper.setDisability(row.getString(DISABILITY));
        patientMapper.setEthnicity(row.getString(ETHNICITY));
        patientMapper.setIsAlive(row.getString(IS_ALIVE));
        patientMapper.setMaritalStatus(row.getString(MARITAL_STATUS));

        patientMapper.setPrimaryContact(row.getString(PRIMARY_CONTACT));

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
        patientMapper.setAddress(address);

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
            patientMapper.setPrimaryContactNumber(primaryContactNumber);
        }

        if (phoneNumber.getNumber() != null) {
            patientMapper.setPhoneNumber(phoneNumber);
        }

        if (permanentaddress.getCountryCode() != null) {
            if (permanentaddress.getCountryCode() == "050" && permanentaddress.getDistrictId() != null) {
                patientMapper.setPermanentAddress(permanentaddress);
            }

            if (permanentaddress.getCountryCode() != "050") {
                patientMapper.setPermanentAddress(permanentaddress);
            }
        }

        patientMapper.setCreatedAt(row.getDate("created_at"));
        patientMapper.setUpdatedAt(row.getDate("updated_at"));

        return patientMapper;
    }

    private List<Relation> getRelationsList(String string) throws IOException {
        return mapper.readValue(string, mapper.getTypeFactory().constructCollectionType(List.class, Relation.class));
    }

    public List<PatientMapper> findAllByQuery(SearchQuery searchQuery) {
        return filterPatients(findProbables(searchQuery), searchQuery);
    }

    private List<PatientMapper> findProbables(SearchQuery searchQuery) {
        List<PatientMapper> patientMappers = new ArrayList<>();
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
                ResultSet resultSet = cassandraOperations.query(buildFindByHidsQuery(healthIds.toArray(new String[healthIds.size()])));
                for (Row row : resultSet) {
                    patientMappers.add(getPatientFromRow(row));
                }
            }
        }
        return patientMappers;
    }

    private List<PatientMapper> filterPatients(List<PatientMapper> patients, SearchQuery searchQuery) {
        List<PatientMapper> result = new ArrayList<>();
        for (PatientMapper patient : patients) {
            if (isMatchingPatient(patient, searchQuery)) {
                result.add(patient);
            }
        }
        return result;
    }

    private boolean isMatchingPatient(PatientMapper p, SearchQuery searchQuery) {
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

    public MCIResponse update(PatientMapper patientMapper, final String hid) {
        if (patientMapper.getHealthId() != null && !StringUtils.equals(patientMapper.getHealthId(), hid)) {
            DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patientMapper, "patient");
            bindingResult.addError(new FieldError("patient", "hid", "1004"));
            throw new ValidationException(bindingResult);
        }
        PatientMapper existingPatient;
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
        PatientMapper patientToSave = new PatientMapper();
        patientToSave.setHealthId(hid);
        patientMapper.setHealthId(hid);
        PatientFilter patientFilter = new PatientFilter(properties, existingPatient, patientMapper, patientToSave);

        Approval approval = patientFilter.filter();

        String fullName = "";

        if (patientMapper.getGivenName() != null) {
            fullName = patientMapper.getGivenName();
        }

        if (patientMapper.getSurName() != null) {
            fullName = fullName + " " + patientMapper.getSurName();
        }

        Patient patient = getEntityFromPatientMapper(patientToSave, existingPatient);
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

    public ListenableFuture<List<PatientMapper>> findAllByLocations(List<String> locations, String start, Date since) {

        final SettableFuture<List<PatientMapper>> result = SettableFuture.create();
        List<PatientMapper> patients = new ArrayList<>();

        int limit = PER_PAGE_LIMIT;

        if (locations != null && locations.size() > 0) {
            String locationPointer = getLocationPointer(locations, start, null);

            for (String catchment : locations) {
                if (patients.size() == 0 && !isLocationBelongsToCatchment(locationPointer, catchment)) {
                    continue;
                }

                ListenableFuture<List<PatientMapper>> res = this.findAllByLocation(catchment, start, limit, since);
                try {
                    List<PatientMapper> temp = res.get();
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

    public ListenableFuture<List<PatientMapper>> findAllByLocation(String location, String start, int limit, Date since) {

        Select select = select().from("patient");

        if (StringUtils.isBlank(location)) {
            final SettableFuture<List<PatientMapper>> result = SettableFuture.create();
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

    private ListenableFuture<List<PatientMapper>> getPatientListListenableFuture(final Select select) {
        return new SimpleListenableFuture<List<PatientMapper>, ResultSet>(
                cassandraOperations.queryAsynchronously(select)) {
            @Override
            protected List<PatientMapper> adapt(ResultSet resultSet) throws ExecutionException {
                List<PatientMapper> patientDtos = new ArrayList<>();
                for (Row result : resultSet.all()) {
                    PatientMapper patientDto = getPatientFromRow(result);
                    patientDtos.add(patientDto);
                }

                return patientDtos;
            }
        };
    }

    private ListenableFuture<List<PatientMapper>> getPatientListListenableFuture(final SettableFuture<List<PatientMapper>> result) {
        return new SimpleListenableFuture<List<PatientMapper>, List<PatientMapper>>(result) {
            @Override
            protected List<PatientMapper> adapt(List<PatientMapper> p) throws ExecutionException {
                return p;
            }
        };
    }

    private String getLocationPointer(List<String> locations, String start, String d) {
        if (locations.size() > 1 && isNotBlank(start)) {
            PatientMapper p = findByHealthId(start);
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

    public Patient getEntityFromPatientMapper(PatientMapper p) {
        return getEntityFromPatientMapper(p, new PatientMapper());
    }

    public Patient getEntityFromPatientMapper(PatientMapper patientDto, PatientMapper existing) {

        Patient patient = new Patient();

        prepareRelationBlock(patientDto, existing, patient);

        Address address = patientDto.getAddress();
        Address permanentAddress = patientDto.getPermanentAddress();

        PhoneNumber phoneNumber = patientDto.getPhoneNumber();
        PhoneNumber primaryContactNumber = patientDto.getPrimaryContactNumber();

        patient.setHealthId(patientDto.getHealthId());
        patient.setNationalId(patientDto.getNationalId());
        patient.setBirthRegistrationNumber(patientDto.getBirthRegistrationNumber());
        patient.setFullNameBangla(StringUtils.trim(patientDto.getNameBangla()));
        patient.setGivenName(StringUtils.trim(patientDto.getGivenName()));
        if (patientDto.getGivenName() != null) {
            patient.setLowerGivenName(StringUtils.trim(patientDto.getGivenName()).toLowerCase());
        }
        patient.setSurName(StringUtils.trim(patientDto.getSurName()));
        if (patientDto.getSurName() != null) {
            patient.setLowerSurName(StringUtils.trim(patientDto.getSurName()).toLowerCase());
        }
        patient.setDateOfBirth(patientDto.getDateOfBirth());
        patient.setGender(patientDto.getGender());
        patient.setOccupation(patientDto.getOccupation());
        patient.setEducationLevel(patientDto.getEducationLevel());

        patient.setUid(patientDto.getUid());
        patient.setPlaceOfBirth(StringUtils.trim(patientDto.getPlaceOfBirth()));
        patient.setReligion(patientDto.getReligion());
        patient.setBloodGroup(patientDto.getBloodGroup());
        patient.setNationality(StringUtils.trim(patientDto.getNationality()));
        patient.setDisability(patientDto.getDisability());
        patient.setEthnicity(patientDto.getEthnicity());
        patient.setIsAlive(patientDto.getIsAlive());
        patient.setMaritalStatus(patientDto.getMaritalStatus());

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

        patient.setPrimaryContact(StringUtils.trim(patientDto.getPrimaryContact()));

        return patient;
    }

    private void prepareRelationBlock(PatientMapper patientDto, PatientMapper existingDto, Patient patient) {

        List<Relation> relations = patientDto.getRelations();

        if (relations == null) {
            return;
        }

        removeDuplicateRelationBlock(relations);
        appendExistingRelationBlock(patientDto, existingDto);
        handleRelationRemovalRequest(relations);
        populateRelationId(relations);

        prepareFathersInfo(patientDto, patient);
        prepareMothersInfo(patientDto, patient);

        try {
            patient.setRelations(mapper.writeValueAsString(patientDto.getRelations()));
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

    private void prepareMothersInfo(PatientMapper patientDto, Patient patient) {
        Relation mother = patientDto.getRelationOfType("MTH");

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

    private void prepareFathersInfo(PatientMapper patientDto, Patient patient) {
        Relation father = patientDto.getRelationOfType("FTH");
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

    private void appendExistingRelationBlock(PatientMapper patientDto, PatientMapper existingPatient) {

        List<Relation> r = patientDto.getRelations();

        if (r == null) {
            r = new ArrayList<>();
        }

        List<Relation> relations = existingPatient.getRelations();

        if (!isValidRelationBlock(r, relations)) {
            DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patientDto, "patient");
            bindingResult.addError(new FieldError("patient", "relations", "1004"));
            throw new ValidationException(bindingResult);
        }

        if (relations == null) {
            return;
        }

        for (Relation relation : relations) {
            if (r.contains(relation)) {
                r.get(r.indexOf(relation)).setId(relation.getId());
            } else if (patientDto.getRelationById(relation.getId()) == null) {
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
