package com.online.online_mistry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class WorkerActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager myViewpager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter mytabsAccessorAdapter;
    private FirebaseUser currentUser;
    private static final String TAG = "Worker";
    private FirebaseAuth mAuth;
    private DatabaseReference regRef;
    String currentUserID;
    private ImageButton profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker);


        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();


        mToolbar=(Toolbar)findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);



        myViewpager=(ViewPager) findViewById(R.id.work_tabs_pager);
        mytabsAccessorAdapter=new TabsAccessorAdapter(getSupportFragmentManager());
        myViewpager.setAdapter(mytabsAccessorAdapter);

        myTabLayout=(TabLayout) findViewById(R.id.work_tabs);
        myTabLayout.setupWithViewPager(myViewpager);

        profileButton=findViewById(R.id.profileSettings);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileSettingsIntent=new Intent(WorkerActivity.this,WorkerSettingActivity.class);
                startActivity(profileSettingsIntent);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser == null){
            SendUserToLoginActivity();
        }

    }

    private void SendUserToLoginActivity() {
        Intent LoginIntent=new Intent(WorkerActivity.this,PhoneAuthActivity.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }

    private void SendUserToPhoneSignInActivity() {
        Intent PhoneSignInIntent=new Intent(WorkerActivity.this,PhoneSignInActivity.class);
        PhoneSignInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(PhoneSignInIntent);
        finish();
    }
}
