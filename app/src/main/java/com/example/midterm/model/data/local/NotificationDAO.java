package com.example.midterm.model.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.midterm.model.dto.NotificationWithEvent;
import com.example.midterm.model.entity.Notification;

import java.util.List;

@Dao
public interface NotificationDAO {

    @Insert
    long insert(Notification notification);

    @Update
    void update(Notification notification);

    @Delete
    void delete(Notification notification);

    // Get all notifications for an event
    @Query("SELECT * FROM notifications WHERE event_id = :eventId ORDER BY created_at DESC")
    LiveData<List<Notification>> getNotificationsByEvent(int eventId);

    // Get all notifications by organizer
    @Query("SELECT * FROM notifications WHERE organizer_id = :organizerId ORDER BY created_at DESC")
    LiveData<List<Notification>> getNotificationsByOrganizer(int organizerId);

    // Get notification by ID
    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    LiveData<Notification> getNotificationById(int notificationId);

    // Get notifications by status
    @Query("SELECT * FROM notifications WHERE organizer_id = :organizerId AND status = :status ORDER BY created_at DESC")
    LiveData<List<Notification>> getNotificationsByStatus(int organizerId, String status);

    // Get notifications by type
    @Query("SELECT * FROM notifications WHERE event_id = :eventId AND notification_type = :type ORDER BY created_at DESC")
    LiveData<List<Notification>> getNotificationsByType(int eventId, String type);

    // Count notifications sent for an event
    @Query("SELECT COUNT(*) FROM notifications WHERE event_id = :eventId AND status = 'sent'")
    LiveData<Integer> countSentNotifications(int eventId);

    // Get recent notifications (last 30 days)
    @Query("SELECT * FROM notifications WHERE organizer_id = :organizerId " +
           "AND created_at >= datetime('now', '-30 days') ORDER BY created_at DESC")
    LiveData<List<Notification>> getRecentNotifications(int organizerId);

    // Update notification status
    @Query("UPDATE notifications SET status = :status, sent_at = :sentAt WHERE id = :notificationId")
    void updateNotificationStatus(int notificationId, String status, String sentAt);

    // Delete notifications for an event
    @Query("DELETE FROM notifications WHERE event_id = :eventId")
    void deleteNotificationsByEvent(int eventId);

    // Get total recipients count for organizer
    @Query("SELECT COALESCE(SUM(recipient_count), 0) FROM notifications WHERE organizer_id = :organizerId AND status = 'sent'")
    LiveData<Integer> getTotalRecipientsByOrganizer(int organizerId);


    // Get notifications for user (from events they have tickets for)
    @Query("SELECT DISTINCT n.* FROM notifications n " +
            "INNER JOIN tickets t ON n.event_id = (SELECT event_id FROM ticket_types WHERE id = t.ticket_type_id) " +
            "WHERE t.buyer_id = :userId AND n.status = 'sent' " +
            "ORDER BY n.sent_at DESC")
    LiveData<List<Notification>> getNotificationsForUser(int userId);

    // Get notifications with event details for user
    @Query("SELECT DISTINCT n.id as notification_id, n.title, n.message, n.sent_at, " +
            "n.notification_type, e.event_name, e.id as event_id " +
            "FROM notifications n " +
            "INNER JOIN events e ON n.event_id = e.id " +
            "INNER JOIN ticket_types tt ON e.id = tt.event_id " +
            "INNER JOIN tickets t ON tt.id = t.ticket_type_id " +
            "WHERE t.buyer_id = :userId AND n.status = 'sent' " +
            "ORDER BY n.sent_at DESC")
    LiveData<List<NotificationWithEvent>> getNotificationsWithEventForUser(int userId);
}
