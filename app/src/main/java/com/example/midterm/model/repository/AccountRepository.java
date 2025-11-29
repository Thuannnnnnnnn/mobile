package com.example.midterm.model.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.midterm.model.data.local.AccountDAO;
import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.OrganizerDAO;
import com.example.midterm.model.data.local.UserProfileDAO;
import com.example.midterm.model.data.remote.FirestoreHelper;
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
    private final FirestoreHelper firestoreHelper;
    private final ExecutorService executorService;

    public AccountRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        accountDAO = db.accountDAO();
        userProfileDAO = db.userProfileDAO();
        organizerProfileDAO = db.organizerDAO();
        firestoreHelper = new FirestoreHelper();
        executorService = Executors.newSingleThreadExecutor();

        initRealtimeSync();
    }

    private void initRealtimeSync() {
        firestoreHelper.listenForAccountUpdates(accounts -> {
            executorService.execute(() -> {
                for (Account acc : accounts) {
                    accountDAO.insert(acc);
                }
                Log.d("AccountRepo", "Đã sync " + accounts.size() + " accounts từ Cloud");
            });
        });
    }

    public LiveData<List<Account>> getAllAccounts() {
        return accountDAO.getAllAccounts();
    }

    public void insert(Account account) {
        executorService.execute(() -> {
            long id = accountDAO.insert(account);
            account.setId((int) id);
            Log.d("AccountRepo", "Insert: Account ID before Firestore sync: " + account.getId()); // Log thêm
            firestoreHelper.syncAccountToCloud(account);
        });
    }

    public void update(Account account) {
        executorService.execute(() -> {
            accountDAO.update(account);
            Log.d("AccountRepo", "Update: Account ID before Firestore sync: " + account.getId()); // Log thêm
            firestoreHelper.syncAccountToCloud(account);
        });
    }

    public void delete(Account account) {
        executorService.execute(() -> accountDAO.delete(account));
    }

    public Account getAccountById(int id) {
        return accountDAO.getAccountById(id);
    }

    public Account login(String email, String password) {
        String hashedPassword = HashPassword.hashPassword(password);
        return accountDAO.login(email, hashedPassword);
    }

    public long register(Account account) {
        int exist = accountDAO.isAccountExist(account.getEmail(), account.getPhone());
        if (exist > 0) return -1;

        long id = accountDAO.insert(account);

        if (id > 0) {
            account.setId((int) id);
            Log.d("AccountRepo", "Register: Account ID before Firestore sync: " + account.getId()); // Log thêm
            executorService.execute(() -> firestoreHelper.syncAccountToCloud(account));
        }

        return id;
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

    public void updateAccount(Account account) {
        executorService.execute(() -> {
            accountDAO.updateEmailAndPhone(account.getId(), account.getEmail(), account.getPhone());
            Log.d("AccountRepo", "UpdateAccount: Account ID before Firestore sync: " + account.getId()); // Log thêm
            firestoreHelper.syncAccountToCloud(account);
        });
    }

    public void updateRole(int accountId, String role) {
        executorService.execute(() -> {
            accountDAO.updateRole(accountId, role);
            Account updatedAccount = accountDAO.getAccountById(accountId);
            if (updatedAccount != null) {
                Log.d("AccountRepo", "UpdateRole: Account ID before Firestore sync: " + updatedAccount.getId()); // Log thêm
                firestoreHelper.syncAccountToCloud(updatedAccount);
            }
        });
    }

    public void changePassword(int userId, String newPassword) {
        executorService.execute(() -> {
            accountDAO.changePassword(userId, newPassword);
            Account updatedAccount = accountDAO.getAccountById(userId);
            if (updatedAccount != null) {
                Log.d("AccountRepo", "ChangePassword: Account ID before Firestore sync: " + updatedAccount.getId()); // Log thêm
                firestoreHelper.syncAccountToCloud(updatedAccount);
            }
        });
    }
}
