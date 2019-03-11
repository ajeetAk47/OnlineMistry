package com.online.online_mistry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

public class WorkerSettingActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView phoneNumber, UserName,shopName;
    private FirebaseAuth mAuth;
    private DatabaseReference regRef, rootref;
    private FirebaseUser currentUser;
    private static final int GalleryPick = 1;
    private String currentUserID;
    private Button  logoutButton;
    private CircleImageView userProfileImage;
    private StorageReference userProfileImagesRef, uploadToDatabase;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_setting);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        regRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootref = FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Shop User").child(currentUserID);


        uploadToDatabase = FirebaseStorage.getInstance().getReference();

        loadingBar = new ProgressDialog(this);

        logoutButton = (Button) findViewById(R.id.mechanic_Logout);
        phoneNumber = (TextView) findViewById(R.id.mechanic_user_profile_number);
        UserName = (TextView) findViewById(R.id.mechanic_user_profile_name);
        shopName=(TextView)findViewById(R.id.mechanic_ShopName);
        userProfileImage = (CircleImageView) findViewById(R.id.mechanic_user_profile_image);

        mToolbar = (Toolbar) findViewById(R.id.mechanic_setting_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final AlertDialog.Builder logoutBuiler = new AlertDialog.Builder(this, R.style.OlmDialogTheme);

        ReceiveUserInfo();
        ReceiveShopName();


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
                        Intent senduserLogin = new Intent(WorkerSettingActivity.this, LoginModeSelection.class);
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




        // worker setting
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
                            Toast.makeText(WorkerSettingActivity.this, "Profile Image Uploaded!!!", Toast.LENGTH_LONG).show();
                            final String downloadUrl = userProfileImagesRef.child(currentUserID + ".jpg").getDownloadUrl().toString();

                            // Toast.makeText(SettingActivity.this, downloadUrl, Toast.LENGTH_LONG).show();

                            uploadToDatabase.child("Shop User/" + currentUserID + "/" + currentUserID + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    rootref.child("Shops").child(currentUserID).child("Image").child("Profile Image").setValue(uri.toString());
                                    //   Toast.makeText(SettingsActivity.this, "Image save to database ......!", Toast.LENGTH_LONG).show();
                                    loadingBar.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(WorkerSettingActivity.this, "Error :- " + e, Toast.LENGTH_LONG).show();
                                    loadingBar.dismiss();

                                }
                            });


                        } else {
                            String error = task.getException().toString();
                            Toast.makeText(WorkerSettingActivity.this, "Error :- " + error, Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();

                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(WorkerSettingActivity.this, "Error :- " + error, Toast.LENGTH_LONG).show();
                //  loadingBar.dismiss();

            }
        }

    }

    private void ReceiveUserInfo() {
        rootref.child("Shops").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //if pic  is available
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("Mechanic Name")) && dataSnapshot.hasChild("Image") && (dataSnapshot.child("Image").hasChild("Profile Image"))) {
                    String receiveUserName = dataSnapshot.child("Mechanic Name").getValue().toString();
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
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("Mechanic Name"))) {
                    String retriveUserName = dataSnapshot.child("Mechanic Name").getValue().toString();
                    String retrivePhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                    UserName.setText(retriveUserName);

                    phoneNumber.setText(retrivePhoneNumber);

                } else {
                    Toast.makeText(WorkerSettingActivity.this, "Please Update profile  Details", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void ReceiveShopName() {
        rootref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.child("Shops").child(currentUserID).exists()) && (dataSnapshot.child("Shops").child(currentUserID).hasChild("Owner ID"))){

                    String retriveOwnerID=dataSnapshot.child("Shops").child(currentUserID).child("Owner ID").getValue().toString();
                    String retriveOwnerUID=dataSnapshot.child("ShopMechanicsRelationship").child(retriveOwnerID).child("ShopUid").getValue().toString();
                    String retriveShopName=dataSnapshot.child("Shops").child(retriveOwnerUID).child("Shop Name").getValue().toString();
                    shopName.setText(retriveShopName);
                }
                else {
                    Toast.makeText(WorkerSettingActivity.this, "Load Fail", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
