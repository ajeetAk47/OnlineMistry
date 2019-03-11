package com.online.online_mistry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditMechanicsActivity extends AppCompatActivity {
    private Toolbar editMechToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference regRef, rootref;
    private FirebaseUser currentUser;
    private String currentUserID;
    private FloatingActionButton addMechanics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mechanics);
        editMechToolbar = (Toolbar) findViewById(R.id.editMech_toolbar);
        addMechanics=findViewById(R.id.addShopMechanics);
        setSupportActionBar(editMechToolbar);
        getSupportActionBar().setTitle("Edit Mechanics");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        regRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootref = FirebaseDatabase.getInstance().getReference().child("Shops").child(currentUserID);


        verifyUser();

        addMechanics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog(EditMechanicsActivity.this);
            }
        });




    }

    private void showInputDialog(Context c) {
        final EditText taskEditText = new EditText(c);
        taskEditText.setTextColor(Color.WHITE);
        taskEditText.setTextSize(50);
        taskEditText.setMaxLines(1);

        taskEditText.setInputType(InputType.TYPE_CLASS_PHONE);
        AlertDialog dialog = new AlertDialog.Builder(c, R.style.OlmDialogTheme)
                .setTitle("Add a new Mechanic")
                .setMessage("Enter Mechanic Phone Number ?")
                .setView(taskEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String task = String.valueOf(taskEditText.getText());
                        if (task.length()==10) {
                            rootref.child("Members").child(task).setValue("");
                            rootref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                   if (dataSnapshot.exists()){
                                       String shopid=dataSnapshot.child("Shop ID").getValue().toString();
                                       regRef.child("ShopMechanicsRelationship").child(shopid).child("Mechanics").child(task).setValue("");
                                   }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }else {
                            Toast.makeText(EditMechanicsActivity.this,"Enter Valid Number",Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();

    }

    private void verifyUser() {

        try {

            regRef.child("Shops").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!(dataSnapshot.child(currentUserID).child("Verified").getValue().equals("true"))) {
                        AlertDialog.Builder logoutBuiler=new AlertDialog.Builder(EditMechanicsActivity.this, R.style.OlmDialogTheme);
                        logoutBuiler.setTitle("Not Verified");
                        logoutBuiler.setMessage(" Upload Your Details and  wait  few  days for  verification !!!!!");
                        logoutBuiler.setCancelable(false);
                        logoutBuiler.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent senduserToSetting = new Intent(EditMechanicsActivity.this, SettingsActivity.class);
                                startActivity(senduserToSetting);
                                finish();
                            }
                        });
                        logoutBuiler.show();



                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
            Toast.makeText(EditMechanicsActivity.this, "Internet Problem", Toast.LENGTH_SHORT).show();

        }

    }
}
