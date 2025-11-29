package com.example.midterm.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.dto.NotificationWithEvent;
import com.example.midterm.view.Adapter.NotificationAdapter;
import com.example.midterm.viewModel.NotificationViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class NotificationsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvNotifications;
    private LinearLayout emptyState;
    private NotificationAdapter adapter;
    private NotificationViewModel viewModel;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Get current user ID from SharedPreferences
        currentUserId = getSharedPreferences("auth", MODE_PRIVATE).getInt("user_id", -1);
        if (currentUserId == -1) {
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupViewModel();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvNotifications = findViewById(R.id.rv_notifications);
        emptyState = findViewById(R.id.empty_state);

        // Setup toolbar
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this::onNotificationClick);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        viewModel.getNotificationsWithEventForUser(currentUserId).observe(this, notifications -> {
            if (notifications != null && !notifications.isEmpty()) {
                adapter.setNotifications(notifications);
                rvNotifications.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            } else {
                rvNotifications.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onNotificationClick(NotificationWithEvent notification) {
        // Navigate to event details when notification is clicked
        Intent intent = new Intent(this, EventDetail.class);
        intent.putExtra("event_id", notification.eventId);
        startActivity(intent);
    }
}