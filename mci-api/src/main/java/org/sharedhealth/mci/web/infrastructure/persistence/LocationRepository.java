package org.sharedhealth.mci.web.infrastructure.persistence;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.google.common.util.concurrent.SettableFuture;
import org.sharedhealth.mci.web.model.Location;
import org.sharedhealth.mci.web.utils.concurrent.SimpleListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.AsynchronousQueryListener;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Component
public class LocationRepository {
    private static final Logger logger = LoggerFactory.getLogger(LocationRepository.class);
    public static final String LOCATION_FIND_BY_GEO_CODE_QUERY = "SELECT * FROM locations WHERE geo_code = '%s'";

    private static long TIMEOUT_IN_MILLIS = 10;

    private CqlOperations cqlOperations;

    @Autowired
    public LocationRepository(@Qualifier("MCICassandraTemplate") CqlOperations cqlOperations) {
        this.cqlOperations = cqlOperations;
    }

    public ListenableFuture<Location> findByGeoCode(final String geoCode) {
        String cql = String.format(LOCATION_FIND_BY_GEO_CODE_QUERY, geoCode);
        logger.debug("Find location by geo_code CQL: [" + cql + "]");
        final SettableFuture<Location> result = SettableFuture.create();

        cqlOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    Row row = rsf.get(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS).one();
                    setLocationOnResult(row, result);
                } catch (Exception e) {
                    logger.error("Error while finding locaiton by geo_code: " + geoCode, e);
                    result.setException(e);
                }
            }
        });

        return new SimpleListenableFuture<Location, Location>(result) {
            @Override
            protected Location adapt(Location adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    private void setLocationOnResult(Row r, SettableFuture<Location> result) throws InterruptedException, ExecutionException {
        Location location = getLocationFromRow(r);
        result.set(location);
    }

    private Location getLocationFromRow(Row r) {
        DatabaseRow row = new DatabaseRow(r);

        Location location = new Location();

        location.setGeoCode(row.getString("geo_code"));
        location.setDivisionId(row.getString("division_id"));
        location.setDivisionName(row.getString("division_name"));
        location.setDistrictId(row.getString("district_id"));
        location.setDistrictName(row.getString("district_name"));
        location.setUpazillaId(row.getString("upazilla_id"));
        location.setUpazillaName(row.getString("upazilla_name"));
        location.setPaurashavaId(row.getString("pourashava_id"));
        location.setPaurashavaName(row.getString("pourashava_name"));
        location.setUnionId(row.getString("union_id"));
        location.setUnionName(row.getString("union_name"));

        return location;
    }
}
