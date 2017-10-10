using System.Collections;
using System.Collections.Generic;
using UnityEngine;
namespace GameDataTypes{
	[System.Serializable]
	public class LatLngClass {

		public double latitude;
		public double longitude;

		public double Accuraccy;

		public LatLngClass(double lat,double lon){
			latitude = lat;
			longitude = lon;
			
		}
		// Use this for initialization
		void Start () {

		}

		// Update is called once per frame
	}
}

