package com.online.online_mistry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private String currentUserID;
    private static final String TAG = "Test";
    private DatabaseReference rootref;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkInternetConnection();
         mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
    }

    private void checkInternetConnection() {

        if (AppStatus.getInstance(this).isOnline()) {
            //Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), "Testing Snackbar", Snackbar.LENGTH_LONG).show();
            //Toast.makeText(getContext(), "You are online!!!!",Toast.LENGTH_SHORT).show();

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mAuth= FirebaseAuth.getInstance();
                        currentUserID = mAuth.getCurrentUser().getUid();
                        rootref = FirebaseDatabase.getInstance().getReference().child("Shops").child(currentUserID);

                        rootref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists() && dataSnapshot.hasChild("Owner ID")){
                                    Intent i = new Intent(SplashActivity.this, WorkerActivity.class);
                                    startActivity(i);
                                    finish();

                                }
                                else{
                                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }catch (Exception e){
                        SendUserToLoginActivity();

                    }




                    // This method will be executed once the timer is over

                }
            });

            /*new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAuth= FirebaseAuth.getInstance();
                    currentUser=mAuth.getCurrentUser();
                    // This method will be executed once the timer is over


                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }, 2000);*/




        } else {
            final AlertDialog.Builder logoutBuiler = new AlertDialog.Builder(this, R.style.OlmDialogTheme);
            logoutBuiler.setTitle("Internet Not Connected");
            logoutBuiler.setMessage("Please Check Internet Connection and  Open App Again!!!");
            logoutBuiler.setCancelable(false);
            logoutBuiler.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            logoutBuiler.show();


            Log.v("Home", "############################You are not online!!!!");
        }
    }


    private void SendUserToLoginActivity() {
        Intent LoginIntent=new Intent(SplashActivity.this,LoginModeSelection.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }

    private void SendUserToPhoneSignInActivity() {
        Intent PhoneSignInIntent=new Intent(SplashActivity.this,PhoneSignInActivity.class);
        PhoneSignInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(PhoneSignInIntent);
        finish();
    }

}
