package com.example.midterm.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
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
import com.example.midterm.model.entity.Organizer;
import com.example.midterm.viewModel.AccountViewModel;
import com.example.midterm.viewModel.OrganizerViewModel;
import com.example.midterm.viewModel.UserProfileViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterOrganizer extends AppCompatActivity {
    TextInputEditText editOrganizerName, editOrganizerEmail, editOrganizerPhone, editOrganizerDescription,
            editOrganizerAddress, editOrganizerWebsite;
    CircleImageView imgLogo;
    ImageButton btnBack;
    Button  btnSubmitRequest;
    TextView tvErrorOrganizerName, tvErrorOrganizerAddress, tvErrorOrganizerEmail, tvErrorOrganizerPhone;

    private OrganizerViewModel organizerViewModel;
    private AccountViewModel accountViewModel;
    private UserProfileViewModel userProfileViewModel;
    // Danh sách domain hợp lệ
    private static final String[] ALLOWED_DOMAINS = {"org.com", "company.vn", "student.tdtu.edu.vn"};
    private int userId;
    private Uri logoUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_organizer);

        initViews();
        setUpRealtimeValidation();

        organizerViewModel = new ViewModelProvider(this).get(OrganizerViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        // Lấy user_id đã lưu sau đăng nhập
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId != -1) {
            accountViewModel.getAccountById(userId).observe(this, account -> {
                if (account != null) {
                    if (account.getEmail() != null) editOrganizerEmail.setText(account.getEmail());
                    if (account.getPhone() != null) editOrganizerPhone.setText(account.getPhone());
                }
            });
        }

        btnBack.setOnClickListener(v -> finish());
        btnSubmitRequest.setOnClickListener(v -> submitRegistration());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void initViews(){
        imgLogo = findViewById(R.id.img_logo_picker);
        editOrganizerName = findViewById(R.id.edit_organizer_register_name);
        editOrganizerEmail = findViewById(R.id.edit_organizer_register_email);
        editOrganizerPhone = findViewById(R.id.edit_organizer_register_phone);
        editOrganizerDescription = findViewById(R.id.edit_organizer_register_description);
        editOrganizerAddress = findViewById(R.id.edit_organizer_register_address);
        editOrganizerWebsite = findViewById(R.id.edit_organizer_register_website);
        tvErrorOrganizerName = findViewById(R.id.tvErrorOrganizerName);
        tvErrorOrganizerAddress = findViewById(R.id.tvErrorOrganizerAddress);
        tvErrorOrganizerEmail = findViewById(R.id.tvErrorOrganizerEmail);
        tvErrorOrganizerPhone = findViewById(R.id.tvErrorOrganizerPhone);
        btnBack = findViewById(R.id.btn_back);
        btnSubmitRequest = findViewById(R.id.btn_submit_request);

        // Nếu muốn logo mặc định
        imgLogo.setImageResource(R.drawable.catlogo_removebg_preview);

        // Cho phép chọn logo
        imgLogo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 101); // 101 là request code
        });
    }

    private void setUpRealtimeValidation(){
        editOrganizerName.addTextChangedListener(new TextWatcher() {
               @Override
               public void afterTextChanged(Editable s) {}
               @Override
               public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
               @Override
               public void onTextChanged(CharSequence s, int start, int before, int count) {
                   validateOrganizerName(s.toString());
               }
           }
        );

        editOrganizerAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateAddress(s.toString());
            }
        });

    }
    private void validateOrganizerName(String name) {
        if (name.trim().isEmpty()) {
            tvErrorOrganizerName.setText("Vui lòng nhập tên tổ chức");
            tvErrorOrganizerName.setVisibility(View.VISIBLE);
        } else {
            tvErrorOrganizerName.setVisibility(View.GONE);
        }
    }
    private void validateAddress(String address) {
        if (address.trim().isEmpty()) {
            tvErrorOrganizerAddress.setText("Vui lòng nhập địa chỉ tổ chức");
            tvErrorOrganizerAddress.setVisibility(View.VISIBLE);
        } else {
            tvErrorOrganizerAddress.setVisibility(View.GONE);
        }
    }

    private boolean validateForm() {
        validateOrganizerName(editOrganizerName.getText().toString());
        validateAddress(editOrganizerAddress.getText().toString());

        boolean noError =
                tvErrorOrganizerName.getVisibility() == View.GONE &&
                        tvErrorOrganizerAddress.getVisibility() == View.GONE;

        return noError;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            logoUri = data.getData();
            imgLogo.setImageURI(logoUri); // Hiển thị lên ImageView
        }
    }
    private void submitRegistration() {
        String name = editOrganizerName.getText().toString().trim();
        String email = editOrganizerEmail.getText().toString().trim();
        String phone = editOrganizerPhone.getText().toString().trim();
        String description = editOrganizerDescription.getText().toString().trim();
        String address = editOrganizerAddress.getText().toString().trim();
        String website = editOrganizerWebsite.getText().toString().trim();

        if (!validateForm()) {
            Snackbar.make(btnSubmitRequest, "Vui lòng nhập đầy đủ thông tin !", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.parseColor("#ED2A2A"))
                    .setTextColor(Color.WHITE)
                    .show();
            return;
        }

        // Kiểm tra email/phone không trùng với account khác
        accountViewModel.checkEmailOrPhoneExist(email, phone, userId, exist -> runOnUiThread(() -> {
            if (exist) {
                Snackbar.make(btnSubmitRequest, "Email hoặc số điện thoại đã tồn tại!", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#ED2A2A"))
                        .setTextColor(Color.WHITE)
                        .show();
                return;
            }

            // Kiểm tra domain email
            boolean approved = false;
            if (!email.isEmpty() && email.contains("@")) {
                String domain = email.substring(email.indexOf("@") + 1);
                for (String allowed : ALLOWED_DOMAINS) {
                    if (domain.equalsIgnoreCase(allowed)) {
                        approved = true;
                        break;
                    }
                }
            }
            final boolean isApprovedDomain = approved;
            final String status = isApprovedDomain ? "approved" : "pending";

            String logoPath = null;
            if (logoUri != null) {
                logoPath = saveImageToInternalStorage(logoUri);
            }
            // Tạo đối tượng Organizer
            final Organizer organizer = new Organizer(userId, name, logoPath
                    , website, address, description, status, System.currentTimeMillis());

            // Gửi yêu cầu qua ViewModel
            organizerViewModel.registerOrganizer(organizer, message -> runOnUiThread(() -> {

                if (message.contains("đã gửi yêu cầu trước đó")) {
                    Snackbar.make(btnSubmitRequest, message, Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#ED2A2A"))
                            .setTextColor(Color.WHITE)
                            .show();
                    return;
                }

                // Nếu đăng ký mới thành công
                if (isApprovedDomain) {
                    accountViewModel.updateRole(userId, "organizer");
                    userProfileViewModel.deleteByUserId(userId);

                    Snackbar.make(btnSubmitRequest, "Đăng ký thành công! Bạn đã trở thành tổ chức.", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#4CAF50"))
                            .setTextColor(Color.WHITE)
                            .show();
                } else {
                    Snackbar.make(btnSubmitRequest, "Yêu cầu đang chờ duyệt. Chúng tôi sẽ xem xét sớm!", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#FFA000"))
                            .setTextColor(Color.WHITE)
                            .show();
                }
            }));
        }));
    }
    private String saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File directory = new File(getFilesDir(), "organizer_logos");
            if (!directory.exists()) directory.mkdirs();

            File file = new File(directory, "logo_" + System.currentTimeMillis() + ".jpg");
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath(); // Trả về đường dẫn thật của file
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}