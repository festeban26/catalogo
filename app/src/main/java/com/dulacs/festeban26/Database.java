package com.dulacs.festeban26;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;

class Database {

    public interface OnInitializeDatabaseListener {
        void databaseReady();
    }

    private Database() {
    }

    private static Database sDatabaseInstance = new Database();

    public static Database getInstance() {
        return sDatabaseInstance;
    }

    @SuppressLint("UseSparseArrays")
    private static HashMap<Long, String> sRegisteredBarcodesHashMap = new HashMap<>();

    private static HashMap<String, Integer> sRegisteredProductsIndexesOnJson = new HashMap<>();


    public boolean containsBarcode(long barcode) {
        return sRegisteredBarcodesHashMap.containsKey(barcode);
    }

    public boolean containsSapCode(String sapCode) {
        return sRegisteredProductsIndexesOnJson.containsKey(sapCode);
    }

    public String getSapCodeForBarcode(long barcode) {
        if (containsBarcode(barcode)) {
            return getRegisteredBarcodesHashMap().get(barcode);
        } else
            return null;
    }

    public int getProductIndex(String productSapCode) {
        if (containsSapCode(productSapCode)) {
            return getProductsIndexesOnJson().get(productSapCode);
        }
        return -1;
    }

    public HashMap<Long, String> getRegisteredBarcodesHashMap() {
        return sRegisteredBarcodesHashMap;
    }


    public HashMap<String, Integer> getProductsIndexesOnJson() {
        return sRegisteredProductsIndexesOnJson;
    }

    public void initializeDatabase(final OnInitializeDatabaseListener delegate) {

        new AsyncTask_LoadProductForemostInformationIntoDatabase(new OnInitializeDatabaseListener() {
            @Override
            public void databaseReady() {
                delegate.databaseReady();
            }
        }).execute();
    }

    private class AsyncTask_LoadProductForemostInformationIntoDatabase extends AsyncTask<Void, Void, Void> {

        private OnInitializeDatabaseListener mDelegate;

        AsyncTask_LoadProductForemostInformationIntoDatabase(OnInitializeDatabaseListener delegate) {
            mDelegate = delegate;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JsonArray productsAsJsonArray = ResourcesManager.getJsonArrayFromDisk(ResourcesManager.PRODUCTS_DATABASE_FILENAME);

            for (int i = 0; i < productsAsJsonArray.size(); i++) {
                JsonObject product = productsAsJsonArray.get(i).getAsJsonObject();
                String sapCode = product.get("sapCode").isJsonNull() ? null : product.get("sapCode").getAsString();
                long barcode = product.get("barcode").isJsonNull() ? 0 : product.get("barcode").getAsLong();

                getProductsIndexesOnJson().put(sapCode, i);
                getRegisteredBarcodesHashMap().put(barcode, sapCode);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mDelegate.databaseReady();
        }
    }
}
