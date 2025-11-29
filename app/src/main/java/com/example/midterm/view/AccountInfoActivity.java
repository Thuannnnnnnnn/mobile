package com.example.midterm.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.midterm.R;
import com.example.midterm.model.entity.Account;
import com.example.midterm.model.entity.UserProfile;
import com.example.midterm.viewModel.AccountViewModel;
import com.example.midterm.viewModel.UserProfileViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountInfoActivity extends AppCompatActivity {
    private CircleImageView imgAvatar;
    private ImageView imgEditAvatar;
    private Uri selectedImageUri;
    private TextInputEditText etFullname, etPhone, etEmail, etDOB;
    private RadioButton rbMale, rbFemale, rbOther;
    private Button btnSaveProfile;
    private ImageButton btnBack;
    private UserProfileViewModel userProfileViewModel;
    private AccountViewModel accountViewModel;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_info);

        initViews();

        // Lấy userId từ Intent
        userId = getIntent().getIntExtra("user_id", -1);

        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        // Quan sát LiveData từ ViewModel
        userProfileViewModel.getUserLiveData().observe(this, this::displayUserInfo);

        // Load dữ liệu user từ Room
        if (userId != -1) {
            userProfileViewModel.loadUserById(userId);
        }

        etDOB.setOnClickListener(v -> showDatePicker(etDOB));
        btnBack.setOnClickListener(v -> finish());
        imgEditAvatar.setOnClickListener(v -> openImagePicker());

        btnSaveProfile.setOnClickListener(v -> {
            UserProfile current = userProfileViewModel.getUserLiveData().getValue();
            if (current == null) return;

            String newFullName = etFullname.getText().toString().trim();
            String newDOB = etDOB.getText().toString().trim();
            String newGender = getSelectedGender();
            String newAvatar = selectedImageUri != null ? selectedImageUri.toString() : current.getAvatar();
            String newEmail = etEmail.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();

            // Kiểm tra UserProfile có thay đổi không
            boolean isProfileChanged = !newFullName.equals(current.getFullName()) ||
                    !newDOB.equals(current.getDateOfBirth()) ||
                    !newGender.equals(current.getSex()) ||
                    !newAvatar.equals(current.getAvatar());

            Account currentAccount = accountViewModel.getAccountById(current.getUserId()).getValue();
            if (currentAccount == null) {
                Toast.makeText(this, "Đang tải dữ liệu, thử lại sau", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isEmailChanged = !newEmail.equals(currentAccount.getEmail());
            boolean isPhoneChanged = !newPhone.equals(currentAccount.getPhone());
            boolean isAccountChanged = isEmailChanged || isPhoneChanged;

            if (!isProfileChanged && !isAccountChanged) {
                Toast.makeText(this, "Không có thay đổi nào", Toast.LENGTH_SHORT).show();
                return;
            }

            // Nếu email/phone thay đổi mới validate
            if (isEmailChanged && !newEmail.isEmpty() && !isValidEmail(newEmail)) {
                etEmail.setError("Email không hợp lệ");
                etEmail.requestFocus();
                return;
            }

            if (isPhoneChanged && !newPhone.isEmpty() && !isValidPhone(newPhone)) {
                etPhone.setError("Số điện thoại không hợp lệ");
                etPhone.requestFocus();
                return;
            }

            // Kiểm tra tồn tại nếu có thay đổi
            if (isAccountChanged) {
                accountViewModel.checkEmailOrPhoneExist(newEmail, newPhone, current.getUserId(), (Boolean exist) -> runOnUiThread(() -> {
                    if (exist) {
                        Toast.makeText(this, "Email hoặc số điện thoại đã tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateProfileAndAccount(current, newFullName, newDOB, newGender, newAvatar, newEmail, newPhone);
                }));
            } else {
                // Chỉ update UserProfile
                updateProfileAndAccount(current, newFullName, newDOB, newGender, newAvatar, newEmail, newPhone);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.img_avatar);
        imgEditAvatar = findViewById(R.id.img_edit_avatar);
        etFullname = findViewById(R.id.edit_user_fullname);
        etEmail = findViewById(R.id.edit_user_email);
        etPhone = findViewById(R.id.edit_user_phone);
        etDOB = findViewById(R.id.edit_user_dob);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        rbOther = findViewById(R.id.rb_other);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        btnBack = findViewById(R.id.btn_back);
    }

    private void updateProfileAndAccount(UserProfile current, String fullName, String dob, String gender, String avatar, String email, String phone) {
        UserProfile updatedProfile = new UserProfile(current.getUserId(), fullName, dob, gender, avatar);

        userProfileViewModel.update(updatedProfile, message ->
                runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show())
        );
        accountViewModel.updateEmailOrPhone(current.getUserId(), email, phone, null);
    }
    private void displayUserInfo(UserProfile profile) {
        if (profile == null) return;

        etFullname.setText(profile.getFullName());
        etDOB.setText(profile.getDateOfBirth());

        switch (profile.getSex()) {
            case "Nam":
                rbMale.setChecked(true);
                break;
            case "Nữ":
                rbFemale.setChecked(true);
                break;
            default:
                rbOther.setChecked(true);
                break;
        }

        String avatar = profile.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(avatar)) // parse content Uri từ String
                    .placeholder(R.drawable.catlogo_removebg_preview)
                    .into(imgAvatar);
        } else {
            Glide.with(this)
                    .load(R.drawable.catlogo_removebg_preview)
                    .into(imgAvatar);
        }
        // Hiển thị email/phone từ AccountViewModel
        accountViewModel.getAccountById(profile.getUserId()).observe(this, account -> {
            if (account != null) {
                etEmail.setText(account.getEmail());
                etPhone.setText(account.getPhone());
            }
        });
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Glide load từ content Uri
                        Glide.with(this)
                                .load(selectedImageUri)
                                .placeholder(R.drawable.catlogo_removebg_preview)
                                .into(imgAvatar);
                    }
                }
            });

    private boolean isValidEmail(String email) {
        return email != null && !email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return phone != null && !phone.isEmpty() && phone.matches("^(\\+84|0)[0-9]{9,10}$");
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private String getSelectedGender() {
        if (rbMale.isChecked()) return "Nam";
        if (rbFemale.isChecked()) return "Nữ";
        return "Khác";
    }

    private void showDatePicker(TextInputEditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                            dayOfMonth, month + 1, year);
                    targetEditText.setText(formatted);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
}
