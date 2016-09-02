package com.example.praveengupta.capstone_project;

import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        Toolbar toolbar= (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
    }

}
