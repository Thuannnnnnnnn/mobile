package com.example.midterm.view.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.midterm.R;
import com.example.midterm.model.entity.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventDashboardAdapter extends RecyclerView.Adapter<EventDashboardAdapter.EventViewHolder> {

    private Context context;
    private List<Event> events;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
        void onEditClick(Event event);
        void onSalesReportClick(Event event);
        void onBroadcastClick(Event event);
    }

    public EventDashboardAdapter(Context context, List<Event> events, OnEventClickListener listener) {
        this.context = context;
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_dashboard, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        // Set event name
        holder.tvEventName.setText(event.getEventName());

        // Set location
        holder.tvLocation.setText(event.getLocation());

        // Set date
        holder.tvDate.setText(formatDate(event.getStartDate()));

        // Set genre/category
        holder.tvGenre.setText(event.getGenre());

        // Set status
        String status = getEventStatus(event.getEndDate());
        holder.tvStatus.setText(status);

        // Set status color
        if (status.equals("Đang diễn ra") || status.equals("Sắp diễn ra")) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_active);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_past);
        }

        // Load banner
        if (event.getBannerUrl() != null && !event.getBannerUrl().isEmpty()) {
            Glide.with(context)
                    .load(event.getBannerUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(holder.imgBanner);
        } else {
            holder.imgBanner.setImageResource(R.drawable.placeholder_image);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(event);
            }
        });

        holder.btnSalesReport.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSalesReportClick(event);
            }
        });

        holder.btnBroadcast.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBroadcastClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private String getEventStatus(String endDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date endDate = sdf.parse(endDateStr);
            Date now = new Date();

            if (endDate.before(now)) {
                return "Đã kết thúc";
            } else {
                // Check if event is happening now or upcoming
                return "Sắp diễn ra";
            }
        } catch (ParseException e) {
            return "Không xác định";
        }
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBanner;
        TextView tvEventName, tvLocation, tvDate, tvGenre, tvStatus;
        ImageButton btnEdit, btnSalesReport, btnBroadcast;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBanner = itemView.findViewById(R.id.img_event_banner);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvGenre = itemView.findViewById(R.id.tv_genre);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnSalesReport = itemView.findViewById(R.id.btn_sales_report);
            btnBroadcast = itemView.findViewById(R.id.btn_broadcast);
        }
    }
}
