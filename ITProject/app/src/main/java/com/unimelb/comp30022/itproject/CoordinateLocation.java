package com.unimelb.comp30022.itproject;

/**
 * Created by Kiptenai on 1/10/2017.
 */

public class CoordinateLocation {
    private double x;
    private double y;
    private double z;
    private double accuracy;

    public CoordinateLocation(double x, double y, double z, double accuracy) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.accuracy = accuracy;
    }
    public CoordinateLocation(){

    }
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
    public CoordinateLocation diff(CoordinateLocation location){
        CoordinateLocation result = new CoordinateLocation();
        result.setX(this.x-location.getX());
        result.setY(this.y-location.getY());
        result.setZ(this.z-location.getZ());
        return result;
    }
    public final String toString() {
        double var1 = this.x;
        double var2 = this.y;
        double var3 = this.z;
        double var4 = this.accuracy;

        return (new StringBuilder(60)).append("x,y,z +-accuracy: (").append(var1).append(",").append(var2).append(",").append(var3).append("+-").append(var4).append(")").toString();
    }
    public  boolean equals(Object location){
        if(this == location){
            //same obj
            return true;
        }
        else if(!(location instanceof CoordinateLocation)){
            return false;
        }
        else{
            CoordinateLocation location1 = (CoordinateLocation)location;
            return Double.doubleToLongBits(this.x) == Double.doubleToLongBits(location1.x) &&
                    Double.doubleToLongBits(this.y) == Double.doubleToLongBits(location1.y) &&
                    Double.doubleToLongBits(this.z) == Double.doubleToLongBits(location1.z);
        }

    }
}
