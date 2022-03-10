package com.example.chatterbox;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatterbox.callbacks.DatabaseReadCallback;
import com.example.chatterbox.firebases.FirebaseAuthUtils;
import com.example.chatterbox.firebases.FirebaseDatabaseUtils;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Map;

public class SearchUserActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, DatabaseReadCallback {

    private static final String TAG = "SearchUserActivity";
    private SearchView mSearchView;
    private MenuItem mSearchMenuItem;
    private ListView mUserList;
    private ArrayList<String> mUserIds;
    private ArrayList<String> mUsernames;
    private ArrayAdapter<String> mUsernameAdapter;

    private ArrayList<ArrayList<String>> mSelectedUserMessages;
    private ArrayList<Long> mSelectedUserUnixTimes;
    private float mSelectedUserRating;

    private int mUserIndex = -1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_user_activity);

        FirebaseDatabaseUtils.readFromRealTimeDatabase(this);

        mUserList = findViewById(R.id.user_list);



        mUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mUserIndex = i;
                getSelectedUserConversation();

            }
        });
    }

    private void getSelectedUserConversation(){
        FirebaseDatabaseUtils.readFromRealTimeDatabase(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu_options, menu);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle("Start a Conversation");

        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        mSearchMenuItem = menu.findItem(R.id.search);


        mSearchView = (SearchView) mSearchMenuItem.getActionView();
        mSearchView.setQueryHint("Search...");
        mSearchView.setSubmitButtonEnabled(false);


        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mSearchView.setQuery("", false);
                return true;
            }
        });

        mSearchView.setSearchableInfo(searchManager.
                getSearchableInfo(getComponentName()));
        mSearchView.setOnQueryTextListener(this);


        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mUsernameAdapter.getFilter().filter(s);
        mUsernameAdapter.notifyDataSetChanged();
        return false;
    }


    @Override
    public void parseDatabaseInfo(DataSnapshot dataSnapshot) {
        if(mUserIndex == -1) {
            Map<String, String> userList = FirebaseDatabaseUtils.readAllUsers(dataSnapshot);

            Log.e(TAG, userList.values().toString());
            mUsernames = new ArrayList<>(userList.values());
            mUserIds = new ArrayList<>(userList.keySet());

            if(mUsernames.contains(FirebaseAuthUtils.getUserProfile().getDisplayName()))
                mUsernames.remove(FirebaseAuthUtils.getUserProfile().getDisplayName());

            try {
                mUsernameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, mUsernames);
                mUserList.setAdapter(mUsernameAdapter);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());

            }
        }else {
            mSelectedUserRating = FirebaseDatabaseUtils.readRating(dataSnapshot, FirebaseAuthUtils.getUserId(),mUserIds.get(mUserIndex));
            mSelectedUserUnixTimes = FirebaseDatabaseUtils.readUnixTimes(dataSnapshot,FirebaseAuthUtils.getUserId(),mUserIds.get(mUserIndex));
            mSelectedUserMessages = FirebaseDatabaseUtils.readMessages(dataSnapshot,FirebaseAuthUtils.getUserId(),mUserIds.get(mUserIndex),mSelectedUserUnixTimes);
            Conversation newConvo = new Conversation(mUserIds.get(mUserIndex),mUsernames.get(mUserIndex),mSelectedUserMessages,mSelectedUserUnixTimes,mSelectedUserRating);
            Intent intent = new Intent(SearchUserActivity.this, ConversationActivity.class);
            intent.putExtra("selected_convo",newConvo);
            Log.e(TAG, mUsernames.get(mUserIndex));
            startActivity(intent);
        }

    }
}
