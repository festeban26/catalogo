package com.dulacs.festeban26;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class CustomGridViewAdapter extends BaseAdapter {
    private ArrayList<ProductModel> mProductsArrayList;

    CustomGridViewAdapter(ArrayList<ProductModel> productsArrayList) {
        mProductsArrayList = productsArrayList;
    }

    @Override
    public int getCount() {
        return mProductsArrayList.size();
    }

    @Override
    public ProductModel getItem(int i) {
        return mProductsArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, final ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.product_cell, parent, false);
        }
        ImageView productImage = convertView.findViewById(R.id.imageView_productCell_ProductImage);
        TextView productName = convertView.findViewById(R.id.textView_productCell_ProductName);
        TextView productFlavour = convertView.findViewById(R.id.textView_productCell_ProductFlavour);
        TextView productPackaging = convertView.findViewById(R.id.textView_productCell_ProductPackaging);

        ProductModel productModel = mProductsArrayList.get(i);

        productName.setText(productModel.getName());
        productFlavour.setText(productModel.getFlavour());
        productPackaging.setText(productModel.getPackaging());

        File internalDirectory = parent.getContext().getFilesDir();
        File imagesDirectory = new File(internalDirectory, "products_images");
        String imageFileName = productModel.getSapCode() + ".webp";
        File imageFile = new File(imagesDirectory, imageFileName);
        if (imageFile.exists())
            Glide.with(parent.getContext())
                    .load(imageFile)
                    .into(productImage);
        /*
        String imagePath = "file:///android_asset/products_images/" + productModel.getSapCode() + ".webp";
        Glide.with(parent.getContext())
                .load(Uri.parse(imagePath))
                .into(productImage);*/

        return convertView;
    }

    public void updateProductsArray(ArrayList<ProductModel> items) {
        mProductsArrayList.clear();
        mProductsArrayList.addAll(items);
        notifyDataSetChanged();
    }
}
