package com.online.online_mistry;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PhoneSignInActivity extends AppCompatActivity {


    private EditText ShopName, OwnerName, EmailId, PinCode, ShopAddress,ShopCity,ShopState;
    private Button Register;
    private FirebaseUser currentUser;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_sign_in);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        ShopName = (EditText) findViewById(R.id.input_shopName);
        OwnerName = (EditText) findViewById(R.id.input_OwnerName);
        EmailId = (EditText) findViewById(R.id.input_EmailId);
        PinCode = (EditText) findViewById(R.id.input_pincode);
        ShopAddress = (EditText) findViewById(R.id.input_address);
        ShopCity = (EditText) findViewById(R.id.input_city);
        ShopState= (EditText) findViewById(R.id.input_state);
        Register = (Button) findViewById(R.id.Register);
        loadingBar = new ProgressDialog(this);


        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterShopUser();
            }
        });

    }

    private void RegisterShopUser() {

        String StringShopName=ShopName.getText().toString();
        String StringOwnerName=OwnerName.getText().toString();
        String StringEmailId =EmailId.getText().toString();
        String StringPinCode=PinCode.getText().toString();
        String StringShopAddress=ShopAddress.getText().toString();
        String StringShopCity=ShopCity.getText().toString();
        String StringShopState=ShopState.getText().toString();

        if (TextUtils.isEmpty(StringShopName)) {
            ShopName.setError("Enter Shop Name");
        }
        if (TextUtils.isEmpty(StringOwnerName)) {
            OwnerName.setError("Enter Owner Name");
        }
        if (TextUtils.isEmpty(StringPinCode) && TextUtils.isDigitsOnly(StringPinCode) && StringPinCode.length()==6 )  {
            PinCode.setError("Enter valid Pin Code");
        }
        if (TextUtils.isEmpty(StringShopAddress)) {
            ShopAddress.setError("Enter Shop Address");
        }
        if (TextUtils.isEmpty(StringShopCity)) {
            ShopCity.setError("Enter City");
        }
        if (TextUtils.isEmpty(StringShopState)) {
            ShopState.setError("Enter State");
        }

        if (TextUtils.isEmpty(StringEmailId)) {
            EmailId.setError("Enter Email Id!");
        }  else {

            loadingBar.setTitle("New Account ");
            loadingBar.setMessage("Please Wait");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

        try {

          //  String time = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());

            String time = new SimpleDateFormat("MMddHHmmss").format(Calendar.getInstance().getTime());



            String currentUserID = mAuth.getCurrentUser().getUid();
            HashMap<String, String> ShopMap = new HashMap<>();
            //profileMap.put("uid",currentUserID);
            ShopMap.put("Shop Name", StringShopName);
            ShopMap.put("Verified","false");
            ShopMap.put("Owner Name", StringOwnerName);
            ShopMap.put("Email Id", StringEmailId);
            ShopMap.put("Members","");
            ShopMap.put("Shop ID",time);
            ShopMap.put("Notifications","");
            ShopMap.put("Image","");
            ShopMap.put("Notices","");
            RootRef.child("Shops").child(currentUserID).setValue(ShopMap);



            HashMap<String, String> AddressMap = new HashMap<>();
            AddressMap.put("Local", StringShopAddress);
            AddressMap.put("City",StringShopCity);
            AddressMap.put("State",StringShopState);
            AddressMap.put("Pin Code",StringPinCode);
            RootRef.child("Shops").child(currentUserID).child("Address").setValue(AddressMap);

            RootRef.child("ShopMechanicsRelationship").child(time).setValue("");
            RootRef.child("ShopMechanicsRelationship").child(time).child("ShopUid").setValue(currentUserID);
            RootRef.child("ShopMechanicsRelationship").child(time).child("Mechanics").setValue("");


            sendUserToShopVerifyActivity();



        }catch (Exception e){

            Toast.makeText(PhoneSignInActivity.this,"Registration Fail !!!",Toast.LENGTH_SHORT).show();


        }finally {
            loadingBar.dismiss();
        }



        }

    }

    private void sendUserToShopVerifyActivity() {
        Intent ShopVerifyIntent=new Intent(PhoneSignInActivity.this,SplashActivity.class);
        startActivity(ShopVerifyIntent);
        finish();
    }


    private void SendUserToMainActivity() {
        Intent PhoneSignINIntent = new Intent(PhoneSignInActivity.this, MainActivity.class);
        startActivity(PhoneSignINIntent);
        finish();
    }
}


