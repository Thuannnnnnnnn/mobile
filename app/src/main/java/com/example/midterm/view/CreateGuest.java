package com.example.midterm.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.Guest;
import com.example.midterm.model.entity.relations.EventGuestCrossRef;
import com.example.midterm.view.Adapter.GuestAdapter;
import com.example.midterm.viewModel.EventViewModel;
import com.example.midterm.viewModel.GuestViewModel;
import com.google.android.material.snackbar.Snackbar;
import android.content.res.ColorStateList;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGuest extends AppCompatActivity implements GuestAdapter.OnGuestActionListener {
    private TextInputLayout inputLayoutGuestName, inputLayoutGuestRole, inputLayoutGuestBio;
    private TextInputEditText editGuestName, editGuestBio, editSocialLink;
    private CircleImageView imgAvatar;
    private AutoCompleteTextView inputRole;
    private Button btnAddNew, btnFinishAdding;
    private ImageButton btnBack;
    private RecyclerView rvAddedGuests;
    private TextView tv_empty_guest_list;
    private View rootView;

    private GuestViewModel guestViewModel;
    private EventViewModel eventViewModel;
    private GuestAdapter guestAdapter;
    private StorageReference storageRef;
    private Uri selectedAvatarUri = null;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private long currentEventId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_guest);

        initViews();
        setupRoleDropdown();

        currentEventId = getIntent().getLongExtra("room_id", -1L);
        if (currentEventId == -1L) {
            finish();
            return;
        }

        storageRef = FirebaseStorage.getInstance().getReference();
        guestViewModel = new ViewModelProvider(this).get(GuestViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        guestAdapter = new GuestAdapter(this, this);
        rvAddedGuests.setLayoutManager(new LinearLayoutManager(this));
        rvAddedGuests.setAdapter(guestAdapter);

        guestViewModel.getGuestsForEvent((int) currentEventId).observe(this, guests -> {
            guestAdapter.setGuests(guests);
            if (guests == null || guests.isEmpty()) {
                rvAddedGuests.setVisibility(View.GONE);
                tv_empty_guest_list.setVisibility(View.VISIBLE);
            } else {
                rvAddedGuests.setVisibility(View.VISIBLE);
                tv_empty_guest_list.setVisibility(View.GONE);
            }
        });

        btnAddNew.setOnClickListener(v -> saveGuestAndContinue());
        imgAvatar.setOnClickListener(v -> selectAvatar());

        btnBack.setOnClickListener(v -> showCancelConfirmationDialog());

        btnFinishAdding.setOnClickListener(v -> {
             Intent intent = new Intent(this, CreateEventSection.class);
             intent.putExtra("room_id", currentEventId);
             startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onBackPressed() {
        showCancelConfirmationDialog();
    }

    private void initViews() {
        rootView = findViewById(R.id.main);
        imgAvatar = findViewById(R.id.img_guest_avatar);
        editGuestName = findViewById(R.id.edit_guest_name);
        editGuestBio = findViewById(R.id.edit_guest_bio);
        editSocialLink = findViewById(R.id.edit_guest_social);
        inputRole = findViewById(R.id.actv_guest_role);
        btnAddNew = findViewById(R.id.btn_save_and_add_new);
        btnFinishAdding = findViewById(R.id.btn_finish_adding);
        btnBack = findViewById(R.id.btn_back);
        rvAddedGuests = findViewById(R.id.rv_added_guests);
        tv_empty_guest_list = findViewById(R.id.tv_empty_guest_list);

        // Thêm các TextInputLayout
        inputLayoutGuestName = findViewById(R.id.input_guest_name);
        inputLayoutGuestRole = findViewById(R.id.input_guest_role);
        inputLayoutGuestBio = findViewById(R.id.input_guest_bio); // Thêm dòng này
    }

    private void setupRoleDropdown() {
        String[] roles = {"Diễn giả", "MC", "Ca sĩ", "Chuyên gia", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        inputRole.setAdapter(adapter);
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy tạo sự kiện?")
                .setMessage("Nếu bạn quay lại, sự kiện và tất cả khách mời đã thêm sẽ bị xóa. Bạn có chắc chắn muốn hủy không?")
                .setPositiveButton("Xóa & Hủy", (dialog, which) -> {
                    // Gọi hàm xóa sự kiện (Event)
                    deleteEventAndFinish();
                })
                .setNegativeButton("Tiếp tục thêm", null)
                .setIcon(R.drawable.warning)
                .show();
    }

    private void deleteEventAndFinish() {
        guestViewModel.deleteGuestsAndCrossRefsByEventId(currentEventId);
        eventViewModel.deleteEventById(currentEventId);
        showSnackbar("Đã hủy tạo sự kiện.", true);

        //Trở về homepage
        Intent intent = new Intent(CreateGuest.this, HomepageOrganizer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void saveGuestAndContinue() {
        // Xóa lỗi cũ
        inputLayoutGuestName.setError(null);
        inputLayoutGuestRole.setError(null);
        inputLayoutGuestBio.setError(null); // Thêm dòng này

        String name = editGuestName.getText().toString().trim();
        String role = inputRole.getText().toString().trim();
        String bio = editGuestBio.getText().toString().trim();
        String social = editSocialLink.getText().toString().trim();

        boolean hasError = false;
        if (name.isEmpty()) {
            inputLayoutGuestName.setError("Vui lòng nhập Tên khách mời");
            hasError = true;
        }
        if (role.isEmpty()) {
            inputLayoutGuestRole.setError("Vui lòng chọn vai trò khách mời");
            hasError = true;
        }
        if (bio.isEmpty()) {
            inputLayoutGuestBio.setError("Vui lòng nhập tiểu sử khách mời");
            hasError = true;
        }
        if (hasError) {
            return; // Dừng lại nếu có lỗi
        }
        if (selectedAvatarUri == null) {
            showSnackbar("Vui lòng thêm ảnh khách mời!", true);
            return; // Dừng lại nếu thiếu ảnh
        }

        btnAddNew.setEnabled(false);

        String filename = "avatarGuests/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(filename);

        imageRef.putFile(selectedAvatarUri)
                .addOnSuccessListener(task -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveGuestToDatabase(name, role, bio, social, imageUrl);
                }))
                .addOnFailureListener(e -> {
                    showSnackbar("Lỗi tải ảnh: " + e.getMessage(), true);
                    btnAddNew.setEnabled(true);
                });
    }

    private void saveGuestToDatabase(String name, String role, String bio, String social, String imageUrl) {
        Guest newGuest = new Guest(name, role, bio, social, imageUrl);

        new Thread(() -> {
            long guestId = guestViewModel.insertSync(newGuest);

            if (guestId > 0 && currentEventId != -1L) {
                EventGuestCrossRef crossRef = new EventGuestCrossRef((int) currentEventId, (int) guestId);
                guestViewModel.insertEventGuestCrossRef(crossRef);
            }
            runOnUiThread(() -> {
                showSnackbar("Đã thêm khách mời thành công!", false);
                clearInputs();
                btnAddNew.setEnabled(true);
            });
        }).start();
    }

    private void selectAvatar() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null && requestCode == PICK_IMAGE_REQUEST) {
            selectedAvatarUri = data.getData();
            imgAvatar.setImageURI(selectedAvatarUri);
        }
    }

    private void clearInputs() {
        editGuestName.setText("");
        inputRole.setText("");
        editGuestBio.setText("");
        editSocialLink.setText("");
        imgAvatar.setImageResource(R.drawable.unnamed_removebg_preview);
        selectedAvatarUri = null;

        inputLayoutGuestName.setError(null);
        inputLayoutGuestRole.setError(null);
        inputLayoutGuestBio.setError(null);
    }

    private void showSnackbar(String message, boolean isError) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        // Lấy view của Snackbar
        View snackbarView = snackbar.getView();

        int color;
        if (isError) {
            color = ContextCompat.getColor(this, R.color.colorError);
        } else {
            color = ContextCompat.getColor(this, R.color.colorSuccess);
        }
        snackbarView.setBackgroundTintList(ColorStateList.valueOf(color));
        snackbar.show();
    }

    @Override
    public void onEditClick(Guest guest) {
        showSnackbar("Chức năng chỉnh sửa: " + guest.getName(), false);
    }

    @Override
    public void onDeleteClick(Guest guest) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa khách mời")
                .setMessage("Bạn có chắc chắn muốn xóa " + guest.getName() + "?\n\n(Hành động này sẽ xóa khách mời khỏi sự kiện.)")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    guestViewModel.delete(guest);
                    showSnackbar("Đã xóa khách mời: " + guest.getName(), false);
                })
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.warning)
                .show();
    }
}