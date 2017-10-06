using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Game;
using GameEntities;
[System.Serializable]
public class ServiceListenerScript : MonoBehaviour {
	public string receiverMessage = "Uninitiated";
	public string PlayerJSON ="";
	AndroidJavaClass javaClass;
	// Use this for initialization
	void Start () {
		javaClass = new AndroidJavaClass("com.unimelb.comp30022.receiver.UnityReceiver");
		javaClass.CallStatic ("createInstance");
	}

	// Update is called once per frame
	void Update () {
		receiverMessage = javaClass.GetStatic<string>("text");
		changeText (receiverMessage);
	}
	void changeText(string newText){
		//GameSessionClass current = gameStateFromJson (newText);
		//GetComponent<TextMesh> ().text = current.creator.displayName; 
		GetComponent<TextMesh> ().text = receiverMessage;
	}
	public static GameSessionClass gameStateFromJson(string jsonString){
		return JsonUtility.FromJson<GameSessionClass> (jsonString);
	}
}
