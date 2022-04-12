package com.example.motorcycledashboard;

import android.os.Bundle;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class settings extends AppCompatActivity {

    Switch DLMode;

    protected void onCreate(@Nullable Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

//        DLMode = findViewById(R.id.D_L_Mode);

        if (DLMode.isChecked()) {
            MainActivity.ML_Mode = true;
        }
    }
}
