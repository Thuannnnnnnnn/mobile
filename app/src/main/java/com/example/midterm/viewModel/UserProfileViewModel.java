package com.example.midterm.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.midterm.model.entity.UserProfile;
import com.example.midterm.model.repository.UserProfileRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UserProfileViewModel extends AndroidViewModel {
    private final UserProfileRepository repository;
    private final ExecutorService executorService;

    private final MutableLiveData<UserProfile> userLiveData = new MutableLiveData<>();

    public UserProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserProfileRepository(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<UserProfile> getUserLiveData() {
        return userLiveData;
    }

    public void insert(UserProfile userProfile) {
        executorService.execute(() -> {
            repository.insert(userProfile);
        });
    }
    public void update(UserProfile userProfile, Consumer<String> callback) {
        executorService.execute(() -> {
            boolean success = repository.update(userProfile);
            if (success) {
                UserProfile updated = repository.getUserById(userProfile.getUserId());
                userLiveData.postValue(updated);
                callback.accept("Cập nhật thành công!");
            } else {
                callback.accept("Không có gì thay đổi!");
            }
        });
    }
    public void deleteByUserId(int userId) {
        executorService.execute(() -> repository.deleteByUserId(userId));
    }
    public void loadUserById(int userId) {
        executorService.execute(() -> {
            UserProfile user = repository.getUserById(userId);
            userLiveData.postValue(user);
        });
    }

    public LiveData<UserProfile> getUserProfileById(int userId) {
        MutableLiveData<UserProfile> result = new MutableLiveData<>();
        executorService.execute(() -> {
            UserProfile user = repository.getUserById(userId);
            result.postValue(user);
        });
        return result;
    }
}
