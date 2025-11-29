package com.example.midterm.model.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.OrganizerVerificationDAO;
import com.example.midterm.model.entity.OrganizerVerification;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class OrganizerVerificationRepository {

    private final OrganizerVerificationDAO verificationDAO;
    private final ExecutorService executorService;

    public OrganizerVerificationRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        verificationDAO = db.organizerVerificationDAO();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Insert verification
    public void insertVerification(OrganizerVerification verification, Consumer<Long> callback) {
        executorService.execute(() -> {
            long id = verificationDAO.insert(verification);
            if (callback != null) callback.accept(id);
        });
    }

    // Update verification
    public void updateVerification(OrganizerVerification verification) {
        executorService.execute(() -> verificationDAO.update(verification));
    }

    // Delete verification
    public void deleteVerification(OrganizerVerification verification) {
        executorService.execute(() -> verificationDAO.delete(verification));
    }

    // Get verification by account ID
    public LiveData<OrganizerVerification> getVerificationByAccountId(int accountId) {
        return verificationDAO.getVerificationByAccountId(accountId);
    }

    // Check if account is verified
    public LiveData<Boolean> isAccountVerified(int accountId) {
        return verificationDAO.isAccountVerified(accountId);
    }

    // Verify OTP code
    public void verifyCode(int accountId, String code, Consumer<Boolean> callback) {
        executorService.execute(() -> {
            OrganizerVerification verification = verificationDAO.getVerificationByCode(accountId, code);
            boolean isValid = verification != null;
            if (callback != null) callback.accept(isValid);
        });
    }

    // Update verification status
    public void updateVerificationStatus(int accountId, boolean isVerified, String verifiedAt, String status, String updatedAt) {
        executorService.execute(() -> verificationDAO.updateVerificationStatus(accountId, isVerified, verifiedAt, status, updatedAt));
    }

    // Update verification code (resend OTP)
    public void updateVerificationCode(int accountId, String code, String expiresAt, String updatedAt) {
        executorService.execute(() -> verificationDAO.updateVerificationCode(accountId, code, expiresAt, updatedAt));
    }

    // Check valid verification
    public void checkValidVerification(int accountId, Consumer<OrganizerVerification> callback) {
        executorService.execute(() -> {
            OrganizerVerification verification = verificationDAO.getValidVerification(accountId);
            if (callback != null) callback.accept(verification);
        });
    }

    // Get pending verifications
    public LiveData<List<OrganizerVerification>> getPendingVerifications() {
        return verificationDAO.getPendingVerifications();
    }

    // Update business info
    public void updateBusinessInfo(int accountId, String licenseUrl, String taxId, String updatedAt) {
        executorService.execute(() -> verificationDAO.updateBusinessInfo(accountId, licenseUrl, taxId, updatedAt));
    }

    // Reject verification
    public void rejectVerification(int accountId, String reason, String updatedAt) {
        executorService.execute(() -> verificationDAO.rejectVerification(accountId, reason, updatedAt));
    }

    // Delete expired verifications
    public void deleteExpiredVerifications() {
        executorService.execute(() -> verificationDAO.deleteExpiredVerifications());
    }
}
