package com.dulacs.festeban26;

import java.util.ArrayList;

public class CategoryModel {

    private String mCategoryName;
    private ArrayList<ProductModel> mProducts;


    public CategoryModel(String categoryName) {
        mCategoryName = categoryName;
        mProducts = new ArrayList<>();
    }

    public String getCategoryName() {
        return mCategoryName;
    }

    public void addProduct(ProductModel product) {
        mProducts.add(product);
    }

    public ArrayList<ProductModel> getProducts() {
        return mProducts;
    }
}
