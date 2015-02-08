package org.sharedhealth.mci.web.mapper;

import org.sharedhealth.mci.web.model.Location;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Component
public class LocationMapper {

    public static final String DEFAULT_PARENT_CODE_FOR_DIVISION = "00";

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
        data.setActive(location.getActive());

        return data;
    }

    public Location mapHRMData(LocationData data) {
        Location location = new Location();
        String geoCode = data.getCode();
        String code = geoCode.substring(geoCode.length() - 2, geoCode.length());
        String parent = geoCode.substring(0, geoCode.length() - 2);
        if (parent.isEmpty()) {
            parent = DEFAULT_PARENT_CODE_FOR_DIVISION;
        }
        location.setCode(code);
        location.setName(data.getName());
        location.setParent(parent);
        location.setActive(data.getActive());

        return location;
    }
}
