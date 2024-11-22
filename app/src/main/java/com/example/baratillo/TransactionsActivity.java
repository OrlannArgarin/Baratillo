package com.example.baratillo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TransactionsActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private Button btnAddItem;
    private TextView tvTotalCost;
    private Button btnCheckout;

    private DatabaseHelper databaseHelper;

    private List<TransactionItem> transactionItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        linearLayout = findViewById(R.id.linear_layout);
        btnAddItem = findViewById(R.id.btnAddItem);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        btnCheckout = findViewById(R.id.btnCheckout);

        databaseHelper = new DatabaseHelper(this);

        // Add initial row
        addRow();

        Toolbar toolbar = findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);  // Set your custom icon
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the navigation icon click
                finish(); // For example, you can finish the activity
            }
        });

        // Button click listeners
        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRow();
            }
        });

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if there are no rows/items present
                if (linearLayout.getChildCount() == 0) {
                    Toast.makeText(TransactionsActivity.this, "No items in the transaction", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if any quantity is null or invalid
                for (int i = 0; i < linearLayout.getChildCount(); i++) {
                    View rowView = linearLayout.getChildAt(i);
                    Spinner spinner = rowView.findViewById(R.id.spinnerItem);
                    EditText etQuantity = rowView.findViewById(R.id.etQuantity);

                    // Check if quantity is null or invalid
                    if (spinner.getSelectedItemPosition() == AdapterView.INVALID_POSITION || etQuantity.getText().toString().isEmpty() || parseQuantity(etQuantity.getText().toString()) <= 0) {
                        Toast.makeText(TransactionsActivity.this, "Invalid entry for item " + (i + 1), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Proceed with the checkout
                showCheckoutDialog(transactionItems);

            }
        });
    }

    private void showCheckoutDialog(List<TransactionItem> items) {
        // Create a custom dialog
        final Dialog dialog = new Dialog(TransactionsActivity.this);
        dialog.setContentView(R.layout.dialog_checkout);
        dialog.setTitle("Checkout");

        // Set specific dimensions for the dialog
        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width); // Define your desired width
        int height = getResources().getDimensionPixelSize(R.dimen.dialog_height_checkout); // Define your desired height
        dialog.getWindow().setLayout(width, height);

        // Initialize components in the dialog
        final Button cancelButton = dialog.findViewById(R.id.btnCancel);
        TextView total = dialog.findViewById(R.id.textTotal);
        EditText discount = dialog.findViewById(R.id.editDiscount);
        EditText amountPaid = dialog.findViewById(R.id.editAmountPaid);
        TextView change = dialog.findViewById(R.id.textChange);
        final Button proceed = dialog.findViewById(R.id.btnProceed);

        // Calculate total cost from the 'items' list
        double totalCostFromItems = calculateTotalCostFromItems(items);

        // Set the text of the total TextView in the dialog
        total.setText(String.format("%.2f", totalCostFromItems));

        // Set onClickListener for the Cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        // Calculate and set the change when the discount and amount paid are entered
        discount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateChange(totalCostFromItems, editable.toString(), amountPaid.getText().toString(), change);
            }
        });

        amountPaid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateChange(totalCostFromItems, discount.getText().toString(), editable.toString(), change);
            }
        });

        // Set onClickListener for the Proceed button
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user email (you may replace this with your user authentication logic)
                String userEmail = getIntent().getStringExtra("userEmail"); // Replace with actual user email

                // Get other necessary data
                double totalAmount = totalCostFromItems;
                double discountAmount = parseDiscount(discount.getText().toString());
                double paidAmount = parseAmountPaid(amountPaid.getText().toString());
                double changeAmount = parseChange(change.getText().toString());

                Log.d("Debug", "Total Amount: " + totalAmount);
                Log.d("Debug", "Discount Amount: " + discountAmount);
                Log.d("Debug", "Paid Amount: " + paidAmount);
                Log.d("Debug", "Change Amount: " + changeAmount);

                // Check if discount, amount paid, and change are empty or invalid
                if (discount.getText().toString().isEmpty() || paidAmount < 0) {
                    Toast.makeText(TransactionsActivity.this, "Fill in all fields with valid values", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if change is negative
                if (changeAmount < 0) {
                    Toast.makeText(TransactionsActivity.this, "Change cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }



                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

                String currentDate = dateFormat.format(calendar.getTime());
                String currentTime = timeFormat.format(calendar.getTime());

                // Store data in transactions table
                boolean transactionInserted = databaseHelper.insertTransaction(userEmail, totalAmount, discountAmount,
                        totalAmount - discountAmount, paidAmount, changeAmount, currentDate, currentTime);

                if (transactionInserted) {
                    // Get the transaction ID for the inserted transaction
                    int transactionId = databaseHelper.getLastTransactionId(userEmail, currentDate, currentTime);

                    // Store data in transactionItems table for each item
                    for (TransactionItem item : items) {
                        databaseHelper.insertTransactionItem(transactionId, item.getProductName(), item.getQuantity(),
                                item.getUnitPrice(), item.getTotalCost());
                    }

                    // Dismiss the dialog
                    dialog.dismiss();
                    // Optionally, you can show a success message to the user
                    Toast.makeText(TransactionsActivity.this, "Transaction successful", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle the case where transaction insertion fails
                    Toast.makeText(TransactionsActivity.this, "Transaction failed", Toast.LENGTH_SHORT).show();
                }
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

    private void updateChange(double totalCost, String discountStr, String amountPaidStr, TextView changeTextView) {
        try {
            double discount = Double.parseDouble(discountStr);
            double amountPaid = Double.parseDouble(amountPaidStr);

            // Calculate change
            double changeAmount = amountPaid - (totalCost - discount);

            // Set the text of the change TextView
            changeTextView.setText(String.format("%.2f", changeAmount));
        } catch (NumberFormatException e) {
            // Handle the case where the entered values are not valid numbers
            changeTextView.setText("0.00");
        }
    }

    private double calculateTotalCostFromItems(List<TransactionItem> items) {
        double totalCost = 0.0;

        // Loop through each item and calculate the cost
        for (TransactionItem item : items) {
            totalCost += item.getTotalCost();
        }

        return totalCost;
    }

    private void addRow() {
        // Inflate a new row layout and add it to the linear layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View rowView = inflater.inflate(R.layout.row_layout, null);
        linearLayout.addView(rowView);

        // Populate the dropdown menu with product names from the database
        Spinner spinner = rowView.findViewById(R.id.spinnerItem);

        // Retrieve product names from the database using a method in DatabaseHelper
        String userEmail = getIntent().getStringExtra("userEmail");
        List<String> productNames = databaseHelper.getProductNames(userEmail);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_list, productNames);
        adapter.setDropDownViewResource(R.layout.spinner_list);
        spinner.setAdapter(adapter);

        // Set up the delete button for the current row
        ImageButton btnDelete = rowView.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the current row from the linear layout
                linearLayout.removeView(rowView);
                // Update total cost
                updateTotalCost();
            }
        });

        // Add a text watcher to the quantity EditText to update total cost on quantity change
        EditText etQuantity = rowView.findViewById(R.id.etQuantity);
        etQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                // Check if a product is selected in the Spinner
                if (spinner.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
                    // If no product is selected, show a toast and return 0.0 as total cost
                    Toast.makeText(TransactionsActivity.this, "Please add a product first", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update the item quantity and total cost before updating the total cost
                TransactionItem item = new TransactionItem(
                        spinner.getSelectedItem().toString(),
                        parseQuantity(etQuantity.getText().toString()),
                        databaseHelper.getProductPrice(spinner.getSelectedItem().toString()),
                        calculateTotalCostForRow(rowView),
                        0.0,  // Default discount value
                        0.0,  // Default amountPaid value
                        0.0   // Default change value
                );

                // Update the item in the list if it already exists, otherwise add a new item
                int index = linearLayout.indexOfChild(rowView);
                if (index < transactionItems.size()) {
                    transactionItems.set(index, item);
                } else {
                    transactionItems.add(item);
                }

                // Update total cost after adding the item to the list
                updateTotalCost();
            }
        });

        // Add an item selected listener to the Spinner to update total cost on item change
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                // Update the item total cost before updating the total cost
                TransactionItem item = new TransactionItem(
                        spinner.getSelectedItem().toString(),
                        parseQuantity(etQuantity.getText().toString()),
                        databaseHelper.getProductPrice(spinner.getSelectedItem().toString()),
                        calculateTotalCostForRow(rowView),
                        0.0,  // Default discount value
                        0.0,  // Default amountPaid value
                        0.0   // Default change value
                );

                // Update the item in the list if it already exists, otherwise add a new item
                int index = linearLayout.indexOfChild(rowView);
                if (index < transactionItems.size()) {
                    transactionItems.set(index, item);
                } else {
                    transactionItems.add(item);
                }

                // Update total cost after adding the item to the list
                updateTotalCost();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
    }

    // Helper method to parse quantity or return 0 if the string is empty
    private int parseQuantity(String quantityString) {
        if (quantityString.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(quantityString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDiscount(String discountString) {
        if (discountString.isEmpty()) {
            return 0.0;
        }

        try {
            return Double.parseDouble(discountString);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double parseAmountPaid(String amountPaidString) {
        if (amountPaidString.isEmpty()) {
            return 0.0;
        }

        try {
            return Double.parseDouble(amountPaidString);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double parseChange(String changeString) {
        if (changeString.isEmpty()) {
            return 0.0;
        }

        try {
            return Double.parseDouble(changeString);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }


    private void updateTotalCost() {
        // Implement logic to calculate and update total cost based on quantity and product prices
        double totalCost = calculateTotalCost();
        Log.d("Debug", "Total Cost: " + totalCost);
        tvTotalCost.setText("Total Cost: ₱" + String.format("%.2f", totalCost));
    }



    // Calculate total cost for the current row
    private double calculateTotalCostForRow(View rowView) {
        Spinner spinner = rowView.findViewById(R.id.spinnerItem);
        EditText etQuantity = rowView.findViewById(R.id.etQuantity);

        double productPrice = databaseHelper.getProductPrice(spinner.getSelectedItem().toString());

        try {
            int quantity = Integer.parseInt(etQuantity.getText().toString());
            return quantity * productPrice;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double calculateTotalCost() {
        double totalCost = 0.0;

        // Loop through each row and calculate the cost
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View rowView = linearLayout.getChildAt(i);
            Spinner spinner = rowView.findViewById(R.id.spinnerItem);
            EditText etQuantity = rowView.findViewById(R.id.etQuantity);

            // Check if quantity is null or invalid
            if (spinner.getSelectedItemPosition() == AdapterView.INVALID_POSITION || etQuantity.getText().toString().isEmpty() || parseQuantity(etQuantity.getText().toString()) <= 0) {
//                Toast.makeText(TransactionsActivity.this, "Invalid entry for item " + (i + 1), Toast.LENGTH_SHORT).show();
                return 0.0;
            }


            // Retrieve the selected product's price from the database using a method in DatabaseHelper
            double productPrice = databaseHelper.getProductPrice(spinner.getSelectedItem().toString());
            int quantity;

            try {
                quantity = Integer.parseInt(etQuantity.getText().toString());
            } catch (NumberFormatException e) {
                // Handle the case where the quantity is not a valid number
                quantity = 0;
                e.printStackTrace();
            }

            // Log values for debugging
            Log.d("Debug", "Product: " + spinner.getSelectedItem().toString() +
                    ", Price: " + productPrice +
                    ", Quantity: " + quantity);

            // Calculate cost for the current row and add it to the total
            totalCost += quantity * productPrice;
        }

        return totalCost;
    }



}
