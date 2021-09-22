package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import com.example.bookapp.adapter.AdapterCatagory;
import com.example.bookapp.databinding.ActivityDashboardAdminBinding;
import com.example.bookapp.models.ModelCatagory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private ActivityDashboardAdminBinding binding;
    private ArrayList<ModelCatagory> catagoryArrayList;
    private AdapterCatagory adapterCatagory;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpViews();

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadbooks();


        binding.searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try
                {
                    adapterCatagory.getFilter().filter(s);
                }
                catch (Exception e)
                {

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();

            }
        });

        binding.addcatagorybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intemt = new Intent(DashboardAdminActivity.this, CatagoryAddActivity.class);
                startActivity(intemt);

            }
        });
        binding.addPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this, PdfAddActivity.class));
            }
        });
    }

    private void setUpViews()
    {
        setUpDrawerLayout();
    }

    private void setUpDrawerLayout()
    {
        setSupportActionBar(binding.appbar);
        actionBarDrawerToggle =
                new ActionBarDrawerToggle(this,findViewById(R.id.mainDrawer),R.string.app_name, R.string.app_name);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadbooks() {
        catagoryArrayList = new ArrayList<>();


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("catagories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //clear arraylist beforeadding data
                catagoryArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    ModelCatagory modelCatagory = ds.getValue(ModelCatagory.class);
                    catagoryArrayList.add(modelCatagory);
                }
                //setup adapter
                adapterCatagory = new AdapterCatagory(DashboardAdminActivity.this, catagoryArrayList);
                binding.catagoryEt.setAdapter(adapterCatagory);

            }

            @Override
            public void onCancelled(DatabaseError error) {


            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null)
        {
            //not login go to main screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else
        {
            String email = firebaseUser.getEmail();
//            binding.subtitle.setText(email);

        }
    }
}