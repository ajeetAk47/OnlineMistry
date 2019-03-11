package com.online.online_mistry;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;


public class PhoneAuthActivity extends AppCompatActivity {


    private Button SendVerificationCodeButton;
    private ImageButton VerifyButton;
    private EditText InputPhoneNumber, InputVerificationCode;
    private TextView NumberHint;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;


    private static final String TAG = "PhoneAuthActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        mAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();

        SendVerificationCodeButton = (Button) findViewById(R.id.send_ver_code_button);
        VerifyButton = (ImageButton) findViewById(R.id.verify_button);
        InputPhoneNumber = (EditText) findViewById(R.id.phone_number_input);
        InputVerificationCode = (EditText) findViewById(R.id.verification_code_input);
       // NumberHint=(TextView)findViewById(R.id.enterNumberHint);
        loadingBar=new ProgressDialog(this);



        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                    String phoneNumber="+91"+InputPhoneNumber.getText().toString();

                    if(TextUtils.isEmpty(phoneNumber)){
                        Toast.makeText(PhoneAuthActivity.this,"Please Enter Phone Number...",Toast.LENGTH_SHORT).show();
                    }
                    else{

                        loadingBar.setTitle("Phone Verification");
                        loadingBar.setMessage("Please Wait");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneNumber,        // Phone number to verify
                                60,                 // Timeout duration
                                TimeUnit.SECONDS,   // Unit of timeout
                                PhoneAuthActivity.this,               // Activity (for callback binding)
                                callbacks);        // OnVerificationStateChangedCallbacks



                    }
            }
        });


        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

               String verificationCode =InputVerificationCode.getText().toString();

               if (TextUtils.isEmpty(verificationCode)){
                   Toast.makeText(PhoneAuthActivity.this,"Please Verification Code...",Toast.LENGTH_SHORT).show();
               }
               else {

                   loadingBar.setTitle("Verification Code");
                   loadingBar.setMessage("Please Wait, while we are verification code ");
                   loadingBar.setCanceledOnTouchOutside(false);
                   loadingBar.show();

                   PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                   signInWithPhoneAuthCredential(credential);
               }

            }
        });


        callbacks =new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneAuthActivity.this,"Error :- " +e,Toast.LENGTH_SHORT).show();

//                Toast.makeText(PhoneAuthActivity.this,"Invalid Phone Number ,Please Enter Correct Phone Number  with Your country Code..",Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);

            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId,token);

                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(PhoneAuthActivity.this,"Code has been Sent..",Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);
            //    NumberHint.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);

            }
        };


    }

    private void SendUserToMainActivity() {
        Intent PhoneAuthIntent=new Intent(PhoneAuthActivity.this,SplashActivity.class);
        startActivity(PhoneAuthIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null) {
            SendUserToMainActivity();
        }
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            RootRef= FirebaseDatabase.getInstance().getReference();
                            final String currentUserID=mAuth.getCurrentUser().getUid();
                            final AlertDialog.Builder authBuiler = new AlertDialog.Builder(PhoneAuthActivity.this, R.style.OlmDialogTheme);



                            RootRef.child("Shops").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.hasChild(currentUserID)) {
                                        RootRef.child("Shops").child(currentUserID).setValue("");
                                        loadingBar.dismiss();
                                        InputVerificationCode.setVisibility(View.INVISIBLE);
                                        VerifyButton.setVisibility(View.INVISIBLE);
                                        authBuiler.setTitle("Welcome Dear User");
                                        authBuiler.setMessage("Hope you like our service");
                                        authBuiler.setCancelable(false);
                                        authBuiler.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                SendUserToPhoneSignInActivity();

                                            }
                                        });
                                        authBuiler.show();
                                       ;
                                    }
                                    else if (dataSnapshot.child(currentUserID).hasChild("Shop Name")){
                                        loadingBar.dismiss();
                                        InputVerificationCode.setVisibility(View.INVISIBLE);
                                        VerifyButton.setVisibility(View.INVISIBLE);
                                        authBuiler.setTitle("Welcome Back Dear User");
                                        authBuiler.setMessage("Hope you like our service");
                                        authBuiler.setCancelable(false);
                                        authBuiler.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                SendUserToMainActivity();

                                            }
                                        });
                                        authBuiler.show();

                                    }
                                    else {
                                        mAuth.signOut();
                                        loadingBar.dismiss();
                                        InputVerificationCode.setVisibility(View.INVISIBLE);
                                        VerifyButton.setVisibility(View.INVISIBLE);
                                        authBuiler.setTitle("Oops!!!");
                                        authBuiler.setMessage("You Entered wrong information");
                                        authBuiler.setCancelable(false);
                                        authBuiler.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                SendUserToMainActivity();

                                            }
                                        });
                                        authBuiler.show();
                                    }

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                          //  Toast.makeText(PhoneAuthActivity.this,"Congratulation You're Logged in successfully..",Toast.LENGTH_SHORT).show();
                        } else {
                            loadingBar.dismiss();
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message=task.getException().toString();
                            Toast.makeText(PhoneAuthActivity.this,"Error "+message,Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void SendUserToPhoneSignInActivity() {
        Intent PhoneSignInIntent=new Intent(PhoneAuthActivity.this,PhoneSignInActivity.class);
        PhoneSignInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(PhoneSignInIntent);
        finish();
    }

}

