package com.example.chatterbox;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.chatterbox.adapters.ConversationListAdapter;
import com.example.chatterbox.callbacks.ConversationCallback;
import com.example.chatterbox.firebases.FirebaseAuthUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class ConversationListActivity extends AppCompatActivity implements ConversationCallback {
    FloatingActionButton mFab;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_list_activity);


        Toast.makeText(getBaseContext(), "Welcome Back "+FirebaseAuthUtils.getUserProfile().getDisplayName(),
                Toast.LENGTH_SHORT).show();

        RecyclerView conversationList = findViewById(R.id.convo_list);
        conversationList.setAdapter(new ConversationListAdapter(this));
        conversationList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        mFab = findViewById(R.id.fab_new_convo) ;

        mFab.setOnClickListener(t -> startActivity(new Intent(ConversationListActivity.this, SearchUserActivity.class)));

    }


    @Override
    public void startConversationActivity(Conversation convo) {
        Intent i = new Intent(ConversationListActivity.this, ConversationActivity.class);
        i.putExtra("selected_convo",convo);
        startActivity(i);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        openLogOutDialog();
        return true;
    }

    private void openLogOutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        builder.setMessage("Are you sure you want to log out?")
                .setTitle("Log-out?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                FirebaseAuthUtils.signOut(getBaseContext());
                startActivity(new Intent(ConversationListActivity.this, MainActivity.class));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onBackPressed() {

        openLogOutDialog();

    }
}
