using System.Collections;
using System.Collections.Generic;
using System.IO;
using GameDataTypes;
using GameEntities;
using LitJson;
using UnityEngine;

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
    float x, y, z, a;
    string name;

    private string xPos = "x";
    private string yPos = "y";
    private string zPos = "z";
    private string playerList = "playerArrayList";

    private string isActive = "isActive";
    private string isCapturing = "isCapturing";

    private string coordinate = "coordinateLocation";

    private string captured = "capturedList";

    private string username = "displayName";

    private string logged = "isLoggedOn";

    private string abs = "absLocation";

    private string lat = "latitude";

    private string lon = "longitude";

    private string acc = "accuracy";
    private string capList = "capturedList";

    private string disp = "displayName";

    private string actv = "isActive";

    private string path = "relativePath";

    private string capt = "isCapturing";

    private string creator = "creator";
    private string teamList = "teamArrayList";
    private string bearing = "bearing";

    private string id = "displayName";

	private string ping = "lastPing";

    public CoordinateLocation cPos;

    public Transform escapingMember;
    public Transform capturingMember;
    public List<PlayerObject> players = new List<PlayerObject> ();
	private Vector3 newPos = new Vector3(50,50,50);
    /**
    Test variables 
     */

    string jsonPath;
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
        jsonPath = Application.streamingAssetsPath + "/testSession.json";
        jsonString = File.ReadAllText (jsonPath);
        //javaClass = new AndroidJavaClass("com.unimelb.comp30022.receiver.UnityReceiver");
        //javaClass.CallStatic ("createInstance");
        //spawn all players

    }

    // Update is called once per frame
    public void Update () {
        //receiverMessage = javaClass.GetStatic<string> ("text");
        receiverMessage = jsonString;
        Debug.Log ("Game started is " + gameStarted.ToString ());
        if (gameStarted == false) {
            Debug.Log ("should only print once");
            generatePlayers (receiverMessage);
            Debug.Log ("should only print once");
            GetComponent<TextMesh> ().text = receiverMessage;
            gameStarted = true;
        } else {
            if (displayingSessionInfo && counter > 0) {
                //display session information
                counter -= Time.fixedDeltaTime;
            } else {
                displayingSessionInfo = false;
                GetComponent<TextMesh> ().text = "";
            }
        }
        updatePlayerLocations (receiverMessage);

    }
    public void generateNewPositions (string gameString) {

    }
    public void generatePlayers (string gameString) {
        //iterate and spawn all objects at relative position
        playerData = gameStateFromJson (jsonString);
        PlayerObject spawner;
        Debug.Log (playerData[teamList][0][playerList].Count);

        for (int teamIdx = 0; teamIdx < (int) playerData[teamList].Count; teamIdx++) {
            for (int playerIdx = 0; playerIdx < (int) playerData[teamList][teamIdx][playerList].Count; playerIdx++) {
                if (getActive (teamIdx, playerIdx)) {
                    Debug.Log (teamIdx + " " + playerIdx);
                    x = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][xPos];
                    y = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][yPos];
                    z = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][zPos];
                    a = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][acc];
                    name = (string) playerData[teamList][teamIdx][playerList][playerIdx][id];
                    Debug.Log ("getCapturing is " + getCapturing (teamIdx, playerIdx).ToString ());
                    Debug.Log ("Coordinate is " + "x " + x.ToString () + "y " + y.ToString () + "z " + z.ToString ());
                    PlayerObject play;
                    if (getCapturing (teamIdx, playerIdx) == true) {
                        //spawn chasers
                        Debug.Log ("Spawning chaser");
                        Debug.Log ("generating item at position" + x + ", " + y + "," + z);
                        play = new PlayerObject (Instantiate (capturingMember, new Vector3 (x, y, z), Quaternion.identity));
                        play.id = name;
						play.speed = 0;
						play.lastPing = getPing(teamIdx,playerIdx);
                        colorCapturing (play);
                        players.Add (play);

                    } else if (getCapturing (teamIdx, playerIdx) == false) {
                        //spawn runners
                        Debug.Log ("Spawning Runner");
                        Debug.Log ("generating item at position" + x + ", " + y + "," + z);
                        play = new PlayerObject (Instantiate (escapingMember, new Vector3 (x, y, z), Quaternion.identity));
                        play.id = name;
						play.speed = 0;
						play.lastPing = getPing(teamIdx,playerIdx);
                        colorEscaping (play);
                        players.Add (play);

                    }
                }

            }
        }

    }
    //parse json file into heirarchy
    public static JsonData gameStateFromJson (string jsonString) {
        return JsonMapper.ToObject (jsonString);
    }

    public LatLngClass getAbsLocation (int teamIdx, int playerIdx) {

        return new LatLngClass ((float) (double) playerData[teamList][teamIdx][playerList][playerIdx][abs][lon], (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][abs][lat],
            (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][abs][acc]);
    }
    public string getDisplayName (int teamIdx, int playerIdx) {
        return (string) playerData[teamList][teamIdx][playerList][playerIdx][id];
    }
    public CoordinateLocation GetCoordinate (int teamIdx, int playerIdx) {
        return new CoordinateLocation ((float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][xPos], (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][yPos], (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][zPos], (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][acc]);
    }
    public bool getActive (int teamIdx, int playerIdx) {
        return (bool) playerData[teamList][teamIdx][playerList][playerIdx][isActive];
    }
    public bool getCapturing (int teamIdx, int playerIdx) {
        return (bool) playerData[teamList][teamIdx][playerList][playerIdx][capt];
    }
	public long getPing (int teamIdx, int playerIdx) {
        return (long) playerData[teamList][teamIdx][playerList][playerIdx][ping];
    }
    public List<CoordinateLocation> getRelPath (int teamIdx, int playerIdx) {
        List<CoordinateLocation> lst = new List<CoordinateLocation> ();
        for (int i = 0; i < (int) playerData[teamList][teamIdx][playerList][path].Count; i++) {
            lst.Add (new CoordinateLocation (
                (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][path][i][yPos],
                (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][path][i][yPos],
                (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][path][i][yPos], (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][path][i][acc]));
        }
        return lst;
    }

    public int getCapturedListLength (int teamIdx, int playerIdx) {
        return (int) playerData[teamList][teamIdx][playerList][playerIdx][captured];
    }
    public void updatePlayerLocations (JsonData data) {
        playerData = gameStateFromJson (jsonString);
		float timeSinceStarted = 0f;

        for (int teamIdx = 0; teamIdx < (int) playerData[teamList].Count; teamIdx++) {
            for (int playerIdx = 0; playerIdx < (int) playerData[teamList][teamIdx][playerList].Count; playerIdx++) {
				//transform.Translate(Vector3.right * movementSpeed * Time.deltaTime);

               x = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][xPos];
                y = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][yPos];
                z = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][zPos];
                a = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][acc];
                name = (string) playerData[teamList][teamIdx][playerList][playerIdx][id];
				Vector3 target = new Vector3(x,y,z);
                for (int i=0;i<players.Count; i++) {
                    if (players[i].id == getDisplayName (teamIdx, playerIdx)) {
						Debug.Log("players.gameModel.position");
						float xDiff = x - players[i].gameModel.position.x;
						float yDiff = y - players[i].gameModel.position.y;
						float zDiff = z - players[i].gameModel.position.z;
						float distance = Mathf.Sqrt(xDiff*xDiff + yDiff*yDiff+zDiff*zDiff);
						long timeDiff= (long)playerData[teamList][teamIdx][playerList][playerIdx][ping]-players[i].lastPing;
						if(timeDiff != 0){
							players[i].speed = (float)distance/timeDiff;
						}
						players[i].speed = 0.2f;
						players[i].gameModel.position = Vector3.MoveTowards(players[i].gameModel.position,target,players[i].speed*Time.deltaTime);
                        if (getActive (teamIdx, playerIdx)) {
                            if (getCapturing (teamIdx, playerIdx) == true) {
								colorCapturing(players[i]);
                            } else {
								colorEscaping(players[i]);
                            }
                        } else {
                            colorInactive(players[i]);
                        }

                    }

                }
            }
        }
    }

  
    public void updatePlayerActiveStates (JsonData data) {
        //check if they are active, if not, change their colors to grey

    }
    public void rotatePlayersByBearing (JsonData data) {

    }
    public void updatePlayerCapturedNumbers () {

    }
    public void updatefootprintlocations () {

    }

    public void updatePointer () {

    }

    public void colorEscaping (PlayerObject player) {
        player.gameModel.GetComponent<MeshRenderer> ().material.SetColor ("_Color", Color.red);
    }
    public void colorInactive (PlayerObject player) {
        player.gameModel.GetComponent<MeshRenderer> ().material.SetColor ("_Color", Color.grey);
    }
    public void colorCapturing (PlayerObject player) {
        player.gameModel.GetComponent<MeshRenderer> ().material.SetColor ("_Color", Color.green);

    }
	
    public void animateHasCaptured () {

    }
    public void startGameSequence () {

    }
    public void endGameSequence () {

    }

}