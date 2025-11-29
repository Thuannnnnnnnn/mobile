package com.example.midterm.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.midterm.model.entity.Account;
import com.example.midterm.model.entity.UserProfile;
import com.example.midterm.model.repository.AccountRepository;
import com.example.midterm.model.repository.UserProfileRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AccountViewModel extends AndroidViewModel {

    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final ExecutorService executorService;

    public MutableLiveData<Account> loggedInUser = new MutableLiveData<>();
    public MutableLiveData<Boolean> registerResult = new MutableLiveData<>();
    private final MutableLiveData<Account> accountLiveData = new MutableLiveData<>();

    public AccountViewModel(@NonNull Application application) {
        super(application);
        accountRepository = new AccountRepository(application);
        userProfileRepository = new UserProfileRepository(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    // Update account
    public void update(Account account) {
        accountRepository.update(account);
    }

    public LiveData<Account> getAccountById(int accountId) {
        executorService.execute(() -> {
            Account account = accountRepository.getAccountById(accountId);
            accountLiveData.postValue(account);
        });
        return accountLiveData;
    }

    public LiveData<Account> getAccountById2(int accountId) {
        MutableLiveData<Account> accountLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Account account = accountRepository.getAccountById(accountId);
            accountLiveData.postValue(account);
        });
        return accountLiveData;
    }

    public void login(String email, String password) {
        executorService.execute(() -> {
            Account account = accountRepository.login(email, password);
            loggedInUser.postValue(account);
        });
    }

    public void register(String username, String password, String role) {
        executorService.execute(() -> {
            String createdAt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new java.util.Date());

            String email = username.contains("@") ? username : null;
            String phone = username.contains("@") ? null : username;

            if (accountRepository.isAccountExist(email, phone)) {
                registerResult.postValue(false);
                return;
            }

            Account account = new Account(email, phone, password, role, createdAt);
            long accountId = accountRepository.register(account);

            if (accountId > 0) {
                UserProfile profile = new UserProfile((int) accountId, "", "", "", "");
                userProfileRepository.insert(profile);
                registerResult.postValue(true);
            } else {
                registerResult.postValue(false);
            }
        });
    }

    public void updateEmailOrPhone(int accountId, String email, String phone, Runnable callback) {
        executorService.execute(() -> {
            Account account = accountRepository.getAccountById(accountId);
            if (account == null) return;

            boolean changed = false;

            if (account.getEmail() != null && !account.getEmail().isEmpty()) {
                if (phone != null && !phone.isEmpty()) {
                    account.setPhone(phone);
                    changed = true;
                }
            }

            if (account.getPhone() != null && !account.getPhone().isEmpty()) {
                if (email != null && !email.isEmpty()) {
                    account.setEmail(email);
                    changed = true;
                }
            }
            if (changed) {
                accountRepository.updateAccount(account);
            }

            if (callback != null) {
                callback.run();
            }
        });
    }
    public void checkEmailOrPhoneExist(String email, String phone, int userId, Consumer<Boolean> callback) {
        accountRepository.checkEmailOrPhoneExist(email, phone, userId, callback);
    }

    //Update role
    public void updateRole(int accountId, String role) {
        accountRepository.updateRole(accountId, role);
    }
}
