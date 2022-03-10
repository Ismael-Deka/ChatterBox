package com.example.chatterbox.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatterbox.Conversation;
import com.example.chatterbox.R;
import com.example.chatterbox.callbacks.ConversationCallback;
import com.example.chatterbox.callbacks.DatabaseReadCallback;
import com.example.chatterbox.firebases.FirebaseAuthUtils;
import com.example.chatterbox.firebases.FirebaseDatabaseUtils;
import com.google.firebase.database.DataSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ViewHolder>
                                    implements DatabaseReadCallback {

    private ConversationCallback mCallback;
    private ArrayList<Conversation> mConversationsList = new ArrayList<>(0);

    public ConversationListAdapter(ConversationCallback callback){
        FirebaseDatabaseUtils.readFromRealTimeDatabase(this);
        mCallback = callback;
    }

    @NonNull
    @Override
    public ConversationListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.convo_list_item, parent, false);

        ConversationListAdapter.ViewHolder viewHolder = new ConversationListAdapter.ViewHolder(contactView, mCallback);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationListAdapter.ViewHolder holder, int position) {
        holder.contactName.setText(mConversationsList.get(position).getReceiverUserName());
        holder.dateTime.setText(formatDate(mConversationsList.get(position).getLatestUnixDateTime()));
        holder.messagePreview.setText("Latest: "+ mConversationsList.get(position).getLatestMessage());
        holder.ratingBar.setRating(mConversationsList.get(position).getRating());
    }

    @Override
    public int getItemCount() {
        return mConversationsList.size();
    }



    @Override
    public void parseDatabaseInfo(DataSnapshot dataSnapshot) {

        mConversationsList = new ArrayList<>(0);

        String currentUserId = FirebaseAuthUtils.getUserId();

        Map<String,String> userMap = (Map<String, String>) dataSnapshot.child("users").getValue();
        Map<String, String> receiverIdMap = (Map<String, String>) dataSnapshot.child(currentUserId).child("conversations").getValue();
        ArrayList<String> receiverIdList;
        if(receiverIdMap != null)
            receiverIdList = new ArrayList<>(receiverIdMap.keySet());
        else
            receiverIdList =new ArrayList<>();

        for(int i = 0; i < receiverIdList.size(); i++){

            String receiverId = receiverIdList.get(i);
            String receiverName = userMap.get(receiverId);

            float rating = FirebaseDatabaseUtils.readRating(dataSnapshot,currentUserId,receiverId);

            ArrayList<Long> unixTimeList = FirebaseDatabaseUtils.readUnixTimes(dataSnapshot,currentUserId,receiverId);
            ArrayList<ArrayList<String>> messages = FirebaseDatabaseUtils.readMessages(dataSnapshot,currentUserId,receiverId,unixTimeList);

            //Log.e(TAG,messages.get(0)[Conversation.MESSAGE_CONTENTS]+" "+ messages.get(1)[Conversation.MESSAGE_CONTENTS]);

            mConversationsList.add(new Conversation(receiverId,receiverName,messages,unixTimeList,rating));
        }

        this.notifyDataSetChanged();

    }

    private String formatDate(long unixTimeStamp){
        Date date = new java.util.Date(unixTimeStamp*1000L);

        SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd-yyyy");

        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT-4"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }



    protected class ViewHolder extends RecyclerView.ViewHolder{

        public TextView contactName;
        public TextView messagePreview;
        public TextView dateTime;
        public RatingBar ratingBar;


        public ViewHolder(@NonNull View itemView, ConversationCallback callback) {

            super(itemView);


            contactName = itemView.findViewById(R.id.contact_name);
            messagePreview = itemView.findViewById(R.id.message_preview);
            dateTime = itemView.findViewById(R.id.date_time);
            ratingBar = itemView.findViewById(R.id.rating);

            itemView.setOnClickListener(t ->{
                callback.startConversationActivity(mConversationsList.get(this.getAdapterPosition()));
            });
        }
    }
}
