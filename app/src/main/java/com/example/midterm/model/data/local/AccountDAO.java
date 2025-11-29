package com.example.midterm.model.data.local;

import com.example.midterm.model.entity.Account;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.lifecycle.LiveData; // Thêm import này
import androidx.room.OnConflictStrategy; // Import OnConflictStrategy

import java.util.List;

@Dao
public interface AccountDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Đã thêm onConflict = OnConflictStrategy.REPLACE
    long insert(Account account);

    @Update
    void update(Account account);

    @Delete
    void delete(Account account);

    @Query("SELECT * FROM accounts WHERE (email = :input OR phone = :input) AND password = :password LIMIT 1")
    Account login(String input, String password);

    @Query("SELECT * FROM accounts")
    LiveData<List<Account>> getAllAccounts(); // Thay đổi kiểu trả về

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    Account getAccountById(int id);

    @Query("SELECT COUNT(*) FROM accounts WHERE email = :email OR phone = :phone")
    int isAccountExist(String email, String phone);

    @Query("SELECT COUNT(*) FROM accounts WHERE (email = :email OR phone = :phone) AND id != :userId")
    int countOtherAccountsWithEmailOrPhone(String email, String phone, int userId);

    @Query("UPDATE accounts SET email = :email, phone = :phone WHERE id = :accountId")
    void updateEmailAndPhone(int accountId, String email, String phone);

    @Query("UPDATE accounts SET role = :role WHERE id = :accountId")
    void updateRole(int accountId, String role);

    @Query("UPDATE accounts SET password = :newPassword WHERE id = :userId")
    void changePassword(int userId, String newPassword); // Thêm phương thức này

    @Query("SELECT COUNT(*) FROM accounts")
    int getAccountCount();
}
