package com.online.online_mistry;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */

public class HomeFragment extends Fragment implements OnMapReadyCallback, LocationListener {
    private GoogleMap mMap;
    private View HomeView;
    TextView path;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private TextView CheckVerified;
    private DatabaseReference verRef;
    private LocationManager locationManager;
    MyLocationListener listener;
    private Boolean mLocationPermissionGranted = false;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        HomeView = inflater.inflate(R.layout.fragment_home, container, false);
        //CheckVerified=HomeView.findViewById(R.id.textVerified);
        mAuth = FirebaseAuth.getInstance();
        path=HomeView.findViewById( R.id.path );

        // checkVerification();

        requestPermission();
        checkPermission();
        listener=new MyLocationListener( getActivity() );


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission( getActivity(), Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                //do your work
                //another way using fused locsation
                //  /* client=LocationServices.getFusedLocationProviderClient( this );
                //  final Task mylocation=client.getLastLocation();
                // mylocation.addOnCompleteListener( new OnCompleteListener() {
                //     @Override
                //   public void onComplete(@NonNull Task task)
                //  {
                //      if(task.isSuccessful())
                //      {
                //          Location location=(Location)task.getResult();

                //using this location object you can navigate your camera on current location
                //        }

                //    }
                //  } );
                //    */
                mLocationPermissionGranted=true;
                getAds();
                listener=new MyLocationListener( getActivity() );
            }
            else {
                ActivityCompat.requestPermissions( getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1 );
            }
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        return HomeView;
    }

    private void getAds()
    {
        String s=listener.getAddress();
        if(!s.isEmpty())
        {   path.setVisibility( View.VISIBLE );
            path.setText( s );

        }
        else{
            path.setVisibility(View.INVISIBLE);
        }


    }

    private void checkVerification() {

        try {
            verRef = FirebaseDatabase.getInstance().getReference();
            currentUserID = mAuth.getCurrentUser().getUid();

            verRef.child("Shops").child(currentUserID)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // if(!dataSnapshot.hasChild("Document")){
                            CheckVerified.setVisibility(View.VISIBLE);

                            //   }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

        } catch (Exception e) {
            Toast.makeText(getContext(), "Internet slow !!!", Toast.LENGTH_SHORT).show();


        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission( getActivity(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
      /*  Criteria criteria = new Criteria();
        String s = locationManager.getBestProvider(criteria, true);

        Location location = locationManager.getLastKnownLocation(s);*/

        locationManager = (LocationManager)  getContext().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
        Log.d("Best ",bestProvider);

        //You can still do this if you like, you might get lucky:
        Location location = locationManager.getLastKnownLocation(bestProvider);
         mMap.setMyLocationEnabled( true );
         mMap.setTrafficEnabled(true);


        //getCurrentLocation();



          // mMap.clear();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker( new MarkerOptions()
                    .position(latLng)
                    .title( "My Location" )
                  .icon( BitmapDescriptorFactory.fromResource( R.drawable.mymarker) ));
            //.icon( BitmapDescriptorFactory.defaultMarker() ) );
            mMap.moveCamera( CameraUpdateFactory.newLatLng( latLng ) );
            mMap.animateCamera( CameraUpdateFactory.zoomTo( 17) );

            /*mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ltt,11)*/


     /*   LatLng Mathura = new LatLng(27.606079, 77.593350);
        mMap.addMarker(new MarkerOptions().position(Mathura).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Mathura));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));*/

    }



    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(getContext(), "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
