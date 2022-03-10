package com.example.chatterbox.firebases;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.chatterbox.R;
import com.example.chatterbox.callbacks.GoogleSignInCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class FirebaseAuthUtils {
    private static FirebaseAuth mAuth;
    private static FirebaseUser mUser;
    private static GoogleSignInClient mGoogleSignInClient;
    private static GoogleSignInCallback mCallback;


    public static void assignGoogleSignInCallback(GoogleSignInCallback callback){
        mCallback = callback;
    }

    public static void initializeFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
    }

    public static boolean isUserSignedIn(){
        FirebaseUser user = mAuth.getCurrentUser();

        return user != null;

    }

    public static void signOut(Context context) {
        mAuth.signOut();
        mGoogleSignInClient = getGoogleSignInClient(context);
        mGoogleSignInClient.signOut();

    }
    private static GoogleSignInClient getGoogleSignInClient(Context context){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.web_client_id))
                .requestProfile()
                .requestEmail()
                .build();

        return GoogleSignIn.getClient(context, gso);
    }

    public static Intent googleSignIn(Context context){

        mGoogleSignInClient = getGoogleSignInClient(context);
        return mGoogleSignInClient.getSignInIntent();
    }

    public static void firebaseAuthWithGoogle(String idToken, Context context) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mUser = mAuth.getCurrentUser();
                            mCallback.verifyCredentials();

                        }else{
                            Toast.makeText(context,"Failed: " + task.getException().getLocalizedMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




    public static void createAccount(String name, String email, String password,Context context) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mUser = mAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        mUser.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            if (!FirebaseDatabaseUtils.doesUserExist(FirebaseAuthUtils.getUserId(), FirebaseAuthUtils.getUserProfile().getDisplayName())) {
                                                FirebaseDatabaseUtils.addUser(FirebaseAuthUtils.getUserId(), FirebaseAuthUtils.getUserProfile().getDisplayName());
                                                Toast.makeText(context,"Account Created Successfully",
                                                        Toast.LENGTH_SHORT).show();
                                            }else {
                                                Toast.makeText(context,"Account already exists",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }else{
                                            Toast.makeText(context,"Failed: " + task.getException().getLocalizedMessage(),
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });
                    }else{
                        Toast.makeText(context,"Failed: " + task.getException().getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }

    public static void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mUser = mAuth.getCurrentUser();
                        }
                    }
                });

    }

    public static String getUserId() {

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            return mUser.getUid();
        } else {
            return null;
        }
    }
    public static FirebaseUser getUserProfile() {

       mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            return mUser;
        }else{
            return null;
        }

    }
    public static void logOut() {

        FirebaseAuth.getInstance().signOut();

    }
}
