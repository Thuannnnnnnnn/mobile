package com.example.midterm.view.Auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.midterm.view.Homepage;
import com.example.midterm.view.HomepageOrganizer;
import com.example.midterm.view.MainActivity;

public class SplashActiviity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        String role = prefs.getString("role", null);

        if (userId != -1 && role != null) {
            // Đã đăng nhập
            Intent intent;
            if ("user".equals(role)) {
                intent = new Intent(this, Homepage.class);
            } else {
                intent = new Intent(this, HomepageOrganizer.class);
            }
            intent.putExtra("user_id", userId);
            startActivity(intent);
        } else {
            // Chưa đăng nhập
            startActivity(new Intent(this, MainActivity.class));
        }

        finish();
    }
}
