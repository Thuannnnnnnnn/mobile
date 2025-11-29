package com.example.midterm.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.midterm.R;
import com.example.midterm.model.entity.Event;
import com.example.midterm.view.Adapter.ViewEventDetailsOrganizerAdapter;
import com.example.midterm.viewModel.EventViewModel;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewEventDetailsOrganizer extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageButton btnBack, btnSendNotification;
    private ImageView  imgEdit;
    private TextView tvEventTitle, tvEventStatus;
    private EventViewModel eventViewModel;
    private int currentEventId = -1;
    private int currentOrganizerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_event_details_organizer);

        currentEventId = getIntent().getIntExtra("event_id", -1);
        if (currentEventId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sự kiện", Toast.LENGTH_SHORT).show(); // Thêm Toast báo lỗi
            finish();
            return;
        }

        initViews();

        ViewEventDetailsOrganizerAdapter adapter = new ViewEventDetailsOrganizerAdapter(this, currentEventId);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Tổng Quan");
                    break;
                case 1:
                    tab.setText("Quản lý Vé");
                    break;
                case 2:
                    tab.setText("Người Tham Dự");
                    break;
                case 3:
                    tab.setText("Sơ đồ");
                    break;
            }
        }).attach();

        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        loadHeaderData();

        btnBack.setOnClickListener(v -> finish());

        imgEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ViewEventDetailsOrganizer.this, UpdateEvent.class);
            intent.putExtra("event_id", currentEventId);
            startActivity(intent);
        });

        btnSendNotification.setOnClickListener(v -> showSendNotificationDialog());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        btnBack = findViewById(R.id.btn_back);
        btnSendNotification = findViewById(R.id.btn_send_notification);
        tvEventTitle = findViewById(R.id.tv_event_title);
        tvEventStatus = findViewById(R.id.tv_event_status);
        imgEdit = findViewById(R.id.imgEdit);

        currentOrganizerId = getSharedPreferences("auth", MODE_PRIVATE).getInt("user_id", -1);
    }

    private void loadHeaderData() {
        eventViewModel.getEventWithGuests(currentEventId).observe(this, eventWithGuests -> {
            if (eventWithGuests != null && eventWithGuests.event != null) {
                Event event = eventWithGuests.event;
                tvEventTitle.setText(event.getEventName());

                updateEventStatus(event.getStartDate(), event.getEndDate());
            } else {
                tvEventTitle.setText("Không tìm thấy sự kiện");
            }
        });
    }

    private void updateEventStatus(String startDateString, String endDateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date startDate = sdf.parse(startDateString);
            Date endDate = sdf.parse(endDateString);
            Date now = new Date(); // Thời gian hiện tại

            if (startDate == null || endDate == null) {
                throw new ParseException("Ngày không hợp lệ", 0);
            }

            if (now.before(startDate)) {
                // Sắp diễn ra (chưa tới giờ bắt đầu)
                tvEventStatus.setText("SẮP DIỄN RA");
                tvEventStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.coming_soon, 0, 0, 0);
            } else if (now.after(startDate) && now.before(endDate)) {
                // Đang diễn ra (nằm giữa start và end)
                tvEventStatus.setText("ĐANG DIỄN RA");
                tvEventStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.open, 0, 0, 0);
            } else {
                // Đã kết thúc (now > end)
                tvEventStatus.setText("ĐÃ KẾT THÚC");
                tvEventStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.closed, 0, 0, 0);
            }
        } catch (ParseException e) {
            tvEventStatus.setText("Không rõ");
            tvEventStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }
    private void showSendNotificationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_send_notification, null);
        EditText etTitle = dialogView.findViewById(R.id.et_notification_title);
        EditText etMessage = dialogView.findViewById(R.id.et_notification_message);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Gửi thông báo đến người tham gia")
                .setView(dialogView)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String message = etMessage.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (message.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sendNotificationToAttendees(title, message);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void sendNotificationToAttendees(String title, String message) {
        new Thread(() -> {
            try {
                com.example.midterm.model.data.local.AppDatabase db =
                        com.example.midterm.model.data.local.AppDatabase.getInstance(this);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                String currentTime = dateFormat.format(new Date());

                // Count attendees (people who bought tickets for this event)
                int attendeeCount = db.ticketDAO().getUniqueAttendeeCountSync(currentEventId);

                // Create notification
                com.example.midterm.model.entity.Notification notification =
                        new com.example.midterm.model.entity.Notification();
                notification.setEventId(currentEventId);
                notification.setOrganizerId(currentOrganizerId);
                notification.setTitle(title);
                notification.setMessage(message);
                notification.setNotificationType("broadcast");
                notification.setSentAt(currentTime);
                notification.setRecipientCount(attendeeCount);
                notification.setStatus("sent");
                notification.setCreatedAt(currentTime);

                db.notificationDAO().insert(notification);

                runOnUiThread(() -> {
                    Toast.makeText(this,
                            "Đã gửi thông báo đến " + attendeeCount + " người tham gia",
                            Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi gửi thông báo: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}