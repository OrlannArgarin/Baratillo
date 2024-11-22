package com.example.baratillo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.baratillo.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    DatabaseHelper databaseHelper;

    private ImageButton back_button;

    private void onLoginSuccess(String userEmail) {
        Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
        intent.putExtra("userEmail", userEmail);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        binding.loginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.loginEmail.getText().toString();
                String password = binding.loginPassword.getText().toString();

                if (email.equals("") || password.equals("")){
                    Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else {
                    Boolean checkCredentials = databaseHelper.checkEmailNamePassword(email, password);

                    if (checkCredentials == true){
                        Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();

                        // Call onLoginSuccess with the valid userEmail before starting MainActivity2
                        onLoginSuccess(email);
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        binding.noAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        back_button = (ImageButton) findViewById(R.id.login_goback);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGetStartedActivity();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        openGetStartedActivity(); // Logout action
    }

    public void openGetStartedActivity(){
        Intent intent = new Intent(this, GetStartedActivity.class);

        // Clear the activity stack and start the GetStartedActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish(); // Finish the current activity (LoginActivity)
    }
}