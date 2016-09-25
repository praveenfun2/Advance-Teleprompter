package com.ex.praveengupta.capstone_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.progress)
    ProgressBar progressBar;
    @BindView(R.id.listview)
    ListView listView;
    @BindView(R.id.drawer_list)
    ListView drawer_list;
    @BindView(R.id.drawer)
    DrawerLayout drawerLayout;
    boolean mirror;
    float screen_ratio;
    Layout layout;
    SpeechRecognizer speechRecognizer;
    int text_margin, j, time_scroll;
    static final int FROM_ADD_TEXT = 1, FROM_CHOOSE_FILE = 2;
    Menu menu;
    Intent intent;
    String temp;
    ArrayList<String> lines;
    boolean scrolling;
    SharedPreferences sharedPreferences;
    ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        j = 0;
        ButterKnife.bind(this);

        if (getIntent().getStringExtra("content_add") != null) {
            Log.d("kk", "at last");
            new MyAsync(this, true, null).execute(new File(getIntent().getStringExtra("content_add")));
        } else if (savedInstanceState != null) {
            layout = null;
            content.setText(savedInstanceState.getString(getString(R.string.content)));
            content.requestLayout();
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        lines = new ArrayList<>();
        listView.setEmptyView(findViewById(R.id.empty_view1));

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
                    case 1:{
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
                switch(i){
                    case 0:{
                        startActivity(new Intent(MainActivity.this, Settings.class));
                        break;
                    }
                    case 1:{
                        startActivity(new Intent(MainActivity.this, help.class));
                        break;
                    }

                }

                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (layout == null && !content.getText().toString().isEmpty()) {
                    text_margin = (int) (findViewById(R.id.cover).getY() + findViewById(R.id.cover).getHeight() / 2);
                    layout = content.getLayout();
                    final String text = content.getText().toString();
                    lines.clear();
                    int start = 0;
                    int end;
                    for (int k = 0; k < layout.getLineCount(); k++) {
                        end = layout.getLineEnd(k);
                        lines.add(text.substring(start, end));
                        start = end;
                    }
                    temp = lines.get(0);
                    listView.setAdapter(new BaseAdapter() {
                        @Override
                        public int getCount() {
                            if (layout == null) {
                                listView.setVisibility(View.GONE);
                                findViewById(R.id.empty_view1).setVisibility(View.VISIBLE);
                                return 0;
                            } else {
                                return layout.getLineCount();
                            }
                        }

                        @Override
                        public String getItem(int i) {
                            return lines.get(i);
                        }

                        @Override
                        public long getItemId(int i) {
                            return i;
                        }

                        @Override
                        public View getView(int i, View view, ViewGroup viewGroup) {
                            TextView textView;
                            if (view == null) {
                                view = getLayoutInflater().inflate(R.layout.content_listview_element, viewGroup, false);
                                textView = (TextView) view.findViewById(R.id.textview);
                            } else {
                                textView = (TextView) view.findViewById(R.id.textview);
                                ((RelativeLayout.LayoutParams) textView.getLayoutParams()).setMargins(0, 0, 0, 0);
                            }
                            textView.setText(getItem(i));
                            if (i == 0)
                                ((RelativeLayout.LayoutParams) textView.getLayoutParams()).setMargins(0, text_margin, 0, 0);
                            else if (i == getCount() - 1)
                                ((RelativeLayout.LayoutParams) textView.getLayoutParams()).setMargins(0, 0, 0, text_margin);

                            if (mirror) textView.setRotationX(180);
                            return view;
                        }
                    });
                }
            }

        });

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        screen_ratio = getResources().getDisplayMetrics().density;
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getSize(p);

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        /*
        not working since jelly bean0
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, Long.parseLong(
                sharedPreferences.getString(resources.getString(R.string.stop_listening_after), "-1")) * 1000);
        not working since jelly bean
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 500L);  */
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle bundle) {
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "Speak", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {

                if (j < lines.size()) {
                    temp = lines.get(j);
                    speechRecognizer.startListening(intent);
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                ArrayList<String> strings = bundle.getStringArrayList("results_recognition");

                if (strings.get(0).equalsIgnoreCase(temp.trim().replaceAll("[!-/:-@]", ""))) {
                    j++;
                    if (j < lines.size())
                        temp += lines.get(j);
                    if (j == lines.size() - 1) {
                        listView.smoothScrollToPositionFromTop(j, text_margin);
                        onStopScroll();
                    } else listView.smoothScrollToPositionFromTop(j, text_margin);

                }

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });


    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(getString(R.string.content), content.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        time_scroll = Integer.parseInt(sharedPreferences.getString(getString(R.string.scroll_interval_auto), "-1"));
        mirror = sharedPreferences.getBoolean(getString(R.string.mirror_state), false);
        if (mirror) ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    public void auto_scroll() {
        new AsyncTask<Void, Integer, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                int i = 0;
                while (true) {

                    try {
                        Thread.sleep(Long.parseLong(sharedPreferences.getString(getString(R.string.scroll_interval_auto), "auto")) * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (scrolling) {
                        publishProgress(i);
                        i++;
                    } else break;
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                if (values[0] == lines.size() - 1) onStopScroll();
                else
                    listView.smoothScrollToPositionFromTop(values[0] + 1, text_margin);
            }
        }.execute();

    }

    public void onPauseScroll() {
        scrolling = false;
        menu.findItem(R.id.play_button).setIcon(android.R.drawable.ic_media_play);
        if (!sharedPreferences.getString(getString(R.string.scroll_mode), "auto").equals("auto") && j < lines.size())
            temp = lines.get(j);
    }

    public void onStopScroll() {
        scrolling = false;
        menu.findItem(R.id.stop_button).setVisible(false);
        menu.findItem(R.id.play_button).setIcon(android.R.drawable.ic_media_play);
        if (lines.size() > 0) temp = lines.get(0);
        speechRecognizer.stopListening();
        j = 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.play_button: {
                if (lines.size() == 0) {
                    Toast.makeText(MainActivity.this, "Nothing to scroll...", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (!scrolling) {
                    if (sharedPreferences.getString(getString(R.string.scroll_mode), "auto").equals("auto"))
                        Toast.makeText(MainActivity.this, "Speak", Toast.LENGTH_SHORT).show();
                    else findViewById(R.id.progress).setVisibility(View.VISIBLE);
                    scrolling = true;
                    menu.findItem(R.id.stop_button).setVisible(true);
                    item.setIcon(android.R.drawable.ic_media_pause);
                    if (sharedPreferences.getString(getString(R.string.scroll_mode), "auto").equals("auto"))
                        auto_scroll();
                    else speechRecognizer.startListening(intent);
                } else {
                    onPauseScroll();
                }
                return true;
            }

            case R.id.stop_button: {
                onStopScroll();
                return true;
            }
            default:
                drawerToggle.onOptionsItemSelected(item);
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
            switch (requestCode) {
                case FROM_ADD_TEXT: {
                    layout = null;
                    content.setText(data.getStringExtra(getString(R.string.content)));
                    content.requestLayout();
                    break;
                }
                case FROM_CHOOSE_FILE: {
                    new MyAsync(this, true, null).execute(new File(data.getStringExtra("content_add")));
                    break;
                }
            }
    }

    public void add(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_add, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.addText_menuitem: {
                        Intent intent1 = new Intent(MainActivity.this, AddText.class);
                        onPauseScroll();
                        startActivityForResult(intent1, FROM_ADD_TEXT);
                        break;
                    }
                    case R.id.chooseFile_menuitem: {
                        onPauseScroll();
                        Intent intent1 = new Intent(MainActivity.this, ChooseFile.class);
                        startActivityForResult(intent1, FROM_CHOOSE_FILE);
                        menu.findItem(R.id.stop_button).setVisible(false);
                        break;
                    }
                    default:
                        return false;
                }
                return true;
            }
        });
        popupMenu.show();
    }

}
