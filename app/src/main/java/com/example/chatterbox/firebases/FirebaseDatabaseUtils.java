package com.example.chatterbox.firebases;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chatterbox.Conversation;
import com.example.chatterbox.callbacks.DatabaseReadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class FirebaseDatabaseUtils {

    private static final String TAG = "FirebaseDatabaseUtils";
    private static FirebaseDatabase mDatabase;
    private static DatabaseReference mRoot;
    private static boolean mDoesUserExist = false;






    public static void initializeDbForReadWrite(DatabaseReadCallback callback) {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        mRoot = mDatabase.getReference();
        mRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(callback != null)
                    callback.parseDatabaseInfo(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }



    public static void writeMessage(String userId, Conversation convo,  DatabaseReadCallback callback){
        if(mRoot == null) {
            initializeDbForReadWrite(callback);
        }
        DatabaseReference messageContents = mRoot.child(userId).child("conversations")
                .child(convo.getReceiverUserId()).child("messages")
                .child(convo.getUnixDateTime(convo.getCount()-1).toString())
                .child("message_contents");
        DatabaseReference messageDirection = mRoot.child(userId).child("conversations")
                .child(convo.getReceiverUserId()).child("messages")
                .child(convo.getUnixDateTime(convo.getCount()-1).toString()).child("message_direction");


        messageContents.setValue(convo.getMessage(convo.getCount()-1));
        messageDirection.setValue(Conversation.SENT);
        sendMessageToReceiver(userId,convo,callback);

    }

    public static void sendMessageToReceiver(String userId, Conversation convo,  DatabaseReadCallback callback){
        if(mRoot == null) {
            initializeDbForReadWrite(callback);
        }
        DatabaseReference messageContents = mRoot.child(convo.getReceiverUserId()).child("conversations")
                .child(userId).child("messages")
                .child(convo.getUnixDateTime(convo.getCount()-1).toString())
                .child("message_contents");
        DatabaseReference messageDirection = mRoot.child(convo.getReceiverUserId()).child("conversations")
                .child(userId).child("messages")
                .child(convo.getUnixDateTime(convo.getCount()-1).toString()).child("message_direction");


        messageContents.setValue(convo.getMessage(convo.getCount()-1));
        messageDirection.setValue(Conversation.RECEIVED);

    }

    public static void addUser(String userId, String userName){
        if(mRoot == null) {
            initializeDbForReadWrite(null);
        }
        DatabaseReference userList = mRoot.child("users").child(userId);

        userList.setValue(userName);
    }


    public static void writeRating(String userId, Conversation convo,  DatabaseReadCallback callback){
        if(mRoot == null) {
            initializeDbForReadWrite(callback);
        }
        DatabaseReference convoRating = mRoot.child(userId).child("conversations")
                .child(convo.getReceiverUserId()).child("rating");

        convoRating.setValue(convo.getRating());
    }



    public static void readFromRealTimeDatabase(DatabaseReadCallback callback) {
        if(mRoot == null) {
            initializeDbForReadWrite(callback);
        }

        mRoot.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        Log.e(TAG,"Successfully Read");
                        DataSnapshot dataSnapshot = task.getResult();
                        callback.parseDatabaseInfo(dataSnapshot);
                    }else {
                        Log.e(TAG,"User Doesn't Exist");
                    }
                }else {
                    Log.e(TAG,"Failed to read");
                }

            }
        });

    }


    public static Map<String,String> readAllUsers(DataSnapshot dataSnapshot){
        if(mRoot == null) {
            initializeDbForReadWrite(null);
        }
        Map<String, String> userList = (Map<String, String>) dataSnapshot.child("users").getValue();

        return userList;

    }

    public static boolean doesUserExist(String userId, String userName){
        if(mRoot == null) {
            initializeDbForReadWrite(null);
        }



        mRoot.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                mDoesUserExist = task.getResult().exists();
            }else {
                Log.e(TAG,"Failed to read");
            }

        });
        return mDoesUserExist;

    }
    public static float readRating(DataSnapshot dataSnapshot, String userId, String receiverId){
        Object ratingObject = dataSnapshot.child(userId).child("conversations")
                .child(receiverId).child("rating").getValue();

        if (ratingObject instanceof Long){
            Long rating = (Long) ratingObject;
            return rating.floatValue();
        } else if(ratingObject != null) {
            Double rating = (Double) ratingObject;
            return rating.floatValue();
        }else{
            return 0.0f;
        }
    }

    public static ArrayList<Long> readUnixTimes(DataSnapshot dataSnapshot, String userId, String receiverId){
        Map<Long,String> unixTimeMap = (Map<Long,String>) dataSnapshot.child(userId).child("conversations")
                .child(receiverId).child("messages").getValue();
        if(unixTimeMap != null) {
            ArrayList<Long> unixTimeList = new ArrayList(unixTimeMap.keySet());
            Collections.sort(unixTimeList);
            return unixTimeList;
        }else{
            return null;
        }
    }

    public static ArrayList<ArrayList<String>> readMessages(DataSnapshot dataSnapshot, String userId, String receiverId, ArrayList<Long> unixTimeList){

        if(unixTimeList != null) {
            ArrayList<ArrayList<String>> messages = new ArrayList<>();
            ArrayList<String> temp = new ArrayList<>();

            for (int j = 0; j < unixTimeList.size(); j++) {

                String messageContents = (String) dataSnapshot.child(userId).child("conversations")
                        .child(receiverId).child("messages").child(unixTimeList.get(j) + "").child("message_contents").getValue();

                Log.e(TAG, dataSnapshot.child(userId).child("conversations")
                        .child(receiverId).child("messages").child(unixTimeList.get(j) + "").child("message_direction") + "");

                String messageDirection = (String) dataSnapshot.child(userId).child("conversations")
                        .child(receiverId).child("messages").child(unixTimeList.get(j) + "").child("message_direction").getValue();

                temp.add(Conversation.MESSAGE_CONTENTS, messageContents);
                temp.add(Conversation.MESSAGE_DIRECTION, messageDirection);

                messages.add(temp);

                temp = new ArrayList<>();
            }
           // Log.e(TAG, messages.get(0).get(Conversation.MESSAGE_CONTENTS) + " " + messages.get(1).get(Conversation.MESSAGE_CONTENTS));
            return messages;
        }else{
            return null;
        }
    }

}

