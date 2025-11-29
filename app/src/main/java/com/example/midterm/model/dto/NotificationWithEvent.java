package com.example.midterm.model.dto;

import androidx.room.ColumnInfo;

public class NotificationWithEvent {
    @ColumnInfo(name = "notification_id")
    public int notificationId;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "message")
    public String message;

    @ColumnInfo(name = "sent_at")
    public String sentAt;

    @ColumnInfo(name = "notification_type")
    public String notificationType;

    @ColumnInfo(name = "event_name")
    public String eventName;

    @ColumnInfo(name = "event_id")
    public int eventId;
}
