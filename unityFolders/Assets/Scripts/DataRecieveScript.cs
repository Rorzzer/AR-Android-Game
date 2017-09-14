using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class DataRecieveScript : MonoBehaviour {

	public string javaMessage = "";
	public string hi = "Hello";
	public TextMesh textMesh1;
	AndroidJavaClass jc;
	// Use this for initialization
	void Start () {
		jc = new AndroidJavaClass("com.ITProject.sendintent.IntentToUnity");
		jc.CallStatic ("createInstance");
	}
	
	// Update is called once per frame
	void Update () {
		javaMessage = jc.GetStatic<string>("text");
		textMesh1.text = hi;

	}
}
