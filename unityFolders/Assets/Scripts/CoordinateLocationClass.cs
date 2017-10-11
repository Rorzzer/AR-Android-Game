using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GameDataTypes;
namespace GameEntities{
	[System.Serializable]
	
	public class CoordinateLocation {
		public double x;
		public double y;
		public double z;
		public double accuracy;
		public CoordinateLocation(double xPos, double yPos ,double zPos,double acc){
		x = xPos;
		y = yPos;
		z = zPos;
		accuracy = acc;
	}
	bool equals(CoordinateLocation point){
		if(this.x == point.x && this.y ==point.y && this.z == point.z){
			return true;
		}
		return false;
	}	

	}
}