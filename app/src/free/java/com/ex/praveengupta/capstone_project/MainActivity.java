package com.ex.praveengupta.capstone_project;

import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    MainFragment mainFragment;

    @BindView(R.id.drawer_list)
    ListView drawer_list;
    @BindView(R.id.adView)
    AdView mAdView;
    @BindView(R.id.drawer)
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mainFragment = new MainFragment();

        if (getIntent().getStringExtra("content_add") != null)
            new AsyncTask<File, Void, String>() {
                @Override
                protected String doInBackground(File... file) {

                    BufferedReader bufferedReader = null;
                    try {
                        bufferedReader = new BufferedReader(new FileReader(file[0]));
                        String s = "", temp;
                        while ((temp = bufferedReader.readLine()) != null) s += temp;
                        return s;
                    } catch (IOException e) {
                        Log.d("kk", e.toString());
                    }


                    return null;
                }

                @Override
                protected void onPostExecute(String s) {

                    mainFragment.layout = null;
                    mainFragment.content.setText(s);
                    mainFragment.content.requestLayout();
                }
            }.execute(new File(getIntent().getStringExtra("content_add")));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.d1, R.string.d2) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                supportInvalidateOptionsMenu();

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                supportInvalidateOptionsMenu();

            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if(slideOffset>0) mAdView.setVisibility(View.INVISIBLE);
                else mAdView.setVisibility(View.VISIBLE);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawer_list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view == null)
                    view = getLayoutInflater().inflate(R.layout.drawer_list_entries, viewGroup, false);
                TextView textview = (TextView) view.findViewById(R.id.d_text);
                switch (i) {
                    case 0: {
                        textview.setText(getString(R.string.setting));
                        textview.setContentDescription(getString(R.string.setting));
                        break;
                    }
                    case 1: {
                        textview.setText(getString(R.string.help));
                        textview.setContentDescription(getString(R.string.help));
                        break;
                    }
                }
                return view;
            }
        });
        drawer_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0: {
                        startActivity(new Intent(MainActivity.this, Settings.class));
                        break;
                    }
                    case 1: {
                        startActivity(new Intent(MainActivity.this, help.class));
                        break;
                    }

                }

                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        getSupportFragmentManager().beginTransaction().replace(R.id.relative, mainFragment).commit();

        MobileAds.initialize(getApplicationContext(), getString(R.string.ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item);
    }

}
