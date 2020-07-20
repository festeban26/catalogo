package com.dulacs.festeban26;

public class ProductModel {

    /**
     * Things that can be improved for memory performance:
     * sapcode could be an char array instead of string
     * barcode could be an char array instead of string
     */
    private String mCategory, mSubcategory, mSapCode, mName, mFlavour, mPackaging;
    private long mBarcode;


    public ProductModel(String group, String subGroup, String sapCode, long barcode,
                        String name, String flavour, String packaging) {
        mCategory = group;
        mSubcategory = subGroup;
        mSapCode = sapCode;
        mBarcode = barcode;
        mName = name;
        mFlavour = flavour;
        mPackaging = packaging;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getSubcategory() {
        return mSubcategory;
    }

    public String getSapCode() {
        return mSapCode;
    }

    public long getBarcode() {
        return mBarcode;
    }

    public String getName() {
        return mName;
    }

    public String getFlavour() {
        if (mFlavour != null)
            if (mFlavour.isEmpty())
                return null;
        return mFlavour;
    }

    public String getPackaging() {
        return mPackaging;
    }
}
