using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GameDataTypes;
namespace GameEntities{
	[System.Serializable]
	
	public class CoordinateLocation {
		public CoordinateLocation(double xPos, double yPos ,double zPos){
		x = xPos;
		y = yPos;
		z = zPos;
	}
		public double x;
		public double y;
		public double z;
	}
}