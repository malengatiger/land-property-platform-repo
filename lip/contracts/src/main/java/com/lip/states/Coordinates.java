package com.lip.states;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public class Coordinates {
    private final double latitude, longitude;

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
