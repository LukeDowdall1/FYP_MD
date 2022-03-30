package com.example.motorcycledashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {

    Switch switch_metric;
    TextView TV_speed;
    TextView TV_TopSpeed;
    TextView TV_AvgSpeed;
    TextView TV_LeanAngle;
    Button button1;
    Button button2;



    public float TopSpeed;
    public List<Float> AvgSpeedList = new ArrayList<>();
    public String strAverage;
    public int Num = 0;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TV_speed = findViewById(R.id.TV_speed);
        TV_TopSpeed = findViewById(R.id.TV_TopSpeed);
        TV_AvgSpeed = findViewById(R.id.TV_AvgSpeed);
        TV_LeanAngle = findViewById(R.id.TV_LeanAngle);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);

        switch_metric = findViewById(R.id.switch_metric);

        TV_TopSpeed.setText("Top speed: " + TopSpeed + " km/h");


        if (strAverage == null){
            TV_AvgSpeed.setText("Avg. speed: " + "0" + " km/h");
        }
        else {
            TV_AvgSpeed.setText("Avg. speed: " + strAverage + " km/h");
        }

        Log.i("On", "Create: ");


        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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



    public void onClick(View v) {
        if (v == button1) {
            TopSpeed++;
            TV_LeanAngle.setText("Num: " + TopSpeed);
        }
        if (v == button2) {
//            Intent intent = new Intent(MainActivity.this, Map.class);
//            startActivity(intent);
//            setContentView(R.layout.mainmap);
        }
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



        if (nCurrentSpeed > TopSpeed) {
            strTopSpeed = strCurrentSpeed;
            TopSpeed = nCurrentSpeed;
            TV_TopSpeed.setText("Top speed: " + strTopSpeed + " km/h");
        }

        if (nCurrentSpeed > 0) {
            AvgSpeedList.add(nCurrentSpeed);
            Double average = AvgSpeedList.stream().mapToDouble(val -> val).average().orElse(0.0);
            Formatter fmt3 = new Formatter(new StringBuilder());
            fmt3.format(Locale.US, "%5.0f", average);
            strAverage = fmt3.toString();
            TV_AvgSpeed.setText("Avg. speed: " + strAverage + " km/h");
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat("float_value", TopSpeed);
        outState.putString("string_value", strAverage);
        outState.putInt("int_value", Num);
        Log.i("Save", "Speed: " + TopSpeed);


    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        TopSpeed = savedInstanceState.getFloat("float_value");
        strAverage = savedInstanceState.getString("string_value");
        Num = savedInstanceState.getInt("int_value");
        Log.i("Save", "Avg: " + strAverage);

        TV_TopSpeed.setText("Top speed: " + TopSpeed + " km/h");


        if (strAverage == null){
            TV_AvgSpeed.setText("Avg. speed: " + "0" + " km/h");
        }
        else {
            TV_AvgSpeed.setText("Avg. speed: " + strAverage + " km/h");
        }

    }
}