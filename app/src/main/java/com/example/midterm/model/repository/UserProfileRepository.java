package com.example.midterm.model.repository;

import android.content.Context;

import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.UserProfileDAO;
import com.example.midterm.model.entity.UserProfile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserProfileRepository {
    private final UserProfileDAO userProfileDAO;
    private final ExecutorService executorService;

    public UserProfileRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userProfileDAO = db.userProfileDAO();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(UserProfile userProfile) {
        executorService.execute(() -> userProfileDAO.insert(userProfile));
    }

    public boolean update(UserProfile userProfile) {
        return userProfileDAO.update(userProfile) > 0;
    }

    public void delete(UserProfile userProfile) {
        executorService.execute(() -> userProfileDAO.delete(userProfile));
    }

    public UserProfile getUserById(int userId) {
        return userProfileDAO.getUserById(userId);
    }

    public void deleteByUserId(int userId) {
        executorService.execute(() -> userProfileDAO.deleteByUserId(userId));
    }
}
