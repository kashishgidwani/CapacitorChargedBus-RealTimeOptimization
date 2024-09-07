package com.example.bus;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MobileLoginActivity extends AppCompatActivity {

    private EditText etMobileNumber, etMobilePassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_login);

        // Initialize the views
        etMobileNumber = findViewById(R.id.et_mobile_number);
        etMobilePassword = findViewById(R.id.et_mobile_password);
        btnLogin = findViewById(R.id.btn_mobile_login);

        // Set OnClickListener for login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobileNumber = etMobileNumber.getText().toString();
                String password = etMobilePassword.getText().toString();

                // Basic validation checks
                if (TextUtils.isEmpty(mobileNumber)) {
                    etMobileNumber.setError("Enter Mobile Number");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    etMobilePassword.setError("Enter Password");
                    return;
                }

                // Dummy authentication logic
                if (mobileNumber.equals("1234567890") && password.equals("password123")) {
                    Toast.makeText(MobileLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    // Navigate to the next activity or home screen
                    // Intent intent = new Intent(MobileLoginActivity.this, HomeActivity.class);
                    // startActivity(intent);
                    // finish();
                } else {
                    Toast.makeText(MobileLoginActivity.this, "Invalid Mobile Number or Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
