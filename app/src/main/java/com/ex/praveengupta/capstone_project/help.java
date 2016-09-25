package com.ex.praveengupta.capstone_project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class help extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        TextView textView= (TextView) findViewById(R.id.help_text);
        textView.setText(getString(R.string.help_text));
        Toolbar toolbar= (Toolbar) findViewById(R.id.help_toolbar);
        setSupportActionBar(toolbar);
    }
}
