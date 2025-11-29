package com.example.midterm.view.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.Seat;
import com.example.midterm.model.entity.TicketType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SeatGridAdapter extends RecyclerView.Adapter<SeatGridAdapter.SeatViewHolder> {

    private Context context;
    private List<Seat> seats;
    private OnSeatClickListener listener;
    private List<TicketType> ticketTypes = new ArrayList<>();

    private int[] paletteColors = new int[]{
            Color.parseColor("#F44336"), // Đỏ
            Color.parseColor("#4CAF50"), // Xanh lá
            Color.parseColor("#2196F3"), // Xanh dương
            Color.parseColor("#FF9800"), // Cam
            Color.parseColor("#9C27B0")  // Tím
    };

    public interface OnSeatClickListener {
        void onSeatClick(Seat seat, int position);
    }

    public SeatGridAdapter(Context context, List<Seat> seats, OnSeatClickListener listener) {
        this.context = context;
        this.seats = seats;
        this.listener = listener;
    }

    public void setSeats(List<Seat> newSeats) {
        this.seats = newSeats;
        notifyDataSetChanged();
    }

    // Hàm này nhận danh sách "cọ vẽ" từ Activity
    public void setTicketTypes(List<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes;
        notifyDataSetChanged();
    }

    public void updateSeat(int position, Seat seat) {
        seats.set(position, seat);
        notifyItemChanged(position);
    }

    //Lấy danh sách các ghế đã được gán (để lưu vào DB)
    public List<Seat> getSeatsToSave() {
        // Chỉ trả về các ghế đã được gán TicketType
        return seats.stream()
                .filter(seat -> seat.getTicketTypeID() != null)
                .collect(Collectors.toList());
    }

    // Trả về danh sách ghế hiện tại trong adapter.
    public List<Seat> getSeats() {
        return this.seats;
    }

    // Hàm helper để tìm màu sắc dựa trên ID của Loại vé
    private int getColorForTicketType(Integer ticketTypeId) {
        if (ticketTypeId == null) {
            return Color.GRAY; // Màu dự phòng
        }
        // Tìm vị trí (index) của loại vé
        for (int i = 0; i < ticketTypes.size(); i++) {
            if (ticketTypes.get(i).getId() == ticketTypeId) {
                // Lấy màu tương ứng với vị trí đó
                return paletteColors[i % paletteColors.length];
            }
        }
        return Color.GRAY; // Màu dự phòng
    }


    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_seat, parent, false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat seat = seats.get(position);
        String seatLabel = seat.getSeatRow() + seat.getSeatNumber();
        holder.seatLabel.setText(seatLabel);

        // === LOGIC RENDER MÀU MỚI ===

        //Nếu ghế đã được bán (booked)
        if ("booked".equals(seat.getStatus())) {
            holder.seatLabel.setEnabled(false); // Kích hoạt state_enabled="false" (Màu xám)
            holder.seatLabel.setSelected(false);
            holder.seatLabel.setBackgroundTintList(null); // Xóa "nhuộm" màu động
        }
        // Nếu ghế đã được gán (selected/assigned)
        else if (seat.getTicketTypeID() != null) {
            holder.seatLabel.setEnabled(true);
            holder.seatLabel.setSelected(false); // TẮT state_selected (không dùng màu xanh lá)

            // Lấy màu động (màu của "cọ vẽ")
            int dynamicColor = getColorForTicketType(seat.getTicketTypeID());

            // "Nhuộm" màu nền cho ghế
            holder.seatLabel.setBackgroundTintList(ColorStateList.valueOf(dynamicColor));
            // Đổi chữ sang màu trắng để dễ đọc
            holder.seatLabel.setTextColor(Color.WHITE);
        }
        // Ghế trống (available/unassigned)
        else {
            holder.seatLabel.setEnabled(true);
            holder.seatLabel.setSelected(false); // Trạng thái mặc định (Màu xám nhạt)

            holder.seatLabel.setBackgroundTintList(null); // Xóa mọi "nhuộm" màu
            holder.seatLabel.setTextColor(ContextCompat.getColor(context, R.color.seat_text_default));
        }

        // Bắt sự kiện click
        holder.itemView.setOnClickListener(v -> {
            // Chỉ cho phép click nếu ghế chưa bị bán
            if ("booked".equals(seat.getStatus())) {
                return; // Không cho click
            }
            if (listener != null) {
                listener.onSeatClick(seat, position);
            }
        });
    }
    @Override
    public int getItemCount() {
        return seats.size();
    }

    static class SeatViewHolder extends RecyclerView.ViewHolder {
        TextView seatLabel;
        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            seatLabel = (TextView) itemView; // Vì item_seat chỉ là một TextView
        }
    }
}