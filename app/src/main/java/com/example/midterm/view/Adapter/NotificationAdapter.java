package com.example.midterm.view.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.dto.NotificationWithEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationWithEvent> notifications = new ArrayList<>();
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationWithEvent notification);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<NotificationWithEvent> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationWithEvent notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvEventName;
        private TextView tvSentTime;
        private TextView tvTitle;
        private TextView tvMessage;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_notification_icon);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvSentTime = itemView.findViewById(R.id.tv_sent_time);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationClick(notifications.get(position));
                }
            });
        }

        public void bind(NotificationWithEvent notification) {
            tvEventName.setText(notification.eventName);
            tvTitle.setText(notification.title);
            tvMessage.setText(notification.message);
            tvSentTime.setText(getTimeAgo(notification.sentAt));

            // Set icon based on notification type
            if (notification.notificationType != null) {
                switch (notification.notificationType) {
                    case "broadcast":
                        ivIcon.setImageResource(R.drawable.info);
                        break;
                    case "reminder":
                        ivIcon.setImageResource(R.drawable.time__1_);
                        break;
                    case "update":
                        ivIcon.setImageResource(R.drawable.info);
                        break;
                    case "cancellation":
                        ivIcon.setImageResource(R.drawable.cancel);
                        break;
                    default:
                        ivIcon.setImageResource(R.drawable.info);
                        break;
                }
            }
        }

        private String getTimeAgo(String dateTimeString) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date sentDate = sdf.parse(dateTimeString);
                if (sentDate == null) return "";

                long diffInMillis = new Date().getTime() - sentDate.getTime();
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
                long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
                long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);

                if (minutes < 1) {
                    return "Vừa xong";
                } else if (minutes < 60) {
                    return minutes + " phút trước";
                } else if (hours < 24) {
                    return hours + " giờ trước";
                } else if (days < 7) {
                    return days + " ngày trước";
                } else {
                    // Format as date if older than 7 days
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    return displayFormat.format(sentDate);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return "";
            }
        }
    }
}