using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Game;
using GameEntities;
[System.Serializable]
public class ServiceListenerScript : MonoBehaviour {
	public string receiverMessage = "";
	public bool gameStarted = false;
	public ArrayList chasers;
	public ArrayList runners;
	AndroidJavaClass javaClass;
	// Use this for initialization
	void Start () {
		javaClass = new AndroidJavaClass("com.unimelb.comp30022.receiver.UnityReceiver");
		javaClass.CallStatic ("createInstance");
		//spawn all players
	}

	// Update is called once per frame
	void Update () {
		receiverMessage = javaClass.GetStatic<string> ("text");
		generatePlayers (receiverMessage);
		if (gameStarted == false) {
			generatePlayers (receiverMessage);
			GetComponent<TextMesh> ().text = receiverMessage;
			gameStarted = true;
		}
	}
	void generatePlayers(string newText){
		GameSessionClass currentGame = gameStateFromJson (newText);
		GetComponent<TextMesh> ().text = currentGame.creator.displayName; 

	}
	public static GameSessionClass gameStateFromJson(string jsonString){
		return JsonUtility.FromJson<GameSessionClass> (jsonString);
	}
}
