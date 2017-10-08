using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GameDataTypes;
namespace GameEntities{
	
	public class PlayerClass : MonoBehaviour {
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

		// Use this for initialization
		void Start () {
			
		}
		
		// Update is called once per frame
		void Update () {
			
		}
	}



}