package com.example.junmung.hangangparksmap;

public class ARPoint {
    private String name;
    private double latitude;
    private double longitude;
    private double altitude;

    public ARPoint(String name, double latitude, double longitude, double altitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }


}
