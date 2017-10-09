using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Game;
using GameEntities;
[System.Serializable]
public class ServiceListenerScript : MonoBehaviour {
	//set initial team positions
	public static int CAPTURING_TEAM = 0;
	public static int ESCAPING_TEAM = 1;
	public string receiverMessage = "";
	public bool gameStarted = false;
	public bool displayingSessionInfo = true;
	float counter = 10;
	public Transform escapingMember;
	public Transform capturingMember;
	public List<Transform> chasers;
	public List<Transform> runners;
	AndroidJavaClass javaClass;
	// Use this for initialization
	void Start () {
		javaClass = new AndroidJavaClass("com.unimelb.comp30022.receiver.UnityReceiver");
		javaClass.CallStatic ("createInstance");
		//spawn all players
	}

	// Update is called once per frame
	public void Update () {
		receiverMessage = javaClass.GetStatic<string> ("text");
		if (gameStarted == false) {
			//generatePlayers (receiverMessage);
			GetComponent<TextMesh> ().text = receiverMessage;
			gameStarted = true;
		} else {
			if (displayingSessionInfo && counter > 0)
				//display session information
				counter -= Time.fixedDeltaTime;
			else {
				displayingSessionInfo = false;
				GetComponent<TextMesh> ().text = "";
			}
			//updatePlayerLocations (receiverMessage);
		}
	}
	public void generatePlayers(string gameString){
		GameSessionClass currentGame = gameStateFromJson (gameString);
		GetComponent<TextMesh> ().text = "Creator: " + currentGame.creator.displayName+ "\n"+
			"Team Names: " +currentGame.teamArrayList[0] + "  and "+ currentGame.teamArrayList[1]+ "\n"+
			"number of players :"+currentGame.teamArrayList[0].playerArraylist.Count+currentGame.teamArrayList[1].playerArraylist.Count ; 
		foreach(TeamClass team in currentGame.teamArrayList){
			foreach (PlayerClass player in team.playerArraylist) {
				float relX = (float)player.coordinateLocation.x;
				float relY = (float)player.coordinateLocation.y;
				float relZ = (float)player.coordinateLocation.z;
				if (player.isCapturing) {
					//spawn capturing indivisual at position
						chasers.Add(Instantiate(capturingMember,new Vector3(relX,relY,relZ),Quaternion.identity));

				} else {
					//spawn escaping individual at position
					runners.Add(Instantiate(escapingMember,new Vector3(relX,relY,relZ),Quaternion.identity));
				}
			}
		}
		GetComponent<TextMesh>().transform.position = Camera.main.transform.position + Camera.main.transform.forward* 10;
	}
	public static GameSessionClass gameStateFromJson(string jsonString){
		return JsonUtility.FromJson<GameSessionClass> (jsonString);
	}
	public void updatePlayerLocations(string recieverMessage){
		GameSessionClass currentGameState = gameStateFromJson (recieverMessage);

		foreach(PlayerClass player in currentGameState.teamArrayList[CAPTURING_TEAM].playerArraylist){
			//update positions for members in team 
		}
		foreach (PlayerClass player in currentGameState.teamArrayList[ESCAPING_TEAM].playerArraylist) {
			//update positions for members in team
		}
	}
	public void animateCaptured(){

	}
	public void animateAlmostCaptured(){

	}
	public void animateCapturing(){

	}
	public void animateHasCaptured(){
		
	}
	public void startGameSequence(){

	}
	public void endGameSequence(){

	}
}
