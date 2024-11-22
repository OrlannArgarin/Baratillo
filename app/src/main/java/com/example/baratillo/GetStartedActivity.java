package com.example.baratillo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class GetStartedActivity extends AppCompatActivity {

    private Button button_start_login;
    private Button button_start_signup;

    private static final int PERMISSION_REQUEST_CODE = 123;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);


//        // Check if the app is launched for the first time
//        if (isFirstLaunch() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            // Request permission
//            requestPermission();
//        }

        button_start_login = (Button) findViewById(R.id.start_login);
        button_start_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoginActivity();
            }
        });

        button_start_signup = (Button) findViewById(R.id.start_signup);
        button_start_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSignupActivity();
            }
        });


    }

    private boolean isFirstLaunch() {
        return sharedPreferences.getBoolean("firstLaunch", true);
    }

    private void setFirstLaunch() {
        sharedPreferences.edit().putBoolean("firstLaunch", false).apply();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                setFirstLaunch(); // Set first launch to false
            } else {
                // Permission denied
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void openLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void openSignupActivity(){
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
}