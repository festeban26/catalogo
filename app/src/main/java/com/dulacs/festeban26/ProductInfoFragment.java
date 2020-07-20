package com.dulacs.festeban26;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProductInfoFragment extends Fragment {

    private ImageView mProductImage;
    private TextView mProductNameTextView;
    private TextView mProductSapCodeTextView;
    private LinearLayout mProductFlavourLayout;
    private Spinner mProductFlavourSpinner;
    private ImageView mProductBarcodeImageView;
    private TextView mProductBarcodeTextView;

    private CardView mNutritionInformationCardview;
    private TextView mNutritionLabel_ServingSizeValue, mNutritionLabel_ServingsPerContainerValue, mNutritionLabel_EnergyValue,
            mNutritionLabel_EnergyFromFatValue, mNutritionLabel_TotalFatValue;
    private TextView nutritionLabel_TotalFatPercentage;
    private TextView nutritionLabel_SatFatValue;
    private TextView nutritionLabel_SatFatPercentage;
    private TextView nutritionLabel_TransFatValue;
    private TextView nutritionLabel_TransFatPercentage;
    private TextView nutritionLabel_MonoUnsaturatedFatValue;
    private TextView nutritionLabel_PolyUnsaturatedFatValue;
    private TextView nutritionLabel_CholesterolValue;
    private TextView nutritionLabel_CholesterolPercentage;
    private TextView nutritionLabel_SodiumValue;
    private TextView nutritionLabel_SodiumPercentage;
    private TextView nutritionLabel_TotalCarbValue;
    private TextView nutritionLabel_TotalCarbPercentage;
    private TextView nutritionLabel_FibersValue;
    private TextView nutritionLabel_FibersPercentage;
    private TextView nutritionLabel_SugarsValue;
    private TextView nutritionLabel_ProteinValue;
    private TextView nutritionLabel_ProteinPercentage;
    private TextView nutritionLabel_Ingredients;
    private TextView nutritionLabel_Allergies;
    private View mNutritionLabel_AllergiesView;
    private NestedScrollView mNestedScrollView;

    // Related products CardView's components
    private ExpandableHeightGridView mSameSubgroupProductsGridView;
    private CustomGridViewAdapter mSameSubgroupProductsAdapter;
    private ArrayList<ProductModel> mSameSubGroupProducts;

    private Boolean mIsNutritionInfoLoaded;
    private String mCurrentProductSapCode;
    private ProductModel mCurrentProduct;
    private ArrayList<ProductModel> mCurrentProductRelatedProducts;

    private ArrayList<String> mFlavourSpinnerArrayList;
    private ArrayAdapter<String> mFlavourSpinnerAdapter;
    private Boolean mIsFlavourSpinnerInitialized;

    public ProductInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIsFlavourSpinnerInitialized = false;
        mFlavourSpinnerArrayList = new ArrayList<>();
        mFlavourSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, mFlavourSpinnerArrayList);
        mFlavourSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSameSubGroupProducts = new ArrayList<>();
        mSameSubgroupProductsAdapter = new CustomGridViewAdapter(mSameSubGroupProducts);

        mCurrentProductRelatedProducts = new ArrayList<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsNutritionInfoLoaded = false;
        Bundle args = getArguments();
        if (args != null) {
            String sapCode = args.getString("sapCode");
            long barcode = args.getLong("barcode");
            if (sapCode != null) // String default value = null
                mCurrentProductSapCode = sapCode;
            else if (barcode != 0) // long default value: 0
                mCurrentProductSapCode = Database.getInstance().getSapCodeForBarcode(barcode);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        container.getContext();
        return inflater.inflate(R.layout.fragment_productinformation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // ProductModel Information CardView's components
        View mProductInformationCardview_TitleBar = view.findViewById(R.id.linearLayout_ProductInfoCardview_TitleBar);
        ImageView mProductInformationCardview_TitleBarIcon = view.findViewById(R.id.imageView__ProductInfoCardview_TitleBarIcon);
        View mProductInformationCardview_Content = view.findViewById(R.id.constraintLayout_ProductInfoCardview_Content);

        // Nutritional Information CardView's components
        View mNutritionInfoCardview_TitleBar = view.findViewById(R.id.linearLayout_NutritionInfoCardview_TitleBar);
        ImageView mNutritionInfoCardview_TitleBarIcon = view.findViewById(R.id.imageView__NutritionInfoCardview_TitleBarIcon);
        View mNutritionInfoCardview_Content = view.findViewById(R.id.linearLayout_NutritionInfoCardview_Content);

        // Related Products CardView's components
        View mRelatedProductsCardview_TitleBar = view.findViewById(R.id.linearLayout_RelatedProductsCardview_TitleBar);
        ImageView mRelatedProductsCardview_TitleBarIcon = view.findViewById(R.id.imageView__RelatedProductsCardview_TitleBarIcon);
        View mRelatedProductsCardview_Content = view.findViewById(R.id.linearLayout_RelatedProductsCardview_Content);

        mNutritionInformationCardview = view.findViewById(R.id.cardView_NutritionInfoCardview);

        mNestedScrollView = view.findViewById(R.id.nestedScrollView_ProductInformation);

        mProductImage = view.findViewById(R.id.imageView_ProductInfoCardviewContent_ProductImage);
        mProductNameTextView = view.findViewById(R.id.textView_ProductInfoCardviewContent_ProductName);
        mProductSapCodeTextView = view.findViewById(R.id.textView_ProductInfoCardviewContent_SapCode);
        mProductFlavourLayout = view.findViewById(R.id.linearLayout_ProductInfoCardviewContent_ProductFlavour);
        mProductFlavourSpinner = view.findViewById(R.id.spinner_ProductInfoCardviewContent_ProductFlavour);
        mProductBarcodeImageView = view.findViewById(R.id.imageView_ProductInformationCardviewContent_Barcode);
        mProductBarcodeTextView = view.findViewById(R.id.textView_ProductInformationCardviewContent_Barcode);

        mNutritionLabel_ServingSizeValue = view.findViewById(R.id.textView_NutritionLabel_ServingSizeValue);
        mNutritionLabel_ServingsPerContainerValue = view.findViewById(R.id.textView_NutritionLabel_ServingsPerContainerValue);
        mNutritionLabel_EnergyValue = view.findViewById(R.id.textView_NutritionLabel_EnergyValue);
        mNutritionLabel_EnergyFromFatValue = view.findViewById(R.id.textView_NutritionLabel_EnergyFromFatValue);
        mNutritionLabel_TotalFatValue = view.findViewById(R.id.textView_NutritionLabel_TotalFatValue);
        nutritionLabel_TotalFatPercentage = view.findViewById(R.id.textView_NutritionLabel_TotalFatPercentage);
        nutritionLabel_SatFatValue = view.findViewById(R.id.textView_NutritionLabel_SatFatValue);
        nutritionLabel_SatFatPercentage = view.findViewById(R.id.textView_NutritionLabel_SatFatPercentage);
        nutritionLabel_TransFatValue = view.findViewById(R.id.textView_NutritionLabel_TransFatValue);
        nutritionLabel_TransFatPercentage = view.findViewById(R.id.textView_NutritionLabel_TransFatPercentage);
        nutritionLabel_MonoUnsaturatedFatValue = view.findViewById(R.id.textView_NutritionLabel_MonoUnsaturatedFatValue);
        nutritionLabel_PolyUnsaturatedFatValue = view.findViewById(R.id.textView_NutritionLabel_PolyUnsaturatedFatValue);
        nutritionLabel_CholesterolValue = view.findViewById(R.id.textView_NutritionLabel_CholesterolValue);
        nutritionLabel_CholesterolPercentage = view.findViewById(R.id.textView_NutritionLabel_CholesterolPercentage);
        nutritionLabel_SodiumValue = view.findViewById(R.id.textView_NutritionLabel_SodiumValue);
        nutritionLabel_SodiumPercentage = view.findViewById(R.id.textView_NutritionLabel_SodiumPercentage);
        nutritionLabel_TotalCarbValue = view.findViewById(R.id.textView_NutritionLabel_TotalCarbValue);
        nutritionLabel_TotalCarbPercentage = view.findViewById(R.id.textView_NutritionLabel_TotalCarbPercentage);
        nutritionLabel_FibersValue = view.findViewById(R.id.textView_NutritionLabel_FibersValue);
        nutritionLabel_FibersPercentage = view.findViewById(R.id.textView_NutritionLabel_FibersPercentage);
        nutritionLabel_SugarsValue = view.findViewById(R.id.textView_NutritionLabel_SugarsValue);
        nutritionLabel_ProteinValue = view.findViewById(R.id.textView_NutritionLabel_ProteinValue);
        nutritionLabel_ProteinPercentage = view.findViewById(R.id.textView_NutritionLabel_ProteinPercentage);
        nutritionLabel_Ingredients = view.findViewById(R.id.textView_NutritionInformation_Ingredients);
        nutritionLabel_Allergies = view.findViewById(R.id.textView_NutritionInformation_Allergies);
        mNutritionLabel_AllergiesView = view.findViewById(R.id.cardView_NutritionInformation_Allergies);

        mSameSubgroupProductsGridView = view.findViewById(R.id.grid_view_image_text);


        // Setup each cardview on click listeners
        setupCardviewListener(mProductInformationCardview_TitleBar,
                mProductInformationCardview_Content, mProductInformationCardview_TitleBarIcon);
        setupCardviewListener(mRelatedProductsCardview_TitleBar,
                mRelatedProductsCardview_Content, mRelatedProductsCardview_TitleBarIcon);
        setupNutritionInformationCardviewListener(mNutritionInfoCardview_TitleBar,
                mNutritionInfoCardview_Content, mNutritionInfoCardview_TitleBarIcon);

        mProductFlavourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mIsFlavourSpinnerInitialized) {
                    if (!mCurrentProduct.getFlavour().equals(mFlavourSpinnerArrayList.get(position))) {
                        for (ProductModel relatedProduct : mCurrentProductRelatedProducts) {
                            if (relatedProduct.getFlavour().equals(mFlavourSpinnerArrayList.get(position))) {
                                ProductModel temp = mCurrentProduct;
                                mCurrentProduct = relatedProduct;
                                mCurrentProductRelatedProducts.set(mCurrentProductRelatedProducts.indexOf(relatedProduct), temp);
                                syncContent();
                                return;
                            }
                        }
                    }
                } else
                    mIsFlavourSpinnerInitialized = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mProductFlavourSpinner.setAdapter(mFlavourSpinnerAdapter);

        mSameSubgroupProductsGridView.setExpanded(true);
        mSameSubgroupProductsGridView.setAdapter(mSameSubgroupProductsAdapter);

        mSameSubgroupProductsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for (ProductModel relatedProduct : mCurrentProductRelatedProducts) {
                    if (relatedProduct.getSapCode().equals(mSameSubGroupProducts.get(position).getSapCode())) {
                        ProductModel temp = mCurrentProduct;
                        mCurrentProduct = relatedProduct;
                        int indexOfNewSelectedProduct = mCurrentProductRelatedProducts.indexOf(relatedProduct);
                        mCurrentProductRelatedProducts.set(indexOfNewSelectedProduct, temp);

                        for (int i = 0; i < mFlavourSpinnerArrayList.size(); i++)
                            if (mCurrentProduct.getFlavour().equals(mFlavourSpinnerArrayList.get(i))) {
                                mIsFlavourSpinnerInitialized = false;
                                mProductFlavourSpinner.setSelection(i, true);
                                syncContent();
                                return;
                            }
                    }
                }
                mCurrentProductSapCode = mSameSubGroupProducts.get(position).getSapCode();
                loadProductInformation();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        loadProductInformation();
    }

    private void loadProductInformation() {

        ResourcesManager.getInstance().getProduct(mCurrentProductSapCode, new ResourcesManager.OnGetProductListener() {
            @Override
            public void onProductLoaded(ProductModel product) {
                mCurrentProduct = product;

                ResourcesManager.getInstance().getRelatedProducts(product, new ResourcesManager.OnGetRelatedProductsListener() {
                    @Override
                    public void onProductsLoaded(ArrayList<ProductModel> relatedProducts) {
                        mCurrentProductRelatedProducts = relatedProducts;
                        mFlavourSpinnerArrayList.clear();
                        if (mCurrentProduct.getFlavour() != null)
                            mFlavourSpinnerArrayList.add(mCurrentProduct.getFlavour());
                        for (ProductModel relatedProduct : mCurrentProductRelatedProducts)
                            if (relatedProduct.getFlavour() != null)
                                mFlavourSpinnerArrayList.add(relatedProduct.getFlavour());
                        mFlavourSpinnerAdapter.notifyDataSetChanged();
                        syncContent();
                    }
                });
            }
        });
    }

    public void syncContent() {
        if (mCurrentProduct != null) {

            File internalDirectory = getContext().getFilesDir();
            File imagesDirectory = new File(internalDirectory, "products_images");
            String imageFileName = mCurrentProduct.getSapCode() + ".webp";
            File imageFile = new File(imagesDirectory, imageFileName);
            if (imageFile.exists())
                Glide.with(this)
                        .load(imageFile)
                        .transition(withCrossFade())
                        .into(mProductImage);
            /*
                Glide.with(this)
                        .load(Uri.parse("file:///android_asset/products_images/" + mCurrentProduct.getSapCode() + ".webp"))
                        .transition(withCrossFade())
                        .into(mProductImage);
                        */

            String productNameForDisplay = mCurrentProduct.getName() + " (" + mCurrentProduct.getPackaging() + ")";
            mProductNameTextView.setText(productNameForDisplay);
            mProductSapCodeTextView.setText(mCurrentProduct.getSapCode());

            if (mCurrentProduct.getFlavour() != null) {
                mProductFlavourLayout.setVisibility(View.VISIBLE);
            } else
                mProductFlavourLayout.setVisibility(View.GONE);

            String barcodeText = Long.toString(mCurrentProduct.getBarcode()); // Whatever you need to encode in the QR code
            if (barcodeText.length() == 13) {
                mProductBarcodeTextView.setVisibility(View.VISIBLE);
                mProductBarcodeImageView.setVisibility(View.VISIBLE);
                mProductBarcodeTextView.setText(barcodeText);
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(barcodeText, BarcodeFormat.EAN_13, 500, 180);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    mProductBarcodeImageView.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            } else if (barcodeText.length() > 13) {
                mProductBarcodeTextView.setVisibility(View.VISIBLE);
                mProductBarcodeImageView.setVisibility(View.VISIBLE);
                mProductBarcodeTextView.setText(barcodeText);
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(barcodeText, BarcodeFormat.CODE_128, 500, 180);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    mProductBarcodeImageView.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            } else {
                mProductBarcodeTextView.setVisibility(View.GONE);
                mProductBarcodeImageView.setVisibility(View.GONE);
            }

            fillNutritionLabelCardViewWithDataFromJson();
            updateRelatedProducts();
        }
    }

    private void scrollToTop(){
        mNestedScrollView.fullScroll(View.FOCUS_UP);
        mNestedScrollView.smoothScrollTo(0, 0);
    }

    public void updateRelatedProducts() {

        ResourcesManager.getInstance().getProductsInTheSameSubGroup(mCurrentProduct,
                new ResourcesManager.OnGetProductsInTheSameSubGroupListener() {
                    @Override
                    public void onProductsLoaded(ArrayList<ProductModel> subgroupProducts) {
                        mSameSubgroupProductsAdapter.updateProductsArray(subgroupProducts);
                        scrollToTop();
                    }
                });
    }

    /**
     * Set up a CardView's listener
     *
     * @param titleBar     The CardView's title bar layout
     * @param content      The CardView's content layout
     * @param titleBarIcon The CardView's title bar icon (arrow)
     */
    private void setupCardviewListener(View titleBar, final View content, final ImageView titleBarIcon) {
        titleBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (content.getVisibility() == View.GONE) {
                    content.setVisibility(View.VISIBLE);
                    titleBarIcon.setImageResource(R.drawable.ic_expand_less_black_24dp);
                } else {
                    content.setVisibility(View.GONE);
                    titleBarIcon.setImageResource(R.drawable.ic_expand_more_black_24dp);
                }
            }
        });
    }

    /**
     * Set up a CardView's listener
     *
     * @param titleBar     The CardView's title bar layout
     * @param content      The CardView's content layout
     * @param titleBarIcon The CardView's title bar icon (arrow)
     */
    private void setupNutritionInformationCardviewListener(View titleBar, final View content, final ImageView titleBarIcon) {
        titleBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (content.getVisibility() == View.GONE) {
                    content.setVisibility(View.VISIBLE);
                    titleBarIcon.setImageResource(R.drawable.ic_expand_less_black_24dp);

                    if (!mIsNutritionInfoLoaded) {
                        mIsNutritionInfoLoaded = true;
                    }

                } else {
                    content.setVisibility(View.GONE);
                    titleBarIcon.setImageResource(R.drawable.ic_expand_more_black_24dp);
                }
            }
        });
    }

    private void setText(TextView textView, JsonObject parentJsonObject, String key) {
        if (parentJsonObject.has(key))
            textView.setText(parentJsonObject.get(key).getAsString());
    }

    private void onNutritionLabelDataReady(JsonObject nutritionDataAsJsonObject) {
        if (nutritionDataAsJsonObject.has("products")) {
            JsonArray products = nutritionDataAsJsonObject.get("products").getAsJsonArray();
            for (JsonElement element : products) {
                JsonObject product = element.getAsJsonObject();
                if (product.has("barcode")) {
                    product.has("test");
                    if (product.get("barcode").getAsLong() == mCurrentProduct.getBarcode()) {
                        JsonObject nutritionInfoObj =
                                product.get("nutritionalInformation").getAsJsonObject();

                        // TODO TOP PRIORITY if no nutritional information is avaiable, show "NO NUTRI..." message
                        setText(mNutritionLabel_ServingSizeValue, nutritionInfoObj, "amountPerServing");
                        setText(mNutritionLabel_ServingsPerContainerValue, nutritionInfoObj, "servingsPerContainer");
                        setText(mNutritionLabel_EnergyValue, nutritionInfoObj, "valueCalories");
                        setText(mNutritionLabel_EnergyFromFatValue, nutritionInfoObj, "valueFatCalories");
                        setText(nutritionLabel_Ingredients, product, "ingredients");

                        // Allergies
                        if (product.has("allergies"))
                            nutritionLabel_Allergies.setText(product.get("allergies").getAsString());
                        else
                            mNutritionLabel_AllergiesView.setVisibility(View.GONE);

                        if (nutritionInfoObj.has("totalFat")) {
                            setText(mNutritionLabel_TotalFatValue, nutritionInfoObj.get("totalFat").getAsJsonObject(), "value");
                            setText(nutritionLabel_TotalFatPercentage, nutritionInfoObj.get("totalFat").getAsJsonObject(), "percentage");
                        }
                        if (nutritionInfoObj.has("satFat")) {
                            setText(nutritionLabel_SatFatValue, nutritionInfoObj.get("satFat").getAsJsonObject(), "value");
                            setText(nutritionLabel_SatFatPercentage, nutritionInfoObj.get("satFat").getAsJsonObject(), "percentage");
                        }
                        if (nutritionInfoObj.has("transFat")) {
                            setText(nutritionLabel_TransFatValue, nutritionInfoObj.get("transFat").getAsJsonObject(), "value");
                            setText(nutritionLabel_TransFatPercentage, nutritionInfoObj.get("transFat").getAsJsonObject(), "percentage");
                        }
                        if (nutritionInfoObj.has("monoUnsaturatedFat"))
                            setText(nutritionLabel_MonoUnsaturatedFatValue, nutritionInfoObj.get("monoUnsaturatedFat").getAsJsonObject(), "value");
                        if (nutritionInfoObj.has("polyUnsaturatedFat"))
                            setText(nutritionLabel_PolyUnsaturatedFatValue, nutritionInfoObj.get("polyUnsaturatedFat").getAsJsonObject(), "value");
                        if (nutritionInfoObj.has("cholesterol")) {
                            setText(nutritionLabel_CholesterolValue, nutritionInfoObj.get("cholesterol").getAsJsonObject(), "value");
                            setText(nutritionLabel_CholesterolPercentage, nutritionInfoObj.get("cholesterol").getAsJsonObject(), "percentage");
                        }
                        if (nutritionInfoObj.has("sodium")) {
                            setText(nutritionLabel_SodiumValue, nutritionInfoObj.get("sodium").getAsJsonObject(), "value");
                            setText(nutritionLabel_SodiumPercentage, nutritionInfoObj.get("sodium").getAsJsonObject(), "percentage");
                        }
                        if (nutritionInfoObj.has("totalCarb")) {
                            setText(nutritionLabel_TotalCarbValue, nutritionInfoObj.get("totalCarb").getAsJsonObject(), "value");
                            setText(nutritionLabel_TotalCarbPercentage, nutritionInfoObj.get("totalCarb").getAsJsonObject(), "percentage");
                        }
                        if (nutritionInfoObj.has("fibers")) {
                            setText(nutritionLabel_FibersValue, nutritionInfoObj.get("fibers").getAsJsonObject(), "value");
                            setText(nutritionLabel_FibersPercentage, nutritionInfoObj.get("fibers").getAsJsonObject(), "percentage");
                        }
                        if (nutritionInfoObj.has("sugars"))
                            setText(nutritionLabel_SugarsValue, nutritionInfoObj.get("sugars").getAsJsonObject(), "value");
                        if (nutritionInfoObj.has("proteins")) {
                            setText(nutritionLabel_ProteinValue, nutritionInfoObj.get("proteins").getAsJsonObject(), "value");
                            setText(nutritionLabel_ProteinPercentage, nutritionInfoObj.get("proteins").getAsJsonObject(), "percentage");
                        }
                        mNutritionInformationCardview.setVisibility(View.VISIBLE);
                        return;
                    }
                }
            }
        }
        mNutritionInformationCardview.setVisibility(View.GONE);
    }

    private void fillNutritionLabelCardViewWithDataFromJson() {
        ResourcesManager.getInstance().getJsonObjectFromDisk(ResourcesManager.PRODUCTS_DATA_FILENAME,
                new ResourcesManager.OnLoadJsonResourceListener() {
                    @Override
                    public void onJsonObjectLoaded(JsonObject jsonObject) {
                        onNutritionLabelDataReady(jsonObject);
                    }
                });
    }

    /*
    private void loadImageByFile() {
        String filename = "myfile";
        String fileContents = "Hello world!";
        File file = new File(mContext.getFilesDir(), filename);

        File[] files = mContext.getFilesDir().listFiles();

        for(File e:files){
            e.getAbsolutePath();
        }

        Field[] drawables = R.drawable.class.getDeclaredFields();
        for( Field f: drawables){
            f.getName();
        }

        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}