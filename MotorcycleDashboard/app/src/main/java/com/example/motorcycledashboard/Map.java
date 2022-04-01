package com.example.motorcycledashboard;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActivityChooserView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "Map";

    private MarkerOptions options = new MarkerOptions();
    private ArrayList<LatLng> latlngs = new ArrayList<>();

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private boolean mLocPermissionGranted = false;
    private static final int Loc_Permissions_code = 1234;
    private GoogleMap mMap;

    private static final float DEF_ZOOM = 15f;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmap);

        getLocPermissions();

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Toast.makeText(this, "Map loaded", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (mLocPermissionGranted) {
            GetDeviceLoc();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

//                Marker m1 = mMap.addMarker(new MarkerOptions()
//                .position(new LatLng(53.353946, -6.252994))
//                .anchor(0.5f, 0.5f)
//                .title("Title1")
//                .snippet("Snippet1")
//        );
//        m1.setTag(0);


        for (LatLng point : MainActivity.myObjects) {
            options.position(point);
            options.title("someTitle");
            options.snippet("someDesc");
            mMap.addMarker(options);
        }

        Log.i(TAG, "onMapReady: " + MainActivity.myObjects.get(1) + "....");


    }




    private void GetDeviceLoc() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if(mLocPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: location found");
                            Location currLoc = (Location) task.getResult();


                            moveCam(new LatLng(currLoc.getLatitude(), currLoc.getLongitude()), DEF_ZOOM);
                        }
                        else {
                            Log.d(TAG, "onComplete: location not found");
                            Toast.makeText(Map.this, "Location not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, "GetDeviceLoc: SecurityException:" + e.getMessage());
        }
    }

    private void moveCam(LatLng latlng, float zoom){
        Log.d(TAG, "moveCam: moving the camera Lat:" +latlng.latitude +"Long:"+ latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
    }

    private void InitMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(Map.this);


    }

    private void getLocPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocPermissionGranted = true;
                InitMap();
            }
            else {
                ActivityCompat.requestPermissions(this, permissions, Loc_Permissions_code);
            }
        }
        else {
            ActivityCompat.requestPermissions(this, permissions, Loc_Permissions_code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocPermissionGranted = false;

        switch (requestCode) {
            case Loc_Permissions_code:{
                if(grantResults.length > 0){
                    for(int i=0; i<grantResults.length; i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocPermissionGranted = false;
                            return;
                        }
                    }
                    mLocPermissionGranted = true;
                    InitMap();

                }
            }
        }
    }


}
