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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.online.online_mistry.R.style.OlmDialogTheme;

public class EditMechanicsActivity extends AppCompatActivity {
    private Toolbar editMechToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference regRef, rootref;
    private FirebaseUser currentUser;
    private String currentUserID;
    private FloatingActionButton addMechanics;
    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_mechanics = new ArrayList<>();
    private static final String TAG = "Edit Mechanics";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mechanics);
        editMechToolbar = (Toolbar) findViewById(R.id.editMech_toolbar);
        addMechanics = findViewById(R.id.addShopMechanics);
        list_view = findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(EditMechanicsActivity.this, R.layout.simple_item, list_of_mechanics);
        list_view.setAdapter(arrayAdapter);
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
        RetriveAndDisplay();

        addMechanics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog(EditMechanicsActivity.this);
            }
        });


        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final String currentNumber = adapterView.getItemAtPosition(position).toString();
                AlertDialog dialog = new AlertDialog.Builder(EditMechanicsActivity.this, OlmDialogTheme)
                        .setTitle("Delete")
                        .setMessage("Do you want  to  delete this  mechanics ? \n" + currentNumber)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                rootref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String shopid = dataSnapshot.child("Shop ID").getValue().toString();
                                        String delUser = dataSnapshot.child("Members").child(currentNumber).getValue().toString();
                                        if (!delUser.equals("")) {
                                            regRef.child("Shops").child(delUser).removeValue();
                                        }


                                        rootref.child("Members").child(currentNumber).removeValue();
                                        regRef.child("ShopMechanicsRelationship").child(shopid).child("Mechanics").child(currentNumber).removeValue();


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();


            }
        });


    }

    private void RetriveAndDisplay() {

        rootref.child("Members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    set.add(((DataSnapshot) iterator.next()).getKey());
                }


                list_of_mechanics.clear();
                list_of_mechanics.addAll(set);
                arrayAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void showInputDialog(Context c) {
        final EditText taskEditText = new EditText(c);
        taskEditText.setTextColor(Color.WHITE);
        taskEditText.setTextSize(50);
        taskEditText.setMaxLines(1);

        taskEditText.setInputType(InputType.TYPE_CLASS_PHONE);
        AlertDialog dialog = new AlertDialog.Builder(c, OlmDialogTheme)
                .setTitle("Add a new Mechanic")
                .setMessage("Enter Mechanic Phone Number ?")
                .setView(taskEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String task = String.valueOf(taskEditText.getText());
                        if (task.length() == 10) {
                            rootref.child("Members").child(task).setValue("");
                            rootref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String shopid = dataSnapshot.child("Shop ID").getValue().toString();
                                        regRef.child("ShopMechanicsRelationship").child(shopid).child("Mechanics").child(task).setValue("");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        } else {
                            Toast.makeText(EditMechanicsActivity.this, "Enter Valid Number", Toast.LENGTH_LONG).show();
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
                        AlertDialog.Builder logoutBuiler = new AlertDialog.Builder(EditMechanicsActivity.this, OlmDialogTheme);
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
