package com.online.online_mistry;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.LOCATION_SERVICE;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 */

public class HomeFragment extends Fragment implements OnMapReadyCallback, LocationListener {
    private GoogleMap mMap;
    private View HomeView;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private TextView CheckVerified;
    private DatabaseReference verRef;
    double longitude, latitude;
    private LocationManager locationManager;
    MyLocationListener listener;
    FusedLocationProviderClient client;
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

        // checkVerification();

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
            }
            else {
                ActivityCompat.requestPermissions( getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1 );
            }
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        return HomeView;
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
        if (ActivityCompat.checkSelfPermission( getActivity(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //mMap.setMyLocationEnabled( true );
         getCurrentLocation();

        if(listener.getIsGPSTrackingEnabled())
        {
            myLocationmethod();

            /*mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ltt,11)*/
        }
        else
        {
            Toast.makeText( getActivity(), "Please enable GPS And Restart Application", Toast.LENGTH_SHORT ).show();
        }

    }

    public void myLocationmethod()
    {
        this.latitude=listener.getlatitude();
        this.longitude=listener.getlongitude();
        LatLng ltt = new LatLng( latitude, longitude );
        mMap.clear();
        mMap.addMarker( new MarkerOptions()
                .position( ltt )
                .title( "My Location" )
                .icon( BitmapDescriptorFactory.fromResource( R.drawable.mymarker ) ));
        //.icon( BitmapDescriptorFactory.defaultMarker() ) );
        mMap.moveCamera( CameraUpdateFactory.newLatLng( ltt ) );
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 14) );

    }

    private void getCurrentLocation(){

        client= LocationServices.getFusedLocationProviderClient(getActivity());
        try{
            if(mLocationPermissionGranted){
                Task task=client.getLastLocation();
                task.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){

                            Location currentLocation=(Location)task.getResult();
                            Navigatecamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),14,"My Location");

                        }else {

                            Toast.makeText(getActivity(),"unable to get current location",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){

        }

    }

    private void Navigatecamera(LatLng latLng,float zoom,String title){

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        MarkerOptions options = new MarkerOptions().position(latLng).title(title);
        mMap.addMarker(options);


    }
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission( getActivity(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale( getActivity(), Manifest.permission.ACCESS_FINE_LOCATION )) {
                new AlertDialog.Builder( getActivity() )
                        .setTitle( "give permission" )
                        .setMessage( "it is important to give the permission" )
                        .setPositiveButton( "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions( getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1 );
                            }
                        } )
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions( getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1 );
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission( getActivity(), Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {

                        mMap.setMyLocationEnabled( true );

                        //do your work
                    }
                } else {
                    checkLocationPermission();
                    Toast.makeText( getActivity(), "Please provide the permission", Toast.LENGTH_LONG ).show();
                }
                break;
            }
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
