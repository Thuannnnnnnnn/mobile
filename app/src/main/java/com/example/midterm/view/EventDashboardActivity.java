package com.example.midterm.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
import com.example.midterm.model.entity.Event;
import com.example.midterm.view.Adapter.EventDashboardAdapter;
import com.example.midterm.viewModel.EventViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class EventDashboardActivity extends AppCompatActivity implements EventDashboardAdapter.OnEventClickListener {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private TextView tvEmptyState, tvTotalEvents, tvTotalRevenue, tvTotalTickets;
    private FloatingActionButton fabCreateEvent;
    private ImageButton btnBack;

    private EventViewModel eventViewModel;
    private EventDashboardAdapter adapter;
    private int userId;

    private List<Event> allEvents = new ArrayList<>();
    private List<Event> activeEvents = new ArrayList<>();
    private List<Event> pastEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_dashboard);

        initViews();
        setupRecyclerView();
        setupTabs();

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            finish();
            return;
        }

        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        // Observe all events
        observeEvents();

        // FAB click - create new event
        fabCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEvent.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        recyclerView = findViewById(R.id.rv_events);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        tvTotalEvents = findViewById(R.id.tv_total_events);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvTotalTickets = findViewById(R.id.tv_total_tickets);
        fabCreateEvent = findViewById(R.id.fab_create_event);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupRecyclerView() {
        adapter = new EventDashboardAdapter(this, new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        tabLayout.addTab(tabLayout.newTab().setText("Đang hoạt động"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã kết thúc"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterEventsByTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void observeEvents() {
        // Observe all events
        eventViewModel.getEventsByOrganizerId(userId).observe(this, events -> {
            if (events != null) {
                allEvents = events;
                updateDashboardStats();
                filterEventsByTab(tabLayout.getSelectedTabPosition());
            }
        });

        // Observe active events
        eventViewModel.getActiveEventsByOrganizer(userId).observe(this, events -> {
            if (events != null) {
                activeEvents = events;
                if (tabLayout.getSelectedTabPosition() == 1) {
                    updateEventList(activeEvents);
                }
            }
        });

        // Observe past events
        eventViewModel.getPastEventsByOrganizer(userId).observe(this, events -> {
            if (events != null) {
                pastEvents = events;
                if (tabLayout.getSelectedTabPosition() == 2) {
                    updateEventList(pastEvents);
                }
            }
        });

        // Observe statistics
        eventViewModel.getTotalRevenueByOrganizer(userId).observe(this, revenue -> {
            if (revenue != null) {
                tvTotalRevenue.setText(String.format("%,.0f VNĐ", revenue));
            }
        });

        eventViewModel.getTotalTicketsSoldByOrganizer(userId).observe(this, tickets -> {
            if (tickets != null) {
                tvTotalTickets.setText(String.valueOf(tickets));
            }
        });
    }

    private void updateDashboardStats() {
        tvTotalEvents.setText(String.valueOf(allEvents.size()));
    }

    private void filterEventsByTab(int position) {
        switch (position) {
            case 0: // All
                updateEventList(allEvents);
                break;
            case 1: // Active
                updateEventList(activeEvents);
                break;
            case 2: // Past
                updateEventList(pastEvents);
                break;
        }
    }

    private void updateEventList(List<Event> events) {
        if (events == null || events.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            adapter.updateEvents(events);
        }
    }

    @Override
    public void onEventClick(Event event) {
        // Open event details
        Intent intent = new Intent(this, ViewEventDetailsOrganizer.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    @Override
    public void onEditClick(Event event) {
        // Open edit event
        Intent intent = new Intent(this, UpdateEvent.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    @Override
    public void onSalesReportClick(Event event) {
        // Open sales report
        Intent intent = new Intent(this, SalesReportActivity.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("event_name", event.getEventName());
        startActivity(intent);
    }

    @Override
    public void onBroadcastClick(Event event) {
        // Open notification broadcast
        Intent intent = new Intent(this, NotificationBroadcastActivity.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("event_name", event.getEventName());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        if (eventViewModel != null) {
            observeEvents();
        }
    }
}
