package com.example.chatterbox;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Conversation implements Parcelable {

    final static public int MESSAGE_CONTENTS = 0;
    final static public int MESSAGE_DIRECTION = 1;
    final static public String SENT = "sent";
    final static public String RECEIVED = "received";


    private String mReceiverUserName;
    private String mReceiverUserId;
    private ArrayList<ArrayList<String>> mMessages;
    private ArrayList<Long> mUnixDatesTimes;
    private float mRating = 0;

    public Conversation(String receiverId, String receiverUserName, ArrayList<ArrayList<String>> sentMessages,
                        ArrayList<Long> unixDateTime,float rating){
        mReceiverUserId = receiverId;
        mReceiverUserName = receiverUserName;
        if(sentMessages!=null)
            mMessages = sentMessages;
        else
            mMessages = new ArrayList<ArrayList<String>>(0);
        mRating = rating;
        if (sentMessages !=null)
            mUnixDatesTimes = unixDateTime;
        else
            mUnixDatesTimes = new ArrayList<Long>(0);
    }

    protected Conversation(Parcel in) {
        mMessages = in.readArrayList(ArrayList.class.getClassLoader());
        mUnixDatesTimes = in.readArrayList(ArrayList.class.getClassLoader());
        mReceiverUserId = in.readString();
        mReceiverUserName = in.readString();
        mRating = in.readFloat();

        if(mMessages == null){
            mMessages = new ArrayList<ArrayList<String>>(0);

        }

        if(mUnixDatesTimes == null){
            mUnixDatesTimes = new ArrayList<Long>(0);
        }
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    public String getReceiverUserName() {
        return mReceiverUserName;
    }

    public void setReceiverUserName(String mReceiverUserName) {
        this.mReceiverUserName = mReceiverUserName;
    }

    public void addNewMessage(String newMessage, String messageDirection, long unixTimeStamp){
        ArrayList<String>  messagePackage = new ArrayList<>();
        messagePackage.add(MESSAGE_CONTENTS,newMessage);
        messagePackage.add(MESSAGE_DIRECTION,messageDirection);
        if(mMessages == null)
            mMessages = new ArrayList<ArrayList<String>>();
        mMessages.add(messagePackage);
        if(mUnixDatesTimes == null)
            mUnixDatesTimes = new ArrayList<Long>();
        mUnixDatesTimes.add(unixTimeStamp);
    }

    public void updateMessages(ArrayList<ArrayList<String>>newMessageList){
        mMessages = newMessageList;
    }

    public void updateUnixDateTimes(ArrayList<Long> newUnixTimes){
        mUnixDatesTimes = newUnixTimes;
    }

    public String getMessage(int index) {
        return mMessages.get(index).get(MESSAGE_CONTENTS);
    }

    public String getLatestMessage(){
        return mMessages.get(mMessages.size()-1).get(MESSAGE_CONTENTS);
    }

    public String getMessageDirection(int index) {
        return mMessages.get(index).get(MESSAGE_DIRECTION);
    }

    public Long getLatestUnixDateTime() {

        return Long.valueOf(mUnixDatesTimes.get(mUnixDatesTimes.size()-1)+"");
    }


    public Long getUnixDateTime(int index) {

        return Long.valueOf(mUnixDatesTimes.get(index)+"");
    }

    public void setUnixDateTime(long unixTimeStamp) {
        mUnixDatesTimes.add(unixTimeStamp);
    }

    public int getCount(){
        if(mMessages != null)
            return mMessages.size();
        else
            return 0;
    }

    public float getRating() {
        return mRating;
    }

    public void setRating(float rating) {
        this.mRating = rating;
    }

    public String getReceiverUserId() {
        return mReceiverUserId;
    }

    public void setReceiverUserId(String receiverUserId) {
        mReceiverUserId = receiverUserId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeList(mMessages);
        parcel.writeList(mUnixDatesTimes);
        parcel.writeString(mReceiverUserId);
        parcel.writeString(mReceiverUserName);
        parcel.writeFloat(mRating);
    }
}
