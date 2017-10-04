package com.unimelb.comp30022.itproject;

/**
 * Created by Kiptenai on 5/10/2017.
 * Storage of coordinates as substitute for google latlng
 */

public class LatLng {
    public double latitude;
    public double longitude;
    public LatLng(double latitude,double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public LatLng(){

    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public final boolean equals(Object var1) {
        if(this == var1) {
            return true;
        } else if(!(var1 instanceof LatLng)) {
            return false;
        } else {
            LatLng var2 = (LatLng)var1;
            return Double.doubleToLongBits(this.latitude) == Double.doubleToLongBits(var2.latitude) && Double.doubleToLongBits(this.longitude) == Double.doubleToLongBits(var2.longitude);
        }
    }
    public final String toString() {
        double var1 = this.latitude;
        double var3 = this.longitude;
        return (new StringBuilder(60)).append("lat/lng: (").append(var1).append(",").append(var3).append(")").toString();
    }
}

