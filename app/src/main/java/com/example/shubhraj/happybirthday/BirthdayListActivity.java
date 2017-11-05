package com.example.shubhraj.happybirthday;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.shubhraj.happybirthday.R;

public class BirthdayListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final int READ_CONTACTS_PERMISSION_REQUEST = 1;
    private static final String DEBUG = "BirthdayListActivity";
    private static final int CONTACTS_LOADER_ID = 10;
    private static final int LOOKUP_KEY_INDEX = 1;
    private static final int CONTACT_ID_INDEX = 0;
    private SimpleCursorAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birthday_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setupCursorAdapter();
        listView = (ListView) findViewById(R.id.list_view_contacts);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        getPermissionToReadUserContacts();
    }

    private void setupCursorAdapter() {
        String[] uiBindFrom = {ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.Contacts.PHOTO_URI};
        int[] uiBindTo = {R.id.tv_name, R.id.contact_image};
        adapter = new SimpleCursorAdapter(this,
                R.layout.contact_list_item, null, uiBindFrom, uiBindTo, 0);

    }

    private void getPermissionToReadUserContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},READ_CONTACTS_PERMISSION_REQUEST);
                return;
            }
            else
            {
                loadingContacts();
            }
        }
        else
        {
            loadingContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case READ_CONTACTS_PERMISSION_REQUEST:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    loadingContacts();
                else
                    Log.d(DEBUG, "Permission denied");
        }
    }

    private void loadingContacts() {
        Log.d(DEBUG,"We have permission to load contacts");
        getSupportLoaderManager().initLoader(CONTACTS_LOADER_ID, new Bundle(), contactsLoader);
    }

    private LoaderManager.LoaderCallbacks<Cursor> contactsLoader = new LoaderManager.LoaderCallbacks<Cursor>()
    {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projectionFields = new String[]{
                    ContactsContract.Contacts._ID
                    ,ContactsContract.Contacts.DISPLAY_NAME
                    ,ContactsContract.Contacts.PHOTO_URI};

            CursorLoader cursorLoader = new CursorLoader(BirthdayListActivity.this
                    ,ContactsContract.Contacts.CONTENT_URI,
                    projectionFields, null, null, null);

            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            adapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter.swapCursor(null);
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Cursor cursor = ((SimpleCursorAdapter) adapterView.getAdapter()).getCursor();
        cursor.moveToPosition(position);
        String contactName = cursor.getString(LOOKUP_KEY_INDEX);
        Uri mContactUri = ContactsContract.Contacts.getLookupUri(cursor.getLong(CONTACT_ID_INDEX),
                contactName);
        String email = getEmail(mContactUri);
        if(!email.equals(""))
        {
            sendEmail(email,contactName);
        }
    }

    private void sendEmail(String email, String contactName) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto",
                email,
                null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.main_email_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.main_email_body, contactName));
        startActivity(Intent.createChooser(emailIntent, getString(R.string.main_email_chooser)));

    }

    private String getEmail(Uri mContactUri) {
        String email = "";
        String id = mContactUri.getLastPathSegment();
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                new String[]{id},
                null);
        int emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
        if(cursor.moveToFirst())
        {
            email = cursor.getString(emailIndex);
        }
        return email;
    }
}
