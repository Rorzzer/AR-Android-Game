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
		public string assignedTeamName;
		public int teamId;
		public long lastPing;
		public bool isActive;
		public int skillLevel;
		public bool isCapturing;
		public bool hasBeenCaptured;

		public string capturedBy;
		public List<string> capturedList = new List<string> (); 
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