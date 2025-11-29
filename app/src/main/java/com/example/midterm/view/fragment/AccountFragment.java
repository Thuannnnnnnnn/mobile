package com.example.midterm.view.fragment;

import android.content.Intent;
import android.net.Uri;
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

import com.example.midterm.R;
import com.example.midterm.view.AccountInfoActivity;
import com.example.midterm.view.MainActivity;
import com.example.midterm.view.RegisterOrganizer;
import com.example.midterm.viewModel.AccountViewModel;
import com.example.midterm.viewModel.UserProfileViewModel;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {
    private TextView tvUserName, tvUserEmail;
    private CircleImageView imgAvatar;
    private TextView menu_personal_info, menu_register_organizer, menu_delete_account, menu_change_theme, menu_logout;
    private int userId;

    private AccountViewModel accountViewModel;
    private UserProfileViewModel userProfileViewModel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        imgAvatar = view.findViewById(R.id.img_avatar);

        menu_personal_info = view.findViewById(R.id.menu_personal_info);
        menu_register_organizer = view.findViewById(R.id.menu_register_organizer);
        menu_delete_account = view.findViewById(R.id.menu_delete_account);
        menu_change_theme = view.findViewById(R.id.menu_change_theme);
        menu_logout = view.findViewById(R.id.menu_logout);

        if (getArguments() != null) {
            userId = getArguments().getInt("user_id", -1);
        }

        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        // Load user info
        loadUserInfo();
        // Mở trang Thông tin cá nhân
        menu_personal_info.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AccountInfoActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        menu_logout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        // Xoá thông tin đăng nhập (SharedPreferences)
                        requireContext().getSharedPreferences("auth", 0)
                                .edit()
                                .clear()
                                .apply();

                        // Quay về màn hình đăng nhập
                        Intent intent = new Intent(requireContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });


        menu_register_organizer.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RegisterOrganizer.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });
        //Xóa tài khoản (confirm)
        menu_delete_account.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xóa tài khoản")
                    .setMessage("Bạn có chắc chắn muốn xóa tài khoản này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // Gọi ViewModel hoặc Repository xử lý xóa
                        Toast.makeText(requireContext(), "Tài khoản đã bị xóa!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        return view;
    }

    private void loadUserInfo() {
        accountViewModel.getAccountById(userId).observe(getViewLifecycleOwner(), account -> {
            if (account != null) {
                tvUserEmail.setText(account.getEmail());
            }
        });

        userProfileViewModel.getUserProfileById(userId).observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                if (profile.getFullName() != null && !profile.getFullName().isEmpty()) {
                    tvUserName.setText(profile.getFullName());
                } else {
                    tvUserName.setText("Người dùng");
                }

                if (profile.getAvatar() != null && !profile.getAvatar().isEmpty()) {
                    try {
                        imgAvatar.setImageURI(Uri.parse(profile.getAvatar()));
                    } catch (Exception e) {
                        // Use default avatar
                    }
                }
            } else {
                tvUserName.setText("Người dùng");
            }
        });
    }
}
