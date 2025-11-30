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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;

public class CreateEvent extends AppCompatActivity {
    private TextInputLayout inputLayoutEventName, inputLayoutLocation, inputLayoutDate;
    private TextInputEditText inputEventName, inputDescription, inputDate, inputTime, inputLocation, inputDateEnd, inputTimeEnd;
    private TextView tvUploadHint;
    private ImageView imgBanner;
    private AutoCompleteTextView inputCategory;
    private Button btnNext, btnSelectVideo;
    private ImageButton btnBack;

    private ProgressBar pbProcessing; // Đổi tên từ pbBannerUpload để dùng chung
    private VideoView videoViewEvent;
    private View rootView;

    // Logic
    private EventViewModel eventViewModel;
    private int userId;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();

    // Trạng thái (State)
    private Uri selectedBannerUri = null;
    private Uri selectedVideoUri = null;
    private String eventUUID;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String> videoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);

        // Tạo UUID duy nhất cho sự kiện
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

        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        setupDatePickers();
        setupCategoryDropdown();

        imgBanner.setOnClickListener(v -> selectImageFromGallery());
        btnSelectVideo.setOnClickListener(v -> selectVideoFromGallery());
        btnNext.setOnClickListener(v -> validateAndSaveEvent()); // Đổi tên hàm
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
                        videoViewEvent.setVisibility(View.VISIBLE);
                        videoViewEvent.setVideoURI(selectedVideoUri);

                        MediaController mediaController = new MediaController(this);
                        videoViewEvent.setMediaController(mediaController);
                        mediaController.setAnchorView(videoViewEvent);
                        videoViewEvent.start();
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

        // Tận dụng lại ProgressBar cũ, bạn có thể đổi ID trong XML nếu muốn
        pbProcessing = findViewById(R.id.pb_banner_upload);
        videoViewEvent = findViewById(R.id.video_view_event);

        inputLayoutEventName = findViewById(R.id.input_event_name);
        inputLayoutLocation = findViewById(R.id.input_location);
        inputLayoutDate = findViewById(R.id.input_date);
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy tạo sự kiện?")
                .setMessage("Bạn có chắc chắn muốn hủy? Dữ liệu chưa lưu sẽ bị mất.")
                .setPositiveButton("Thoát", (dialog, which) -> finish())
                .setNegativeButton("Ở lại", null)
                .setIcon(R.drawable.warning)
                .show();
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

    private void validateAndSaveEvent() {
        inputLayoutEventName.setError(null);
        inputLayoutLocation.setError(null);
        inputLayoutDate.setError(null);

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

        if (hasError) return;

        // Bắt đầu lưu file và tạo sự kiện
        btnNext.setEnabled(false);
        btnNext.setText("Đang lưu dữ liệu...");
        pbProcessing.setVisibility(View.VISIBLE);

        // Xử lý lưu file trong background thread để tránh lag UI
        Executors.newSingleThreadExecutor().execute(() -> {
            String localBannerPath = saveFileToInternalStorage(selectedBannerUri, "banner_" + eventUUID + ".jpg");
            String localVideoPath = "";

            if (selectedVideoUri != null) {
                // Lưu video nếu có
                localVideoPath = saveFileToInternalStorage(selectedVideoUri, "video_" + eventUUID + ".mp4");
            }

            String finalLocalVideoPath = localVideoPath;

            // Quay lại UI thread để lưu vào DB
            runOnUiThread(() -> {
                createEventInDb(localBannerPath, finalLocalVideoPath);
            });
        });
    }

    /**
     * Hàm copy file từ Uri vào Internal Storage của ứng dụng
     */
    private String saveFileToInternalStorage(Uri uri, String fileName) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File directory = new File(getFilesDir(), "event_media"); // Tạo thư mục con
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, fileName);
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((inputStream != null) && ((length = inputStream.read(buffer)) > 0)) {
                outputStream.write(buffer, 0, length);
            }

            if (inputStream != null) inputStream.close();
            outputStream.close();

            return file.getAbsolutePath(); // Trả về đường dẫn tuyệt đối
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void createEventInDb(String bannerPath, String videoPath) {
        if (bannerPath == null) {
            showSnackbar("Lỗi khi lưu ảnh banner.", true);
            btnNext.setEnabled(true);
            btnNext.setText("Tiếp Tục");
            pbProcessing.setVisibility(View.GONE);
            return;
        }

        String name = String.valueOf(inputEventName.getText()).trim();
        String desc = String.valueOf(inputDescription.getText()).trim();
        String category = String.valueOf(inputCategory.getText()).trim();
        String location = String.valueOf(inputLocation.getText()).trim();
        String startDate = String.valueOf(inputDate.getText()).trim() + " " + String.valueOf(inputTime.getText()).trim();
        String endDate = String.valueOf(inputDateEnd.getText()).trim() + " " + String.valueOf(inputTimeEnd.getText()).trim();

        // Tạo đối tượng Event với đường dẫn file cục bộ
        Event event = new Event(
                this.eventUUID,
                userId,
                name,
                bannerPath, // Lưu đường dẫn file cục bộ
                videoPath,  // Lưu đường dẫn file cục bộ (nếu có)
                desc,
                category,
                location,
                startDate,
                endDate,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime()),
                null,
                false
        );

        eventViewModel.insert(event, newId -> runOnUiThread(() -> {
            pbProcessing.setVisibility(View.GONE);
            notifyFollowers(newId, userId, name);

            Intent intent = new Intent(CreateEvent.this, CreateGuest.class);
            intent.putExtra("room_id", newId);
            startActivity(intent);
            finish();
        }));
    }

    private void notifyFollowers(long eventId, long organizerId, String eventName) {
        new Thread(() -> {
            // Logic thông báo giữ nguyên (đang chờ DAO)
            List<Long> followerIds = new ArrayList<>();
            // List<Long> followerIds = db.followedArtistDAO().getFollowerIdsByOrganizer(organizerId);

            List<Notification> notifications = new ArrayList<>();
            String content = "Nghệ sĩ bạn theo dõi vừa tạo sự kiện mới: " + eventName;

            for (Long followerId : followerIds) {
                Notification noti = new Notification();
                // noti.setUserId(followerId);
                noti.setTitle("Sự kiện mới!");
                noti.setMessage(content);
                // noti.setEventId(eventId);
                noti.setRead(false);
                notifications.add(noti);
            }
            // if (!notifications.isEmpty()) db.notificationDAO().insertAll(notifications);
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