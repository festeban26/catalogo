package com.dulacs.festeban26;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A lot of things to improve.
 */
public class ResourcesManager {

    public static String PRODUCTS_DATABASE_FILENAME = "products_database.json";
    public static String PRODUCTS_DATA_FILENAME = "products_data.json";
    private static String ZIP_ULR = "https://github.com/festeban26/inprolac/archive/master.zip";
    private static String ZIP_ULR_TEXT_TO_BE_REPLACED = "inprolac-master/";
    private static int BUFFER_SIZE = 1024 * 8;
    private static File sFilesDir;

    private static volatile ResourcesManager sResourcesManagerInstance;

    public static synchronized ResourcesManager getInstance() {
        if (sResourcesManagerInstance == null) {
            synchronized (ResourcesManager.class) {
                if (sResourcesManagerInstance == null)
                    sResourcesManagerInstance = new ResourcesManager();
            }
        }
        return sResourcesManagerInstance;
    }

    public void setFilesDir(File filesDir) {
        sFilesDir = filesDir;
    }

    private static String getStringRepresentationFromInternalStorage(String filename) {

        File file = new File(sFilesDir, filename);
        StringBuilder builder = new StringBuilder();
        String line;

        try (FileInputStream fis = new FileInputStream(file)) {
            try (InputStreamReader isr = new InputStreamReader(fis)) {
                try (BufferedReader bufferedReader = new BufferedReader(isr)) {
                    while ((line = bufferedReader.readLine()) != null)
                        builder.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return builder.toString();
    }

    private static JsonObject getJsonObjectFromDisk(String filename) {
        return new Gson().fromJson(getStringRepresentationFromInternalStorage(filename), JsonObject.class);
    }

    public static JsonArray getJsonArrayFromDisk(String filename) {
        return new Gson().fromJson(getStringRepresentationFromInternalStorage(filename), JsonArray.class);
    }

    public void getCategoriesArrays(final OnGetCategoriesArraysListener delegate) {
        new GetCategoriesArrays(new OnGetCategoriesArraysListener() {
            @Override
            public void onProductsLoaded(ArrayList<CategoryModel> categories) {
                delegate.onProductsLoaded(categories);
            }
        }).execute();
    }

    public void getProductsInCategory(String category, final OnGetProductsInCategoryListener delegate) {
        new GetProductsInCategory(category, new OnGetProductsInCategoryListener() {
            @Override
            public void onProductsLoaded(ArrayList<ProductModel> products) {
                delegate.onProductsLoaded(products);
            }
        }).execute();
    }

    public void getProduct(String sapCode, final OnGetProductListener delegate) {
        new GetProduct(sapCode, new OnGetProductListener() {
            @Override
            public void onProductLoaded(ProductModel product) {
                delegate.onProductLoaded(product);
            }
        }).execute();
    }

    public void getRelatedProducts(ProductModel product, final OnGetRelatedProductsListener delegate) {
        new GetRelatedProducts(product, new OnGetRelatedProductsListener() {
            @Override
            public void onProductsLoaded(ArrayList<ProductModel> relatedProducts) {
                delegate.onProductsLoaded(relatedProducts);
            }
        }).execute();
    }

    public void getProductsInTheSameSubGroup(ProductModel forProduct, final OnGetProductsInTheSameSubGroupListener delegate) {
        new GetProductsInTheSameSubGroup(forProduct, new OnGetProductsInTheSameSubGroupListener() {
            @Override
            public void onProductsLoaded(ArrayList<ProductModel> subgroupProducts) {
                delegate.onProductsLoaded(subgroupProducts);
            }
        }).execute();
    }

    public void getJsonObjectFromDisk(String filename, final OnLoadJsonResourceListener delegate) {
        new GetJsonObjectFromDisk(filename, new OnLoadJsonResourceListener() {
            @Override
            public void onJsonObjectLoaded(JsonObject jsonObject) {
                delegate.onJsonObjectLoaded(jsonObject);
            }
        }).execute();
    }

    public void updateApplicationData(final OnUpdateApplicationDataListener delegate) {
        new UpdateApplicationData(new OnUpdateApplicationDataListener() {
            @Override
            public void onDataSuccessfullyUpdated(boolean successfullyCompleted) {
                delegate.onDataSuccessfullyUpdated(successfullyCompleted);
            }
        }).execute();
    }

    /**
     * For the following classes and considering the Android Studio warning: Leaks will not occur!
     * There are no references to an Activity,  Service or any other UI component. The references
     * are pointing to the Resource Manager Class, which is a class that should and it is designed
     * to live as long as the application is running.
     */

    private class UpdateApplicationData extends AsyncTask<Void, Void, Boolean> {
        private OnUpdateApplicationDataListener mDelegate;

        UpdateApplicationData(OnUpdateApplicationDataListener delegate) {
            mDelegate = delegate;
        }


        @Override
        protected Boolean doInBackground(Void... voids) {
            return downloadZip() && unzipZipFile();
        }

        @Override
        protected void onPostExecute(Boolean successfullyCompleted) {
            mDelegate.onDataSuccessfullyUpdated(Boolean.TRUE.equals(successfullyCompleted));
        }

        boolean deleteDirectory(File directoryToBeDeleted) {
            File[] allContents = directoryToBeDeleted.listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    deleteDirectory(file);
                }
            }
            return directoryToBeDeleted.delete();
        }

        private boolean unzipZipFile() {

            File productsDatabaseFile = new File(sFilesDir, "master.zip");
            byte buffer[] = new byte[BUFFER_SIZE];

            try (FileInputStream fis = new FileInputStream(productsDatabaseFile)) {
                try (ZipInputStream zis = new ZipInputStream(fis)) {
                    ZipEntry zipEntry;
                    while ((zipEntry = zis.getNextEntry()) != null) {
                        // What it is being replaced is the "inprolac-master/"
                        String entry = zipEntry.getName().replace(ZIP_ULR_TEXT_TO_BE_REPLACED, "").trim();
                        if (entry.isEmpty())
                            continue;

                        File entryFile = new File(sFilesDir, entry);
                        if (entryFile.exists())
                            deleteDirectory(entryFile);

                        if (zipEntry.isDirectory())
                            entryFile.mkdir();
                        else {
                            try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                                try (BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                                    int length;
                                    while ((length = zis.read(buffer)) != -1)
                                        bos.write(buffer, 0, length);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        private boolean downloadZip() {

            File productsDatabaseFile = new File(sFilesDir, "master.zip");
            byte buffer[] = new byte[BUFFER_SIZE];

            try {
                URL url = new URL(ZIP_ULR);
                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();

                try (InputStream inputStream = url.openStream()) {
                    if (productsDatabaseFile.exists())
                        productsDatabaseFile.delete();
                    try (FileOutputStream fos = new FileOutputStream(productsDatabaseFile)) {
                        try (BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                            int bytes;
                            while ((bytes = inputStream.read(buffer)) != -1)
                                bos.write(buffer, 0, bytes);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    private class GetCategoriesArrays extends AsyncTask<Void, Void, ArrayList<CategoryModel>> {

        private OnGetCategoriesArraysListener mDelegate;

        GetCategoriesArrays(OnGetCategoriesArraysListener delegate) {
            mDelegate = delegate;
        }

        @Override
        protected ArrayList<CategoryModel> doInBackground(Void... voids) {

            ArrayList<CategoryModel> categories = new ArrayList<>();
            HashMap<String, Integer> registeredCategoriesIndexes = new HashMap<>();
            JsonArray productsAsJsonArray = getJsonArrayFromDisk(PRODUCTS_DATABASE_FILENAME);

            for (JsonElement element : productsAsJsonArray) {
                JsonObject obj = element.getAsJsonObject();

                String productCategory = obj.get("group").isJsonNull() ? null : obj.get("group").getAsString();
                String productSubCategory = obj.get("subGroup").isJsonNull() ? null : obj.get("subGroup").getAsString();
                String productSapCode = obj.get("sapCode").isJsonNull() ? null : obj.get("sapCode").getAsString();
                long productBarcode = obj.get("barcode").isJsonNull() ? 0 : obj.get("barcode").getAsLong();
                String productName = obj.get("name").isJsonNull() ? null : obj.get("name").getAsString();
                String productFlavour = obj.get("flavour").isJsonNull() ? null : obj.get("flavour").getAsString();
                String productPackaging = obj.get("packaging").isJsonNull() ? null : obj.get("packaging").getAsString();
                ProductModel product = new ProductModel(productCategory, productSubCategory,
                        productSapCode, productBarcode, productName, productFlavour, productPackaging);

                if (registeredCategoriesIndexes.containsKey(productCategory)) {
                    CategoryModel categoryModel = categories.get(registeredCategoriesIndexes.get(productCategory));
                    categoryModel.addProduct(product);
                } else {
                    CategoryModel categoryModel = new CategoryModel(productCategory);
                    categoryModel.addProduct(product);
                    categories.add(categoryModel);
                    registeredCategoriesIndexes.put(productCategory, categories.indexOf(categoryModel));
                }

            }
            return categories;
        }

        @Override
        protected void onPostExecute(ArrayList<CategoryModel> categories) {
            mDelegate.onProductsLoaded(categories);
        }
    }

    private class GetProductsInCategory extends AsyncTask<Void, Void, ArrayList<ProductModel>> {

        private String mCategory;
        private OnGetProductsInCategoryListener mDelegate;

        GetProductsInCategory(String category, OnGetProductsInCategoryListener delegate) {
            mCategory = category;
            mDelegate = delegate;
        }

        @Override
        protected ArrayList<ProductModel> doInBackground(Void... voids) {

            ArrayList<ProductModel> products = new ArrayList<>();
            JsonArray productsAsJsonArray = getJsonArrayFromDisk(PRODUCTS_DATABASE_FILENAME);

            for (JsonElement element : productsAsJsonArray) {
                JsonObject obj = element.getAsJsonObject();
                String category = obj.get("group").isJsonNull() ? "" : obj.get("group").getAsString();
                if (category.equals(mCategory)) {
                    String subCategory = obj.get("subGroup").isJsonNull() ? null : obj.get("subGroup").getAsString();
                    String sapCode = obj.get("sapCode").isJsonNull() ? null : obj.get("sapCode").getAsString();
                    long barcode = obj.get("barcode").isJsonNull() ? 0 : obj.get("barcode").getAsLong();
                    String name = obj.get("name").isJsonNull() ? null : obj.get("name").getAsString();
                    String flavour = obj.get("flavour").isJsonNull() ? null : obj.get("flavour").getAsString();
                    String packaging = obj.get("packaging").isJsonNull() ? null : obj.get("packaging").getAsString();
                    products.add(new ProductModel(category, subCategory, sapCode, barcode, name, flavour, packaging));
                }
            }
            return products;
        }

        @Override
        protected void onPostExecute(ArrayList<ProductModel> products) {
            mDelegate.onProductsLoaded(products);
        }
    }

    private class GetProduct extends AsyncTask<Void, Void, ProductModel> {

        private String mSapCode;
        private OnGetProductListener mDelegate;

        GetProduct(String sapCode, OnGetProductListener delegate) {
            mSapCode = sapCode;
            mDelegate = delegate;
        }

        @Override
        protected ProductModel doInBackground(Void... voids) {

            JsonArray productsAsJsonArray = getJsonArrayFromDisk(PRODUCTS_DATABASE_FILENAME);

            // First get the product with the mProductSapCode and add it to the array list
            int index = Database.getInstance().getProductIndex(mSapCode);
            if (index >= 0 && index < productsAsJsonArray.size()) {
                JsonObject obj = productsAsJsonArray.get(index).getAsJsonObject();

                String sapCode = obj.get("sapCode").isJsonNull() ? null : obj.get("sapCode").getAsString();
                long barcode = obj.get("barcode").isJsonNull() ? 0 : obj.get("barcode").getAsLong();
                String category = obj.get("group").isJsonNull() ? null : obj.get("group").getAsString();
                String subCategory = obj.get("subGroup").isJsonNull() ? null : obj.get("subGroup").getAsString();
                String name = obj.get("name").isJsonNull() ? null : obj.get("name").getAsString();
                String flavour = obj.get("flavour").isJsonNull() ? null : obj.get("flavour").getAsString();
                String packaging = obj.get("packaging").isJsonNull() ? null : obj.get("packaging").getAsString();

                return new ProductModel(category, subCategory, sapCode, barcode, name, flavour, packaging);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ProductModel product) {
            mDelegate.onProductLoaded(product);
        }
    }

    private class GetRelatedProducts extends AsyncTask<Void, Void, ArrayList<ProductModel>> {

        private ProductModel mProduct;
        private OnGetRelatedProductsListener mDelegate;

        GetRelatedProducts(ProductModel product, OnGetRelatedProductsListener delegate) {
            mProduct = product;
            mDelegate = delegate;
        }

        @Override
        protected ArrayList<ProductModel> doInBackground(Void... voids) {

            ArrayList<ProductModel> relatedProducts = new ArrayList<>();
            JsonArray productsAsJsonArray = getJsonArrayFromDisk(PRODUCTS_DATABASE_FILENAME);

            for (JsonElement element : productsAsJsonArray) {
                JsonObject object = element.getAsJsonObject();
                // r stands for test object
                String tCategory = object.get("group").isJsonNull() ? "" : object.get("group").getAsString();
                if (tCategory.equals(mProduct.getCategory())) {
                    String tSubCategory = object.get("subGroup").isJsonNull() ? "" : object.get("subGroup").getAsString();
                    if (tSubCategory.equals(mProduct.getSubcategory())) {
                        String tName = object.get("name").isJsonNull() ? "" : object.get("name").getAsString();
                        if (tName.equals(mProduct.getName())) {
                            String tPackaging = object.get("packaging").isJsonNull() ? "" : object.get("packaging").getAsString();
                            if (tPackaging.equals(mProduct.getPackaging())) {
                                String tSapCode = object.get("sapCode").isJsonNull() ? "" : object.get("sapCode").getAsString();
                                if (!tSapCode.equals(mProduct.getSapCode())) {
                                    long tBarcode = object.get("barcode").isJsonNull() ? 0 : object.get("barcode").getAsLong();
                                    String tFlavour = object.get("flavour").isJsonNull() ? "" : object.get("flavour").getAsString();
                                    relatedProducts.add(new ProductModel(tCategory, tSubCategory, tSapCode,
                                            tBarcode, tName, tFlavour, tPackaging));
                                }
                            }
                        }
                    }
                }
            }

            return relatedProducts;
        }

        @Override
        protected void onPostExecute(ArrayList<ProductModel> relatedProducts) {
            mDelegate.onProductsLoaded(relatedProducts);
        }
    }

    private class GetJsonObjectFromDisk extends AsyncTask<Void, Void, JsonObject> {

        private String mJsonObjectFileName;
        private OnLoadJsonResourceListener mDelegate;

        GetJsonObjectFromDisk(String fileName, OnLoadJsonResourceListener delegate) {
            mJsonObjectFileName = fileName;
            mDelegate = delegate;
        }

        @Override
        protected JsonObject doInBackground(Void... voids) {
            return getJsonObjectFromDisk(mJsonObjectFileName);
        }

        @Override
        protected void onPostExecute(JsonObject jsonObject) {
            mDelegate.onJsonObjectLoaded(jsonObject);
        }
    }

    private class GetProductsInTheSameSubGroup extends AsyncTask<Void, Void, ArrayList<ProductModel>> {

        private OnGetProductsInTheSameSubGroupListener mDelegate;
        private ProductModel mProduct;

        GetProductsInTheSameSubGroup(ProductModel forProductModel, OnGetProductsInTheSameSubGroupListener delegate) {
            mDelegate = delegate;
            mProduct = forProductModel;
        }

        @Override
        protected ArrayList<ProductModel> doInBackground(Void... voids) {

            ArrayList<ProductModel> subgroupProducts = new ArrayList<>();
            JsonArray productsAsJsonArray = getJsonArrayFromDisk(PRODUCTS_DATABASE_FILENAME);

            for (JsonElement element : productsAsJsonArray) {
                JsonObject object = element.getAsJsonObject();

                String tCategory = object.get("group").isJsonNull() ? "" : object.get("group").getAsString();
                if (tCategory.equals(mProduct.getCategory())) {
                    String tSubCategory = object.get("subGroup").isJsonNull() ? "" : object.get("subGroup").getAsString();
                    if (tSubCategory.equals(mProduct.getSubcategory())) {
                        String tSapCode = object.get("sapCode").isJsonNull() ? "" : object.get("sapCode").getAsString();
                        if (!tSapCode.equals(mProduct.getSapCode())) {
                            String tName = object.get("name").isJsonNull() ? "" : object.get("name").getAsString();
                            String tPackaging = object.get("packaging").isJsonNull() ? null : object.get("packaging").getAsString();
                            long tBarcode = object.get("barcode").isJsonNull() ? 0 : object.get("barcode").getAsLong();
                            String tFlavour = object.get("flavour").isJsonNull() ? null : object.get("flavour").getAsString();
                            subgroupProducts.add(new ProductModel(tCategory, tSubCategory, tSapCode,
                                    tBarcode, tName, tFlavour, tPackaging));
                        }
                    }
                }
            }
            return subgroupProducts;
        }

        @Override
        protected void onPostExecute(ArrayList<ProductModel> relatedProducts) {
            mDelegate.onProductsLoaded(relatedProducts);
        }
    }

    public interface OnLoadJsonResourceListener {
        void onJsonObjectLoaded(JsonObject jsonObject);
    }

    public interface OnGetProductListener {
        void onProductLoaded(ProductModel product);
    }

    public interface OnGetCategoriesArraysListener {
        void onProductsLoaded(ArrayList<CategoryModel> categories);
    }

    public interface OnGetProductsInCategoryListener {
        void onProductsLoaded(ArrayList<ProductModel> products);
    }

    public interface OnGetRelatedProductsListener {
        void onProductsLoaded(ArrayList<ProductModel> relatedProducts);
    }

    public interface OnGetProductsInTheSameSubGroupListener {
        void onProductsLoaded(ArrayList<ProductModel> subgroupProducts);
    }

    public interface OnUpdateApplicationDataListener {
        void onDataSuccessfullyUpdated(boolean successfullyCompleted);
    }
}