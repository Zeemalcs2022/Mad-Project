package com.example.bookapp.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.PdfEditActivity;
import com.example.bookapp.databinding.RowPdfAdminBinding;
import com.example.bookapp.filters.FilterPdfAdmin;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;

import static com.example.bookapp.Constants.MAX_BYTES_PDF;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {


    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private RowPdfAdminBinding binding;
    private static final String TAG = "PDF_ADAPTER_TAG";
    private FilterPdfAdmin filter;
    private ProgressDialog progressDialog;

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
        Log.i(TAG, "AdapterPdfAdmin: "+pdfArrayList.size());
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        ModelPdf model  = pdfArrayList.get(position);
        String title  = model.getTitle();
        String description = model.getDescription();
        long timestamp = model.getTimestamp();

        String formattedDate = MyApplication.formatTimestamp(timestamp);
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        loadCatagory(model, holder);
        loadPdffromUrl(model, holder);
        loadPdfSize(model, holder);

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionDialog(model, holder);
            }
        });


        


    }

    private void moreOptionDialog(ModelPdf model, HolderPdfAdmin holder) {
        String bookId = model.getId();
        String boookUrl = model.getUrl();
        String bookTitle = model.getTitle();
        String[] options = {"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Option")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0)
                        {
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId", bookId);
                            context.startActivity(intent);


                        }
                        else if(which ==1)
                        {
                            deleteBook(model, holder);
                        }

                    }
                })
                .show();
    }

    private void deleteBook(ModelPdf model, HolderPdfAdmin holder) {
        String bookId = model.getId();
        String boookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        Log.d(TAG, "deleteBook: Deleting...");
        progressDialog.setMessage("Deleting"+bookTitle+"...");
        progressDialog.show();

        Log.d(TAG, "deleteBook: Deleting from Storage...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(boookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Delete from storage");
                        Log.d(TAG, "deleteBook: Now deleting info from db");

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSucccess: Delete from db too...:)");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Book delete successfully", Toast.LENGTH_SHORT).show();



                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Log.d(TAG, "onFailure: failed to delete due to..."+e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();



                                    }
                                });




                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Log.d(TAG, "onFailure: failed to delete from storage due to..."+e.getMessage());
                        progressDialog.dismiss();



                    }
                });



    }

    private void loadPdfSize(ModelPdf model, HolderPdfAdmin holder)
    {
        String pdfUrl=model.getUrl();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {

                        double bytes = new StorageMetadata().getSizeBytes();
                        Log.d(TAG, "onSuccess: "+model.getTitle()+ " "+ bytes);
                        double kb = bytes/1024;
                        double mb = kb/1024;
                        if(mb >=1)
                        {
                            holder.sizeTv.setText(String.format("%.2f", mb)+"MB");
                        }
                        else if(kb >=1)
                        {
                            holder.sizeTv.setText(String.format("%.2f",kb)+ "KB");
                        }
                        else
                        {
                            holder.sizeTv.setText(String.format("%.2f",bytes)+"bytes");
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());


                    }
                });
    }

    private void loadPdffromUrl(ModelPdf model, HolderPdfAdmin holder) {
        String pdfUrl = model.getUrl();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG,"onSuccess: "+model.getTitle()+ "successfully got the file");
                        holder.pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG,"onError : "+t.getMessage());

                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG,"onPageError : "+t.getMessage());

                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "LoadCompletely: pdf loaded ");
                                    }
                                })
                                .load();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG,"onFailure: failed getting file from url due to "+e.getMessage());


                    }
                });
    }

    private void loadCatagory(ModelPdf model, HolderPdfAdmin holder) {
        /*String catagoryId = model.getCatagoryId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("catagories");
        ref.child(catagoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String catagory = ""+snapshot.child("catagory").getValue();
                        holder.catagoryTv.setText(catagory);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });*/
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null)
        {
            filter = new FilterPdfAdmin(filterList, this);

        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv,catagoryTv,sizeTv,dateTv;
        ImageButton moreBtn;



        

        public HolderPdfAdmin(@NonNull View itemView) {

            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressbar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            catagoryTv = binding.catagoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.morebtn;



        }
    }
}
