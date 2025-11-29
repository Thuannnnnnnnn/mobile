package com.example.midterm.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.midterm.R;
import com.example.midterm.utils.HashPassword;
import com.example.midterm.viewModel.AccountViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword, etConfirmPassword;
    private TextView tvErrorUsername, tvErrorPassword, tvErrorConfirmPassword, tvLoginNow;
    private Button btnRegister;
    private ImageButton btnBack;
    private AccountViewModel accountViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        initViews();
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        setUpRealtimeValidation();

        btnRegister.setOnClickListener(v -> handleRegister());

        btnBack.setOnClickListener(v -> onBackPressed());
        tvLoginNow.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.edit_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        tvErrorUsername = findViewById(R.id.tvErrorRegisterUsername);
        tvErrorPassword = findViewById(R.id.tvErrorRegisterPassword);
        tvErrorConfirmPassword = findViewById(R.id.tvErrorConfirmPassword);
        tvLoginNow = findViewById(R.id.tv_login_now);
        btnRegister = findViewById(R.id.btn_register);
        btnBack = findViewById(R.id.btn_back);
    }

    private void handleRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String hashedPassword = HashPassword.hashPassword(password);
        String confirm = etConfirmPassword.getText().toString().trim();

        boolean valid = validateUsername(username) &
                validatePassword(password) &
                validateConfirmPassword(password, confirm);

        if (!valid) return;

        btnRegister.setEnabled(false);
        String role = "user";
        accountViewModel.register(username, hashedPassword, role);

        accountViewModel.registerResult.observe(this, success -> {
            btnRegister.setEnabled(true);
            if (success) {
                Snackbar.make(btnRegister, "Đăng ký thành công!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#4CAF50"))
                        .setTextColor(Color.WHITE)
                        .show();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Snackbar.make(btnRegister, "Email hoặc SĐT đã tồn tại!", Snackbar.LENGTH_LONG).
                        setBackgroundTint(Color.parseColor("#ED2A2A"))
                        .show();
            }
        });
    }

    private void setUpRealtimeValidation() {
        etUsername.addTextChangedListener(new SimpleTextWatcher(s -> validateUsername(s.toString())));
        etPassword.addTextChangedListener(new SimpleTextWatcher(s -> validatePassword(s.toString())));
        etConfirmPassword.addTextChangedListener(new SimpleTextWatcher(s ->
                validateConfirmPassword(etPassword.getText().toString(), s.toString())));
    }

    private boolean validateUsername(String input) {
        if (input.isEmpty()) {
            showError(tvErrorUsername, "Không được để trống");
            return false;
        } else if (!isValidEmail(input) && !isValidPhone(input)) {
            showError(tvErrorUsername, "Email hoặc số điện thoại không hợp lệ");
            return false;
        } else {
            hideError(tvErrorUsername);
            return true;
        }
    }

    private boolean validatePassword(String input) {
        if (input.isEmpty()) {
            showError(tvErrorPassword, "Vui lòng nhập mật khẩu");
            return false;
        } else if (input.length() < 6) {
            showError(tvErrorPassword, "Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        } else {
            hideError(tvErrorPassword);
            return true;
        }
    }

    private boolean validateConfirmPassword(String password, String confirm) {
        if (confirm.isEmpty()) {
            showError(tvErrorConfirmPassword, "Vui lòng nhập lại mật khẩu");
            return false;
        } else if (!confirm.equals(password)) {
            showError(tvErrorConfirmPassword, "Mật khẩu không khớp");
            return false;
        } else {
            hideError(tvErrorConfirmPassword);
            return true;
        }
    }

    private void showError(TextView textView, String message) {
        textView.setText(message);
        textView.setVisibility(View.VISIBLE);
    }

    private void hideError(TextView textView) {
        textView.setVisibility(View.GONE);
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^(\\+84|0)[0-9]{9,10}$");
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final OnTextChangedListener listener;

        SimpleTextWatcher(OnTextChangedListener listener) {
            this.listener = listener;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            listener.onTextChanged(s);
        }
        @Override public void afterTextChanged(Editable s) {}

        interface OnTextChangedListener {
            void onTextChanged(CharSequence s);
        }
    }
}
