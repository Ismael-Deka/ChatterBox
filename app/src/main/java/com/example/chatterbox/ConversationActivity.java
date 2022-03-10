package com.example.chatterbox;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatterbox.adapters.ConversationAdapter;
import com.example.chatterbox.firebases.FirebaseAuthUtils;
import com.example.chatterbox.firebases.FirebaseDatabaseUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ConversationActivity extends AppCompatActivity {

    FloatingActionButton mRatingFab;
    CardView mRatingView;
    TextView mCancelRatingView;
    RatingBar mRatingBarView;
    Button mSubmitRatingButton;
    EditText mMessageBox;
    ImageView mSubmitButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity);

        Conversation selectedConvo = (Conversation) getIntent().getParcelableExtra("selected_convo");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(selectedConvo.getReceiverUserName());

        RecyclerView messageList = findViewById(R.id.message_list);
        mRatingFab = findViewById(R.id.rating_fab);
        mRatingView = findViewById(R.id.rating_view);
        mCancelRatingView =findViewById(R.id.cancel_rating);
        mRatingBarView = findViewById(R.id.rating_bar);
        mSubmitRatingButton = findViewById(R.id.sumbit_rating);
        mMessageBox = findViewById(R.id.message_box);
        mSubmitButton = findViewById(R.id.submit_message);

        mRatingBarView.setRating(selectedConvo.getRating());

        ConversationAdapter adapter = new ConversationAdapter(selectedConvo);

        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());



        messageList.setAdapter(adapter);
        messageList.setLayoutManager(manager);
        messageList.scrollToPosition(selectedConvo.getCount()-1);
        manager.setStackFromEnd(true);
        mRatingFab.setOnClickListener(r -> {
            mRatingView.setVisibility(View.VISIBLE);
        });

        mCancelRatingView.setOnClickListener(c ->{
            mRatingView.setVisibility(View.GONE);
        });

        mSubmitRatingButton.setOnClickListener(view -> {
            mRatingView.setVisibility(View.GONE);
            selectedConvo.setRating((float) mRatingBarView.getRating());
            FirebaseDatabaseUtils.writeRating(FirebaseAuthUtils.getUserId(),selectedConvo,adapter);
            Toast.makeText(getBaseContext(), "Rating Updated",
                    Toast.LENGTH_SHORT).show();
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedConvo.addNewMessage(mMessageBox.getText().toString(),Conversation.SENT, System.currentTimeMillis() / 1000L);
                FirebaseDatabaseUtils.writeMessage(FirebaseAuthUtils.getUserId(),selectedConvo,adapter);
                messageList.scrollToPosition(adapter.getItemCount()-1);
                mMessageBox.getText().clear();
            }
        });


    }
}
