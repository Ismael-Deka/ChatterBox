package com.example.chatterbox;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatterbox.firebases.FirebaseAuthUtils;

public class CreateAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_activity);

        EditText nameView = findViewById(R.id.name);
        EditText newEmailView = findViewById(R.id.new_email);
        EditText newPasswordView = findViewById(R.id.new_password);
        EditText confirmPasswordView = findViewById(R.id.confirm_password);

        Button createAccountButton = findViewById(R.id.create_account_button);

        createAccountButton.setOnClickListener(c -> {
            String name = nameView.getText().toString();
            String newEmail = newEmailView.getText().toString();
            String newPassword = newPasswordView.getText().toString();
            String confirmPassword = confirmPasswordView.getText().toString();

            if(!newPassword.equals(confirmPassword)){
                Toast.makeText(getBaseContext(),"Passwords must match. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }else if(newPassword.length() < 6 ) {
                Toast.makeText(getBaseContext(), "Password must be a least 6 characters. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }else if(!newEmail.matches(".*@.*[.].*")){
                Toast.makeText(getBaseContext(), "Not a valid email. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
            else{
                FirebaseAuthUtils.createAccount(name,newEmail,newPassword,getBaseContext());
                finish();
            }
        });

    }


}
