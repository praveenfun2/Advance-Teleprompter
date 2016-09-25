package com.ex.praveengupta.capstone_project;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.praveengupta.capstone_project.providers.MyProvider;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChooseFile extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    @BindView(R.id.listview)
    ListView listView;
    @BindView(R.id.choose_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_file);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        listView.setAdapter(new SimpleCursorAdapter(this, R.layout.files_listview_element, null,
                new String[]{MyProvider.Contracts.fileInfo.FILE_NAME, MyProvider.Contracts.fileInfo.FILE_PATH}, new int[]{R.id.file_name, R.id.hidden_text}, 0));
        getSupportLoaderManager().initLoader(1, null, this);
        listView.setEmptyView(findViewById(R.id.empty_view2));
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if(listView.getCheckedItemCount()!=0) actionMode.setTitle(listView.getCheckedItemCount() + "");
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                getMenuInflater().inflate(R.menu.choose_file_context_menu, menu);
                actionMode.setTitle("1");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete: {
                        for (int i = 0; i < listView.getCount(); i++) {
                            View view = listView.getChildAt(i);
                            if (view.isActivated()) {
                                getContentResolver().delete(MyProvider.Contracts.fileInfo.CONTENT_URI,
                                        MyProvider.Contracts.fileInfo.FILE_NAME + " = ?", new String[]{((TextView) view.findViewById(R.id.file_name)).getText().toString()});
                            }
                        }
                        actionMode.finish();
                        break;
                    }
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }

        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_choose, menu);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, MyProvider.Contracts.fileInfo.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((SimpleCursorAdapter) listView.getAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((SimpleCursorAdapter) listView.getAdapter()).swapCursor(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(Intent.createChooser(intent, "MyChooser"), 1);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent=new Intent();
        intent.putExtra("content_add", ((TextView)view.findViewById(R.id.hidden_text)).getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if(uri.getLastPathSegment().matches(".*(txt|doc)$")) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MyProvider.Contracts.fileInfo.FILE_PATH, uri.getPath());
                contentValues.put(MyProvider.Contracts.fileInfo.FILE_NAME, uri.getLastPathSegment());
                getContentResolver().insert(MyProvider.Contracts.fileInfo.CONTENT_URI, contentValues);
            }
            else Toast.makeText(ChooseFile.this, "Please choose a valid text file", Toast.LENGTH_SHORT).show();
        } else Log.d("kk", "result not ok");

    }
}
