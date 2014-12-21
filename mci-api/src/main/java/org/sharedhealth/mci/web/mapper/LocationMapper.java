package org.sharedhealth.mci.web.mapper;

import org.sharedhealth.mci.web.model.Location;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Component
public class LocationMapper {


    public List<LocationData> map(List<Location> locations) {

        List<LocationData> dataList = new ArrayList<>();
        if (isNotEmpty(locations)) {
            for (Location location : locations) {
                LocationData data = this.map(location);
                dataList.add(data);
            }
        }
        return dataList;
    }

    public LocationData map(Location location) {

        LocationData data = new LocationData();

        data.setCode(location.getCode());
        data.setName(location.getName());
        data.setParent(location.getParent());

        return data;
    }
}
