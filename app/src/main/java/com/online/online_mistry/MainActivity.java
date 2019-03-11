package com.online.online_mistry;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity   {


    private Toolbar mToolbar;
    private ViewPager myViewpager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter mytabsAccessorAdapter;
    private FirebaseUser currentUser;
    private static final String TAG = "Test";
    private FirebaseAuth mAuth;
    private DatabaseReference regRef;
    String currentUserID;
    private ImageButton profileButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();

        checkRegistration();


        mToolbar=(Toolbar)findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);



        myViewpager=(ViewPager) findViewById(R.id.main_tabs_pager);
        mytabsAccessorAdapter=new TabsAccessorAdapter(getSupportFragmentManager());
        myViewpager.setAdapter(mytabsAccessorAdapter);

        myTabLayout=(TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewpager);

        profileButton=findViewById(R.id.profileSettings);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileSettingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(profileSettingsIntent);
            }
        });

    }


    private void checkRegistration() {
        try{
            regRef= FirebaseDatabase.getInstance().getReference();
            currentUserID=mAuth.getCurrentUser().getUid();

            regRef.child("Shops").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(currentUserID).getValue().equals("")) {
                        SendUserToPhoneSignInActivity();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }catch (Exception e){
            Toast.makeText(MainActivity.this,"Login First!!!",Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser == null){
            SendUserToLoginActivity();
        }

    }

    private void SendUserToLoginActivity() {
        Intent LoginIntent=new Intent(MainActivity.this,LoginModeSelection.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }

    private void SendUserToPhoneSignInActivity() {
        Intent PhoneSignInIntent=new Intent(MainActivity.this,PhoneSignInActivity.class);
        PhoneSignInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(PhoneSignInIntent);
        finish();
    }
}

