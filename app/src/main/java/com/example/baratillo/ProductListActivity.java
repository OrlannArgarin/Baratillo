package com.example.baratillo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baratillo.databinding.ActivityProductListBinding;
import com.example.baratillo.databinding.ContentScrollingBinding;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ActivityProductListBinding binding;
    private ContentScrollingBinding contentScrollingBinding;
    private static final int PICK_IMAGE_REQUEST = 1;
    private DatabaseHelper databaseHelper;
    private Uri selectedImageUri;
    private ImageView productImageView;
    private Glide Glide;
    private ImageView productImageViewInDialog;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);

        // Set the title dynamically
        setTitle("Product List");

        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle(getTitle());

        // Set up the Toolbar
//        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        productList = new ArrayList<>();

        // Set up the RecyclerView from the included layout
        contentScrollingBinding = binding.contentScrolling;
        recyclerView = contentScrollingBinding.productListView;

        // Use GridLayoutManager with spanCount 2 to create a two-column layout
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        productAdapter = new ProductAdapter(productList, this, this);
        recyclerView.setAdapter(productAdapter);

        String userEmail = getIntent().getStringExtra("userEmail");
        Log.d("ProductList", "userEmail: " + userEmail);
        if (userEmail != null) {
            updateProductList(userEmail);
        } else {
            // Handle the case where userName is null
            Toast.makeText(this, "User email is null", Toast.LENGTH_SHORT).show();
        }

        // Update the RecyclerView
        updateProductList(userEmail);

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddProductDialog();
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        showEditProductDialog(product);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle the back button press
                onBackPressed();
                return true;
            // Add other cases if needed
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private EditText nameEditText;
    private EditText priceEditText;
    private void showEditProductDialog(Product product) {
        // Create a custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_product);
        dialog.setTitle("Edit Product");

        // Set specific dimensions for the dialog
        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        int height = getResources().getDimensionPixelSize(R.dimen.dialog_height);
        dialog.getWindow().setLayout(width, height);

        // Initialize components in the dialog
        nameEditText = dialog.findViewById(R.id.editTextNameEdit);
        priceEditText = dialog.findViewById(R.id.editTextPriceEdit);
        final Button applyButton = dialog.findViewById(R.id.btnEdit);
        final Button deleteButton = dialog.findViewById(R.id.btnDelete);
        final Button cancelButton = dialog.findViewById(R.id.btnCancelEdit);

        // Find the ImageView in the dialog's layout
        productImageViewInDialog = dialog.findViewById(R.id.imageViewProductEdit);

        // Assign the ImageView to the global variable
        productImageView = productImageViewInDialog;

        // Set default values based on the selected product
        nameEditText.setText(product.getName());
        priceEditText.setText(String.valueOf(product.getPrice()));

        // Load image using Glide or your preferred image loading library
//        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
//            Log.d("ProductListActivity", "Loading image with URI: " + product.getImageUri());
//
//            // Use dialog's context and the local productImageViewInDialog
//            Glide.with(dialog.getContext())
//                    .load(Uri.parse(product.getImageUri()))
//                    .into(productImageViewInDialog);
//        }

        // Set onClickListener for the ImageView to open the image picker
        productImageViewInDialog.setOnClickListener(view -> openImagePicker());

        // Reset the selected image URI
        selectedImageUri = null;

        // Set onClickListener for the Delete button
        deleteButton.setOnClickListener(view -> {
            // Get the ID of the selected product
            int productId = product.getId();

            Log.d("ProductListActivity", "Deleting product with ID: " + productId);

            // Show a confirmation dialog before deleting
            showDeleteConfirmationDialog(productId, dialog);
        });

        // Set onClickListener for the Apply button
        applyButton.setOnClickListener(view -> {
            // Get the ID of the selected product
            int productId = product.getId();

            if (selectedImageUri == null) {
                selectedImageUri = Uri.parse(product.getImageUri());
            }

            // Validate if the input in the price field is a valid float
            try {
                float priceValue = Float.parseFloat(priceEditText.getText().toString());

                // Apply the changes to the database after confirmation
                showApplyConfirmationDialog(product, productId, nameEditText.getText().toString(), priceValue, selectedImageUri.toString(), dialog);
            } catch (NumberFormatException e) {
                // Handle the case where the input is not a valid float
                Toast.makeText(ProductListActivity.this, "Please enter a valid numeric value for the price", Toast.LENGTH_SHORT).show();
            }
        });

        // Set onClickListener for the Cancel button
        cancelButton.setOnClickListener(view -> dialog.dismiss());

        // Show the dialog
        dialog.show();

        // Inflate the dim background layout
        View dimBackground = LayoutInflater.from(this).inflate(R.layout.dim_background_layout, null);

        // Add dim background as an overlay
        ViewGroup root = getWindow().getDecorView().findViewById(android.R.id.content);
        root.addView(dimBackground);

        // Remove dim background when the dialog is dismissed
        dialog.setOnDismissListener(dialogInterface -> root.removeView(dimBackground));
    }


    private void showDeleteConfirmationDialog(int productId, Dialog editProductDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this product?");
        builder.setPositiveButton("Delete", (dialogInterface, i) -> {
            // Delete the product from the database
            boolean isDeleted = databaseHelper.deleteProduct(productId);

            if (isDeleted) {
                Toast.makeText(ProductListActivity.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                updateProductList(getIntent().getStringExtra("userEmail"));
            } else {
                Toast.makeText(ProductListActivity.this, "Failed to delete product", Toast.LENGTH_SHORT).show();
            }
            editProductDialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();


    }


    private void showApplyConfirmationDialog(Product product, int productId, String newName, Float newPrice, String newUri, Dialog editProductDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Apply Changes");
        builder.setMessage("Are you sure you want to apply changes to this product?");
        builder.setPositiveButton("Apply", (dialogInterface, i) -> {
            // Check if the image has been changed
            Uri oldUri = Uri.parse(product.getImageUri());

            // Check if the image has been changed or other details have been modified
            if (!newUri.equals(oldUri.toString()) || !newName.equals(product.getName()) || !newPrice.equals(product.getPrice())) {
                // Apply the changes to the database
                boolean isUpdated = databaseHelper.updateProduct(productId, newName, newPrice, newUri);

                if (isUpdated) {
                    Toast.makeText(ProductListActivity.this, "Changes applied successfully", Toast.LENGTH_SHORT).show();
                    updateProductList(getIntent().getStringExtra("userEmail"));
                } else {
                    Toast.makeText(ProductListActivity.this, "Failed to apply changes", Toast.LENGTH_SHORT).show();
                }
            } else {
                // No changes were made, display a message or take appropriate action
                Toast.makeText(ProductListActivity.this, "No changes were made", Toast.LENGTH_SHORT).show();
            }
            editProductDialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();



    }

    private void showAddProductDialog() {
        // Create a custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_product);
        dialog.setTitle("Add Product");

        // Set specific dimensions for the dialog
        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width); // Define your desired width
        int height = getResources().getDimensionPixelSize(R.dimen.dialog_height); // Define your desired height
        dialog.getWindow().setLayout(width, height);

        // Initialize components in the dialog
        final EditText nameEditText = dialog.findViewById(R.id.editTextName);
        final EditText priceEditText = dialog.findViewById(R.id.editTextPrice);
        final Button addButton = dialog.findViewById(R.id.btnAdd);
        final Button cancelButton = dialog.findViewById(R.id.btnCancel);

        // Find the ImageView in the dialog's layout
        ImageView productImageViewInDialog = dialog.findViewById(R.id.imageViewProduct);

        // Assign the ImageView to the global variable
        productImageView = productImageViewInDialog;

        // Set onClickListener for the ImageView to open the image picker
        productImageViewInDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    openImagePicker();
            }
        });

        // Set onClickListener for the Add button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // Get the entered data
                String userEmail = getIntent().getStringExtra("userEmail");
                String productName = nameEditText.getText().toString();
                String productPrice = priceEditText.getText().toString();

                // Check if name and price are empty
                if (productName.isEmpty() || productPrice.isEmpty()) {
                    // Display a toast if any field is empty
                    Toast.makeText(ProductListActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if an image is selected
                if (selectedImageUri == null) {
                    // Handle the case where no image is selected
                    Toast.makeText(ProductListActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate if the input in the price field is a valid float
                try {
                    float priceValue = Float.parseFloat(productPrice);

                    // Insert the product into the database
                    boolean isInserted = databaseHelper.insertProduct(userEmail, productName, priceValue, selectedImageUri.toString());

                    if (isInserted) {
                        Toast.makeText(ProductListActivity.this, "Product successfully added!", Toast.LENGTH_SHORT).show();

                        // Update the RecyclerView
                        updateProductList(userEmail);

                        // Reset the selected image URI
                        selectedImageUri = null;

                        // Dismiss the dialog
                        dialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    // Handle the case where the input is not a valid float
                    Toast.makeText(ProductListActivity.this, "Please enter a valid numeric value for the price", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Set onClickListener for the Cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();

        // Inflate the dim background layout
        View dimBackground = LayoutInflater.from(this).inflate(R.layout.dim_background_layout, null);

        // Add dim background as an overlay
        ViewGroup root = getWindow().getDecorView().findViewById(android.R.id.content);
        root.addView(dimBackground);

        // Remove dim background when the dialog is dismissed
        dialog.setOnDismissListener(dialogInterface -> root.removeView(dimBackground));
    }


    private void updateProductList(String userEmail) {
        // Fetch products from the database for the specified user
        Cursor cursor = databaseHelper.getProductsByUser(userEmail);
        List<Product> updatedProductList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            int idColumnIndex = cursor.getColumnIndex("id");
            int nameColumnIndex = cursor.getColumnIndex("name");
            int priceColumnIndex = cursor.getColumnIndex("price");
            int imageUriColumnIndex = cursor.getColumnIndex("image_uri");

            do {
                if (idColumnIndex >= 0 && nameColumnIndex >= 0 && priceColumnIndex >= 0 && imageUriColumnIndex >= 0) {
                    // Ensure column indices are valid

                    int id = cursor.getInt(idColumnIndex);
                    String name = cursor.getString(nameColumnIndex);
                    Float price = cursor.getFloat(priceColumnIndex);
                    String imageUri = cursor.getString(imageUriColumnIndex);

                    // Create a new Product object and add it to the list
                    updatedProductList.add(new Product(id, name, price, imageUri));
                } else {
                    // Handle the case where one or more columns are not found
                    // Log an error, show a toast, or take appropriate action
                }
            } while (cursor.moveToNext());
        }

        // Update the RecyclerView with the new product list
        productList.clear();
        productList.addAll(updatedProductList);
        productAdapter.notifyDataSetChanged();

        // Update the visibility of the TextView based on the product list size
        TextView noProductsTextView = contentScrollingBinding.noProductsTextView;
        if (productList.isEmpty()) {
            noProductsTextView.setVisibility(View.VISIBLE);
        } else {
            noProductsTextView.setVisibility(View.GONE);
        }
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                // Handle the result from the image picker
                Uri selectedImageUri = result.getData().getData();

                // Set the selected image to the ImageView for preview (optional)
                productImageView.setImageURI(selectedImageUri);

                // Set the selected image URI to the class-level variable
                this.selectedImageUri = selectedImageUri;
            }
        }
    );

    // Method to open image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }



}
