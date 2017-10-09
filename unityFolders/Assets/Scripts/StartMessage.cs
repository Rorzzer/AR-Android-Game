using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class StartMessage : MonoBehaviour {
	public float timerCounter = 10;
	public float gameStartCounter = 2;
	public bool displayingCounter;
	public bool displayingStart ;
	// Use this for initialization
	void Start () {
		displayingCounter = true;
		displayingStart = false;
	}
	
	// Update is called once per frame
	void Update () {
		changeText ();
	}
	void changeText(){
		if (displayingCounter) {
			if (timerCounter > 1) {
				//displaying countdown
				timerCounter -= Time.deltaTime;
				GetComponent<TextMesh> ().text = "Begin in " + (int)timerCounter;
			} else {
				displayingCounter = false;
				displayingStart = true;
			}
		}
		if (displayingStart) {
			if (gameStartCounter > 1 && displayingStart) {
				//displaying start message
				gameStartCounter -= Time.deltaTime;
				GetComponent<TextMesh> ().text = "Start Game!";
			} else {
				displayingStart = false;
			}
		}
		if (!displayingStart && !displayingCounter) {
			GetComponent<TextMesh> ().text = "";
		}


	}
}
