using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GameDataTypes;
namespace GameEntities{
	
	public class PlayerClass : MonoBehaviour {
		public int  playerId;
		public string displayName;
		public LatLngClass location;
		public bool isLoggedOn;
		public long lastLoggedOn;
		public string imageUri;
		public int score;
		public string teamName;
		public int teamId;
		public long lastPing;
		public int skillLevel;
		public bool isActive;
		// Use this for initialization
		void Start () {
			
		}
		
		// Update is called once per frame
		void Update () {
			
		}
	}



}