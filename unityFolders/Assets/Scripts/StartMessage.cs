using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class StartMessage : MonoBehaviour {
	public int startTextDistance = 10;
	public float timerCounter = 10;
	public float gameStartCounter = 2;
	public bool displayingCounter;
	public bool displayingStart ;
	// Use this for initialization
	void Start () {
		displayingCounter = true;
		displayingStart = false;
		//GetComponent<TextMesh>().transform.position = Camera.transform.position +Camera.transform.forward*startTextDistance;
	}
	
	// Update is called once per frame
	void Update () {
		changeText ();
	}
	void changeText(){
		int delay =2;
		Transform textTrans = GetComponent<TextMesh>().transform;
		textTrans.position = Camera.main.transform.position + Camera.main.transform.forward* 10;
		Vector3 look = Camera.main.transform.position - textTrans.transform.position;
		look.y = 0;
		Quaternion rotate = Quaternion.LookRotation(look);
		GetComponent<TextMesh>().transform.rotation = Quaternion.Slerp(textTrans.rotation,rotate,Time.deltaTime*delay);
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
