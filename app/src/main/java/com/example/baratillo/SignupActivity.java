package com.example.baratillo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.baratillo.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    DatabaseHelper databaseHelper;

    private ImageButton back_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        binding.signupSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.signupName.getText().toString();
                String email = binding.signupEmail.getText().toString();
                String password = binding.signupPassword.getText().toString();

                if (name.equals("") || email.equals("") || password.equals("")){
                    Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    Boolean checkUserEmail = databaseHelper.checkEmail(email);
                    Boolean checkUserName = databaseHelper.checkName(name);

                    if (checkUserEmail == false && checkUserName == false){
                        Boolean insert = databaseHelper.insertData(email, password, name);

                        if(insert == true){
                            Toast.makeText(SignupActivity.this, "Registered successfully, please Login", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                        } else{
                            Toast.makeText(SignupActivity.this, "Signup Failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignupActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        binding.haveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        back_button = (ImageButton) findViewById(R.id.signup_goback);
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
        finish(); // Finish the current activity (SignupActivity)
    }
}