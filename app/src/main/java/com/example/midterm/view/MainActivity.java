package com.example.midterm.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.midterm.R;
import com.example.midterm.viewModel.AccountViewModel;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class MainActivity extends AppCompatActivity {
    private AccountViewModel accountViewModel;
    private TextView tvForgotpassword, tvCreateAccount, tvErrorUsername, tvErrorPassword;
    private TextInputEditText edtUsername, edtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        edtUsername = findViewById(R.id.edit_email_phone);
        edtPassword = findViewById(R.id.edit_password);
        tvErrorUsername = findViewById(R.id.tvErrorUsername);
        tvErrorPassword = findViewById(R.id.tvErrorPassword);
        tvForgotpassword = findViewById(R.id.tv_forgot_password);
        tvCreateAccount = findViewById(R.id.tv_create_account);
        btnLogin = findViewById(R.id.btn_login);

        FirebaseApp.initializeApp(this);

        if (FirebaseApp.getApps(this).size() > 0) {
            Log.d("FirebaseTest", " Firebase initialized successfully!");
        } else {
            Log.e("FirebaseTest", "Firebase NOT initialized!");
        }

        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                accountViewModel.login(username, password);
            }
        });

        accountViewModel.loggedInUser.observe(this, user -> {
            if (user != null) {
                int userID  = user.getId();

                // Lưu thông tin đăng nhập
                SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                prefs.edit()
                        .putInt("user_id", userID)
                        .putString("role", user.getRole())
                        .apply();
                if("user".equals(user.getRole())){
                    Intent intent = new Intent(this, Homepage.class);
                    intent.putExtra("user_id", userID);
                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(this, HomepageOrganizer.class);
                    intent.putExtra("user_id", userID);
                    startActivity(intent);
                    finish();
                }
            } else {
                tvErrorPassword.setText("Thông tin đăng nhập không hợp lệ");
                tvErrorPassword.setVisibility(View.VISIBLE);
            }
        });

        tvCreateAccount.setOnClickListener(v ->{
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        setUpRealtimeValidation();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void setUpRealtimeValidation(){
        edtUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUsername(s.toString());
                }
            }
        );
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }
        });
    }

    private void validateUsername(String input) {
        if (input.isEmpty()) {
            tvErrorUsername.setText("Không được để trống");
            tvErrorUsername.setVisibility(View.VISIBLE);
        } else if (!isValidEmail(input) && !isValidPhone(input)) {
            tvErrorUsername.setText("Email hoặc số điện thoại không hợp lệ");
            tvErrorUsername.setVisibility(View.VISIBLE);
        } else {
            tvErrorUsername.setVisibility(View.GONE);
        }
    }

    private void validatePassword(String input) {
        if (input.isEmpty()) {
            tvErrorPassword.setText("Vui lòng nhập mật khẩu");
            tvErrorPassword.setVisibility(View.VISIBLE);
        } else if (input.length() < 6) {
            tvErrorPassword.setText("Mật khẩu phải có ít nhất 6 ký tự");
            tvErrorPassword.setVisibility(View.VISIBLE);
        } else {
            tvErrorPassword.setVisibility(View.GONE);
        }
    }
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean isValidPhone(String phone) {
        return phone.matches("^(\\+84|0)[0-9]{9,10}$"); // Việt Nam
    }
}