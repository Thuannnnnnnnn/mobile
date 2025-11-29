package com.example.midterm.model.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.NotificationDAO;
import com.example.midterm.model.dto.NotificationWithEvent;
import com.example.midterm.model.entity.Notification;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class NotificationRepository {

    private final NotificationDAO notificationDAO;
    private final ExecutorService executorService;

    public NotificationRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        notificationDAO = db.notificationDAO();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Insert notification
    public void insertNotification(Notification notification, Consumer<Long> callback) {
        executorService.execute(() -> {
            long id = notificationDAO.insert(notification);
            if (callback != null) callback.accept(id);
        });
    }

    // Update notification
    public void updateNotification(Notification notification) {
        executorService.execute(() -> notificationDAO.update(notification));
    }

    // Delete notification
    public void deleteNotification(Notification notification) {
        executorService.execute(() -> notificationDAO.delete(notification));
    }

    // Get notifications by event
    public LiveData<List<Notification>> getNotificationsByEvent(int eventId) {
        return notificationDAO.getNotificationsByEvent(eventId);
    }

    // Get notifications by organizer
    public LiveData<List<Notification>> getNotificationsByOrganizer(int organizerId) {
        return notificationDAO.getNotificationsByOrganizer(organizerId);
    }

    // Get notification by ID
    public LiveData<Notification> getNotificationById(int notificationId) {
        return notificationDAO.getNotificationById(notificationId);
    }

    // Get notifications by status
    public LiveData<List<Notification>> getNotificationsByStatus(int organizerId, String status) {
        return notificationDAO.getNotificationsByStatus(organizerId, status);
    }

    // Get notifications by type
    public LiveData<List<Notification>> getNotificationsByType(int eventId, String type) {
        return notificationDAO.getNotificationsByType(eventId, type);
    }

    // Count sent notifications
    public LiveData<Integer> countSentNotifications(int eventId) {
        return notificationDAO.countSentNotifications(eventId);
    }

    // Get recent notifications
    public LiveData<List<Notification>> getRecentNotifications(int organizerId) {
        return notificationDAO.getRecentNotifications(organizerId);
    }

    // Update notification status
    public void updateNotificationStatus(int notificationId, String status, String sentAt) {
        executorService.execute(() -> notificationDAO.updateNotificationStatus(notificationId, status, sentAt));
    }

    // Delete notifications by event
    public void deleteNotificationsByEvent(int eventId) {
        executorService.execute(() -> notificationDAO.deleteNotificationsByEvent(eventId));
    }

    // Get total recipients by organizer
    public LiveData<Integer> getTotalRecipientsByOrganizer(int organizerId) {
        return notificationDAO.getTotalRecipientsByOrganizer(organizerId);
    }

    // Get notifications with event details for user
    public LiveData<List<NotificationWithEvent>> getNotificationsWithEventForUser(int userId) {
        return notificationDAO.getNotificationsWithEventForUser(userId);
    }
}
