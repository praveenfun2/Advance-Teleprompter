package com.praveengupta.capstone_project;


import android.os.Bundle;
import android.preference.PreferenceFragment;


public class Settings_fragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
