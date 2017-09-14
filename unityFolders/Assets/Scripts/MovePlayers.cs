using System.Collections;
using System.Collections.Generic;
using UnityEngine;


public class MovePlayers : MonoBehaviour {
	
	public int speed = 1 ;
	int count=0;
	// Use this for initialization
	void Start () {
	}
	
	// Update is called once per frame
	void Update () {
		moveObjectsAwayFromCamera();
		count++;
	}
	void moveObjectsAwayFromCamera(){
		Vector3 dir = transform.position - Camera.main.transform.position;
		dir.Normalize();
		transform.Translate (dir * speed * Time.deltaTime);
		transform.Translate( Vector3.right * speed * Time.deltaTime);
	}
}
