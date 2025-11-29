package com.example.midterm.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "discounts",
        foreignKeys = @ForeignKey(
                entity = Event.class,
                parentColumns = "id",
                childColumns = "event_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "event_id"),
                @Index(value = "code", unique = true)
        }
)
public class Discount implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "event_id")
    private int eventId;

    @ColumnInfo(name = "code")
    private String code; // Discount code like "EARLY20"

    @ColumnInfo(name = "discount_type")
    private String discountType; // "percentage" or "fixed"

    @ColumnInfo(name = "discount_value")
    private double discountValue; // 20 for 20% or 50000 for fixed amount

    @ColumnInfo(name = "min_purchase")
    private double minPurchase; // Minimum purchase amount to apply

    @ColumnInfo(name = "max_discount")
    private double maxDiscount; // Maximum discount amount (for percentage)

    @ColumnInfo(name = "usage_limit")
    private int usageLimit; // Total number of times this code can be used

    @ColumnInfo(name = "used_count")
    private int usedCount;

    @ColumnInfo(name = "start_date")
    private String startDate;

    @ColumnInfo(name = "end_date")
    private String endDate;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    @ColumnInfo(name = "created_at")
    private String createdAt;

    // Constructors
    public Discount() {}

    @Ignore
    public Discount(int eventId, String code, String discountType, double discountValue,
                    double minPurchase, double maxDiscount, int usageLimit,
                    String startDate, String endDate, String createdAt) {
        this.eventId = eventId;
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minPurchase = minPurchase;
        this.maxDiscount = maxDiscount;
        this.usageLimit = usageLimit;
        this.usedCount = 0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = true;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }

    public double getMinPurchase() { return minPurchase; }
    public void setMinPurchase(double minPurchase) { this.minPurchase = minPurchase; }

    public double getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(double maxDiscount) { this.maxDiscount = maxDiscount; }

    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public double calculateDiscount(double totalAmount) {
        if (!isActive || usedCount >= usageLimit) return 0;
        if (totalAmount < minPurchase) return 0;

        double discount;
        if ("percentage".equals(discountType)) {
            discount = totalAmount * (discountValue / 100);
            if (maxDiscount > 0 && discount > maxDiscount) {
                discount = maxDiscount;
            }
        } else {
            discount = discountValue;
        }
        return Math.min(discount, totalAmount);
    }
}
