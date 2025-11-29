package com.example.midterm.model.data.remote;

import android.util.Log;

import com.example.midterm.model.entity.Account;
import com.example.midterm.model.entity.AppLog; // Import AppLog
import com.example.midterm.model.entity.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FirestoreHelper {
    private final FirebaseFirestore db;
    private static final String TAG = "FirestoreHelper";
    private static final String EVENTS_COLLECTION = "events";
    private static final String ACCOUNTS_COLLECTION = "accounts";
    private static final String APP_LOGS_COLLECTION = "app_logs"; // Tên collection cho AppLog

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // --- Event Sync Methods ---
    public void syncEventToCloud(Event event) {
        if (event == null) return;
        String docId = String.valueOf(event.getId());
        db.collection(EVENTS_COLLECTION)
                .document(docId)
                .set(event)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Đồng bộ Event " + docId + " lên Cloud thành công!"))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi đồng bộ Event lên Cloud", e));
    }

    public void listenForEventUpdates(Consumer<List<Event>> onUpdate) {
        db.collection(EVENTS_COLLECTION)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Lắng nghe dữ liệu Event thất bại.", e);
                        return;
                    }
                    List<Event> events = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            try {
                                Event event = doc.toObject(Event.class);
                                events.add(event);
                            } catch (Exception ex) {
                                Log.e(TAG, "Lỗi parse data Event từ Firebase", ex);
                            }
                        }
                    }
                    if (onUpdate != null) {
                        onUpdate.accept(events);
                    }
                });
    }

    public void deleteEventFromCloud(long eventId) {
        db.collection(EVENTS_COLLECTION)
                .document(String.valueOf(eventId))
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Xóa Event trên Cloud thành công!"))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi xóa Event trên Cloud", e));
    }

    // --- Account Sync Methods ---
    public void listenForAccountUpdates(Consumer<List<Account>> onAccountsReceived) {
        db.collection(ACCOUNTS_COLLECTION)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed for accounts: " + error.getMessage());
                        return;
                    }
                    List<Account> accounts = new ArrayList<>();
                    if (value != null && !value.isEmpty()) {
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Account account = doc.toObject(Account.class);
                                accounts.add(account);
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document to Account: " + e.getMessage());
                            }
                        }
                    }
                    onAccountsReceived.accept(accounts);
                });
    }

    public void syncAccountToCloud(Account account) {
        if (account.getId() != 0) {
            db.collection(ACCOUNTS_COLLECTION).document(String.valueOf(account.getId()))
                    .set(account)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Account synced successfully: " + account.getId()))
                    .addOnFailureListener(e -> Log.e(TAG, "Error syncing account " + account.getId() + " to Cloud: " + e.getMessage()));
        } else {
            Log.w(TAG, "Cannot sync account with invalid ID to Cloud: " + account);
        }
    }

    // --- AppLog Sync Methods ---
    public void syncAppLogToCloud(AppLog appLog) {
        if (appLog == null) {
            Log.w(TAG, "Cannot sync null AppLog to Cloud.");
            return;
        }
        // Firestore thường tự tạo ID cho logs. Sử dụng add() thay vì set() với document()
        db.collection(APP_LOGS_COLLECTION)
                .add(appLog)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "AppLog synced successfully with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error syncing AppLog to Cloud", e));
    }

    // Listener cho AppLog (để hoàn thiện tính năng đồng bộ hai chiều nếu cần)
    public void listenForAppLogUpdates(Consumer<List<AppLog>> onAppLogsReceived) {
        db.collection(APP_LOGS_COLLECTION)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed for AppLogs: " + error.getMessage());
                        return;
                    }
                    List<AppLog> appLogs = new ArrayList<>();
                    if (value != null && !value.isEmpty()) {
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                AppLog appLog = doc.toObject(AppLog.class);
                                // Bạn có thể muốn set FireStore document ID vào appLog nếu cần
                                // appLog.setFirestoreDocId(doc.getId());
                                appLogs.add(appLog);
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document to AppLog: " + e.getMessage());
                            }
                        }
                    }
                    onAppLogsReceived.accept(appLogs);
                });
    }
}
