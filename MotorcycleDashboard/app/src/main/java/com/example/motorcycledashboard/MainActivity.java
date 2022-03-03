package com.example.motorcycledashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Formatter;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    Switch switch_metric;
    TextView TV_speed;
    TextView TV_TopSpeed;

    public float TopSpeed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        TV_speed = findViewById(R.id.TV_speed);
        TV_TopSpeed = findViewById(R.id.TV_TopSpeed);

        switch_metric = findViewById(R.id.switch_metric);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }
        else {
            doStuff();
        }

        //this.updateSpeed(null);

        switch_metric.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.this.updateSpeed(null);
            }
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(location != null) {
            LocMain myLocation = new LocMain(location, this.useMetricUnits());
            this.updateSpeed(myLocation);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }



    @SuppressLint("MissingPermission")
    private void doStuff() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager!= null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, 0, this);
        }
        Toast.makeText(this, "Connecting GPS...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doStuff();
        }
        else {
            finish();
        }
    }


    private void updateSpeed(LocMain location) {
        float nCurrentSpeed = 0;


        if(location != null){
            location.setMetricUnits(this.useMetricUnits());
            nCurrentSpeed = location.getSpeed();
            nCurrentSpeed = Math.round(nCurrentSpeed);
        }



        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.0f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
//        strCurrentSpeed = strCurrentSpeed.replace(" ", "0");

        Formatter fmt2 = new Formatter(new StringBuilder());
        fmt2.format(Locale.US, "%5.0f", TopSpeed);
        String strTopSpeed = fmt2.toString();

        if (nCurrentSpeed >= TopSpeed) {
            Log.d("Top Speed",strTopSpeed);
            Log.d("Current Speed",strCurrentSpeed);

            strTopSpeed = strCurrentSpeed;
            TopSpeed = nCurrentSpeed;
            TV_TopSpeed.setText("Top speed: " + strTopSpeed + " km/h");
        }

        if (this.useMetricUnits()) {

            TV_speed.setText(strCurrentSpeed + " \nkm/h");
        }
        else {
            TV_speed.setText(strCurrentSpeed + " \nmph");
        }
    }

    private boolean useMetricUnits() {
        return switch_metric.isChecked();
    }
}