package com.example.midterm.view;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.Notification;
import com.example.midterm.utils.OTPUtils;
import com.example.midterm.view.Adapter.NotificationHistoryAdapter;
import com.example.midterm.viewModel.EventViewModel;
import com.example.midterm.viewModel.NotificationViewModel;
import com.example.midterm.viewModel.TicketViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class NotificationBroadcastActivity extends AppCompatActivity {
    private TextView tvEventName, tvRecipientCount, tvEmptyHistory;
    private TextInputLayout inputLayoutTitle, inputLayoutMessage;
    private TextInputEditText editTitle, editMessage;
    private AutoCompleteTextView spinnerType;
    private Button btnSend;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private RecyclerView rvHistory;
    private NotificationViewModel notificationViewModel;
    private EventViewModel eventViewModel;
    private TicketViewModel ticketViewModel;

    private NotificationHistoryAdapter historyAdapter;

    private int eventId;
    private String eventName;
    private int userId;
    private int recipientCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_broadcast);

        initViews();
        setupSpinner();
        setupRecyclerView();

        eventId = getIntent().getIntExtra("event_id", -1);
        eventName = getIntent().getStringExtra("event_name");

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (eventId == -1 || userId == -1) {
            finish();
            return;
        }

        tvEventName.setText(eventName != null ? eventName : "Sự kiện");

        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        ticketViewModel = new ViewModelProvider(this).get(TicketViewModel.class);

        loadData();

        btnSend.setOnClickListener(v -> sendNotification());
        btnBack.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        tvEventName = findViewById(R.id.tv_event_name);
        tvRecipientCount = findViewById(R.id.tv_recipient_count);
        tvEmptyHistory = findViewById(R.id.tv_empty_history);
        inputLayoutTitle = findViewById(R.id.input_layout_title);
        inputLayoutMessage = findViewById(R.id.input_layout_message);
        editTitle = findViewById(R.id.edit_title);
        editMessage = findViewById(R.id.edit_message);
        spinnerType = findViewById(R.id.spinner_type);
        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);
        rvHistory = findViewById(R.id.rv_history);
    }

    private void setupSpinner() {
        String[] types = {"Thông báo chung", "Nhắc nhở", "Cập nhật", "Hủy sự kiện"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types);
        spinnerType.setAdapter(adapter);
        spinnerType.setText(types[0], false);
    }

    private void setupRecyclerView() {
        historyAdapter = new NotificationHistoryAdapter(this, new ArrayList<>());
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }

    private void loadData() {
        // Lấy số luượng người tham gia (buyer)
        ticketViewModel.countTicketsSoldForEvent(eventId).observe(this, count -> {
            if (count != null) {
                recipientCount = count;
                tvRecipientCount.setText(String.format("Số người nhận: %d", count));
            }
        });

        // Load lịch sử thông báo đã gửi đi
        notificationViewModel.getNotificationsByEvent(eventId).observe(this, notifications -> {
            if (notifications != null && !notifications.isEmpty()) {
                historyAdapter.updateData(notifications);
                rvHistory.setVisibility(View.VISIBLE);
                tvEmptyHistory.setVisibility(View.GONE);
            } else {
                rvHistory.setVisibility(View.GONE);
                tvEmptyHistory.setVisibility(View.VISIBLE);
            }
        });
    }

    private void sendNotification() {
        inputLayoutTitle.setError(null);
        inputLayoutMessage.setError(null);

        String title = editTitle.getText().toString().trim();
        String message = editMessage.getText().toString().trim();
        String type = getNotificationType(spinnerType.getText().toString());

        boolean hasError = false;
        if (TextUtils.isEmpty(title)) {
            inputLayoutTitle.setError("Vui lòng nhập tiêu đề");
            hasError = true;
        }
        if (TextUtils.isEmpty(message)) {
            inputLayoutMessage.setError("Vui lòng nhập nội dung");
            hasError = true;
        }
        if (hasError) return;

        if (recipientCount == 0) {
            Snackbar.make(btnSend, "Không có người nhận. Sự kiện chưa có ai mua vé.", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.parseColor("#FFA000"))
                    .setTextColor(Color.WHITE)
                    .show();
            return;
        }

        btnSend.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Tạo thông báo
        Notification notification = new Notification(eventId, userId,title,message, type,
                OTPUtils.getCurrentTimestamp(), recipientCount, "sent", OTPUtils.getCurrentTimestamp()
        );

        // Lưu vào database
        notificationViewModel.insertNotification(notification, id -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnSend.setEnabled(true);

                if (id > 0) {
                    Snackbar.make(btnSend, "Đã gửi thông báo đến " + recipientCount + " người", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#4CAF50"))
                            .setTextColor(Color.WHITE)
                            .show();

                    editTitle.setText("");
                    editMessage.setText("");
                } else {
                    Snackbar.make(btnSend, "Lỗi khi gửi thông báo", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#ED2A2A"))
                            .setTextColor(Color.WHITE)
                            .show();
                }
            });
        });
    }
    private String getNotificationType(String displayType) {
        switch (displayType) {
            case "Nhắc nhở":
                return "reminder";
            case "Cập nhật":
                return "update";
            case "Hủy sự kiện":
                return "cancellation";
            default:
                return "broadcast";
        }
    }
}
