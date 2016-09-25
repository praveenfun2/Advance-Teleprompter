package com.ex.praveengupta.capstone_project;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.ex.praveengupta.capstone_project.providers.MyProvider;
import com.ex.praveengupta.capstone_project.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Praveen Gupta on 9/3/2016.
 */
public class MyAsync extends AsyncTask<File, Void, String> {
    boolean read = true;
    String write_what;
    Activity activity;

    public MyAsync(Activity activity, boolean read, String write_what) {
        this.read = read;
        this.activity = activity;
        this.write_what = write_what;
    }

    @Override
    protected String doInBackground(File... file) {
        if (read) {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(file[0]));
                String s = "", temp;
                while ((temp = bufferedReader.readLine()) != null) s += temp;
                return s;
            } catch (IOException e) {
                Log.d("kk", e.toString());
            }

        } else {
            BufferedWriter b = null;
            try {
                b = new BufferedWriter(new FileWriter(file[0]));
                b.write(write_what);
                b.close();
                Log.d("kk", file[0].getCanonicalPath());
                ContentValues contentValues = new ContentValues();
                contentValues.put(MyProvider.Contracts.fileInfo.FILE_NAME, file[0].getName());
                contentValues.put(MyProvider.Contracts.fileInfo.FILE_PATH, file[0].getCanonicalPath());
                activity.getContentResolver().insert(MyProvider.Contracts.fileInfo.CONTENT_URI, contentValues);
                return write_what;

            } catch (IOException e) {
                Log.d("kk", e.toString());
            }

        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if(read) {

            ((MainActivity)activity).layout = null;
            ((MainActivity)activity).content.setText(s);
            ((MainActivity)activity).content.requestLayout();
        }
        else{
                Intent intent = new Intent();
                intent.putExtra(activity.getString(R.string.content), write_what);
                activity.setResult(activity.RESULT_OK, intent);
                activity.finish();

        }
    }
}
