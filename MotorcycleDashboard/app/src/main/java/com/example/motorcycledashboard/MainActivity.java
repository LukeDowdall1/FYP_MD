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

    public static ArrayList<LatLng> myObjects = new ArrayList<>();

    Switch switch_metric;
    TextView TV_speed;
    TextView TV_TopSpeed;
    TextView TV_AvgSpeed;
    TextView TV_LeanAngle;
    TextView TV_MaxLeanR;
    TextView TV_MaxLeanL;
    Button button1;
    Button button2;
    float Azimut;


    public float TopSpeed;
    public List<Float> AvgSpeedList = new ArrayList<>();
    public String strAverage;
    public float LeanAngle;
    public int MaxLeanL = 0;
    public int MaxLeanR = 0;
    public int Num = 0;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    private MarkerOptions options = new MarkerOptions();
    private ArrayList<LatLng> latlngs = new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int i = 0;
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
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);

        myObjects.add(new LatLng(53.353946, -6.252994));
        myObjects.add(new LatLng(53.351929, -6.247714));

        switch_metric = findViewById(R.id.switch_metric);

        TV_TopSpeed.setText("Top speed: " + TopSpeed + " km/h");
        TV_LeanAngle.setText("Lean Angle: " + LeanAngle + " km/h");


        if (strAverage == null) {
            TV_AvgSpeed.setText("Avg. speed: " + "0" + " km/h");
        } else {
            TV_AvgSpeed.setText("Avg. speed: " + strAverage + " km/h");
        }

        Log.i("On", "Create: ");


        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            doStuff();
        }

        //this.updateSpeed(null);

        switch_metric.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.this.updateSpeed(null);
            }
        });


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }



    public void onClick(View v) {
        if (v == button1) {
            TopSpeed++;
            TV_LeanAngle.setText("Num: " + TopSpeed);
        }
        if (v == button2) {
            Intent intent = new Intent(MainActivity.this, Map.class);

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
            TV_AvgSpeed.setText("Avg. speed: " + strAverage + " km/h");
        }


        if (this.useMetricUnits()) {

            TV_speed.setText(strCurrentSpeed + " \nkm/h");
        } else {
            TV_speed.setText(strCurrentSpeed + " \nmph");
        }
    }

    private void updateLean() {


        LeanAngle = SensorManager.AXIS_X;

        Formatter fmt3 = new Formatter(new StringBuilder());
        fmt3.format(Locale.US, "%5.0f", LeanAngle);
        String strLeanAngle = fmt3.toString();

        TV_LeanAngle.setText("Avg. speed: " + strLeanAngle + " km/h");

        Log.d("Main", "updateLean: " + LeanAngle);
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


        if (strAverage == null) {
            TV_AvgSpeed.setText("Avg. speed: " + "0" + " km/h");
        } else {
            TV_AvgSpeed.setText("Avg. speed: " + strAverage + " km/h");
        }

    }


    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            Log.i("OrientationTestActivity", String.format("Orientation: %f, %f, %f",
                    mOrientation[0], mOrientation[1], mOrientation[2]));
        }





        Log.d("Main","updateLean: "+mOrientation[0]);

//        int iLeanAngle = (int) (event.values[0] *9);
        int iLeanAngle = (int) (mOrientation[0]);

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

//    @Override
//    protected void onResume() {
//        super.onResume();
//        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        sensorManager.unregisterListener(this);
//    }
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
}