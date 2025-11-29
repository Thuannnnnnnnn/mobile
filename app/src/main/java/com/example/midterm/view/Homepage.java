package com.example.midterm.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.midterm.R;
import com.example.midterm.view.fragment.AccountFragment;
import com.example.midterm.view.fragment.HomepageFragment;
import com.example.midterm.view.fragment.TicketsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Homepage extends AppCompatActivity {
    private int userId;
    private BottomNavigationView bottomNavigationView;

    private boolean shouldShowBackInTickets = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        userId = getIntent().getIntExtra("user_id", -1);

        setupBottomNavigation();

        // Xử lý logic khi App vừa khởi động (lần đầu vào)
        if (savedInstanceState == null) {
            if (checkRedirectFromPayment(getIntent())) {
            } else {
                loadFragment(new HomepageFragment());
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật intent mới nhất
        checkRedirectFromPayment(intent);
    }

    private boolean checkRedirectFromPayment(Intent intent) {
        if (intent != null && intent.getBooleanExtra("FROM_PAYMENT", false)) {
            // Bật cờ lên
            shouldShowBackInTickets = true;

            // Tự động chuyển sang tab Tickets
            // Dòng này sẽ kích hoạt setOnItemSelectedListener bên dưới
            bottomNavigationView.setSelectedItemId(R.id.nav_tickets);

            // Xóa extra để nếu xoay màn hình không bị kích hoạt lại
            intent.removeExtra("FROM_PAYMENT");
            return true;
        }
        return false;
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomepageFragment();
                Bundle args = new Bundle();
                args.putInt("user_id", userId);
                selectedFragment.setArguments(args);

                // Khi user bấm sang Home, reset cờ nút back để lần sau vào vé không hiện nữa
                shouldShowBackInTickets = false;

            } else if (itemId == R.id.nav_tickets) {
                selectedFragment = TicketsFragment.newInstance(userId);
                Bundle args = selectedFragment.getArguments(); // Lấy bundle đã tạo trong newInstance
                if (args == null) args = new Bundle();

                // Truyền trạng thái nút back vào Fragment
                args.putBoolean("show_back", shouldShowBackInTickets);
                selectedFragment.setArguments(args);

                // Sau khi đã load fragment với nút back 1 lần,
                // reset cờ ngay. Lần sau bấm vào tab này sẽ không còn nút back.
                shouldShowBackInTickets = false;

            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new AccountFragment();
                Bundle args = new Bundle();
                args.putInt("user_id", userId);
                selectedFragment.setArguments(args);

                shouldShowBackInTickets = false;
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}