package com.example.midterm.view.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.Guest; // Import Guest
import java.util.ArrayList;
import java.util.List;

public class GuestDetailAdapter extends RecyclerView.Adapter<GuestDetailAdapter.GuestViewHolder> {

    private Context context;
    private List<Guest> guestList = new ArrayList<>();

    public GuestDetailAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public GuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item_guest.xml đã tạo ở lần trước
        View view = LayoutInflater.from(context).inflate(R.layout.item_guest, parent, false);
        return new GuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuestViewHolder holder, int position) {
        Guest guest = guestList.get(position);

        // Lấy Name và Role từ entity Guest
        holder.tvGuestName.setText(guest.getName());
        holder.tvGuestRole.setText(guest.getRole());
    }

    @Override
    public int getItemCount() {
        return guestList.size();
    }

    // Method để cập nhật dữ liệu
    public void updateData(List<Guest> newGuests) {
        guestList.clear();
        guestList.addAll(newGuests);
        notifyDataSetChanged();
    }

    public static class GuestViewHolder extends RecyclerView.ViewHolder {
        TextView tvGuestName, tvGuestRole;

        public GuestViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID từ item_guest.xml
            tvGuestName = itemView.findViewById(R.id.tv_guest_name);
            tvGuestRole = itemView.findViewById(R.id.tv_guest_role);
        }
    }
}
