package com.example.midterm.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.midterm.R;
import com.example.midterm.model.entity.Event;
import com.example.midterm.viewModel.EventViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpdateEvent extends AppCompatActivity {
    private ImageButton btnBack;
    private Button btnUpdateSubmit, btnDeleteEvent, btnSelectVideo;
    private ImageView imgEventBanner;
    private TextView tvUploadHint, tvVideoUploadStatus;
    private ProgressBar pbBannerUpload, pbVideoUpload;
    private TextInputEditText editEventName, editEventDescription, etDate, etTime, etDateEnd, etTimeEnd, editEventLocation;
    private AutoCompleteTextView actvCategory;
    private VideoView videoViewEvent;
    private View rootView;

    private EventViewModel eventViewModel;
    // Data
    private int currentEventId = -1;
    private Event currentEvent; // Biến để lưu trữ sự kiện đang được chỉnh sửa
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();

    // Firebase
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Trạng thái file mới (để phân biệt với file cũ)
    private Uri newBannerUri = null;
    private Uri newVideoUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_event);

        currentEventId = getIntent().getIntExtra("event_id", -1);
        if (currentEventId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sự kiện.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Khởi tạo Firebase
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        initViews();
        initViewModels();
        setupClickListeners();
        setupCategoryDropdown();
        loadEventData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        rootView = findViewById(R.id.main);
        btnBack = findViewById(R.id.btn_back);
        btnUpdateSubmit = findViewById(R.id.btn_update_event_submit);
        btnDeleteEvent = findViewById(R.id.btn_delete_event);
        imgEventBanner = findViewById(R.id.img_event_banner);
        tvUploadHint = findViewById(R.id.tv_upload_hint);
        pbBannerUpload = findViewById(R.id.pb_banner_upload);
        editEventName = findViewById(R.id.edit_event_name);
        btnSelectVideo = findViewById(R.id.btn_select_video);
        tvVideoUploadStatus = findViewById(R.id.tv_video_upload_status);
        pbVideoUpload = findViewById(R.id.pb_video_upload);
        videoViewEvent = findViewById(R.id.video_view_event);
        editEventDescription = findViewById(R.id.edit_event_description);
        actvCategory = findViewById(R.id.actv_category);
        etDate = findViewById(R.id.et_date);
        etTime = findViewById(R.id.et_time);
        etDateEnd = findViewById(R.id.et_date_end);
        etTimeEnd = findViewById(R.id.et_time_end);
        editEventLocation = findViewById(R.id.edit_event_location);
    }

    private void initViewModels() {
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
    }

    /*** Tải dữ liệu sự kiện từ ViewModel và đổ lên UI*/
    private void loadEventData() {
        // Dùng hàm getEventWithGuests (vì nó đã tồn tại trong ViewModel của bạn)
        eventViewModel.getEventWithGuests(currentEventId).observe(this, eventWithGuests -> {
            if (eventWithGuests == null || eventWithGuests.event == null) {
                // Sự kiện có thể đã bị xóa ở đâu đó
                Toast.makeText(this, "Không thể tải dữ liệu sự kiện.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            this.currentEvent = eventWithGuests.event;
            populateUI(this.currentEvent);
        });
    }

    /*** Đổ dữ liệu từ Event object lên các trường input*/
    private void populateUI(Event event) {
        // Đổ Banner
        if (event.getBannerUrl() != null && !event.getBannerUrl().isEmpty()) {
            tvUploadHint.setVisibility(View.GONE);
            Glide.with(this).load(event.getBannerUrl()).into(imgEventBanner);
        }

        // Đổ các trường Text
        editEventName.setText(event.getEventName());
        editEventDescription.setText(event.getDescription());
        actvCategory.setText(event.getGenre(), false); // false để không xổ dropdown
        editEventLocation.setText(event.getLocation());

        // Đổ Video
        if (event.getVideoUrl() != null && !event.getVideoUrl().isEmpty()) {
            videoViewEvent.setVideoPath(event.getVideoUrl());
            videoViewEvent.setVisibility(View.VISIBLE);
            tvVideoUploadStatus.setText("Video đã được tải lên.");

            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoViewEvent);
            videoViewEvent.setMediaController(mediaController);
        }

        setDateTimeFields(event.getStartDate(), etDate, etTime, startCalendar);
        setDateTimeFields(event.getEndDate(), etDateEnd, etTimeEnd, endCalendar);
    }

    /*** Helper: Tách chuỗi "yyyy-MM-dd HH:mm" và cập nhật Calendar */
    private void setDateTimeFields(String dateTimeString, TextInputEditText dateField, TextInputEditText timeField, Calendar calendar) {
        if (dateTimeString == null || dateTimeString.isEmpty()) return;
        try {
            SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date date = inputSdf.parse(dateTimeString);
            if (date == null) return;

            calendar.setTime(date); // Cập nhật Calendar để Picker mở đúng ngày

            // Dùng định dạng yyyy-MM-dd
            SimpleDateFormat outputDateSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputTimeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            dateField.setText(outputDateSdf.format(date));
            timeField.setText(outputTimeSdf.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /*** Cài đặt các trình lắng nghe sự kiện click*/
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnUpdateSubmit.setOnClickListener(v -> handleUpdateEvent());
        btnDeleteEvent.setOnClickListener(v -> showDeleteConfirmationDialog());

        setupDatePickers();

        imgEventBanner.setOnClickListener(v -> selectImageFromGallery());
        btnSelectVideo.setOnClickListener(v -> selectVideoFromGallery());
    }


    private void showSnackbar(String message, boolean isError) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        int color = isError ? ContextCompat.getColor(this, R.color.colorError) : ContextCompat.getColor(this, R.color.colorSuccess);
        snackbarView.setBackgroundTintList(ColorStateList.valueOf(color));
        snackbar.show();
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1001);
    }

    private void selectVideoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, 1002);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == 1001) { // Banner MỚI
                newBannerUri = data.getData();
                imgEventBanner.setImageURI(newBannerUri); // Hiển thị preview
                tvUploadHint.setVisibility(View.GONE);
            } else if (requestCode == 1002) { // Video MỚI
                newVideoUri = data.getData();
                // Hiển thị preview
                showUploadedVideo(newVideoUri, true);
                tvVideoUploadStatus.setText("Đã chọn video mới. Nhấn 'Lưu' để tải lên.");
            }
        }
    }
    //Hiển thị video trong VideoView
    private void showUploadedVideo(Uri videoUri, boolean autoPlay) {
        videoViewEvent.setVisibility(View.VISIBLE);
        videoViewEvent.setVideoURI(videoUri);

        MediaController mediaController = new MediaController(this);
        videoViewEvent.setMediaController(mediaController);
        mediaController.setAnchorView(videoViewEvent);
        if(autoPlay) {
            videoViewEvent.start(); // Tự động phát
        }
    }

    /*** Logic cho nút "Lưu Thay Đổi"*/
    private void handleUpdateEvent() {
        if (currentEvent == null) return;

        String eventName = editEventName.getText().toString().trim();
        String location = editEventLocation.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();

        String startDate = etDate.getText().toString().trim() + " " + etTime.getText().toString().trim();
        String endDate = etDateEnd.getText().toString().trim() + " " + etDateEnd.getText().toString().trim();

        if (TextUtils.isEmpty(eventName) || TextUtils.isEmpty(location) || TextUtils.isEmpty(etDate.getText().toString())) {
            Toast.makeText(this, "Tên, Địa điểm và Ngày bắt đầu không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpdateSubmit.setEnabled(false);
        btnDeleteEvent.setEnabled(false);
        btnUpdateSubmit.setText("Đang xử lý...");

        // Bắt đầu chuỗi cập nhật (Tải banner mới nếu có)
        uploadNewBanner();
    }

    /*** Tải banner mới (nếu có)*/
    private void uploadNewBanner() {
        // Kiểm tra xem người dùng có chọn banner mới không
        if (newBannerUri != null) {
            pbBannerUpload.setVisibility(View.VISIBLE);
            pbBannerUpload.setProgress(0);

            StorageReference bannerRef = storageRef.child("events/" + currentEvent.getEventID() + "/banner.jpg");

            bannerRef.putFile(newBannerUri)
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        pbBannerUpload.setProgress((int) progress);
                    })
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) throw task.getException();
                        return bannerRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(bannerUri -> {
                        pbBannerUpload.setVisibility(View.GONE);
                        currentEvent.setBannerUrl(bannerUri.toString()); // Cập nhật URL mới
                        uploadNewVideo(); // Đi tới bước 2
                    })
                    .addOnFailureListener(e -> {
                        showSnackbar("Upload banner mới thất bại: " + e.getMessage(), true);
                        resetButtons();
                    });
        } else {
            // Không có banner mới, đi thẳng tới bước 2
            uploadNewVideo();
        }
    }

    /***Tải video mới (nếu có)*/
    private void uploadNewVideo() {
        if (newVideoUri != null) {
            pbVideoUpload.setVisibility(View.VISIBLE);
            pbVideoUpload.setProgress(0);
            tvVideoUploadStatus.setText("Đang tải lên video mới...");

            StorageReference videoRef = storageRef.child("events/" + currentEvent.getEventID() + "/video.mp4");

            videoRef.putFile(newVideoUri)
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        pbVideoUpload.setProgress((int) progress);
                    })
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) throw task.getException();
                        return videoRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(videoUri -> {
                        pbVideoUpload.setVisibility(View.GONE);
                        tvVideoUploadStatus.setText("Tải lên video mới thành công!");
                        currentEvent.setVideoUrl(videoUri.toString()); // Cập nhật URL mới
                        saveEventToDatabase(); // Đi tới bước 3
                    })
                    .addOnFailureListener(e -> {
                        showSnackbar("Upload video mới thất bại: " + e.getMessage(), true);
                        resetButtons();
                    });
        } else {
            // Không có video mới, đi thẳng tới bước 3
            saveEventToDatabase();
        }
    }

    /***Cập nhật các trường text và lưu vào Room*/
    private void saveEventToDatabase() {
        String eventName = editEventName.getText().toString().trim();
        String description = editEventDescription.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String location = editEventLocation.getText().toString().trim();

        String startDate = etDate.getText().toString().trim() + " " + etTime.getText().toString().trim();
        String endDate = etDateEnd.getText().toString().trim() + " " + etTimeEnd.getText().toString().trim();

        currentEvent.setEventName(eventName);
        currentEvent.setDescription(description);
        currentEvent.setGenre(category);
        currentEvent.setLocation(location);
        currentEvent.setStartDate(startDate);
        currentEvent.setEndDate(endDate);

        eventViewModel.update(currentEvent);  // Gọi ViewModel

        showSnackbar("Đã cập nhật sự kiện!", false);
        finish(); // Quay lại trang chi tiết
    }

    private void resetButtons() {
        btnUpdateSubmit.setEnabled(true);
        btnDeleteEvent.setEnabled(true);
        btnUpdateSubmit.setText("Lưu Thay Đổi");
    }

    /*** Hiển thị hộp thoại xác nhận Xóa*/
    private void showDeleteConfirmationDialog() {
        if (currentEvent == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Xóa sự kiện?")
                .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn sự kiện '" + currentEvent.getEventName() + "' không? Mọi dữ liệu (vé, sơ đồ,...) VÀ file (banner, video) sẽ bị xóa.")
                .setIcon(R.drawable.warning)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteEventFilesFromFirebase();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /*** Xóa file trên Firebase VÀ dữ liệu trên Room*/
    private void deleteEventFilesFromFirebase() {
        // SỬA LỖI PHÒNG THỦ CRASH
        if (videoViewEvent != null) {
            videoViewEvent.stopPlayback();
            videoViewEvent.setMediaController(null);
            videoViewEvent.setVideoURI(null);
        }
        String eventUUID = currentEvent.getEventID();
        if (eventUUID == null || eventUUID.isEmpty()) {
            // Nếu không có UUID, chỉ xóa Room
            deleteEventFromRoom();
            return;
        }
        StorageReference eventFolderRef = storageRef.child("events/" + eventUUID);
        // Xóa Banner
        eventFolderRef.child("banner.jpg").delete().addOnFailureListener(e ->
                Log.w("UpdateEvent", "Không tìm thấy banner để xóa hoặc lỗi: " + e.getMessage())
        );
        // Xóa Video
        eventFolderRef.child("video.mp4").delete().addOnFailureListener(e ->
                Log.w("UpdateEvent", "Không tìm thấy video để xóa hoặc lỗi: " + e.getMessage())
        );
        deleteEventFromRoom(); // Xóa Room (không cần đợi Firebase xóa xong)
    }

    private void deleteEventFromRoom() {
        eventViewModel.deleteEventById(currentEvent.getId());
        Intent intent = new Intent(this, HomepageOrganizer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void setupDatePickers() {
        etDate.setOnClickListener(v -> showDatePicker(startCalendar, etDate));
        etDateEnd.setOnClickListener(v -> showDatePicker(endCalendar, etDateEnd));
        etTime.setOnClickListener(v -> showTimePicker(startCalendar, etTime));
        etTimeEnd.setOnClickListener(v -> showTimePicker(endCalendar, etTimeEnd));
    }
    private void showDatePicker(Calendar calendar, TextInputEditText editText) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            // Dùng định dạng yyyy-MM-dd (Giống CreateEvent)
            editText.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void showTimePicker(Calendar calendar, TextInputEditText editText) {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            editText.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }
    private void setupCategoryDropdown() {
        String[] categories = {"Âm nhạc", "Thể thao", "Hội thảo", "Nghệ thuật", "Giáo dục"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(adapter);
    }
}