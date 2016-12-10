package com.praveengupta.capstone_project.Fragments;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.praveengupta.capstone_project.R;


public class Settings_fragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
