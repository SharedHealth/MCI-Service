package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.web.mapper.LocationCriteria;
import org.sharedhealth.mci.web.mapper.LocationData;
import org.sharedhealth.mci.web.mapper.LocationMapper;
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
    private LocationMapper mapper;

    @Autowired
    public LocationRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations, LocationMapper mapper) {
        super(cassandraOperations);
        this.mapper = mapper;
    }

    public LocationData findByGeoCode(final String geoCode) {

        String code = geoCode.substring(geoCode.length() - 2, geoCode.length());
        String parent = geoCode.substring(0, geoCode.length() - 2);

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
}
