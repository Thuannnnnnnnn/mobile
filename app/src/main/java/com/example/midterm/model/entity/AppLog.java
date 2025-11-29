package com.example.midterm.model.entity;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class AppLog {
    private String logId;       // ID của log (tự sinh)
    private String actionType;  // Loại hành động: "REGISTER", "LOGIN", "BUY_TICKET", "CHECK_IN"
    private String userId;      // Ai thực hiện? (ID hoặc Email)
    private String description; // Chi tiết: "Mua vé sự kiện A", "Check-in thành công"

    @ServerTimestamp
    private Date timestamp;     // Thời gian thực hiện (Firebase tự điền)

    // Constructor rỗng (Bắt buộc cho Firebase)
    public AppLog() {}

    public AppLog(String actionType, String userId, String description) {
        this.actionType = actionType;
        this.userId = userId;
        this.description = description;
    }

    // Getters và Setters
    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}