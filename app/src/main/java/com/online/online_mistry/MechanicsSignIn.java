package com.online.online_mistry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MechanicsSignIn extends AppCompatActivity {

    private EditText MechanicsName;
    private CircleImageView MechanicsProfileImage;
    private Button Register;
    private FirebaseUser currentUser;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private StorageReference userProfileImagesRef, uploadToDatabase;
    private static final int GalleryPick = 1;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanics_sign_in);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = mAuth.getCurrentUser().getUid();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Shop User").child(currentUserID);

        uploadToDatabase = FirebaseStorage.getInstance().getReference();

        MechanicsName=findViewById(R.id.input_mechanic_Name);
        MechanicsProfileImage=findViewById(R.id.mechanic_profile_image);
        Register = (Button) findViewById(R.id.mechanic_Register);
        loadingBar = new ProgressDialog(this);
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterMechanicUser();

            }
        });

        MechanicsProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });


    }

    private void RegisterMechanicUser() {
        String StringMechanicsName=MechanicsName.getText().toString();

        if (TextUtils.isEmpty(StringMechanicsName)) {
            MechanicsName.setError("Enter Shop Name");
        }
        else{
            loadingBar.setTitle("Adding  Mechanic");
            loadingBar.setMessage("Please Wait");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            try{

                RootRef.child("Shops").child(currentUserID).child("Mechanic Name").setValue(StringMechanicsName);
                RootRef.child("Shops").child(currentUserID).child("Status").setValue("Available");
                sendUserToShopVerifyActivity();

            }catch (Exception e){

                Toast.makeText(MechanicsSignIn.this,"Registration Fail !!!",Toast.LENGTH_SHORT).show();

            }
            finally {
                loadingBar.dismiss();
            }

        }
    }
    private void sendUserToShopVerifyActivity() {
        Intent ShopVerifyIntent=new Intent(MechanicsSignIn.this,SplashActivity.class);
        startActivity(ShopVerifyIntent);
        finish();
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
                            Toast.makeText(MechanicsSignIn.this, "Profile Image Uploaded!!!", Toast.LENGTH_LONG).show();
                            final String downloadUrl = userProfileImagesRef.child(currentUserID + ".jpg").getDownloadUrl().toString();

                            // Toast.makeText(SettingActivity.this, downloadUrl, Toast.LENGTH_LONG).show();

                            uploadToDatabase.child("Shop User/" + currentUserID + "/" + currentUserID + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    RootRef.child("Shops").child(currentUserID).child("Image").child("Profile Image").setValue(uri.toString());
                                    //   Toast.makeText(SettingsActivity.this, "Image save to database ......!", Toast.LENGTH_LONG).show();
                                    loadingBar.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MechanicsSignIn.this, "Error :- " + e, Toast.LENGTH_LONG).show();
                                    loadingBar.dismiss();

                                }
                            });


                        } else {
                            String error = task.getException().toString();
                            Toast.makeText(MechanicsSignIn.this, "Error :- " + error, Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();

                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(MechanicsSignIn.this, "Error :- " + error, Toast.LENGTH_LONG).show();
                //  loadingBar.dismiss();

            }
        }

    }
}
