using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ServiceListenerScript : MonoBehaviour {
	public string receiverMessage = "Uninitiated";
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
		GetComponent<TextMesh> ().text = newText; 
	}
}
