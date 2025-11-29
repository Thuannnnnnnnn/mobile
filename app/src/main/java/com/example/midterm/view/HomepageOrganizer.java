package com.example.midterm.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.midterm.R;
import com.example.midterm.view.fragment.OrganizerAnalyticsFragment;
import com.example.midterm.view.fragment.OrganizerDashboardFragment;
import com.example.midterm.view.fragment.OrganizerEventsFragment;
import com.example.midterm.view.fragment.OrganizerProfileFragment;
import com.example.midterm.viewModel.OrganizerViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class HomepageOrganizer extends AppCompatActivity {
    private ImageView imgOrganizerLogo, btnNotification;
    private TextView tvGreeting;
    private BottomNavigationView bottomNavigationView;

    private OrganizerViewModel organizerViewModel;
    private int currentOrganizerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage_organizer);

        // Get organizer ID
        currentOrganizerId = getIntent().getIntExtra("user_id", -1);
        if (currentOrganizerId == -1) {
            finish();
            return;
        }

        initViews();
        initViewModel();
        setupClickListeners();
        setupBottomNavigation();
        loadOrganizerInfo();

        // Load default fragment (Dashboard)
        if (savedInstanceState == null) {
            loadFragment(OrganizerDashboardFragment.newInstance(currentOrganizerId));
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        imgOrganizerLogo = findViewById(R.id.imgOrganizerLogo);
        btnNotification = findViewById(R.id.btn_notification);
        tvGreeting = findViewById(R.id.tv_greeting);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void initViewModel() {
        organizerViewModel = new ViewModelProvider(this).get(OrganizerViewModel.class);
    }

    private void loadOrganizerInfo() {
        organizerViewModel.observeOrganizerByAccountId(currentOrganizerId).observe(this, organizer -> {
            if (organizer != null) {
                tvGreeting.setText("Xin chào, " + organizer.getOrganizerName());
            }
        });
    }

    private void setupClickListeners() {
        // Logo/Organizer info
        imgOrganizerLogo.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageOrganizer.this, OrganizerInfoActivity.class);
            intent.putExtra("user_id", currentOrganizerId);
            startActivity(intent);
        });

        // Notification button
        btnNotification.setOnClickListener(v -> {
            Toast.makeText(this, "Mở Thông báo", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                selectedFragment = OrganizerDashboardFragment.newInstance(currentOrganizerId);
            } else if (itemId == R.id.nav_events) {
                selectedFragment = OrganizerEventsFragment.newInstance(currentOrganizerId);
            } else if (itemId == R.id.nav_analytics) {
                selectedFragment = OrganizerAnalyticsFragment.newInstance(currentOrganizerId);
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = OrganizerProfileFragment.newInstance(currentOrganizerId);
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
