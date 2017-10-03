package com.unimelb.comp30022.itproject;

/**
 * Created by Kiptenai on 1/10/2017.
 */

public class RelLocation{
    private double x;
    private double y;
    private double z;

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
    public RelLocation diff(RelLocation location){
        RelLocation result = new RelLocation();
        result.setX(this.x-location.getX());
        result.setY(this.y-location.getY());
        result.setZ(this.z-location.getZ());
        return result;
    }
    public final String toString() {
        double var1 = this.x;
        double var2 = this.y;
        double var3 = this.z;
        return (new StringBuilder(60)).append("x,y,z : (").append(var1).append(",").append(var2).append(",").append(var3).append(")").toString();
    }
    public  boolean equals(Object location){
        if(this == location){
            //same obj
            return true;
        }
        else if(!(location instanceof RelLocation)){
            return false;
        }
        else{
            RelLocation location1 = (RelLocation)location;
            return Double.doubleToLongBits(this.x) == Double.doubleToLongBits(location1.x) &&
                    Double.doubleToLongBits(this.y) == Double.doubleToLongBits(location1.y) &&
                    Double.doubleToLongBits(this.z) == Double.doubleToLongBits(location1.z);
        }

    }
}
