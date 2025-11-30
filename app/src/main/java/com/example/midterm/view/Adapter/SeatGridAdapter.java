package com.example.midterm.view.Adapter;

import android.content.Context;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatGridAdapter extends RecyclerView.Adapter<SeatGridAdapter.SeatViewHolder> {

    private Context context;
    private List<Seat> seatList;
    private List<TicketType> ticketTypes;
    private OnSeatClickListener listener;

    // Map lưu màu sắc theo TicketTypeID để tra cứu nhanh
    private Map<Integer, Integer> typeColorMap = new HashMap<>();

    public interface OnSeatClickListener {
        void onSeatClick(Seat seat, int position);
    }

    public SeatGridAdapter(Context context, List<Seat> seatList, OnSeatClickListener listener) {
        this.context = context;
        this.seatList = seatList;
        this.listener = listener;
    }

    public void setSeats(List<Seat> seats) {
        this.seatList = seats;
        notifyDataSetChanged();
    }

    // Cập nhật danh sách loại vé và tạo bảng màu
    public void setTicketTypes(List<TicketType> types) {
        this.ticketTypes = types;
        typeColorMap.clear();
        // Mảng màu mặc định nếu TicketType không có màu
        int[] defaultColors = {
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#2196F3"), // Blue
                Color.parseColor("#FF9800"), // Orange
                Color.parseColor("#9C27B0")  // Purple
        };

        for (int i = 0; i < types.size(); i++) {
            // Ở đây giả sử TicketType chưa có trường color, ta gán màu theo index
            // Nếu bạn đã thêm trường color vào TicketType, hãy dùng: type.getColor()
            typeColorMap.put(types.get(i).getId(), defaultColors[i % defaultColors.length]);
        }
        notifyDataSetChanged();
    }

    public List<Seat> getSeats() {
        return seatList;
    }

    public void updateSeat(int position, Seat seat) {
        seatList.set(position, seat);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_seat, parent, false);
        // Ép kích thước ghế thành hình vuông cố định để ZoomLayout hoạt động tốt
        // Ví dụ: 40dp x 40dp
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat seat = seatList.get(position);

        // Xử lý ghế ẩn (Lối đi)
        if ("hidden".equals(seat.getStatus())) {
            holder.itemView.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(null);
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }

        // Hiển thị tên ghế (VD: A1)
        String seatLabel = seat.getSeatRow() + seat.getSeatNumber();
        holder.tvSeatName.setText(seatLabel);

        // Xử lý màu sắc dựa trên trạng thái
        if (seat.getTicketTypeID() != null && typeColorMap.containsKey(seat.getTicketTypeID())) {
            // Đã được gán vé -> Tô màu theo loại vé
            int color = typeColorMap.get(seat.getTicketTypeID());
            holder.viewBackground.setBackgroundColor(color);
            holder.tvSeatName.setTextColor(Color.WHITE);
        } else {
            // Chưa gán vé (Unassigned) -> Màu xám nhạt
            holder.viewBackground.setBackgroundColor(Color.parseColor("#EEEEEE"));
            holder.tvSeatName.setTextColor(Color.parseColor("#AAAAAA"));
        }

        // Xử lý click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSeatClick(seat, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return seatList != null ? seatList.size() : 0;
    }

    public static class SeatViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeatName;
        View viewBackground;

        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeatName = itemView.findViewById(R.id.tv_seat_name);
            viewBackground = itemView.findViewById(R.id.view_seat_bg);
        }
    }
}