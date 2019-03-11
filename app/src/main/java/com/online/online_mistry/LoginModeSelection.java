package com.online.online_mistry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginModeSelection extends AppCompatActivity {
    Button Owner, Mechanic;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_mode_selection);
        Owner = findViewById(R.id.shopQwner);
        Mechanic = findViewById(R.id.mechanic);
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();

        Owner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToOwnerLoginActivity();

            }
        });
        Mechanic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMechincsLoginActivity();

            }
        });

    }

    private void SendUserToOwnerLoginActivity() {
        Intent OwnerIntent = new Intent(LoginModeSelection.this, PhoneAuthActivity.class);
        startActivity(OwnerIntent);
        // finish();
    }

    private void SendUserToMechincsLoginActivity() {
        Intent MIntent = new Intent(LoginModeSelection.this,MechanicsLogin.class);
      //  LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MIntent);
     //   finish();
    }

    private void SendUserToMainActivity() {
        Intent PhoneIntent=new Intent(LoginModeSelection.this,SplashActivity.class);
        startActivity(PhoneIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null) {
            SendUserToMainActivity();
        }
    }
}
