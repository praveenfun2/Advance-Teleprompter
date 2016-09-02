package com.example.praveengupta.capstone_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Px;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.listview)
    ListView listView;
    boolean mirror;
    float screen_ratio;
    Layout layout;
    int time_scroll, stop_listening;
    Resources resources;
    SpeechRecognizer speechRecognizer;
    int text_margin;
    int j;
    Intent intent;
    String temp;
    ArrayList<String> lines;

    boolean scrolling;
    Rect bounds;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        j = 0;
        resources = getResources();
        ButterKnife.bind(this);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        lines = new ArrayList<>();
        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (layout == null) {
                    layout = content.getLayout();
                    final String text = content.getText().toString();
                    Log.d("kk", text);
                    int start = 0;
                    int end;
                    Log.d("kk", layout.getLineCount() + "");
                    for (int i = 0; i < layout.getLineCount(); i++) {
                        end = layout.getLineEnd(i);
                        lines.add(text.substring(start, end));
                        start = end;
                    }
                    temp = lines.get(0);
                    listView.setAdapter(new BaseAdapter() {
                        @Override
                        public int getCount() {
                            return layout.getLineCount();
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
                            if (view == null)
                                view = getLayoutInflater().inflate(R.layout.content_listview_element, viewGroup, false);
                            ((TextView) view).setText(getItem(i));
                            if (i == 0) view.setPadding(0, text_margin, 0, 0);
                            else if (i == getCount() - 1) view.setPadding(0, 0, 0, text_margin);
                            return view;
                        }
                    });
                }
            }
        });
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setSupportActionBar(toolbar);
        screen_ratio = resources.getDisplayMetrics().density;
        bounds = new Rect();
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getSize(p);
        text_margin = (p.y) / 2 - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 50, new DisplayMetrics());
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, Long.parseLong(
                sharedPreferences.getString(resources.getString(R.string.stop_listening_after), "-1")) * 1000);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 500L);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.d("kk", "ready");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("kk", "begin");
            }

            @Override
            public void onRmsChanged(float v) {
                Log.d("kk", "rmschanged");
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                Log.d("kk", "bufferreceived");
            }

            @Override
            public void onEndOfSpeech() {
                Log.d("kk", "endofspeech");
            }

            @Override
            public void onError(int i) {
                Log.d("kk", "error" + i);
            }

            @Override
            public void onResults(Bundle bundle) {

            }

            @Override
            public void onPartialResults(Bundle bundle) {
                ArrayList<String> strings = bundle.getStringArrayList("results_recognition");
                if (strings.get(0).equalsIgnoreCase(temp.trim().replaceAll("[!-/:-@]", ""))) {
                    j++;
                    if (j < lines.size())
                        temp += lines.get(j);
                    listView.smoothScrollToPositionFromTop(j, text_margin);
                    speechRecognizer.startListening(intent);
                }
            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        time_scroll = Integer.parseInt(sharedPreferences.getString(resources.getString(R.string.scroll_interval_auto), "-1"));
        stop_listening = Integer.parseInt(sharedPreferences.getString(resources.getString(R.string.stop_listening_after), "-1"));
        mirror = sharedPreferences.getBoolean(resources.getString(R.string.mirror_state), false);
    }

    public void play(MenuItem item) {
        if (!scrolling) {
            scrolling = true;
            item.setIcon(resources.getDrawable(android.R.drawable.ic_media_pause));
            if (sharedPreferences.getString(resources.getString(R.string.scroll_mode), "auto").equals("auto"))
                auto_scroll();
            else speech_scroll();
        } else {
            scrolling = false;
            item.setIcon(resources.getDrawable(android.R.drawable.ic_media_play));
            if (!sharedPreferences.getString(resources.getString(R.string.scroll_mode), "auto").equals("auto"))
                stop_speech_scroll();
        }
    }

    public void settings(MenuItem item) {
        startActivity(new Intent(this, Settings.class));
    }

    public void auto_scroll() {
        content.getLayout().getLineBounds(1, bounds);
        new AsyncTask<Void, Integer, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                int i = 0;
                while (true) {

                    try {
                        Thread.sleep(Long.parseLong(sharedPreferences.getString(resources.getString(R.string.scroll_interval_auto), "auto")) * 1000);
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
                listView.smoothScrollToPositionFromTop(values[0] + 1, text_margin);
            }
        }.execute();

    }

    public void speech_scroll() {

        speechRecognizer.startListening(intent);
    }

    public void stop_speech_scroll() {
        temp = "";
        speechRecognizer.stopListening();
        j = 0;
    }
}
