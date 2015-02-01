package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.web.mapper.LocationCriteria;
import org.sharedhealth.mci.web.mapper.LocationData;
import org.sharedhealth.mci.web.mapper.LocationMapper;
import org.sharedhealth.mci.web.model.LRMarker;
import org.sharedhealth.mci.web.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Component
public class LocationRepository extends BaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(LocationRepository.class);
    public static final String DEFAULT_PARENT_CODE_FOR_DIVISION = "00";
    public static final String INVALID_CITY_CORPORATION = "99";
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

        if (StringUtils.length(geoCode) == 8 && StringUtils.equals(code, INVALID_CITY_CORPORATION)) {
            return null;
        }
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

    public Location saveOrUpdateLocationData(LocationData locationData) {

        LocationData existingLocationData;
        Location location;
        try {
            existingLocationData = findByGeoCode(locationData.getCode());
            if (existingLocationData == null) {
                location = mapper.mapHRMData(locationData);
                cassandraOps.insert(location);
            } else if (!locationData.getActive().equals(ACTIVE_STATUS)) {
                location = new Location(existingLocationData.getCode(),
                        existingLocationData.getName(), existingLocationData.getParent(), INACTIVE_STATUS);
                cassandraOps.update(location);
            } else {
                location = new Location(existingLocationData.getCode(),
                        locationData.getName(), existingLocationData.getParent(), existingLocationData.getActive());
                cassandraOps.update(location);
            }
        } catch (Exception e) {
            logger.info("Failed to insert or update into Local DB " + e.getMessage());
            throw e;
        }

        return location;
    }


    public LRMarker getLRMarkerData(String type) {

        Select select = QueryBuilder.select().from("lr_markers");

        select.where(QueryBuilder.eq("type", type));

        LRMarker lrMapper = cassandraOps.selectOne(select, LRMarker.class);

        if (lrMapper != null) {
            return lrMapper;
        }

        return null;
    }

    public boolean saveOrUpdateLRMarkerData(String type, String lastSync) {
        LRMarker oldLrMarker = getLRMarkerData(type);
        if (oldLrMarker != null) {
            oldLrMarker.setLastSync(lastSync);
            cassandraOps.update(oldLrMarker);
        } else {
            LRMarker lrMapper = new LRMarker(type, lastSync);
            cassandraOps.insert(lrMapper);
        }
        return true;
    }


}
