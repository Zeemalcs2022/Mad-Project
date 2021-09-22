package com.example.bookapp.filters;

import android.widget.Filter;

import com.example.bookapp.adapter.AdapterCatagory;
import com.example.bookapp.models.ModelCatagory;

import java.util.ArrayList;

public class FilterBookCatagory extends Filter {
    //arraylist in which we want to search
    ArrayList<ModelCatagory> filterList;
    AdapterCatagory adapterCatagory;

    //contructor

    public FilterBookCatagory(ArrayList<ModelCatagory> filterList, AdapterCatagory adapterCatagory) {
        this.filterList = filterList;
        this.adapterCatagory = adapterCatagory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not null
        if(constraint != null && constraint.length() >0)
        {
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCatagory> filteredModels = new ArrayList<>();
            for (int i=0; 1<filterList.size(); i++)
            {
                if(filterList.get(i).getCatagory().toUpperCase().contains(constraint))
                {
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else
        {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterCatagory.catagoryArrayList = (ArrayList<ModelCatagory>)results.values;
        adapterCatagory.notifyDataSetChanged();

    }
}
