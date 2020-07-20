package com.dulacs.festeban26;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class SectionListDataAdapter extends RecyclerView.Adapter<SectionListDataAdapter.SingleItemRowHolder> {

    private ArrayList<ProductModel> mItemModels;

    public SectionListDataAdapter(ArrayList<ProductModel> mItemModels) {
        this.mItemModels = mItemModels;
    }

    @Override
    public SingleItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_cell, parent, false);
        SingleItemRowHolder singleItemRowHolder = new SingleItemRowHolder(v);
        return singleItemRowHolder;
    }


    @Override
    public void onBindViewHolder(SingleItemRowHolder holder, int position) {
        ProductModel itemModel = mItemModels.get(position);

        File internalDirectory = holder.itemView.getContext().getFilesDir();
        File imagesDirectory = new File(internalDirectory, "products_images");
        String imageFileName = itemModel.getSapCode() + ".webp";
        File imageFile = new File(imagesDirectory, imageFileName);
        if (imageFile.exists())
            Glide.with(holder.itemView.getContext())
                    .load(imageFile)
                    .into(holder.getProductImage());
        /*
        Glide
                .with(holder.itemView.getContext())
                .load(Uri.parse("file:///android_asset/products_images/" + itemModel.getSapCode() + ".webp"))
                .into(holder.getProductImage());*/

        holder.getProductName().setText(itemModel.getName());
        holder.getProductFlavour().setText(itemModel.getFlavour());
        holder.getProductPackaging().setText(itemModel.getPackaging());
        holder.setProductSapCode(itemModel.getSapCode());
    }

    @Override
    public int getItemCount() {
        return (null != mItemModels ? mItemModels.size() : 0);
    }

    class SingleItemRowHolder extends RecyclerView.ViewHolder {

        private ImageView mProductImage;
        private TextView mProductName;
        private TextView mProductFlavour;
        private TextView mProductPackaging;
        private String mProductSapCode;

        SingleItemRowHolder(View itemView) {
            super(itemView);

            mProductImage = itemView.findViewById(R.id.imageView_productCell_ProductImage);
            mProductName = itemView.findViewById(R.id.textView_productCell_ProductName);
            mProductFlavour = itemView.findViewById(R.id.textView_productCell_ProductFlavour);
            mProductPackaging = itemView.findViewById(R.id.textView_productCell_ProductPackaging);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((HomeActivity) view.getContext()).attachProductInformationFragmentForSapCode(mProductSapCode);
                }
            });
        }

        void setProductSapCode(String sapCode) {
            mProductSapCode = sapCode;
        }

        ImageView getProductImage() {
            return mProductImage;
        }

        TextView getProductName() {
            return mProductName;
        }

        TextView getProductFlavour() {
            return mProductFlavour;
        }

        TextView getProductPackaging() {
            return mProductPackaging;
        }
    }
}