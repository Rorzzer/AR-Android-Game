package com.unimelb.comp30022.itproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapsFragment extends Fragment {
    private static String TAG = MapsFragment.class.getName();

    private OnFragmentInteractionListener mListener;
    private BroadcastReceiver currentLocationReciever;
    private Type locationType = new TypeToken<Location>() {}.getType();
    private LatLng PlayerLocation;
    private Gson gson = new Gson();
    private GoogleMap googleMap;
    private Map<String, Marker> markers;
    private MapView mMapView;
    private GameSession currentGameState;

    public MapsFragment() {
        // Required empty public constructor
    }

    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);


        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        currentGameState = ((RunningGameActivity)getActivity()).getCurrentGameState();
        if(currentGameState == null){
            Log.d(TAG, "Null gamestate");
        }

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //hashmap for key location pairs
        markers = new HashMap<String, Marker>();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;


                // For showing a move to my location button
                googleMap.setMyLocationEnabled(true);
                Log.d(TAG, "Current player count ->" + currentGameState.currentPlayerCount());
                for(int i=0; i < currentGameState.currentPlayerCount(); i++ ){
                    Log.d(TAG, "Currently Player ->" + i);

                   // if(currentGameState.allPlayerArrayLists().get(i).getDisplayName() != FirebaseAuth.getInstance().getCurrentUser().getDisplayName()){
                        Marker marker = markers.get(currentGameState.allPlayerArrayLists().get(i).getDisplayName());
                        if(marker != null){
                            marker.remove();
                        }
                        PlayerLocation = new LatLng(currentGameState.allPlayerArrayLists().get(i).getAbsLocation().getLatitude(), currentGameState.allPlayerArrayLists().get(0).getAbsLocation().getLongitude());
                        marker = mMap.addMarker(new MarkerOptions().position(PlayerLocation));
                        markers.put(currentGameState.allPlayerArrayLists().get(i).getDisplayName(), marker);
                    }
                }
  //          }
        });

        return rootView;


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
