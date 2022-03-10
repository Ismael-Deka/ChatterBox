package com.example.chatterbox.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.chatterbox.Conversation;
import com.example.chatterbox.R;
import com.example.chatterbox.callbacks.DatabaseReadCallback;
import com.example.chatterbox.firebases.FirebaseAuthUtils;
import com.example.chatterbox.firebases.FirebaseDatabaseUtils;
import com.google.firebase.database.DataSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> implements DatabaseReadCallback {

    private static final String TAG = "ConversationAdapter";
    private Conversation mConvo;

    public ConversationAdapter(Conversation convo){
        mConvo = convo;
        FirebaseDatabaseUtils.initializeDbForReadWrite(this);
        FirebaseDatabaseUtils.readFromRealTimeDatabase(this);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.message_list_item, parent, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {



        final String messageDirection = mConvo.getMessageDirection(index);
        if(messageDirection.equals(Conversation.SENT)){
            holder._MessageReceivedLayout.setVisibility(View.GONE);
            holder._MessageSentLayout.setVisibility(View.VISIBLE);
            holder._MessageSentView.setText(mConvo.getMessage(index));
            holder._MessageTimeSentView.setText(formatDate(mConvo.getUnixDateTime(index)));
        }else {
            holder._MessageSentLayout.setVisibility(View.GONE);
            holder._MessageReceivedLayout.setVisibility(View.VISIBLE);
            holder._MessageReceivedView.setText(mConvo.getMessage(index));
            holder._MessageTimeReceivedView.setText(formatDate(mConvo.getUnixDateTime(index)));
        }




    }


    @Override
    public int getItemCount() {
        return mConvo.getCount();
    }


    public static String formatDate(long unixTimeStamp){
        Date date = new java.util.Date(unixTimeStamp*1000L);

        SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd-yyyy hh:mm a");

        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT-4"));
        return sdf.format(date);
    }

    @Override
    public void parseDatabaseInfo(DataSnapshot dataSnapshot) {
        Log.e(TAG, mConvo.getReceiverUserId());
        float newRating = FirebaseDatabaseUtils.readRating(dataSnapshot,FirebaseAuthUtils.getUserId(),mConvo.getReceiverUserId());
        ArrayList<Long> newUnixTimes = FirebaseDatabaseUtils.readUnixTimes(dataSnapshot,FirebaseAuthUtils.getUserId(),mConvo.getReceiverUserId());
        ArrayList<ArrayList<String>> newMessages = FirebaseDatabaseUtils.readMessages(dataSnapshot,FirebaseAuthUtils.getUserId(),mConvo.getReceiverUserId(),newUnixTimes);

        mConvo.updateMessages(newMessages);
        mConvo.updateUnixDateTimes(newUnixTimes);
        mConvo.setRating(newRating);

        this.notifyDataSetChanged();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder{

        public LinearLayout _MessageReceivedLayout;
        public LinearLayout _MessageSentLayout;
        public TextView _MessageSentView;
        public TextView _MessageReceivedView;
        public TextView _MessageTimeReceivedView;
        public TextView _MessageTimeSentView;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            _MessageReceivedLayout = itemView.findViewById(R.id.message_from_layout);
            _MessageSentLayout = itemView.findViewById(R.id.message_sent_layout);
            _MessageSentView = itemView.findViewById(R.id.message_sent);
            _MessageReceivedView =itemView.findViewById(R.id.message_received);
            _MessageTimeReceivedView = itemView.findViewById(R.id.message_received_time);
            _MessageTimeSentView = itemView.findViewById(R.id.message_sent_time);
        }
    }

}
