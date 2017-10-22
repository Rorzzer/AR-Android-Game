# COMP30022 - House Targaryen's IT project
Code Repository for the It Project app by group: House Targaryen

This app was developed by House Targaryen to essentially be an enhanced version of the game knwon to many as Tag. The game uses AR (Augmented Reality) and realtime location updates to enable players to find each other, setup a game and hunt down each other once the game has begun.

The repository contains two main directories that hold the following Content::
1. ITproject: android studio project comprising of the App folders
2. UnityFolder: unity C# project that parses data from the App and draws assets using vuforia plugin and was exported back to the android app.

This App is comprised of the following sections:
1. The location of various classes, activities, services and fragments
2. The unity Plugin which handles the state of the AR (Augmented Reality)
3. The Layout XML files, corresponding to various activities
4. The GoogleMaps API key files
5. Testcases for game Events.

# Installation and Usage:
1. Download and import into android studio
2. Run gradle build and install onto devices as required
3. Go to Google maps API and generate a key for your device(https://developers.google.com/maps/documentation/android-api/)
4. Insert the key in the AndroidManifest in the app folder under the <meta-data android:name="com.google.android.geo.API_KEY"> tag as well as in the app\src\release\res\values\google_maps+api.xml and remove the old keys
5. Create or login to a user profile.
6. Create or Join a Lobby
7. Launch the game
8. Navigate to test directory in src app directory and run gametests
9. Open UnityFolders>assets>scripts> using unity and open the serviceListenerScript.cs to view how the AR elements are drawn

