package com.example.motorcycledashboard;

import android.location.Location;

public class LocMain extends Location {

    private boolean MetricUnits = false;

    public LocMain (Location location) {
        this(location, true);
    }

    public LocMain(Location location, boolean MetricUnits) {
        super(location);
        this.MetricUnits = MetricUnits;
    }

    public boolean getMetricUnits() {
        return this.MetricUnits;
    }

    public void setMetricUnits(boolean MetricUnits) {
        this.MetricUnits = MetricUnits;
    }

    @Override
    public float distanceTo(Location dest) {
        float nDistance = super.distanceTo(dest);

        if(!this.getMetricUnits()) {
            //metres to feet
            nDistance = nDistance * 3.2808398501312f;
        }

        return nDistance;
    }

    @Override
    public double getAltitude() {
        double nAltitude = super.getAltitude();

        if(!this.getMetricUnits()) {
            //metres to feet
            nAltitude = nAltitude * 3.2808398501312d;
        }

        return nAltitude;
    }

    @Override
    public float getSpeed() {
        float nSpeed = super.getSpeed() * 3.6f;

        if(!this.getMetricUnits()) {
            //metres/sec to mph
            nSpeed = super.getSpeed() * 2.23693629f;
        }

        return nSpeed;
    }

    @Override
    public float getAccuracy() {
        float nAccuracy = super.getAccuracy();

        if(!this.getMetricUnits()) {
            //metres to feet
            nAccuracy = nAccuracy * 3.2808398501312f;
        }

        return nAccuracy;
    }
}
