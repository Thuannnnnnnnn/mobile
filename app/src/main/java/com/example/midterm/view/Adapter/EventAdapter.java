package com.example.midterm.view.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.midterm.R;
import com.example.midterm.model.entity.Event;
import com.bumptech.glide.Glide;

import java.util.ArrayList; // Thêm
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organizer_event_card, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event currentEvent = events.get(position);

        holder.eventNameTextView.setText(currentEvent.getEventName());

        //Load Banner
        Glide.with(holder.itemView.getContext())
                .load(currentEvent.getBannerUrl())
                .placeholder(R.drawable.unnamed_removebg_preview)
                .error(R.drawable.unnamed_removebg_preview)
                .into(holder.bannerImageView);

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentEvent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        final ImageView bannerImageView;
        final TextView eventNameTextView;

        EventViewHolder(View itemView) {
            super(itemView);
            bannerImageView = itemView.findViewById(R.id.img_event_banner);
            eventNameTextView = itemView.findViewById(R.id.tv_event_name_overlay);
        }
    }
}