package com.example.midterm.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.midterm.R;

public class NotificationSettingsActivity extends AppCompatActivity {
    private SwitchCompat switchNewTicket, switchCancelTicket, switchUpcomingEvent;
    private SwitchCompat switchEmail, switchPush, switchMarketing;
    private SwitchCompat switchSound, switchVibration;
    private ImageButton btnBack;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        preferences = getSharedPreferences("notification_settings", MODE_PRIVATE);

        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);

        // Event notifications
        switchNewTicket = findViewById(R.id.switch_new_ticket);
        switchCancelTicket = findViewById(R.id.switch_cancel_ticket);
        switchUpcomingEvent = findViewById(R.id.switch_upcoming_event);

        // System notifications
        switchEmail = findViewById(R.id.switch_email);
        switchPush = findViewById(R.id.switch_push);
        switchMarketing = findViewById(R.id.switch_marketing);

        // Sound and vibration
        switchSound = findViewById(R.id.switch_sound);
        switchVibration = findViewById(R.id.switch_vibration);
    }

    private void loadSettings() {
        // Load saved settings
        switchNewTicket.setChecked(preferences.getBoolean("new_ticket", true));
        switchCancelTicket.setChecked(preferences.getBoolean("cancel_ticket", true));
        switchUpcomingEvent.setChecked(preferences.getBoolean("upcoming_event", true));

        switchEmail.setChecked(preferences.getBoolean("email", true));
        switchPush.setChecked(preferences.getBoolean("push", true));
        switchMarketing.setChecked(preferences.getBoolean("marketing", false));

        switchSound.setChecked(preferences.getBoolean("sound", true));
        switchVibration.setChecked(preferences.getBoolean("vibration", true));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Event notifications
        switchNewTicket.setOnCheckedChangeListener((buttonView, isChecked) ->
            savePreference("new_ticket", isChecked));

        switchCancelTicket.setOnCheckedChangeListener((buttonView, isChecked) ->
            savePreference("cancel_ticket", isChecked));

        switchUpcomingEvent.setOnCheckedChangeListener((buttonView, isChecked) ->
            savePreference("upcoming_event", isChecked));

        // System notifications
        switchEmail.setOnCheckedChangeListener((buttonView, isChecked) ->
            savePreference("email", isChecked));

        switchPush.setOnCheckedChangeListener((buttonView, isChecked) ->
            savePreference("push", isChecked));

        switchMarketing.setOnCheckedChangeListener((buttonView, isChecked) ->
            savePreference("marketing", isChecked));

        // Sound and vibration
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) ->
            savePreference("sound", isChecked));

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) ->
            savePreference("vibration", isChecked));
    }

    private void savePreference(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }
}
