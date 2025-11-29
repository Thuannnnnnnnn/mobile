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
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.midterm.R;
import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.entity.Event;
import com.example.midterm.model.entity.Notification;
import com.example.midterm.viewModel.EventViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class CreateEvent extends AppCompatActivity {
    private TextInputLayout inputLayoutEventName, inputLayoutLocation, inputLayoutDate;
    private TextInputEditText inputEventName, inputDescription, inputDate, inputTime, inputLocation, inputDateEnd, inputTimeEnd;
    private TextView tvUploadHint, tvVideoUploadStatus;
    private ImageView imgBanner;
    private AutoCompleteTextView inputCategory;
    private Button btnNext, btnSelectVideo;
    private ImageButton btnBack;

    private ProgressBar pbBannerUpload, pbVideoUpload;
    private VideoView videoViewEvent;
    private View rootView;

    // Logic
    private EventViewModel eventViewModel;
    private int userId;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private StorageReference storageRef;
    private AppDatabase db; // Database instance

    // Trạng thái (State)
    private Uri selectedBannerUri = null;
    private Uri selectedVideoUri = null;
    private String eventUUID; // Dùng chung cho banner và video
    private String uploadedBannerUrl = null;
    private String uploadedVideoUrl = null;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String> videoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);

        db = AppDatabase.getInstance(getApplicationContext()); // Khởi tạo DB
        FirebaseStorage storage = FirebaseStorage.getInstance(); // Khởi tạo Firebase
        storageRef = storage.getReference();

        // Tạo UUID duy nhất cho sự kiện này ngay lập tức
        this.eventUUID = UUID.randomUUID().toString();

        initViews();
        setupResultLaunchers();


        // Xử lý nút back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showCancelConfirmationDialog();
            }
        });


        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            finish();
            return;
        }
        // Khởi tạo ViewModel
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        // Cài đặt các listeners
        setupDatePickers();
        setupCategoryDropdown();

        imgBanner.setOnClickListener(v -> selectImageFromGallery());
        btnSelectVideo.setOnClickListener(v -> selectVideoFromGallery());
        btnNext.setOnClickListener(v -> validateAndCreateEvent());
        btnBack.setOnClickListener(v -> showCancelConfirmationDialog());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupResultLaunchers() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedBannerUri = uri;
                        imgBanner.setImageURI(selectedBannerUri);
                        tvUploadHint.setVisibility(View.GONE);
                    }
                });

        videoPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedVideoUri = uri;
                        videoViewEvent.setVisibility(View.GONE); // Ẩn video cũ
                        startVideoUpload(); // Bắt đầu tải lên ngay
                    }
                });
    }


    private void initViews() {
        rootView = findViewById(R.id.main);
        imgBanner = findViewById(R.id.img_event_banner);
        inputEventName = findViewById(R.id.edit_event_name);
        inputDescription = findViewById(R.id.edit_event_description);
        inputCategory = findViewById(R.id.actv_category);
        inputDate = findViewById(R.id.et_date);
        inputTime = findViewById(R.id.et_time);
        inputDateEnd = findViewById(R.id.et_date_end);
        inputTimeEnd = findViewById(R.id.et_time_end);
        inputLocation = findViewById(R.id.edit_event_location);
        tvUploadHint = findViewById(R.id.tv_upload_hint);
        btnSelectVideo = findViewById(R.id.btn_select_video);
        btnNext = findViewById(R.id.btn_create_event_submit);
        btnBack = findViewById(R.id.btn_back);

        // View upload banner and video
        pbBannerUpload = findViewById(R.id.pb_banner_upload);
        pbVideoUpload = findViewById(R.id.pb_video_upload);
        tvVideoUploadStatus = findViewById(R.id.tv_video_upload_status);
        videoViewEvent = findViewById(R.id.video_view_event);

        // Views cho validation
        inputLayoutEventName = findViewById(R.id.input_event_name);
        inputLayoutLocation = findViewById(R.id.input_location);
        inputLayoutDate = findViewById(R.id.input_date);
    }

    ///Hiển thị cảnh báo xác nhận khi người dùng muốn Hủy

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy tạo sự kiện?")
                .setMessage("Bạn có chắc chắn muốn hủy? Mọi video hoặc hình ảnh đã tải lên sẽ bị xóa.")
                .setPositiveButton("Hủy bỏ", (dialog, which) -> deleteUploadedFilesAndFinish())
                .setNegativeButton("Tiếp tục", null)
                .setIcon(R.drawable.warning) // Cần có icon @drawable/warning
                .show();
    }

    //Gửi lệnh xóa các file đã tải lên Firebase và đóng Activity
    private void deleteUploadedFilesAndFinish() {
        // Chỉ xóa nếu người dùng đã chọn file
        // Không cần đợi xóa xong, chỉ cần gửi lệnh và thoát

        // 1. Xóa Banner (nếu đã chọn)
        if (selectedBannerUri != null) {
            StorageReference bannerRef = storageRef.child("events/" + this.eventUUID + "/banner.jpg");
            bannerRef.delete().addOnFailureListener(e -> Log.w("CreateEvent", "Không thể xóa banner mồ côi: ", e));
        }
        // 2. Xóa Video (nếu đã chọn)
        if (selectedVideoUri != null) {
            StorageReference videoRef = storageRef.child("events/" + this.eventUUID + "/video.mp4");
            videoRef.delete().addOnFailureListener(e -> Log.w("CreateEvent", "Không thể xóa video mồ côi: ", e));
        }
        finish();
    }

    private void showSnackbar(String message, boolean isError) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        int color = isError ? ContextCompat.getColor(this, R.color.colorError) : ContextCompat.getColor(this, R.color.colorSuccess);
        snackbarView.setBackgroundTintList(ColorStateList.valueOf(color));
        snackbar.show();
    }

    private void selectImageFromGallery() {
        imagePickerLauncher.launch("image/*");
    }

    private void selectVideoFromGallery() {
        videoPickerLauncher.launch("video/*");
    }


    //Tải video lên Firebase ngay sau khi chọn
    private void startVideoUpload() {
        if (selectedVideoUri == null) return;

        pbVideoUpload.setVisibility(View.VISIBLE);
        pbVideoUpload.setProgress(0);
        tvVideoUploadStatus.setText("Đang tải lên video...");
        StorageReference videoRef = storageRef.child("events/" + this.eventUUID + "/video.mp4");

        videoRef.putFile(selectedVideoUri)
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    pbVideoUpload.setProgress((int) progress);
                    tvVideoUploadStatus.setText(String.format(Locale.getDefault(), "Đang tải lên... %.0f%%", progress));
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw Objects.requireNonNull(task.getException());
                    return videoRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    pbVideoUpload.setVisibility(View.GONE);
                    uploadedVideoUrl = downloadUri.toString(); // Lưu URL
                    tvVideoUploadStatus.setText("Tải lên video thành công!");
                    showUploadedVideo(downloadUri); // Hiển thị video
                })
                .addOnFailureListener(e -> {
                    showSnackbar("Upload video thất bại: " + e.getMessage(), true);
                    pbVideoUpload.setVisibility(View.GONE);
                    tvVideoUploadStatus.setText("Tải lên thất bại.");
                });
    }

    //Kiểm tra các trường nhập liệu trước khi tạo sự kiện
    private void validateAndCreateEvent() {
        inputLayoutEventName.setError(null);
        inputLayoutLocation.setError(null);
        inputLayoutDate.setError(null);

        // Lấy dữ liệu
        String name = String.valueOf(inputEventName.getText()).trim();
        String location = String.valueOf(inputLocation.getText()).trim();
        String startDate = String.valueOf(inputDate.getText()).trim();

        boolean hasError = false;
        if (TextUtils.isEmpty(name)) {
            inputLayoutEventName.setError("Vui lòng nhập tên sự kiện");
            hasError = true;
        }
        if (TextUtils.isEmpty(location)) {
            inputLayoutLocation.setError("Vui lòng nhập địa điểm");
            hasError = true;
        }
        if (TextUtils.isEmpty(startDate)) {
            inputLayoutDate.setError("Vui lòng chọn ngày bắt đầu");
            hasError = true;
        }
        if (selectedBannerUri == null) {
            showSnackbar("Vui lòng chọn ảnh banner", true);
            hasError = true;
        }
        if (hasError) {
            return; // Dừng lại nếu có lỗi
        }

        // Vô hiệu hóa nút và bắt đầu upload banner
        btnNext.setEnabled(false);
        btnNext.setText("Đang xử lý...");
        uploadBannerAndCreateEvent();
    }

    private void uploadBannerAndCreateEvent() {
        pbBannerUpload.setVisibility(View.VISIBLE);
        pbBannerUpload.setProgress(0);
        StorageReference bannerRef = storageRef.child("events/" + this.eventUUID + "/banner.jpg");

        bannerRef.putFile(selectedBannerUri)
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    pbBannerUpload.setProgress((int) progress);
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw Objects.requireNonNull(task.getException());
                    return bannerRef.getDownloadUrl();
                })
                .addOnSuccessListener(bannerUri -> {
                    pbBannerUpload.setVisibility(View.GONE);
                    uploadedBannerUrl = bannerUri.toString(); // Lưu URL banner
                    createEvent(); // Tạo sự kiện
                })
                .addOnFailureListener(e -> {
                    showSnackbar("Upload banner thất bại: " + e.getMessage(), true);
                    btnNext.setEnabled(true);
                    btnNext.setText("Tạo Sự Kiện");
                });
    }

    //Hiển thị video đã tải lên trong VideoView
    private void showUploadedVideo(Uri videoUri) {
        videoViewEvent.setVisibility(View.VISIBLE);
        videoViewEvent.setVideoURI(videoUri);

        // Thêm điều khiển (play/pause)
        MediaController mediaController = new MediaController(this);
        videoViewEvent.setMediaController(mediaController);
        mediaController.setAnchorView(videoViewEvent);

        videoViewEvent.start(); // Tự động phát
    }

    //Tạo đối tượng Event và lưu vào Room
    private void createEvent() {
        String name = String.valueOf(inputEventName.getText()).trim();
        String desc = String.valueOf(inputDescription.getText()).trim();
        String category = String.valueOf(inputCategory.getText()).trim();
        String location = String.valueOf(inputLocation.getText()).trim();
        String startDate = String.valueOf(inputDate.getText()).trim() + " " + String.valueOf(inputTime.getText()).trim();
        String endDate = String.valueOf(inputDateEnd.getText()).trim() + " " + String.valueOf(inputTimeEnd.getText()).trim();

        // Lấy URL từ biến class
        String bannerUrlToSave = (uploadedBannerUrl != null) ? uploadedBannerUrl : "";
        String videoUrlToSave = (uploadedVideoUrl != null) ? uploadedVideoUrl : "";

        // Dùng eventUUID từ biến class
        Event event = new Event(this.eventUUID, userId, name, bannerUrlToSave, videoUrlToSave, desc, category, location, startDate,
                endDate,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime()),
                null, false // Mặc định isPromoted là false
        );

        eventViewModel.insert(event, newId -> runOnUiThread(() -> {
            // Gửi thông báo cho followers
            notifyFollowers(newId, userId, name);

            // Chuyển sang Activity tiếp theo (CreateGuest)
            Intent intent = new Intent(CreateEvent.this, CreateGuest.class);
            intent.putExtra("room_id", newId);
            startActivity(intent);

            finish();
        }));
    }

    // Sau khi insert Event thành công:
    private void notifyFollowers(long eventId, long organizerId, String eventName) {
        new Thread(() -> {
            // 1. Lấy danh sách user đang follow organizer này
            // Giả sử bạn có FollowedArtistDAO và phương thức getFollowerIdsByOrganizer
            // List<Long> followerIds = db.followedArtistDAO().getFollowerIdsByOrganizer(organizerId);

            // TODO: Bỏ comment dòng trên và xóa dòng dưới khi có DAO
            List<Long> followerIds = new ArrayList<>();


            List<Notification> notifications = new ArrayList<>();
            String content = "Nghệ sĩ bạn theo dõi vừa tạo sự kiện mới: " + eventName;

            for (Long followerId : followerIds) {
                Notification noti = new Notification();
                //noti.setUserId(followerId);
                noti.setTitle("Sự kiện mới!");
                noti.setMessage(content);
                //noti.setEventId(eventId);
                //noti.setCreatedAt(System.currentTimeMillis());
                noti.setRead(false);
                notifications.add(noti);
            }

            // 2. Lưu vào DB thông báo
            if (!notifications.isEmpty()) {
                // Giả sử bạn có NotificationDAO và phương thức insertAll
                // db.notificationDAO().insertAll(notifications);
            }
        }).start();
    }


    private void setupDatePickers() {
        inputDate.setOnClickListener(v -> showDatePicker(startCalendar, inputDate));
        inputDateEnd.setOnClickListener(v -> showDatePicker(endCalendar, inputDateEnd));
        inputTime.setOnClickListener(v -> showTimePicker(startCalendar, inputTime));
        inputTimeEnd.setOnClickListener(v -> showTimePicker(endCalendar, inputTimeEnd));
    }

    private void showDatePicker(Calendar calendar, TextInputEditText editText) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
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
        inputCategory.setAdapter(adapter);
    }
}