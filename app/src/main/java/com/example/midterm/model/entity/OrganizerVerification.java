package com.example.midterm.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "organizer_verifications",
        foreignKeys = @ForeignKey(
                entity = Account.class,
                parentColumns = "id",
                childColumns = "account_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = "account_id")}
)
public class OrganizerVerification {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "account_id")
    private int accountId;

    @ColumnInfo(name = "verification_code")
    private String verificationCode; // OTP code

    @ColumnInfo(name = "verification_type")
    private String verificationType; // "email", "phone", "document"

    @ColumnInfo(name = "is_verified")
    private boolean isVerified;

    @ColumnInfo(name = "verified_at")
    private String verifiedAt;

    @ColumnInfo(name = "expires_at")
    private String expiresAt;

    @ColumnInfo(name = "business_license")
    private String businessLicense; // URL to uploaded document

    @ColumnInfo(name = "tax_id")
    private String taxId;

    @ColumnInfo(name = "verification_status")
    private String verificationStatus; // "pending", "approved", "rejected"

    @ColumnInfo(name = "rejection_reason")
    private String rejectionReason;

    @ColumnInfo(name = "created_at")
    private String createdAt;

    @ColumnInfo(name = "updated_at")
    private String updatedAt;

    // Constructors
    public OrganizerVerification() {}

    @Ignore
    public OrganizerVerification(int accountId, String verificationCode, String verificationType,
                                  boolean isVerified, String verifiedAt, String expiresAt,
                                  String businessLicense, String taxId, String verificationStatus,
                                  String rejectionReason, String createdAt, String updatedAt) {
        this.accountId = accountId;
        this.verificationCode = verificationCode;
        this.verificationType = verificationType;
        this.isVerified = isVerified;
        this.verifiedAt = verifiedAt;
        this.expiresAt = expiresAt;
        this.businessLicense = businessLicense;
        this.taxId = taxId;
        this.verificationStatus = verificationStatus;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public String getVerificationType() { return verificationType; }
    public void setVerificationType(String verificationType) { this.verificationType = verificationType; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(String verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public String getBusinessLicense() { return businessLicense; }
    public void setBusinessLicense(String businessLicense) { this.businessLicense = businessLicense; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
