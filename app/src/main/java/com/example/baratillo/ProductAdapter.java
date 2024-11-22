package com.example.baratillo;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList; // Assume Product is a class representing your product data
    private Context context;
    private OnProductClickListener onProductClickListener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> productList, Context context, OnProductClickListener listener) {
        this.productList = productList;
        this.context = context;
        this.onProductClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Load image using Glide or your preferred image loading library
        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            // Use your preferred image loading library (Glide, Picasso, etc.)
            // Example using Glide:
            Glide.with(context)
                    .load(Uri.parse(product.getImageUri()))
                    .into(holder.productImageView);
        }

        holder.productNameTextView.setText(product.getName());
        holder.productPriceTextView.setText(String.valueOf(product.getPrice()));
        holder.itemView.setOnClickListener(view -> {
            if (onProductClickListener != null) {
                onProductClickListener.onProductClick(product);
            }
        });
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView productNameTextView;
        private TextView productPriceTextView;
        private ImageView productImageView; // Add this line

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            productImageView = itemView.findViewById(R.id.productImageView); // Add this line
            productImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        public void bind(Product product) {
            productNameTextView.setText(product.getName());
            productPriceTextView.setText(String.valueOf(product.getPrice()));
        }
    }

}
