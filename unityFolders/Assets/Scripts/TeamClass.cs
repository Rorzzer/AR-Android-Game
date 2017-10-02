using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GameEntities;

namespace GameEntities{
	public class TeamClass : MonoBehaviour {
		public int teamId;
		public string teamName;
		public long timeTeamCreated;
		public PlayerClass creator;
		public int maxPlayers;
		public bool isActive;
		public string teamImageUri;
		public  ArrayList playerArraylist  = new ArrayList();
		// Use this for initialization
		void Start () {

		}

		// Update is called once per frame
		void Update () {

		}
	}

}

