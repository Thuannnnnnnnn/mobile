package com.example.midterm.viewModel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.midterm.model.entity.Account;
import com.example.midterm.model.entity.Organizer;
import com.example.midterm.model.repository.AccountRepository;
import com.example.midterm.model.repository.OrganizerRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class OrganizerViewModel extends AndroidViewModel {

    private final OrganizerRepository organizerRepository;
    private final AccountRepository accountRepository;
    private final ExecutorService executorService;

    public MutableLiveData<Organizer> organizerLiveData = new MutableLiveData<>();
    public MutableLiveData<List<Organizer>> organizersLiveData = new MutableLiveData<>();

    public OrganizerViewModel(@NonNull Application application) {
        super(application);
        organizerRepository = new OrganizerRepository(application);
        executorService = Executors.newSingleThreadExecutor();
        accountRepository = new AccountRepository(application);
    }

    public void insert(Organizer organizer) {
        organizerRepository.insert(organizer);
    }

    public void update(Organizer organizer) {
        organizerRepository.update(organizer);
    }

    public void delete(Organizer organizer) {
        organizerRepository.delete(organizer);
    }

    public LiveData<Organizer> observeOrganizerByAccountId(int accountId) {
        return organizerRepository.observeOrganizerByAccountId(accountId);
    }
    public void registerOrganizer(Organizer organizer, Consumer<String> callback) {
        executorService.execute(() -> {
            // Lấy email/phone từ Account
            Account account = accountRepository.getAccountById(organizer.getOrganizerId());
            boolean exists = organizerRepository.isOrganizerExist(organizer.getOrganizerId());
            if (exists) {
                callback.accept("Bạn đã gửi yêu cầu trước đó. Vui lòng chờ duyệt!");
                return;
            }
            // Kiểm tra email/phone trùng với các account khác (trừ chính nó)
            accountRepository.checkEmailOrPhoneExist(
                    account.getEmail(), account.getPhone(), account.getId(), exist -> {
                        if (exist) {
                            callback.accept("Email hoặc số điện thoại đã được sử dụng!");
                        } else {
                            organizerRepository.insert(organizer);
                            callback.accept("Yêu cầu đã được gửi thành công!");
                        }
                    });
        });
    }
}

