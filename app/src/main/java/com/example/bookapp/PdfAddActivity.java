package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.example.bookapp.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {
    private ActivityPdfAddBinding binding;

    private FirebaseAuth firebaseAuth;
    private static final String TAG = "Add PDF TAG";

    private ProgressDialog progressDialog;

    private static final int PDF_PICK_CODE = 1000;

    private Uri pdfUri = null;

    private ArrayList<String> catagoryTitleArrayList, catagoryIdArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCatagories();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait..");
        progressDialog.setCanceledOnTouchOutside(false);


        //handle button
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });
        binding.catagoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catagoryPickDialog();
            }
        });

        //handle upload button
        binding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validation
                validateData();
            }
        });
    }
    private String title = "", description = "";

    private void validateData() {

        Log.d(TAG, "validateData: validating Data...");
        //validate data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();


        //now validation
        if(TextUtils.isEmpty(title))
        {
            Toast.makeText(this, "Enter title...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description))
        {
            Toast.makeText(this, "Plz enter descrition...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(SelectedCatagoryTitle))
        {
            Toast.makeText(this, "Pick Catagory first...", Toast.LENGTH_SHORT).show();
        }
        else if(pdfUri == null)
        {
            Toast.makeText(this, "Pick Pdf", Toast.LENGTH_SHORT).show();
        }
        else
        {
            //now upload
            uploadPdfToStorage();
        }

    }

    private void uploadPdfToStorage() {
        //upload data to storage
        Log.d(TAG, "uploadPdfToStorage: uploading Pdf Catagories..");

        progressDialog.setMessage("Uploading Pdf..");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        //path of pdf in storage

        String filePathAndName = "Books/" + timestamp;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: PDF uploaded to Storage...");
                        Log.d(TAG, "onSuccess: getting pdf url");

                        //get pdf url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();

                        //upload to firebas db
                        uploadPdfInfoToDb(uploadedPdfUrl, timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                         progressDialog.dismiss();
                        Log.d(TAG, "onFailure: PDF upload failed due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "PDF upload failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        //upload pdf info to firebase db
        Log.d(TAG, "uploadPdfToStorage: uploading Pdf info to firebase db..");

        progressDialog.setMessage("Uploading Pdf Info...");

        String uid = firebaseAuth.getUid();

        //setup data to upload
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("description", ""+description);
        hashMap.put("catagoryId",""+SelectedCatagoryId);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);

        //db reference
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: uploaded Successfully..");
                        Toast.makeText(PdfAddActivity.this, "uploaded Successfully..", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Failed to upload due to"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void loadPdfCatagories() {
        Log.d(TAG, "loadPdfCatagories: Loading pdf catagories...");
        catagoryTitleArrayList = new ArrayList<>();
        catagoryIdArrayList = new ArrayList<>();
        //dbreferencw to load catagories
        DatabaseReference reference = FirebaseDatabase .getInstance().getReference("catagories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                catagoryTitleArrayList.clear();//clear before adding
                catagoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    String catagoryId = ""+ds.child("id").getValue();
                    String catagoryTitle = ""+ds.child("catagory").getValue();

                    catagoryTitleArrayList.add(catagoryTitle);
                    catagoryIdArrayList.add(catagoryTitle);


//                    Log.d(TAG, "onDataChanged: "+model.getCatagory());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private String SelectedCatagoryId, SelectedCatagoryTitle;

    private void catagoryPickDialog() {
        Log.d(TAG, "catagoryPickDialog: showing catagory pick dialog");
        String[] catagoriesArray = new String[catagoryTitleArrayList.size()];
        for (int i = 0; i< catagoryTitleArrayList.size(); i++)
        {
            catagoriesArray[i] = catagoryTitleArrayList.get(i);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick Catagory")
                    .setItems(catagoriesArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //handle item click
                            //getclick item from list
                            SelectedCatagoryTitle = catagoryTitleArrayList.get(which);
                            SelectedCatagoryId = catagoryIdArrayList.get(which);

                            //set to catagory text view
                            binding.catagoryTv.setText(SelectedCatagoryTitle);

                            Log.d(TAG, "onClick: Selected Catagory: "+SelectedCatagoryId+" "+SelectedCatagoryTitle);

                        }
                    })
                    .show();

        }
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PDF_PICK_CODE ) ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            if(requestCode == PDF_PICK_CODE)
            {
                Log.d(TAG, "onActivityResult: PDF Picked");
                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: URI:"+pdfUri);


            }
        }
        else
        {
            Log.d(TAG, "onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }

    }
}