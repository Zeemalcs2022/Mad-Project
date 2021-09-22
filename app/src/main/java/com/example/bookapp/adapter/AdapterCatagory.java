package com.example.bookapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.PdfListAdminActivity;
import com.example.bookapp.filters.FilterBookCatagory;
import com.example.bookapp.models.ModelCatagory;
import com.example.bookapp.databinding.RowCatagoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AdapterCatagory extends RecyclerView.Adapter<AdapterCatagory.HolderCatagory> implements Filterable {
    private Context context;
    public ArrayList<ModelCatagory> catagoryArrayList, filterList;


    private RowCatagoryBinding binding;

    private FilterBookCatagory filter;



    public AdapterCatagory(Context context, ArrayList<ModelCatagory> catagoryArrayList) {
        this.context = context;
        this.catagoryArrayList = catagoryArrayList;
        this.filterList = catagoryArrayList;
    }

    @Override
    public HolderCatagory onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = RowCatagoryBinding.inflate(LayoutInflater.from(context),parent, false);
        return new HolderCatagory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(HolderCatagory holder, int position) {
        ModelCatagory model = catagoryArrayList.get(position);
        String id = model.getId();
        String catagory = model.getCatagory();
        String uid = model.getUid();
        long timestamp = model.getTimestamp();

        holder.catagorytv.setText(catagory);
        holder.deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //confirm delete
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this book!!")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(context, "Deleting file...", Toast.LENGTH_SHORT).show();
                                deleteBook(model, holder);



                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();


                            }
                        })
                        .show();
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("catagoryId", id);
                intent.putExtra("catagoryTitle", catagory);
                context.startActivity(intent);
            }
        });

    }




    private void deleteBook(ModelCatagory model, HolderCatagory holder) {
        String id = model.getId();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("catagories");
        reference.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //delete Successfull
                        Toast.makeText(context, "Successfully deleted....", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        //failure
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public int getItemCount() {
        return catagoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null)
        {
                filter = new FilterBookCatagory(filterList, this);

        }
        return filter;
    }

    class HolderCatagory extends RecyclerView.ViewHolder{
        TextView catagorytv;
        ImageButton deletebtn;


        public HolderCatagory(View itemView) {
            super(itemView);
            catagorytv = binding.catagorytv;
            deletebtn = binding.delbtn;
        }
    }
}
