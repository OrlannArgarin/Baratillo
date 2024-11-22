package com.example.baratillo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

public class MainActivity2 extends AppCompatActivity {

    private Button main_logout;
    private Button main_productlist;
    private TextView welcome_text;
    private DatabaseHelper databaseHelper;
    private Button main_transactions;
    private Button db_download;
    private Button main_database;
    private ImageView feedback;
    private static final int STORAGE_PERMISSION_CODE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        welcome_text = findViewById(R.id.welcome_text);
        databaseHelper = new DatabaseHelper(this);

        // Retrieve the user's email from the Intent
        String userEmail = getIntent().getStringExtra("userEmail");
        Log.d("MainActivity", "userEmail: " + userEmail);

        if (userEmail != null) {
            // Fetch the user name from the database
            String userName = databaseHelper.getUserNameByEmail(userEmail);

            // Display the user's name in the welcome_text
            if (userName != null) {
                String welcomeMessage = "Welcome, " + userName + "!";
                welcome_text.setText(welcomeMessage);
            }
        } else {
            // Handle the case where userEmail is null
            welcome_text.setText("Welcome!");
        }

        main_logout = findViewById(R.id.main_logout);
        main_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutConfirmationDialog();
            }
        });

        main_productlist = findViewById(R.id.main_productlist);
        main_productlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openProductListActivity(userEmail);
            }
        });

        main_transactions = findViewById(R.id.main_transact);
        main_transactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTransactionsActivity(userEmail);
            }
        });

        main_database = findViewById(R.id.main_database);
        main_database.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatabaseDialog(userEmail);
            }
        });

        feedback = findViewById(R.id.feedback);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFeedbackDialog();
            }
        });

    }

    private void openFeedbackDialog() {

        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.feedback, null);

        // Create the Dialog instance
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView);

        // Set specific dimensions for the dialog (optional)
        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        int height = getResources().getDimensionPixelSize(R.dimen.dialog_height_feedback);
        dialog.getWindow().setLayout(width, height);

        // Handle the close button in the dialog
        Button closeButton = dialogView.findViewById(R.id.btnCloseFeedback);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        dialog.show();

        // Inflate the dim background layout
        View dimBackground = LayoutInflater.from(this).inflate(R.layout.dim_background_layout, null);

        // Add dim background as an overlay
        ViewGroup root = getWindow().getDecorView().findViewById(android.R.id.content);
        root.addView(dimBackground);

        // Remove dim background when the dialog is dismissed
        dialog.setOnDismissListener(dialogInterface -> root.removeView(dimBackground));
    }

    // Inside MainActivity2
    private void openDatabaseDialog(String email) {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_database, null);

        // Create the Dialog instance
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView);

        // Set specific dimensions for the dialog (optional)
        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        int height = getResources().getDimensionPixelSize(R.dimen.dialog_height_database);
        dialog.getWindow().setLayout(width, height);

        // Handle the close button in the dialog
        Button closeButton = dialogView.findViewById(R.id.btnClose);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        Button db_download = dialogView.findViewById(R.id.db_download);
        db_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkStoragePermissions()) {
                    String userName = getIntent().getStringExtra("userEmail");
                    downloadCsvFiles(userName);
                } else {
                    requestForStoragePermissions();
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

    public boolean checkStoragePermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        }else {
            //Below android 11
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            }catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        }else{
            //Below android 11
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }

    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>(){

                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                                //Android is 11 (R) or above
                                if(Environment.isExternalStorageManager()){
                                    //Manage External Storage Permissions Granted
//                                    Log.d("storageActivityResultLauncher", "onActivityResult: Manage External Storage Permissions Granted");
                                    String userName = getIntent().getStringExtra("userEmail");
                                    downloadCsvFiles(userName);
                                }else{

                                    Toast.makeText(MainActivity2.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                //Below android 11

                            }
                        }
                    });

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length > 0){
                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if(read && write){
                    String userName = getIntent().getStringExtra("userEmail");
                    downloadCsvFiles(userName);
                }else{
                    Toast.makeText(MainActivity2.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



//    private void requestRuntimePermission() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
//        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
//            AlertDialog.Builder builder = new AlertDialog.Builder (this);
//            builder.setMessage("This app requires MANAGE_EXTERNAL_STORAGE permission for particular feature to work as expected.")
//                    .setTitle("Permission Required")
//                    .setCancelable(false)
//                    .setPositiveButton("Ok", (dialog, which) -> {
//                        ActivityCompat.requestPermissions (this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE},
//                            PERMISSION_REQUEST_MANAGE_EXTERNAL_STORAGE);
//                        dialog.dismiss();
//                    })
//                    .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()));
//
//            builder.show();
//
//        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE},
//                    PERMISSION_REQUEST_MANAGE_EXTERNAL_STORAGE);
//        }
//    }



//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == PERMISSION_REQUEST_MANAGE_EXTERNAL_STORAGE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
//            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setMessage("This feature is unavailable because this feature requires permission that you have denied." +
//                        "Please allow Storage permission from settings to proceed further.")
//                        .setTitle("Permission Required")
//                        .setCancelable(false)
//                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                        .setPositiveButton("Settings", (dialog, which) -> {
//                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            Uri uri = Uri.fromParts("package", getPackageName(), null);
//                            intent.setData(uri);
//                            startActivity(intent);
//
//                            dialog.dismiss();
//                        });
//
//                builder.show();
//            }
//        } else {
//            requestRuntimePermission();
//        }
//    }

    //    private void requestStoragePermission() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//            new AlertDialog.Builder(this)
//                    .setTitle("Permission needed")
//                    .setMessage("This permission is needed because this app will download CSV files to backup your data.")
//                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            ActivityCompat.requestPermissions(MainActivity2.this,
//                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
//                        }
//                    })
//                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    })
//                    .create().show();
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    // Inside MainActivity2
    private void downloadCsvFiles(String userEmail) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

//        // Export "allusers" table
//        databaseHelper.exportTableAsCsvAllUsers("allusers", userEmail);

        // Export "products" table
        databaseHelper.exportTableAsCsv("products", userEmail);

        // Export "transactions" table
        databaseHelper.exportTableAsCsv("transactions", userEmail);

        // Export "transactionItems" table
        databaseHelper.exportTransactionItemsAsCsv(userEmail);

        Toast.makeText(this, "CSV files downloaded successfully", Toast.LENGTH_SHORT).show();
    }


    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            showLogoutConfirmationDialog(); // Logout action
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to logout", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000); // Reset the flag after 2 seconds
        }
    }

    private void openProductListActivity(String userEmail) {
        Intent intent = new Intent(this, ProductListActivity.class);
        intent.putExtra("userEmail", userEmail);
        startActivity(intent);
    }

    private void openTransactionsActivity(String userEmail) {
        Intent intent = new Intent(this, TransactionsActivity.class);
        intent.putExtra("userEmail", userEmail);
        startActivity(intent);
    }

    private void showLogoutConfirmationDialog() {
        // Inflate the dim background layout
        View dimBackground = getLayoutInflater().inflate(R.layout.dim_background_layout, null);

        // Add dim background as an overlay
        addContentView(dimBackground, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openGetStartedActivity(); // Perform logout action
                removeDimBackground(); // Remove dim background when the dialog is dismissed
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeDimBackground(); // Remove dim background when the dialog is dismissed
            }
        });

        // Set a listener to handle the dismissal of the dialog
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                removeDimBackground(); // Remove dim background when the dialog is dismissed
            }
        });

        builder.show();
    }

    // Helper method to remove dim background
    private void removeDimBackground() {
        // Find the dim background view and remove it
        View dimBackground = findViewById(R.id.dim_background);
        if (dimBackground != null) {
            ViewGroup parent = (ViewGroup) dimBackground.getParent();
            parent.removeView(dimBackground);
        }
    }


    public void openGetStartedActivity(){
        Intent intent = new Intent(this, GetStartedActivity.class);

        // Clear the activity stack and start the GetStartedActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish(); // Finish the current activity (MainActivity2)
    }


}