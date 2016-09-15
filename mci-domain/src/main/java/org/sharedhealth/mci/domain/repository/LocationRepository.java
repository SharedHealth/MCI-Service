package org.sharedhealth.mci.domain.repository;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.domain.model.Location;
import org.sharedhealth.mci.domain.model.LocationData;
import org.sharedhealth.mci.domain.model.LocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Component
public class LocationRepository extends BaseRepository {

    public static final String DEFAULT_PARENT_CODE_FOR_DIVISION = "00";
    public static final String ACTIVE_STATUS = "1";
    public static final String INACTIVE_STATUS = "0";
    private LocationMapper mapper;

    @Autowired
    public LocationRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations, LocationMapper mapper) {
        super(cassandraOperations);
        this.mapper = mapper;
    }

    public LocationData findByGeoCode(final String geoCode) {
        String code = geoCode.substring(geoCode.length() - 2, geoCode.length());
        String parent = geoCode.substring(0, geoCode.length() - 2);

        if (parent.isEmpty()) {
            parent = DEFAULT_PARENT_CODE_FOR_DIVISION;
        }
        Select select = select().from("locations");
        select.where(QueryBuilder.eq("parent", parent));
        select.where(QueryBuilder.eq("code", code));

        Location location = cassandraOps.selectOne(select, Location.class);
        if (location != null) {
            return mapper.map(location);
        }

        return null;
    }

    public List<LocationData> findLocationsByParent(LocationCriteria locationCriteria) {
        String parent = locationCriteria.getParent();
        if (locationCriteria.isEmpty()) {
            parent = DEFAULT_PARENT_CODE_FOR_DIVISION;
        }
        Select select = select().from("locations");
        select.where(QueryBuilder.eq("parent", parent));

        List<Location> locations = cassandraOps.select(select, Location.class);
        return mapper.map(locations);
    }

    public boolean saveOrUpdateLocationData(List<LocationData> locationDataList) {

        LocationData existingLocationData;
        List<Location> insertLocations = new ArrayList<>();
        List<Location> updatedLocations = new ArrayList<>();
        Location location;
        String status;

        for (LocationData locationData : locationDataList) {
            existingLocationData = findByGeoCode(locationData.getCode());
            if (existingLocationData == null) {
                location = mapper.mapHRMData(locationData);
                insertLocations.add(location);
            } else {
                status = locationData.getActive().equals(ACTIVE_STATUS) ? ACTIVE_STATUS : INACTIVE_STATUS;
                location = new Location(existingLocationData.getCode(),
                        locationData.getName(), existingLocationData.getParent(), status);
                updatedLocations.add(location);
            }
        }

        if(insertLocations.size() > 0) {
            cassandraOps.insert(insertLocations);
        }

        if(updatedLocations.size() > 0) {
            cassandraOps.update(updatedLocations);
        }

        return true;

    }


}
