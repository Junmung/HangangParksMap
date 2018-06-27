package com.example.junmung.hangangparksmap;

import android.location.Location;

public class ARPoint {
    private String name;
    private Location location;

    public ARPoint(String name, double latitude, double longitude, double altitude) {
        this.name = name;
        location = new Location("ARPoint");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(altitude);
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }


}
