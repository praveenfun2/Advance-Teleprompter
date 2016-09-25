package com.ex.praveengupta.capstone_project;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddText extends AppCompatActivity implements DialogInterface.OnClickListener {

    Intent intent;
    String file_name;
    AlertDialog dialog;
    @BindView(R.id.editText)
    EditText editText;
    @BindView(R.id.addText_toolbar)
    Toolbar toolbar;
    EditText dialog_editext;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_addtext, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);
        ButterKnife.bind(this);
        editText.setHighlightColor(getResources().getColor(R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);
        dialog_editext = new EditText(this);
        intent = new Intent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menuitem: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                dialog = builder.setTitle("Enter the file name").setPositiveButton("Save", this).setView(dialog_editext).create();
                dialog.show();
                return true;
            }
            case R.id.dont_save_menuitem: {
                intent.putExtra(getString(R.string.content), editText.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
                return true;
            }
        }
        return false;
    }

    public void save() {

        File file = new File(getFilesDir(), file_name + ".txt");
        new MyAsync(this, false, editText.getText().toString()).execute(file);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (!isFilenameValid(dialog_editext.getText().toString()))
            Toast.makeText(AddText.this, "please enter a name", Toast.LENGTH_SHORT).show();
        else {
            file_name = String.valueOf(dialog_editext.getText());
            save();
        }

    }

    public boolean isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}