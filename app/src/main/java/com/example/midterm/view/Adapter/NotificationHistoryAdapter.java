package com.example.midterm.view.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.Notification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationHistoryAdapter extends RecyclerView.Adapter<NotificationHistoryAdapter.ViewHolder> {

    private Context context;
    private List<Notification> notifications;

    public NotificationHistoryAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvDate.setText(formatDate(notification.getSentAt()));
        holder.tvRecipients.setText(String.format(Locale.getDefault(), "%d người nhận", notification.getRecipientCount()));
        holder.tvType.setText(getTypeDisplay(notification.getNotificationType()));

        // Set status indicator
        if ("sent".equals(notification.getStatus())) {
            holder.tvStatus.setText("Đã gửi");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_active);
        } else if ("failed".equals(notification.getStatus())) {
            holder.tvStatus.setText("Thất bại");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_past);
        } else {
            holder.tvStatus.setText("Đang chờ");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
        }
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    public void updateData(List<Notification> newData) {
        this.notifications = newData;
        notifyDataSetChanged();
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private String getTypeDisplay(String type) {
        switch (type) {
            case "reminder":
                return "Nhắc nhở";
            case "update":
                return "Cập nhật";
            case "cancellation":
                return "Hủy sự kiện";
            default:
                return "Thông báo chung";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvDate, tvRecipients, tvType, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvRecipients = itemView.findViewById(R.id.tv_recipients);
            tvType = itemView.findViewById(R.id.tv_type);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}
