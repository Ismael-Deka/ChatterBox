package com.example.chatterbox.callbacks;

import com.google.firebase.database.DataSnapshot;

public interface DatabaseReadCallback {
    void parseDatabaseInfo(DataSnapshot dataSnapshot);


}
