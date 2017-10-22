using System.Collections;
using System.Collections.Generic;
using System.IO;
using GameDataTypes;
using GameEntities;
using LitJson;
using UnityEngine;
using UnityEngine.UI;
public class ServiceListenerScript : MonoBehaviour {
    //json parser for data recieved from android modules
    public JsonData playerData;

    //set initial team positions
    public static int CAPTURING_TEAM = 0;
    public static int ESCAPING_TEAM = 1;
    private static float RAD_TO_DEGREE = 57.2958f;

    public bool gameStarted = false;
    public bool displayingSessionInfo = true;
    //public JsonData playerData;
    float counter = 10;

    float x, y, z, a ;

    int max, capturedCount;
    string name;
    //strings describing keys in the recieved json file
    private string xPos = "x";
    private string yPos = "y";
    private string zPos = "z";
    private string playerList = "playerArrayList";

    private string maximumPlayers = "maxPlayers";

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
    private string teamID = "teamName";

    private string endTime = "endTime";

    private string hasBeenCaptured = "hasBeenCaptured";
    
    //dimension specification of gameobjects
    public float largestObjectSize = 20;
    public float smallestObjectsSize = 10;
    //default bearing before the AR activity is started
    public float initialBearing =0.0f;
    //prefabs of the player objects
    public Transform escapingMember;
    public Transform capturingMember;
    //list containing all of the spawned members
    public List<PlayerObject> players = new List<PlayerObject> ();
    //Ui elements that display status, remaining members, bearing and closest individuals
    public GameObject currentStatus;
    public GameObject compassReading;

    public GameObject capturedProgress;

    public GameObject closestEscapername;
    //direcion pointer of nearest escaping member and surrounding constants
    public GameObject dirPointer;
    public GameObject pointerInstance;

    public int pointerDist = 3;
    public Vector3 pointerDisplacement = new Vector3 (0, -1, 0);

    string displayString = "";
    string jsonPath;
    string currentPlayerName;
    public string receiverMessage = "";

    AndroidJavaClass javaClass;
    //set initialization parameters
    void Start () {
        /**
        preliminary tests with sample json file of a simple game state
            jsonPath = Application.streamingAssetsPath + "/testSession.json";
            receiverMessage = File.ReadAllText (jsonPath);
        */
         //assignment of ui elements
        closestEscapername = GameObject.FindGameObjectWithTag ("closestEscaperName");
        currentStatus = GameObject.FindGameObjectWithTag ("currentStatusText");
        compassReading = GameObject.FindGameObjectWithTag ("bearingText");
        capturedProgress = GameObject.FindGameObjectWithTag ("capturedText");
        //pointer instatiation
        pointerInstance = Instantiate (dirPointer, Camera.main.transform.position +
            (Camera.main.transform.forward) * pointerDist + pointerDisplacement, Quaternion.identity);
        //declaration of android receiver that fetches json on all player states from android to unity sender service
        javaClass = new AndroidJavaClass ("com.unimelb.comp30022.receiver.UnityReceiver");
        javaClass.CallStatic ("createInstance");
        //spawn all players
    }

    // Update is called once per frame
    public void Update () {
        receiverMessage = javaClass.GetStatic<string> ("text");
        if (gameStarted != true) {
            //generate and displace players depending on the users current bearing
            initialBearing = getBearing(receiverMessage);
            generatePlayers (receiverMessage);
            rotatePlayersByBearing (players,initialBearing);
            gameStarted = true;
        }
    
        compassReading.GetComponent<Text> ().text = "Bearing: " + getBearing (receiverMessage).ToString ();
        updatePlayerLocations (receiverMessage);
        rotatePlayersByBearing (players,initialBearing);
        capturedCount =  updatePlayerCapturedNumbers (receiverMessage);
        max = (int) playerData[maximumPlayers];
        capturedProgress.GetComponent<Text> ().text = "Remaining: " + (max / 2 - capturedCount);
        //fetch and point at the closest escaping member
        PlayerObject closestTarget = getClosestEscaper (receiverMessage);
        if(closestTarget == null){
            closestEscapername.GetComponent<Text> ().text = "Unavailable";
        }
        else{
            closestEscapername.GetComponent<Text> ().text = ((string) closestTarget.id).ToString ();
            pointerInstance.transform.LookAt (closestTarget.gameModel.transform);

        }
        pointerInstance.transform.position = Camera.main.transform.position + (Camera.main.transform.forward) * pointerDist + pointerDisplacement;
    }
    //generate gameObjects for both teams and identify current player
    /**
    @param gamestring json string containing all player positions

     */
    public void generatePlayers (string gameString) {
        //iterate and spawn all objects at relative position
        playerData = gameStateFromJson (gameString);
        if (playerData == null) {
            return;
        }
        //iterate through the gamesession information and determine the current player(coordinate 0,0,0)
        for (int teamIdx = 0; teamIdx < (int) playerData[teamList].Count; teamIdx++) {
            for (int playerIdx = 0; playerIdx < (int) playerData[teamList][teamIdx][playerList].Count; playerIdx++) {
                if (!idInPlayerList (players, playerData[teamList][teamIdx][playerList][playerIdx][id].ToString ())) {
                    //spawn
                    Vector3 pos = GetCoordinate (teamIdx, playerIdx);
                    PlayerObject play;
                    if (pos.x == 0.0 && pos.y == 0.0 && pos.z == 0.0) {
                        currentPlayerName = getDisplayName (teamIdx, playerIdx);
                        if (getCapturing (teamIdx, playerIdx)) {
                            setCapturingState (true);
                        } else {
                            setCapturingState (false);
                        }
                    }
                    //spawn the remainder of the players depending on their team and store their data in the PlayerObject
                    if (getCapturing (teamIdx, playerIdx)) {
                        //spawn chasers 
                        Debug.Log ("Spawning chaser");
                        play = new PlayerObject (Instantiate (capturingMember, pos, Quaternion.identity));
                        play.id = getDisplayName (teamIdx, playerIdx);
                        play.speed = 0;
                        play.lastPing = getPing (teamIdx, playerIdx);
                        colorCapturing (play);
                        resize (play, a);
                        players.Add (play);

                    } else if (!getCapturing (teamIdx, playerIdx)) {
                        //spawn runners and assign the relevant playerObjects their data
                        Debug.Log ("Spawning Runner");
                        play = new PlayerObject (Instantiate (escapingMember, pos, Quaternion.identity));
                        play.id = getDisplayName (teamIdx, playerIdx);
                        play.speed = 0;
                        play.lastPing = getPing (teamIdx, playerIdx);
                        colorEscaping (play);
                        resize (play, a);
                        players.Add (play);

                    }

                }

            }
        }

    }
    //fetches the matching player data from the json input for each playerObject and updates the relative position 
    // and capture states
    /**
    @param gamestring json string contatining updated player positions
     */
    public void updatePlayerLocations (string gameString) {
        playerData = gameStateFromJson (gameString);
        if (playerData == null) {
            return;
        }
        //for each player object, find the matching data from the json file 
        for (int teamIdx = 0; teamIdx < (int) playerData[teamList].Count; teamIdx++) {
            for (int playerIdx = 0; playerIdx < (int) playerData[teamList][teamIdx][playerList].Count; playerIdx++) {
                Vector3 target = GetCoordinate (teamIdx, playerIdx);
                for (int i = 0; i < players.Count; i++) {
                    if (players[i].id == getDisplayName (teamIdx, playerIdx)) {
                        //update the capture state of the current player on the user interface
                        if (players[i].id == currentPlayerName) {
                            setCapturingState (getCapturing (teamIdx, playerIdx));
                        }
                        players[i].gameModel.position = target;
                        //update their capturing states of every other player
                        if (getCapturing (teamIdx, playerIdx) == true) {
                            colorCapturing (players[i]);
                        } else {
                            colorEscaping (players[i]);
                        }
                        //resize according to the number of captured members
                        resize (players[i], getCapturedListLength(teamIdx,playerIdx));

                    }
                }

            }
        }
    }
    //fetches the closest player in the json input and returns a Player object identifying the stated player
    /**
    @param gamestring json string containing all player positions
     */
    public PlayerObject getClosestEscaper (string gameString) {
        playerData = gameStateFromJson (gameString);
        if (playerData == null) {
            return null;
        }
        int closestDistance = int.MaxValue;
        int distance;
        PlayerObject closest = null;
        float timeSinceStarted = 0.0f;
        for (int teamIdx = 0; teamIdx < (int) playerData[teamList].Count; teamIdx++) {
            for (int playerIdx = 0; playerIdx < (int) playerData[teamList][teamIdx][playerList].Count; playerIdx++) {
                Vector3 target = GetCoordinate (teamIdx, playerIdx);
                for (int i = 0; i < players.Count; i++) {
                    //Get the matching player object 
                    if (players[i].id == getDisplayName (teamIdx, playerIdx)) {
                        //among the escaping members, if they are closer than the previous, assign them as the closest
                        if (!getCapturing (teamIdx, playerIdx) && !target.Equals(Vector3.zero)) {
                            Vector3 diff = target - Vector3.zero;
                            distance = (int) Mathf.Sqrt (diff.sqrMagnitude);
                            if (distance < closestDistance) {
                                closest = players[i];
                            }
                        }

                    }
                }
            }
        }

        return closest;
    }
    //rotates vectors according to bearinginformation to provide the matching coordinate in the real world
    /**
    @param point to be rotated
    @param pivot center of rotation
    @param angle angle swept by rotation transformation
     */
    public Vector3 rotateVectorAroundPivot (Vector3 point, Vector3 pivot, Vector3 angle) {
        Vector3 diff = (point - pivot);
        return Quaternion.Euler (angle) * diff + pivot;
    }
    //determines if the current player name is in the list of all players generated
    /**
    @param players list of PlayerObjects that contains the names, status and positions of generated players
    @param id name of player to be searched
     */
    public bool idInPlayerList (List<PlayerObject> players, string id) {
        for (int i = 0; i < players.Count; i++) {
            if (players[i].id == id) {
                return true;
            }
        }
        return false;
    }
    //parses a string using Litjon library to get a structure that can be keyed to fetch relevant game info
    /**
    @param gamestring json string containing all player positions
     */
    public JsonData gameStateFromJson (string gameString) {
        return JsonMapper.ToObject (gameString);
    }
    //fetches the displayname of the player from the Parsed json file
    /**
    @param teamIdx index of the team being queried
    @param playerIdx index of the player in the team being queried
     */
    public string getDisplayName (int teamIdx, int playerIdx) {
        return (string) playerData[teamList][teamIdx][playerList][playerIdx][id];
    }
    //fetches the position as a Vector for the current player in the json structure
    /**
    @param teamIdx index of the team being queried
    @param playerIdx index of the player in the team being queried
     */
    public Vector3 GetCoordinate (int teamIdx, int playerIdx) {
        return new Vector3 (
            (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][xPos],
            (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][yPos],
            (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][zPos]);
    }
    //fetches the bearing of the gamestate from the android broadcast
    /**
    @param gamestring json string containing all player positions
     */
    public float getBearing (string gameString) {
        playerData = gameStateFromJson (gameString);
        return (float) (double) playerData[bearing];
    }

    //fetches the activity state of players
    /**
    @param teamIdx index of the team being queried
    @param playerIdx index of the player in the team being queried
     */
    public bool getActive (int teamIdx, int playerIdx) {
        return (bool) playerData[teamList][teamIdx][playerList][playerIdx][isActive];
    }
    //fetches whether players are capturing or being captured
    /**
    @param teamIdx index of the team being queried
    @param playerIdx index of the player in the team being queried
     */
    public bool getCapturing (int teamIdx, int playerIdx) {
        return (bool) playerData[teamList][teamIdx][playerList][playerIdx][capt];
    }
    //fetches whether a player has been captured
    /**
    @param teamIdx index of the team being queried
    @param playerIdx index of the player in the team being queried
     */
    public bool getHasBeenCaptured (int teamIdx, int playerIdx) {
        return (bool) playerData[teamList][teamIdx][playerList][playerIdx][hasBeenCaptured];
    }
    //fetches the most frequent ping value in epoch time
    /**
    @param teamIdx index of the team being queried
    @param playerIdx index of the player in the team being queried
     */
    public long getPing (int teamIdx, int playerIdx) {
        return (long) playerData[teamList][teamIdx][playerList][playerIdx][ping];
    }
    //sets the UI depending on whether the current player is escaping or capturing other players
    /**
    @param teamIdx index of the team being queried
    @param playerIdx index of the player in the team being queried
     */
    public void setCapturingState (bool capturing) {
        if (capturing) {
            currentStatus.GetComponent<Text> ().text = "Currently Capturing";

        } else {
            currentStatus.GetComponent<Text> ().text = "Currently Escaping";

        }

    }
    //fetches the path of the player as a list of vectors
    /**
    @param teamIdx index of the team being queried
    @param playerIdx index of the player in the team being queried
     */
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
    //fetches the length of the player's list of captured members
    /**
    @param teamIdx index of the team being queried
    @param playerIdx index of the player in the team being queried
     */
    public int getCapturedListLength (int teamIdx, int playerIdx) {
        return (int) playerData[teamList][teamIdx][playerList][playerIdx][captured].Count;
    }
    //rotates the coorinates of the players so as to match the real world coordinates, using the inital bearing
    /**
    @param players list of all the players generated from the input json string
    @param bearing orientation from magnetic north of the current player on AR initialisation
     */
    public void rotatePlayersByBearing (List<PlayerObject> players, float bearing) {
        Debug.Log ("rotating around" + bearing.ToString ());
        for (int i = 0; i < players.Count; i++) {
            players[i].gameModel.transform.position =
                rotateVectorAroundPivot (players[i].gameModel.transform.position, Vector3.zero, new Vector3 (0, -bearing, 0));
        }
    }
    //fetches the latest number of captured individuals 
    /**
    @param gamestring json string containing all player positions
     */
    public int updatePlayerCapturedNumbers (string gameString) {
        playerData = gameStateFromJson (gameString);
        if (playerData == null) {
            return 0;
        }
        
        int capturedCount = 0;
        for (int teamIdx = 0; teamIdx < (int) playerData[teamList].Count; teamIdx++) {
            for (int playerIdx = 0; playerIdx < (int) playerData[teamList][teamIdx][playerList].Count; playerIdx++) {
                for (int i = 0; i < players.Count; i++) {
                    if (players[i].id == getDisplayName (teamIdx, playerIdx)) {
                        if (getCapturing (teamIdx, playerIdx) == true && getHasBeenCaptured (teamIdx, playerIdx) == true) {
                            capturedCount++;
                        }
                    }
                }
            }
        }
        return capturedCount;
    }

    //colors the playerObject instance
    /**
    @param player object containing identifying info and coordinate position
     */

    public void colorEscaping (PlayerObject player) {
        player.gameModel.GetComponent<MeshRenderer> ().material.SetColor ("_Color", Color.green);
    }
    //colors the playerObject instance
    /**
    @param player object containing identifying info and coordinate position
     */
    public void colorInactive (PlayerObject player) {
        player.gameModel.GetComponent<MeshRenderer> ().material.SetColor ("_Color", Color.grey);
    }
    //colors the playerObject instance
    /**
    @param player object containing identifying info and coordinate position
     */
    public void colorCapturing (PlayerObject player) {
        player.gameModel.GetComponent<MeshRenderer> ().material.SetColor ("_Color", Color.red);
    }
    //resizes the playerObject instance
    /**
    @param player object containing identifying info and coordinate position
    @param size new size of the player's model
     */
    public void resize (PlayerObject player, float size) {
        if (size > largestObjectSize) {
            size = largestObjectSize;
        }
        if (size < smallestObjectsSize) {
            size = smallestObjectsSize;
        }
        player.gameModel.transform.localScale = new Vector3 (size, size, size);
    }

}