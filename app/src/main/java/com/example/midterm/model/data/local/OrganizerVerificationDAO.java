package com.example.midterm.model.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.midterm.model.entity.OrganizerVerification;

import java.util.List;

@Dao
public interface OrganizerVerificationDAO {

    @Insert
    long insert(OrganizerVerification verification);

    @Update
    void update(OrganizerVerification verification);

    @Delete
    void delete(OrganizerVerification verification);

    // Get verification by account ID
    @Query("SELECT * FROM organizer_verifications WHERE account_id = :accountId")
    LiveData<OrganizerVerification> getVerificationByAccountId(int accountId);

    // Get verification by account ID (sync)
    @Query("SELECT * FROM organizer_verifications WHERE account_id = :accountId")
    OrganizerVerification getVerificationByAccountIdSync(int accountId);

    // Check if account is verified
    @Query("SELECT is_verified FROM organizer_verifications WHERE account_id = :accountId")
    LiveData<Boolean> isAccountVerified(int accountId);

    // Get verification by code
    @Query("SELECT * FROM organizer_verifications WHERE verification_code = :code AND account_id = :accountId")
    OrganizerVerification getVerificationByCode(int accountId, String code);

    // Update verification status
    @Query("UPDATE organizer_verifications SET is_verified = :isVerified, verified_at = :verifiedAt, " +
           "verification_status = :status, updated_at = :updatedAt WHERE account_id = :accountId")
    void updateVerificationStatus(int accountId, boolean isVerified, String verifiedAt, String status, String updatedAt);

    // Update verification code (for resend OTP)
    @Query("UPDATE organizer_verifications SET verification_code = :code, expires_at = :expiresAt, " +
           "updated_at = :updatedAt WHERE account_id = :accountId")
    void updateVerificationCode(int accountId, String code, String expiresAt, String updatedAt);

    // Check if verification code is expired
    @Query("SELECT * FROM organizer_verifications WHERE account_id = :accountId " +
           "AND expires_at >= datetime('now', 'localtime')")
    OrganizerVerification getValidVerification(int accountId);

    // Get all pending verifications (for admin)
    @Query("SELECT * FROM organizer_verifications WHERE verification_status = 'pending' ORDER BY created_at ASC")
    LiveData<List<OrganizerVerification>> getPendingVerifications();

    // Update business document info
    @Query("UPDATE organizer_verifications SET business_license = :licenseUrl, tax_id = :taxId, " +
           "updated_at = :updatedAt WHERE account_id = :accountId")
    void updateBusinessInfo(int accountId, String licenseUrl, String taxId, String updatedAt);

    // Reject verification with reason
    @Query("UPDATE organizer_verifications SET verification_status = 'rejected', rejection_reason = :reason, " +
           "updated_at = :updatedAt WHERE account_id = :accountId")
    void rejectVerification(int accountId, String reason, String updatedAt);

    // Delete expired verifications
    @Query("DELETE FROM organizer_verifications WHERE expires_at < datetime('now', 'localtime') AND is_verified = 0")
    void deleteExpiredVerifications();
}
