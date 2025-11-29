package com.example.midterm.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.midterm.R;
import com.example.midterm.view.fragment.TicketsFragment;


public class MyTicketsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        loadTicketsFragment(savedInstanceState);
    }
    private void loadTicketsFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // Lấy userId từ SharedPreferences
            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);

            // Tạo và nhúng Fragment
            TicketsFragment fragment = TicketsFragment.newInstance(userId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment) // Sử dụng ID fragment_container mới
                    .commit();
        }
    }
}
