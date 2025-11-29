package com.example.midterm.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "notifications",
        foreignKeys = @ForeignKey(
                entity = Event.class,
                parentColumns = "id",
                childColumns = "event_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = "event_id")}
)
public class Notification {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "event_id")
    private int eventId;

    @ColumnInfo(name = "organizer_id")
    private int organizerId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "notification_type")
    private String notificationType; // "broadcast", "reminder", "update", "cancellation"

    @ColumnInfo(name = "sent_at")
    private String sentAt;

    @ColumnInfo(name = "recipient_count")
    private int recipientCount;

    @ColumnInfo(name = "status")
    private String status; // "pending", "sent", "failed"

    @ColumnInfo(name = "created_at")
    private String createdAt;

    // Constructors
    public Notification() {}

    @Ignore
    public Notification(int eventId, int organizerId, String title, String message,
                        String notificationType, String sentAt, int recipientCount,
                        String status, String createdAt) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.sentAt = sentAt;
        this.recipientCount = recipientCount;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public int getOrganizerId() { return organizerId; }
    public void setOrganizerId(int organizerId) { this.organizerId = organizerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    public int getRecipientCount() { return recipientCount; }
    public void setRecipientCount(int recipientCount) { this.recipientCount = recipientCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public void setRead(boolean b) {

    }
}
