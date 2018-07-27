package com.example.junmung.hangangparksmap;

import android.location.Location;

import com.example.junmung.hangangparksmap.ARGuide.Point;

public class FavoritePoint extends Point{
    private String address;

    public FavoritePoint(String name, double latitude, double longitude, double altitude) {
        super(name, latitude, longitude, altitude);
    }

    public FavoritePoint(Location location) {
        super(location);
    }

    public FavoritePoint(String name, String address, double latitude, double longitude, double altitude) {
        super(name, latitude, longitude, altitude);
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
