using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GameEntities;
using GameEntities;
using GameDataTypes;
namespace Game{
	public class GameSessionClass : MonoBehaviour {
		public int sessionId;
		public long startTime;
		public long endTime;
		public int maxTeams;
		public int maxPlayers;
		public long duration;
		public bool gameStarted;
		public bool gameCompleted;
		public LatLngClass location;
		public int gameRadius;
		public PlayerClass creator;
		public string description;
		public string sessionImageUri;
		public ArrayList teamArrayList = new ArrayList ();

		// Use this for initialization
		void Start () {

		}

		// Update is called once per frame
		void Update () {

		}
	}
}

