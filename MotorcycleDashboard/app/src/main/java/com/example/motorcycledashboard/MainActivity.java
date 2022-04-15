package com.example.motorcycledashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
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


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener, SensorEventListener {

    private static final String TAG = "Main";

    public static ArrayList<LatLng> GlPointers = new ArrayList<>();
    public static ArrayList<String> GlDesc = new ArrayList<>();
    public static Boolean ML_Mode;




    Switch switch_metric;
    TextView TV_speed;
    TextView TV_TopSpeed;
    TextView TV_AvgSpeed;
    TextView TV_LeanAngle;
    TextView TV_MaxLeanR;
    TextView TV_MaxLeanL;
    TextView Img_Settings;
    Button button1;
    Button button2;


    public float TopSpeed;
    public List<Float> AvgSpeedList = new ArrayList<>();
    public String strAverage;
    public float LeanAngle;
    public int MaxLeanL = 0;
    public int MaxLeanR = 0;
    public int DistBetweenMarkers = 50;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        findViewById(R.id.MColor) = "#3A3A3A";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TV_speed = findViewById(R.id.TV_speed);
        TV_TopSpeed = findViewById(R.id.TV_TopSpeed);
        TV_AvgSpeed = findViewById(R.id.TV_AvgSpeed);
        TV_LeanAngle = findViewById(R.id.TV_LeanAngle);
        TV_MaxLeanL = findViewById(R.id.TV_MaxLeanL);
        TV_MaxLeanR = findViewById(R.id.TV_MaxLeanR);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        Img_Settings = findViewById(R.id.settings);
        Img_Settings.setOnClickListener(this);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);





        switch_metric = findViewById(R.id.switch_metric);

        TV_TopSpeed.setText("Top speed: " + TopSpeed + " km/h");
        TV_LeanAngle.setText("Lean Angle: " + LeanAngle + " km/h");


        if (strAverage == null) {
            TV_AvgSpeed.setText("Avg speed: " + "0" + " km/h");
        } else {
            TV_AvgSpeed.setText("Avg speed: " + strAverage + " km/h");
        }

        Log.i("On", "Create: ");


        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            doStuff();
        }


        switch_metric.setOnCheckedChangeListener((buttonView, isChecked) -> MainActivity.this.updateSpeed(null));


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);



    }





    public void onClick(View v) {
        if (v == button1) {

            TopSpeed = 0;
            AvgSpeedList.clear();
            MaxLeanL = 0;
            MaxLeanR = 0;
        }
        if (v == button2) {
            Intent intent = new Intent(MainActivity.this, Map.class);

            startActivity(intent);
        }
        if (v == Img_Settings) {
            Intent intent = new Intent(MainActivity.this, settings.class);

            startActivity(intent);
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location != null) {
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
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        Toast.makeText(this, "Connecting GPS...", Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doStuff();
        } else {
            finish();
        }
    }


    public int temp1 = 0;
    private void updateSpeed(LocMain location) {
        float nCurrentSpeed = 0;



        if (location != null) {
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
            Formatter fmt4 = new Formatter(new StringBuilder());
            fmt4.format(Locale.US, "%5.0f", average);
            strAverage = fmt4.toString();
            TV_AvgSpeed.setText("Avg speed: " + strAverage + " km/h");
        }


        if (this.useMetricUnits()) {
            TV_speed.setText(strCurrentSpeed + " \nkm/h");
        }
        else {
            TV_speed.setText(strCurrentSpeed + " \nkm/h");
        }


        if (temp1 == 0) {
            GlPointers.add(new LatLng(location.getLatitude(), location.getLongitude()));
            GlPointers.add(new LatLng(location.getLatitude(), location.getLongitude()));
            GlDesc.add("Speed:, Lean Angle:" + LeanAngle);
            GlDesc.add("Speed:, Lean Angle:" + LeanAngle);
            temp1++;
        }


        Location startP = new Location("locationA");
        startP.setLatitude((GlPointers.get(GlPointers.size() - 1).latitude));
        startP.setLongitude((GlPointers.get(GlPointers.size() - 1).longitude));
        Location endP = new Location("locationB");
        endP.setLatitude(location.getLatitude());
        endP.setLongitude(location.getLongitude());

        if (startP.distanceTo(endP) > DistBetweenMarkers) {
            GlPointers.add(new LatLng(location.getLatitude(), location.getLongitude()));
            GlDesc.add("Speed: " + strCurrentSpeed + ", Lean Angle:" + LeanAngle);
            startP.setLatitude((GlPointers.get(GlPointers.size() - 1).latitude));
            startP.setLongitude((GlPointers.get(GlPointers.size() - 1).longitude));
            endP.setLatitude(location.getLatitude());
            endP.setLongitude(location.getLongitude());
        }
//        Log.i(TAG, "updateSpeed: " +startP.distanceTo(endP));
    }

    private boolean useMetricUnits() {
        return switch_metric.isChecked();
    }


    float[] mGravity;
    float[] mGeomagnetic;
    float azimuth = 0;

    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[1]; // orientation contains: azimut, pitch and roll
            }
        }





//        Log.d("Main","updateLean: "+mOrientation[0]);

//        int iLeanAngle = (int) (event.values[0] *9);
//        int iLeanAngle = (int) (mOrientation[0]*57.2957768);

        int iLeanAngle = (int) (azimuth *57.2957768)+90;

//        Log.d("Main", "updateLean: " +iLeanAngle);

        // *57.2957768

        TV_LeanAngle.setText("Lean Angle: "+iLeanAngle +"°");

        if(iLeanAngle >MaxLeanL)
        {
            MaxLeanL = iLeanAngle;
            TV_MaxLeanL.setText("L: " + iLeanAngle + "°");
        }

        if(iLeanAngle<MaxLeanR)
        {
            MaxLeanR = iLeanAngle;
            TV_MaxLeanR.setText("R: " + iLeanAngle + "°");
        }

    //        Log.d("Main", "updateLean: " +event.values[0]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onResume() {
        super.onResume();
        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat("float_value", TopSpeed);
        outState.putString("string_value", strAverage);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        TopSpeed = savedInstanceState.getFloat("float_value");
        strAverage = savedInstanceState.getString("string_value");

        TV_TopSpeed.setText("Top speed: " + TopSpeed + " km/h");


        if (strAverage == null) {
            TV_AvgSpeed.setText("Avg speed: " + "0" + " km/h");
        } else {
            TV_AvgSpeed.setText("Avg speed: " + strAverage + " km/h");
        }

    }
}