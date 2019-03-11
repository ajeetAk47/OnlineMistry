package com.online.online_mistry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class MechanicsLogin extends AppCompatActivity {
    private Button SendVerificationCodeButton;
    private ImageButton VerifyButton;
    private EditText InputPhoneNumber, InputVerificationCode,InputShopId;
    private TextView NumberHint;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;
    private static final String TAG = "MechanicsLogin";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanics_login);

        mAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();

        SendVerificationCodeButton = (Button) findViewById(R.id.mechanic_send_ver_code_button);
        VerifyButton = (ImageButton) findViewById(R.id.mechanic_verify_button);
        InputPhoneNumber = (EditText) findViewById(R.id.mechanic_phone_number_input);
        InputVerificationCode = (EditText) findViewById(R.id.mechanic_verification_code_input);
        // NumberHint=(TextView)findViewById(R.id.enterNumberHint);
        InputShopId=(EditText)findViewById(R.id.mechanic_shop_id);
        loadingBar=new ProgressDialog(this);

        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String phoneNumber="+91"+InputPhoneNumber.getText().toString();
                String inputShopID=InputShopId.getText().toString();

                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(MechanicsLogin.this,"Please Enter Phone Number...",Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(inputShopID)){
                    Toast.makeText(MechanicsLogin.this,"Please Enter Shop Number...",Toast.LENGTH_SHORT).show();
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
                            MechanicsLogin.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks



                }
            }
        });


        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);
                InputShopId.setVisibility(View.INVISIBLE);

                String verificationCode =InputVerificationCode.getText().toString();


                if (TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(MechanicsLogin.this,"Please Verification Code...",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(MechanicsLogin.this,"Error :- " +e,Toast.LENGTH_SHORT).show();

//                Toast.makeText(PhoneAuthActivity.this,"Invalid Phone Number ,Please Enter Correct Phone Number  with Your country Code..",Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);
                InputShopId.setVisibility(View.VISIBLE);

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

                Toast.makeText(MechanicsLogin.this,"Code has been Sent..",Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);
                InputShopId.setVisibility(View.INVISIBLE);
                //    NumberHint.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);

            }
        };


    }

    private void SendUserToMainActivity() {
        Intent PhoneAuthIntent=new Intent(MechanicsLogin.this,SplashActivity.class);
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


    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            RootRef= FirebaseDatabase.getInstance().getReference();
                            final String currentUserID=mAuth.getCurrentUser().getUid();
                            final String shopID=InputShopId.getText().toString();
                            final String CheckphoneNumber=InputPhoneNumber.getText().toString();
                            final AlertDialog.Builder checkBuiler = new AlertDialog.Builder(MechanicsLogin.this, R.style.OlmDialogTheme);



                            RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if ( !dataSnapshot.child("Shops").hasChild(currentUserID) && dataSnapshot.child("ShopMechanicsRelationship").child(shopID).child("Mechanics").hasChild(CheckphoneNumber)) {
                                        RootRef.child("Shops").child(currentUserID).setValue("");
                                        RootRef.child("Shops").child(currentUserID).child("Owner ID").setValue(shopID);
                                        RootRef.child("ShopMechanicsRelationship").child(shopID).child("Mechanics").child(CheckphoneNumber).setValue(currentUserID);

                                        String OwnerUid=dataSnapshot.child("ShopMechanicsRelationship").child(shopID).child("ShopUid").getValue().toString();
                                        RootRef.child("Shops").child(OwnerUid).child("Members").child(CheckphoneNumber).setValue(currentUserID);


                                        SendUserToPhoneSignInActivity();

                                        loadingBar.dismiss();
                                        Toast.makeText(MechanicsLogin.this,"Congratulation You're Logged in successfully..",Toast.LENGTH_SHORT).show();
                                    }
                                    else if (dataSnapshot.child("Shops").child(currentUserID).hasChild("Owner ID") && dataSnapshot.child("ShopMechanicsRelationship").child(shopID).child("Mechanics").hasChild(CheckphoneNumber) &&   dataSnapshot.child("ShopMechanicsRelationship").child(shopID).child("Mechanics").child(CheckphoneNumber).getValue().equals(currentUserID)){
                                        SendUserToMainActivity();
                                        loadingBar.dismiss();
                                    }
                                    else {
                                        Toast.makeText(MechanicsLogin.this,"Error",Toast.LENGTH_SHORT).show();
                                        InputVerificationCode.setVisibility(View.INVISIBLE);
                                        VerifyButton.setVisibility(View.INVISIBLE);
                                        loadingBar.dismiss();
                                        mAuth.signOut();
                                        checkBuiler.setTitle("Invalid Mechanic");
                                        checkBuiler.setMessage("Please Enter Valid Information !!!");
                                        checkBuiler.setCancelable(false);
                                        checkBuiler.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent MainIntent=new Intent(MechanicsLogin.this,LoginModeSelection.class);
                                                MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(MainIntent);

                                            }
                                        });
                                        checkBuiler.show();
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
                            Toast.makeText(MechanicsLogin.this,"Error "+message,Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void SendUserToPhoneSignInActivity() {
        Intent PhoneSignInIntent=new Intent(MechanicsLogin.this,MechanicsSignIn.class);
        PhoneSignInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(PhoneSignInIntent);
        finish();
    }

}
