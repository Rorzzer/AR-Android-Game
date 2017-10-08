using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GameEntities;

namespace GameEntities{
	[System.Serializable]
	public class TeamClass {
		public int teamId;
		public string teamName;
		public long timeTeamCreated;
		public PlayerClass creator;
		public int maxPlayers;
		public int numPlayers;
		public bool isActive;
		private bool isCapturing;
		public string teamImageUri;
		public  List<PlayerClass> playerArraylist  = new List<PlayerClass>();
		// Use this for initialization
		void Start () {

		}

		// Update is called once per frame
		void Update () {

		}
	}

}

