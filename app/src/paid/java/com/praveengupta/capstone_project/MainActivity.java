package com.praveengupta.capstone_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_list)
    ListView drawer_list;
    @BindView(R.id.drawer)
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    SpeechRecognizer speechRecognizer;
    ArrayList<String> lines;
    @BindView(R.id.listview)
    ListView listView;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.progress)
    ProgressBar progressBar;
    int text_margin;
    int j, a;
    int scroll_speed;
    SharedPreferences sharedPreferences;
    boolean mirror, scrolling, auto;
    Intent intent;
    Menu menu;
    int textViewWidth;
    static final int FROM_ADD_TEXT = 1, FROM_CHOOSE_FILE = 2;
    static final int RECORD_AUDIO_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        j = 0;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        lines = new ArrayList<>();

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

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
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
        });

        listView.setEmptyView(findViewById(R.id.empty_view1));
        listView.setAdapter(new BaseAdapter() {

            int margin = (int) getResources().getDimension(R.dimen.activity_vertical_margin);

            @Override
            public int getCount() {
                return lines.size();
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
            public View getView(int i, View v, ViewGroup viewGroup) {
                TextView textView;
                if (v == null) {
                    v = getLayoutInflater().inflate(R.layout.content_listview_element, viewGroup, false);
                    textView = (TextView) v.findViewById(R.id.textview);
                } else {
                    textView = (TextView) v.findViewById(R.id.textview);
                    textView.setPadding(margin, 0, margin, 0);
                }

                textView.setText(getItem(i));
                if (i == 0) textView.setPadding(margin, text_margin, margin, 0);
                else if (i == getCount() - 1)
                    textView.setPadding(margin, 0, margin, text_margin);

                if (mirror) textView.setRotationX(180);
                return v;
            }

        });
        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            int k;
            String string;

            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.d("kk", "onready");
                string=lines.get(j).replaceAll("[!-/:-@\\s]", " ").replaceAll(" +", " ").trim();
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "Speak", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("kk", "onbegin");

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                Log.d("kk", "onbuffer");

            }

            @Override
            public void onEndOfSpeech() {
                Log.d("kk", "onendofspeech");


            }

            @Override
            public void onError(int i) {
                Log.d("kk", i + "");
                if ((i == 7 || i == 6) && j < lines.size()) {
                    speechRecognizer.cancel();
                    speechRecognizer.startListening(intent);
                } else if (i == 2)
                    Toast.makeText(MainActivity.this, "Check your network connection", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle bundle) {
                Log.d("kk", "onresult");
                k=0;
                speechRecognizer.cancel();
                speechRecognizer.startListening(intent);
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                Log.d("kk", "onpartialresult");
                ArrayList<String> strings = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (strings.get(0).length() >= k + string.length() && strings.get(0).substring(k, k + string.length()).equalsIgnoreCase(string)) {
                    k += string.length() + 1;
                    j++;
                    string=lines.get(j).replaceAll("[!-/:-@\\s]", " ").replaceAll(" +", " ").trim();
                    listView.smoothScrollToPositionFromTop(j, text_margin);
                    if (j >= lines.size())
                        onStopScroll();
                }

            }

            @Override
            public void onEvent(int i, Bundle bundle) {
                Log.d("kk", "onevent");
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (getIntent().getStringExtra("content_add") != null)
            new AsyncTask<File, Void, String>() {

                @Override
                protected String doInBackground(File... file) {
                    BufferedReader bufferedReader = null;
                    try {
                        bufferedReader = new BufferedReader(new FileReader(file[0]));
                        String s = "", temp;
                        while ((temp = bufferedReader.readLine()) != null) s += "\n" + temp;
                        return s.trim();
                    } catch (IOException e) {
                        Log.d("kk", e.toString());
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    setList(s);
                }
            }.execute(new File(getIntent().getStringExtra("content_add")));
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.cover).setMinimumHeight(3 * findViewById(R.id.content).getHeight());
        textViewWidth = findViewById(R.id.content).getMeasuredWidth();
        text_margin = (int) (findViewById(R.id.cover).getY() + (findViewById(R.id.cover).getHeight() - findViewById(R.id.content).getHeight()) / 2);
        scroll_speed = Integer.parseInt(sharedPreferences.getString(getString(R.string.scroll_speed_auto), "-1"));
        if (mirror != sharedPreferences.getBoolean(getString(R.string.mirror_state), false) && listView.getAdapter() != null) {
            mirror = sharedPreferences.getBoolean(getString(R.string.mirror_state), false);
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        }
        a = scroll_speed;
        auto = sharedPreferences.getString(getString(R.string.scroll_mode), "auto").equals("auto");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            switch (requestCode) {
                case FROM_ADD_TEXT: {
                    content.setText(data.getStringExtra(getString(R.string.content)));
                    break;
                }
                case FROM_CHOOSE_FILE: {
                    new AsyncTask<String, Void, String>() {
                        @Override
                        protected String doInBackground(String... file) {

                            BufferedReader bufferedReader = null;
                            try {
                                bufferedReader = new BufferedReader(new FileReader(new File(file[0])));
                                String s = "", temp;
                                while ((temp = bufferedReader.readLine()) != null) s += "\n" + temp;
                                return s.trim();
                            } catch (IOException e) {
                                Log.d("kk", e.toString());
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            setList(s);
                        }
                    }.execute(data.getStringExtra("content_add"));
                    break;
                }
            }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RECORD_AUDIO_PERMISSION_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    progressBar.setVisibility(View.VISIBLE);
                    speechRecognizer.startListening(intent);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.play_button: {
                if (lines.size() == 0) {
                    Toast.makeText(this, "Nothing to scroll...", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (!scrolling) {
                    scrolling = true;
                    menu.findItem(R.id.stop_button).setVisible(true);
                    item.setIcon(android.R.drawable.ic_media_pause);
                    if (auto) {
                        Toast.makeText(this, "Speak", Toast.LENGTH_SHORT).show();
                        auto_scroll();
                    } else if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED)
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO},
                                RECORD_AUDIO_PERMISSION_CODE);
                    else {
                        progressBar.setVisibility(View.VISIBLE);
                        while (lines.get(j).equals("")) {
                            j++;
                            listView.smoothScrollToPositionFromTop(j, text_margin);
                            if (j == lines.size() - 1) {
                                onStopScroll();
                                break;
                            }
                        }
                        speechRecognizer.startListening(intent);
                    }

                } else {
                    onPauseScroll();
                }
                return true;
            }

            case R.id.stop_button: {
                onStopScroll();
                progressBar.setVisibility(View.GONE);
                return true;
            }

        }
        return drawerToggle.onOptionsItemSelected(item);
    }

    public void auto_scroll() {
        new AsyncTask<Void, Integer, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                while (true) {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (scrolling) {
                        publishProgress(a);
                        a += scroll_speed;
                    } else break;
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                if (values[0] >= lines.size() - 1 + scroll_speed) onStopScroll();
                else if (values[0] > lines.size() - 1)
                    listView.smoothScrollToPositionFromTop(values[0], text_margin, 1000 * (-values[0] + scroll_speed + lines.size() - 1) / scroll_speed);
                else
                    listView.smoothScrollToPositionFromTop(values[0], text_margin, 1000);

            }
        }.execute();

    }

    public void onPauseScroll() {
        scrolling = false;
        if (!auto)
            speechRecognizer.cancel();
        menu.findItem(R.id.play_button).setIcon(android.R.drawable.ic_media_play);

    }

    public void onStopScroll() {
        scrolling = false;
        menu.findItem(R.id.stop_button).setVisible(false);
        menu.findItem(R.id.play_button).setIcon(android.R.drawable.ic_media_play);
        if (!auto) speechRecognizer.cancel();
        j = 0;
        a = scroll_speed;
    }

    public void setList(String string) {
        int indexofwhitespace_pre = -1, indexofwhitespace = 0, startindex = 0;
        while (startindex < string.length()) {
            indexofwhitespace = indexOf(string, startindex);
            while (textViewWidth >= content.getPaint().measureText(string, startindex, indexofwhitespace)) {
                indexofwhitespace_pre = indexofwhitespace;
                if (indexofwhitespace == string.length() || (string.charAt(indexofwhitespace) == '\n' && string.charAt(indexofwhitespace + 1) != '\n'))
                    break;
                indexofwhitespace = indexOf(string, indexofwhitespace + 1);
            }

            if (indexofwhitespace_pre < startindex)
                indexofwhitespace_pre = indexofwhitespace;
            lines.add(string.substring(startindex, indexofwhitespace_pre));
            Log.d("kk", lines.get(lines.size() - 1));
            startindex = indexofwhitespace_pre + 1;
        }
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

    }

    public int indexOf(String string, int start) {
        while (start < string.length()) {
            if (Character.isWhitespace(string.charAt(start)))
                return start;
            start++;
        }
        return string.length();
    }

}
