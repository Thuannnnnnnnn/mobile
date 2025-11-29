package com.example.midterm.view;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.midterm.R;
import com.example.midterm.model.entity.Organizer;
import com.example.midterm.viewModel.AccountViewModel;
import com.example.midterm.viewModel.OrganizerViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class OrganizerInfoActivity extends AppCompatActivity {
    private ImageView imgLogo;
    private TextView organizerUpdatedAt;
    private TextInputEditText organizerName, organizerEmail, organizerPhone, organizerWebsite, organizerAddress, organizerDescription;
    private Button btnSave;
    private ImageButton btnBack;
    private OrganizerViewModel organizerViewModel;
    private AccountViewModel accountViewModel;
    private int userId;
    private Uri logoUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_info);

        initViews();

        // Lấy user_id từ intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            finish();
            return;
        }
        organizerViewModel = new ViewModelProvider(this).get(OrganizerViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        organizerViewModel.observeOrganizerByAccountId(userId)
                .observe(this, organizer -> {
                    if (organizer != null) {
                        organizerViewModel.organizerLiveData.setValue(organizer);
                        renderOrganizer(organizer);
                    }
                });
        imgLogo.setOnClickListener(v -> {
            Intent pickImage = new Intent(Intent.ACTION_PICK);
            pickImage.setType("image/*");
            startActivityForResult(pickImage, 101);
        });

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v ->{
            Organizer currentOrganizer = organizerViewModel.organizerLiveData.getValue();
            if (currentOrganizer == null) return;

            boolean changed = false;

            String name = organizerName.getText().toString().trim();
            String website = organizerWebsite.getText().toString().trim();
            String address = organizerAddress.getText().toString().trim();
            String description = organizerDescription.getText().toString().trim();

            // Nếu logoUri khác null và khác với logo hiện tại → có thay đổi
            if (logoUri != null) {
                String newLogoPath = saveLogoToInternalStorage(logoUri);
                if (newLogoPath != null && !newLogoPath.equals(currentOrganizer.getLogo())) {
                    currentOrganizer.setLogo(newLogoPath);
                    changed = true;
                }
            }
            if (!name.equals(currentOrganizer.getOrganizerName())) {
                currentOrganizer.setOrganizerName(name);
                changed = true;
            }
            if (!website.equals(currentOrganizer.getWebsite())) {
                currentOrganizer.setWebsite(website);
                changed = true;
            }
            if (!address.equals(currentOrganizer.getAddress())) {
                currentOrganizer.setAddress(address);
                changed = true;
            }
            if (!description.equals(currentOrganizer.getDescription())) {
                currentOrganizer.setDescription(description);
                changed = true;
            }
            if (changed) {
                currentOrganizer.setUpdatedAt(System.currentTimeMillis());
                organizerViewModel.update(currentOrganizer);

                Snackbar.make(btnSave, "Cập nhật thành công!", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#4CAF50"))
                        .setTextColor(Color.WHITE)
                        .show();
            } else {
                Snackbar.make(btnSave, "Không có thay đổi nào để lưu.", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.GRAY)
                        .setTextColor(Color.WHITE)
                        .show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void renderOrganizer(Organizer organizer) {
        organizerName.setText(organizer.getOrganizerName());
        organizerWebsite.setText(organizer.getWebsite());
        organizerAddress.setText(organizer.getAddress());
        organizerDescription.setText(organizer.getDescription());
        organizerUpdatedAt.setText("Cập nhật: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date(organizer.getUpdatedAt())));

        if (organizer.getLogo() != null && !organizer.getLogo().isEmpty()) {
            File logoFile = new File(organizer.getLogo());
            Glide.with(this)
                    .load(logoFile)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.unnamed_removebg_preview)
                    .into(imgLogo);
        }
        // Hiển thị email/phone từ AccountViewModel
        accountViewModel.getAccountById(organizer.getOrganizerId()).observe(this, account -> {
            if (account != null) {
                organizerEmail.setText(account.getEmail());
                organizerPhone.setText(account.getPhone());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            logoUri = data.getData();
            if (logoUri != null) {
                // Copy ảnh vào bộ nhớ trong và lấy đường dẫn thực
                String newLogoPath = saveLogoToInternalStorage(logoUri);
                if (newLogoPath != null) {
                    imgLogo.setImageURI(logoUri); // hiển thị ngay

                    // Cập nhật luôn vào DB và LiveData để render lại
                    Organizer currentOrganizer = organizerViewModel.organizerLiveData.getValue();
                    if (currentOrganizer != null) {
                        currentOrganizer.setLogo(newLogoPath);
                        currentOrganizer.setUpdatedAt(System.currentTimeMillis());
                        organizerViewModel.update(currentOrganizer);

                        // cập nhật LiveData thủ công để tránh reload cũ
                        organizerViewModel.organizerLiveData.setValue(currentOrganizer);

                        Snackbar.make(imgLogo, "Logo đã được cập nhật!", Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(Color.parseColor("#4CAF50"))
                                .setTextColor(Color.WHITE)
                                .show();
                    }
                } else {
                    Snackbar.make(imgLogo, "Lỗi khi lưu logo!", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(Color.RED)
                            .setTextColor(Color.WHITE)
                            .show();
                }
            }
        }
    }

    private String saveLogoToInternalStorage(Uri uri) {
        try {
            File dir = new File(getFilesDir(), "organizer_logos");
            if (!dir.exists()) dir.mkdirs();

            File newFile = new File(dir, "logo_" + System.currentTimeMillis() + ".jpg");

            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(newFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return newFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initViews(){
        imgLogo = findViewById(R.id.img_logo);
        organizerName = findViewById(R.id.edit_organizer_register_name);
        organizerEmail = findViewById(R.id.edit_organizer_email);
        organizerPhone = findViewById(R.id.edit_user_phone);
        organizerWebsite = findViewById(R.id.edit_organizer_website);
        organizerAddress = findViewById(R.id.edit_organizer_address);
        organizerDescription = findViewById(R.id.edit_organizer_description);
        organizerUpdatedAt = findViewById(R.id.tv_updated_at);
        btnSave = findViewById(R.id.btn_save_profile);
        btnBack = findViewById(R.id.btn_back);
    }
}