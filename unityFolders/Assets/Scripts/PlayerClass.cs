using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GameDataTypes;
namespace GameEntities{
	[System.Serializable]
	public class PlayerClass {
		public string displayName;
		public LatLngClass abslocation;
		public CoordinateLocation coordinateLocation;
		public bool isLoggedOn;
		public long lastLoggedOn;
		public string imageUri;
		public int score;
		public string teamName;
		public int teamId;
		public long lastPing;
		public int skillLevel;
		public bool isActive;
		public bool isCapturing;
		public string capturedBy;
		public List<PlayerClass> capturedList = new List<PlayerClass> (); 
		public List<LatLngClass> path = new List<LatLngClass> ();
		public List<CoordinateLocation> relativePath = new List<CoordinateLocation>();
	
		// Use this for initialization
		void Start () {
			
		}
		
		// Update is called once per frame
		void Update () {
			
		}
	}



}