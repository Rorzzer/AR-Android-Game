using System.Collections;
using System.Collections.Generic;
using UnityEngine;
namespace GameDataTypes{
	[System.Serializable]
	public class LatLngClass {

		public double latitude;
		public double longitude;

		public double accuraccy;

		public LatLngClass(double lat,double lon,double acc){
			latitude = lat;
			longitude = lon;
			accuraccy = acc;
		}
		// Use this for initialization
		void Start () {

		}

		// Update is called once per frame
	}
}

