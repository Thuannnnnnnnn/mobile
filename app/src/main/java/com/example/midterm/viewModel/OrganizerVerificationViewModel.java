package com.example.midterm.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.midterm.model.entity.OrganizerVerification;
import com.example.midterm.model.repository.OrganizerVerificationRepository;

import java.util.List;
import java.util.function.Consumer;

public class OrganizerVerificationViewModel extends AndroidViewModel {

    private final OrganizerVerificationRepository verificationRepository;

    public OrganizerVerificationViewModel(@NonNull Application application) {
        super(application);
        verificationRepository = new OrganizerVerificationRepository(application);
    }

    // Insert verification
    public void insertVerification(OrganizerVerification verification, Consumer<Long> callback) {
        verificationRepository.insertVerification(verification, callback);
    }

    // Update verification
    public void updateVerification(OrganizerVerification verification) {
        verificationRepository.updateVerification(verification);
    }

    // Delete verification
    public void deleteVerification(OrganizerVerification verification) {
        verificationRepository.deleteVerification(verification);
    }

    // Get verification by account ID
    public LiveData<OrganizerVerification> getVerificationByAccountId(int accountId) {
        return verificationRepository.getVerificationByAccountId(accountId);
    }

    // Check if account is verified
    public LiveData<Boolean> isAccountVerified(int accountId) {
        return verificationRepository.isAccountVerified(accountId);
    }

    // Verify OTP code
    public void verifyCode(int accountId, String code, Consumer<Boolean> callback) {
        verificationRepository.verifyCode(accountId, code, callback);
    }

    // Update verification status
    public void updateVerificationStatus(int accountId, boolean isVerified, String verifiedAt, String status, String updatedAt) {
        verificationRepository.updateVerificationStatus(accountId, isVerified, verifiedAt, status, updatedAt);
    }

    // Update verification code (resend OTP)
    public void updateVerificationCode(int accountId, String code, String expiresAt, String updatedAt) {
        verificationRepository.updateVerificationCode(accountId, code, expiresAt, updatedAt);
    }

    // Check valid verification
    public void checkValidVerification(int accountId, Consumer<OrganizerVerification> callback) {
        verificationRepository.checkValidVerification(accountId, callback);
    }

    // Get pending verifications
    public LiveData<List<OrganizerVerification>> getPendingVerifications() {
        return verificationRepository.getPendingVerifications();
    }

    // Update business info
    public void updateBusinessInfo(int accountId, String licenseUrl, String taxId, String updatedAt) {
        verificationRepository.updateBusinessInfo(accountId, licenseUrl, taxId, updatedAt);
    }

    // Reject verification
    public void rejectVerification(int accountId, String reason, String updatedAt) {
        verificationRepository.rejectVerification(accountId, reason, updatedAt);
    }

    // Delete expired verifications
    public void deleteExpiredVerifications() {
        verificationRepository.deleteExpiredVerifications();
    }
}
