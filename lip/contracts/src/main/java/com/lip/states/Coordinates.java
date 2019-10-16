package com.lip.states;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public class Coordinates {
    private final double latitude, longitude;
    private final String dateTime;

    public Coordinates(double latitude, double longitude, String dateTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDateTime() {
        return dateTime;
    }
}
