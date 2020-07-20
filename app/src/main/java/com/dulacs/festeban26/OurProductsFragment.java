package com.dulacs.festeban26;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class OurProductsFragment extends Fragment {

    private ArrayList<SectionDataModel> mAllSampleData;
    private RecyclerViewDataAdapter mAdapter;

    public OurProductsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAllSampleData = new ArrayList<>();
        mAdapter = new RecyclerViewDataAdapter(mAllSampleData);

        ResourcesManager.getInstance().getCategoriesArrays(new ResourcesManager.OnGetCategoriesArraysListener() {
            @Override
            public void onProductsLoaded(ArrayList<CategoryModel> categories) {
                createData(categories);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ourproducts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((HomeActivity) getActivity()).setActionBarTitle(getString(R.string.navActionBar_OurProducts));

        RecyclerView recyclerView = view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);
    }

    private void createData(ArrayList<CategoryModel> categories) {

        for (CategoryModel category : categories) {
            SectionDataModel dm = new SectionDataModel(category.getCategoryName(), category.getProducts());
            mAllSampleData.add(dm);
        }
        mAdapter.notifyDataSetChanged();
    }
}
