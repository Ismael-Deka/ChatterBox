package com.example.chatterbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatterbox.callbacks.GoogleSignInCallback;
import com.example.chatterbox.firebases.FirebaseAuthUtils;
import com.example.chatterbox.firebases.FirebaseDatabaseUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements GoogleSignInCallback {

    private static final int RC_SIGN_IN = 9001;
    private static final int RC_CREATE_ACCOUNT = 9002;

    private static final String TAG = "MainActivity";

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuthUtils.initializeFirebaseAuth();
        if(FirebaseAuthUtils.isUserSignedIn()){
            FirebaseDatabaseUtils.addUser(FirebaseAuthUtils.getUserId(),FirebaseAuthUtils.getUserProfile().getDisplayName());
            Intent intent = new Intent(MainActivity.this, ConversationListActivity.class);

            startActivity(intent);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuthUtils.assignGoogleSignInCallback(this);

        TextView createAccountButton = findViewById(R.id.create_account_button);

        Button signInButton = findViewById(R.id.sign_in_button);
        SignInButton googleSignInButton = findViewById(R.id.sign_in_google);


        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);

        googleSignInButton.setOnClickListener(g -> {
            Intent signInIntent = FirebaseAuthUtils.googleSignIn(getBaseContext());
            startActivityForResult(signInIntent,RC_SIGN_IN);

        });


        signInButton.setOnClickListener(v -> {
            if(!email.getText().toString().equals("") && !password.getText().toString().equals("") ) {


                FirebaseAuthUtils.signIn(email.getText().toString(), password.getText().toString());
                if (FirebaseAuthUtils.isUserSignedIn())
                    startActivity(new Intent(MainActivity.this, ConversationListActivity.class));
                else {
                    Toast.makeText(getBaseContext(), "Failed to Sign in. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        createAccountButton.setOnClickListener(l -> {
            startActivityForResult(new Intent( MainActivity.this, CreateAccountActivity.class)
                    ,RC_CREATE_ACCOUNT);

                }

        );


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                FirebaseAuthUtils.firebaseAuthWithGoogle(account.getIdToken(),getBaseContext());

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    @Override
    public void verifyCredentials() {
        if(FirebaseAuthUtils.isUserSignedIn()){
            if (!FirebaseDatabaseUtils.doesUserExist(FirebaseAuthUtils.getUserId(), FirebaseAuthUtils.getUserProfile().getDisplayName()))
                FirebaseDatabaseUtils.addUser(FirebaseAuthUtils.getUserId(), FirebaseAuthUtils.getUserProfile().getDisplayName());
            startActivity(new Intent(MainActivity.this, ConversationListActivity.class));
        }else{
            Toast.makeText(getBaseContext(),"Failed to Sign in. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}