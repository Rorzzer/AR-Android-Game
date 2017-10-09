using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GameEntities;
using GameEntities;
using GameDataTypes;
namespace Game{
	[System.Serializable]
	public class GameSessionClass {
		public int sessionId;
		public long startTime;
		public long endTime;
		public int maxPlayers;
		public long duration;
		public bool gameStarted;
		public bool gameCompleted;
		public LatLngClass location;
		public int gameRadius;
		public long timeSessionCreated;
		public PlayerClass creator;
		public string sessionName;
		public string description;
		public string sessionImageUri;
		public float bearing;
		public List<TeamClass> teamArrayList = new List<TeamClass> ();

		// Use this for initialization
		void Start () {

		}

		// Update is called once per frame
		void Update () {

		}
	}
}

