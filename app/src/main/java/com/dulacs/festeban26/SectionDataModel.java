package com.dulacs.festeban26;

import java.util.ArrayList;

public class SectionDataModel {
    private String headerTitle;
    private ArrayList<ProductModel> allItemInSection;

    public SectionDataModel(String headerTitle, ArrayList<ProductModel> allItemInSection) {
        this.headerTitle = headerTitle;
        this.allItemInSection = allItemInSection;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public ArrayList<ProductModel> getAllItemInSection() {
        return allItemInSection;
    }

    public void setAllItemInSection(ArrayList<ProductModel> allItemInSection) {
        this.allItemInSection = allItemInSection;
    }
}