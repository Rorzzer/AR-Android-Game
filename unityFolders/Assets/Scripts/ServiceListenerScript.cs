using System.Collections;
using System.Collections.Generic;
using System.IO;
using GameDataTypes;
using GameEntities;
using LitJson;
using UnityEngine;

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

    public float largestObjectSize = 15;
    public float smallestObjectsSize = 5;
    public Transform escapingMember;
    public Transform capturingMember;
    public List<PlayerObject> players = new List<PlayerObject> ();
    private Vector3 newPos = new Vector3 (17, 0, 50);
   

    string displayString = "";
    string jsonPath;
    
   
    public string receiverMessage = "Uninitiated";

    float count  = 0.0f;
    AndroidJavaClass javaClass;
    // Use this for initialization
    void Start () {
        /**
        	preliminary tests	
         */
        //jsonPath = Application.streamingAssetsPath + "/testSession.json";
        //receiverMessage = File.ReadAllText (jsonPath);
        javaClass = new AndroidJavaClass ("com.unimelb.comp30022.receiver.UnityReceiver");
        javaClass.CallStatic ("createInstance");

        //spawn all players
        
    }

    // Update is called once per frame
    public void Update () {
        receiverMessage = javaClass.GetStatic<string> ("text");
        if(gameStarted != true){
            generatePlayers (receiverMessage);
            rotatePlayersByBearing(players,(float)(double)playerData[bearing]);
            gameStarted = true;
        }
        changeText(receiverMessage);
        
        if (displayingSessionInfo && counter > 0) {
            //display session information
            counter -= Time.fixedDeltaTime;
        } else {
            displayingSessionInfo = false;
             GetComponent<TextMesh> ().text = "";
        }
        updatePlayerLocations (receiverMessage);
        //Vector3 output = rotateVectorAroundPivot(newPos,Vector3.zero,new Vector3(0.0f,getBearing(receiverMessage),0.0f));
        //Debug.Log("rotation of vector new pos by 90 deg = "+ output.ToString());

    }
  
    void changeText (string newText) {
        GetComponent<TextMesh> ().text = displayString;


    }
    public void generateNewPositions (string gameString) {

    }
    public void generatePlayers (string gameString) {
        //iterate and spawn all objects at relative position
        playerData = gameStateFromJson (gameString);
        if(playerData == null){
            return;
        }
        
        int count = 0;
        for (int teamIdx = 0; teamIdx < (int) playerData[teamList].Count; teamIdx++) {
           // Debug.Log(playerData[teamList][teamIdx]["startTime"].ToString());
            for (int playerIdx = 0; playerIdx < (int) playerData[teamList][teamIdx][playerList].Count; playerIdx++) {
                if (!idInPlayerList (players, playerData[teamList][teamIdx][playerList][playerIdx][id].ToString ())) {
                    //spawn
                    if (getActive (teamIdx, playerIdx)) {
                            
                        x = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][xPos];
                        y = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][yPos];
                        z = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][zPos];
                        a = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][acc];
                        
                        name = (string) playerData[teamList][teamIdx][playerList][playerIdx][id];
                        PlayerObject play;
                        
                        if (getCapturing (teamIdx, playerIdx) == true) {
                            //spawn chasers
                            Debug.Log ("Spawning chaser");
                            Debug.Log ("generating item at position" + x + ", " + y + "," + z);
                            play = new PlayerObject (Instantiate (capturingMember, new Vector3 (x, y, z), Quaternion.identity));
                            play.id = name;
                            play.speed = 0;
                            play.lastPing = getPing (teamIdx, playerIdx);
                            colorCapturing (play);
                            resize(play,a);
                            players.Add (play);
                            displayString += play.id.ToString () + "@" + x.ToString () + "," + y.ToString () + "," + z.ToString () + "\n";

                        } else if (getCapturing (teamIdx, playerIdx) == false) {
                            //spawn runners
                            Debug.Log ("Spawning Runner");
                            Debug.Log ("generating item at position" + x + ", " + y + "," + z);
                            play = new PlayerObject (Instantiate (escapingMember, new Vector3 (x, y, z), Quaternion.identity));
                            play.id = name;
                            play.speed = 0;
                            play.lastPing = getPing (teamIdx, playerIdx);
                            colorEscaping (play);
                            resize(play,a);
                            players.Add (play);
                            displayString += play.id.ToString () + "@" + x.ToString () + "," + y.ToString () + "," + z.ToString () + "\n";

                        }
                        
                    }
                }

            }
        }        

    }
    public void updatePlayerLocations (string gameString) {
        playerData = gameStateFromJson (gameString);
        if(playerData == null){
            return;
        }
        float timeSinceStarted = 0.0f;
        
        for (int teamIdx = 0; teamIdx < (int) playerData[teamList].Count; teamIdx++) {
            for (int playerIdx = 0; playerIdx < (int) playerData[teamList][teamIdx][playerList].Count; playerIdx++) {

                x = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][xPos];
                y = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][yPos];
                z = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][zPos];
                a = (float) (double) playerData[teamList][teamIdx][playerList][playerIdx][coordinate][acc];
                name = (string) playerData[teamList][teamIdx][playerList][playerIdx][id];
                
                Vector3 target = new Vector3 (x, y, z);
                target = rotateVectorAroundPivot(target,Vector3.up,new Vector3(0.0f,getBearing(gameString),0.0f));
                for (int i = 0; i < players.Count; i++) {
                    if (players[i].id == getDisplayName (teamIdx, playerIdx)) {
                        Debug.Log ("players.gameModel.position");
                        float xDiff = target.x - players[i].gameModel.position.x;
                        float yDiff = target.y - players[i].gameModel.position.y;
                        float zDiff = target.z - players[i].gameModel.position.z;
                        float distance = Mathf.Sqrt (xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
                        long timeDiff = (long) playerData[teamList][teamIdx][playerList][playerIdx][ping] - players[i].lastPing;
                        if (timeDiff != 0) {
                            players[i].speed = (float) distance / timeDiff;
                            players[i].gameModel.position = Vector3.MoveTowards (players[i].gameModel.position, target, players[i].speed * Time.deltaTime);
                        }
                        else{
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
                        resize(players[i],a);
                    
                    }
                }
                
            }
        }
    }
    public void realignCoordinates(List<PlayerObject> players, string gameString){
        float bearing = getBearing(gameString);
        playerData = gameStateFromJson (gameString);
        for (int teamIdx = 0; teamIdx < (int) playerData[teamList].Count; teamIdx++) {
            for (int playerIdx = 0; playerIdx < (int) playerData[teamList][teamIdx][playerList].Count; playerIdx++) {
            }
        }
        for (int i = 0; i < players.Count; i++) {
        
        }
        rotatePlayersByBearing(players,bearing);

    }
    public Vector3 rotateVectorAroundPivot(Vector3 point, Vector3 pivot, Vector3 angle){
        return Quaternion.Euler(angle) * (point - pivot) + pivot;
    }
    public bool idInPlayerList (List<PlayerObject> players, string id) {
        for (int i = 0; i < players.Count; i++) {
            if (players[i].id == id) {
                return true;
            }
        }
        return false;
    }
    public  JsonData gameStateFromJson (string gameString) {
        return JsonMapper.ToObject (gameString);
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
    public float getBearing(string gameString){
        return (float)(double)playerData[bearing];
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

    public void updatePlayerActiveStates (JsonData data) {
        //check if they are active, if not, change their colors to grey

    }
    public void rotatePlayersByBearing (List<PlayerObject> players,float bearing) {
        Debug.Log("rotating around"+ bearing.ToString());
        for(int i =0;i<players.Count;i++){
            players[i].gameModel.transform.RotateAround(Vector3.zero,Vector3.up,-bearing);
        }
    }

    public void updatePlayerCapturedNumbers () {

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
    public void resize(PlayerObject player, float size){
        if(size>largestObjectSize){
            size = largestObjectSize;
        }
        if(size < smallestObjectsSize){
            size = smallestObjectsSize;
        }
        player.gameModel.transform.localScale = new  Vector3(size,size,size);
    }

    public void animateHasCaptured () {

    }
    public void startGameSequence () {

    }
    public void endGameSequence () {

    }
     

}