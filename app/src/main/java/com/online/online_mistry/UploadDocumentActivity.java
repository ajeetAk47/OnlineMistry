package com.online.online_mistry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class UploadDocumentActivity extends AppCompatActivity {
    private Toolbar uploadDocumentToolbar;
    private TextView openTime, closeTime, workingHr;
    private EditText inputAadhar, inputPan, inputGst, inputShopLicence;
    private Button uploadAadhar, uploadPan, SaveDetails;
    private FirebaseUser currentUser;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference regRef, rootref;
    private StorageReference userProfileImagesRef, uploadToDatabase;
    private String currentUserID;
    private static final int GalleryPick1 = 1, GalleryPick2 = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_document);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        regRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootref = FirebaseDatabase.getInstance().getReference().child("Shops").child(currentUserID);
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Shop User").child(currentUserID);

        uploadToDatabase = FirebaseStorage.getInstance().getReference();

        uploadDocumentToolbar = (Toolbar) findViewById(R.id.uploadDocument_toolbar);
        setSupportActionBar(uploadDocumentToolbar);
        getSupportActionBar().setTitle("Upload Documents");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        openTime = findViewById(R.id.openingTime);
        closeTime = findViewById(R.id.closingTime);
        workingHr = findViewById(R.id.tvWorking);
        inputAadhar = findViewById(R.id.input_aadharNumber);
        inputPan = findViewById(R.id.input_panNumber);
        inputGst = findViewById(R.id.input_gstNumber);
        inputShopLicence = findViewById(R.id.inputRegistration_Id);

        uploadAadhar = findViewById(R.id.upload_aadharNumber);
        uploadPan = findViewById(R.id.upload_panNumber);
        SaveDetails = findViewById(R.id.SaveDetails);

        final AlertDialog.Builder saveBuiler = new AlertDialog.Builder(this, R.style.OlmDialogTheme);
        loadingBar = new ProgressDialog(this);


        openTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TimePickerDialog timePickerDialog = new TimePickerDialog(UploadDocumentActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        String amPm;
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        } else {
                            amPm = "AM";
                        }

                        openTime.setText(hourOfDay + ":" + minutes + " " + amPm);
                    }
                }, 0, 0, false);

                timePickerDialog.show();

            }
        });

        closeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TimePickerDialog timePickerDialog = new TimePickerDialog(UploadDocumentActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        String amPm;
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        } else {
                            amPm = "AM";
                        }

                        closeTime.setText(hourOfDay + ":" + minutes + " " + amPm);
                    }
                }, 0, 0, false);

                timePickerDialog.show();

            }
        });

        uploadAadhar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent AadharIntent = new Intent();
                AadharIntent.setAction(Intent.ACTION_GET_CONTENT);
                AadharIntent.setType("image/*");
                startActivityForResult(AadharIntent, GalleryPick1);

            }
        });

        uploadPan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent panIntent = new Intent();
                panIntent.setAction(Intent.ACTION_GET_CONTENT);
                panIntent.setType("image/*");
                startActivityForResult(panIntent, GalleryPick2);

            }
        });


        SaveDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBuiler.setTitle("Confirm Save?");
                saveBuiler.setMessage("Do you really want to Lock these Details ? \nIt will take 3-4 days for verification.");
                saveBuiler.setCancelable(false);
                saveBuiler.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        String stringAadhar = inputAadhar.getText().toString();
                        String stringPan = inputPan.getText().toString();
                        String stringGst = inputGst.getText().toString();
                        String stringOpen = openTime.getText().toString();
                        String stringClose = closeTime.getText().toString();
                        String stringShopLicence = inputShopLicence.getText().toString();


                        if (TextUtils.isEmpty(stringAadhar)) {
                            inputAadhar.setError("Enter Aadhar Number");
                        }
                        if (TextUtils.isEmpty(stringPan)) {
                            inputPan.setError("Enter Pancard Number");
                        }

                        if (TextUtils.isEmpty(stringGst)) {
                            inputGst.setError("Enter Gst Number");
                        }
                        if (TextUtils.isEmpty(stringShopLicence)) {
                            inputGst.setError("Enter Shop Licence Number");
                        }

                        if (TextUtils.equals(stringOpen, "Opening") && TextUtils.equals(stringClose, "Closing")) {
                            workingHr.setError("Set Working Hours");
                        }
                       /* if (  ){
                            Toast.makeText(UploadDocumentActivity.this,"Upload Photo of  Documents ",Toast.LENGTH_SHORT).show();

                        }*/
                        else {

                            loadingBar.setTitle("Uploding  Details ");
                            loadingBar.setMessage("Please Wait !!!!");
                            loadingBar.setCanceledOnTouchOutside(true);
                            loadingBar.show();

                            try {

                                String time = stringOpen + " - " + stringClose;
                                HashMap<String, String> DetailsMap = new HashMap<>();
                                DetailsMap.put("Aadhar Number", stringAadhar);
                                DetailsMap.put("Pancard Number", stringPan);
                                DetailsMap.put("Gst Number", stringGst);
                                DetailsMap.put("Working Hours", time);
                                DetailsMap.put("Shop Licence Number", stringShopLicence);
                                DetailsMap.put("Details Lock","true");


                                rootref.child("Details").setValue(DetailsMap);


                                Intent senduserSetting = new Intent(UploadDocumentActivity.this, SettingsActivity.class);
                                startActivity(senduserSetting);
                                finish();


                            } catch (Exception e) {

                                Toast.makeText(UploadDocumentActivity.this, "Upload Fail!!!", Toast.LENGTH_SHORT).show();


                            } finally {
                                loadingBar.dismiss();
                            }


                        }

                    }
                });
                saveBuiler.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss
                    }
                });
                saveBuiler.show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri ImageUri = data.getData();

            loadingBar.setTitle("Uploading Aadhar Card Photo");
            loadingBar.setMessage("Please wait Image is Uploading......");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            Uri resultUri = ImageUri;


            final StorageReference filepath = userProfileImagesRef.child("AadharPhoto.jpg");
            filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(UploadDocumentActivity.this, "Image Uploaded!!!", Toast.LENGTH_LONG).show();
                        final String downloadUrl = userProfileImagesRef.child(currentUserID + ".jpg").getDownloadUrl().toString();

                        // Toast.makeText(SettingActivity.this, downloadUrl, Toast.LENGTH_LONG).show();

                        uploadToDatabase.child("Shop User/" + currentUserID + "/" + "AadharPhoto.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                rootref.child("Image").child("AadharPhoto").setValue(uri.toString());
                                //   Toast.makeText(SettingsActivity.this, "Image save to database ......!", Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UploadDocumentActivity.this, "Error :- " + e, Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();

                            }
                        });


                    } else {
                        String error = task.getException().toString();
                        Toast.makeText(UploadDocumentActivity.this, "Error :- " + error, Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();

                    }
                }
            });
        }
        if (requestCode == GalleryPick2 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri ImageUri = data.getData();

            loadingBar.setTitle("Uploading Pan Card Photo");
            loadingBar.setMessage("Please wait Image is Uploading......");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            Uri resultUri = ImageUri;


            final StorageReference filepath = userProfileImagesRef.child("PanCardPhoto.jpg");
            filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(UploadDocumentActivity.this, "Image Uploaded!!!", Toast.LENGTH_LONG).show();
                        final String downloadUrl = userProfileImagesRef.child(currentUserID + ".jpg").getDownloadUrl().toString();

                        // Toast.makeText(SettingActivity.this, downloadUrl, Toast.LENGTH_LONG).show();

                        uploadToDatabase.child("Shop User/" + currentUserID + "/" + "PanCardPhoto.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                rootref.child("Image").child("PanCardPhoto").setValue(uri.toString());
                                //   Toast.makeText(SettingsActivity.this, "Image save to database ......!", Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UploadDocumentActivity.this, "Error :- " + e, Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();

                            }
                        });


                    } else {
                        String error = task.getException().toString();
                        Toast.makeText(UploadDocumentActivity.this, "Error :- " + error, Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();

                    }
                }
            });
        }

    }


}
