using System.Collections;
using System.Collections.Generic;
using System.IO;
using GameDataTypes;
using GameEntities;
using LitJson;
using UnityEngine;
using UnityEngine.UI;
public class ServiceListenerScript : MonoBehaviour {

    //set initial team positions
    public static int CAPTURING_TEAM = 0;
    public static int ESCAPING_TEAM = 1;
    public bool gameStarted = false;
    public bool displayingSessionInfo = true;
    //public JsonData playerData;
    float counter = 10;

    public JsonData playerData;

    float x, y, z, a;
    string name;

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

    public CoordinateLocation cPos;

    public float largestObjectSize = 15;
    public float smallestObjectsSize = 5;
    public Transform escapingMember;
    public Transform capturingMember;
    public List<PlayerObject> players = new List<PlayerObject> ();
    public GameObject teamName;
    public GameObject remainingTime;
    public GameObject currentStatus;
    public GameObject compassBearing;

    public GameObject capturedProgress;

    public GameObject dirPointer;

    private Vector3 newPos = new Vector3 (17, 0, 50);

    string displayString = "";
    string jsonPath;
    string currentPlayerName;
    public string receiverMessage = "Uninitiated";

    float count = 0.0f;
    AndroidJavaClass javaClass;
    //set initialization parameters
    void Start () {
        /**
        	preliminary tests	
         */
        teamName = GameObject.FindGameObjectWithTag ("teamNameText");
        remainingTime = GameObject.FindGameObjectWithTag ("remainingTimeText");
        currentStatus = GameObject.FindGameObjectWithTag ("currentStatusText");
        compassBearing = GameObject.FindGameObjectWithTag ("bearingText");
        capturedProgress = GameObject.FindGameObjectWithTag ("capturedText");
        //jsonPath = Application.streamingAssetsPath + "/testSession.json";
        //receiverMessage = File.ReadAllText (jsonPath);
        javaClass = new AndroidJavaClass ("com.unimelb.comp30022.receiver.UnityReceiver");
        javaClass.CallStatic ("createInstance");
        //spawn all players

    }

    // Update is called once per frame
    public void Update () {
        receiverMessage = javaClass.GetStatic<string> ("text");
        if (gameStarted != true) {
            generatePlayers(receiverMessage);
            rotatePlayersByBearing (players, (float) (double) playerData[bearing]);
            gameStarted = true;
        }
        compassBearing.GetComponent<Text> ().text = "Bearing: " + getBearing (receiverMessage).ToString ();
        if (displayingSessionInfo && counter > 0) {
            counter -= Time.fixedDeltaTime;
        } else {
            displayingSessionInfo = false;
        }

        updatePlayerLocations (receiverMessage);
        updatePlayerCapturedNumbers (receiverMessage);
        getRemainingTime (receiverMessage);
        getClosestEscaper (receiverMessage);
    }

    
    public void generatePlayers (string gameString) {
        //iterate and spawn all objects at relative position
        playerData = gameStateFromJson (gameString);
        if (playerData == null) {
            return;
        }

        int count = 0;
        for (int teamIdx = 0; teamIdx < (int) playerData[teamList].Count; teamIdx++) {
            // Debug.Log(playerData[teamList][teamIdx]["startTime"].ToString());
            for (int playerIdx = 0; playerIdx < (int) playerData[teamList][teamIdx][playerList].Count; playerIdx++) {
                if (!idInPlayerList (players, playerData[teamList][teamIdx][playerList][playerIdx][id].ToString ())) {
                    //spawn
                    Vector3 pos = GetCoordinate (teamIdx, playerIdx);
                    PlayerObject play;
                    if (pos.x == 0.0 && pos.y == 0.0 && pos.z == 0.0) {
                        currentPlayerName = getDisplayName (teamIdx, playerIdx);
                        teamName.GetComponent<Text> ().text = (string) playerData[teamList][teamIdx][teamID];
                        if(getCapturing(teamIdx,playerIdx)){
                            setCapturingState(true);
                        }
                        else{
                            setCapturingState(false);
                        }
                    }
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
                        //spawn runners
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
    public void updatePlayerLocations (string gameString) {
        playerData = gameStateFromJson (gameString);
        if (playerData == null) {
            return;
        }
        float timeSinceStarted = 0.0f;
        for (int teamIdx = 0; teamIdx < (int) playerData[teamList].Count; teamIdx++) {
            for (int playerIdx = 0; playerIdx < (int) playerData[teamList][teamIdx][playerList].Count; playerIdx++) {
                Vector3 target = GetCoordinate (teamIdx, playerIdx);
                target = rotateVectorAroundPivot (target, Vector3.up, new Vector3 (0.0f, getBearing (gameString), 0.0f));
                for (int i = 0; i < players.Count; i++) {
                    if (players[i].id == getDisplayName (teamIdx, playerIdx)) {
                        if (players[i].id == currentPlayerName) {
                            setCapturingState (getCapturing (teamIdx, playerIdx));
                        }
                        Debug.Log ("players.gameModel.position");
                        Vector3 diff = target - players[i].gameModel.position;
                        float distance = Mathf.Sqrt (diff.sqrMagnitude);
                        long timeDiff = getPing (teamIdx, playerIdx) - players[i].lastPing;
                        if (timeDiff != 0) {
                            players[i].speed = (float) distance / timeDiff;
                            players[i].gameModel.position = Vector3.MoveTowards (players[i].gameModel.position, target, players[i].speed * Time.deltaTime);
                        } else {
                            players[i].gameModel.position = target;
                        }
                        if (getActive (teamIdx, playerIdx)) {
                            if (getCapturing (teamIdx, playerIdx) == true) {
                                colorCapturing (players[i]);
                            } else {
                                colorEscaping (players[i]);
                            }
                        } else {
                            colorInactive (players[i]);
                        }
                        resize (players[i], a);
                        players[i].lastPing = getPing (teamIdx, playerIdx);

                    }
                }

            }
        }
    }

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
                    if (players[i].id == getDisplayName (teamIdx, playerIdx)) {
                        if (!getCapturing (teamIdx, playerIdx)) {
                            Debug.Log ("players.gameModel.position");
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
        float speed = 0.1f;
        return closest;
    }

    public Vector3 rotateVectorAroundPivot (Vector3 point, Vector3 pivot, Vector3 angle) {
        Vector3 diff = (point - pivot);
        return Quaternion.Euler (angle) * diff + pivot;
    }
    public bool idInPlayerList (List<PlayerObject> players, string id) {
        for (int i = 0; i < players.Count; i++) {
            if (players[i].id == id) {
                return true;
            }
        }
        return false;
    }
    public JsonData gameStateFromJson (string gameString) {
        return JsonMapper.ToObject (gameString);
    }

    public string getDisplayName (int teamIdx, int playerIdx) {
        return (string) playerData[teamList][teamIdx][playerList][playerIdx][id];
    }
    public Vector3 GetCoordinate (int teamIdx, int playerIdx) {
        return new Vector3 (
            (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][xPos],
            (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][yPos],
            (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][zPos]);
    }
    public float getBearing (string gameString) {
        playerData = gameStateFromJson (gameString);
        return (float) (double) playerData[bearing];
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
    public void getRemainingTime (string gameString) {
        playerData = gameStateFromJson (gameString);
        long epoch = (long) (System.DateTime.Now - new System.DateTime (1970, 1, 1)).TotalSeconds;
        remainingTime.GetComponent<Text> ().text = "Time left(s): " + (((long) playerData[endTime] - epoch) / 1000).ToString ();

    }
    public void setCapturingState (bool capturing) {
        if (capturing) {
            currentStatus.GetComponent<Text> ().text = "Capturing";

        } else {
            currentStatus.GetComponent<Text> ().text = "Escaping";

        }

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

    public void rotatePlayersByBearing (List<PlayerObject> players, float bearing) {
        Debug.Log ("rotating around" + bearing.ToString ());
        for (int i = 0; i < players.Count; i++) {
            players[i].gameModel.transform.position =
             rotateVectorAroundPivot(players[i].gameModel.transform.position,Vector3.zero,new Vector3(0,-bearing,0));
        }
    }

    public void updatePlayerCapturedNumbers (string gameString) {
        playerData = gameStateFromJson (gameString);
        if (playerData == null) {
            return;
        }
        int max = (int) playerData[maximumPlayers];
        int capturedCount = 0;
        for (int teamIdx = 0; teamIdx < (int) playerData[teamList].Count; teamIdx++) {
            for (int playerIdx = 0; playerIdx < (int) playerData[teamList][teamIdx][playerList].Count; playerIdx++) {
                for (int i = 0; i < players.Count; i++) {
                    if (players[i].id == getDisplayName (teamIdx, playerIdx)) {
                        capturedCount++;
                        if (getCapturing (teamIdx, playerIdx) == true) {
                            capturedCount++;
                        }
                    }
                }
            }
        }
        capturedProgress.GetComponent<Text> ().text = "Remaining: " + (max/2 - capturedCount);

    }
    public void updatefootprintlocations () {

    }

    public void updatePointer () {

    }

    public void colorEscaping (PlayerObject player) {
        player.gameModel.GetComponent<MeshRenderer> ().material.SetColor ("_Color", Color.green);
    }
    public void colorInactive (PlayerObject player) {
        player.gameModel.GetComponent<MeshRenderer> ().material.SetColor ("_Color", Color.grey);
    }
    public void colorCapturing (PlayerObject player) {
        player.gameModel.GetComponent<MeshRenderer> ().material.SetColor ("_Color", Color.red);
    }
    public void resize (PlayerObject player, float size) {
        if (size > largestObjectSize) {
            size = largestObjectSize;
        }
        if (size < smallestObjectsSize) {
            size = smallestObjectsSize;
        }
        player.gameModel.transform.localScale = new Vector3 (size, size, size);
    }

    public void animateHasCaptured () {

    }
    public void startGameSequence () {

    }
    public void endGameSequence () {

    }

}