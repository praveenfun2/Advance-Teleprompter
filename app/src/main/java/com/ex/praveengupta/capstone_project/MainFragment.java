package com.ex.praveengupta.capstone_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class MainFragment extends Fragment {

    float screen_ratio;
    SpeechRecognizer speechRecognizer;
    ArrayList<String> lines;
    @BindView(R.id.listview)
    ListView listView;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.progress)
    ProgressBar progressBar;
    int text_margin;
    int j;
    long time_scroll;
    String temp;
    Layout layout;
    SharedPreferences sharedPreferences;
    boolean mirror, scrolling;
    Intent intent;
    Menu menu;
    static final int FROM_ADD_TEXT = 1, FROM_CHOOSE_FILE = 2;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        j = 0;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity());
        lines = new ArrayList<>();

        screen_ratio = getResources().getDisplayMetrics().density;
        Point p = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(p);

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

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        if (savedInstanceState != null) {
            layout = null;
            content.setText(savedInstanceState.getString(getString(R.string.content)));
            content.requestLayout();
        }

        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_add, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.addText_menuitem: {
                                Intent intent1 = new Intent(getContext(), AddText.class);
                                onPauseScroll();
                                startActivityForResult(intent1, FROM_ADD_TEXT);
                                break;
                            }
                            case R.id.chooseFile_menuitem: {
                                onPauseScroll();
                                Intent intent1 = new Intent(getContext(), ChooseFile.class);
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

        listView.setEmptyView(view.findViewById(R.id.empty_view1));
        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (layout == null && !content.getText().toString().isEmpty()) {
                    text_margin = (int) (view.findViewById(R.id.cover).getY() + view.findViewById(R.id.cover).getHeight() / 2);
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
                            if (layout == null) return 0;
                            else return layout.getLineCount();

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
                                v = inflater.inflate(R.layout.content_listview_element, viewGroup, false);
                                textView = (TextView) v.findViewById(R.id.textview);
                            } else {
                                textView = (TextView) v.findViewById(R.id.textview);
                                ((RelativeLayout.LayoutParams) textView.getLayoutParams()).setMargins(0, 0, 0, 0);
                            }
                            textView.setText(getItem(i));
                            if (i == 0)
                                ((RelativeLayout.LayoutParams) textView.getLayoutParams()).setMargins(0, text_margin, 0, 0);
                            else if (i == getCount() - 1)
                                ((RelativeLayout.LayoutParams) textView.getLayoutParams()).setMargins(0, 0, 0, text_margin);

                            if (mirror) textView.setRotationX(180);
                            return v;
                        }
                    });
                } else if (layout == null) {
                    listView.setAdapter(null);
                    lines.clear();
                }
            }

        });

        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle bundle) {
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), "Speak", Toast.LENGTH_SHORT).show();
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
        return view;
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.content), content.getText().toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        time_scroll = Long.parseLong(sharedPreferences.getString(getString(R.string.scroll_interval_auto), "-1")) * 1000;
        mirror = sharedPreferences.getBoolean(getString(R.string.mirror_state), false);
        if (mirror && listView.getAdapter() != null)
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.play_button: {
                if (lines.size() == 0) {
                    Toast.makeText(getActivity(), "Nothing to scroll...", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (!scrolling) {
                    if (sharedPreferences.getString(getString(R.string.scroll_mode), "auto").equals("auto"))
                        Toast.makeText(getActivity(), "Speak", Toast.LENGTH_SHORT).show();
                    else getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
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

        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == RESULT_OK)
            switch (requestCode) {
                case FROM_ADD_TEXT: {
                    layout = null;
                    content.setText(data.getStringExtra(getString(R.string.content)));
                    content.requestLayout();
                    break;
                }
                case FROM_CHOOSE_FILE: {
                    new AsyncTask<String, Void, String>() {
                        @Override
                        protected String doInBackground(String... file) {

                            BufferedReader bufferedReader = null;
                            try {
                                bufferedReader = new BufferedReader(new InputStreamReader(
                                        getContext().getContentResolver().openInputStream(Uri.parse(file[0]))
                                ));
                                String s = "", temp;
                                while ((temp = bufferedReader.readLine()) != null) s += temp;
                                Log.d("kk", s);
                                return s;
                            } catch (IOException e) {
                                Log.d("kk", e.toString());
                            }


                            return null;
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            layout = null;
                            content.setText(s);
                            content.requestLayout();
                        }
                    }.execute(data.getStringExtra("content_add"));
                    break;
                }
            }
    }

    public void auto_scroll() {
        new AsyncTask<Void, Integer, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                int i = 0;
                while (true) {

                    try {
                        Thread.sleep(time_scroll);
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
                    listView.smoothScrollToPositionFromTop(values[0] + 1, text_margin, (int) time_scroll);

            }
        }.execute();

    }

    public void onPauseScroll() {
        scrolling = false;
        menu.findItem(R.id.play_button).setIcon(android.R.drawable.ic_media_play);
        if (!sharedPreferences.getString(getString(R.string.scroll_mode), "auto").equals("auto") && j < lines.size())
            temp = lines.get(j);
    }


}
