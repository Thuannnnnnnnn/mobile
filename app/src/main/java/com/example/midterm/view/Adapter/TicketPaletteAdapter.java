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
import com.example.midterm.model.entity.TicketType;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TicketPaletteAdapter extends RecyclerView.Adapter<TicketPaletteAdapter.PaletteViewHolder> {

    private List<TicketType> ticketTypes = new ArrayList<>();
    private Context context;
    private OnPaletteClickListener listener;
    private int selectedPosition = -1; // Vị trí đang được chọn

    // Mảng màu (Bạn có thể tự định nghĩa)
    private int[] paletteColors = new int[]{
            Color.parseColor("#FF9800"), // Cam
            Color.parseColor("#4CAF50"), // Xanh lá
            Color.parseColor("#2196F3"), // Xanh dương
            Color.parseColor("#F44336"), // Cam
            Color.parseColor("#9C27B0")  // Tím
    };

    public interface OnPaletteClickListener {
        void onPaletteClick(TicketType ticketType, int position);
    }

    public TicketPaletteAdapter(Context context, OnPaletteClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setTicketTypes(List<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldPosition); // Cập nhật item cũ (bỏ chọn)
        notifyItemChanged(selectedPosition); // Cập nhật item mới (chọn)
    }

    @NonNull
    @Override
    public PaletteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket_type_palette, parent, false);
        return new PaletteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaletteViewHolder holder, int position) {
        TicketType ticketType = ticketTypes.get(position);

        holder.tvName.setText(ticketType.getCode());
        holder.tvPrice.setText(formatPrice(ticketType.getPrice()));

        // Gán màu cho "cọ vẽ"
        int color = paletteColors[position % paletteColors.length]; // Lấy màu xoay vòng
        holder.colorView.setBackgroundColor(color);

        // Xử lý viền (stroke) khi được chọn
        if (position == selectedPosition) {
            holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.colorSuccess));
            holder.card.setStrokeWidth(6);
        } else {
            holder.card.setStrokeColor(Color.TRANSPARENT);
            holder.card.setStrokeWidth(0);
        }

        // Bắt sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaletteClick(ticketType, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ticketTypes.size();
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    static class PaletteViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        View colorView;
        TextView tvName, tvPrice;

        public PaletteViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            colorView = itemView.findViewById(R.id.view_ticket_color);
            tvName = itemView.findViewById(R.id.tv_ticket_type_name);
            tvPrice = itemView.findViewById(R.id.tv_ticket_type_price);
        }
    }
}