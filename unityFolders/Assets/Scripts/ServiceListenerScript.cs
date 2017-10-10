using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GameEntities;
using GameDataTypes;
using System.IO;
using LitJson;
[System.Serializable]
public class ServiceListenerScript : MonoBehaviour {
	//set initial team positions
	public static int CAPTURING_TEAM = 0;
	public static int ESCAPING_TEAM = 1;
	public string receiverMessage = "uninitiated reciever Message";
	public bool gameStarted = false;
	public bool displayingSessionInfo = true;
    public JsonData playerData;
	float counter = 10;
	float x,y,z;

	private string xPos = "x";
	private string yPos = "y";
	private string zPos = "z";

	private string isActive = "isActive";
	private string isCapturing ="isCapturing";

	private string coordinate ="coordinateLocation";

	private string captured = "capturedList";

	private string username = "displayName";

	private string logged = "isLoggedOn";

	private string abs = "absLocation";

	private string lat = "latitude";

	private string lon = "longitude";
	private string capList = "capturedList";

	private string disp = "displayName";

	private string actv = "isActive";

	private string path = "relativePath";

	private string capt = "isCapturing";
	public Transform escapingMember;
	public Transform capturingMember;
	public List<playerObject> chasers = new List<playerObject>();
	public List<playerObject> escapers = new List<playerObject>();

	/**
	Test variables 
	 */

	string jsonPath ;
	string jsonString;
	 /**
	 Test variables
	  */
	AndroidJavaClass javaClass;
	// Use this for initialization
	void Start () {
		/**
			preliminary tests	
		 */
		//jsonPath = Application.streamingAssetsPath + "/testSession.json"; 
	    //jsonString =File.ReadAllText(jsonPath);
		//Debug.Log("Before parsing"+ jsonString);
		//playerData = gameStateFromJson(jsonString);
        //Debug.Log("First player name " + playerData["playerArrayList"][0]["capturedList"][0].ToString());
        //Debug.Log( playerData["playerArrayList"][0]["coordinateLocation"]["x"]);
        javaClass = new AndroidJavaClass("com.unimelb.comp30022.receiver.UnityReceiver");
        javaClass.CallStatic ("createInstance");
        //spawn all players
    }

    // Update is called once per frame
    public void Update () {
		receiverMessage = javaClass.GetStatic<string> ("text");
		if (gameStarted == false) {
			generatePlayers (receiverMessage);
			GetComponent<TextMesh> ().text = receiverMessage;
			gameStarted = true;
		} else {
			if (displayingSessionInfo && counter > 0)
			{	
				//display session information
				counter -= Time.fixedDeltaTime;
			}	
			else {
				displayingSessionInfo = false;
				GetComponent<TextMesh> ().text = "";
			}
			updatePlayerLocations (receiverMessage);
			//updatefootprintlocations()

		}
	}
	public void generatePlayers(string gameString){
		//iterate and spawn all objects at relative position
		playerObject spawner;
		for(int i =0; i< (int)playerData["playerArrayList"].Count;i++)
		{
			
			if( getActive(i)){
				x = (float)(double)playerData["playerArrayList"][i][coordinate][xPos];
				y = (float)(double)playerData["playerArrayList"][i][coordinate][yPos];
				z = (float)(double)playerData["playerArrayList"][i][coordinate][zPos];
				Debug.Log("is active");
				Debug.Log(playerData["playerArrayList"][i][isCapturing].ToString());
				if(x == 0.0 && y == 0.0 && z == 0.0){
					//player position
					return;
				}
				playerObject play ;
				if( getCapturing(i) == true)
				{
					//spawn chasers
					Debug.Log("Spawning chaser");
					play = new playerObject(Instantiate(capturingMember,new Vector3(x ,y,z),Quaternion.identity));
					animateCapturing(play);
					chasers.Add(play);

				}
				else if(getCapturing(i) == false)
				{
					//spawn runners
					Debug.Log("Spawning Runner");
					play = new playerObject(Instantiate(escapingMember,new Vector3(x ,y,z),Quaternion.identity));
					animateCaptured(play);
					escapers.Add(play);
				}
			}
			
		}
	}
	//parse json file into heirarchy
	public static JsonData gameStateFromJson(string jsonString){
		return JsonMapper.ToObject(jsonString);
	}
	
	public void updatePlayerLocations(JsonData data){
		playerData = gameStateFromJson(jsonString);

		foreach(playerObject player in chasers){
			//update positions for members in team 
			//player.gameModel.transform.position = 
			//player.gameModel.position = 
			Debug.Log(" position of chaser"+ player.gameModel.position.ToString());

		}
		foreach (playerObject player in escapers) {
			//update positions for members in team
			Debug.Log(" position of Runner"+ player.gameModel.position.ToString());

		}
	}
	public LatLngClass getAbsLocation(int playerIdx){

		return new LatLngClass((float)(double)playerData["playerArrayList"][playerIdx][abs][lon]
				,(float)(double)playerData["playerArrayList"][playerIdx][abs][lat]);
	}
	public CoordinateLocation GetCoordinate(int playerIdx){
		return new CoordinateLocation((float)(double)playerData["playerArrayList"][playerIdx][coordinate][xPos]
				,(float)(double)playerData["playerArrayList"][playerIdx][coordinate][yPos]
				,(float)(double)playerData["playerArrayList"][playerIdx][coordinate][zPos]);
	}
	public bool getActive(int playerIdx){
		return (bool)playerData["playerArrayList"][playerIdx][isActive];
	}
	public bool getCapturing(int playerIdx){
		return (bool)playerData["playerArrayList"][playerIdx][capt];
	}
	public List<CoordinateLocation> getRelPath(int playerIdx){
		List<CoordinateLocation> lst = new List<CoordinateLocation>();
		for(int i =0; i< (int)playerData["playerArrayList"][path].Count;i++){
			lst.Add(new CoordinateLocation(
			(float)(double)playerData["playerArrayList"][playerIdx][path][i][yPos],
			(float)(double)playerData["playerArrayList"][playerIdx][path][i][yPos],
			(float)(double)playerData["playerArrayList"][playerIdx][path][i][yPos]));
		}
		return lst;
	}

	public int getCapturedListLength(int playerIdx){
		return (int)playerData["playerArrayList"][playerIdx][captured];
	}

	public void animateCaptured(playerObject player){
		player.gameModel.GetComponent<MeshRenderer>().material.SetColor("_Color",Color.red);
	}
	public void animateAlmostCaptured(playerObject player){
		player.gameModel.GetComponent<MeshRenderer>().material.SetColor("_Color",Color.blue);
	}
	public void animateCapturing(playerObject player){
		player.gameModel.GetComponent<MeshRenderer>().material.SetColor("_Color",Color.green);

	}
	public void animateHasCaptured(){
		
	}
	public void startGameSequence(){

	}
	public void endGameSequence(){
		
	}
}
