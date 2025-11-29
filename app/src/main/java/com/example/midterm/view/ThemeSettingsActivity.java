package com.example.midterm.view;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.midterm.R;
import com.google.android.material.snackbar.Snackbar;

public class ThemeSettingsActivity extends AppCompatActivity {
    private RadioGroup radioGroupTheme;
    private RadioButton radioLight, radioDark, radioSystem;
    private View colorBlue, colorRed, colorGreen, colorPurple, colorOrange;
    private SeekBar seekbarFontSize;
    private SwitchCompat switchAnimations;
    private ImageButton btnBack;
    private SharedPreferences preferences;

    private View selectedColorView;
    private String selectedColor = "#2196F3"; // Default blue

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_settings);

        preferences = getSharedPreferences("theme_settings", MODE_PRIVATE);

        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);

        radioGroupTheme = findViewById(R.id.radio_group_theme);
        radioLight = findViewById(R.id.radio_light);
        radioDark = findViewById(R.id.radio_dark);
        radioSystem = findViewById(R.id.radio_system);

        colorBlue = findViewById(R.id.color_blue);
        colorRed = findViewById(R.id.color_red);
        colorGreen = findViewById(R.id.color_green);
        colorPurple = findViewById(R.id.color_purple);
        colorOrange = findViewById(R.id.color_orange);

        seekbarFontSize = findViewById(R.id.seekbar_font_size);
        switchAnimations = findViewById(R.id.switch_animations);
    }

    private void loadSettings() {
        String themeMode = preferences.getString("theme_mode", "system");
        switch (themeMode) {
            case "light":
                radioLight.setChecked(true);
                break;
            case "dark":
                radioDark.setChecked(true);
                break;
            default:
                radioSystem.setChecked(true);
                break;
        }

        selectedColor = preferences.getString("primary_color", "#2196F3");
        updateColorSelection(getColorViewByHex(selectedColor));

        int fontSize = preferences.getInt("font_size", 2);
        seekbarFontSize.setProgress(fontSize);

        boolean animations = preferences.getBoolean("animations", true);
        switchAnimations.setChecked(animations);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Theme mode selection
        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            String mode;
            int nightMode;

            if (checkedId == R.id.radio_light) {
                mode = "light";
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.radio_dark) {
                mode = "dark";
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                mode = "system";
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }

            preferences.edit().putString("theme_mode", mode).apply();
            AppCompatDelegate.setDefaultNightMode(nightMode);

            Snackbar.make(radioGroupTheme, "Đã thay đổi chế độ giao diện", Snackbar.LENGTH_SHORT).show();
        });

        colorBlue.setOnClickListener(v -> selectColor(v, "#2196F3"));
        colorRed.setOnClickListener(v -> selectColor(v, "#F44336"));
        colorGreen.setOnClickListener(v -> selectColor(v, "#4CAF50"));
        colorPurple.setOnClickListener(v -> selectColor(v, "#9C27B0"));
        colorOrange.setOnClickListener(v -> selectColor(v, "#FF9800"));

        seekbarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    preferences.edit().putInt("font_size", progress).apply();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Snackbar.make(seekBar, "Cỡ chữ sẽ được áp dụng khi khởi động lại ứng dụng",
                    Snackbar.LENGTH_SHORT).show();
            }
        });

        // Animations
        switchAnimations.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("animations", isChecked).apply();
        });
    }

    private void selectColor(View view, String colorHex) {
        selectedColor = colorHex;
        updateColorSelection(view);
        preferences.edit().putString("primary_color", colorHex).apply();

        Snackbar.make(view, "Màu chủ đạo sẽ được áp dụng khi khởi động lại ứng dụng",
            Snackbar.LENGTH_SHORT).show();
    }

    private void updateColorSelection(View view) {
        // Reset previous selection
        if (selectedColorView != null) {
            resetColorBorder(selectedColorView);
        }

        // Set new selection
        selectedColorView = view;
        if (view != null) {
            setColorBorder(view, true);
        }
    }

    private void setColorBorder(View view, boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);

        if (view.getBackgroundTintList() != null) {
            drawable.setColor(view.getBackgroundTintList().getDefaultColor());
        }

        if (selected) {
            drawable.setStroke(8, Color.parseColor("#000000"));
        }

        view.setBackground(drawable);
    }

    private void resetColorBorder(View view) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);

        if (view.getBackgroundTintList() != null) {
            drawable.setColor(view.getBackgroundTintList().getDefaultColor());
        }

        view.setBackground(drawable);
    }

    private View getColorViewByHex(String hex) {
        switch (hex) {
            case "#2196F3":
                return colorBlue;
            case "#F44336":
                return colorRed;
            case "#4CAF50":
                return colorGreen;
            case "#9C27B0":
                return colorPurple;
            case "#FF9800":
                return colorOrange;
            default:
                return colorBlue;
        }
    }
}
