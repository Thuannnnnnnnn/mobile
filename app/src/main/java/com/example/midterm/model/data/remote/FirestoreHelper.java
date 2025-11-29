package com.example.midterm.model.data.remote;

import android.util.Log;

import com.example.midterm.model.entity.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FirestoreHelper {
    private final FirebaseFirestore db;
    private static final String TAG = "FirestoreHelper";

    public FirestoreHelper() {
        // Khởi tạo Firestore instance
        db = FirebaseFirestore.getInstance();
    }

    // 1. Đồng bộ sự kiện lên Cloud (Sync Up)
    // Gọi hàm này sau khi Insert vào Room thành công
    public void syncEventToCloud(Event event) {
        if (event == null) return;

        // Sử dụng ID của Event làm Document ID trên Firebase để dễ mapping
        // Lưu ý: ID của Room là int tự tăng, nên convert sang String
        String docId = String.valueOf(event.getId());

        db.collection("events")
                .document(docId)
                .set(event)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Đồng bộ Event " + docId + " lên Cloud thành công!"))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi đồng bộ Event lên Cloud", e));
    }

    // 2. Lắng nghe dữ liệu thay đổi từ Cloud (Realtime Sync Down)
    // Gọi hàm này ở ViewModel hoặc Repository khi khởi tạo
    public void listenForEventUpdates(Consumer<List<Event>> onUpdate) {
        db.collection("events")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Lắng nghe dữ liệu thất bại.", e);
                        return;
                    }

                    List<Event> events = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            // Firebase tự động map JSON sang Object Event
                            // Yêu cầu class Event phải có constructor rỗng (public Event() {})
                            try {
                                Event event = doc.toObject(Event.class);
                                events.add(event);
                            } catch (Exception ex) {
                                Log.e(TAG, "Lỗi parse data từ Firebase", ex);
                            }
                        }
                    }

                    // Trả danh sách mới về cho UI/Repository cập nhật
                    if (onUpdate != null) {
                        onUpdate.accept(events);
                    }
                });
    }

    // 3. Xóa sự kiện trên Cloud
    public void deleteEventFromCloud(long eventId) {
        db.collection("events")
                .document(String.valueOf(eventId))
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Xóa Event trên Cloud thành công!"))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi xóa Event trên Cloud", e));
    }
}