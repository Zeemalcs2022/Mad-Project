package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityPdfEditBinding;
import com.example.bookapp.databinding.ActivityPdfListAdminBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {

    private ActivityPdfEditBinding binding;

    private String bookId;

    private ProgressDialog progressDialog;

    private ArrayList<String> catagoryTitleArrayList , catagoryIdArrayList;

    private static final String TAG = "BOOK_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);
        loadCatagories();
        loadbookinfo();

        binding.catagoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catagoryDialog();
            }
        });



        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatedata();

            }
        });


    }
    private String title="",description="";
    private void validatedata()
    {
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        if(TextUtils.isEmpty(title))
        {
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description))
        {
            Toast.makeText(this, "ENter description...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(selectedCatagoryId))
        {
            Toast.makeText(this, "Pick Catagory...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            updatepdf();
        }
    }
    private void updatepdf()
    {
        Log.d(TAG, "updatepdf: Starting updating pdf");
        progressDialog.setMessage("updating info");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("id",""+selectedCatagoryId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Book updated...");
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, "Book Info Updated...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update..."+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadbookinfo() {
        Log.d(TAG, "loadbookinfo: Loading book info...");

        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedCatagoryId = ""+snapshot.child("id").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String title = ""+snapshot.child("title").getValue();
                        binding.titleEt .setText(title);
                        binding.descriptionEt.setText(description);

                        Log.d(TAG, "onDataChanged: Loading Book Catagory..");
                        DatabaseReference refBookCatagory = FirebaseDatabase.getInstance().getReference("catagories");
                        refBookCatagory.child(selectedCatagoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String catagory = ""+snapshot.child("catagory").getValue();
                                        binding.catagoryTv.setText(catagory);


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String selectedCatagoryId="", selectedCatagoryTitle="";
    private void catagoryDialog(){
        String[] catagoriesArray = new String[catagoryTitleArrayList.size()];
        for (int i=0; i<catagoryTitleArrayList.size();i++)
        {
            catagoriesArray[i] = catagoryTitleArrayList.get(i);

        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("CHoose Catagory")
                .setItems(catagoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCatagoryId = catagoryIdArrayList.get(which);
                        selectedCatagoryTitle = catagoryTitleArrayList.get(which);

                        binding.catagoryTv.setText(selectedCatagoryTitle);

                    }
                })
                .show();
    }
    private void loadCatagories() {
        Log.d(TAG, "LoadCatagories: Loading Catagories...");
        catagoryIdArrayList = new ArrayList<>();
        catagoryTitleArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("catagories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                catagoryIdArrayList.clear();
                catagoryTitleArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String id = ""+ds.child("id").getValue();
                    String catagory = ""+ds.child("catagory").getValue();
                    catagoryIdArrayList.add(id);
                    catagoryTitleArrayList.add(catagory);

                    Log.d(TAG, "onDataChanged: ID: "+id);
                    Log.d(TAG, "onDataChanged: catagory: "+catagory);




                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
}