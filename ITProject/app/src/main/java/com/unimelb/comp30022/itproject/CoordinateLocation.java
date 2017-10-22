package com.unimelb.comp30022.itproject;

/**
 * Created by Kiptenai on 1/10/2017.
 * Class to store relative coordinate information obtained from
 * Latitude and longitude computations.
 * The x value represents difference in latitudes, while
 * the z value represents difference in longitues
 * the y Value is always equal to zero
 * the accuracy is similar to the value extracted from the Latitude and longitudes
 *
 */

public class CoordinateLocation {
    //x - coordinate, Y -coordinate z-coordinates and accuracy
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
    // Mutator methods and accessor methods
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
    /* String conversion method for coordinate location object
     *
      */
    public final String toString() {
        double var1 = this.x;
        double var2 = this.y;
        double var3 = this.z;
        double var4 = this.accuracy;

        return (new StringBuilder(60)).append("x,y,z +-accuracy: (").append(var1).append(",").append(var2).append(",").append(var3).append("+-").append(var4).append(")").toString();
    }
    /* Equality test for coordinateLocation objects
     * @param  location value to be compared with
      */
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
