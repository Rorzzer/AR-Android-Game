package com.unimelb.comp30022.itproject;

/**
 * Created by Kiptenai on 5/10/2017.
 * Storage of coordinates as substitute for google latlng
 */

public class LatLng {
    private double latitude;
    private double longitude;
    private double accuracy;

    public LatLng(double latitude, double longitude, double accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;

    }
    public LatLng(){

    }
    /****
     * Basic acessor and mutator methods
     * */
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

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * Comparison based on the latitude, and longitude
     * @param var1 location with which the current location is compared
     * */

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
    /****
     * string conversion method
     * */
    public final String toString() {
        double var1 = this.latitude;
        double var2 = this.longitude;
        double var3 = this.accuracy;
        return (new StringBuilder(60)).append("").append(var1).append(",").append(var2).append("+-").append(var3).append(")").toString();
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(getLatitude());
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getLongitude());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}

