package com.example.midterm.view;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.midterm.R;
import com.example.midterm.model.entity.Account;
import com.example.midterm.viewModel.AccountViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ChangePasswordActivity extends AppCompatActivity {
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmPassword;
    private TextInputEditText editCurrentPassword, editNewPassword, editConfirmPassword;
    private LinearLayout layoutPasswordStrength;
    private ProgressBar progressPasswordStrength;
    private TextView tvPasswordStrength;
    private Button btnChangePassword;
    private ImageButton btnBack;

    private AccountViewModel accountViewModel;
    private int userId;
    private Account currentAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            finish();
            return;
        }

        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        initViews();
        loadAccountData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tilCurrentPassword = findViewById(R.id.til_current_password);
        tilNewPassword = findViewById(R.id.til_new_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        editCurrentPassword = findViewById(R.id.edit_current_password);
        editNewPassword = findViewById(R.id.edit_new_password);
        editConfirmPassword = findViewById(R.id.edit_confirm_password);
        layoutPasswordStrength = findViewById(R.id.layout_password_strength);
        progressPasswordStrength = findViewById(R.id.progress_password_strength);
        tvPasswordStrength = findViewById(R.id.tv_password_strength);
        btnChangePassword = findViewById(R.id.btn_change_password);
    }

    private void loadAccountData() {
        accountViewModel.getAccountById(userId).observe(this, account -> {
            if (account != null) {
                currentAccount = account;
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Password strength checker for new password
        editNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    layoutPasswordStrength.setVisibility(View.VISIBLE);
                    updatePasswordStrength(s.toString());
                } else {
                    layoutPasswordStrength.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Real-time validation for confirm password
        editConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newPassword = editNewPassword.getText().toString();
                if (!s.toString().isEmpty() && !s.toString().equals(newPassword)) {
                    tilConfirmPassword.setError("Mật khẩu không khớp");
                } else {
                    tilConfirmPassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnChangePassword.setOnClickListener(v -> validateAndChangePassword());
    }

    private void updatePasswordStrength(String password) {
        int strength = calculatePasswordStrength(password);
        progressPasswordStrength.setProgress(strength);

        if (strength < 40) {
            tvPasswordStrength.setText("Yếu");
            tvPasswordStrength.setTextColor(Color.parseColor("#F44336"));
            progressPasswordStrength.getProgressDrawable().setColorFilter(
                Color.parseColor("#F44336"), android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (strength < 70) {
            tvPasswordStrength.setText("Trung bình");
            tvPasswordStrength.setTextColor(Color.parseColor("#FF9800"));
            progressPasswordStrength.getProgressDrawable().setColorFilter(
                Color.parseColor("#FF9800"), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            tvPasswordStrength.setText("Mạnh");
            tvPasswordStrength.setTextColor(Color.parseColor("#4CAF50"));
            progressPasswordStrength.getProgressDrawable().setColorFilter(
                Color.parseColor("#4CAF50"), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;

        // Length bonus
        if (password.length() >= 6) strength += 20;
        if (password.length() >= 8) strength += 10;
        if (password.length() >= 10) strength += 10;

        // Character variety
        if (password.matches(".*[a-z].*")) strength += 15; // lowercase
        if (password.matches(".*[A-Z].*")) strength += 15; // uppercase
        if (password.matches(".*\\d.*")) strength += 15; // digits
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) strength += 15; // special chars

        return Math.min(strength, 100);
    }

    private void validateAndChangePassword() {
        // Clear previous errors
        tilCurrentPassword.setError(null);
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);

        String currentPassword = editCurrentPassword.getText().toString().trim();
        String newPassword = editNewPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        boolean isValid = true;

        // Validate current password
        if (currentPassword.isEmpty()) {
            tilCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            isValid = false;
        } else if (currentAccount != null && !currentPassword.equals(currentAccount.getPassword())) {
            tilCurrentPassword.setError("Mật khẩu hiện tại không đúng");
            isValid = false;
        }

        // Validate new password
        if (newPassword.isEmpty()) {
            tilNewPassword.setError("Vui lòng nhập mật khẩu mới");
            isValid = false;
        } else if (newPassword.length() < 6) {
            tilNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            isValid = false;
        } else if (newPassword.equals(currentPassword)) {
            tilNewPassword.setError("Mật khẩu mới phải khác mật khẩu cũ");
            isValid = false;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu mới");
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            tilConfirmPassword.setError("Mật khẩu không khớp");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đổi mật khẩu")
                .setMessage("Bạn có chắc chắn muốn đổi mật khẩu không?")
                .setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
                    performPasswordChange(newPassword);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performPasswordChange(String newPassword) {
        if (currentAccount == null) {
            Snackbar.make(btnChangePassword, "Lỗi: Không tìm thấy thông tin tài khoản",
                Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.parseColor("#F44336"))
                .setTextColor(Color.WHITE)
                .show();
            return;
        }

        // Update password
        currentAccount.setPassword(newPassword);
        accountViewModel.update(currentAccount);

        // Show success dialog
        new AlertDialog.Builder(this)
                .setTitle("Thành công")
                .setMessage("Mật khẩu của bạn đã được thay đổi thành công!")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Clear all fields
                    editCurrentPassword.setText("");
                    editNewPassword.setText("");
                    editConfirmPassword.setText("");

                    // Show success snackbar
                    Snackbar.make(btnChangePassword, "Đổi mật khẩu thành công!",
                        Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#4CAF50"))
                        .setTextColor(Color.WHITE)
                        .show();

                    // Finish activity after a delay
                    btnChangePassword.postDelayed(() -> finish(), 1500);
                })
                .setCancelable(false)
                .show();
    }
}
