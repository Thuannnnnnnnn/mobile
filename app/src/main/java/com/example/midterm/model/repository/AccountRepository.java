package com.example.midterm.model.repository;

import android.content.Context;

import com.example.midterm.model.data.local.AccountDAO;
import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.OrganizerDAO;
import com.example.midterm.model.data.local.UserProfileDAO;
import com.example.midterm.model.entity.Account;
import com.example.midterm.utils.HashPassword;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AccountRepository {

    private final AccountDAO accountDAO;
    private final UserProfileDAO userProfileDAO;
    private final OrganizerDAO organizerProfileDAO;

    private final ExecutorService executorService;

    public AccountRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        accountDAO = db.accountDAO();
        userProfileDAO = db.userProfileDAO();
        organizerProfileDAO = db.organizerDAO();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Account account) {
        executorService.execute(() -> accountDAO.insert(account));
    }

    public void update(Account account) {
        executorService.execute(() -> accountDAO.update(account));
    }

    public void delete(Account account) {
        executorService.execute(() -> accountDAO.delete(account));
    }

    public Account login(String email, String password) {
        String hashedPassword = HashPassword.hashPassword(password);
        return accountDAO.login(email, hashedPassword);
    }
    public List<Account> getAllAccounts() {
        return accountDAO.getAllAccounts();
    }
    // Đăng ký tài khoản (return true nếu đăng ký thành công)
    public long register(Account account) {
        int exist = accountDAO.isAccountExist(account.getEmail(), account.getPhone());
        if (exist > 0) return -1; // Email/Phone đã tồn tại

        return accountDAO.insert(account); // Trả về ID mới tạo
    }
    public boolean isAccountExist(String email, String phone) {
        return accountDAO.isAccountExist(email, phone) > 0;
    }
    public void checkEmailOrPhoneExist(String email, String phone, int userId, Consumer<Boolean> callback) {
        executorService.execute(() -> {
            boolean exist = accountDAO.countOtherAccountsWithEmailOrPhone(email, phone, userId) > 0;
            callback.accept(exist);
        });
    }

    public Account getAccountById(int accountId) {
        return accountDAO.getAccountById(accountId);
    }
    public void updateAccount(Account account) {
        accountDAO.updateEmailAndPhone(account.getId(), account.getEmail(), account.getPhone());
    }
    //Update role
    public void updateRole(int accountId, String role) {
        executorService.execute(() -> accountDAO.updateRole(accountId, role));
    }
}
