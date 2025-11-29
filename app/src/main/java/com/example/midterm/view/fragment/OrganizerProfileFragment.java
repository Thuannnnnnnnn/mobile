package com.example.midterm.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.midterm.R;
import com.example.midterm.view.MainActivity;
import com.example.midterm.view.OrganizerInfoActivity;
import com.example.midterm.view.AccountInfoActivity;
import com.example.midterm.view.NotificationSettingsActivity;
import com.example.midterm.view.ThemeSettingsActivity;
import com.example.midterm.view.ChangePasswordActivity;
import com.example.midterm.viewModel.AccountViewModel;
import com.example.midterm.viewModel.OrganizerViewModel;

import java.io.File;

public class OrganizerProfileFragment extends Fragment {
    private ImageView imgOrganizerAvatar;
    private TextView tvOrganizerName, tvOrganizerEmail;
    private TextView menuOrganizerInfo, menuPersonalInfo, menuNotificationSettings, menuChangeTheme;
    private TextView menuChangePassword, menuPrivacy;
    private TextView menuLanguage, menuHelp, menuAbout;
    private TextView menuLogout;
    private int userId;
    private OrganizerViewModel organizerViewModel;
    private AccountViewModel accountViewModel;

    public static OrganizerProfileFragment newInstance(int userId) {
        OrganizerProfileFragment fragment = new OrganizerProfileFragment();
        Bundle args = new Bundle();
        args.putInt("user_id", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_profile, container, false);

        if (getArguments() != null) {
            userId = getArguments().getInt("user_id", -1);
        }

        organizerViewModel = new ViewModelProvider(this).get(OrganizerViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        initViews(view);
        loadOrganizerInfo();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        imgOrganizerAvatar = view.findViewById(R.id.img_organizer_avatar);
        tvOrganizerName = view.findViewById(R.id.tv_organizer_name);
        tvOrganizerEmail = view.findViewById(R.id.tv_organizer_email);

        menuOrganizerInfo = view.findViewById(R.id.menu_organizer_info);
        menuPersonalInfo = view.findViewById(R.id.menu_personal_info);
        menuNotificationSettings = view.findViewById(R.id.menu_notification_settings);
        menuChangeTheme = view.findViewById(R.id.menu_change_theme);
        menuChangePassword = view.findViewById(R.id.menu_change_password);
        menuPrivacy = view.findViewById(R.id.menu_privacy);
        menuLanguage = view.findViewById(R.id.menu_language);
        menuHelp = view.findViewById(R.id.menu_help);
        menuAbout = view.findViewById(R.id.menu_about);
        menuLogout = view.findViewById(R.id.menu_logout);
    }

    private void loadOrganizerInfo() {
        if (userId == -1) return;

        organizerViewModel.observeOrganizerByAccountId(userId).observe(getViewLifecycleOwner(), organizer -> {
            if (organizer != null) {
                tvOrganizerName.setText(organizer.getOrganizerName());

                // Load avatar
                if (organizer.getLogo() != null && !organizer.getLogo().isEmpty()) {
                    File logoFile = new File(organizer.getLogo());
                    Glide.with(this)
                            .load(logoFile)
                            .placeholder(R.drawable.unnamed_removebg_preview)
                            .error(R.drawable.unnamed_removebg_preview)
                            .into(imgOrganizerAvatar);
                }

                // Load email from account
                accountViewModel.getAccountById(organizer.getOrganizerId()).observe(getViewLifecycleOwner(), account -> {
                    if (account != null) {
                        tvOrganizerEmail.setText(account.getEmail());
                    }
                });
            }
        });
    }

    private void setupClickListeners() {
        // Organizer info
        menuOrganizerInfo.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), OrganizerInfoActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        // Personal info
        menuPersonalInfo.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AccountInfoActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        // Notification settings
        menuNotificationSettings.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), NotificationSettingsActivity.class);
            startActivity(intent);
        });

        // Change theme
        menuChangeTheme.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ThemeSettingsActivity.class);
            startActivity(intent);
        });

        // Change password
        menuChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChangePasswordActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        // Privacy
        menuPrivacy.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Cài đặt quyền riêng tư", Toast.LENGTH_SHORT).show();
        });

        // Language
        menuLanguage.setOnClickListener(v -> {
            showLanguageDialog();
        });

        // Help
        menuHelp.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Trung tâm trợ giúp", Toast.LENGTH_SHORT).show();
        });

        // About
        menuAbout.setOnClickListener(v -> {
            showAboutDialog();
        });

        // Logout
        menuLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        // Clear login info
                        requireContext().getSharedPreferences("auth", 0)
                                .edit()
                                .clear()
                                .apply();

                        // Return to login screen
                        Intent intent = new Intent(requireContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void showLanguageDialog() {
        String[] languages = {"Tiếng Việt", "English", "中文 (Chinese)", "日本語 (Japanese)", "한국어 (Korean)"};
        int currentSelection = requireContext().getSharedPreferences("app_settings", 0)
                .getInt("language", 0);

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn ngôn ngữ")
                .setSingleChoiceItems(languages, currentSelection, (dialog, which) -> {
                    requireContext().getSharedPreferences("app_settings", 0)
                            .edit()
                            .putInt("language", which)
                            .apply();
                    Toast.makeText(requireContext(), "Đã chọn: " + languages[which], Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Về ứng dụng")
                .setMessage("Event Organizer App\n\n" +
                        "Phiên bản: 1.0.0\n" +
                        "Build: 2025.01\n\n" +
                        "Ứng dụng quản lý sự kiện và bán vé trực tuyến.\n\n" +
                        "© 2025 Event Organizer Team")
                .setPositiveButton("OK", null)
                .show();
    }
}
