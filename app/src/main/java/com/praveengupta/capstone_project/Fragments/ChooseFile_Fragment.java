package com.praveengupta.capstone_project.Fragments;


import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.praveengupta.capstone_project.R;
import com.praveengupta.capstone_project.providers.MyProvider;

import static android.app.Activity.RESULT_OK;

public class ChooseFile_Fragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_choose_file_, container, false);
        listView= (ListView) view.findViewById(R.id.listview);
        setHasOptionsMenu(true);
        listView.setAdapter(new SimpleCursorAdapter(getActivity(), R.layout.files_listview_element, null,
                new String[]{MyProvider.Contracts.fileInfo.FILE_NAME, MyProvider.Contracts.fileInfo.FILE_PATH}, new int[]{R.id.file_name, R.id.hidden_text}, 0));
        getActivity().getSupportLoaderManager().initLoader(1, null, this);
        listView.setEmptyView(view.findViewById(R.id.empty_view2));
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if (listView.getCheckedItemCount() != 0)
                    actionMode.setTitle(listView.getCheckedItemCount() + "");
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                getActivity().getMenuInflater().inflate(R.menu.choose_file_context_menu, menu);
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
                                getActivity().getContentResolver().delete(MyProvider.Contracts.fileInfo.CONTENT_URI,
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

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_choose, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), MyProvider.Contracts.fileInfo.CONTENT_URI, null, null, null, null);
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent();
        intent.putExtra("content_add", ((TextView) view.findViewById(R.id.hidden_text)).getText().toString());
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri.getLastPathSegment().matches(".*(txt|doc|docx)$"))
                if(uri.toString().split(":", 2)[0].equalsIgnoreCase("content")){
                    ContentValues contentValues = new ContentValues();
                    String path = Environment.getExternalStorageDirectory() + "/" + DocumentsContract.getDocumentId(uri).split(":", 2)[1];
                    contentValues.put(MyProvider.Contracts.fileInfo.FILE_PATH, path);
                    contentValues.put(MyProvider.Contracts.fileInfo.FILE_NAME, Uri.parse(path).getLastPathSegment());
                    getActivity().getContentResolver().insert(MyProvider.Contracts.fileInfo.CONTENT_URI, contentValues);
                }
                else{
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MyProvider.Contracts.fileInfo.FILE_PATH, uri.getPath());
                    contentValues.put(MyProvider.Contracts.fileInfo.FILE_NAME, uri.getLastPathSegment());
                    getActivity().getContentResolver().insert(MyProvider.Contracts.fileInfo.CONTENT_URI, contentValues);
                }
            else Toast.makeText(getActivity(), "Please choose a valid text file", Toast.LENGTH_SHORT).show();
        } else Log.d("kk", "result not ok");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Build.VERSION.SDK_INT > 22 && ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "My Chooser"), 1);
        }
        return true;
    }

}
