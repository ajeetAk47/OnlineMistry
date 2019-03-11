package com.online.online_mistry;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView phoneNumber, UserName,Note;
    private FirebaseAuth mAuth;
    private DatabaseReference regRef, rootref;
    private FirebaseUser currentUser;
    private static final int GalleryPick = 1;
    private String currentUserID;
    private Button SettingVerifiedButton, uploadDocumentButton, membersButton, logoutButton;
    private CircleImageView userProfileImage;
    private StorageReference userProfileImagesRef, uploadToDatabase;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        regRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootref = FirebaseDatabase.getInstance().getReference().child("Shops").child(currentUserID);
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Shop User").child(currentUserID);

        uploadToDatabase = FirebaseStorage.getInstance().getReference();

        loadingBar = new ProgressDialog(this);


        //initialization
        SettingVerifiedButton = (Button) findViewById(R.id.setting_verified);
        uploadDocumentButton = (Button) findViewById(R.id.update_document);
        membersButton = (Button) findViewById(R.id.shop_memebers);
        logoutButton = (Button) findViewById(R.id.Logout);
        phoneNumber = (TextView) findViewById(R.id.user_profile_number);
        UserName = (TextView) findViewById(R.id.user_profile_name);
        Note=(TextView)findViewById(R.id.note);
        userProfileImage = (CircleImageView) findViewById(R.id.user_profile_image);


        mToolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //logout Alert
        final AlertDialog.Builder logoutBuiler = new AlertDialog.Builder(this, R.style.OlmDialogTheme);


        VerifiedProfile();
        ReceiveUserInfo();


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutBuiler.setTitle("Confirm Logout ?");
                logoutBuiler.setMessage(" Do you really want to Logout?");
                logoutBuiler.setCancelable(false);
                logoutBuiler.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        Intent senduserLogin = new Intent(SettingsActivity.this, LoginModeSelection.class);
                        startActivity(senduserLogin);
                        finish();
                    }
                });
                logoutBuiler.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss
                    }
                });
                logoutBuiler.show();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });

        uploadDocumentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent uploadDocumentIntent = new Intent(SettingsActivity.this, UploadDocumentActivity.class);
                startActivity(uploadDocumentIntent);
            }
        });

        membersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent memberIntent = new Intent(SettingsActivity.this, EditMechanicsActivity.class);
                startActivity(memberIntent);
            }
        });

    }

    private void VerifiedProfile() {

        try {

            regRef.child("Shops").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(currentUserID).child("Verified").getValue().equals("true")) {
                        SettingVerifiedButton.setText("Verified");
                        SettingVerifiedButton.getBackground().setColorFilter(SettingVerifiedButton.getContext().getResources().getColor(R.color.colorBlue), PorterDuff.Mode.SRC);
                        String ShopID = dataSnapshot.child(currentUserID).child("Shop ID").getValue().toString();
                        //Shop ID
                        uploadDocumentButton.setText("Shop ID " + ShopID);
                        uploadDocumentButton.setClickable(false);
                    }
                    else if (dataSnapshot.child(currentUserID).hasChild("Details")&&dataSnapshot.child(currentUserID).child("Details").hasChild("Details Lock")&& dataSnapshot.child(currentUserID).child("Details").child("Details Lock").getValue().equals("true")) {

                        String note = "Details Locked Wait Few Dates For Verification";
                        //Shop ID
                        uploadDocumentButton.setText(note);
                        uploadDocumentButton.setClickable(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
            Toast.makeText(SettingsActivity.this, "Internet Problem", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {

            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait profile Image is Uploading......");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resultUri = result.getUri();


                final StorageReference filepath = userProfileImagesRef.child(currentUserID + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Profile Image Uploaded!!!", Toast.LENGTH_LONG).show();
                            final String downloadUrl = userProfileImagesRef.child(currentUserID + ".jpg").getDownloadUrl().toString();

                            // Toast.makeText(SettingActivity.this, downloadUrl, Toast.LENGTH_LONG).show();

                            uploadToDatabase.child("Shop User/" + currentUserID + "/" + currentUserID + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    rootref.child("Image").child("Profile Image").setValue(uri.toString());
                                    //   Toast.makeText(SettingsActivity.this, "Image save to database ......!", Toast.LENGTH_LONG).show();
                                    loadingBar.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SettingsActivity.this, "Error :- " + e, Toast.LENGTH_LONG).show();
                                    loadingBar.dismiss();

                                }
                            });


                        } else {
                            String error = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error :- " + error, Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();

                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(SettingsActivity.this, "Error :- " + error, Toast.LENGTH_LONG).show();
                //  loadingBar.dismiss();

            }
        }

    }


    private void ReceiveUserInfo() {
        rootref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //if pic  is available
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("Owner Name")) && (dataSnapshot.child("Image").hasChild("Profile Image"))) {
                    String receiveUserName = dataSnapshot.child("Owner Name").getValue().toString();
                    final String retriveUserImage = dataSnapshot.child("Image").child("Profile Image").getValue().toString();

                    String receivePhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();


                    phoneNumber.setText(receivePhoneNumber);
                    UserName.setText(receiveUserName);


                    // Picasso.get().load(retriveUserImage).placeholder(R.drawable.profile).into(userProfileImage);
                    //For image offline
                    Picasso.get().load(retriveUserImage).networkPolicy(NetworkPolicy.OFFLINE).
                            placeholder(R.drawable.profile).
                            into(userProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(retriveUserImage).placeholder(R.drawable.profile).into(userProfileImage);
                                }
                            });


                }
                //if pic not available
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("Owner Name"))) {
                    String retriveUserName = dataSnapshot.child("Owner Name").getValue().toString();
                    String retrivePhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                    UserName.setText(retriveUserName);

                    phoneNumber.setText(retrivePhoneNumber);

                } else {
                    Toast.makeText(SettingsActivity.this, "Please Update profile  Details", Toast.LENGTH_LONG).show();
                }

                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("Notices"))){

                    String retriveNote=dataSnapshot.child("Notices").getValue().toString();
                    Note.setText(retriveNote);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
