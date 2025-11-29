package com.example.midterm.view.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.Guest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuestAdapter extends RecyclerView.Adapter<GuestAdapter.GuestViewHolder> {
    private List<Guest> guests = new ArrayList<>();
    private final Context context;

    public interface OnGuestActionListener {
        void onEditClick(Guest guest);
        void onDeleteClick(Guest guest);
    }
    private final OnGuestActionListener listener;

    public GuestAdapter(Context context, OnGuestActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setGuests(List<Guest> newGuests) {
        List<Guest> reversedList = new ArrayList<>(newGuests);
        Collections.reverse(reversedList);
        this.guests = reversedList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_guest_summary, parent, false);
        return new GuestViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GuestViewHolder holder, int position) {
        final Guest currentGuest = guests.get(position);

        holder.tvGuestName.setText(currentGuest.getName());
        holder.tvGuestRole.setText(currentGuest.getRole());

        // Xử lý sự kiện click
        holder.iconEditGuest.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(currentGuest);
            }
        });

        holder.iconDeleteGuest.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(currentGuest);
            }
        });

        // Bổ sung: Xử lý sự kiện click toàn bộ item (theo item_guest_summary có foreground)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(currentGuest);
            }
        });
    }

    @Override
    public int getItemCount() {
        return guests.size();
    }

    static class GuestViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvGuestName, tvGuestRole;
        private final ImageView iconEditGuest, iconDeleteGuest;
        public GuestViewHolder(View itemView) {
            super(itemView);
            tvGuestName = itemView.findViewById(R.id.tv_guest_name);
            tvGuestRole = itemView.findViewById(R.id.tv_guest_role_label);
            iconEditGuest = itemView.findViewById(R.id.icon_edit_guest);
            iconDeleteGuest = itemView.findViewById(R.id.icon_delete_guest);
        }
    }
}