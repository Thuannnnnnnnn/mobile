package com.example.midterm.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.midterm.model.dto.NotificationWithEvent;
import com.example.midterm.model.entity.Notification;
import com.example.midterm.model.repository.NotificationRepository;

import java.util.List;
import java.util.function.Consumer;

public class NotificationViewModel extends AndroidViewModel {

    private final NotificationRepository notificationRepository;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        notificationRepository = new NotificationRepository(application);
    }

    ///Organizer
    public void insertNotification(Notification notification, Consumer<Long> callback) {
        notificationRepository.insertNotification(notification, callback);
    }

    public void updateNotification(Notification notification) {
        notificationRepository.updateNotification(notification);
    }

    public void deleteNotification(Notification notification) {
        notificationRepository.deleteNotification(notification);
    }

    /// User
    /// Lấy những thông báo của 1 event
    public LiveData<List<Notification>> getNotificationsByEvent(int eventId) {
        return notificationRepository.getNotificationsByEvent(eventId);
    }

    // Get notifications by organizer
    public LiveData<List<Notification>> getNotificationsByOrganizer(int organizerId) {
        return notificationRepository.getNotificationsByOrganizer(organizerId);
    }

    // Get notification by ID
    public LiveData<Notification> getNotificationById(int notificationId) {
        return notificationRepository.getNotificationById(notificationId);
    }

    // Get notifications by status
    public LiveData<List<Notification>> getNotificationsByStatus(int organizerId, String status) {
        return notificationRepository.getNotificationsByStatus(organizerId, status);
    }

    // Get notifications by type
    public LiveData<List<Notification>> getNotificationsByType(int eventId, String type) {
        return notificationRepository.getNotificationsByType(eventId, type);
    }

    // Count sent notifications
    public LiveData<Integer> countSentNotifications(int eventId) {
        return notificationRepository.countSentNotifications(eventId);
    }

    // Get recent notifications
    public LiveData<List<Notification>> getRecentNotifications(int organizerId) {
        return notificationRepository.getRecentNotifications(organizerId);
    }

    // Update notification status
    public void updateNotificationStatus(int notificationId, String status, String sentAt) {
        notificationRepository.updateNotificationStatus(notificationId, status, sentAt);
    }

    // Delete notifications by event
    public void deleteNotificationsByEvent(int eventId) {
        notificationRepository.deleteNotificationsByEvent(eventId);
    }

    // Get total recipients by organizer
    public LiveData<Integer> getTotalRecipientsByOrganizer(int organizerId) {
        return notificationRepository.getTotalRecipientsByOrganizer(organizerId);
    }
    // Get notifications with event details for user
    public LiveData<List<NotificationWithEvent>> getNotificationsWithEventForUser(int userId) {
        return notificationRepository.getNotificationsWithEventForUser(userId);
    }
}
